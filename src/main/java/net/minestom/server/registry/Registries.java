package net.minestom.server.registry;

import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.item.enchant.*;
import net.minestom.server.message.ChatType;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Provides access to all the dynamic registries. {@link net.minestom.server.ServerProcess} is the most relevant
 * implementation of this interface.</p>
 *
 * @see net.minestom.server.MinecraftServer for static access to these
 */
public interface Registries {

    @NotNull DynamicRegistry<ChatType> chatType();

    @NotNull DynamicRegistry<DimensionType> dimensionType();

    @NotNull DynamicRegistry<Biome> biome();

    @NotNull DynamicRegistry<DamageType> damageType();

    @NotNull DynamicRegistry<TrimMaterial> trimMaterial();

    @NotNull DynamicRegistry<TrimPattern> trimPattern();

    @NotNull DynamicRegistry<BannerPattern> bannerPattern();

    @NotNull DynamicRegistry<WolfMeta.Variant> wolfVariant();

    @NotNull DynamicRegistry<Enchantment> enchantment();

    @NotNull DynamicRegistry<PaintingMeta.Variant> paintingVariant();

    @NotNull DynamicRegistry<JukeboxSong> jukeboxSong();

    // The following are _not_ sent to the client.

    @NotNull DynamicRegistry<BinaryTagSerializer<? extends LevelBasedValue>> enchantmentLevelBasedValues();

    @NotNull DynamicRegistry<BinaryTagSerializer<? extends ValueEffect>> enchantmentValueEffects();

    @NotNull DynamicRegistry<BinaryTagSerializer<? extends EntityEffect>> enchantmentEntityEffects();

    @NotNull DynamicRegistry<BinaryTagSerializer<? extends LocationEffect>> enchantmentLocationEffects();

}
