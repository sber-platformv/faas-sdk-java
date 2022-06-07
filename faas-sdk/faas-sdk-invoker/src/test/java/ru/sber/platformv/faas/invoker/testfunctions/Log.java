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

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Emit log messages with configurable level, message, and exception.
 */
public class Log implements HttpFunction {
    private static final Logger logger = Logger.getLogger(Log.class.getName());

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        String message = request.getFirstQueryParameter("message").orElse("Default message");
        String levelString = request.getFirstQueryParameter("level").orElse("info");
        Optional<String> exceptionString = request.getFirstQueryParameter("exception");
        Field levelField = Level.class.getField(levelString.toUpperCase());
        Level level = (Level) levelField.get(null);
        if (exceptionString.isPresent()) {
            logger.log(level, message, new Exception(exceptionString.get()));
        } else {
            logger.log(level, message);
        }
    }
}
