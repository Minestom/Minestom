package net.minestom.server.item.metadata;

import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EnchantedBookMeta extends ItemMeta implements ItemMetaBuilder.Provider<EnchantedBookMeta.Builder> {

    private final Map<Enchantment, Short> storedEnchantmentMap;

    protected EnchantedBookMeta(@NotNull ItemMetaBuilder metaBuilder, Map<Enchantment, Short> storedEnchantmentMap) {
        super(metaBuilder);
        this.storedEnchantmentMap = new HashMap<>(storedEnchantmentMap);
    }

    /**
     * Gets the stored enchantment map.
     * Stored enchantments are used on enchanted book.
     *
     * @return an unmodifiable map containing the item stored enchantments
     */
    public @NotNull Map<Enchantment, Short> getStoredEnchantmentMap() {
        return Collections.unmodifiableMap(storedEnchantmentMap);
    }

    public static class Builder extends ItemMetaBuilder {

        private Map<Enchantment, Short> enchantments = new HashMap<>();

        public @NotNull Builder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
            this.enchantments = enchantments;
            mutateNbt(compound -> NBTUtils.writeEnchant(compound, "StoredEnchantments", enchantments));
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
            if (nbtCompound.containsKey("StoredEnchantments")) {
                NBTUtils.loadEnchantments(nbtCompound.getList("StoredEnchantments"), this::enchantment);
            }
        }

        @Override
        protected @NotNull Supplier<ItemMetaBuilder> getSupplier() {
            return Builder::new;
        }
    }
}
