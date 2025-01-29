package dev.smto.simpleconfig.serializers.list;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class BooleanListSerializer implements TypeSerializer<List<Boolean>> {
    @Override public String matches() { return "boolean"; }
    @Override
    public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
        ArrayList<Boolean> original = (ArrayList<Boolean>) entry.reference().get(null);
        AtomicBoolean failed = new AtomicBoolean(false);
        entry.reference().set(null, new ArrayList<Boolean>(Arrays.stream(value.split(",")).map(String::trim).map((v) -> {
            if (v.trim().toLowerCase(Locale.ROOT).equals("true")) return true;
            if (v.trim().toLowerCase(Locale.ROOT).equals("false")) return false;
            failed.set(true);
            return false;
        }).toList()));
        if (failed.get()) {
            entry.reference().set(null, original); // restore original values
            return false;
        }
        return true;
    }
}
