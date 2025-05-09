package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.ProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

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

            return new CustomSoundEvent(buffer.read(NetworkBuffer.KEY),
                    buffer.read(NetworkBuffer.FLOAT.optional()));
        }
    };
    @NotNull Codec<SoundEvent> CODEC = new Codec<>() {
        @Override
        public @NotNull <D> Result<SoundEvent> decode(@NotNull Transcoder<D> coder, @NotNull D value) {
            final Result<String> stringResult = coder.getString(value);
            if (stringResult instanceof Result.Ok(String string)) {
                final SoundEvent soundEvent = BuiltinSoundEvent.getSafe(string);
                if (soundEvent == null) return new Result.Error<>("Unknown sound event: " + string);
                return new Result.Ok<>(soundEvent);
            }

            final Result<CustomSoundEvent> customResult = CustomSoundEvent.CODEC.decode(coder, value);
            if (customResult instanceof Result.Ok(CustomSoundEvent customSoundEvent))
                return new Result.Ok<>(customSoundEvent);
            return customResult.cast();
        }

        @Override
        public @NotNull <D> Result<D> encode(@NotNull Transcoder<D> coder, @Nullable SoundEvent value) {
            if (value == null) return new Result.Error<>("null");
            return switch (value) {
                case BuiltinSoundEvent soundEvent -> new Result.Ok<>(coder.createString(soundEvent.name()));
                case CustomSoundEvent soundEvent -> CustomSoundEvent.CODEC.encode(coder, soundEvent);
            };
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
