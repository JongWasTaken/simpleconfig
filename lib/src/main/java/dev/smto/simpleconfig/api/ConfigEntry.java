package dev.smto.simpleconfig.api;

import com.mojang.serialization.Codec;

import java.lang.reflect.Field;

public record ConfigEntry<T>(String key, String comment, Field reference, Codec<T> codec) {}