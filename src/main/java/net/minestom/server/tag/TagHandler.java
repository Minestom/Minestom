package net.minestom.server.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTCompoundLike;

/**
 * Represents an element which can read and write {@link Tag tags}.
 */
public interface TagHandler extends TagReadable, TagWritable {

    @NotNull TagReadable readableCopy();

    void updateContent(@NotNull NBTCompoundLike compound);

    @NotNull NBTCompound asCompound();

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
