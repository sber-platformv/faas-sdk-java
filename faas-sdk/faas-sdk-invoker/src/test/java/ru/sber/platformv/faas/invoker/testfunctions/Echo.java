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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Collectors;

public class Echo implements HttpFunction {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        boolean binary = "application/octet-stream".equals(request.getContentType().orElse(null));
        if (binary) {
            response.setContentType("application/octet-stream");
            byte[] buf = new byte[1024];
            InputStream in = request.getInputStream();
            OutputStream out = response.getOutputStream();
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
        } else {
            String body = request.getReader().lines().collect(Collectors.joining("\n")) + "\n";
            response.setContentType("text/plain");
            response.getWriter().write(body);
        }
    }
}
