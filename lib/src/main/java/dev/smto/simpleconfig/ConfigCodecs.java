package dev.smto.simpleconfig;

import com.mojang.serialization.Codec;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;

public class ConfigCodecs {
    private static final HashMap<String, Codec<?>> BUILTIN = new HashMap<>();
    static {{
        ConfigCodecs.BUILTIN.put("boolean", Codec.BOOL);
        ConfigCodecs.BUILTIN.put("string", Codec.STRING);
        ConfigCodecs.BUILTIN.put("int", Codec.INT);
        ConfigCodecs.BUILTIN.put("integer", Codec.INT);
        ConfigCodecs.BUILTIN.put("double", Codec.DOUBLE);
        ConfigCodecs.BUILTIN.put("float", Codec.FLOAT);
        ConfigCodecs.BUILTIN.put("byte", Codec.BYTE);
        ConfigCodecs.BUILTIN.put("short", Codec.SHORT);
        ConfigCodecs.BUILTIN.put("long", Codec.LONG);
        ConfigCodecs.BUILTIN.put("char", Codec.STRING.xmap(s -> s.charAt(0), Object::toString));
        ConfigCodecs.BUILTIN.put("character", Codec.STRING.xmap(s -> s.charAt(0), Object::toString));
    }}

    public static void set(String simpleTypeName, Codec<?> codec) {
        ConfigCodecs.BUILTIN.put(simpleTypeName, codec);
    }

    public static Codec<?> get(Field field) {
        var type = field.getType().getSimpleName().toLowerCase();
        if (type.contains("map")) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Codec<?> c1 = null;
            Codec<?> c2 = null;
            try {
                Class<?> gt1 = (Class<?>) pt.getActualTypeArguments()[0];
                Class<?> gt2 = (Class<?>) pt.getActualTypeArguments()[1];
                c1 = ConfigCodecs.BUILTIN.get(gt1.getSimpleName().toLowerCase());
                c2 = ConfigCodecs.BUILTIN.get(gt2.getSimpleName().toLowerCase());
            } catch (Throwable ignored) {}
            if (c1 != null || c2 != null) {
                return Codec.unboundedMap(c1, c2);
            }
        }
        if (type.contains("pair")) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Codec<?> c1 = null;
            Codec<?> c2 = null;
            try {
                Class<?> gt1 = (Class<?>) pt.getActualTypeArguments()[0];
                Class<?> gt2 = (Class<?>) pt.getActualTypeArguments()[1];
                c1 = ConfigCodecs.BUILTIN.get(gt1.getSimpleName().toLowerCase());
                c2 = ConfigCodecs.BUILTIN.get(gt2.getSimpleName().toLowerCase());
            } catch (Throwable ignored) {}
            if (c1 != null || c2 != null) {
                return Codec.pair(c1, c2);
            }
        }
        if (type.contains("list")) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Class<?> gt = (Class<?>) pt.getActualTypeArguments()[0];
            Codec<?> codec = ConfigCodecs.BUILTIN.get(gt.getSimpleName().toLowerCase());
            if (codec != null) return codec.listOf();
        }
        return ConfigCodecs.BUILTIN.getOrDefault(type, null);
    }
}
