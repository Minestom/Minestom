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

import java.util.Objects;
import java.util.function.Consumer;

public class TestRegistries implements Registries {
    public DynamicRegistry<ChatType> chatType = null;
    public DynamicRegistry<DimensionType> dimensionType = null;
    public DynamicRegistry<Biome> biome = null;
    public DynamicRegistry<DamageType> damageType = null;
    public DynamicRegistry<TrimMaterial> trimMaterial = null;
    public DynamicRegistry<TrimPattern> trimPattern = null;
    public DynamicRegistry<BannerPattern> bannerPattern = null;
    public DynamicRegistry<WolfMeta.Variant> wolfVariant = null;
    public DynamicRegistry<Enchantment> enchantment = null;
    public DynamicRegistry<PaintingMeta.Variant> paintingVariant = null;
    public DynamicRegistry<JukeboxSong> jukeboxSong = null;
    public DynamicRegistry<BinaryTagSerializer<? extends LevelBasedValue>> enchantmentLevelBasedValues = null;
    public DynamicRegistry<BinaryTagSerializer<? extends ValueEffect>> enchantmentValueEffects = null;
    public DynamicRegistry<BinaryTagSerializer<? extends EntityEffect>> enchantmentEntityEffects = null;
    public DynamicRegistry<BinaryTagSerializer<? extends LocationEffect>> enchantmentLocationEffects = null;

    public TestRegistries() {

    }

    public TestRegistries(Consumer<TestRegistries> init) {
        init.accept(this);
    }

    @Override
    public @NotNull DynamicRegistry<ChatType> chatType() {
        return Objects.requireNonNull(chatType);
    }

    @Override
    public @NotNull DynamicRegistry<DimensionType> dimensionType() {
        return Objects.requireNonNull(dimensionType);
    }

    @Override
    public @NotNull DynamicRegistry<Biome> biome() {
        return Objects.requireNonNull(biome);
    }

    @Override
    public @NotNull DynamicRegistry<DamageType> damageType() {
        return Objects.requireNonNull(damageType);
    }

    @Override
    public @NotNull DynamicRegistry<TrimMaterial> trimMaterial() {
        return Objects.requireNonNull(trimMaterial);
    }

    @Override
    public @NotNull DynamicRegistry<TrimPattern> trimPattern() {
        return Objects.requireNonNull(trimPattern);
    }

    @Override
    public @NotNull DynamicRegistry<BannerPattern> bannerPattern() {
        return Objects.requireNonNull(bannerPattern);
    }

    @Override
    public @NotNull DynamicRegistry<WolfMeta.Variant> wolfVariant() {
        return Objects.requireNonNull(wolfVariant);
    }

    @Override
    public @NotNull DynamicRegistry<Enchantment> enchantment() {
        return Objects.requireNonNull(enchantment);
    }

    @Override
    public @NotNull DynamicRegistry<PaintingMeta.Variant> paintingVariant() {
        return Objects.requireNonNull(paintingVariant);
    }

    @Override
    public @NotNull DynamicRegistry<JukeboxSong> jukeboxSong() {
        return Objects.requireNonNull(jukeboxSong);
    }

    @Override
    public @NotNull DynamicRegistry<BinaryTagSerializer<? extends LevelBasedValue>> enchantmentLevelBasedValues() {
        return Objects.requireNonNull(enchantmentLevelBasedValues);
    }

    @Override
    public @NotNull DynamicRegistry<BinaryTagSerializer<? extends ValueEffect>> enchantmentValueEffects() {
        return Objects.requireNonNull(enchantmentValueEffects);
    }

    @Override
    public @NotNull DynamicRegistry<BinaryTagSerializer<? extends EntityEffect>> enchantmentEntityEffects() {
        return Objects.requireNonNull(enchantmentEntityEffects);
    }

    @Override
    public @NotNull DynamicRegistry<BinaryTagSerializer<? extends LocationEffect>> enchantmentLocationEffects() {
        return Objects.requireNonNull(enchantmentLocationEffects);
    }
}
