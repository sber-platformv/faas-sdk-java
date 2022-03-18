// Copyright© «Публичное акционерное общество «Сбербанк России» (Место нахождения: 117997, г.
// Москва, ул. Вавилова, д. 19)»
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

package com.example.functionjar;

import ru.sber.platformv.faas.api.HttpFunction;
import ru.sber.platformv.faas.api.HttpRequest;
import ru.sber.platformv.faas.api.HttpResponse;

public class Foreground implements HttpFunction {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        // runtimeClassName is the name of some class that the runtime can see but we should not
        String runtimeClassName = request.getFirstQueryParameter("class").get();
        try {
            new Checker().serviceOrAssert(runtimeClassName);
            response.setStatusCode(200, "OK");
            response.getWriter().write("OK");
        } catch (AssertionError e) {
            response.setStatusCode(500, e.toString());
        }
    }
}
