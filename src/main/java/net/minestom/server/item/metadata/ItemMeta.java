package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemStack;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Represent nbt data only available for a type of item
 */
public interface ItemMeta {

    /**
     * Get if this meta object contains any useful data to send to the client
     *
     * @return true if this item has nbt data, false otherwise
     */
    boolean hasNbt();

    /**
     * Get if the two ItemMeta are similar
     * <p>
     * It is used by {@link ItemStack#isSimilar(ItemStack)}
     *
     * @param itemMeta the second item meta to check
     * @return true if the two meta are similar, false otherwise
     */
    boolean isSimilar(ItemMeta itemMeta);

    /**
     * Read nbt data from a compound
     * <p>
     * WARNING: it is possible that it contains no useful data,
     * it has to be checked before getting anything
     *
     * @param compound the compound containing the data
     */
    void read(NBTCompound compound);

    /**
     * Write nbt data to a compound
     *
     * @param compound the compound receiving the item meta data
     */
    void write(NBTCompound compound);

    /**
     * Clone this item meta
     * <p>
     * Used by {@link ItemStack#clone()}
     *
     * @return the cloned item meta
     */
    ItemMeta clone();

}
