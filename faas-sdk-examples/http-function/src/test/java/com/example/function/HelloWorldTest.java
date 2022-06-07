// Copyright 2022 АО «СберТех»
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

package com.example.function;

import io.restassured.http.ContentType;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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