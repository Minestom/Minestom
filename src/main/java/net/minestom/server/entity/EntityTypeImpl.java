package net.minestom.server.entity;

import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.ParrotMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.arrow.ArrowMeta;
import net.minestom.server.entity.metadata.arrow.SpectralArrowMeta;
import net.minestom.server.entity.metadata.arrow.ThrownTridentMeta;
import net.minestom.server.entity.metadata.flying.GhastMeta;
import net.minestom.server.entity.metadata.flying.PhantomMeta;
import net.minestom.server.entity.metadata.golem.IronGolemMeta;
import net.minestom.server.entity.metadata.golem.ShulkerMeta;
import net.minestom.server.entity.metadata.golem.SnowGolemMeta;
import net.minestom.server.entity.metadata.item.*;
import net.minestom.server.entity.metadata.minecart.*;
import net.minestom.server.entity.metadata.monster.*;
import net.minestom.server.entity.metadata.monster.raider.*;
import net.minestom.server.entity.metadata.monster.skeleton.SkeletonMeta;
import net.minestom.server.entity.metadata.monster.skeleton.StrayMeta;
import net.minestom.server.entity.metadata.monster.skeleton.WitherSkeletonMeta;
import net.minestom.server.entity.metadata.monster.zombie.*;
import net.minestom.server.entity.metadata.other.*;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.entity.metadata.villager.WanderingTraderMeta;
import net.minestom.server.entity.metadata.water.AxolotlMeta;
import net.minestom.server.entity.metadata.water.DolphinMeta;
import net.minestom.server.entity.metadata.water.GlowSquidMeta;
import net.minestom.server.entity.metadata.water.SquidMeta;
import net.minestom.server.entity.metadata.water.fish.CodMeta;
import net.minestom.server.entity.metadata.water.fish.PufferfishMeta;
import net.minestom.server.entity.metadata.water.fish.SalmonMeta;
import net.minestom.server.entity.metadata.water.fish.TropicalFishMeta;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

import static java.util.Map.entry;

record EntityTypeImpl(Registry.EntityEntry registry) implements EntityType {
    private static final Registry.Container<EntityType> CONTAINER = Registry.createContainer(Registry.Resource.ENTITIES,
            (namespace, properties) -> new EntityTypeImpl(Registry.entity(namespace, properties)));
    static final Map<String, BiFunction<Entity, Metadata, EntityMeta>> ENTITY_META_SUPPLIER = createMetaMap();

    static EntityType get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static EntityType getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static EntityType getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<EntityType> values() {
        return CONTAINER.values();
    }

    static EntityMeta createMeta(EntityType entityType, Entity entity, Metadata metadata) {
        return ENTITY_META_SUPPLIER.get(entityType.name()).apply(entity, metadata);
    }

    @Override
    public String toString() {
        return name();
    }

    private static Map<String, BiFunction<Entity, Metadata, EntityMeta>> createMetaMap() {
        return Map.<String, BiFunction<Entity, Metadata, EntityMeta>>ofEntries(
                entry("minecraft:allay", EntityMeta::new), // TODO dedicated metadata
                entry("minecraft:area_effect_cloud", AreaEffectCloudMeta::new),
                entry("minecraft:armor_stand", ArmorStandMeta::new),
                entry("minecraft:arrow", ArrowMeta::new),
                entry("minecraft:axolotl", AxolotlMeta::new),
                entry("minecraft:bat", BatMeta::new),
                entry("minecraft:bee", BeeMeta::new),
                entry("minecraft:blaze", BlazeMeta::new),
                entry("minecraft:boat", BoatMeta::new),
                entry("minecraft:chest_boat", EntityMeta::new), // TODO dedicated metadata
                entry("minecraft:cat", CatMeta::new),
                entry("minecraft:cave_spider", CaveSpiderMeta::new),
                entry("minecraft:chicken", ChickenMeta::new),
                entry("minecraft:cod", CodMeta::new),
                entry("minecraft:cow", CowMeta::new),
                entry("minecraft:creeper", CreeperMeta::new),
                entry("minecraft:dolphin", DolphinMeta::new),
                entry("minecraft:donkey", DonkeyMeta::new),
                entry("minecraft:dragon_fireball", DragonFireballMeta::new),
                entry("minecraft:drowned", DrownedMeta::new),
                entry("minecraft:elder_guardian", ElderGuardianMeta::new),
                entry("minecraft:end_crystal", EndCrystalMeta::new),
                entry("minecraft:ender_dragon", EnderDragonMeta::new),
                entry("minecraft:enderman", EndermanMeta::new),
                entry("minecraft:endermite", EndermiteMeta::new),
                entry("minecraft:evoker", EvokerMeta::new),
                entry("minecraft:evoker_fangs", EvokerFangsMeta::new),
                entry("minecraft:experience_orb", ExperienceOrbMeta::new),
                entry("minecraft:eye_of_ender", EyeOfEnderMeta::new),
                entry("minecraft:falling_block", FallingBlockMeta::new),
                entry("minecraft:firework_rocket", FireworkRocketMeta::new),
                entry("minecraft:fox", FoxMeta::new),
                entry("minecraft:frog", EntityMeta::new), // TODO dedicated metadata
                entry("minecraft:ghast", GhastMeta::new),
                entry("minecraft:giant", GiantMeta::new),
                entry("minecraft:glow_item_frame", GlowItemFrameMeta::new),
                entry("minecraft:glow_squid", GlowSquidMeta::new),
                entry("minecraft:goat", GoatMeta::new),
                entry("minecraft:guardian", GuardianMeta::new),
                entry("minecraft:hoglin", HoglinMeta::new),
                entry("minecraft:horse", HorseMeta::new),
                entry("minecraft:husk", HuskMeta::new),
                entry("minecraft:illusioner", IllusionerMeta::new),
                entry("minecraft:iron_golem", IronGolemMeta::new),
                entry("minecraft:item", ItemEntityMeta::new),
                entry("minecraft:item_frame", ItemFrameMeta::new),
                entry("minecraft:fireball", FireballMeta::new),
                entry("minecraft:leash_knot", LeashKnotMeta::new),
                entry("minecraft:lightning_bolt", LightningBoltMeta::new),
                entry("minecraft:llama", LlamaMeta::new),
                entry("minecraft:llama_spit", LlamaSpitMeta::new),
                entry("minecraft:magma_cube", MagmaCubeMeta::new),
                entry("minecraft:marker", MarkerMeta::new),
                entry("minecraft:minecart", MinecartMeta::new),
                entry("minecraft:chest_minecart", ChestMinecartMeta::new),
                entry("minecraft:command_block_minecart", CommandBlockMinecartMeta::new),
                entry("minecraft:furnace_minecart", FurnaceMinecartMeta::new),
                entry("minecraft:hopper_minecart", HopperMinecartMeta::new),
                entry("minecraft:spawner_minecart", SpawnerMinecartMeta::new),
                entry("minecraft:tnt_minecart", TntMinecartMeta::new),
                entry("minecraft:mule", MuleMeta::new),
                entry("minecraft:mooshroom", MooshroomMeta::new),
                entry("minecraft:ocelot", OcelotMeta::new),
                entry("minecraft:painting", PaintingMeta::new),
                entry("minecraft:panda", PandaMeta::new),
                entry("minecraft:parrot", ParrotMeta::new),
                entry("minecraft:phantom", PhantomMeta::new),
                entry("minecraft:pig", PigMeta::new),
                entry("minecraft:piglin", PiglinMeta::new),
                entry("minecraft:piglin_brute", PiglinBruteMeta::new),
                entry("minecraft:pillager", PillagerMeta::new),
                entry("minecraft:polar_bear", PolarBearMeta::new),
                entry("minecraft:tnt", PrimedTntMeta::new),
                entry("minecraft:pufferfish", PufferfishMeta::new),
                entry("minecraft:rabbit", RabbitMeta::new),
                entry("minecraft:ravager", RavagerMeta::new),
                entry("minecraft:salmon", SalmonMeta::new),
                entry("minecraft:sheep", SheepMeta::new),
                entry("minecraft:shulker", ShulkerMeta::new),
                entry("minecraft:shulker_bullet", ShulkerBulletMeta::new),
                entry("minecraft:silverfish", SilverfishMeta::new),
                entry("minecraft:skeleton", SkeletonMeta::new),
                entry("minecraft:skeleton_horse", SkeletonHorseMeta::new),
                entry("minecraft:slime", SlimeMeta::new),
                entry("minecraft:small_fireball", SmallFireballMeta::new),
                entry("minecraft:snow_golem", SnowGolemMeta::new),
                entry("minecraft:snowball", SnowballMeta::new),
                entry("minecraft:spectral_arrow", SpectralArrowMeta::new),
                entry("minecraft:spider", SpiderMeta::new),
                entry("minecraft:squid", SquidMeta::new),
                entry("minecraft:stray", StrayMeta::new),
                entry("minecraft:strider", StriderMeta::new),
                entry("minecraft:tadpole", EntityMeta::new), // TODO dedicated metadata
                entry("minecraft:egg", ThrownEggMeta::new),
                entry("minecraft:ender_pearl", ThrownEnderPearlMeta::new),
                entry("minecraft:experience_bottle", ThrownExperienceBottleMeta::new),
                entry("minecraft:potion", ThrownPotionMeta::new),
                entry("minecraft:trident", ThrownTridentMeta::new),
                entry("minecraft:trader_llama", TraderLlamaMeta::new),
                entry("minecraft:tropical_fish", TropicalFishMeta::new),
                entry("minecraft:turtle", TurtleMeta::new),
                entry("minecraft:vex", VexMeta::new),
                entry("minecraft:villager", VillagerMeta::new),
                entry("minecraft:vindicator", VindicatorMeta::new),
                entry("minecraft:wandering_trader", WanderingTraderMeta::new),
                entry("minecraft:warden", EntityMeta::new), // TODO dedicated metadata
                entry("minecraft:witch", WitchMeta::new),
                entry("minecraft:wither", WitherMeta::new),
                entry("minecraft:wither_skeleton", WitherSkeletonMeta::new),
                entry("minecraft:wither_skull", WitherSkullMeta::new),
                entry("minecraft:wolf", WolfMeta::new),
                entry("minecraft:zoglin", ZoglinMeta::new),
                entry("minecraft:zombie", ZombieMeta::new),
                entry("minecraft:zombie_horse", ZombieHorseMeta::new),
                entry("minecraft:zombie_villager", ZombieVillagerMeta::new),
                entry("minecraft:zombified_piglin", ZombifiedPiglinMeta::new),
                entry("minecraft:player", PlayerMeta::new),
                entry("minecraft:fishing_bobber", FishingHookMeta::new)
        );
    }
}
