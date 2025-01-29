package dev.smto.simpleconfig.serializers.primitive;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

public class LongSerializer implements TypeSerializer<Long> {
    @Override public String matches() { return "long"; }

    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        entry.reference().set(null, Long.decode(value));
        return true;
    }
}
