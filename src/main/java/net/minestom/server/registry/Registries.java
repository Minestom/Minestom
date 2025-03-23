package net.minestom.server.registry;

import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.ChickenVariant;
import net.minestom.server.entity.metadata.animal.CowVariant;
import net.minestom.server.entity.metadata.animal.FrogVariant;
import net.minestom.server.entity.metadata.animal.PigVariant;
import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfSoundVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfVariant;
import net.minestom.server.entity.metadata.other.PaintingVariant;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.item.enchant.*;
import net.minestom.server.item.instrument.Instrument;
import net.minestom.server.message.ChatType;
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

    @NotNull DynamicRegistry<Enchantment> enchantment();

    @NotNull DynamicRegistry<PaintingVariant> paintingVariant();

    @NotNull DynamicRegistry<JukeboxSong> jukeboxSong();

    @NotNull DynamicRegistry<Instrument> instrument();

    @NotNull DynamicRegistry<WolfVariant> wolfVariant();

    @NotNull DynamicRegistry<WolfSoundVariant> wolfSoundVariant();

    @NotNull DynamicRegistry<CatVariant> catVariant();

    @NotNull DynamicRegistry<ChickenVariant> chickenVariant();

    @NotNull DynamicRegistry<CowVariant> cowVariant();

    @NotNull DynamicRegistry<FrogVariant> frogVariant();

    @NotNull DynamicRegistry<PigVariant> pigVariant();

    // The following are _not_ sent to the client.

    @NotNull DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues();

    @NotNull DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects();

    @NotNull DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects();

    @NotNull DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects();

    @FunctionalInterface
    interface Selector<T> {
        @NotNull DynamicRegistry<T> select(@NotNull Registries registries);
    }

}
