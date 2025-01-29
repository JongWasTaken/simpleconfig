package dev.smto.simpleconfig.serializers.primitive;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

public class IntegerSerializer implements TypeSerializer<Integer> {
    @Override public String matches() { return "int|integer"; }

    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        entry.reference().set(null, Integer.decode(value));
        return true;
    }
}
