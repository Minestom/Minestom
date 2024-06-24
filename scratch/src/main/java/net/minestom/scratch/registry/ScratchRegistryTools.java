package net.minestom.scratch.registry;

import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.message.ChatType;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ScratchRegistryTools {
    public static final DynamicRegistry<ChatType> CHAT_REGISTRY = ChatType.createDefaultRegistry();
    public static final DynamicRegistry<DimensionType> DIMENSION_REGISTRY = DimensionType.createDefaultRegistry();
    public static final DynamicRegistry<Biome> BIOME_REGISTRY = Biome.createDefaultRegistry();
    public static final DynamicRegistry<DamageType> DAMANGE_TYPE_REGISTRY = DamageType.createDefaultRegistry();
    public static final DynamicRegistry<TrimMaterial> TRIM_MATERIAL_REGISTRY = TrimMaterial.createDefaultRegistry();
    public static final DynamicRegistry<TrimPattern> TRIM_PATTERN_REGISTRY = TrimPattern.createDefaultRegistry();
    public static final DynamicRegistry<BannerPattern> BANNER_REGISTRY = BannerPattern.createDefaultRegistry();
    public static final DynamicRegistry<WolfMeta.Variant> WOLF_REGISTRY = WolfMeta.Variant.createDefaultRegistry();
    //public static final DynamicRegistry<ChatType> ENCHANTMENT_REGISTRY = Enchantment.createDefaultRegistry(this);
    public static final DynamicRegistry<PaintingMeta.Variant> PAINTING_REGISTRY = PaintingMeta.Variant.createDefaultRegistry();
    public static final DynamicRegistry<JukeboxSong> JUKEBOX_REGISTRY = JukeboxSong.createDefaultRegistry();

    public static final Set<DynamicRegistry<?>> REGISTRIES = Set.of(
            CHAT_REGISTRY,
            DIMENSION_REGISTRY,
            BIOME_REGISTRY,
            DAMANGE_TYPE_REGISTRY,
            TRIM_MATERIAL_REGISTRY,
            TRIM_PATTERN_REGISTRY,
            BANNER_REGISTRY,
            WOLF_REGISTRY,
            //ENCHANTMENT_REGISTRY,
            PAINTING_REGISTRY,
            JUKEBOX_REGISTRY
    );

    public static final List<ServerPacket> REGISTRY_PACKETS;

    static {
        List<ServerPacket> packets = new ArrayList<>();
        for (DynamicRegistry<?> registry : REGISTRIES) {
            final SendablePacket sendablePacket = registry.registryDataPacket(false);
            final ServerPacket packet = SendablePacket.extractServerPacket(ConnectionState.CONFIGURATION, sendablePacket);
            packets.add(packet);
        }
        REGISTRY_PACKETS = List.copyOf(packets);
    }
}
