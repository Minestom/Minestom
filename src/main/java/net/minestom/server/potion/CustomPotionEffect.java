package net.minestom.server.potion;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

/**
 * Represents a custom effect in {@link net.minestom.server.component.DataComponents#POTION_CONTENTS}.
 */
public record CustomPotionEffect(PotionEffect id, Settings settings) {

    public static final NetworkBuffer.Type<CustomPotionEffect> NETWORK_TYPE = NetworkBufferTemplate.template(
            PotionEffect.NETWORK_TYPE, CustomPotionEffect::id,
            Settings.NETWORK_TYPE, CustomPotionEffect::settings,
            CustomPotionEffect::new);
    public static final Codec<CustomPotionEffect> CODEC = StructCodec.struct(
            "id", PotionEffect.CODEC, CustomPotionEffect::id,
            StructCodec.INLINE, Settings.CODEC, CustomPotionEffect::settings,
            CustomPotionEffect::new);

    public CustomPotionEffect(PotionEffect id, int amplifier, int duration, boolean isAmbient, boolean showParticles, boolean showIcon) {
        this(id, new Settings(amplifier, duration, isAmbient, showParticles, showIcon, null));
    }

    public int amplifier() {
        return settings.amplifier;
    }

    public int duration() {
        return settings.duration;
    }

    public boolean isAmbient() {
        return settings.isAmbient;
    }

    public boolean showParticles() {
        return settings.showParticles;
    }

    public boolean showIcon() {
        return settings.showIcon;
    }

    public record Settings(
            int amplifier, int duration,
            boolean isAmbient, boolean showParticles, boolean showIcon,
            @Nullable Settings hiddenEffect
    ) {
        public static final NetworkBuffer.Type<Settings> NETWORK_TYPE = NetworkBuffer.Recursive(self -> NetworkBufferTemplate.template(
                VAR_INT, Settings::amplifier,
                VAR_INT, Settings::duration,
                BOOLEAN, Settings::isAmbient,
                BOOLEAN, Settings::showParticles,
                BOOLEAN, Settings::showIcon,
                self.optional(), Settings::hiddenEffect,
                Settings::new
        ));

        public static final Codec<Settings> CODEC = Codec.Recursive(self -> StructCodec.struct(
                "amplifier", Codec.BYTE.optional((byte) 0), s -> (byte) s.amplifier,
                "duration", Codec.INT.optional(0), Settings::duration,
                "ambient", Codec.BOOLEAN.optional(false), Settings::isAmbient,
                "show_particles", Codec.BOOLEAN.optional(true), Settings::showParticles,
                "show_icon", Codec.BOOLEAN.optional(), Settings::showIcon,
                "hidden_effect", self.optional(), Settings::hiddenEffect,
                Settings::withOptionalIcon
        ));

        // Exists because showIcon needs to default to the value of showParticles which we can't do inline.
        private static Settings withOptionalIcon(
                byte amplifier, int duration,
                boolean isAmbient, boolean showParticles,
                @Nullable Boolean showIcon,
                @Nullable Settings hiddenEffect
        ) {
            return new Settings(amplifier, duration, isAmbient, showParticles,
                    Objects.requireNonNullElse(showIcon, showParticles), hiddenEffect);
        }

    }

}
