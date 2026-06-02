package net.minestom.server.registry;

import net.minestom.server.codec.StructCodec;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatSoundVariant;
import net.minestom.server.entity.metadata.animal.tameable.CatVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfSoundVariant;
import net.minestom.server.entity.metadata.animal.tameable.WolfVariant;
import net.minestom.server.entity.metadata.other.PaintingVariant;
import net.minestom.server.game.GameEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.banner.BannerPattern;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.instance.fluid.Fluid;
import net.minestom.server.instance.gamerule.GameRule;
import net.minestom.server.item.Material;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.item.enchant.*;
import net.minestom.server.item.instrument.Instrument;
import net.minestom.server.message.ChatType;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import net.minestom.server.world.clock.WorldClock;
import net.minestom.server.world.timeline.Timeline;

import java.util.List;

/**
 * <p>Provides access to all the dynamic registries. {@link net.minestom.server.ServerProcess} is the most relevant
 * implementation of this interface.</p>
 *
 * @see net.minestom.server.MinecraftServer for static access to these
 */
public interface Registries {
    static Registries vanilla() {
        return new VanillaRegistries();
    }

    static List<SendablePacket> registryDataPackets(Registries registries, boolean excludeVanilla) {
        return RegistriesImpl.registryDataPackets(registries, excludeVanilla);
    }

    static TagsPacket tagsPacket(Registries registries) {
        return RegistriesImpl.tagsPacket(registries);
    }

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

    default Registry<GameRule<?>> gameRule() {
        return GameRule.staticRegistry();
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

    DynamicRegistry<CatSoundVariant> catSoundVariant();

    DynamicRegistry<ChickenVariant> chickenVariant();

    DynamicRegistry<ChickenSoundVariant> chickenSoundVariant();

    DynamicRegistry<CowVariant> cowVariant();

    DynamicRegistry<CowSoundVariant> cowSoundVariant();

    DynamicRegistry<FrogVariant> frogVariant();

    DynamicRegistry<PigVariant> pigVariant();

    DynamicRegistry<PigSoundVariant> pigSoundVariant();

    DynamicRegistry<ZombieNautilusVariant> zombieNautilusVariant();

    DynamicRegistry<Dialog> dialog();

    DynamicRegistry<Timeline> timeline();

    DynamicRegistry<WorldClock> worldClock();

    // The following are _not_ sent to the client.

    DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues();

    DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects();

    DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects();

    DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects();

    @FunctionalInterface
    interface Selector<T> {
        Registry<T> select(Registries registries);
    }

    @FunctionalInterface
    interface Delegating extends Registries {
        Registries registries();

        @Override
        default Registry<Block> blocks() {
            return registries().blocks();
        }

        @Override
        default Registry<Material> material() {
            return registries().material();
        }

        @Override
        default Registry<PotionEffect> potionEffect() {
            return registries().potionEffect();
        }

        @Override
        default Registry<EntityType> entityType() {
            return registries().entityType();
        }

        @Override
        default Registry<Fluid> fluid() {
            return registries().fluid();
        }

        @Override
        default Registry<GameEvent> gameEvent() {
            return registries().gameEvent();
        }

        @Override
        default DynamicRegistry<ChatType> chatType() {
            return registries().chatType();
        }

        @Override
        default DynamicRegistry<DimensionType> dimensionType() {
            return registries().dimensionType();
        }

        @Override
        default DynamicRegistry<Biome> biome() {
            return registries().biome();
        }

        @Override
        default DynamicRegistry<DamageType> damageType() {
            return registries().damageType();
        }

        @Override
        default DynamicRegistry<TrimMaterial> trimMaterial() {
            return registries().trimMaterial();
        }

        @Override
        default DynamicRegistry<TrimPattern> trimPattern() {
            return registries().trimPattern();
        }

        @Override
        default DynamicRegistry<BannerPattern> bannerPattern() {
            return registries().bannerPattern();
        }

        @Override
        default DynamicRegistry<Enchantment> enchantment() {
            return registries().enchantment();
        }

        @Override
        default DynamicRegistry<PaintingVariant> paintingVariant() {
            return registries().paintingVariant();
        }

        @Override
        default DynamicRegistry<JukeboxSong> jukeboxSong() {
            return registries().jukeboxSong();
        }

        @Override
        default DynamicRegistry<Instrument> instrument() {
            return registries().instrument();
        }

        @Override
        default DynamicRegistry<WolfVariant> wolfVariant() {
            return registries().wolfVariant();
        }

        @Override
        default DynamicRegistry<WolfSoundVariant> wolfSoundVariant() {
            return registries().wolfSoundVariant();
        }

        @Override
        default DynamicRegistry<CatVariant> catVariant() {
            return registries().catVariant();
        }

        @Override
        default DynamicRegistry<CatSoundVariant> catSoundVariant() {
            return registries().catSoundVariant();
        }

        @Override
        default DynamicRegistry<ChickenVariant> chickenVariant() {
            return registries().chickenVariant();
        }

        @Override
        default DynamicRegistry<ChickenSoundVariant> chickenSoundVariant() {
            return registries().chickenSoundVariant();
        }

        @Override
        default DynamicRegistry<CowVariant> cowVariant() {
            return registries().cowVariant();
        }

        @Override
        default DynamicRegistry<CowSoundVariant> cowSoundVariant() {
            return registries().cowSoundVariant();
        }

        @Override
        default DynamicRegistry<FrogVariant> frogVariant() {
            return registries().frogVariant();
        }

        @Override
        default DynamicRegistry<PigVariant> pigVariant() {
            return registries().pigVariant();
        }

        @Override
        default DynamicRegistry<PigSoundVariant> pigSoundVariant() {
            return registries().pigSoundVariant();
        }

        @Override
        default DynamicRegistry<ZombieNautilusVariant> zombieNautilusVariant() {
            return registries().zombieNautilusVariant();
        }

        @Override
        default DynamicRegistry<Dialog> dialog() {
            return registries().dialog();
        }

        @Override
        default DynamicRegistry<Timeline> timeline() {
            return registries().timeline();
        }

        @Override
        default DynamicRegistry<WorldClock> worldClock() {
            return registries().worldClock();
        }

        @Override
        default DynamicRegistry<StructCodec<? extends LevelBasedValue>> enchantmentLevelBasedValues() {
            return registries().enchantmentLevelBasedValues();
        }

        @Override
        default DynamicRegistry<StructCodec<? extends ValueEffect>> enchantmentValueEffects() {
            return registries().enchantmentValueEffects();
        }

        @Override
        default DynamicRegistry<StructCodec<? extends EntityEffect>> enchantmentEntityEffects() {
            return registries().enchantmentEntityEffects();
        }

        @Override
        default DynamicRegistry<StructCodec<? extends LocationEffect>> enchantmentLocationEffects() {
            return registries().enchantmentLocationEffects();
        }
    }
}
