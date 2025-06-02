package dev.smto.simpleconfig.api;

import com.mojang.serialization.DynamicOps;

import java.util.Optional;

public interface ConfigTranscoder<T> {
    DynamicOps<T> getOps();
    String processEncoderOutput(Optional<T> result);
    T processDecoderInput(String input);
}
