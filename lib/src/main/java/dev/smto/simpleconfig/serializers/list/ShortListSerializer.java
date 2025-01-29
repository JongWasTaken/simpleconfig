package dev.smto.simpleconfig.serializers.list;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShortListSerializer implements TypeSerializer<List<Short>> {
    @Override public String matches() { return "short"; }
    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        entry.reference().set(null, new ArrayList<Short>(Arrays.stream(value.split(",")).map(String::trim).map(Short::decode).toList()));
        return true;
    }
}
