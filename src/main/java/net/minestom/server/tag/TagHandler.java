package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;

/**
 * Represents an element which can read and write {@link Tag tags}.
 */
@ApiStatus.Experimental
public interface TagHandler extends TagReadable, TagWritable {

    @NotNull TagReadable readableCopy();

    void updateContent(@NotNull NBTCompoundLike compound);

    @NotNull NBTCompound asCompound();

    @ApiStatus.Experimental
    static @NotNull TagHandler newHandler() {
        return new TagHandlerImpl();
    }

    /**
     * Converts a nbt compound to a tag handler.
     * <p>
     * The returned tag handler is not thread-safe.
     *
     * @param compound the compound to convert
     * @return a {@link TagHandler} capable of writing and reading {@code compound}
     */
    static @NotNull TagHandler fromCompound(@NotNull NBTCompoundLike compound) {
        TagHandler handler = newHandler();
        handler.updateContent(compound);
        return handler;
    }
}
