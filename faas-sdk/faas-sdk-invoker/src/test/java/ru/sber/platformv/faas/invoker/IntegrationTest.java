// Copyright 2022 АО «СберТех»
//
// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ru.sber.platformv.faas.invoker;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.truth.Expect;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import ru.sber.platformv.faas.invoker.runner.Invoker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.util.stream.Collectors.toList;

/**
 * Integration test that starts up a web server running the Function Framework and sends HTTP
 * requests to it.
 */
public class IntegrationTest {
    @Rule
    public final Expect expect = Expect.create();
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public final TestName testName = new TestName();

    private static final String SERVER_READY_STRING = "Started ServerConnector";

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private static int serverPort;

    /**
     * Each test method will start up a server on the same port, make one or more HTTP requests to
     * that port, then kill the server. So the port should be free when the next test method runs.
     */
    @BeforeClass
    public static void allocateServerPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            serverPort = serverSocket.getLocalPort();
        }
    }

    /**
     * Description of a test case. When we send an HTTP POST to the given {@link #url()} in the
     * server, with the given {@link #requestContent()} ()} as the body of the POST, then we expect to
     * get back the given {@link #expectedResponseText()} in the body of the response.
     */
    @AutoValue
    abstract static class TestCase {

        abstract String url();

        abstract ContentProvider requestContent();

        abstract int expectedResponseCode();

        abstract Optional<String> expectedResponseText();

        abstract Optional<JsonObject> expectedJson();

        abstract Optional<String> expectedContentType();

        abstract Optional<String> expectedOutput();

        abstract Optional<String> httpContentType();

        abstract ImmutableMap<String, String> httpHeaders();

        abstract Optional<File> snoopFile();

        static Builder builder() {
            return new AutoValue_IntegrationTest_TestCase.Builder()
                .setUrl("/")
                .setRequestText("")
                .setExpectedResponseCode(HttpStatus.OK_200)
                .setExpectedResponseText("")
                .setHttpContentType("text/plain")
                .setHttpHeaders(ImmutableMap.of());
        }

        @AutoValue.Builder
        abstract static class Builder {

            abstract Builder setUrl(String x);

            abstract Builder setRequestContent(ContentProvider x);

            Builder setRequestText(String text) {
                return setRequestContent(new StringContentProvider(text));
            }

            abstract Builder setExpectedResponseCode(int x);

            abstract Builder setExpectedResponseText(String x);

            abstract Builder setExpectedResponseText(Optional<String> x);

            abstract Builder setExpectedContentType(String x);

            abstract Builder setExpectedOutput(String x);

            abstract Builder setExpectedJson(JsonObject x);

            abstract Builder setHttpContentType(String x);

            abstract Builder setHttpContentType(Optional<String> x);

            abstract Builder setHttpHeaders(ImmutableMap<String, String> x);

            abstract Builder setSnoopFile(File x);

            abstract TestCase build();
        }
    }

    private static String fullTarget(String nameWithoutPackage) {
        return "ru.sber.platformv.faas.invoker.testfunctions." + nameWithoutPackage;
    }

    private static final TestCase FAVICON_TEST_CASE =
        TestCase.builder()
            .setUrl("/favicon.ico?foo=bar")
            .setExpectedResponseCode(HttpStatus.NOT_FOUND_404)
            .setExpectedResponseText(Optional.empty())
            .build();

    private static final TestCase ROBOTS_TXT_TEST_CASE =
        TestCase.builder()
            .setUrl("/robots.txt?foo=bar")
            .setExpectedResponseCode(HttpStatus.NOT_FOUND_404)
            .setExpectedResponseText(Optional.empty())
            .build();

    @Test
    public void helloWorld() throws Exception {
        testHttpFunction(
            fullTarget("HelloWorld"),
            ImmutableList.of(
                TestCase.builder().setExpectedResponseText("hello\n").build(),
                FAVICON_TEST_CASE,
                ROBOTS_TXT_TEST_CASE));
    }

    @Test
    public void exceptionHttp() throws Exception {
        String exceptionExpectedOutput =
            "Failed to execute ru.sber.platformv.faas.invoker.testfunctions.ExceptionHttp\n"
                + "java.lang.RuntimeException: exception thrown for test";
        testHttpFunction(
            fullTarget("ExceptionHttp"),
            ImmutableList.of(
                TestCase.builder()
                    .setExpectedResponseCode(500)
                    .setExpectedOutput(exceptionExpectedOutput)
                    .build()));
    }

    @Test
    public void echo() throws Exception {
        String testText = "hello\nworld\n";
        testHttpFunction(
            fullTarget("Echo"),
            ImmutableList.of(
                TestCase.builder()
                    .setRequestText(testText)
                    .setExpectedResponseText(testText)
                    .setExpectedContentType("text/plain")
                    .build(),
                TestCase.builder()
                    .setHttpContentType("application/octet-stream")
                    .setRequestText(testText)
                    .setExpectedResponseText(testText)
                    .setExpectedContentType("application/octet-stream")
                    .build()));
    }

    @Test
    public void echoUrl() throws Exception {
        String[] testUrls = {"/", "/foo/bar", "/?foo=bar&baz=buh", "/foo?bar=baz"};
        List<TestCase> testCases =
            Arrays.stream(testUrls)
                .map(url -> TestCase.builder().setUrl(url).setExpectedResponseText(url + "\n").build())
                .collect(toList());
        testHttpFunction(fullTarget("EchoUrl"), testCases);
    }

    @Test
    public void stackDriverLogging() throws Exception {
        String simpleExpectedOutput =
            "blim";
        TestCase simpleTestCase =
            TestCase.builder().setUrl("/?message=blim").setExpectedOutput(simpleExpectedOutput).build();
        String quotingExpectedOutput = "foo\nbar";
        TestCase quotingTestCase =
            TestCase.builder()
                .setUrl("/?message=" + URLEncoder.encode("foo\nbar\"", "UTF-8"))
                .setExpectedOutput(quotingExpectedOutput)
                .build();
        String exceptionExpectedOutput =
            "oops\njava.lang.Exception: disaster\n"
                + "\tat ru.sber.platformv.faas.invoker.testfunctions.Log.service(Log.java:";
        TestCase exceptionTestCase =
            TestCase.builder()
                .setUrl("/?message=oops&level=severe&exception=disaster")
                .setExpectedOutput(exceptionExpectedOutput)
                .build();
        testHttpFunction(
            fullTarget("Log"), ImmutableList.of(simpleTestCase, quotingTestCase, exceptionTestCase));
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    @Test
    public void nested() throws Exception {
        String testText = "sic transit gloria mundi";
        testHttpFunction(
            fullTarget("Nested.Echo"),
            ImmutableList.of(
                TestCase.builder().setRequestText(testText).setExpectedResponseText(testText).build()));
    }

    @Test
    public void packageless() throws Exception {
        testHttpFunction(
            "PackagelessHelloWorld",
            ImmutableList.of(TestCase.builder().setExpectedResponseText("hello, world\n").build()));
    }

    @Test
    public void multipart() throws Exception {
        MultiPartContentProvider multiPartProvider = new MultiPartContentProvider();
        byte[] bytes = new byte[17];
        multiPartProvider.addFieldPart("bytes", new BytesContentProvider(bytes), new HttpFields());
        String string = "1234567890";
        multiPartProvider.addFieldPart("string", new StringContentProvider(string), new HttpFields());
        String expectedResponse =
            "part bytes type application/octet-stream length 17\n"
                + "part string type text/plain;charset=UTF-8 length 10\n";
        testHttpFunction(
            fullTarget("Multipart"),
            ImmutableList.of(
                TestCase.builder()
                    .setHttpContentType(Optional.empty())
                    .setRequestContent(multiPartProvider)
                    .setExpectedResponseText(expectedResponse)
                    .build()));
    }

    private File snoopFile() throws IOException {
        return temporaryFolder.newFile(testName.getMethodName() + ".txt");
    }

    /**
     * Any runtime class that user code shouldn't be able to see.
     */
    private static final Class<?> INTERNAL_CLASS = HttpFunctionExecutor.class;

    private String functionJarString() throws IOException {
        Path functionJarTargetDir = Paths.get("../faas-sdk-testfunction/target");
        Pattern functionJarPattern =
            Pattern.compile("faas-sdk-testfunction-.*-tests\\.jar");
        List<Path> functionJars =
            Files.list(functionJarTargetDir)
                .map(path -> path.getFileName().toString())
                .filter(s -> functionJarPattern.matcher(s).matches())
                .map(s -> functionJarTargetDir.resolve(s))
                .collect(toList());
        assertWithMessage("Number of jars in %s matching %s", functionJarTargetDir, functionJarPattern)
            .that(functionJars)
            .hasSize(1);
        return Iterables.getOnlyElement(functionJars).toString();
    }

    /**
     * Tests that if we launch an HTTP function with {@code --classpath}, then the function code
     * cannot see the classes from the runtime. This is allows us to avoid conflicts between versions
     * of libraries that we use in the runtime and different versions of the same libraries that the
     * function might use.
     */
    @Test
    public void classpathOptionHttp() throws Exception {
        TestCase testCase =
            TestCase.builder()
                .setUrl("/?class=" + INTERNAL_CLASS.getName())
                .setExpectedResponseText("OK")
                .build();
        testFunction(
            SignatureType.HTTP,
            "com.example.functionjar.Foreground",
            ImmutableList.of("--classpath", functionJarString()),
            ImmutableList.of(testCase));
    }

    private void checkSnoopFile(TestCase testCase) throws IOException {
        File snoopFile = testCase.snoopFile().get();
        JsonObject expectedJson = testCase.expectedJson().get();
        String snooped = new String(Files.readAllBytes(snoopFile.toPath()), StandardCharsets.UTF_8);
        Gson gson = new Gson();
        JsonObject snoopedJson = gson.fromJson(snooped, JsonObject.class);
        expect.withMessage("Testing with %s", testCase).that(snoopedJson).isEqualTo(expectedJson);
    }

    private void testHttpFunction(String target, List<TestCase> testCases) throws Exception {
        testFunction(SignatureType.HTTP, target, ImmutableList.of(), testCases);
    }

    private void testFunction(
        SignatureType signatureType,
        String target,
        ImmutableList<String> extraArgs,
        List<TestCase> testCases)
        throws Exception {
        ServerProcess serverProcess = startServer(signatureType, target, extraArgs);
        try {
            HttpClient httpClient = new HttpClient();
            httpClient.start();
            for (TestCase testCase : testCases) {
                testCase.snoopFile().ifPresent(File::delete);
                String uri = "http://localhost:" + serverPort + testCase.url();
                Request request = httpClient.POST(uri);
                testCase
                    .httpContentType()
                    .ifPresent(contentType -> request.header(HttpHeader.CONTENT_TYPE, contentType));
                testCase.httpHeaders().forEach((header, value) -> request.header(header, value));
                request.content(testCase.requestContent());
                ContentResponse response = request.send();
                expect
                    .withMessage("Response to %s is %s %s", uri, response.getStatus(), response.getReason())
                    .that(response.getStatus())
                    .isEqualTo(testCase.expectedResponseCode());
                testCase
                    .expectedResponseText()
                    .ifPresent(text -> expect.that(response.getContentAsString()).isEqualTo(text));
                testCase
                    .expectedContentType()
                    .ifPresent(type -> expect.that(response.getMediaType()).isEqualTo(type));
                if (testCase.snoopFile().isPresent()) {
                    checkSnoopFile(testCase);
                }
            }
        } finally {
            serverProcess.close();
        }
        for (TestCase testCase : testCases) {
            testCase
                .expectedOutput()
                .ifPresent(output -> expect.that(serverProcess.output()).contains(output));
        }
        // Wait for the output monitor task to terminate. If it threw an exception, we will get an
        // ExecutionException here.
        serverProcess.outputMonitorResult().get();
    }

    private enum SignatureType {
        HTTP("http");

        private final String name;

        SignatureType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class ServerProcess implements AutoCloseable {
        private final Process process;
        private final Future<?> outputMonitorResult;
        private final StringBuilder output;

        ServerProcess(Process process, Future<?> outputMonitorResult, StringBuilder output) {
            this.process = process;
            this.outputMonitorResult = outputMonitorResult;
            this.output = output;
        }

        Process process() {
            return process;
        }

        Future<?> outputMonitorResult() {
            return outputMonitorResult;
        }

        String output() {
            synchronized (output) {
                return output.toString();
            }
        }

        @Override
        public void close() {
            process().destroy();
            try {
                process().waitFor();
            } catch (InterruptedException e) {
                // Should not happen.
            }
        }
    }

    private ServerProcess startServer(
        SignatureType signatureType, String target, ImmutableList<String> extraArgs)
        throws IOException, InterruptedException {
        File javaHome = new File(System.getProperty("java.home"));
        assertThat(javaHome.exists()).isTrue();
        File javaBin = new File(javaHome, "bin");
        File javaCommand = new File(javaBin, "java");
        assertThat(javaCommand.exists()).isTrue();
        String myClassPath = System.getProperty("java.class.path");
        assertThat(myClassPath).isNotNull();
        ImmutableList<String> command =
            ImmutableList.<String>builder()
                .add(javaCommand.toString(), "-classpath", myClassPath, Invoker.class.getName())
                .addAll(extraArgs)
                .build();
        ProcessBuilder processBuilder = new ProcessBuilder().command(command).redirectErrorStream(true);
        Map<String, String> environment =
            ImmutableMap.of(
                "PORT",
                String.valueOf(serverPort),
                "FUNCTION_SIGNATURE_TYPE",
                signatureType.toString(),
                "FUNCTION_TARGET",
                target);
        processBuilder.environment().putAll(environment);
        Process serverProcess = processBuilder.start();
        CountDownLatch ready = new CountDownLatch(1);
        StringBuilder output = new StringBuilder();
        Future<?> outputMonitorResult =
            EXECUTOR.submit(() -> monitorOutput(serverProcess.getInputStream(), ready, output));
        boolean serverReady = ready.await(5, TimeUnit.SECONDS);
        if (!serverReady) {
            serverProcess.destroy();
            throw new AssertionError("Server never became ready");
        }
        return new ServerProcess(serverProcess, outputMonitorResult, output);
    }

    private void monitorOutput(
        InputStream processOutput, CountDownLatch ready, StringBuilder output) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(processOutput))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(SERVER_READY_STRING)) {
                    ready.countDown();
                }
                System.out.println(line);
                synchronized (output) {
                    output.append(line).append('\n');
                }
                if (line.contains("WARNING")) {
                    throw new AssertionError("Found warning in server output:\n" + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }
}
