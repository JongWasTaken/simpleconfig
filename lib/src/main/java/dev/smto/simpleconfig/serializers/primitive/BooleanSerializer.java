package dev.smto.simpleconfig.serializers.primitive;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class BooleanSerializer implements TypeSerializer<Boolean> {
    @Override public String matches() { return "boolean"; }
    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        if (value.trim().toLowerCase(Locale.ROOT).equals("true")) {
            entry.reference().set(null, true);
            return true;
        } else if (value.trim().toLowerCase(Locale.ROOT).equals("false")) {
            entry.reference().set(null, false);
            return true;
        }
        return false;
    }
}
