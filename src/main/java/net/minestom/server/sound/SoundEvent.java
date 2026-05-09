package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Can represent a builtin/vanilla sound or a custom sound.
 */
public sealed interface SoundEvent extends Keyed, Sound.Type, SoundEvents permits BuiltinSoundEvent, CustomSoundEvent {

    @SuppressWarnings("unchecked")
    NetworkBuffer.Type<SoundEvent> NETWORK_TYPE = NetworkBuffer.Tagged(
            NetworkBuffer.VAR_INT, value -> value instanceof BuiltinSoundEvent builtin ? builtin.id() + 1 : 0,
            rawId -> {
                int id = rawId - 1;
                if (id == -1)
                    return (NetworkBuffer.Type<SoundEvent>) (NetworkBuffer.Type<?>) NetworkBufferTemplate.template(
                            NetworkBuffer.KEY, SoundEvent::key,
                            NetworkBuffer.FLOAT.optional(), CustomSoundEvent::range,
                            CustomSoundEvent::new);
                return (NetworkBuffer.Type<SoundEvent>) (NetworkBuffer.Type<?>) NetworkBufferTemplate.template(BuiltinSoundEvent.REGISTRY.get(id));
            }
    );
    Codec<SoundEvent> CODEC = new Codec<>() {
        @Override
        public <D> Result<SoundEvent> decode(Transcoder<D> coder, D value) {
            final Result<String> stringResult = coder.getString(value);
            if (stringResult instanceof Result.Ok(String string)) {
                final SoundEvent soundEvent = BuiltinSoundEvent.get(string);
                if (soundEvent == null) return new Result.Error<>("Unknown sound event: " + string);
                return new Result.Ok<>(soundEvent);
            }

            final Result<CustomSoundEvent> customResult = CustomSoundEvent.CODEC.decode(coder, value);
            if (customResult instanceof Result.Ok(CustomSoundEvent customSoundEvent))
                return new Result.Ok<>(customSoundEvent);
            return customResult.cast();
        }

        @Override
        public <D> Result<D> encode(Transcoder<D> coder, @Nullable SoundEvent value) {
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
    static Collection<? extends SoundEvent> values() {
        return BuiltinSoundEvent.REGISTRY.values();
    }

    /**
     * Get a builtin sound event by its namespace ID. Will never return a custom/resource pack sound.
     *
     * @param key the key of the sound event
     * @return the sound event, or null if not found
     */
    static @Nullable SoundEvent fromKey(@KeyPattern String key) {
        return fromKey(Key.key(key));
    }

    /**
     * Get a builtin sound event by its key. Will never return a custom/resource pack sound.
     *
     * @param key the key of the sound event
     * @return the sound event, or null if not found
     */
    static @Nullable SoundEvent fromKey(Key key) {
        return BuiltinSoundEvent.REGISTRY.get(key);
    }

    /**
     * Get a builtin sound event by its protocol ID. Will never return a custom/resource pack sound.
     *
     * @param id the ID of the sound event
     * @return the sound event, or null if not found
     */
    static @Nullable SoundEvent fromId(int id) {
        return BuiltinSoundEvent.REGISTRY.get(id);
    }

    /**
     * Create a custom sound event. The namespace should match a sound provided in the resource pack.
     *
     * @param key   the key of the custom sound event
     * @param range the range of the sound event, or null for (legacy) dynamic range
     * @return the custom sound event
     */
    static SoundEvent of(String key, @Nullable Float range) {
        return new CustomSoundEvent(Key.key(key), range);
    }

    /**
     * Create a custom sound event. The {@link Key} should match a sound provided in the resource pack.
     *
     * @param key   the key of the custom sound event
     * @param range the range of the sound event, or null for (legacy) dynamic range
     * @return the custom sound event
     */
    static SoundEvent of(Key key, @Nullable Float range) {
        return new CustomSoundEvent(key, range);
    }

    @Contract(pure = true)
    default String name() {
        return key().asString();
    }

    @Override
    @Contract(pure = true)
    Key key();

}
