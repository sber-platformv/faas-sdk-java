package com.example;

import ru.sber.platformv.faas.test.FunctionApplication;

public class HelloWorldMainTest {
    public static void main(String[] args) {
        FunctionApplication.run(HelloWorld.class);
    }
}
