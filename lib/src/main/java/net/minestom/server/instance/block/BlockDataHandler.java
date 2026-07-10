package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;

import java.util.Collection;
import java.util.List;

/**
 * Lib-safe identity surface of a block handler. Stored by {@link Block} and set with
 * {@link Block#withHandler(BlockDataHandler)}.
 * <p>
 * Carries only data-oriented members (block-entity identity, tags and serialization). Behavioral
 * callbacks that require a running instance live on {@link BlockHandler}.
 * <p>
 * Implementations are expected to be thread safe.
 */
public interface BlockDataHandler {

    default boolean isTickable() {
        return false;
    }

    /**
     * Specifies which block entity tags should be sent to the player.
     *
     * @return The list of tags from this block's block entity that should be sent to the player
     * @see <a href="https://minecraft.wiki/w/Block_entity">Block entity on the Minecraft wiki</a>
     */
    default Collection<Tag<?>> getBlockEntityTags() {
        return List.of();
    }

    default byte getBlockEntityAction() {
        return -1;
    }

    /**
     * Gets the id of this handler.
     * <p>
     * Used to write the block entity in the anvil world format.
     *
     * @return the key of this handler
     */
    Key getKey();
}
