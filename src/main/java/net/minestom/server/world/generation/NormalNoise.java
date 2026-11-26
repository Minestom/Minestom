package net.minestom.server.world.generation;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.utils.validate.Check;

import java.util.List;

/**
 * The parameters of one octaved perlin noise, as a block state provider spells them out inline.
 *
 * @param baseAmplitude      the amplitude of the first octave, between 1.0E-5 and 1.0E6
 * @param baseOctave         the octave the noise starts at, between -32 and 32
 * @param octaveCount        how many octaves are summed, between 1 and 32
 * @param normalize          whether the summed octaves are scaled back into range
 * @param amplitudeModifiers one factor per octave, each between 0 and 1.0E6, either empty or exactly {@code octaveCount} long
 */
public record NormalNoise(
        double baseAmplitude,
        int baseOctave,
        int octaveCount,
        boolean normalize,
        List<Double> amplitudeModifiers
) {
    public static final Codec<NormalNoise> CODEC = StructCodec.struct(
            "base_amplitude", Codec.DOUBLE.optional(1.0), NormalNoise::baseAmplitude,
            "base_octave", Codec.INT, NormalNoise::baseOctave,
            "octave_count", Codec.INT.optional(1), NormalNoise::octaveCount,
            "normalize", Codec.BOOLEAN.optional(true), NormalNoise::normalize,
            "amplitude_modifiers", Codec.DOUBLE.list(32).optional(List.of()), NormalNoise::amplitudeModifiers,
            NormalNoise::new);

    public NormalNoise {
        amplitudeModifiers = List.copyOf(amplitudeModifiers);
        Check.argCondition(baseAmplitude < 1.0E-5F || baseAmplitude > 1.0E6, "Base amplitude must be between 1.0E-5 and 1.0E6");
        Check.argCondition(baseOctave < -32 || baseOctave > 32, "Base octave must be between -32 and 32");
        Check.argCondition(octaveCount < 1 || octaveCount > 32, "Octave count must be between 1 and 32");
        for (double modifier : amplitudeModifiers) {
            Check.argCondition(modifier < 0 || modifier > 1.0E6, "Amplitude modifiers must be between 0 and 1.0E6");
        }
        if (!amplitudeModifiers.isEmpty() && amplitudeModifiers.size() != octaveCount) {
            throw new IllegalArgumentException("amplitude_modifiers had size " + amplitudeModifiers.size()
                    + ", but octave_count was " + octaveCount);
        }
    }
}
