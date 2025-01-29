package dev.smto.simpleconfig;

import dev.smto.simpleconfig.api.ConfigLogger;

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
}
