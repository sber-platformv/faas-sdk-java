package ru.sber.platformv.faas;

import io.restassured.http.ContentType;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.sber.platformv.faas.test.junit.FunctionTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@FunctionTest
class HelloWorldHttpFunctionTest {

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

    @ParameterizedTest
    @ValueSource(strings = {"testName", "name"})
    void testName(String name) {
        given()
            .accept(ContentType.TEXT)

            .when()
            .queryParam("name", name)
            .get()

            .then()
            .contentType(ContentType.TEXT)
            .statusCode(HttpStatus.OK_200)
            .body(equalTo(String.format("Hello, %s!", name)));
    }
}
