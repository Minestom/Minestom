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
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

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

    private static Map<String, BiFunction<Entity, Metadata, EntityMeta>> createMetaMap() {
        Map<String, BiFunction<Entity, Metadata, EntityMeta>> supplier = new HashMap<>();
        supplier.put("minecraft:area_effect_cloud", AreaEffectCloudMeta::new);
        supplier.put("minecraft:armor_stand", ArmorStandMeta::new);
        supplier.put("minecraft:arrow", ArrowMeta::new);
        supplier.put("minecraft:axolotl", AxolotlMeta::new);
        supplier.put("minecraft:bat", BatMeta::new);
        supplier.put("minecraft:bee", BeeMeta::new);
        supplier.put("minecraft:blaze", BlazeMeta::new);
        supplier.put("minecraft:boat", BoatMeta::new);
        supplier.put("minecraft:cat", CatMeta::new);
        supplier.put("minecraft:cave_spider", CaveSpiderMeta::new);
        supplier.put("minecraft:chicken", ChickenMeta::new);
        supplier.put("minecraft:cod", CodMeta::new);
        supplier.put("minecraft:cow", CowMeta::new);
        supplier.put("minecraft:creeper", CreeperMeta::new);
        supplier.put("minecraft:dolphin", DolphinMeta::new);
        supplier.put("minecraft:donkey", DonkeyMeta::new);
        supplier.put("minecraft:dragon_fireball", DragonFireballMeta::new);
        supplier.put("minecraft:drowned", DrownedMeta::new);
        supplier.put("minecraft:elder_guardian", ElderGuardianMeta::new);
        supplier.put("minecraft:end_crystal", EndCrystalMeta::new);
        supplier.put("minecraft:ender_dragon", EnderDragonMeta::new);
        supplier.put("minecraft:enderman", EndermanMeta::new);
        supplier.put("minecraft:endermite", EndermiteMeta::new);
        supplier.put("minecraft:evoker", EvokerMeta::new);
        supplier.put("minecraft:evoker_fangs", EvokerFangsMeta::new);
        supplier.put("minecraft:experience_orb", ExperienceOrbMeta::new);
        supplier.put("minecraft:eye_of_ender", EyeOfEnderMeta::new);
        supplier.put("minecraft:falling_block", FallingBlockMeta::new);
        supplier.put("minecraft:firework_rocket", FireworkRocketMeta::new);
        supplier.put("minecraft:fox", FoxMeta::new);
        supplier.put("minecraft:ghast", GhastMeta::new);
        supplier.put("minecraft:giant", GiantMeta::new);
        supplier.put("minecraft:glow_item_frame", GlowItemFrameMeta::new);
        supplier.put("minecraft:glow_squid", GlowSquidMeta::new);
        supplier.put("minecraft:goat", GoatMeta::new);
        supplier.put("minecraft:guardian", GuardianMeta::new);
        supplier.put("minecraft:hoglin", HoglinMeta::new);
        supplier.put("minecraft:horse", HorseMeta::new);
        supplier.put("minecraft:husk", HuskMeta::new);
        supplier.put("minecraft:illusioner", IllusionerMeta::new);
        supplier.put("minecraft:iron_golem", IronGolemMeta::new);
        supplier.put("minecraft:item", ItemEntityMeta::new);
        supplier.put("minecraft:item_frame", ItemFrameMeta::new);
        supplier.put("minecraft:fireball", FireballMeta::new);
        supplier.put("minecraft:leash_knot", LeashKnotMeta::new);
        supplier.put("minecraft:lightning_bolt", LightningBoltMeta::new);
        supplier.put("minecraft:llama", LlamaMeta::new);
        supplier.put("minecraft:llama_spit", LlamaSpitMeta::new);
        supplier.put("minecraft:magma_cube", MagmaCubeMeta::new);
        supplier.put("minecraft:marker", MarkerMeta::new);
        supplier.put("minecraft:minecart", MinecartMeta::new);
        supplier.put("minecraft:chest_minecart", ChestMinecartMeta::new);
        supplier.put("minecraft:command_block_minecart", CommandBlockMinecartMeta::new);
        supplier.put("minecraft:furnace_minecart", FurnaceMinecartMeta::new);
        supplier.put("minecraft:hopper_minecart", HopperMinecartMeta::new);
        supplier.put("minecraft:spawner_minecart", SpawnerMinecartMeta::new);
        supplier.put("minecraft:tnt_minecart", TntMinecartMeta::new);
        supplier.put("minecraft:mule", MuleMeta::new);
        supplier.put("minecraft:mooshroom", MooshroomMeta::new);
        supplier.put("minecraft:ocelot", OcelotMeta::new);
        supplier.put("minecraft:painting", PaintingMeta::new);
        supplier.put("minecraft:panda", PandaMeta::new);
        supplier.put("minecraft:parrot", ParrotMeta::new);
        supplier.put("minecraft:phantom", PhantomMeta::new);
        supplier.put("minecraft:pig", PigMeta::new);
        supplier.put("minecraft:piglin", PiglinMeta::new);
        supplier.put("minecraft:piglin_brute", PiglinBruteMeta::new);
        supplier.put("minecraft:pillager", PillagerMeta::new);
        supplier.put("minecraft:polar_bear", PolarBearMeta::new);
        supplier.put("minecraft:tnt", PrimedTntMeta::new);
        supplier.put("minecraft:pufferfish", PufferfishMeta::new);
        supplier.put("minecraft:rabbit", RabbitMeta::new);
        supplier.put("minecraft:ravager", RavagerMeta::new);
        supplier.put("minecraft:salmon", SalmonMeta::new);
        supplier.put("minecraft:sheep", SheepMeta::new);
        supplier.put("minecraft:shulker", ShulkerMeta::new);
        supplier.put("minecraft:shulker_bullet", ShulkerBulletMeta::new);
        supplier.put("minecraft:silverfish", SilverfishMeta::new);
        supplier.put("minecraft:skeleton", SkeletonMeta::new);
        supplier.put("minecraft:skeleton_horse", SkeletonHorseMeta::new);
        supplier.put("minecraft:slime", SlimeMeta::new);
        supplier.put("minecraft:small_fireball", SmallFireballMeta::new);
        supplier.put("minecraft:snow_golem", SnowGolemMeta::new);
        supplier.put("minecraft:snowball", SnowballMeta::new);
        supplier.put("minecraft:spectral_arrow", SpectralArrowMeta::new);
        supplier.put("minecraft:spider", SpiderMeta::new);
        supplier.put("minecraft:squid", SquidMeta::new);
        supplier.put("minecraft:stray", StrayMeta::new);
        supplier.put("minecraft:strider", StriderMeta::new);
        supplier.put("minecraft:egg", ThrownEggMeta::new);
        supplier.put("minecraft:ender_pearl", ThrownEnderPearlMeta::new);
        supplier.put("minecraft:experience_bottle", ThrownExperienceBottleMeta::new);
        supplier.put("minecraft:potion", ThrownPotionMeta::new);
        supplier.put("minecraft:trident", ThrownTridentMeta::new);
        supplier.put("minecraft:trader_llama", TraderLlamaMeta::new);
        supplier.put("minecraft:tropical_fish", TropicalFishMeta::new);
        supplier.put("minecraft:turtle", TurtleMeta::new);
        supplier.put("minecraft:vex", VexMeta::new);
        supplier.put("minecraft:villager", VillagerMeta::new);
        supplier.put("minecraft:vindicator", VindicatorMeta::new);
        supplier.put("minecraft:wandering_trader", WanderingTraderMeta::new);
        supplier.put("minecraft:witch", WitchMeta::new);
        supplier.put("minecraft:wither", WitherMeta::new);
        supplier.put("minecraft:wither_skeleton", WitherSkeletonMeta::new);
        supplier.put("minecraft:wither_skull", WitherSkullMeta::new);
        supplier.put("minecraft:wolf", WolfMeta::new);
        supplier.put("minecraft:zoglin", ZoglinMeta::new);
        supplier.put("minecraft:zombie", ZombieMeta::new);
        supplier.put("minecraft:zombie_horse", ZombieHorseMeta::new);
        supplier.put("minecraft:zombie_villager", ZombieVillagerMeta::new);
        supplier.put("minecraft:zombified_piglin", ZombifiedPiglinMeta::new);
        supplier.put("minecraft:player", PlayerMeta::new);
        supplier.put("minecraft:fishing_bobber", FishingHookMeta::new);
        return supplier;
    }

    @Override
    public String toString() {
        return name();
    }
}
