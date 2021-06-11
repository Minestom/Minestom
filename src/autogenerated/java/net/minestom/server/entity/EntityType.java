package net.minestom.server.entity;

import java.util.function.BiFunction;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import net.minestom.server.entity.metadata.animal.BeeMeta;
import net.minestom.server.entity.metadata.animal.ChickenMeta;
import net.minestom.server.entity.metadata.animal.CowMeta;
import net.minestom.server.entity.metadata.animal.DonkeyMeta;
import net.minestom.server.entity.metadata.animal.FoxMeta;
import net.minestom.server.entity.metadata.animal.GoatMeta;
import net.minestom.server.entity.metadata.animal.HoglinMeta;
import net.minestom.server.entity.metadata.animal.HorseMeta;
import net.minestom.server.entity.metadata.animal.LlamaMeta;
import net.minestom.server.entity.metadata.animal.MooshroomMeta;
import net.minestom.server.entity.metadata.animal.MuleMeta;
import net.minestom.server.entity.metadata.animal.OcelotMeta;
import net.minestom.server.entity.metadata.animal.PandaMeta;
import net.minestom.server.entity.metadata.animal.PigMeta;
import net.minestom.server.entity.metadata.animal.PolarBearMeta;
import net.minestom.server.entity.metadata.animal.RabbitMeta;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.entity.metadata.animal.SkeletonHorseMeta;
import net.minestom.server.entity.metadata.animal.StriderMeta;
import net.minestom.server.entity.metadata.animal.TurtleMeta;
import net.minestom.server.entity.metadata.animal.ZombieHorseMeta;
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
import net.minestom.server.entity.metadata.item.EyeOfEnderMeta;
import net.minestom.server.entity.metadata.item.FireballMeta;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.entity.metadata.item.SmallFireballMeta;
import net.minestom.server.entity.metadata.item.SnowballMeta;
import net.minestom.server.entity.metadata.item.ThrownEggMeta;
import net.minestom.server.entity.metadata.item.ThrownEnderPearlMeta;
import net.minestom.server.entity.metadata.item.ThrownExperienceBottleMeta;
import net.minestom.server.entity.metadata.item.ThrownPotionMeta;
import net.minestom.server.entity.metadata.minecart.ChestMinecartMeta;
import net.minestom.server.entity.metadata.minecart.CommandBlockMinecartMeta;
import net.minestom.server.entity.metadata.minecart.FurnaceMinecartMeta;
import net.minestom.server.entity.metadata.minecart.HopperMinecartMeta;
import net.minestom.server.entity.metadata.minecart.MinecartMeta;
import net.minestom.server.entity.metadata.minecart.SpawnerMinecartMeta;
import net.minestom.server.entity.metadata.minecart.TntMinecartMeta;
import net.minestom.server.entity.metadata.monster.BlazeMeta;
import net.minestom.server.entity.metadata.monster.CaveSpiderMeta;
import net.minestom.server.entity.metadata.monster.CreeperMeta;
import net.minestom.server.entity.metadata.monster.ElderGuardianMeta;
import net.minestom.server.entity.metadata.monster.EndermanMeta;
import net.minestom.server.entity.metadata.monster.EndermiteMeta;
import net.minestom.server.entity.metadata.monster.GiantMeta;
import net.minestom.server.entity.metadata.monster.GuardianMeta;
import net.minestom.server.entity.metadata.monster.PiglinBruteMeta;
import net.minestom.server.entity.metadata.monster.PiglinMeta;
import net.minestom.server.entity.metadata.monster.SilverfishMeta;
import net.minestom.server.entity.metadata.monster.SpiderMeta;
import net.minestom.server.entity.metadata.monster.VexMeta;
import net.minestom.server.entity.metadata.monster.WitherMeta;
import net.minestom.server.entity.metadata.monster.ZoglinMeta;
import net.minestom.server.entity.metadata.monster.raider.EvokerMeta;
import net.minestom.server.entity.metadata.monster.raider.IllusionerMeta;
import net.minestom.server.entity.metadata.monster.raider.PillagerMeta;
import net.minestom.server.entity.metadata.monster.raider.RavagerMeta;
import net.minestom.server.entity.metadata.monster.raider.VindicatorMeta;
import net.minestom.server.entity.metadata.monster.raider.WitchMeta;
import net.minestom.server.entity.metadata.monster.skeleton.SkeletonMeta;
import net.minestom.server.entity.metadata.monster.skeleton.StrayMeta;
import net.minestom.server.entity.metadata.monster.skeleton.WitherSkeletonMeta;
import net.minestom.server.entity.metadata.monster.zombie.DrownedMeta;
import net.minestom.server.entity.metadata.monster.zombie.HuskMeta;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.entity.metadata.monster.zombie.ZombieVillagerMeta;
import net.minestom.server.entity.metadata.monster.zombie.ZombifiedPiglinMeta;
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.entity.metadata.other.BoatMeta;
import net.minestom.server.entity.metadata.other.DragonFireballMeta;
import net.minestom.server.entity.metadata.other.EndCrystalMeta;
import net.minestom.server.entity.metadata.other.EnderDragonMeta;
import net.minestom.server.entity.metadata.other.EvokerFangsMeta;
import net.minestom.server.entity.metadata.other.ExperienceOrbMeta;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.entity.metadata.other.FireworkRocketMeta;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.entity.metadata.other.GlowItemFrameMeta;
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.entity.metadata.other.LeashKnotMeta;
import net.minestom.server.entity.metadata.other.LightningBoltMeta;
import net.minestom.server.entity.metadata.other.LlamaSpitMeta;
import net.minestom.server.entity.metadata.other.MagmaCubeMeta;
import net.minestom.server.entity.metadata.other.MarkerMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.entity.metadata.other.ShulkerBulletMeta;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.entity.metadata.other.TraderLlamaMeta;
import net.minestom.server.entity.metadata.other.WitherSkullMeta;
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
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * AUTOGENERATED by EntityTypeGenerator
 */
public enum EntityType implements Keyed {
    AREA_EFFECT_CLOUD(NamespaceID.from("minecraft:area_effect_cloud"), 6.0, 0.5, AreaEffectCloudMeta::new, EntitySpawnType.BASE),

    ARMOR_STAND(NamespaceID.from("minecraft:armor_stand"), 0.5, 1.975, ArmorStandMeta::new, EntitySpawnType.LIVING),

    ARROW(NamespaceID.from("minecraft:arrow"), 0.5, 0.5, ArrowMeta::new, EntitySpawnType.BASE),

    AXOLOTL(NamespaceID.from("minecraft:axolotl"), 0.75, 0.42, AxolotlMeta::new, EntitySpawnType.LIVING),

    BAT(NamespaceID.from("minecraft:bat"), 0.5, 0.9, BatMeta::new, EntitySpawnType.LIVING),

    BEE(NamespaceID.from("minecraft:bee"), 0.7, 0.6, BeeMeta::new, EntitySpawnType.LIVING),

    BLAZE(NamespaceID.from("minecraft:blaze"), 0.6, 1.8, BlazeMeta::new, EntitySpawnType.LIVING),

    BOAT(NamespaceID.from("minecraft:boat"), 1.375, 0.5625, BoatMeta::new, EntitySpawnType.BASE),

    CAT(NamespaceID.from("minecraft:cat"), 0.6, 0.7, CatMeta::new, EntitySpawnType.LIVING),

    CAVE_SPIDER(NamespaceID.from("minecraft:cave_spider"), 0.7, 0.5, CaveSpiderMeta::new, EntitySpawnType.LIVING),

    CHICKEN(NamespaceID.from("minecraft:chicken"), 0.4, 0.7, ChickenMeta::new, EntitySpawnType.LIVING),

    COD(NamespaceID.from("minecraft:cod"), 0.5, 0.3, CodMeta::new, EntitySpawnType.LIVING),

    COW(NamespaceID.from("minecraft:cow"), 0.9, 1.4, CowMeta::new, EntitySpawnType.LIVING),

    CREEPER(NamespaceID.from("minecraft:creeper"), 0.6, 1.7, CreeperMeta::new, EntitySpawnType.LIVING),

    DOLPHIN(NamespaceID.from("minecraft:dolphin"), 0.9, 0.6, DolphinMeta::new, EntitySpawnType.LIVING),

    DONKEY(NamespaceID.from("minecraft:donkey"), 1.3964844, 1.5, DonkeyMeta::new, EntitySpawnType.LIVING),

    DRAGON_FIREBALL(NamespaceID.from("minecraft:dragon_fireball"), 1.0, 1.0, DragonFireballMeta::new, EntitySpawnType.BASE),

    DROWNED(NamespaceID.from("minecraft:drowned"), 0.6, 1.95, DrownedMeta::new, EntitySpawnType.LIVING),

    ELDER_GUARDIAN(NamespaceID.from("minecraft:elder_guardian"), 1.9975, 1.9975, ElderGuardianMeta::new, EntitySpawnType.LIVING),

    END_CRYSTAL(NamespaceID.from("minecraft:end_crystal"), 2.0, 2.0, EndCrystalMeta::new, EntitySpawnType.BASE),

    ENDER_DRAGON(NamespaceID.from("minecraft:ender_dragon"), 16.0, 8.0, EnderDragonMeta::new, EntitySpawnType.LIVING),

    ENDERMAN(NamespaceID.from("minecraft:enderman"), 0.6, 2.9, EndermanMeta::new, EntitySpawnType.LIVING),

    ENDERMITE(NamespaceID.from("minecraft:endermite"), 0.4, 0.3, EndermiteMeta::new, EntitySpawnType.LIVING),

    EVOKER(NamespaceID.from("minecraft:evoker"), 0.6, 1.95, EvokerMeta::new, EntitySpawnType.LIVING),

    EVOKER_FANGS(NamespaceID.from("minecraft:evoker_fangs"), 0.5, 0.8, EvokerFangsMeta::new, EntitySpawnType.BASE),

    EXPERIENCE_ORB(NamespaceID.from("minecraft:experience_orb"), 0.5, 0.5, ExperienceOrbMeta::new, EntitySpawnType.EXPERIENCE_ORB),

    EYE_OF_ENDER(NamespaceID.from("minecraft:eye_of_ender"), 0.25, 0.25, EyeOfEnderMeta::new, EntitySpawnType.BASE),

    FALLING_BLOCK(NamespaceID.from("minecraft:falling_block"), 0.98, 0.98, FallingBlockMeta::new, EntitySpawnType.BASE),

    FIREWORK_ROCKET(NamespaceID.from("minecraft:firework_rocket"), 0.25, 0.25, FireworkRocketMeta::new, EntitySpawnType.BASE),

    FOX(NamespaceID.from("minecraft:fox"), 0.6, 0.7, FoxMeta::new, EntitySpawnType.LIVING),

    GHAST(NamespaceID.from("minecraft:ghast"), 4.0, 4.0, GhastMeta::new, EntitySpawnType.LIVING),

    GIANT(NamespaceID.from("minecraft:giant"), 3.6, 12.0, GiantMeta::new, EntitySpawnType.LIVING),

    GLOW_ITEM_FRAME(NamespaceID.from("minecraft:glow_item_frame"), 0.5, 0.5, GlowItemFrameMeta::new, EntitySpawnType.BASE),

    GLOW_SQUID(NamespaceID.from("minecraft:glow_squid"), 0.8, 0.8, GlowSquidMeta::new, EntitySpawnType.LIVING),

    GOAT(NamespaceID.from("minecraft:goat"), 0.9, 1.3, GoatMeta::new, EntitySpawnType.LIVING),

    GUARDIAN(NamespaceID.from("minecraft:guardian"), 0.85, 0.85, GuardianMeta::new, EntitySpawnType.LIVING),

    HOGLIN(NamespaceID.from("minecraft:hoglin"), 1.3964844, 1.4, HoglinMeta::new, EntitySpawnType.LIVING),

    HORSE(NamespaceID.from("minecraft:horse"), 1.3964844, 1.6, HorseMeta::new, EntitySpawnType.LIVING),

    HUSK(NamespaceID.from("minecraft:husk"), 0.6, 1.95, HuskMeta::new, EntitySpawnType.LIVING),

    ILLUSIONER(NamespaceID.from("minecraft:illusioner"), 0.6, 1.95, IllusionerMeta::new, EntitySpawnType.LIVING),

    IRON_GOLEM(NamespaceID.from("minecraft:iron_golem"), 1.4, 2.7, IronGolemMeta::new, EntitySpawnType.LIVING),

    ITEM(NamespaceID.from("minecraft:item"), 0.25, 0.25, ItemEntityMeta::new, EntitySpawnType.BASE),

    ITEM_FRAME(NamespaceID.from("minecraft:item_frame"), 0.5, 0.5, ItemFrameMeta::new, EntitySpawnType.BASE),

    FIREBALL(NamespaceID.from("minecraft:fireball"), 1.0, 1.0, FireballMeta::new, EntitySpawnType.BASE),

    LEASH_KNOT(NamespaceID.from("minecraft:leash_knot"), 0.375, 0.5, LeashKnotMeta::new, EntitySpawnType.BASE),

    LIGHTNING_BOLT(NamespaceID.from("minecraft:lightning_bolt"), 0.0, 0.0, LightningBoltMeta::new, EntitySpawnType.BASE),

    LLAMA(NamespaceID.from("minecraft:llama"), 0.9, 1.87, LlamaMeta::new, EntitySpawnType.LIVING),

    LLAMA_SPIT(NamespaceID.from("minecraft:llama_spit"), 0.25, 0.25, LlamaSpitMeta::new, EntitySpawnType.BASE),

    MAGMA_CUBE(NamespaceID.from("minecraft:magma_cube"), 2.04, 2.04, MagmaCubeMeta::new, EntitySpawnType.LIVING),

    MARKER(NamespaceID.from("minecraft:marker"), 0.0, 0.0, MarkerMeta::new, EntitySpawnType.BASE),

    MINECART(NamespaceID.from("minecraft:minecart"), 0.98, 0.7, MinecartMeta::new, EntitySpawnType.BASE),

    CHEST_MINECART(NamespaceID.from("minecraft:chest_minecart"), 0.98, 0.7, ChestMinecartMeta::new, EntitySpawnType.BASE),

    COMMAND_BLOCK_MINECART(NamespaceID.from("minecraft:command_block_minecart"), 0.98, 0.7, CommandBlockMinecartMeta::new, EntitySpawnType.BASE),

    FURNACE_MINECART(NamespaceID.from("minecraft:furnace_minecart"), 0.98, 0.7, FurnaceMinecartMeta::new, EntitySpawnType.BASE),

    HOPPER_MINECART(NamespaceID.from("minecraft:hopper_minecart"), 0.98, 0.7, HopperMinecartMeta::new, EntitySpawnType.BASE),

    SPAWNER_MINECART(NamespaceID.from("minecraft:spawner_minecart"), 0.98, 0.7, SpawnerMinecartMeta::new, EntitySpawnType.BASE),

    TNT_MINECART(NamespaceID.from("minecraft:tnt_minecart"), 0.98, 0.7, TntMinecartMeta::new, EntitySpawnType.BASE),

    MULE(NamespaceID.from("minecraft:mule"), 1.3964844, 1.6, MuleMeta::new, EntitySpawnType.LIVING),

    MOOSHROOM(NamespaceID.from("minecraft:mooshroom"), 0.9, 1.4, MooshroomMeta::new, EntitySpawnType.LIVING),

    OCELOT(NamespaceID.from("minecraft:ocelot"), 0.6, 0.7, OcelotMeta::new, EntitySpawnType.LIVING),

    PAINTING(NamespaceID.from("minecraft:painting"), 0.5, 0.5, PaintingMeta::new, EntitySpawnType.PAINTING),

    PANDA(NamespaceID.from("minecraft:panda"), 1.3, 1.25, PandaMeta::new, EntitySpawnType.LIVING),

    PARROT(NamespaceID.from("minecraft:parrot"), 0.5, 0.9, ParrotMeta::new, EntitySpawnType.LIVING),

    PHANTOM(NamespaceID.from("minecraft:phantom"), 0.9, 0.5, PhantomMeta::new, EntitySpawnType.LIVING),

    PIG(NamespaceID.from("minecraft:pig"), 0.9, 0.9, PigMeta::new, EntitySpawnType.LIVING),

    PIGLIN(NamespaceID.from("minecraft:piglin"), 0.6, 1.95, PiglinMeta::new, EntitySpawnType.LIVING),

    PIGLIN_BRUTE(NamespaceID.from("minecraft:piglin_brute"), 0.6, 1.95, PiglinBruteMeta::new, EntitySpawnType.LIVING),

    PILLAGER(NamespaceID.from("minecraft:pillager"), 0.6, 1.95, PillagerMeta::new, EntitySpawnType.LIVING),

    POLAR_BEAR(NamespaceID.from("minecraft:polar_bear"), 1.4, 1.4, PolarBearMeta::new, EntitySpawnType.LIVING),

    TNT(NamespaceID.from("minecraft:tnt"), 0.98, 0.98, PrimedTntMeta::new, EntitySpawnType.BASE),

    PUFFERFISH(NamespaceID.from("minecraft:pufferfish"), 0.7, 0.7, PufferfishMeta::new, EntitySpawnType.LIVING),

    RABBIT(NamespaceID.from("minecraft:rabbit"), 0.4, 0.5, RabbitMeta::new, EntitySpawnType.LIVING),

    RAVAGER(NamespaceID.from("minecraft:ravager"), 1.95, 2.2, RavagerMeta::new, EntitySpawnType.LIVING),

    SALMON(NamespaceID.from("minecraft:salmon"), 0.7, 0.4, SalmonMeta::new, EntitySpawnType.LIVING),

    SHEEP(NamespaceID.from("minecraft:sheep"), 0.9, 1.3, SheepMeta::new, EntitySpawnType.LIVING),

    SHULKER(NamespaceID.from("minecraft:shulker"), 1.0, 1.0, ShulkerMeta::new, EntitySpawnType.LIVING),

    SHULKER_BULLET(NamespaceID.from("minecraft:shulker_bullet"), 0.3125, 0.3125, ShulkerBulletMeta::new, EntitySpawnType.BASE),

    SILVERFISH(NamespaceID.from("minecraft:silverfish"), 0.4, 0.3, SilverfishMeta::new, EntitySpawnType.LIVING),

    SKELETON(NamespaceID.from("minecraft:skeleton"), 0.6, 1.99, SkeletonMeta::new, EntitySpawnType.LIVING),

    SKELETON_HORSE(NamespaceID.from("minecraft:skeleton_horse"), 1.3964844, 1.6, SkeletonHorseMeta::new, EntitySpawnType.LIVING),

    SLIME(NamespaceID.from("minecraft:slime"), 2.04, 2.04, SlimeMeta::new, EntitySpawnType.LIVING),

    SMALL_FIREBALL(NamespaceID.from("minecraft:small_fireball"), 0.3125, 0.3125, SmallFireballMeta::new, EntitySpawnType.BASE),

    SNOW_GOLEM(NamespaceID.from("minecraft:snow_golem"), 0.7, 1.9, SnowGolemMeta::new, EntitySpawnType.LIVING),

    SNOWBALL(NamespaceID.from("minecraft:snowball"), 0.25, 0.25, SnowballMeta::new, EntitySpawnType.BASE),

    SPECTRAL_ARROW(NamespaceID.from("minecraft:spectral_arrow"), 0.5, 0.5, SpectralArrowMeta::new, EntitySpawnType.BASE),

    SPIDER(NamespaceID.from("minecraft:spider"), 1.4, 0.9, SpiderMeta::new, EntitySpawnType.LIVING),

    SQUID(NamespaceID.from("minecraft:squid"), 0.8, 0.8, SquidMeta::new, EntitySpawnType.LIVING),

    STRAY(NamespaceID.from("minecraft:stray"), 0.6, 1.99, StrayMeta::new, EntitySpawnType.LIVING),

    STRIDER(NamespaceID.from("minecraft:strider"), 0.9, 1.7, StriderMeta::new, EntitySpawnType.LIVING),

    EGG(NamespaceID.from("minecraft:egg"), 0.25, 0.25, ThrownEggMeta::new, EntitySpawnType.BASE),

    ENDER_PEARL(NamespaceID.from("minecraft:ender_pearl"), 0.25, 0.25, ThrownEnderPearlMeta::new, EntitySpawnType.BASE),

    EXPERIENCE_BOTTLE(NamespaceID.from("minecraft:experience_bottle"), 0.25, 0.25, ThrownExperienceBottleMeta::new, EntitySpawnType.BASE),

    POTION(NamespaceID.from("minecraft:potion"), 0.25, 0.25, ThrownPotionMeta::new, EntitySpawnType.BASE),

    TRIDENT(NamespaceID.from("minecraft:trident"), 0.5, 0.5, ThrownTridentMeta::new, EntitySpawnType.BASE),

    TRADER_LLAMA(NamespaceID.from("minecraft:trader_llama"), 0.9, 1.87, TraderLlamaMeta::new, EntitySpawnType.LIVING),

    TROPICAL_FISH(NamespaceID.from("minecraft:tropical_fish"), 0.5, 0.4, TropicalFishMeta::new, EntitySpawnType.LIVING),

    TURTLE(NamespaceID.from("minecraft:turtle"), 1.2, 0.4, TurtleMeta::new, EntitySpawnType.LIVING),

    VEX(NamespaceID.from("minecraft:vex"), 0.4, 0.8, VexMeta::new, EntitySpawnType.LIVING),

    VILLAGER(NamespaceID.from("minecraft:villager"), 0.6, 1.95, VillagerMeta::new, EntitySpawnType.LIVING),

    VINDICATOR(NamespaceID.from("minecraft:vindicator"), 0.6, 1.95, VindicatorMeta::new, EntitySpawnType.LIVING),

    WANDERING_TRADER(NamespaceID.from("minecraft:wandering_trader"), 0.6, 1.95, WanderingTraderMeta::new, EntitySpawnType.LIVING),

    WITCH(NamespaceID.from("minecraft:witch"), 0.6, 1.95, WitchMeta::new, EntitySpawnType.LIVING),

    WITHER(NamespaceID.from("minecraft:wither"), 0.9, 3.5, WitherMeta::new, EntitySpawnType.LIVING),

    WITHER_SKELETON(NamespaceID.from("minecraft:wither_skeleton"), 0.7, 2.4, WitherSkeletonMeta::new, EntitySpawnType.LIVING),

    WITHER_SKULL(NamespaceID.from("minecraft:wither_skull"), 0.3125, 0.3125, WitherSkullMeta::new, EntitySpawnType.BASE),

    WOLF(NamespaceID.from("minecraft:wolf"), 0.6, 0.85, WolfMeta::new, EntitySpawnType.LIVING),

    ZOGLIN(NamespaceID.from("minecraft:zoglin"), 1.3964844, 1.4, ZoglinMeta::new, EntitySpawnType.LIVING),

    ZOMBIE(NamespaceID.from("minecraft:zombie"), 0.6, 1.95, ZombieMeta::new, EntitySpawnType.LIVING),

    ZOMBIE_HORSE(NamespaceID.from("minecraft:zombie_horse"), 1.3964844, 1.6, ZombieHorseMeta::new, EntitySpawnType.LIVING),

    ZOMBIE_VILLAGER(NamespaceID.from("minecraft:zombie_villager"), 0.6, 1.95, ZombieVillagerMeta::new, EntitySpawnType.LIVING),

    ZOMBIFIED_PIGLIN(NamespaceID.from("minecraft:zombified_piglin"), 0.6, 1.95, ZombifiedPiglinMeta::new, EntitySpawnType.LIVING),

    PLAYER(NamespaceID.from("minecraft:player"), 0.6, 1.8, PlayerMeta::new, EntitySpawnType.PLAYER),

    FISHING_BOBBER(NamespaceID.from("minecraft:fishing_bobber"), 0.25, 0.25, FishingHookMeta::new, EntitySpawnType.BASE);

    private static final EntityType[] VALUES = values();

    @NotNull
    private final NamespaceID id;

    private final double width;

    private final double height;

    @NotNull
    private final BiFunction<Entity, Metadata, EntityMeta> metaConstructor;

    @NotNull
    private final EntitySpawnType spawnType;

    EntityType(@NotNull NamespaceID id, double width, double height,
            @NotNull BiFunction<Entity, Metadata, EntityMeta> metaConstructor,
            @NotNull EntitySpawnType spawnType) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.metaConstructor = metaConstructor;
        this.spawnType = spawnType;
        Registries.entityTypes.put(id, this);
    }

    @Override
    @NotNull
    public Key key() {
        return this.id;
    }

    public short getId() {
        return (short) ordinal();
    }

    @NotNull
    public NamespaceID getNamespaceID() {
        return this.id;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public BiFunction<Entity, Metadata, EntityMeta> getMetaConstructor() {
        return this.metaConstructor;
    }

    public EntitySpawnType getSpawnType() {
        return this.spawnType;
    }

    @Nullable
    public static EntityType fromId(short id) {
        if(id >= 0 && id < VALUES.length) {
            return VALUES[id];
        }
        return null;
    }

    @NotNull
    @Override
    public String toString() {
        return "[" + this.id + "]";
    }
}
