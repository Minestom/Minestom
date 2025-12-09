package net.minestom.server.registry;

import net.minestom.server.codec.StructCodec;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.*;
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
import net.minestom.server.world.timeline.Timeline;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class TestRegistries implements Registries {
    public @Nullable DynamicRegistry<ChatType> chatType = null;
    public @Nullable DynamicRegistry<DimensionType> dimensionType = null;
    public @Nullable DynamicRegistry<Biome> biome = null;
    public @Nullable DynamicRegistry<DamageType> damageType = null;
    public @Nullable DynamicRegistry<TrimMaterial> trimMaterial = null;
    public @Nullable DynamicRegistry<TrimPattern> trimPattern = null;
    public @Nullable DynamicRegistry<BannerPattern> bannerPattern = null;
    public @Nullable DynamicRegistry<WolfVariant> wolfVariant = null;
    public @Nullable DynamicRegistry<Enchantment> enchantment = null;
    public @Nullable DynamicRegistry<PaintingVariant> paintingVariant = null;
    public @Nullable DynamicRegistry<JukeboxSong> jukeboxSong = null;
    public @Nullable DynamicRegistry<Instrument> instrument = null;
    public @Nullable DynamicRegistry<WolfSoundVariant> wolfSoundVariant = null;
    public @Nullable DynamicRegistry<CatVariant> catVariant = null;
    public @Nullable DynamicRegistry<ChickenVariant> chickenVariant = null;
    public @Nullable DynamicRegistry<CowVariant> cowVariant = null;
    public @Nullable DynamicRegistry<FrogVariant> frogVariant = null;
    public @Nullable DynamicRegistry<PigVariant> pigVariant = null;
    public @Nullable DynamicRegistry<ZombieNautilusVariant> zombieNautilusVariant = null;
    public @Nullable DynamicRegistry<Dialog> dialog = null;
    public @Nullable DynamicRegistry<Timeline> timeline = null;
    public @Nullable DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues = null;
    public @Nullable DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects = null;
    public @Nullable DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects = null;
    public @Nullable DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects = null;

    public TestRegistries(Consumer<TestRegistries> init) {
        init.accept(this);
    }

    @Override
    public DynamicRegistry<ChatType> chatType() {
        return Objects.requireNonNull(chatType);
    }

    @Override
    public DynamicRegistry<DimensionType> dimensionType() {
        return Objects.requireNonNull(dimensionType);
    }

    @Override
    public DynamicRegistry<Biome> biome() {
        return Objects.requireNonNull(biome);
    }

    @Override
    public DynamicRegistry<DamageType> damageType() {
        return Objects.requireNonNull(damageType);
    }

    @Override
    public DynamicRegistry<TrimMaterial> trimMaterial() {
        return Objects.requireNonNull(trimMaterial);
    }

    @Override
    public DynamicRegistry<TrimPattern> trimPattern() {
        return Objects.requireNonNull(trimPattern);
    }

    @Override
    public DynamicRegistry<BannerPattern> bannerPattern() {
        return Objects.requireNonNull(bannerPattern);
    }

    @Override
    public DynamicRegistry<WolfVariant> wolfVariant() {
        return Objects.requireNonNull(wolfVariant);
    }

    @Override
    public DynamicRegistry<WolfSoundVariant> wolfSoundVariant() {
        return Objects.requireNonNull(wolfSoundVariant);
    }

    @Override
    public DynamicRegistry<CatVariant> catVariant() {
        return Objects.requireNonNull(catVariant);
    }

    @Override
    public DynamicRegistry<ChickenVariant> chickenVariant() {
        return Objects.requireNonNull(chickenVariant);
    }

    @Override
    public DynamicRegistry<CowVariant> cowVariant() {
        return Objects.requireNonNull(cowVariant);
    }

    @Override
    public DynamicRegistry<FrogVariant> frogVariant() {
        return Objects.requireNonNull(frogVariant);
    }

    @Override
    public DynamicRegistry<PigVariant> pigVariant() {
        return Objects.requireNonNull(pigVariant);
    }

    @Override
    public DynamicRegistry<ZombieNautilusVariant> zombieNautilusVariant() {
        return Objects.requireNonNull(zombieNautilusVariant);
    }

    @Override
    public DynamicRegistry<Enchantment> enchantment() {
        return Objects.requireNonNull(enchantment);
    }

    @Override
    public DynamicRegistry<PaintingVariant> paintingVariant() {
        return Objects.requireNonNull(paintingVariant);
    }

    @Override
    public DynamicRegistry<JukeboxSong> jukeboxSong() {
        return Objects.requireNonNull(jukeboxSong);
    }

    @Override
    public DynamicRegistry<Instrument> instrument() {
        return Objects.requireNonNull(instrument);
    }

    @Override
    public DynamicRegistry<Dialog> dialog() {
        return Objects.requireNonNull(dialog);
    }

    @Override
    public DynamicRegistry<Timeline> timeline() {
        return Objects.requireNonNull(timeline);
    }

    @Override
    public DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues() {
        return Objects.requireNonNull(enchantmentLevelBasedValues);
    }

    @Override
    public DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects() {
        return Objects.requireNonNull(enchantmentValueEffects);
    }

    @Override
    public DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects() {
        return Objects.requireNonNull(enchantmentEntityEffects);
    }

    @Override
    public DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects() {
        return Objects.requireNonNull(enchantmentLocationEffects);
    }
}
