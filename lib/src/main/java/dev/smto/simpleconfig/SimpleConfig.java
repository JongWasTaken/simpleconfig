package dev.smto.simpleconfig;

import dev.smto.simpleconfig.api.ConfigAnnotations;
import dev.smto.simpleconfig.api.ConfigEntry;
import dev.smto.simpleconfig.api.ConfigLogger;
import dev.smto.simpleconfig.serializers.TypeSerializers;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
    private final List<ConfigEntry> configEntries = new ArrayList<>();

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     */
    public SimpleConfig(Path file, Class<?> configClass) {
        this(file, configClass, ConfigLoggers.NONE);
    }

    /**
     * Creates a new instance of SimpleConfig.
     * @param file Target path, which will be used without modification, so make sure it is valid!
     * @param configClass Class holding the static fields defining this config
     * @param logger Logger to use, check the ConfigLoggers class
     */
    public SimpleConfig(Path file, Class<?> configClass, ConfigLogger logger) {
        this.logger = logger;
        this.configFilePath = file;
        for (Field field : configClass.getFields()) {
            String comment = null;
            var commentAnnotation = field.getAnnotation(ConfigAnnotations.Comment.class);
            if (commentAnnotation != null) comment = commentAnnotation.comment();

            String holds = null;
            var annotation = field.getAnnotation(ConfigAnnotations.Holds.class);
            if (annotation != null) holds = annotation.type().getSimpleName();

            this.logger.debug("Config reference \"" + field.getName() + "\" is of type \""+ field.getType().getSimpleName() +"\", nestedType \""+ holds +"\"");
            this.configEntries.add(new ConfigEntry(field.getName(), comment, field, field.getType(), holds));
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
                    for (ConfigEntry field : this.configEntries) {
                        if (data[0].trim().equals(field.key())) {
                            var nData = data[1];
                            if (data.length > 2) {
                                for(int i = 2; i < data.length; i++) {
                                    nData += "=" + data[i];
                                }
                            }
                            try {
                                boolean success = this.applyToField(field, nData.trim());
                                if (!success) throw new RuntimeException("Could not set field: \"" + field.key() + "\"");
                                break;
                            } catch (Exception ignored) {
                                this.logger.warn("Could not fully read config file \""+this.configFilePath.getFileName().toString()+"\"!");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Writes all values of the config class to the target file.
     */
    public void write() {
        // Write current values to the file
        try {
            var out = new StringBuilder();
            for (ConfigEntry field : this.configEntries) {
                if (field.comment() != null) {
                    for (String s : field.comment().split("\n")) {
                        out.append("# ").append(s).append("\n");
                    }
                }
                out.append(field.key()).append("=").append(field.reference().get(null)).append("\n");
            }
            Files.writeString(this.configFilePath, ""); // if this write fails, the file will not be deleted due to the exception
            Files.delete(this.configFilePath);
            Files.writeString(this.configFilePath, out);
        } catch (Exception ignored) {
            this.logger.error("Could not write config file \""+ this.configFilePath.getFileName().toString() +"\"! Changes will not be saved!");
        }
    }

    /**
     * Tries to set the value of the config entry with the given key to the given value.
     * Returns true if the value was set, false if it was not found or could not be set.
     */
    public boolean trySet(String key, String value) {
        for (ConfigEntry entry : this.configEntries) {
            if (entry.key().equals(key)) {
                return this.applyToField(entry, value);
            }
        }
        return false;
    }

    private boolean applyToField(ConfigEntry entry, String value) {
        String type = entry.type().getSimpleName().toLowerCase(Locale.ROOT);
        String holds = null;
        if (entry.nestedType() != null) holds = entry.nestedType().toLowerCase(Locale.ROOT);
        this.logger.debug("Applying value \""+value+"\" to config entry \""+entry.key()+"\" of type \""+type+"\"");
        try {
            return TypeSerializers.get(type).serialize(entry, value, holds);
        } catch (Throwable ignored) {
            this.logger.warn("Could not apply value \""+value+"\" to config entry \""+entry.key()+"\"!");
        }
        return false;
    }

    public record Pair<T, U>(T first, U second) {}

    /**
     * Returns immutable pairs of all config keys and values as a list for presentation purposes, which can then be used for GUIs or commands for example.
     */
    public List<Pair<String,String>> toPairList() {
        var out = new ArrayList<Pair<String,String>>();
        for (ConfigEntry entry : this.configEntries) {
            try {
                out.add(new Pair<>(entry.key(), entry.reference().get(null).toString()));
            } catch (Throwable ignored) {
                this.logger.warn("Could not get value of config entry \""+entry.key()+"\"!");
                out.add(new Pair<>(entry.key(), ""));
            }
        }
        return out;
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
        for (ConfigEntry entry : this.configEntries) {
            try {
                out.put(entry.key(), entry.reference().get(null).toString());
            } catch (Throwable ignored) {
                this.logger.warn("Could not get value of config entry \""+entry.key()+"\"!");
                out.put(entry.key(), "");
            }
        }
        return out;
    }
}

