package ru.sber.platformv.faas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sber.platformv.faas.api.HttpFunction;
import ru.sber.platformv.faas.api.HttpRequest;
import ru.sber.platformv.faas.api.HttpResponse;

public class HelloWorldHttpFunction implements HttpFunction {

    private final static Logger logger = LoggerFactory.getLogger(HelloWorldHttpFunction.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        logger.info("Received request [method: {}, path: '{}', headers: {}]", request.getMethod(), request.getPath(),
            request.getHeaders());
        var name = request.getFirstQueryParameter("name").orElse("world");
        response.setContentType("text/plain");
        response.getWriter().write(String.format("Hello, %s!", name));
    }
}
