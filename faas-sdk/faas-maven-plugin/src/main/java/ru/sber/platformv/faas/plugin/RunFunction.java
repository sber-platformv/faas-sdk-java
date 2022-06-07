// Copyright 2022 АО «СберТех»
//
// Copyright 2021 Google LLC
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

package ru.sber.platformv.faas.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import ru.sber.platformv.faas.invoker.runner.Invoker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs a function using the Java Functions Framework. Typically this plugin is configured in one of
 * two ways. Either in the pom.xml file, like this...
 *
 * <pre>{@code
 * <plugin>
 *   <groupId>ru.sber.platformv.faas</groupId>
 *   <artifactId>faas-maven-plugin</artifactId>
 *   <version>1.0.0</version>
 *   <configuration>
 *     <functionTarget>com.example.function.HelloWorld</functionTarget>
 *     <port>8080</port>
 *   </configuration>
 * </plugin>
 * }</pre>
 * <p>
 * ...and then run using {@code mvn faas:run}. Or using properties on the command line, like
 * this...<br>
 *
 * <pre>{@code
 * mvn ru.sber.platformv.faas:faas:1.0.0:run \
 *     -Drun.functionTarget=com.example.function.HelloWorld
 * }</pre>
 */
@Mojo(
    name = "run",
    defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    requiresDependencyCollection = ResolutionScope.RUNTIME)
@Execute(phase = LifecyclePhase.COMPILE)
public class RunFunction extends AbstractMojo {

    /**
     * The name of the function to run.
     * This must be the path to a class implementing the interface {@code ru.sber.plarformv.faas.api.HttpFunction}.
     * For instance: {@code com.example.function.HelloWorld}
     */
    @Parameter(property = "run.target")
    private String target;

    /**
     * The port on which the HTTP server wrapping the function should listen.
     */
    @Parameter(property = "run.port", defaultValue = "8080")
    private Integer port;

    /**
     * Used to determine what classpath needs to be used to load the function. This parameter is
     * injected by Maven and can't be set explicitly in a pom.xml file.
     */
    @Parameter(defaultValue = "${project.runtimeClasspathElements}", readonly = true, required = true)
    private List<String> runtimePath;

    public void execute() throws MojoExecutionException {
        runtimePath.add("libs/*");
        String classpath = String.join(File.pathSeparator, runtimePath);
        List<String> args = new ArrayList<>();
        args.addAll(Arrays.asList("--classpath", classpath));
        if (target != null) {
            args.addAll(Arrays.asList("--target", target));
        }
        if (port != null) {
            args.addAll(Arrays.asList("--port", String.valueOf(port)));
        }
        try {
            getLog().info("Calling Invoker with " + args);
            Invoker.main(args.toArray(new String[0]));
        } catch (Exception e) {
            getLog().error("Could not invoke function: " + e, e);
            throw new MojoExecutionException("Could not invoke function", e);
        }
    }
}
