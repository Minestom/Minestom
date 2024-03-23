package net.minestom.scratch.tools;

import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.armor.TrimManager;
import net.minestom.server.message.Messenger;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
import net.minestom.server.world.DimensionTypeManager;
import net.minestom.server.world.biomes.BiomeManager;
import org.jglrxavpok.hephaistos.nbt.NBT;

import java.util.HashMap;

public final class ScratchTools {
    public static RegistryDataPacket REGISTRY_DATA_PACKET;

    static {
        DimensionTypeManager dimensionTypeManager = new DimensionTypeManager();
        BiomeManager biomeManager = new BiomeManager();
        TrimManager trimManager = new TrimManager();
        var registry = new HashMap<String, NBT>();
        registry.put("minecraft:chat_type", Messenger.chatRegistry());
        registry.put("minecraft:dimension_type", dimensionTypeManager.toNBT());
        registry.put("minecraft:worldgen/biome", biomeManager.toNBT());
        registry.put("minecraft:damage_type", DamageType.getNBT());
        registry.put("minecraft:trim_material", trimManager.getTrimMaterialNBT());
        registry.put("minecraft:trim_pattern", trimManager.getTrimPatternNBT());
        REGISTRY_DATA_PACKET = new RegistryDataPacket(NBT.Compound(registry));
    }
}
