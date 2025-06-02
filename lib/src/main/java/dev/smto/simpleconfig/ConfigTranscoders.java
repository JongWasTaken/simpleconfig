package dev.smto.simpleconfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.smto.simpleconfig.api.ConfigTranscoder;

import java.util.Optional;

public class ConfigTranscoders {
    public static final ConfigTranscoder<JsonElement> JSON = new ConfigTranscoder<>() {
        @Override
        public DynamicOps<JsonElement> getOps() {
            return JsonOps.INSTANCE;
        }
        @Override
        public String processEncoderOutput(Optional<JsonElement> result) {
            return ((JsonElement) result.orElseThrow()).toString() ;
        }
        @Override
        public JsonElement processDecoderInput(String input) {
            try {
                return JsonParser.parseString(input);
            } catch (Throwable ignored) {
                return null;
            }
        }
    };
}
