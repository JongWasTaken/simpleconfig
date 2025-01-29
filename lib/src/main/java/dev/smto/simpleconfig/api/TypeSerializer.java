package dev.smto.simpleconfig.api;

import org.jetbrains.annotations.Nullable;

public interface TypeSerializer<T> {
    String matches();
    boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException;
}
