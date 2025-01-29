package dev.smto.simpleconfig.serializers.primitive;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

public class ByteSerializer implements TypeSerializer<Byte> {
    @Override public String matches() { return "byte"; }

    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        entry.reference().set(null, Byte.decode(value));
        return true;
    }
}
