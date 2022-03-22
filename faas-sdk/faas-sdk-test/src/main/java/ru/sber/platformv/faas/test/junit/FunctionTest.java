package ru.sber.platformv.faas.test.junit;

import org.junit.jupiter.api.extension.ExtendWith;
import ru.sber.platformv.faas.api.HttpFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be specified on a test class that runs Faas SDK based tests.
 * Provides the following features:
 * <ul>
 * <li>Automatically starts HTTP server with an implementation of {@link HttpFunction}
 * before tests and stops it after.</li>
 * <li>Allows custom {@link HttpFunction} implementation class that can be defined
 * using the {@link #function() function attribute}.</li>
 * <li>Automatically searches for a {@link HttpFunction} implementation when
 * {@link #function() function attribute} is not defined. The package with a test class
 * will be scanned by default. Custom package to scan can be defined using the
 * using the {@link #scanPackage() scanPackage attribute}.</li>
 * <li>Allows HTTP server port to be defined using the {@link #port() port attribute}.
 * By default random free port will be used.</li>
 * <li>Sets a {@link #PORT_PROPERTY test port system property} with HTTP server port
 * value and initializes {@link io.restassured.RestAssured#port} before each test method
 * execution.</li>
 * </ul>
 */
@ExtendWith({FunctionTestExtension.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FunctionTest {

    /**
     * System property with this name is initialized before each test with the port of
     * HTTP server that serves the function. The property is cleared after each test,
     * because for different test classes the port values can be different. So for test
     * client initialization ones should read the actual value before each test method,
     * for example using {@link org.junit.jupiter.api.BeforeEach @BeforeEach annotation}.
     */
    String PORT_PROPERTY = "faas.test.port";

    /**
     * @return The function class of the function. If not specified the testing framework
     * will try to find the class by package scanning.
     * @see #scanPackage()
     */
    Class<? extends HttpFunction> function() default HttpFunction.class;

    /**
     * @return The package to consider for scanning. The package with a test class will be
     * scanned by default.
     */
    String scanPackage() default "";

    /**
     * @return The port to run the function on. Default value: -1 indicates random free port.
     */
    int port() default -1;
}
