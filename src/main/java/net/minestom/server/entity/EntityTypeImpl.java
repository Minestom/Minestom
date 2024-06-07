package net.minestom.server.entity;

import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.ParrotMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.entity.metadata.flying.GhastMeta;
import net.minestom.server.entity.metadata.flying.PhantomMeta;
import net.minestom.server.entity.metadata.golem.IronGolemMeta;
import net.minestom.server.entity.metadata.golem.ShulkerMeta;
import net.minestom.server.entity.metadata.golem.SnowGolemMeta;
import net.minestom.server.entity.metadata.item.*;
import net.minestom.server.entity.metadata.minecart.*;
import net.minestom.server.entity.metadata.monster.*;
import net.minestom.server.entity.metadata.monster.raider.*;
import net.minestom.server.entity.metadata.monster.skeleton.BoggedMeta;
import net.minestom.server.entity.metadata.monster.skeleton.SkeletonMeta;
import net.minestom.server.entity.metadata.monster.skeleton.StrayMeta;
import net.minestom.server.entity.metadata.monster.skeleton.WitherSkeletonMeta;
import net.minestom.server.entity.metadata.monster.zombie.*;
import net.minestom.server.entity.metadata.other.*;
import net.minestom.server.entity.metadata.projectile.*;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.entity.metadata.villager.WanderingTraderMeta;
import net.minestom.server.entity.metadata.water.AxolotlMeta;
import net.minestom.server.entity.metadata.water.DolphinMeta;
import net.minestom.server.entity.metadata.water.GlowSquidMeta;
import net.minestom.server.entity.metadata.water.SquidMeta;
import net.minestom.server.entity.metadata.water.fish.*;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

record EntityTypeImpl(Registry.EntityEntry registry) implements EntityType {
    private static final Registry.Container<EntityType> CONTAINER = Registry.createStaticContainer(Registry.Resource.ENTITIES,
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
                Map.entry("minecraft:allay", AllayMeta::new),
                Map.entry("minecraft:area_effect_cloud", AreaEffectCloudMeta::new),
                Map.entry("minecraft:armadillo", ArmadilloMeta::new),
                Map.entry("minecraft:armor_stand", ArmorStandMeta::new),
                Map.entry("minecraft:arrow", ArrowMeta::new),
                Map.entry("minecraft:axolotl", AxolotlMeta::new),
                Map.entry("minecraft:bat", BatMeta::new),
                Map.entry("minecraft:bee", BeeMeta::new),
                Map.entry("minecraft:blaze", BlazeMeta::new),
                Map.entry("minecraft:block_display", BlockDisplayMeta::new),
                Map.entry("minecraft:boat", BoatMeta::new),
                Map.entry("minecraft:bogged", BoggedMeta::new),
                Map.entry("minecraft:breeze", BreezeMeta::new),
                Map.entry("minecraft:breeze_wind_charge", BreezeWindChargeMeta::new),
                Map.entry("minecraft:chest_boat", BoatMeta::new),
                Map.entry("minecraft:camel", CamelMeta::new),
                Map.entry("minecraft:cat", CatMeta::new),
                Map.entry("minecraft:cave_spider", CaveSpiderMeta::new),
                Map.entry("minecraft:chicken", ChickenMeta::new),
                Map.entry("minecraft:cod", CodMeta::new),
                Map.entry("minecraft:cow", CowMeta::new),
                Map.entry("minecraft:creeper", CreeperMeta::new),
                Map.entry("minecraft:dolphin", DolphinMeta::new),
                Map.entry("minecraft:donkey", DonkeyMeta::new),
                Map.entry("minecraft:dragon_fireball", DragonFireballMeta::new),
                Map.entry("minecraft:drowned", DrownedMeta::new),
                Map.entry("minecraft:elder_guardian", ElderGuardianMeta::new),
                Map.entry("minecraft:end_crystal", EndCrystalMeta::new),
                Map.entry("minecraft:ender_dragon", EnderDragonMeta::new),
                Map.entry("minecraft:enderman", EndermanMeta::new),
                Map.entry("minecraft:endermite", EndermiteMeta::new),
                Map.entry("minecraft:evoker", EvokerMeta::new),
                Map.entry("minecraft:evoker_fangs", EvokerFangsMeta::new),
                Map.entry("minecraft:experience_orb", ExperienceOrbMeta::new),
                Map.entry("minecraft:eye_of_ender", EyeOfEnderMeta::new),
                Map.entry("minecraft:falling_block", FallingBlockMeta::new),
                Map.entry("minecraft:firework_rocket", FireworkRocketMeta::new),
                Map.entry("minecraft:fox", FoxMeta::new),
                Map.entry("minecraft:frog", FrogMeta::new),
                Map.entry("minecraft:ghast", GhastMeta::new),
                Map.entry("minecraft:giant", GiantMeta::new),
                Map.entry("minecraft:glow_item_frame", GlowItemFrameMeta::new),
                Map.entry("minecraft:glow_squid", GlowSquidMeta::new),
                Map.entry("minecraft:goat", GoatMeta::new),
                Map.entry("minecraft:guardian", GuardianMeta::new),
                Map.entry("minecraft:hoglin", HoglinMeta::new),
                Map.entry("minecraft:horse", HorseMeta::new),
                Map.entry("minecraft:husk", HuskMeta::new),
                Map.entry("minecraft:illusioner", IllusionerMeta::new),
                Map.entry("minecraft:interaction", InteractionMeta::new),
                Map.entry("minecraft:iron_golem", IronGolemMeta::new),
                Map.entry("minecraft:item", ItemEntityMeta::new),
                Map.entry("minecraft:item_display", ItemDisplayMeta::new),
                Map.entry("minecraft:item_frame", ItemFrameMeta::new),
                Map.entry("minecraft:fireball", FireballMeta::new),
                Map.entry("minecraft:leash_knot", LeashKnotMeta::new),
                Map.entry("minecraft:lightning_bolt", LightningBoltMeta::new),
                Map.entry("minecraft:llama", LlamaMeta::new),
                Map.entry("minecraft:llama_spit", LlamaSpitMeta::new),
                Map.entry("minecraft:magma_cube", MagmaCubeMeta::new),
                Map.entry("minecraft:marker", MarkerMeta::new),
                Map.entry("minecraft:minecart", MinecartMeta::new),
                Map.entry("minecraft:chest_minecart", ChestMinecartMeta::new),
                Map.entry("minecraft:command_block_minecart", CommandBlockMinecartMeta::new),
                Map.entry("minecraft:furnace_minecart", FurnaceMinecartMeta::new),
                Map.entry("minecraft:hopper_minecart", HopperMinecartMeta::new),
                Map.entry("minecraft:spawner_minecart", SpawnerMinecartMeta::new),
                Map.entry("minecraft:text_display", TextDisplayMeta::new),
                Map.entry("minecraft:tnt_minecart", TntMinecartMeta::new),
                Map.entry("minecraft:mule", MuleMeta::new),
                Map.entry("minecraft:mooshroom", MooshroomMeta::new),
                Map.entry("minecraft:ocelot", OcelotMeta::new),
                Map.entry("minecraft:ominous_item_spawner", OminousItemSpawnerMeta::new),
                Map.entry("minecraft:painting", PaintingMeta::new),
                Map.entry("minecraft:panda", PandaMeta::new),
                Map.entry("minecraft:parrot", ParrotMeta::new),
                Map.entry("minecraft:phantom", PhantomMeta::new),
                Map.entry("minecraft:pig", PigMeta::new),
                Map.entry("minecraft:piglin", PiglinMeta::new),
                Map.entry("minecraft:piglin_brute", PiglinBruteMeta::new),
                Map.entry("minecraft:pillager", PillagerMeta::new),
                Map.entry("minecraft:polar_bear", PolarBearMeta::new),
                Map.entry("minecraft:tnt", PrimedTntMeta::new),
                Map.entry("minecraft:pufferfish", PufferfishMeta::new),
                Map.entry("minecraft:rabbit", RabbitMeta::new),
                Map.entry("minecraft:ravager", RavagerMeta::new),
                Map.entry("minecraft:salmon", SalmonMeta::new),
                Map.entry("minecraft:sheep", SheepMeta::new),
                Map.entry("minecraft:shulker", ShulkerMeta::new),
                Map.entry("minecraft:shulker_bullet", ShulkerBulletMeta::new),
                Map.entry("minecraft:silverfish", SilverfishMeta::new),
                Map.entry("minecraft:skeleton", SkeletonMeta::new),
                Map.entry("minecraft:skeleton_horse", SkeletonHorseMeta::new),
                Map.entry("minecraft:slime", SlimeMeta::new),
                Map.entry("minecraft:small_fireball", SmallFireballMeta::new),
                Map.entry("minecraft:sniffer", SnifferMeta::new),
                Map.entry("minecraft:snow_golem", SnowGolemMeta::new),
                Map.entry("minecraft:snowball", SnowballMeta::new),
                Map.entry("minecraft:spectral_arrow", SpectralArrowMeta::new),
                Map.entry("minecraft:spider", SpiderMeta::new),
                Map.entry("minecraft:squid", SquidMeta::new),
                Map.entry("minecraft:stray", StrayMeta::new),
                Map.entry("minecraft:strider", StriderMeta::new),
                Map.entry("minecraft:tadpole", TadpoleMeta::new),
                Map.entry("minecraft:egg", ThrownEggMeta::new),
                Map.entry("minecraft:ender_pearl", ThrownEnderPearlMeta::new),
                Map.entry("minecraft:experience_bottle", ThrownExperienceBottleMeta::new),
                Map.entry("minecraft:potion", ThrownPotionMeta::new),
                Map.entry("minecraft:trident", ThrownTridentMeta::new),
                Map.entry("minecraft:trader_llama", TraderLlamaMeta::new),
                Map.entry("minecraft:tropical_fish", TropicalFishMeta::new),
                Map.entry("minecraft:turtle", TurtleMeta::new),
                Map.entry("minecraft:vex", VexMeta::new),
                Map.entry("minecraft:villager", VillagerMeta::new),
                Map.entry("minecraft:vindicator", VindicatorMeta::new),
                Map.entry("minecraft:wandering_trader", WanderingTraderMeta::new),
                Map.entry("minecraft:warden", WardenMeta::new),
                Map.entry("minecraft:wind_charge", WindChargeMeta::new),
                Map.entry("minecraft:witch", WitchMeta::new),
                Map.entry("minecraft:wither", WitherMeta::new),
                Map.entry("minecraft:wither_skeleton", WitherSkeletonMeta::new),
                Map.entry("minecraft:wither_skull", WitherSkullMeta::new),
                Map.entry("minecraft:wolf", WolfMeta::new),
                Map.entry("minecraft:zoglin", ZoglinMeta::new),
                Map.entry("minecraft:zombie", ZombieMeta::new),
                Map.entry("minecraft:zombie_horse", ZombieHorseMeta::new),
                Map.entry("minecraft:zombie_villager", ZombieVillagerMeta::new),
                Map.entry("minecraft:zombified_piglin", ZombifiedPiglinMeta::new),
                Map.entry("minecraft:player", PlayerMeta::new),
                Map.entry("minecraft:fishing_bobber", FishingHookMeta::new)
        );
    }
}
