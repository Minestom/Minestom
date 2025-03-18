package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static net.kyori.adventure.nbt.StringBinaryTag.stringBinaryTag;

/**
 * Can represent a builtin/vanilla sound or a custom sound.
 */
public sealed interface SoundEvent extends ProtocolObject, Keyed, Sound.Type, SoundEvents permits BuiltinSoundEvent, CustomSoundEvent {

    @NotNull NetworkBuffer.Type<SoundEvent> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, SoundEvent value) {
            switch (value) {
                case BuiltinSoundEvent soundEvent -> buffer.write(NetworkBuffer.VAR_INT, soundEvent.id() + 1);
                case CustomSoundEvent soundEvent -> {
                    buffer.write(NetworkBuffer.VAR_INT, 0); // Custom sound
                    buffer.write(NetworkBuffer.STRING, soundEvent.name());
                    buffer.write(NetworkBuffer.FLOAT.optional(), soundEvent.range());
                }
            }
        }

        @Override
        public SoundEvent read(@NotNull NetworkBuffer buffer) {
            int id = buffer.read(NetworkBuffer.VAR_INT) - 1;
            if (id != -1) return BuiltinSoundEvent.getId(id);

            Key key = Key.key(buffer.read(NetworkBuffer.STRING));
            return new CustomSoundEvent(key, buffer.read(NetworkBuffer.FLOAT.optional()));
        }
    };
    @NotNull Codec<SoundEvent> CODEC = null; // TODO(1.21.5)
    @NotNull BinaryTagSerializer<SoundEvent> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull SoundEvent value) {
            return switch (value) {
                case BuiltinSoundEvent soundEvent -> stringBinaryTag(soundEvent.name());
                case CustomSoundEvent soundEvent -> {
                    final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder()
                            .putString("sound_id", soundEvent.name());
                    if (soundEvent.range() != null) {
                        builder.putFloat("range", soundEvent.range());
                    }
                    yield builder.build();
                }
            };
        }

        @Override
        public @NotNull SoundEvent read(@NotNull Context context, @NotNull BinaryTag tag) {
            if (tag instanceof CompoundBinaryTag compound) {
                final String soundId = compound.getString("sound_id");
                final Float range = compound.getFloat("range");
                return new CustomSoundEvent(Key.key(soundId), range);
            }
            return BuiltinSoundEvent.getSafe(((StringBinaryTag) tag).value());
        }
    };

    /**
     * Get all the builtin sound events. Resource pack sounds will never be returned from this method.
     */
    static @NotNull Collection<? extends SoundEvent> values() {
        return BuiltinSoundEvent.values();
    }

    /**
     * Get a builtin sound event by its namespace ID. Will never return a custom/resource pack sound.
     *
     * @param key the key of the sound event
     * @return the sound event, or null if not found
     */
    static @Nullable SoundEvent fromKey(@NotNull String key) {
        return BuiltinSoundEvent.getSafe(key);
    }

    /**
     * Get a builtin sound event by its key. Will never return a custom/resource pack sound.
     *
     * @param key the key of the sound event
     * @return the sound event, or null if not found
     */
    static @Nullable SoundEvent fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    /**
     * Get a builtin sound event by its protocol ID. Will never return a custom/resource pack sound.
     *
     * @param id the ID of the sound event
     * @return the sound event, or null if not found
     */
    static @Nullable SoundEvent fromId(int id) {
        return BuiltinSoundEvent.getId(id);
    }

    /**
     * Create a custom sound event. The namespace should match a sound provided in the resource pack.
     *
     * @param key the key of the custom sound event
     * @param range       the range of the sound event, or null for (legacy) dynamic range
     * @return the custom sound event
     */
    static @NotNull SoundEvent of(@NotNull String key, @Nullable Float range) {
        return new CustomSoundEvent(Key.key(key), range);
    }

    /**
     * Create a custom sound event. The {@link Key} should match a sound provided in the resource pack.
     *
     * @param key the key of the custom sound event
     * @param range       the range of the sound event, or null for (legacy) dynamic range
     * @return the custom sound event
     */
    static @NotNull SoundEvent of(@NotNull Key key, @Nullable Float range) {
        return new CustomSoundEvent(key, range);
    }

    @Contract(pure = true)
    default @NotNull String name() {
        return key().asString();
    }

    @Override
    @Contract(pure = true)
    @NotNull Key key();

}
