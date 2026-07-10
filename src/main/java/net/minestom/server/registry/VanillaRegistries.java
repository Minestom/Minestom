package net.minestom.server.registry;

import net.minestom.server.codec.StructCodec;
import net.minestom.server.component.DataComponents;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatSoundVariant;
import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfSoundVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfVariant;
import net.minestom.server.entity.metadata.cube.SulfurCubeArchetype;
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
import net.minestom.server.world.clock.WorldClock;
import net.minestom.server.world.timeline.Timeline;

final class VanillaRegistries implements Registries {
    private final DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues;
    private final DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects;
    private final DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects;
    private final DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects;
    private final DynamicRegistry<Codec<? extends DataComponentPredicate>> componentPredicateTypes;

    private final DynamicRegistry<ChatType> chatType;
    private final DynamicRegistry<Dialog> dialog;
    private final DynamicRegistry<DimensionType> dimensionType;
    private final DynamicRegistry<Biome> biome;
    private final DynamicRegistry<DamageType> damageType;
    private final DynamicRegistry<TrimMaterial> trimMaterial;
    private final DynamicRegistry<TrimPattern> trimPattern;
    private final DynamicRegistry<BannerPattern> bannerPattern;
    private final DynamicRegistry<Enchantment> enchantment;
    private final DynamicRegistry<PaintingVariant> paintingVariant;
    private final DynamicRegistry<JukeboxSong> jukeboxSong;
    private final DynamicRegistry<Instrument> instrument;
    private final DynamicRegistry<WolfVariant> wolfVariant;
    private final DynamicRegistry<WolfSoundVariant> wolfSoundVariant;
    private final DynamicRegistry<CatVariant> catVariant;
    private final DynamicRegistry<CatSoundVariant> catSoundVariant;
    private final DynamicRegistry<ChickenVariant> chickenVariant;
    private final DynamicRegistry<ChickenSoundVariant> chickenSoundVariant;
    private final DynamicRegistry<CowVariant> cowVariant;
    private final DynamicRegistry<CowSoundVariant> cowSoundVariant;
    private final DynamicRegistry<FrogVariant> frogVariant;
    private final DynamicRegistry<PigVariant> pigVariant;
    private final DynamicRegistry<PigSoundVariant> pigSoundVariant;
    private final DynamicRegistry<ZombieNautilusVariant> zombieNautilusVariant;
    private final DynamicRegistry<WorldClock> worldClock;
    private final DynamicRegistry<Timeline> timeline;
    private final DynamicRegistry<SulfurCubeArchetype> sulfurCubeArchetype;

    VanillaRegistries() {
        // The order of initialization here is relevant, we must load the enchantment util registries before the vanilla data is loaded.
        var ignoredForInit = DataComponents.ITEM_NAME;

        this.enchantmentLevelBasedValues = LevelBasedValue.createDefaultRegistry();
        this.enchantmentValueEffects = ValueEffect.createDefaultRegistry();
        this.enchantmentEntityEffects = EntityEffect.createDefaultRegistry();
        this.enchantmentLocationEffects = LocationEffect.createDefaultRegistry();
        this.componentPredicateTypes = DataComponentPredicate.createDefaultRegistry();

        this.chatType = ChatType.createDefaultRegistry();
        this.dialog = Dialog.createDefaultRegistry(this);
        this.biome = Biome.createDefaultRegistry();
        this.damageType = DamageType.createDefaultRegistry();
        this.trimMaterial = TrimMaterial.createDefaultRegistry();
        this.trimPattern = TrimPattern.createDefaultRegistry();
        this.bannerPattern = BannerPattern.createDefaultRegistry();
        this.enchantment = Enchantment.createDefaultRegistry(this);
        this.paintingVariant = PaintingVariant.createDefaultRegistry();
        this.jukeboxSong = JukeboxSong.createDefaultRegistry();
        this.instrument = Instrument.createDefaultRegistry();
        this.wolfVariant = WolfVariant.createDefaultRegistry();
        this.wolfSoundVariant = WolfSoundVariant.createDefaultRegistry();
        this.catVariant = CatVariant.createDefaultRegistry();
        this.catSoundVariant = CatSoundVariant.createDefaultRegistry();
        this.chickenVariant = ChickenVariant.createDefaultRegistry();
        this.chickenSoundVariant = ChickenSoundVariant.createDefaultRegistry();
        this.cowVariant = CowVariant.createDefaultRegistry();
        this.cowSoundVariant = CowSoundVariant.createDefaultRegistry();
        this.frogVariant = FrogVariant.createDefaultRegistry();
        this.pigVariant = PigVariant.createDefaultRegistry();
        this.pigSoundVariant = PigSoundVariant.createDefaultRegistry();
        this.zombieNautilusVariant = ZombieNautilusVariant.createDefaultRegistry();
        this.worldClock = WorldClock.createDefaultRegistry();
        this.timeline = Timeline.createDefaultRegistry(this);
        this.dimensionType = DimensionType.createDefaultRegistry(this); // depends on timelines
        this.sulfurCubeArchetype = SulfurCubeArchetype.createDefaultRegistry(this);

        // Quite a hack because materials are a static registry, and can be loaded before but are cyclic on components.
        // So we break the loop and bind them here
        for (var entry: material().values()) {
            entry.registry().bindComponents(this);
        }
    }

    @Override
    public DynamicRegistry<ChatType> chatType() {
        return chatType;
    }

    @Override
    public DynamicRegistry<DimensionType> dimensionType() {
        return dimensionType;
    }

    @Override
    public DynamicRegistry<Biome> biome() {
        return biome;
    }

    @Override
    public DynamicRegistry<DamageType> damageType() {
        return damageType;
    }

    @Override
    public DynamicRegistry<TrimMaterial> trimMaterial() {
        return trimMaterial;
    }

    @Override
    public DynamicRegistry<TrimPattern> trimPattern() {
        return trimPattern;
    }

    @Override
    public DynamicRegistry<BannerPattern> bannerPattern() {
        return bannerPattern;
    }

    @Override
    public DynamicRegistry<Enchantment> enchantment() {
        return enchantment;
    }

    @Override
    public DynamicRegistry<PaintingVariant> paintingVariant() {
        return paintingVariant;
    }

    @Override
    public DynamicRegistry<JukeboxSong> jukeboxSong() {
        return jukeboxSong;
    }

    @Override
    public DynamicRegistry<Instrument> instrument() {
        return instrument;
    }

    @Override
    public DynamicRegistry<WolfVariant> wolfVariant() {
        return wolfVariant;
    }

    @Override
    public DynamicRegistry<WolfSoundVariant> wolfSoundVariant() {
        return wolfSoundVariant;
    }

    @Override
    public DynamicRegistry<CatVariant> catVariant() {
        return catVariant;
    }

    @Override
    public DynamicRegistry<CatSoundVariant> catSoundVariant() {
        return catSoundVariant;
    }

    @Override
    public DynamicRegistry<ChickenVariant> chickenVariant() {
        return chickenVariant;
    }

    @Override
    public DynamicRegistry<ChickenSoundVariant> chickenSoundVariant() {
        return chickenSoundVariant;
    }

    @Override
    public DynamicRegistry<CowVariant> cowVariant() {
        return cowVariant;
    }

    @Override
    public DynamicRegistry<CowSoundVariant> cowSoundVariant() {
        return cowSoundVariant;
    }

    @Override
    public DynamicRegistry<FrogVariant> frogVariant() {
        return frogVariant;
    }

    @Override
    public DynamicRegistry<PigVariant> pigVariant() {
        return pigVariant;
    }

    @Override
    public DynamicRegistry<PigSoundVariant> pigSoundVariant() {
        return pigSoundVariant;
    }

    @Override
    public DynamicRegistry<ZombieNautilusVariant> zombieNautilusVariant() {
        return zombieNautilusVariant;
    }

    @Override
    public DynamicRegistry<Dialog> dialog() {
        return dialog;
    }

    @Override
    public DynamicRegistry<WorldClock> worldClock() {
        return worldClock;
    }

    @Override
    public DynamicRegistry<Timeline> timeline() {
        return timeline;
    }

    @Override
    public DynamicRegistry<SulfurCubeArchetype> sulfurCubeArchetype() {
        return sulfurCubeArchetype;
    }

    @Override
    public DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues() {
        return enchantmentLevelBasedValues;
    }

    @Override
    public DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects() {
        return enchantmentValueEffects;
    }

    @Override
    public DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects() {
        return enchantmentEntityEffects;
    }

    @Override
    public DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects() {
        return enchantmentLocationEffects;
    }

    @Override
    public DynamicRegistry<Codec<? extends DataComponentPredicate>> componentPredicateTypes() {
        return componentPredicateTypes;
    }
}
