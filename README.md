# FAAS-SDK для тестирования и локальной отладки Java функций

FAAS-SDK позволяет локально тестировать функции [Platform V Functions](https://developers.sber.ru/portal/products/platform-v-functions) без необходимости писать HTTP сервер и логику обработки запросов.

SDK для Java может быть подключен как плагин или запущен напрямую.

## Пререквизиты

* Java 11 SDK, например OpenJDK 11;
* Maven 3.6.3 или выше;

## Установка и использование

1. Распакуйте экспортированную функцию в отдельную директорию, или перейдите в директорию проекта функции, если уже ведете локальную разработку.

2. Добавьте в `pom.xml` следующую зависимость, если она отсутствует:
    
    ```xml
    <dependency>
      <groupId>ru.sber.platformv.faas</groupId>
      <artifactId>faas-sdk-api</artifactId>
      <version>1.0.1</version>
      <scope>provided</scope>
    </dependency>
    ```

3. В случае, если у вас нет готовой функции — создайте файл `src/main/java/handlers/Handler.java` со следующим содержанием:

    ```java
    package handlers;

    import ru.sber.platformv.faas.api.HttpFunction;
    import ru.sber.platformv.faas.api.HttpRequest;
    import ru.sber.platformv.faas.api.HttpResponse;

    import java.io.IOException;
    import java.util.logging.Logger;

    public class Handler implements HttpFunction {

        // Метод service. Данный метод будет обрабатывать HTTP запрыосы поыыступающие к функции
        @Override
        public void service(HttpRequest request, HttpResponse response) throws IOException {

            // Логирование входящего запроса
            String requestBody = new String(request.getInputStream().readAllBytes());
            Logger.getGlobal().info("Request received: " + requestBody + "\nMethod: " + request.getMethod());

            // Подготовка и возврат ответа на вызов
            response.setContentType("text/plain; charset=utf-8");
            response.getWriter().write("Hello from Java11 function!\nYou said: " + requestBody);
        }
    }
    ```

Для запуска проекта для тестирования вы можете воспользоваться одним из трех методов:
* запустить функцию при помощи плагина, настроенного в `pom.xml`;
* запустить функцию при помощи плагина через CLI;
* запустить сервер с функцией напрямую.

### Запуск при помощи плагина

Вы можете настроить запуск функции через плагин либо в `pom.xml`, либо напрямую через команду запуска плагина в CLI.

Для запуска плагина из `pom.xml`:

1. Откройте `pom.xml` и добавьте в него следующие строки:
    
    ```xml
    <plugin>
      <groupId>ru.sber.platformv.faas</groupId>
      <artifactId>faas-maven-plugin</artifactId>
      <version>1.0.1</version>
      <configuration>
        <target>handlers.Handler</target>
        <port>8080</port>
      </configuration>
    </plugin>
   ```
   , где:
   * `functionTarget` — класс, имплементирующий интерфейс `HttpFunction` пакета `ru.sber.platformv.faas.api.HttpFunction`;
   * `port` — порт локального сервера, на который запущенная функция будет принимать запросы.

2. В CLI перейдите в папку проекта и запустите плагин командой:
    
    ```shell
    mvn faas:run
    ```
    
    Функция будет запущена по адресу `localhost:8080` (или на том порте, который указан в конфигурации).

3. Отправляйте запросы используя `curl`, браузер или другие инструменты:
    
    ```shell
        curl localhost:8080
        # Hello from Java11 function!
        # You said:
    ```

### Запуск плагина из командной строки

Вы также можете сконфигурировать и запустить плагин через командную строку:

1. В CLI перейдите в папку проекта и запустите плагин следующей командой:
    
    ```shell
     mvn ru.sber.platformv.faas:faas-maven-plugin:1.0.1:run -Drun.target=handlers.Handler -Drun.port=8080
    ```
   , где:
   * `-Drun.target` — класс, имплементирующий интерфейс `HttpFunction` пакета `ru.sber.platformv.faas.api.HttpFunction`;
   * `-Drun.port` — порт локального сервера, на который запущенная функция будет принимать запросы.

2. Отправляйте запросы используя `curl`, браузер или другие инструменты:
    
    ```shell
        curl localhost:8082
        # Hello from Java11 function!
        # You said:
    ```

### Запуск сервера напрямую

Для запуска напрямую вам понадобятся .jar файл сервера и полный (то есть включающий в себя все классы из зависимостей проекта) .jar функции, которую вы хотите запустить.

1. Поместите полный (или "толстый") .jar функции (например `myfunction.jar`) в отдельную директорию.

2. В CLI перейдите в эту директорию и выполните следующую команду:
    
    ```shell
    mvn dependency:copy -Dartifact='ru.sber.platformv.faas:faas-sdk-invoker:1.0.1' -DoutputDirectory=.
    ```
   
   Эта команда загрузит .jar сервера в текущую директорию. Вы также можете изменить директорию, в которую хотите загрузить .jar, указав путь до нее в значение параметра `-DoutputDirectory`.

3. Запустите сервер с функцией, выполнив в CLI следующую команду:
   
   ```shell
   java -jar faas-sdk-invoker-1.0.1.jar --classpath myfunction.jar --target handlers.Handler --port 8080
   ```
   , где:
   * `--classpath` — путь до кода функции и ее зависимостей. Если вы используете не "толстый" .jar файл функции, вам понадобится указать путь согласно инструкции команды [java -classpath](https://docs.oracle.com/en/java/javase/13/docs/specs/man/java.html#standard-options-for-java);
   * `--target` — класс, имплементирующий интерфейс `HttpFunction` пакета `ru.sber.platformv.faas.api.HttpFunction`;
   * `--port` — порт локального сервера, на который запущенная функция будет принимать запросы.

4. Отправляйте запросы используя `curl`, браузер или другие инструменты:
    
    ```shell
        curl localhost:8080
        # Hello from Java11 function!
        # You said:
    ```


## Добавление локальных зависимостей

Если вам необходимо добавить в функцию зависимости как .jar файлы, в случае когда они не опубликованы в центральном репозитории, то:

1. Поместите .jar файл зависимости в директорию `/libs` в корне проекта функции.

2. Объявите эту зависимость в `pom.xml` указав `<scope>system</scope>` и `<systemPath>`, например:
	```xml
	    <dependency>
    	    <groupId>groupId</groupId>
        	<artifactId>artifactId</artifactId>
        	<version>version</version>
        	<scope>system</scope>
        	<systemPath>${project.basedir}/libs/my-lib.jar</systemPath>
    	</dependency>
	```
    
**Обратите внимание!**

Если вы запускаете сервер для тестирования напрямую, разместите директорию `/libs` в той же директории, где расположен файл `faas-sdk-invoker`, например:

```
    my-folder 
     ├ faas-sdk-invoker-1.0.1.jar     
     │
     ├ myfunction.jar                     
     │
     └ libs                               
       └ my-lib.jar                    
       └ my-lib2.jar
       └ ...
```

## Unit-тестирование

Вы можете добавить unit-тесты для своей функции. Для этого используется JUnit 5 фреймворк - `faas-sdk-test`.

Чтобы подключить фреймворк, добавьте в `pom.xml` вашей функции следующие строки:

```xml
<dependency>
  <groupId>ru.sber.platformv.faas</groupId>
  <artifactId>faas-sdk-test</artifactId>
  <version>1.0.1</version>
  <scope>test</scope>
</dependency>
```

Фреймворк по умолчанию поддерживает библиотеку для тестирования RestAssured, однако вы можете использовать любой другой тестовый клиент. Вам нужно будет корректно инициализировать его с используемым серверным портом HTTP, который считывается с параметра `faas.test.port` перед каждым тестом.

Основной класс теста должен быть отмечен аннотацией `@FunctionTest`. Вы также можете настроить следующие параметры аннотации:
* `function` — класс, имплементирующий интерфейс `HttpFunction` пакета `ru.sber.platformv.faas.api.HttpFunction`, который будет тестироваться. Если параметр не указан, SDK автоматически будет искать первый класс, имплементирующий `HttpFunction`, в своем пакете;
* `scanPackage` — пакет, в котором SDK будет искать класс функции, если он не указан в параметре `function`.
* `port` — порт, на котором будут подниматься серверы функций для тестов. Если параметр не указан, для каждого теста будет подниматься отдельный сервер на случайном свободном порте.

Например, для тестирования `hello, world` примера из базовой функции:

1. Создайте в директории `src` цепочку директорий `test/java/handlers/`.

2. В директории `handlers` создайте файл `HandlerTest.java` со следующим содержанием:
    
    ```java
    package handlers;

    import io.restassured.http.ContentType;
    import org.eclipse.jetty.http.HttpStatus;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.params.ParameterizedTest;
    import org.junit.jupiter.params.provider.ValueSource;
    import ru.sber.platformv.faas.test.junit.FunctionTest;

    import static io.restassured.RestAssured.given;
    import static org.hamcrest.Matchers.equalTo;

    @FunctionTest
    class HandlerTest {
        @Test
        void test() {
            given()
                .param("request", "Hello, world!")
                .accept(ContentType.TEXT)

                .when()
                .get()

                .then()
                .contentType(ContentType.TEXT)
                .statusCode(HttpStatus.OK_200)
                .body(equalTo("Hello from Java11 function!\nYou said: Hello, world!"));
        }
    }
    ```
    
3. Запустите сборку функции. Логи unit-теста будут отображены в терминале.


Подробнее о настройках фреймворка смотрите в [@FunctionTest annotation](faas-sdk/faas-sdk-test/src/main/java/ru/sber/platformv/faas/test/junit/FunctionTest.java).

Дополнительные примеры тестов расположены [здесь](faas-sdk-examples/http-function/src/test/java/com/example/function/HelloWorldTest.java).
