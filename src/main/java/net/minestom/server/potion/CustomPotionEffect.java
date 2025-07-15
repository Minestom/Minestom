package net.minestom.server.potion;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jspecify.annotations.Nullable;

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
        public static final NetworkBuffer.Type<Settings> NETWORK_TYPE = new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, Settings value) {
                buffer.write(VAR_INT, value.amplifier);
                buffer.write(VAR_INT, value.duration);
                buffer.write(BOOLEAN, value.isAmbient);
                buffer.write(BOOLEAN, value.showParticles);
                buffer.write(BOOLEAN, value.showIcon);
                buffer.write(NETWORK_TYPE.optional(), value.hiddenEffect);
            }

            @Override
            public Settings read(NetworkBuffer buffer) {
                return new Settings(
                        buffer.read(VAR_INT),
                        buffer.read(VAR_INT),
                        buffer.read(BOOLEAN),
                        buffer.read(BOOLEAN),
                        buffer.read(BOOLEAN),
                        buffer.read(NETWORK_TYPE.optional())
                );
            }
        };
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
