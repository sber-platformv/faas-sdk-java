# Faas SDK for Java

[![Maven Central (faas-sdk-api)](https://img.shields.io/maven-central/v/ru.sber.platformv.faas/faas-sdk-api.svg?label=faas-sdk-api)](https://search.maven.org/artifact/ru.sber.platformv.faas/faas-sdk-api)
[![Maven Central (faas-sdk-invoker)](https://img.shields.io/maven-central/v/ru.sber.platformv.faas/faas-sdk-invoker.svg?label=faas-sdk-invoker)](https://search.maven.org/artifact/ru.sber.platformv.faas/faas-sdk-invoker)
[![Maven Central (faas-sdk-test)](https://img.shields.io/maven-central/v/ru.sber.platformv.faas/faas-sdk-test.svg?label=faas-sdk-test)](https://search.maven.org/artifact/ru.sber.platformv.faas/faas-sdk-test)
[![Maven Central (faas-maven-plugin)](https://img.shields.io/maven-central/v/ru.sber.platformv.faas/faas-maven-plugin.svg?label=faas-maven-plugin)](https://search.maven.org/artifact/ru.sber.platformv.faas/faas-maven-plugin)

[![Java Unit CI](https://github.com/sber-platformv/faas-sdk-java/actions/workflows/unit.yaml/badge.svg)](https://github.com/sber-platformv/faas-sdk-java/actions/workflows/unit.yaml)

An open source FaaS (Function as a service) SDK for writing portable Java functions -- brought to you by the Platform V
Functions team.

The Faas SDK lets you write lightweight functions that run in many different environments, including:

* [Platform V Functions](https://developers.sber.ru/portal/tools/platform-v-functions)
* Your local development machine
* JUnit 5 tests

## Installation

The Faas SDK for Java uses
[Java](https://java.com/en/download/help/download_options.xml) and
[Maven](http://maven.apache.org/install.html) (the `mvn` command), for building functions from source.

## Quickstart: Hello, World on your local machine

A function is typically structured as a Maven project. We recommend using an IDE that supports Maven to create the Maven
project. Add this dependency in the
`pom.xml` file of your project:

```xml

<dependency>
  <groupId>ru.sber.platformv.faas</groupId>
  <artifactId>faas-sdk-api</artifactId>
  <version>1.0.0</version>
  <scope>provided</scope>
</dependency>
```

### Writing an HTTP function

Create a file `src/main/java/com/example/HelloWorld.java` with the following contents:

```java
package com.example;

import ru.sber.platformv.faas.api.HttpFunction;
import ru.sber.platformv.faas.api.HttpRequest;
import ru.sber.platformv.faas.api.HttpResponse;

public class HelloWorld implements HttpFunction {
  @Override
  public void service(HttpRequest request, HttpResponse response)
          throws Exception {
    response.getWriter().write("Hello, World\n");
  }
}
```

## Running a function with the Maven plugin

The Maven plugin called `faas-maven-plugin` allows you to run functions on your development machine.

### Configuration in `pom.xml`

You can configure the plugin in `pom.xml`:

```xml

<plugin>
  <groupId>ru.sber.platformv.faas</groupId>
  <artifactId>faas-maven-plugin</artifactId>
  <version>1.0.0-rc.1</version>
  <configuration>
    <functionTarget>com.example.function.Echo</functionTarget>
  </configuration>
</plugin>
```

Then run it from the command line:

```sh
mvn faas:run
```

### Configuration on the command line

You can alternatively configure the plugin with properties on the command line:

```sh
  mvn ru.sber.platformv.faas:faas-maven-plugin:1.0.0-rc.1:run \
      -Drun.functionTarget=com.example.function.Echo
```

### Running the Faas SDK Invoker directly

You can also run a function by using the Faas SDK Invoker jar directly. Copy the Faas SDK Invoker jar to a local
location like this:

```sh
mvn dependency:copy \
    -Dartifact='ru.sber.platformv.faas:faas-sdk-invoker:1.0.0-rc.1' \
    -DoutputDirectory=.
```

In this example we use the current directory `.` but you can specify any other directory to copy to. Then run your
function:

```sh
java -jar faas-sdk-invoker-1.0.0-rc.1 \
    --classpath myfunction.jar \
    --target com.example.HelloWorld
```

## Faas SDK Invoker configuration

There are a number of options that can be used to configure the Faas SDK Invoker, whether run directly or on the command
line.

### Which function to run

A function is a Java class. You must specify the name of that class when running the Faas SDK Invoker:

```
--target com.example.HelloWorld
<functionTarget>com.example.HelloWorld</functionTarget>
-Drun.functionTarget=com.example.HelloWorld
-Prun.functionTarget=com.example.HelloWorld
```

* Invoker argument: `--target com.example.HelloWorld`
* Maven `pom.xml`: `<functionTarget>com.example.HelloWorld</functionTarget>`
* Maven CLI argument: `-Drun.functionTarget=com.example.HelloWorld`
* Gradle CLI argument: `-Prun.functionTarget=com.example.HelloWorld`

### Which port to listen on

The Faas SDK Invoker is an HTTP server that directs incoming HTTP requests to the function code. By default this server
listens on port 8080. Specify an alternative value like this:

* Invoker argument: `--port 12345`
* Maven `pom.xml`: `<port>12345</port>`
* Maven CLI argument: `-Drun.port=12345`
* Gradle CLI argument: `-Prun.port=12345`

### Function classpath

Function code runs with a classpath that includes the function code itself and its dependencies. The Maven plugin
automatically computes the classpath based on the dependencies expressed in `pom.xml`. When invoking the Faas SDK
Invoker directly, you must use `--classpath` to indicate how to find the code and its dependencies. For example:

```
java -jar faas-sdk-invoker-1.0.0-rc.1 \
    --classpath 'myfunction.jar:/some/directory:/some/library/*' \
    --target com.example.HelloWorld
```

The `--classpath` option works like
[`java -classpath`](https://docs.oracle.com/en/java/javase/13/docs/specs/man/java.html#standard-options-for-java). It is
a list of entries separated by `:` (`;` on Windows), where each entry is:

* a directory, in which case class `com.example.Foo` is looked for in a file
  `com/example/Foo.class` under that directory;
* a jar file, in which case class `com.example.Foo` is looked for in a file
  `com/example/Foo.class` in that jar file;
* a directory followed by `/*` (`\*` on Windows), in which case each jar file in that directory (file called `foo.jar`)
  is treated the same way as if it had been named explicitly.

#### Simplifying the classpath

Specifying the right classpath can be tricky. A simpler alternative is to build the function as a "fat jar", where the
function code and all its dependencies are in a single jar file. Then `--classpath myfatfunction.jar`
is enough. An example of how this is done is the Faas SDK Invoker jar itself, as seen
[here](faas-sdk/faas-sdk-invoker/pom.xml).

Alternatively, you can arrange for your jar to have its own classpath, as described
[here](https://maven.apache.org/shared/maven-archiver/examples/classpath.html).

## Testing a function with JUnit 5

The JUnit 5 test framework called `faas-sdk-test` allows you to test functions in unit tests.

### Configuration in `pom.xml`

You should add the following test dependency in `pom.xml`:

```xml

<dependency>
  <groupId>ru.sber.platformv.faas</groupId>
  <artifactId>faas-sdk-test</artifactId>
  <version>1.0.0-rc.1</version>
  <scope>test</scope>
</dependency>
```

### Writing JUnit 5 test

Write unit test for your http function:

```java
package com.example;

import io.restassured.http.ContentType;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import ru.sber.platformv.faas.test.junit.FunctionTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@FunctionTest
class HelloWorldTest {
  @Test
  void test() {
    given()
            .accept(ContentType.TEXT)

            .when()
            .get()

            .then()
            .contentType(ContentType.TEXT)
            .statusCode(HttpStatus.OK_200)
            .body(equalTo("Hello, world!"));
  }
}
```

RestAssured testing library is supported of the box, but you can use any test client you want. You should just
initialize it with correct HTTP server port: you should read it from "faas.test.port" system property before each test.

For more configuration options please see
[@FunctionTest annotation](faas-sdk/faas-sdk-test/src/main/java/ru/sber/platformv/faas/test/junit/FunctionTest.java).

You can find more test examples
[here](faas-sdk-examples/http-function/src/test/java/com/example/HelloWorldTest.java).
