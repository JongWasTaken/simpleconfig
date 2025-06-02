package dev.smto.simpleconfig;

import dev.smto.simpleconfig.api.ConfigLogger;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class ConfigLoggers {
    public static final ConfigLogger NONE = new ConfigLogger() {
    };
    public static final ConfigLogger SYSTEM_OUT = new ConfigLogger() {
        @Override
        public void debug(String message) {
            System.out.println("[DEBUG] " + message);
        }

        @Override
        public void error(String message) {
            System.out.println("[ERROR] " + message);
        }

        @Override
        public void info(String message) {
            System.out.println("[INFO] " + message);
        }

        @Override
        public void warn(String message) {
            System.out.println("[WARN] " + message);
        }
    };
    public static ConfigLogger createSimple(Consumer<String> logConsumer) {
        return new ConfigLogger() {
            @Override
            public void debug(String message) {
                logConsumer.accept("[DEBUG] " + message);
            }

            @Override
            public void error(String message) {
                logConsumer.accept("[ERROR] " + message);
            }

            @Override
            public void info(String message) {
                logConsumer.accept("[INFO] " + message);
            }

            @Override
            public void warn(String message) {
                logConsumer.accept("[WARN] " + message);
            }
        };
    }

    public static ConfigLogger create(Consumer<String> debugConsumer, Consumer<String> infoConsumer, Consumer<String> warnConsumer, Consumer<String> errorConsumer) {
        return new ConfigLogger() {
            @Override
            public void debug(String message) {
                debugConsumer.accept(message);
            }

            @Override
            public void error(String message) {
                errorConsumer.accept(message);
            }

            @Override
            public void info(String message) {
                infoConsumer.accept(message);
            }

            @Override
            public void warn(String message) {
                warnConsumer.accept(message);
            }
        };
    }
}
