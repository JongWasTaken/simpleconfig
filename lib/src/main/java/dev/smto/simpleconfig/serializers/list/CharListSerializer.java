package dev.smto.simpleconfig.serializers.list;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CharListSerializer implements TypeSerializer<List<Character>> {
    @Override public String matches() { return "char|character"; }
    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        entry.reference().set(null, new ArrayList<Character>(Arrays.stream(value.split(",")).map(s -> s.charAt(0)).toList()));
        return true;
    }
}
