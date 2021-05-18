package net.minestom.server.raw_data;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.block.Block;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class RawMaterialData {
    private final boolean damageable;
    private final int maxDurability;
    private final boolean edible;
    private final boolean fireResistant;
    @NotNull
    private final Supplier<@NotNull Block> block;
    @NotNull
    private final Supplier<@NotNull SoundEvent> eatingSound;
    @NotNull
    private final Supplier<@NotNull SoundEvent> drinkingSound;
    @Nullable
    private final RawArmorData armorData;

    public RawMaterialData(
            boolean damageable,
            int maxDurability,
            boolean edible,
            boolean fireResistant,
            @NotNull Supplier<@NotNull Block> block,
            @NotNull Supplier<@NotNull SoundEvent> eatingSound,
            @NotNull Supplier<@NotNull SoundEvent> drinkingSound,
            @Nullable RawArmorData armorData
    ) {
        this.damageable = damageable;
        this.maxDurability = maxDurability;
        this.edible = edible;
        this.fireResistant = fireResistant;
        this.block = block;
        this.eatingSound = eatingSound;
        this.drinkingSound = drinkingSound;
        this.armorData = armorData;
    }

    public boolean isDamageable() {
        return damageable;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public boolean isEdible() {
        return edible;
    }

    public boolean isFireResistant() {
        return fireResistant;
    }

    @NotNull
    public Block getBlock() {
        return block.get();
    }

    @NotNull
    public SoundEvent getEatingSound() {
        return eatingSound.get();
    }

    @NotNull
    public SoundEvent getDrinkingSound() {
        return drinkingSound.get();
    }

    @Nullable
    public RawArmorData getArmorData() {
        return armorData;
    }

    public static class RawArmorData {
        public final int defense;
        public final double toughness;
        public final EquipmentSlot slot;

        public RawArmorData(int defense, double toughness, EquipmentSlot slot) {
            this.defense = defense;
            this.toughness = toughness;
            this.slot = slot;
        }

        public int getDefense() {
            return defense;
        }

        public double getToughness() {
            return toughness;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }
    }
}