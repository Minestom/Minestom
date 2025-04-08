package net.minestom.server.config;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

import java.util.List;

public record NoiseConfig(int firstOctave, List<Double> amplitudes) {
    public static final StructCodec<NoiseConfig> CODEC = StructCodec.struct(
            "firstOctave", Codec.INT, NoiseConfig::firstOctave,
            "amplitudes", Codec.DOUBLE.list(), NoiseConfig::amplitudes,
            NoiseConfig::new
    );
}
