package ru.sber.platformv.faas.invoker.testfunctions;

import ru.sber.platformv.faas.api.HttpFunction;
import ru.sber.platformv.faas.api.HttpRequest;
import ru.sber.platformv.faas.api.HttpResponse;

public class EchoUrl implements HttpFunction {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        StringBuilder url = new StringBuilder(request.getPath());
        request.getQuery().ifPresent(q -> url.append("?").append(q));
        url.append("\n");
        response.getWriter().write(url.toString());
    }
}
