package dev.smto.simpleconfig.serializers.list;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FloatListSerializer implements TypeSerializer<List<Float>> {
    @Override public String matches() { return "float"; }
    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        entry.reference().set(null, new ArrayList<Float>(Arrays.stream(value.split(",")).map(String::trim).map(Float::parseFloat).toList()));
        return true;
    }
}
