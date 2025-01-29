package dev.smto.simpleconfig.serializers.primitive;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

public class DoubleSerializer implements TypeSerializer<Double> {
    @Override public String matches() { return "double"; }

    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        entry.reference().set(null, Double.parseDouble(value));
        return true;
    }
}
