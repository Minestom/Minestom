package net.minestom.server.tag;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an element which can read and write {@link Tag tags}.
 */
public interface TagHandler extends TagReadable, TagWritable {

    /**
     * Creates a readable copy of this handler.
     * <p>
     * Similar to {@link #asCompound()} with the advantage that cached objects
     * and adaptive optimizations may be reused.
     *
     * @return a copy of this handler
     */
    @NotNull TagReadable readableCopy();

    /**
     * Creates a copy of this handler.
     * <p>
     * Similar to {@link #fromCompound(CompoundBinaryTag)} using {@link #asCompound()}
     * with the advantage that cached objects and adaptive optimizations may be reused.
     *
     * @return a copy of this handler
     */
    @NotNull TagHandler copy();

    /**
     * Updates the content of this handler.
     * <p>
     * Can be used as a clearing method with {@link CompoundBinaryTag#empty()}.
     *
     * @param compound the new content of this handler
     */
    void updateContent(@NotNull CompoundBinaryTag compound);

    /**
     * Converts the content of this handler into a {@link CompoundBinaryTag}.
     *
     * @return a nbt compound representation of this handler
     */
    @NotNull CompoundBinaryTag asCompound();

    static @NotNull TagHandler newHandler() {
        return new TagHandlerImpl();
    }

    /**
     * Copy the content of the given {@link CompoundBinaryTag} into a new {@link TagHandler}.
     *
     * @param compound the compound to read tags from
     * @return a new tag handler with the content of the given compound
     */
    static @NotNull TagHandler fromCompound(@NotNull CompoundBinaryTag compound) {
        return TagHandlerImpl.fromCompound(compound);
    }
}
