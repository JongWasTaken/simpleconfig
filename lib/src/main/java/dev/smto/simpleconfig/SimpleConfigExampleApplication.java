package dev.smto.simpleconfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SimpleConfigExampleApplication {
    public static void main(String[] args) {
        SimpleConfig configManager = new SimpleConfig(Path.of("test.conf"), TestConfig.class, ConfigLoggers.SYSTEM_OUT, new HashMap<>() {{
            this.put("testRecord", TestRecord.CODEC);
        }});
        if (configManager.getMinecraftCommandHelper() == null) {
            System.out.println("Brigadier is not loaded!");
        }
        for (Field field : TestConfig.class.getFields()) {
            try {
                System.out.println(field.getName() + ": " + field.get(null) + ", of type " + field.getType().getSimpleName());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class TestConfig {
        public static String testString = "Hello World";
        public static int testInt = 42;
        public static boolean testBoolean = true;
        public static double testDouble = 3.14;
        public static float testFloat = 3.14f;
        public static ArrayList<String> testList = new ArrayList<>() {{
            this.add("Hello");
            this.add("World");
        }};
        public static Map<String, Integer> testMap = new HashMap<>() {{
            this.put("Hello", 1);
            this.put("World", 2);
        }};
        public static char testChar = '~';
        public static byte testByte = 42;
        public static short testShort = 41;
        public static long testLong = 1201136465L;
        public static TestRecord testRecord = new TestRecord("John Doe", 42);
    }

    public record TestRecord(String name, int age) {
        public static Codec<TestRecord> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(TestRecord::name),
                Codec.INT.fieldOf("age").forGetter(TestRecord::age)
        ).apply(instance, TestRecord::new));
    }
}
