package dev.smto.simpleconfig.api;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public record ConfigEntry(String key, String comment, Field reference, Class<?> type, @Nullable String nestedType) {}