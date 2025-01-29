package dev.smto.simpleconfig.serializers;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

public class StringSerializer implements TypeSerializer<String> {
    @Override public String matches() { return "string"; }

    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        entry.reference().set(null, value);
        return true;
    }
}
