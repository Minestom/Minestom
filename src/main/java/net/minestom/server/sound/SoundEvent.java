package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Can represent a builtin/vanilla sound or a custom sound.
 */
public sealed interface SoundEvent extends ProtocolObject, Sound.Type, SoundEvents permits BuiltinSoundEvent, CustomSoundEvent {

    @NotNull NetworkBuffer.Type<SoundEvent> NETWORK_TYPE = NetworkBuffer.lazy(() -> BuiltinSoundEvent.NETWORK_TYPE); //todo what is the init issue here??

    static @NotNull Collection<? extends SoundEvent> values() {
        return BuiltinSoundEvent.values();
    }

    /**
     * Get a builtin sound event by its namespace ID. Will never return a custom/resource pack sound.
     *
     * @param namespaceID the namespace ID of the sound event
     * @return the sound event, or null if not found
     */
    static @Nullable SoundEvent fromNamespaceId(@NotNull String namespaceID) {
        return BuiltinSoundEvent.getSafe(namespaceID);
    }

    /**
     * Get a builtin sound event by its namespace ID. Will never return a custom/resource pack sound.
     *
     * @param namespaceID the namespace ID of the sound event
     * @return the sound event, or null if not found
     */
    static @Nullable SoundEvent fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
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
     * @param namespaceID the namespace ID of the custom sound event
     * @param range the range of the sound event, or null for (legacy) dynamic range
     * @return the custom sound event
     */
    static @NotNull SoundEvent of(@NotNull String namespaceID, @Nullable Float range) {
        return new CustomSoundEvent(NamespaceID.from(namespaceID), range);
    }

    /**
     * Create a custom sound event. The {@link NamespaceID} should match a sound provided in the resource pack.
     * @param namespaceID the namespace ID of the custom sound event
     * @param range the range of the sound event, or null for (legacy) dynamic range
     * @return the custom sound event
     */
    static @NotNull SoundEvent of(@NotNull NamespaceID namespaceID, @Nullable Float range) {
        return new CustomSoundEvent(namespaceID, range);
    }

    @Override
    default @NotNull Key key() {
        return ProtocolObject.super.key();
    }
}
