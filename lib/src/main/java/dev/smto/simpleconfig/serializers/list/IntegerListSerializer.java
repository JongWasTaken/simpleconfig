package dev.smto.simpleconfig.serializers.list;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntegerListSerializer implements TypeSerializer<List<Integer>> {
    @Override public String matches() { return "int|integer"; }
    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        entry.reference().set(null, new ArrayList<Integer>(Arrays.stream(value.split(",")).map(String::trim).map(Integer::decode).toList()));
        return true;
    }
}
