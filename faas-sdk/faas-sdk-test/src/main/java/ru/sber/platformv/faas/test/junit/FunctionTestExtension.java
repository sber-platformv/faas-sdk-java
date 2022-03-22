package ru.sber.platformv.faas.test.junit;

import io.restassured.RestAssured;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.reflections.Reflections;
import ru.sber.platformv.faas.api.HttpFunction;
import ru.sber.platformv.faas.test.FunctionApplication;

import java.util.ArrayList;
import java.util.List;

public class FunctionTestExtension
    implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

    private FunctionApplication functionApplication;

    @Override
    public void beforeAll(ExtensionContext context) {
        final Class<?> testClass = context.getRequiredTestClass();
        FunctionTestValue functionTestValue = buildFunctionTestValue(testClass);

        Class<? extends HttpFunction> function = functionTestValue.getFunction();
        if (function == null || function.equals(HttpFunction.class)) {
            String scanPackage = functionTestValue.getScanPackage();
            if (scanPackage == null || scanPackage.isBlank()) {
                scanPackage = testClass.getPackage().getName();
            }
            Reflections reflections = new Reflections(scanPackage);
            List<Class<? extends HttpFunction>> functions =
                new ArrayList<>(reflections.getSubTypesOf(HttpFunction.class));
            if (functions.isEmpty()) {
                throw new IllegalStateException(
                    "Failed to find any implementation of " + HttpFunction.class.getName() +
                        ". Please make sure you specified correct scanPackage in FunctionTest annotation.");
            }
            if (functions.size() > 1) {
                throw new IllegalStateException(
                    "Found several implementations of " + HttpFunction.class.getName() +
                        ": " + functions + ". Consider to specify function in FunctionTest annotation.");
            }
            function = functions.get(0);
        }
        functionApplication = FunctionApplication.run(function, functionTestValue.getPort());
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (functionApplication != null) {
            RestAssured.port = functionApplication.getPort();
            System.setProperty(FunctionTest.PORT_PROPERTY, Integer.toString(functionApplication.getPort()));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        RestAssured.port = -1;
        System.clearProperty(FunctionTest.PORT_PROPERTY);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (functionApplication != null) {
            functionApplication.stop();
        }
    }

    /**
     * Builds a {@link FunctionTestValue} object from the provided class (e.g. by scanning annotations).
     *
     * @param testClass the class to extract builder configuration from
     * @return a FunctionTestValue to configure the test function
     */
    private FunctionTestValue buildFunctionTestValue(Class<?> testClass) {
        return AnnotationSupport
            .findAnnotation(testClass, FunctionTest.class)
            .map(this::buildValueObject)
            .orElseThrow(() -> new IllegalStateException(
                "Failed to find FunctionTest annotation for test class: " + testClass.getName()));
    }

    private FunctionTestValue buildValueObject(FunctionTest functionTest) {
        return new FunctionTestValue(
            functionTest.function(),
            functionTest.scanPackage(),
            functionTest.port());
    }
}
