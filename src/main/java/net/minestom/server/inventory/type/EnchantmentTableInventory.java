package net.minestom.server.inventory.type;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryProperty;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class EnchantmentTableInventory extends Inventory {
    private final Registry<Enchantment> enchantmentRegistry;

    private final short[] levelRequirements = new short[EnchantmentSlot.values().length];
    private short seed;
    private final Map<EnchantmentSlot, RegistryKey<Enchantment>> enchantmentShown = new EnumMap<>(EnchantmentSlot.class);
    private final short[] enchantmentLevel = new short[EnchantmentSlot.values().length];

    /**
     * Creates an enchantment table inventory that sends displayed enchantments by their ID in
     * {@code enchantmentRegistry}.
     *
     * @param title               the inventory title
     * @param enchantmentRegistry the registry that assigns the IDs of displayed enchantments
     */
    public EnchantmentTableInventory(Component title, Registry<Enchantment> enchantmentRegistry) {
        this.enchantmentRegistry = Objects.requireNonNull(enchantmentRegistry, "enchantmentRegistry");
        super(InventoryType.ENCHANTMENT, title);
    }

    public EnchantmentTableInventory(Component title) {
        this(title, MinecraftServer.getEnchantmentRegistry());
    }

    public EnchantmentTableInventory(String title) {
        this(Component.text(title));
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
    public @Nullable RegistryKey<Enchantment> getEnchantmentShown(EnchantmentSlot enchantmentSlot) {
        return enchantmentShown.get(enchantmentSlot);
    }

    /**
     * Sets the enchantment shown in a slot.
     *
     * @param enchantmentSlot the enchantment slot
     * @param enchantment     the enchantment, or {@code null} to hide it
     * @throws IllegalArgumentException if the enchantment is not registered in this inventory's enchantment registry
     *                                  or its ID is too large for an inventory property
     */
    public void setEnchantmentShown(EnchantmentSlot enchantmentSlot, @Nullable RegistryKey<Enchantment> enchantment) {
        final int registryId = enchantment == null ? -1 : enchantmentRegistry.getId(enchantment);
        Check.argCondition(enchantment != null && registryId == -1,
                "Unknown enchantment {0} for registry {1}", enchantment, enchantmentRegistry.key());
        Check.argCondition(registryId > Short.MAX_VALUE,
                "Enchantment id {0} cannot be represented by an inventory property", registryId);
        final short id = (short) registryId;
        switch (enchantmentSlot) {
            case TOP -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_ENCH_ID_TOP, id);
            case MIDDLE -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_ENCH_ID_MIDDLE, id);
            case BOTTOM -> sendProperty(InventoryProperty.ENCHANTMENT_TABLE_ENCH_ID_BOTTOM, id);
        }
        if (enchantment == null) this.enchantmentShown.remove(enchantmentSlot);
        else this.enchantmentShown.put(enchantmentSlot, enchantment);
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
