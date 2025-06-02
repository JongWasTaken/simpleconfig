package dev.smto.simpleconfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.smto.simpleconfig.api.*;

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
 * A dead simple config system.
 */
@SuppressWarnings("unused")
public class SimpleConfig {
    private ConfigLogger logger;

    public void setLogger(ConfigLogger logger) {
        this.logger = logger;
    }

    private final ConfigTranscoder transcoder;
    private final Path configFilePath;
    private final List<ConfigEntry<?>> configEntries = new ArrayList<>();

    private MinecraftCommandHelper minecraftCommandHelper = null;

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     */
    public SimpleConfig(Path file, Class<?> configClass) {
        this(file, configClass, ConfigLoggers.NONE, ConfigTranscoders.JSON, new HashMap<>());
    }

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     * @param logger Logger to use, check the ConfigLoggers class
     */
    public SimpleConfig(Path file, Class<?> configClass, ConfigLogger logger) {
        this(file, configClass, logger, ConfigTranscoders.JSON, new HashMap<>());
    }

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     * @param codecOverrides Assign custom codecs for specific field names
     */
    public SimpleConfig(Path file, Class<?> configClass, Map<String, Codec<?>> codecOverrides) {
        this(file, configClass, ConfigLoggers.NONE, ConfigTranscoders.JSON, codecOverrides);
    }

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     * @param logger Logger to use, check the ConfigLoggers class
     * @param codecOverrides Assign custom codecs for specific field names
     */
    public SimpleConfig(Path file, Class<?> configClass, ConfigLogger logger, Map<String, Codec<?>> codecOverrides) {
        this(file, configClass, logger, ConfigTranscoders.JSON, codecOverrides);
    }

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     * @param logger Logger to use, check the ConfigLoggers class
     * @param transcoder Transcoder to use, default is ConfigTranscoders.JSON
     */
    public SimpleConfig(Path file, Class<?> configClass, ConfigLogger logger, ConfigTranscoder<?> transcoder) {
        this(file, configClass, logger, transcoder, new HashMap<>());
    }

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     * @param logger Logger to use, check the ConfigLoggers class
     * @param transcoder Transcoder to use, default is ConfigTranscoders.JSON
     * @param codecOverrides Assign custom codecs for specific field names
     */
    public SimpleConfig(Path file, Class<?> configClass, ConfigLogger logger, ConfigTranscoder<?> transcoder, Map<String, Codec<?>> codecOverrides) {
        this.logger = logger;
        this.configFilePath = file;
        this.transcoder = transcoder;
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
                codec = ConfigCodecs.get(field);
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

        try {
            Class.forName("com.mojang.brigadier.suggestion.Suggestion", false, this.getClass().getClassLoader());
            this.minecraftCommandHelper = new MinecraftCommandHelper(this);
        } catch (ClassNotFoundException ignored) {}
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
                            } catch (Throwable ignored) {
                                this.logger.error("Failed to parse config file line: \""+line+"\"!");
                            }
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
        String out;
        try {
            out = this.transcoder.processEncoderOutput(((DataResult<?>) m.invoke(field.codec(), this.transcoder.getOps(), field.reference().get(null))).resultOrPartial(this.logger::warn));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return out;
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
                    this.logger.warn("Failed to encode field: \""+field.key()+"\"! This could indicate a broken codec or input.");
                    out.append("\n");
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
        var parsed = this.transcoder.processDecoderInput(value);
        if (parsed == null) {
            return false;
        }
        try {
            this.logger.debug("Applying value \""+value+"\" to config entry \""+entry.key()+"\"");
            var newVal = entry.codec().parse(this.transcoder.getOps(), parsed).resultOrPartial().orElseThrow();
            // codec.parse always returns an immutable list
            if (newVal instanceof List) {
                newVal = new ArrayList<>((List<?>) newVal);
            }
            entry.reference().set(null, newVal);
        } catch (Throwable ignored) {
            this.logger.warn("Failed to decode \""+value+"\" for config entry \""+entry.key()+"\"! This could indicate a broken codec or input.");
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
     * Returns a helper class which makes interacting with the config file easier when using Minecraft commands.
     * NOTE: This class uses com.mojang.brigadier.*! This method will return null if that library is not present in the classpath.
     */
    public MinecraftCommandHelper getMinecraftCommandHelper() {
        return this.minecraftCommandHelper;
    }
}

