package dev.smto.simpleconfig;

import dev.smto.simpleconfig.api.ConfigAnnotations;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;

public class SimpleConfigExampleApplication {
    public static void main(String[] args) {
        SimpleConfig manager = new SimpleConfig(Path.of("test.conf"), TestConfig.class, ConfigLoggers.SYSTEM_OUT);
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
        @ConfigAnnotations.Holds(type = String.class)
        public static ArrayList<String> testList = new ArrayList<>() {{
            this.add("Hello");
            this.add("World");
        }};
        public static char testChar = '~';
        public static byte testByte = 42;
        public static short testShort = 41;
        public static long testLong = 1201136465L;
    }
}
