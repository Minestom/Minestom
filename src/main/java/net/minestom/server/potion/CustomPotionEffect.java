package net.minestom.server.potion;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

/**
 * Represents a custom effect in {@link net.minestom.server.item.ItemComponent#POTION_CONTENTS}.
 */
public record CustomPotionEffect(@NotNull PotionEffect id, @NotNull Settings settings) {

    public static final NetworkBuffer.Type<CustomPotionEffect> NETWORK_TYPE = NetworkBufferTemplate.template(
            PotionEffect.NETWORK_TYPE, CustomPotionEffect::id,
            Settings.NETWORK_TYPE, CustomPotionEffect::settings,
            CustomPotionEffect::new
    );

    public static final BinaryTagSerializer<CustomPotionEffect> NBT_TYPE = BinaryTagSerializer.lazy(() -> BinaryTagSerializer.COMPOUND.map(
            tag -> new CustomPotionEffect(
                    PotionEffect.fromNamespaceId(tag.getString("id")),
                    Settings.NBT_TYPE.read(tag)),
            value -> CompoundBinaryTag.builder()
                    .putString("id", value.id.name())
                    .put((CompoundBinaryTag) Settings.NBT_TYPE.write(value.settings))
                    .build()
    ));

    public CustomPotionEffect(@NotNull PotionEffect id, byte amplifier, int duration, boolean isAmbient, boolean showParticles, boolean showIcon) {
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
            public void write(@NotNull NetworkBuffer buffer, Settings value) {
                buffer.write(VAR_INT, value.amplifier);
                buffer.write(VAR_INT, value.duration);
                buffer.write(BOOLEAN, value.isAmbient);
                buffer.write(BOOLEAN, value.showParticles);
                buffer.write(BOOLEAN, value.showIcon);
                buffer.write(NETWORK_TYPE.optional(), value.hiddenEffect);
            }

            @Override
            public Settings read(@NotNull NetworkBuffer buffer) {
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

        public static final BinaryTagSerializer<Settings> NBT_TYPE = BinaryTagSerializer.recursive(self -> BinaryTagSerializer.COMPOUND.map(
                tag -> {
                    byte amplifier = tag.getByte("amplifier");
                    int duration = tag.getInt("duration");
                    boolean ambient = tag.getBoolean("ambient", false);
                    boolean showParticles = tag.getBoolean("show_particles", true);
                    boolean showIcon = tag.getBoolean("show_icon", showParticles);
                    Settings hiddenEffect = null;
                    if (tag.get("hidden_effect") instanceof CompoundBinaryTag hiddenEffectTag) {
                        hiddenEffect = self.read(hiddenEffectTag);
                    }
                    return new Settings(amplifier, duration, ambient, showParticles, showIcon, hiddenEffect);
                },
                value -> {
                    CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                    if (value.amplifier != 0) builder.putByte("amplifier", (byte) value.amplifier);
                    builder.putInt("duration", value.duration);
                    if (value.isAmbient) builder.putBoolean("ambient", true);
                    if (!value.showParticles) builder.putBoolean("show_particles", false);
                    builder.putBoolean("show_icon", value.showIcon);
                    if (value.hiddenEffect != null) {
                        builder.put("hidden_effect", self.write(value.hiddenEffect));
                    }
                    return builder.build();
                }
        ));

    }

}
