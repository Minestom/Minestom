package net.minestom.server.raw_data;

import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class RawMaterialData {
    public boolean damageable;
    public int maxDurability;
    public boolean edible;
    public boolean fireResistant;
    public Block block;
    public SoundEvent eatingSound;
    public SoundEvent drinkingSound;
    @Nullable
    public RawArmorData armorData;

    public static class RawArmorData {
        public int defense;
        public double toughness;
        public EntityEquipmentPacket.Slot slot; // TODO: Maybe better class
    }
}