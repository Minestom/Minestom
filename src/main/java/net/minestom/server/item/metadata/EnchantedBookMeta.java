package net.minestom.server.item.metadata;

import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.HashMap;
import java.util.Map;

public class EnchantedBookMeta extends ItemMeta implements ItemMetaBuilder.Provider<EnchantedBookMeta.Builder> {

    private final Map<Enchantment, Short> storedEnchantmentMap;

    protected EnchantedBookMeta(@NotNull ItemMetaBuilder metaBuilder, Map<Enchantment, Short> storedEnchantmentMap) {
        super(metaBuilder);
        this.storedEnchantmentMap = Map.copyOf(storedEnchantmentMap);
    }

    /**
     * Gets the stored enchantment map.
     * Stored enchantments are used on enchanted book.
     *
     * @return an unmodifiable map containing the item stored enchantments
     */
    public @NotNull Map<Enchantment, Short> getStoredEnchantmentMap() {
        return storedEnchantmentMap;
    }

    public static class Builder extends ItemMetaBuilder {

        private Map<Enchantment, Short> enchantments = new HashMap<>();

        public @NotNull Builder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
            this.enchantments = enchantments;
            NBTUtils.writeEnchant(mutableNbt(), "StoredEnchantments", enchantments);
            return this;
        }

        public @NotNull Builder enchantment(@NotNull Enchantment enchantment, short level) {
            this.enchantments.put(enchantment, level);
            enchantments(enchantments);
            return this;
        }

        @Override
        public @NotNull EnchantedBookMeta build() {
            return new EnchantedBookMeta(this, enchantments);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.get("StoredEnchantments") instanceof NBTList<?> list &&
                    list.getSubtagType() == NBTType.TAG_Compound) {
                NBTUtils.loadEnchantments(list.asListOf(), this::enchantment);
            }
        }
    }
}
