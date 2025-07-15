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

/**
 * <p>Provides access to all the dynamic registries. {@link net.minestom.server.ServerProcess} is the most relevant
 * implementation of this interface.</p>
 *
 * @see net.minestom.server.MinecraftServer for static access to these
 */
public interface Registries {

    // Static registries

    // The name block conflicts with blockmanager :(
    default Registry<Block> blocks() {
        return Block.staticRegistry();
    }

    default Registry<Material> material() {
        return Material.staticRegistry();
    }

    default Registry<PotionEffect> potionEffect() {
        return PotionEffect.staticRegistry();
    }

    default Registry<EntityType> entityType() {
        return EntityType.staticRegistry();
    }

    default Registry<Fluid> fluid() {
        return Fluid.staticRegistry();
    }

    default Registry<GameEvent> gameEvent() {
        return GameEvent.staticRegistry();
    }

    // Dynamic registries

    DynamicRegistry<ChatType> chatType();

    DynamicRegistry<DimensionType> dimensionType();

    DynamicRegistry<Biome> biome();

    DynamicRegistry<DamageType> damageType();

    DynamicRegistry<TrimMaterial> trimMaterial();

    DynamicRegistry<TrimPattern> trimPattern();

    DynamicRegistry<BannerPattern> bannerPattern();

    DynamicRegistry<Enchantment> enchantment();

    DynamicRegistry<PaintingVariant> paintingVariant();

    DynamicRegistry<JukeboxSong> jukeboxSong();

    DynamicRegistry<Instrument> instrument();

    DynamicRegistry<WolfVariant> wolfVariant();

    DynamicRegistry<WolfSoundVariant> wolfSoundVariant();

    DynamicRegistry<CatVariant> catVariant();

    DynamicRegistry<ChickenVariant> chickenVariant();

    DynamicRegistry<CowVariant> cowVariant();

    DynamicRegistry<FrogVariant> frogVariant();

    DynamicRegistry<PigVariant> pigVariant();

    DynamicRegistry<Dialog> dialog();

    // The following are _not_ sent to the client.

    DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues();

    DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects();

    DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects();

    DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects();

    @FunctionalInterface
    interface Selector<T> {
        Registry<T> select(Registries registries);
    }

    class Delegating implements Registries {
        private final Registries delegate;

        public Delegating(Registries delegate) {
            this.delegate = delegate;
        }

        @Override
        public Registry<Block> blocks() {
            return delegate.blocks();
        }

        @Override
        public Registry<Material> material() {
            return delegate.material();
        }

        @Override
        public Registry<PotionEffect> potionEffect() {
            return delegate.potionEffect();
        }

        @Override
        public Registry<EntityType> entityType() {
            return delegate.entityType();
        }

        @Override
        public Registry<Fluid> fluid() {
            return delegate.fluid();
        }

        @Override
        public Registry<GameEvent> gameEvent() {
            return delegate.gameEvent();
        }

        @Override
        public DynamicRegistry<ChatType> chatType() {
            return delegate.chatType();
        }

        @Override
        public DynamicRegistry<DimensionType> dimensionType() {
            return delegate.dimensionType();
        }

        @Override
        public DynamicRegistry<Biome> biome() {
            return delegate.biome();
        }

        @Override
        public DynamicRegistry<DamageType> damageType() {
            return delegate.damageType();
        }

        @Override
        public DynamicRegistry<TrimMaterial> trimMaterial() {
            return delegate.trimMaterial();
        }

        @Override
        public DynamicRegistry<TrimPattern> trimPattern() {
            return delegate.trimPattern();
        }

        @Override
        public DynamicRegistry<BannerPattern> bannerPattern() {
            return delegate.bannerPattern();
        }

        @Override
        public DynamicRegistry<Enchantment> enchantment() {
            return delegate.enchantment();
        }

        @Override
        public DynamicRegistry<PaintingVariant> paintingVariant() {
            return delegate.paintingVariant();
        }

        @Override
        public DynamicRegistry<JukeboxSong> jukeboxSong() {
            return delegate.jukeboxSong();
        }

        @Override
        public DynamicRegistry<Instrument> instrument() {
            return delegate.instrument();
        }

        @Override
        public DynamicRegistry<WolfVariant> wolfVariant() {
            return delegate.wolfVariant();
        }

        @Override
        public DynamicRegistry<WolfSoundVariant> wolfSoundVariant() {
            return delegate.wolfSoundVariant();
        }

        @Override
        public DynamicRegistry<CatVariant> catVariant() {
            return delegate.catVariant();
        }

        @Override
        public DynamicRegistry<ChickenVariant> chickenVariant() {
            return delegate.chickenVariant();
        }

        @Override
        public DynamicRegistry<CowVariant> cowVariant() {
            return delegate.cowVariant();
        }

        @Override
        public DynamicRegistry<FrogVariant> frogVariant() {
            return delegate.frogVariant();
        }

        @Override
        public DynamicRegistry<PigVariant> pigVariant() {
            return delegate.pigVariant();
        }

        @Override
        public DynamicRegistry<Dialog> dialog() {
            return delegate.dialog();
        }

        @Override
        public DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues() {
            return delegate.enchantmentLevelBasedValues();
        }

        @Override
        public DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects() {
            return delegate.enchantmentValueEffects();
        }

        @Override
        public DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects() {
            return delegate.enchantmentEntityEffects();
        }

        @Override
        public DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects() {
            return delegate.enchantmentLocationEffects();
        }
    }
}
