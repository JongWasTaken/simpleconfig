package dev.smto.simpleconfig.api;

public interface ConfigLogger {
    default void info(String message) {}
    default void debug(String message) {}
    default void warn(String message) {}
    default void error(String message) {}
}
