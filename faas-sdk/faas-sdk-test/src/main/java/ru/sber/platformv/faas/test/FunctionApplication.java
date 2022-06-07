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

package ru.sber.platformv.faas.test;

import ru.sber.platformv.faas.invoker.runner.Invoker;

import java.io.IOException;
import java.net.ServerSocket;

public final class FunctionApplication {

    private static final int DEFAULT_PORT = 8080;

    private final Invoker invoker;
    private final int port;
    private final Thread shutdownHook;
    private boolean started;

    public static FunctionApplication run(String functionClass) throws ClassNotFoundException {
        return run(Class.forName(functionClass));
    }

    public static FunctionApplication run(String functionClass, int port) throws ClassNotFoundException {
        return run(Class.forName(functionClass), port);
    }

    public static FunctionApplication run(Class<?> functionClass) {
        return run(functionClass, DEFAULT_PORT);
    }

    public static FunctionApplication run(Class<?> functionClass, int port) {
        return new FunctionApplication(functionClass, port).start();
    }

    private FunctionApplication(Class<?> functionClass, int port) {
        if (port <= 0) {
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                port = serverSocket.getLocalPort();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.port = port;
        this.invoker = new Invoker(this.port, functionClass.getCanonicalName(), "http",
            FunctionApplication.class.getClassLoader());
        this.shutdownHook = new Thread(() -> {
            try {
                invoker.stopServer();
            } catch (Exception e) {
                // do nothing
            }
        });
    }

    private synchronized FunctionApplication start() {
        if (started) {
            throw new IllegalStateException("Application already started.");
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        try {
            invoker.startTestServer();
            started = true;
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        return port;
    }

    public synchronized void stop() {
        try {
            invoker.stopServer();
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            started = false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
