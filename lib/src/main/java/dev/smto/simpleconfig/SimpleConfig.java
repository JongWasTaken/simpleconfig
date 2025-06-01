package dev.smto.simpleconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.smto.simpleconfig.api.ConfigAnnotations;
import dev.smto.simpleconfig.api.ConfigDecoration;
import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.ConfigLogger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dead simple config system, using some reflection.
 */
@SuppressWarnings("unused")
public class SimpleConfig {
    private ConfigLogger logger;

    public void setLogger(ConfigLogger logger) {
        this.logger = logger;
    }

    private final Path configFilePath;
    private final List<ConfigEntry<?>> configEntries = new ArrayList<>();

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     */
    public SimpleConfig(Path file, Class<?> configClass) {
        this(file, configClass, ConfigLoggers.NONE, new HashMap<>());
    }

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     * @param logger Logger to use, check the ConfigLoggers class
     */
    public SimpleConfig(Path file, Class<?> configClass, ConfigLogger logger) {
        this(file, configClass, logger, new HashMap<>());
    }

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     * @param logger Logger to use, check the ConfigLoggers class
     * @param codecOverrides Assign custom codecs for specific field names
     */
    public SimpleConfig(Path file, Class<?> configClass, ConfigLogger logger, Map<String, Codec<?>> codecOverrides) {
        this.logger = logger;
        this.configFilePath = file;
        for (Field field : configClass.getFields()) {
            String section = null;
            var sectionAnnotation = field.getAnnotation(ConfigAnnotations.Section.class);
            if (sectionAnnotation != null) section = sectionAnnotation.section();

            String comment = null;
            var commentAnnotation = field.getAnnotation(ConfigAnnotations.Comment.class);
            if (commentAnnotation != null) comment = commentAnnotation.comment();

            Codec<?> codec;
            if (codecOverrides.containsKey(field.getName())) {
                codec = codecOverrides.get(field.getName());
            } else {
                codec = CodecHandler.get(field);
            }

            try {
                this.configEntries.add(new ConfigEntry<>(field.getName(), new ConfigDecoration(section, comment), field, codec));
            } catch (Throwable x) {
                throw new RuntimeException(x);
            }
        }

        try {
            Files.createDirectories(file.getParent());
        } catch (Throwable ignored) {}

        // If the file is not readable, abort and use default values
        if (this.configFilePath.toFile().exists() && !this.configFilePath.toFile().canRead()) {
            this.logger.warn("Could not read config file \""+this.configFilePath.getFileName().toString()+"\"! Default values will be used!");
            return;
        }
        this.read();
        this.write();
    }

    /**
     * Reads and parses the config file, applying all values to the static fields of the config class.
     */
    public void read() {
        // If the file exists, read and parse it
        if (this.configFilePath.toFile().exists()) {
            try {
                for (String line : Files.readAllLines(this.configFilePath)) {
                    var data = line.split("#")[0].trim().split("=");
                    for (ConfigEntry<?> field : this.configEntries) {
                        if (data[0].trim().equals(field.key())) {
                            try {
                                StringBuilder nData = new StringBuilder(data[1]);
                                if (data.length > 2) {
                                    for(int i = 2; i < data.length; i++) {
                                        nData.append("=").append(data[i]);
                                    }
                                }
                                try {
                                    boolean success = this.applyToField(field, nData.toString().trim());
                                    if (!success) throw new RuntimeException("Could not set field: \"" + field.key() + "\"");
                                    break;
                                } catch (Exception ignored) {
                                    this.logger.warn("Could not fully read config file \""+this.configFilePath.getFileName().toString()+"\"!");
                                }
                            } catch (Throwable ignored) {}
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String encodeField(ConfigEntry<?> field) {
        if (field.codec() == null) {
            return "";
        }

        Method m;
        try {
            m = field.codec().getClass().getMethod("encodeStart", DynamicOps.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        DataResult<?> d;
        try {
            d = (DataResult<?>) m.invoke(field.codec(), JsonOps.INSTANCE, field.reference().get(null));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        var res = (JsonElement) d.resultOrPartial(this.logger::warn).orElseThrow();
        System.out.println(res);
        return res.toString();
    }

    /**
     * Writes all values of the config class to the target file.
     */
    public void write() {
        // Write current values to the file
        try {
            var out = new StringBuilder();
            for (ConfigEntry<?> field : this.configEntries) {
                if (field.decorations() != null) {
                    if (field.decorations().section() != null) {
                        StringBuilder sectionLength = new StringBuilder();
                        sectionLength.append("=".repeat(field.decorations().section().length()));
                        out.append("# ").append(sectionLength).append("\n");
                        out.append("# ");
                        out.append(field.decorations().section()).append("\n");
                        out.append("# ").append(sectionLength).append("\n");
                    }
                    if (field.decorations().comment() != null) {
                        for (String s : field.decorations().comment().split("\n")) {
                            out.append("# ").append(s).append("\n");
                        }
                    }
                }
                try {
                    out.append(field.key()).append("=").append(this.encodeField(field)).append("\n");
                } catch (Throwable ignored) {
                    out.append("null").append("\n");
                }
            }
            Files.writeString(this.configFilePath, ""); // if this write fails, the file will not be deleted due to the exception
            Files.delete(this.configFilePath);
            Files.writeString(this.configFilePath, out);
        } catch (Exception ignored) {
            this.logger.error("Could not write config file \""+ this.configFilePath.getFileName().toString() +"\"! Changes will not be saved!");
        }
    }

    public void reload() {
        this.read();
    }

    /**
     * Tries to set the value of the config entry with the given key to the given value.
     * Returns true if the value was set, false if it was not found or could not be set.
     */
    public boolean trySet(String key, String value) {
        for (ConfigEntry<?> entry : this.configEntries) {
            if (entry.key().equals(key)) {
                return this.applyToField(entry, value);
            }
        }
        return false;
    }

    private boolean applyToField(ConfigEntry<?> entry, String value) {
        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(value);
        } catch (Throwable ignored) {
            return false;
        }
        try {
            this.logger.debug("Applying value \""+value+"\" to config entry \""+entry.key()+"\"");
            var newVal = entry.codec().parse(JsonOps.INSTANCE, parsed).resultOrPartial().orElseThrow();
            if (newVal instanceof List) {
                newVal = new ArrayList<>((List<?>) newVal);
            }
            entry.reference().set(null, newVal);
        } catch (Throwable ignored) {
            this.logger.warn("Could not apply value \""+value+"\" to config entry \""+entry.key()+"\"!");
            return false;
        }

        return true;
    }

    /**
     * Returns list of all config keys.
     */
    public List<String> getKeys() {
        return this.configEntries.stream().map(ConfigEntry::key).toList();
    }

    /**
     * Creates a HashMap of all config keys and their values. Changing the HashMap will not change the values in the config file.
     */
    public HashMap<String,String> toMap() {
        var out = new HashMap<String,String>();
        for (ConfigEntry<?> entry : this.configEntries) {
            try {
                out.put(entry.key(), this.encodeField(entry));
            } catch (Throwable ignored) {
                this.logger.warn("Could not get value of config entry \""+entry.key()+"\"!");
                out.put(entry.key(), "");
            }
        }
        return out;
    }

    /**
     * Creates a Helper class which makes interacting with the config file easier when using Minecraft commands.
     * You should save the return value of this method to a variable.
     * NOTE: This class uses com.mojang.brigadier.*! This method will return null if it is not present.
     */
    public MinecraftCommandHelper getMinecraftCommandHelper() {
        try {
            Class.forName("com.mojang.brigadier.suggestion.Suggestion", false, this.getClass().getClassLoader());
            return new MinecraftCommandHelper(this);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}

