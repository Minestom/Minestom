package net.minestom.server.registry;

import net.minestom.server.codec.StructCodec;
import net.minestom.server.dialog.Dialog;
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
    public DynamicRegistry<WolfVariant> wolfVariant = null;
    public DynamicRegistry<Enchantment> enchantment = null;
    public DynamicRegistry<PaintingVariant> paintingVariant = null;
    public DynamicRegistry<JukeboxSong> jukeboxSong = null;
    public DynamicRegistry<Instrument> instrument = null;
    public DynamicRegistry<WolfSoundVariant> wolfSoundVariant = null;
    public DynamicRegistry<CatVariant> catVariant = null;
    public DynamicRegistry<ChickenVariant> chickenVariant = null;
    public DynamicRegistry<CowVariant> cowVariant = null;
    public DynamicRegistry<FrogVariant> frogVariant = null;
    public DynamicRegistry<PigVariant> pigVariant = null;
    public DynamicRegistry<Dialog> dialog = null;
    public DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues = null;
    public DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects = null;
    public DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects = null;
    public DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects = null;

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
    public @NotNull DynamicRegistry<WolfVariant> wolfVariant() {
        return Objects.requireNonNull(wolfVariant);
    }

    @Override
    public @NotNull DynamicRegistry<WolfSoundVariant> wolfSoundVariant() {
        return Objects.requireNonNull(wolfSoundVariant);
    }

    @Override
    public @NotNull DynamicRegistry<CatVariant> catVariant() {
        return Objects.requireNonNull(catVariant);
    }

    @Override
    public @NotNull DynamicRegistry<ChickenVariant> chickenVariant() {
        return Objects.requireNonNull(chickenVariant);
    }

    @Override
    public @NotNull DynamicRegistry<CowVariant> cowVariant() {
        return Objects.requireNonNull(cowVariant);
    }

    @Override
    public @NotNull DynamicRegistry<FrogVariant> frogVariant() {
        return Objects.requireNonNull(frogVariant);
    }

    @Override
    public @NotNull DynamicRegistry<PigVariant> pigVariant() {
        return Objects.requireNonNull(pigVariant);
    }

    @Override
    public @NotNull DynamicRegistry<Enchantment> enchantment() {
        return Objects.requireNonNull(enchantment);
    }

    @Override
    public @NotNull DynamicRegistry<PaintingVariant> paintingVariant() {
        return Objects.requireNonNull(paintingVariant);
    }

    @Override
    public @NotNull DynamicRegistry<JukeboxSong> jukeboxSong() {
        return Objects.requireNonNull(jukeboxSong);
    }

    @Override
    public @NotNull DynamicRegistry<Instrument> instrument() {
        return instrument;
    }

    @Override
    public @NotNull DynamicRegistry<Dialog> dialog() {
        return dialog;
    }

    @Override
    public @NotNull DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues() {
        return Objects.requireNonNull(enchantmentLevelBasedValues);
    }

    @Override
    public @NotNull DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects() {
        return Objects.requireNonNull(enchantmentValueEffects);
    }

    @Override
    public @NotNull DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects() {
        return Objects.requireNonNull(enchantmentEntityEffects);
    }

    @Override
    public @NotNull DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects() {
        return Objects.requireNonNull(enchantmentLocationEffects);
    }
}
