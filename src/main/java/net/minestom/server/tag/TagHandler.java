package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;

import java.util.function.UnaryOperator;

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
     * Similar to {@link #fromCompound(NBTCompoundLike)} using {@link #asCompound()}
     * with the advantage that cached objects and adaptive optimizations may be reused.
     *
     * @return a copy of this handler
     */
    @NotNull TagHandler copy();

    /**
     * Updates the content of this handler.
     * <p>
     * Can be used as a clearing method with {@link NBTCompound#EMPTY}.
     *
     * @param compound the new content of this handler
     */
    void updateContent(@NotNull NBTCompoundLike compound);

    /**
     * Converts the content of this handler into a {@link NBTCompound}.
     *
     * @return a nbt compound representation of this handler
     */
    @NotNull NBTCompound asCompound();

    @ApiStatus.Experimental
    <T> void updateTag(@NotNull Tag<T> tag,
                       @NotNull UnaryOperator<@UnknownNullability T> value);

    @ApiStatus.Experimental
    <T> @UnknownNullability T updateAndGetTag(@NotNull Tag<T> tag,
                                              @NotNull UnaryOperator<@UnknownNullability T> value);

    @ApiStatus.Experimental
    <T> @UnknownNullability T getAndUpdateTag(@NotNull Tag<T> tag,
                                              @NotNull UnaryOperator<@UnknownNullability T> value);

    @ApiStatus.Experimental
    static @NotNull TagHandler newHandler() {
        return new TagHandlerImpl();
    }

    /**
     * Copy the content of the given {@link NBTCompoundLike} into a new {@link TagHandler}.
     *
     * @param compound the compound to read tags from
     * @return a new tag handler with the content of the given compound
     */
    static @NotNull TagHandler fromCompound(@NotNull NBTCompoundLike compound) {
        return TagHandlerImpl.fromCompound(compound);
    }
}
