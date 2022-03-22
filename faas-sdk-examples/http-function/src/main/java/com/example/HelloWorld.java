package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sber.platformv.faas.api.HttpFunction;
import ru.sber.platformv.faas.api.HttpRequest;
import ru.sber.platformv.faas.api.HttpResponse;

public class HelloWorld implements HttpFunction {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorld.class.getName());

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        logger.info("Received request [method: {}, path: '{}', headers: {}]", request.getMethod(), request.getPath(),
            request.getHeaders());
        var name = request.getFirstQueryParameter("name").orElse("world");
        response.setContentType("text/plain; charset=utf-8");
        response.getWriter().write(String.format("Hello, %s!", name));
    }
}
