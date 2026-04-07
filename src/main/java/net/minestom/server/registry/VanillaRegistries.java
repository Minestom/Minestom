package net.minestom.server.registry;

import net.minestom.server.codec.StructCodec;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatSoundVariant;
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
import net.minestom.server.world.clock.WorldClock;
import net.minestom.server.world.timeline.Timeline;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Vanilla registries are built in registries that the client also can load or aware of its existence.
 */
public final class VanillaRegistries {

    /**
     * Loads all the builtin registries.
     *
     * @param executor the executor to use for loading the registries asynchronously.
     *                 Note that some registries depend on others, so the order of loading is not guaranteed.
     *                 For example, the {@link Dialog} registry depends on the {@link ChatType} registry,
     *                 so if you want to load them synchronously, you should use an executor that only has one worker
     * @return the registries object, potentially frozen
     */
    public static Registries load(Executor executor) {
        return new Registries() { // This registry is also known as MutableRegistries
            final CompletableFuture<DynamicRegistry<StructCodec<? extends LevelBasedValue>>> enchantmentLevelBasedValues = CompletableFuture.supplyAsync(LevelBasedValue::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<StructCodec<? extends ValueEffect>>> enchantmentValueEffects = CompletableFuture.supplyAsync(ValueEffect::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<StructCodec<? extends EntityEffect>>> enchantmentEntityEffects = CompletableFuture.supplyAsync(EntityEffect::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<StructCodec<? extends LocationEffect>>> enchantmentLocationEffects = CompletableFuture.supplyAsync(LocationEffect::createDefaultRegistry, executor);

            final CompletableFuture<DynamicRegistry<ChatType>> chatType = CompletableFuture.supplyAsync(ChatType::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<Dialog>> dialog = CompletableFuture.supplyAsync(() -> Dialog.createDefaultRegistry(this), executor);
            final CompletableFuture<DynamicRegistry<Biome>> biome = CompletableFuture.supplyAsync(Biome::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<DamageType>> damageType = CompletableFuture.supplyAsync(DamageType::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<TrimMaterial>> trimMaterial = CompletableFuture.supplyAsync(TrimMaterial::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<TrimPattern>> trimPattern = CompletableFuture.supplyAsync(TrimPattern::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<BannerPattern>> bannerPattern = CompletableFuture.supplyAsync(BannerPattern::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<Enchantment>> enchantment = CompletableFuture.supplyAsync(() -> Enchantment.createDefaultRegistry(this), executor);
            final CompletableFuture<DynamicRegistry<PaintingVariant>> paintingVariant = CompletableFuture.supplyAsync(PaintingVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<JukeboxSong>> jukeboxSong = CompletableFuture.supplyAsync(JukeboxSong::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<Instrument>> instrument = CompletableFuture.supplyAsync(Instrument::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<WolfVariant>> wolfVariant = CompletableFuture.supplyAsync(WolfVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<WolfSoundVariant>> wolfSoundVariant = CompletableFuture.supplyAsync(WolfSoundVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<CatVariant>> catVariant = CompletableFuture.supplyAsync(CatVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<CatSoundVariant>> catSoundVariant = CompletableFuture.supplyAsync(CatSoundVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<ChickenVariant>> chickenVariant = CompletableFuture.supplyAsync(ChickenVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<ChickenSoundVariant>> chickenSoundVariant = CompletableFuture.supplyAsync(ChickenSoundVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<CowVariant>> cowVariant = CompletableFuture.supplyAsync(CowVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<CowSoundVariant>> cowSoundVariant = CompletableFuture.supplyAsync(CowSoundVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<FrogVariant>> frogVariant = CompletableFuture.supplyAsync(FrogVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<PigVariant>> pigVariant = CompletableFuture.supplyAsync(PigVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<PigSoundVariant>> pigSoundVariant = CompletableFuture.supplyAsync(PigSoundVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<ZombieNautilusVariant>> zombieNautilusVariant = CompletableFuture.supplyAsync(ZombieNautilusVariant::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<WorldClock>> worldClock = CompletableFuture.supplyAsync(WorldClock::createDefaultRegistry, executor);
            final CompletableFuture<DynamicRegistry<Timeline>> timeline = CompletableFuture.supplyAsync(() -> Timeline.createDefaultRegistry(this), executor);
            final CompletableFuture<DynamicRegistry<DimensionType>> dimensionType = CompletableFuture.supplyAsync(() -> DimensionType.createDefaultRegistry(this), executor);

            @Override
            public DynamicRegistry<ChatType> chatType() {
                return chatType.join();
            }

            @Override
            public DynamicRegistry<DimensionType> dimensionType() {
                return dimensionType.join();
            }

            @Override
            public DynamicRegistry<Biome> biome() {
                return biome.join();
            }

            @Override
            public DynamicRegistry<DamageType> damageType() {
                return damageType.join();
            }

            @Override
            public DynamicRegistry<TrimMaterial> trimMaterial() {
                return trimMaterial.join();
            }

            @Override
            public DynamicRegistry<TrimPattern> trimPattern() {
                return trimPattern.join();
            }

            @Override
            public DynamicRegistry<BannerPattern> bannerPattern() {
                return bannerPattern.join();
            }

            @Override
            public DynamicRegistry<Enchantment> enchantment() {
                return enchantment.join();
            }

            @Override
            public DynamicRegistry<PaintingVariant> paintingVariant() {
                return paintingVariant.join();
            }

            @Override
            public DynamicRegistry<JukeboxSong> jukeboxSong() {
                return jukeboxSong.join();
            }

            @Override
            public DynamicRegistry<Instrument> instrument() {
                return instrument.join();
            }

            @Override
            public DynamicRegistry<WolfVariant> wolfVariant() {
                return wolfVariant.join();
            }

            @Override
            public DynamicRegistry<WolfSoundVariant> wolfSoundVariant() {
                return wolfSoundVariant.join();
            }

            @Override
            public DynamicRegistry<CatVariant> catVariant() {
                return catVariant.join();
            }

            @Override
            public DynamicRegistry<CatSoundVariant> catSoundVariant() {
                return catSoundVariant.join();
            }

            @Override
            public DynamicRegistry<ChickenVariant> chickenVariant() {
                return chickenVariant.join();
            }

            @Override
            public DynamicRegistry<ChickenSoundVariant> chickenSoundVariant() {
                return chickenSoundVariant.join();
            }

            @Override
            public DynamicRegistry<CowVariant> cowVariant() {
                return cowVariant.join();
            }

            @Override
            public DynamicRegistry<CowSoundVariant> cowSoundVariant() {
                return cowSoundVariant.join();
            }

            @Override
            public DynamicRegistry<FrogVariant> frogVariant() {
                return frogVariant.join();
            }

            @Override
            public DynamicRegistry<PigVariant> pigVariant() {
                return pigVariant.join();
            }

            @Override
            public DynamicRegistry<PigSoundVariant> pigSoundVariant() {
                return pigSoundVariant.join();
            }

            @Override
            public DynamicRegistry<ZombieNautilusVariant> zombieNautilusVariant() {
                return zombieNautilusVariant.join();
            }

            @Override
            public DynamicRegistry<Dialog> dialog() {
                return dialog.join();
            }

            @Override
            public DynamicRegistry<Timeline> timeline() {
                return timeline.join();
            }

            @Override
            public DynamicRegistry<WorldClock> worldClock() {
                return worldClock.join();
            }

            @Override
            public DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues() {
                return enchantmentLevelBasedValues.join();
            }

            @Override
            public DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects() {
                return enchantmentValueEffects.join();
            }

            @Override
            public DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects() {
                return enchantmentEntityEffects.join();
            }

            @Override
            public DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects() {
                return enchantmentLocationEffects.join();
            }
        };
    }
}
