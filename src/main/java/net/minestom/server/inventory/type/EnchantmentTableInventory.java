package net.minestom.server.inventory.type;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

public class EnchantmentTableInventory extends Inventory {
    private static final DynamicRegistry<Enchantment> ENCHANTMENT_REGISTRY = MinecraftServer.getEnchantmentRegistry();

    private final short[] levelRequirements = new short[EnchantmentSlot.values().length];
    private short seed;
    private final short[] enchantmentShown = new short[EnchantmentSlot.values().length];
    private final short[] enchantmentLevel = new short[EnchantmentSlot.values().length];

    public EnchantmentTableInventory(Component title) {
        super(InventoryType.ENCHANTMENT, title);
    }

    public EnchantmentTableInventory(String title) {
        super(InventoryType.ENCHANTMENT, title);
    }

    /**
     * Gets the level requirement in a slot.
     *
     * @param enchantmentSlot the slot to check the level requirement
     * @return the level requirement of the slot
     */
    public short getLevelRequirement(EnchantmentSlot enchantmentSlot) {
        return levelRequirements[enchantmentSlot.ordinal()];
    }

    /**
     * Sets the level requirement of a slot.
     *
     * @param enchantmentSlot the slot
     * @param level           the level
     */
    public void setLevelRequirement(EnchantmentSlot enchantmentSlot, short level) {
        switch (enchantmentSlot) {
            case TOP -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_LEVEL_REQUIREMENT_TOP, level);
            case MIDDLE -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_LEVEL_REQUIREMENT_MIDDLE, level);
            case BOTTOM -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_LEVEL_REQUIREMENT_BOTTOM, level);
        }
        this.levelRequirements[enchantmentSlot.ordinal()] = level;
    }

    /**
     * Gets the enchantment seed.
     *
     * @return the enchantment seed
     */
    public short getSeed() {
        return seed;
    }

    /**
     * Sets the enchantment seed.
     *
     * @param seed the enchantment seed
     */
    public void setSeed(short seed) {
        this.seed = seed;
        sendProperty(InventoryProperty.ENCHANTMENT_TABLE_SEED, seed);
    }

    /**
     * Gets the enchantment shown in a slot.
     *
     * @param enchantmentSlot the enchantment slot
     * @return the enchantment shown in the slot, null if it is hidden
     */
    public RegistryKey<Enchantment> getEnchantmentShown(EnchantmentSlot enchantmentSlot) {
        final int id = enchantmentShown[enchantmentSlot.ordinal()];
        if (id == -1) return null;
        return ENCHANTMENT_REGISTRY.getKey(id);
    }

    /**
     * Sets the enchantment shown in a slot.
     * <p>
     * Can be set to null to hide it.
     *
     * @param enchantmentSlot the enchantment slot
     * @param enchantment     the enchantment
     */
    public void setEnchantmentShown(EnchantmentSlot enchantmentSlot, @Nullable RegistryKey<Enchantment> enchantment) {
        final short id = enchantment == null ? -1 : (short) ENCHANTMENT_REGISTRY.getId(enchantment);
        switch (enchantmentSlot) {
            case TOP -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_ENCH_ID_TOP, id);
            case MIDDLE -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_ENCH_ID_MIDDLE, id);
            case BOTTOM -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_ENCH_ID_BOTTOM, id);
        }
        this.enchantmentShown[enchantmentSlot.ordinal()] = id;
    }

    /**
     * Gets the enchantment level shown on mouse hover.
     *
     * @param enchantmentSlot the enchantment slot
     * @return the level shown, -1 if no enchant
     */
    public short getEnchantmentLevel(EnchantmentSlot enchantmentSlot) {
        return enchantmentLevel[enchantmentSlot.ordinal()];
    }

    /**
     * Sets the enchantment level shown on mouse hover.
     * <p>
     * Can be set to -1 if no enchant.
     *
     * @param enchantmentSlot the enchantment slot
     * @param level           the level shown
     */
    public void setEnchantmentLevel(EnchantmentSlot enchantmentSlot, short level) {
        switch (enchantmentSlot) {
            case TOP -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_ENCH_LEVEL_TOP, level);
            case MIDDLE -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_ENCH_LEVEL_MIDDLE, level);
            case BOTTOM -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_ENCH_LEVEL_BOTTOM, level);
        }
        this.enchantmentLevel[enchantmentSlot.ordinal()] = level;
    }

    public enum EnchantmentSlot {
        TOP, MIDDLE, BOTTOM
    }

}
