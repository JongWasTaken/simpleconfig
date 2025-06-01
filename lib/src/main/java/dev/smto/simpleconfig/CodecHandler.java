package dev.smto.simpleconfig;

import com.mojang.serialization.Codec;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;

public class CodecHandler {
    private static final HashMap<String, Codec<?>> BUILTIN = new HashMap<>();
    static {{
        CodecHandler.BUILTIN.put("boolean", Codec.BOOL);
        CodecHandler.BUILTIN.put("string", Codec.STRING);
        CodecHandler.BUILTIN.put("int", Codec.INT);
        CodecHandler.BUILTIN.put("integer", Codec.INT);
        CodecHandler.BUILTIN.put("double", Codec.DOUBLE);
        CodecHandler.BUILTIN.put("float", Codec.FLOAT);
        CodecHandler.BUILTIN.put("byte", Codec.BYTE);
        CodecHandler.BUILTIN.put("short", Codec.SHORT);
        CodecHandler.BUILTIN.put("long", Codec.LONG);
        CodecHandler.BUILTIN.put("char", Codec.STRING.xmap(s -> s.charAt(0), Object::toString));
        CodecHandler.BUILTIN.put("character", Codec.STRING.xmap(s -> s.charAt(0), Object::toString));
    }}

    public static Codec<?> get(Field field) {
        boolean isList = false;
        var type = field.getType().getSimpleName().toLowerCase();
        if (type.contains("map")) {
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Codec<?> c1 = null;
            Codec<?> c2 = null;
            try {
                Class<?> gt1 = (Class<?>) pt.getActualTypeArguments()[0];
                Class<?> gt2 = (Class<?>) pt.getActualTypeArguments()[1];
                c1 = CodecHandler.BUILTIN.get(gt1.getSimpleName().toLowerCase());
                c2 = CodecHandler.BUILTIN.get(gt2.getSimpleName().toLowerCase());
            } catch (Throwable ignored) {}
            if (c1 != null || c2 != null) {
                return Codec.unboundedMap(c1, c2);
            }
        }
        if (type.contains("list")) {
            isList = true;
            ParameterizedType pt = (ParameterizedType) field.getGenericType();
            Class<?> gt = (Class<?>) pt.getActualTypeArguments()[0];
            type = gt.getSimpleName().toLowerCase();
        }
        var codec =  CodecHandler.BUILTIN.getOrDefault(type, null);
        if (isList) {
            return codec.listOf();
        }
        return codec;
    }
}
