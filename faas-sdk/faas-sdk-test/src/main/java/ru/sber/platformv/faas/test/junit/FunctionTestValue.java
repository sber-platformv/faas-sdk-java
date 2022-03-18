package ru.sber.platformv.faas.test.junit;

import ru.sber.platformv.faas.api.HttpFunction;

/**
 * Value object for the values from any of the FunctionTest annotations.
 */
public class FunctionTestValue {

    private final Class<? extends HttpFunction> function;
    private final String scanPackage;
    private final int port;

    public FunctionTestValue(Class<? extends HttpFunction> function, String scanPackage, int port) {
        this.function = function;
        this.scanPackage = scanPackage;
        this.port = port;
    }

    public Class<? extends HttpFunction> getFunction() {
        return function;
    }

    public String getScanPackage() {
        return scanPackage;
    }

    public int getPort() {
        return port;
    }
}
