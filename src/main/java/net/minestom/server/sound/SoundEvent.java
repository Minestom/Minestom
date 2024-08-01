package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.NamespaceID;
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
                    buffer.writeOptional(NetworkBuffer.FLOAT, soundEvent.range());
                }
            }
        }

        @Override
        public SoundEvent read(@NotNull NetworkBuffer buffer) {
            int id = buffer.read(NetworkBuffer.VAR_INT) - 1;
            if (id != -1) return BuiltinSoundEvent.getId(id);

            Key key = Key.key(buffer.read(NetworkBuffer.STRING));
            return new CustomSoundEvent(key, buffer.readOptional(NetworkBuffer.FLOAT));
        }
    };

    /**
     * Get all the builtin sound events. Resource pack sounds will never be returned from this method.
     */
    static @NotNull Collection<? extends SoundEvent> values() {
        return BuiltinSoundEvent.values();
    }

    /**
     * Get a builtin sound event by its key ID. Will never return a custom/resource pack sound.
     *
     * @param key the {@link Key} of the sound event
     * @return the sound event, or null if not found
     */
    static @Nullable SoundEvent fromKey(@NotNull String key) {
        return BuiltinSoundEvent.getSafe(key);
    }

    /**
     * Get a builtin sound event by its key ID. Will never return a custom/resource pack sound.
     *
     * @param key the {@link Key} of the sound event
     * @return the sound event, or null if not found
     */
    static @Nullable SoundEvent fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    /**
     * @deprecated use {@link #fromKey(String)}
     */
    @Deprecated
    static SoundEvent fromNamespaceId(@NotNull String namespaceID) {
        return fromKey(namespaceID);
    }

    /**
     * @deprecated use {@link #fromKey(Key)}
     */
    @Deprecated
    static SoundEvent fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromKey(namespaceID);
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
     * Create a custom sound event. The key should match a sound provided in the resource pack.
     *
     * @param key the key ID of the custom sound event
     * @param range the range of the sound event, or null for (legacy) dynamic range
     * @return the custom sound event
     */
    static @NotNull SoundEvent of(@NotNull String key, @Nullable Float range) {
        return new CustomSoundEvent(Key.key(key), range);
    }

    /**
     * Create a custom sound event. The {@link Key} should match a sound provided in the resource pack.
     * @param key the key ID of the custom sound event
     * @param range the range of the sound event, or null for (legacy) dynamic range
     * @return the custom sound event
     */
    static @NotNull SoundEvent of(@NotNull Key key, @Nullable Float range) {
        return new CustomSoundEvent(key, range);
    }

    @Contract(pure = true)
    default @NotNull String name() {
        return key().asString();
    }



    /**
     * @deprecated use {@link #key()}
     */
    @Deprecated
    @Contract(pure = true)
    default @NotNull NamespaceID namespace() {
        return NamespaceID.from(key());
    }

}
