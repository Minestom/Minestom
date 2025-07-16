package net.minestom.server.registry;

import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.ChickenVariant;
import net.minestom.server.entity.metadata.animal.CowVariant;
import net.minestom.server.entity.metadata.animal.FrogVariant;
import net.minestom.server.entity.metadata.animal.PigVariant;
import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfSoundVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfVariant;
import net.minestom.server.entity.metadata.other.PaintingVariant;
import net.minestom.server.game.GameEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.instance.fluid.Fluid;
import net.minestom.server.item.Material;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.item.enchant.EntityEffect;
import net.minestom.server.item.enchant.LevelBasedValue;
import net.minestom.server.item.enchant.LocationEffect;
import net.minestom.server.item.enchant.ValueEffect;
import net.minestom.server.item.instrument.Instrument;
import net.minestom.server.message.ChatType;
import net.minestom.server.potion.PotionEffect;
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

    // Static registries

    // The name block conflicts with blockmanager :(
    default @NotNull Registry<Block> blocks() {
        return Block.staticRegistry();
    }

    default @NotNull Registry<Material> material() {
        return Material.staticRegistry();
    }

    default @NotNull Registry<PotionEffect> potionEffect() {
        return PotionEffect.staticRegistry();
    }

    default @NotNull Registry<EntityType> entityType() {
        return EntityType.staticRegistry();
    }

    default @NotNull Registry<Fluid> fluid() {
        return Fluid.staticRegistry();
    }

    default @NotNull Registry<GameEvent> gameEvent() {
        return GameEvent.staticRegistry();
    }

    // Dynamic registries

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

    @NotNull DynamicRegistry<Dialog> dialog();

    // The following are _not_ sent to the client.

    @NotNull DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues();

    @NotNull DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects();

    @NotNull DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects();

    @NotNull DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects();

    @FunctionalInterface
    interface Selector<T> {
        @NotNull Registry<T> select(@NotNull Registries registries);
    }

    class Delegating implements Registries {
        private final Registries delegate;

        public Delegating(@NotNull Registries delegate) {
            this.delegate = delegate;
        }

        @Override
        public @NotNull Registry<Block> blocks() {
            return delegate.blocks();
        }

        @Override
        public @NotNull Registry<Material> material() {
            return delegate.material();
        }

        @Override
        public @NotNull Registry<PotionEffect> potionEffect() {
            return delegate.potionEffect();
        }

        @Override
        public @NotNull Registry<EntityType> entityType() {
            return delegate.entityType();
        }

        @Override
        public @NotNull Registry<Fluid> fluid() {
            return delegate.fluid();
        }

        @Override
        public @NotNull Registry<GameEvent> gameEvent() {
            return delegate.gameEvent();
        }

        @Override
        public @NotNull DynamicRegistry<ChatType> chatType() {
            return delegate.chatType();
        }

        @Override
        public @NotNull DynamicRegistry<DimensionType> dimensionType() {
            return delegate.dimensionType();
        }

        @Override
        public @NotNull DynamicRegistry<Biome> biome() {
            return delegate.biome();
        }

        @Override
        public @NotNull DynamicRegistry<DamageType> damageType() {
            return delegate.damageType();
        }

        @Override
        public @NotNull DynamicRegistry<TrimMaterial> trimMaterial() {
            return delegate.trimMaterial();
        }

        @Override
        public @NotNull DynamicRegistry<TrimPattern> trimPattern() {
            return delegate.trimPattern();
        }

        @Override
        public @NotNull DynamicRegistry<BannerPattern> bannerPattern() {
            return delegate.bannerPattern();
        }

        @Override
        public @NotNull DynamicRegistry<Enchantment> enchantment() {
            return delegate.enchantment();
        }

        @Override
        public @NotNull DynamicRegistry<PaintingVariant> paintingVariant() {
            return delegate.paintingVariant();
        }

        @Override
        public @NotNull DynamicRegistry<JukeboxSong> jukeboxSong() {
            return delegate.jukeboxSong();
        }

        @Override
        public @NotNull DynamicRegistry<Instrument> instrument() {
            return delegate.instrument();
        }

        @Override
        public @NotNull DynamicRegistry<WolfVariant> wolfVariant() {
            return delegate.wolfVariant();
        }

        @Override
        public @NotNull DynamicRegistry<WolfSoundVariant> wolfSoundVariant() {
            return delegate.wolfSoundVariant();
        }

        @Override
        public @NotNull DynamicRegistry<CatVariant> catVariant() {
            return delegate.catVariant();
        }

        @Override
        public @NotNull DynamicRegistry<ChickenVariant> chickenVariant() {
            return delegate.chickenVariant();
        }

        @Override
        public @NotNull DynamicRegistry<CowVariant> cowVariant() {
            return delegate.cowVariant();
        }

        @Override
        public @NotNull DynamicRegistry<FrogVariant> frogVariant() {
            return delegate.frogVariant();
        }

        @Override
        public @NotNull DynamicRegistry<PigVariant> pigVariant() {
            return delegate.pigVariant();
        }

        @Override
        public @NotNull DynamicRegistry<Dialog> dialog() {
            return delegate.dialog();
        }

        @Override
        public @NotNull DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues() {
            return delegate.enchantmentLevelBasedValues();
        }

        @Override
        public @NotNull DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects() {
            return delegate.enchantmentValueEffects();
        }

        @Override
        public @NotNull DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects() {
            return delegate.enchantmentEntityEffects();
        }

        @Override
        public @NotNull DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects() {
            return delegate.enchantmentLocationEffects();
        }
    }
}
