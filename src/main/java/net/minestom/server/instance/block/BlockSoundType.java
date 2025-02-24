package net.minestom.server.instance.block;

import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a BlockSoundType object, a set of sounds associated with a particular block (or group of blocks).
 * <p>
 * Note: Although this extends StaticProtocolObject, it's not actually Registry sent through the protocol, and purely for data organization.
 */
public sealed interface BlockSoundType extends StaticProtocolObject, BlockSoundTypes permits BlockSoundImpl {

    /**
     * Returns the 'registry' data for the block sound type. Note: Block sound types are not an actual minecraft registry
     */
    @Contract(pure = true)
    @NotNull
    Registry.BlockSoundTypeEntry registry();

    @Override
    default @NotNull NamespaceID namespace() {
        return registry().namespaceID();
    }

    static @Nullable BlockSoundType fromNamespaceId(@NotNull String namespaceID) {
        return BlockSoundImpl.getSafe(namespaceID);
    }

    static @Nullable BlockSoundType fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    default float volume() {
        return registry().volume();
    }

    default float pitch() {
        return registry().pitch();
    }

    default @NotNull SoundEvent breakSound() {
        return registry().breakSound();
    }

    default @NotNull SoundEvent hitSound() {
        return registry().hitSound();
    }

    default @NotNull SoundEvent fallSound() {
        return registry().fallSound();
    }

    default @NotNull SoundEvent placeSound() {
        return registry().placeSound();
    }

    default @NotNull SoundEvent stepSound() {
        return registry().stepSound();
    }

    default int id() {
        return 0; // Not sent through packets in the protocol, also must be between 0 and [size of block sound type list] because id mappings are stored in an array
    }
}
