package dev.smto.simpleconfig.serializers;

import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.TypeSerializer;
import dev.smto.simpleconfig.serializers.list.*;
import dev.smto.simpleconfig.serializers.primitive.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class TypeSerializers {
    public static final TypeSerializer<?> NONE = new TypeSerializer<Object>() {
        @Override
        public String matches() {
            return "none";
        }

        @Override
        public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
            throw new RuntimeException("No serializer for type \""+entry.type().getSimpleName()+"\"!");
        }
    };
    public static final TypeSerializer<?> LIST = new TypeSerializer<List<?>>() {
        @Override
        public String matches() {
            return "arraylist|list";
        }

        @Override
        public boolean serialize(ConfigEntry entry, String value, @Nullable String holds) throws IllegalAccessException {
            if (holds == null) return false;
            if (!TypeSerializers.LIST_SERIALIZERS.containsKey(holds)) return false;
            if (value.charAt(0) == '[') {
                value = value.substring(1);
            }
            if (value.charAt(value.length() - 1) == ']') {
                value = value.substring(0, value.length() - 1);
            }
            return TypeSerializers.LIST_SERIALIZERS.get(holds).serialize(entry, value, holds);
        }
    };
    public static final TypeSerializer<Boolean> BOOLEAN = new BooleanSerializer();
    public static final TypeSerializer<String> STRING = new StringSerializer();
    public static final TypeSerializer<Integer> INTEGER = new IntegerSerializer();
    public static final TypeSerializer<Double> DOUBLE = new DoubleSerializer();
    public static final TypeSerializer<Float> FLOAT = new FloatSerializer();
    public static final TypeSerializer<Byte> BYTE = new ByteSerializer();
    public static final TypeSerializer<Short> SHORT = new ShortSerializer();
    public static final TypeSerializer<Long> LONG = new LongSerializer();
    public static final TypeSerializer<Character> CHAR = new CharSerializer();

    private static final HashMap<String, TypeSerializer<?>> SERIALIZERS = new HashMap<>();
    static {{
        TypeSerializers.SERIALIZERS.put("list", TypeSerializers.LIST);
        TypeSerializers.SERIALIZERS.put("arraylist", TypeSerializers.LIST);
        TypeSerializers.SERIALIZERS.put("boolean", TypeSerializers.BOOLEAN);
        TypeSerializers.SERIALIZERS.put("string", TypeSerializers.STRING);
        TypeSerializers.SERIALIZERS.put("int", TypeSerializers.INTEGER);
        TypeSerializers.SERIALIZERS.put("integer", TypeSerializers.INTEGER);
        TypeSerializers.SERIALIZERS.put("double", TypeSerializers.DOUBLE);
        TypeSerializers.SERIALIZERS.put("float", TypeSerializers.FLOAT);
        TypeSerializers.SERIALIZERS.put("byte", TypeSerializers.BYTE);
        TypeSerializers.SERIALIZERS.put("short", TypeSerializers.SHORT);
        TypeSerializers.SERIALIZERS.put("long", TypeSerializers.LONG);
        TypeSerializers.SERIALIZERS.put("char", TypeSerializers.CHAR);
        TypeSerializers.SERIALIZERS.put("character", TypeSerializers.CHAR);
    }}

    public static final TypeSerializer<List<Boolean>> BOOLEAN_LIST = new BooleanListSerializer();
    public static final TypeSerializer<List<String>> STRING_LIST = new StringListSerializer();
    public static final TypeSerializer<List<Integer>> INTEGER_LIST = new IntegerListSerializer();
    public static final TypeSerializer<List<Double>> DOUBLE_LIST = new DoubleListSerializer();
    public static final TypeSerializer<List<Float>> FLOAT_LIST = new FloatListSerializer();
    public static final TypeSerializer<List<Byte>> BYTE_LIST = new ByteListSerializer();
    public static final TypeSerializer<List<Short>> SHORT_LIST = new ShortListSerializer();
    public static final TypeSerializer<List<Long>> LONG_LIST = new LongListSerializer();
    public static final TypeSerializer<List<Character>> CHAR_LIST = new CharListSerializer();

    private static final HashMap<String, TypeSerializer<?>> LIST_SERIALIZERS = new HashMap<>();
    static {{
        TypeSerializers.LIST_SERIALIZERS.put("boolean", TypeSerializers.BOOLEAN_LIST);
        TypeSerializers.LIST_SERIALIZERS.put("string", TypeSerializers.STRING_LIST);
        TypeSerializers.LIST_SERIALIZERS.put("int", TypeSerializers.INTEGER_LIST);
        TypeSerializers.LIST_SERIALIZERS.put("integer", TypeSerializers.INTEGER_LIST);
        TypeSerializers.LIST_SERIALIZERS.put("double", TypeSerializers.DOUBLE_LIST);
        TypeSerializers.LIST_SERIALIZERS.put("float", TypeSerializers.FLOAT_LIST);
        TypeSerializers.LIST_SERIALIZERS.put("byte", TypeSerializers.BYTE_LIST);
        TypeSerializers.LIST_SERIALIZERS.put("short", TypeSerializers.SHORT_LIST);
        TypeSerializers.LIST_SERIALIZERS.put("long", TypeSerializers.LONG_LIST);
        TypeSerializers.LIST_SERIALIZERS.put("char", TypeSerializers.CHAR_LIST);
        TypeSerializers.LIST_SERIALIZERS.put("character", TypeSerializers.CHAR_LIST);
    }}

    public static TypeSerializer<?> get(String type) {
        return TypeSerializers.SERIALIZERS.getOrDefault(type, TypeSerializers.NONE);
    }

    public static void addSerializer(String type, TypeSerializer<?> serializer) {
        TypeSerializers.SERIALIZERS.put(type, serializer);
    }

    public static void addListSerializer(String type, TypeSerializer<?> listSerializer) {
        TypeSerializers.LIST_SERIALIZERS.put(type, listSerializer);
    }
}
