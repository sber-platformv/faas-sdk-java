// Copyright 2022 АО «СберТех»
//
// Copyright 2020 Google LLC
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

package ru.sber.platformv.faas.invoker.testfunctions;

import ru.sber.platformv.faas.api.HttpFunction;
import ru.sber.platformv.faas.api.HttpRequest;
import ru.sber.platformv.faas.api.HttpResponse;

import java.io.PrintWriter;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * A simple proof-of-concept function for multipart handling.
 *
 * <p>{@code HttpTest} contains more detailed testing, but this function is part of the integration
 * test that shows that we can indeed access the multipart API from a function.
 */
public class Multipart implements HttpFunction {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        response.setContentType("text/plain");
        String contentType = request.getContentType().orElse("<unknown>");
        if (!contentType.startsWith("multipart/form-data")) {
            response.getWriter().write("Content-Type is " + contentType + " not multipart/form-data");
            return;
        }
        PrintWriter writer = new PrintWriter(response.getWriter());
        NavigableMap<String, HttpRequest.HttpPart> parts = new TreeMap<>(request.getParts());
        parts.forEach(
            (name, contents) -> {
                writer.printf(
                    "part %s type %s length %d\n",
                    name, contents.getContentType().get(), contents.getContentLength());
            });
    }
}
