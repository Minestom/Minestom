package net.minestom.server.item.metadata;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.minestom.server.item.Enchantment;
import net.minestom.server.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Collections;
import java.util.Map;

public class EnchantedBookMeta extends ItemMeta {

    private final Object2ShortMap<Enchantment> storedEnchantmentMap = new Object2ShortOpenHashMap<>();

    /**
     * Gets the stored enchantment map.
     * Stored enchantments are used on enchanted book.
     *
     * @return an unmodifiable map containing the item stored enchantments
     */
    @NotNull
    public Map<Enchantment, Short> getStoredEnchantmentMap() {
        return Collections.unmodifiableMap(storedEnchantmentMap);
    }

    /**
     * Sets a stored enchantment level.
     *
     * @param enchantment the enchantment type
     * @param level       the enchantment level
     */
    public void setStoredEnchantment(@NotNull Enchantment enchantment, short level) {
        if (level < 1) {
            removeStoredEnchantment(enchantment);
            return;
        }

        this.storedEnchantmentMap.put(enchantment, level);
    }

    /**
     * Removes a stored enchantment.
     *
     * @param enchantment the enchantment type
     */
    public void removeStoredEnchantment(@NotNull Enchantment enchantment) {
        this.storedEnchantmentMap.removeShort(enchantment);
    }

    /**
     * Gets a stored enchantment level.
     *
     * @param enchantment the enchantment type
     * @return the stored enchantment level, 0 if not present
     */
    public int getStoredEnchantmentLevel(@NotNull Enchantment enchantment) {
        return this.storedEnchantmentMap.getOrDefault(enchantment, (short) 0);
    }

    @Override
    public boolean hasNbt() {
        return !storedEnchantmentMap.isEmpty();
    }

    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        return itemMeta instanceof EnchantedBookMeta &&
                ((EnchantedBookMeta) itemMeta).storedEnchantmentMap.equals(storedEnchantmentMap);
    }

    @Override
    public void read(@NotNull NBTCompound compound) {
        if (compound.containsKey("StoredEnchantments")) {
            NBTUtils.loadEnchantments(compound.getList("StoredEnchantments"), this::setStoredEnchantment);
        }
    }

    @Override
    public void write(@NotNull NBTCompound compound) {
        if (!storedEnchantmentMap.isEmpty()) {
            NBTUtils.writeEnchant(compound, "StoredEnchantments", storedEnchantmentMap);
        }
    }

    @NotNull
    @Override
    public ItemMeta clone() {
        EnchantedBookMeta enchantedBookMeta = (EnchantedBookMeta) super.clone();
        enchantedBookMeta.storedEnchantmentMap.putAll(storedEnchantmentMap);

        return enchantedBookMeta;
    }
}
