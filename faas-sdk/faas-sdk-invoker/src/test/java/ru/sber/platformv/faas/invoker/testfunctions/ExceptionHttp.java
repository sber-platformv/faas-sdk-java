package ru.sber.platformv.faas.invoker.testfunctions;

import ru.sber.platformv.faas.api.HttpFunction;
import ru.sber.platformv.faas.api.HttpRequest;
import ru.sber.platformv.faas.api.HttpResponse;

public class ExceptionHttp implements HttpFunction {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        throw new RuntimeException("exception thrown for test");
    }
}
