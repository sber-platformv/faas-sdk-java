package ru.sber.platformv.faas.test.junit;

import org.junit.jupiter.api.extension.ExtendWith;
import ru.sber.platformv.faas.api.HttpFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ExtendWith({FunctionTestExtension.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FunctionTest {
    /**
     * @return The function class of the function
     */
    Class<? extends HttpFunction> function() default HttpFunction.class;

    /**
     * @return The package to consider for scanning.
     */
    String scanPackage() default "";

    /**
     * @return The port to run the function on. -1 indicates random free port.
     */
    int port() default -1;
}
