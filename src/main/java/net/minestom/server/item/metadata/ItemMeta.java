package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Represents nbt data only available for a type of item.
 */
public interface ItemMeta {

    /**
     * Gets if this meta object contains any useful data to send to the client.
     *
     * @return true if this item has nbt data, false otherwise
     */
    boolean hasNbt();

    /**
     * Gets if the two ItemMeta are similar.
     * <p>
     * It is used by {@link ItemStack#isSimilar(ItemStack)}.
     *
     * @param itemMeta the second item meta to check
     * @return true if the two meta are similar, false otherwise
     */
    boolean isSimilar(@NotNull ItemMeta itemMeta);

    /**
     * Reads nbt data from a compound.
     * <p>
     * WARNING: it is possible that it contains no useful data,
     * it has to be checked before getting anything.
     *
     * @param compound the compound containing the data
     */
    void read(@NotNull NBTCompound compound);

    /**
     * Writes nbt data to a compound.
     *
     * @param compound the compound receiving the item meta data
     */
    void write(@NotNull NBTCompound compound);

    /**
     * Clones this item meta.
     * <p>
     * Used by {@link ItemStack#clone()}.
     *
     * @return the cloned item meta
     */
    @NotNull
    ItemMeta clone();

}
