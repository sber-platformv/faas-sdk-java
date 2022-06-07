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
