package ru.sber.platformv.faas.invoker.testfunctions;

import ru.sber.platformv.faas.api.HttpFunction;
import ru.sber.platformv.faas.api.HttpRequest;
import ru.sber.platformv.faas.api.HttpResponse;

import java.util.stream.Collectors;

public class Nested {
    public static class Echo implements HttpFunction {
        @Override
        public void service(HttpRequest request, HttpResponse response) throws Exception {
            String body = request.getReader().lines().collect(Collectors.joining("\n"));
            response.setContentType("text/plain");
            response.getWriter().write(body);
            response.getWriter().flush();
        }
    }
}
