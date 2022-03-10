# Faas SDK for Java

[![Maven Central (functions-framework-api)](https://img.shields.io/maven-central/v/io.github.sber-platformv/faas-sdk-api.svg?label=faas-sdk-api)](https://search.maven.org/artifact/io.github.sber-platformv/faas-sdk-api)

[![Java Unit CI](https://github.com/sber-platformv/faas-sdk-java/actions/workflows/unit.yaml/badge.svg)](https://github.com/sber-platformv/faas-sdk-java/actions/workflows/unit.yaml)

An open source FaaS (Function as a service) SDK for writing portable
Java functions -- brought to you by the Platform V Functions team.

The Faas SDK lets you write lightweight functions that run in many
different environments, including:

*   [Platform V Functions](https://developers.sber.ru/portal/tools/platform-v-functions)
*   Your local development machine

## Installation

The Functions Framework for Java uses
[Java](https://java.com/en/download/help/download_options.xml) and
[Maven](http://maven.apache.org/install.html) (the `mvn` command),
for building functions from source.

## Quickstart: Hello, World on your local machine

A function is typically structured as a Maven project. We recommend using an IDE
that supports Maven to create the Maven project. Add this dependency in the
`pom.xml` file of your project:

```xml
    <dependency>
      <groupId>io.github.sber-platformv</groupId>
      <artifactId>faas-sdk-api</artifactId>
      <version>1.0.0-rc.5</version>
      <scope>provided</scope>
    </dependency>
```

### Writing an HTTP function

Create a file `src/main/java/com/example/HelloWorld.java` with the following
contents:

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
