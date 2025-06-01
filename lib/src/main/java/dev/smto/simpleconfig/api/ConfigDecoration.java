package dev.smto.simpleconfig.api;

import org.jetbrains.annotations.Nullable;

public record ConfigDecoration(@Nullable String section, @Nullable String comment) {
}
