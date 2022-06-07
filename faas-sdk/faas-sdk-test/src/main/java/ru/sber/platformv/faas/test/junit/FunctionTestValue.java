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

package ru.sber.platformv.faas.test.junit;

import ru.sber.platformv.faas.api.HttpFunction;

/**
 * Value object for the values from any of the FunctionTest annotations.
 */
public class FunctionTestValue {

    private final Class<? extends HttpFunction> function;
    private final String scanPackage;
    private final int port;

    public FunctionTestValue(Class<? extends HttpFunction> function, String scanPackage, int port) {
        this.function = function;
        this.scanPackage = scanPackage;
        this.port = port;
    }

    public Class<? extends HttpFunction> getFunction() {
        return function;
    }

    public String getScanPackage() {
        return scanPackage;
    }

    public int getPort() {
        return port;
    }
}
