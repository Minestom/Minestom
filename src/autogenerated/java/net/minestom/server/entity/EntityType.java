package net.minestom.server.entity;

import java.util.function.BiFunction;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import net.minestom.server.entity.metadata.animal.BeeMeta;
import net.minestom.server.entity.metadata.animal.ChickenMeta;
import net.minestom.server.entity.metadata.animal.CowMeta;
import net.minestom.server.entity.metadata.animal.DonkeyMeta;
import net.minestom.server.entity.metadata.animal.FoxMeta;
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
import net.minestom.server.entity.metadata.other.ItemFrameMeta;
import net.minestom.server.entity.metadata.other.LeashKnotMeta;
import net.minestom.server.entity.metadata.other.LightningBoltMeta;
import net.minestom.server.entity.metadata.other.LlamaSpitMeta;
import net.minestom.server.entity.metadata.other.MagmaCubeMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.entity.metadata.other.ShulkerBulletMeta;
import net.minestom.server.entity.metadata.other.SlimeMeta;
import net.minestom.server.entity.metadata.other.TraderLlamaMeta;
import net.minestom.server.entity.metadata.other.WitherSkullMeta;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.entity.metadata.villager.WanderingTraderMeta;
import net.minestom.server.entity.metadata.water.DolphinMeta;
import net.minestom.server.entity.metadata.water.SquidMeta;
import net.minestom.server.entity.metadata.water.fish.CodMeta;
import net.minestom.server.entity.metadata.water.fish.PufferfishMeta;
import net.minestom.server.entity.metadata.water.fish.SalmonMeta;
import net.minestom.server.entity.metadata.water.fish.TropicalFishMeta;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

/**
 * //==============================
 * //  AUTOGENERATED BY EnumGenerator
 * //==============================
 */
@SuppressWarnings({"deprecation"})
public enum EntityType {
    AREA_EFFECT_CLOUD("minecraft:area_effect_cloud", 6.0, 0.5, AreaEffectCloudMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    ARMOR_STAND("minecraft:armor_stand", 0.5, 1.975, ArmorStandMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ARROW("minecraft:arrow", 0.5, 0.5, ArrowMeta::new, EntitySpawnType.BASE, 0.05, 0.01),

    BAT("minecraft:bat", 0.5, 0.9, BatMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    BEE("minecraft:bee", 0.7, 0.6, BeeMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    BLAZE("minecraft:blaze", 0.6, 1.8, BlazeMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    BOAT("minecraft:boat", 1.375, 0.5625, BoatMeta::new, EntitySpawnType.BASE, 0.04, 0.0),

    CAT("minecraft:cat", 0.6, 0.7, CatMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    CAVE_SPIDER("minecraft:cave_spider", 0.7, 0.5, CaveSpiderMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    CHICKEN("minecraft:chicken", 0.4, 0.7, ChickenMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    COD("minecraft:cod", 0.5, 0.3, CodMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    COW("minecraft:cow", 0.9, 1.4, CowMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    CREEPER("minecraft:creeper", 0.6, 1.7, CreeperMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    DOLPHIN("minecraft:dolphin", 0.9, 0.6, DolphinMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    DONKEY("minecraft:donkey", 1.39648, 1.5, DonkeyMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    DRAGON_FIREBALL("minecraft:dragon_fireball", 1.0, 1.0, DragonFireballMeta::new, EntitySpawnType.BASE, 0.1, 0.02),

    DROWNED("minecraft:drowned", 0.6, 1.95, DrownedMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ELDER_GUARDIAN("minecraft:elder_guardian", 1.9975, 1.9975, ElderGuardianMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    END_CRYSTAL("minecraft:end_crystal", 2.0, 2.0, EndCrystalMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    ENDER_DRAGON("minecraft:ender_dragon", 16.0, 8.0, EnderDragonMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ENDERMAN("minecraft:enderman", 0.6, 2.9, EndermanMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ENDERMITE("minecraft:endermite", 0.4, 0.3, EndermiteMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    EVOKER("minecraft:evoker", 0.6, 1.95, EvokerMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    EVOKER_FANGS("minecraft:evoker_fangs", 0.5, 0.8, EvokerFangsMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    EXPERIENCE_ORB("minecraft:experience_orb", 0.5, 0.5, ExperienceOrbMeta::new, EntitySpawnType.EXPERIENCE_ORB, 0.03, 0.02),

    EYE_OF_ENDER("minecraft:eye_of_ender", 0.25, 0.25, EyeOfEnderMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    FALLING_BLOCK("minecraft:falling_block", 0.98, 0.98, FallingBlockMeta::new, EntitySpawnType.BASE, 0.04, 0.02),

    FIREWORK_ROCKET("minecraft:firework_rocket", 0.25, 0.25, FireworkRocketMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    FOX("minecraft:fox", 0.6, 0.7, FoxMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    GHAST("minecraft:ghast", 4.0, 4.0, GhastMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    GIANT("minecraft:giant", 3.6, 12.0, GiantMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    GUARDIAN("minecraft:guardian", 0.85, 0.85, GuardianMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    HOGLIN("minecraft:hoglin", 1.39648, 1.4, HoglinMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    HORSE("minecraft:horse", 1.39648, 1.6, HorseMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    HUSK("minecraft:husk", 0.6, 1.95, HuskMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ILLUSIONER("minecraft:illusioner", 0.6, 1.95, IllusionerMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    IRON_GOLEM("minecraft:iron_golem", 1.4, 2.7, IronGolemMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ITEM("minecraft:item", 0.25, 0.25, ItemEntityMeta::new, EntitySpawnType.BASE, 0.04, 0.02),

    ITEM_FRAME("minecraft:item_frame", 0.5, 0.5, ItemFrameMeta::new, EntitySpawnType.BASE, 0.0, 0.02),

    FIREBALL("minecraft:fireball", 1.0, 1.0, FireballMeta::new, EntitySpawnType.BASE, 0.1, 0.02),

    LEASH_KNOT("minecraft:leash_knot", 0.5, 0.5, LeashKnotMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    LIGHTNING_BOLT("minecraft:lightning_bolt", 0.0, 0.0, LightningBoltMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    LLAMA("minecraft:llama", 0.9, 1.87, LlamaMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    LLAMA_SPIT("minecraft:llama_spit", 0.25, 0.25, LlamaSpitMeta::new, EntitySpawnType.BASE, 0.06, 0.01),

    MAGMA_CUBE("minecraft:magma_cube", 2.04, 2.04, MagmaCubeMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    MINECART("minecraft:minecart", 0.98, 0.7, MinecartMeta::new, EntitySpawnType.BASE, 0.04, 0.05),

    CHEST_MINECART("minecraft:chest_minecart", 0.98, 0.7, ChestMinecartMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    COMMAND_BLOCK_MINECART("minecraft:command_block_minecart", 0.98, 0.7, CommandBlockMinecartMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    FURNACE_MINECART("minecraft:furnace_minecart", 0.98, 0.7, FurnaceMinecartMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    HOPPER_MINECART("minecraft:hopper_minecart", 0.98, 0.7, HopperMinecartMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    SPAWNER_MINECART("minecraft:spawner_minecart", 0.98, 0.7, SpawnerMinecartMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    TNT_MINECART("minecraft:tnt_minecart", 0.98, 0.7, TntMinecartMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    MULE("minecraft:mule", 1.39648, 1.6, MuleMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    MOOSHROOM("minecraft:mooshroom", 0.9, 1.4, MooshroomMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    OCELOT("minecraft:ocelot", 0.6, 0.7, OcelotMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    PAINTING("minecraft:painting", 0.5, 0.5, PaintingMeta::new, EntitySpawnType.PAINTING, 0.0784000015258789, 0.02),

    PANDA("minecraft:panda", 1.3, 1.25, PandaMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    PARROT("minecraft:parrot", 0.5, 0.9, ParrotMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    PHANTOM("minecraft:phantom", 0.9, 0.5, PhantomMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    PIG("minecraft:pig", 0.9, 0.9, PigMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    PIGLIN("minecraft:piglin", 0.6, 1.95, PiglinMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    PIGLIN_BRUTE("minecraft:piglin_brute", 0.6, 1.95, PiglinBruteMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    PILLAGER("minecraft:pillager", 0.6, 1.95, PillagerMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    POLAR_BEAR("minecraft:polar_bear", 1.4, 1.4, PolarBearMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    TNT("minecraft:tnt", 0.98, 0.98, PrimedTntMeta::new, EntitySpawnType.BASE, 0.04, 0.02),

    PUFFERFISH("minecraft:pufferfish", 0.7, 0.7, PufferfishMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    RABBIT("minecraft:rabbit", 0.4, 0.5, RabbitMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    RAVAGER("minecraft:ravager", 1.95, 2.2, RavagerMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    SALMON("minecraft:salmon", 0.7, 0.4, SalmonMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    SHEEP("minecraft:sheep", 0.9, 1.3, SheepMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    SHULKER("minecraft:shulker", 1.0, 1.0, ShulkerMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    SHULKER_BULLET("minecraft:shulker_bullet", 0.3125, 0.3125, ShulkerBulletMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    SILVERFISH("minecraft:silverfish", 0.4, 0.3, SilverfishMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    SKELETON("minecraft:skeleton", 0.6, 1.99, SkeletonMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    SKELETON_HORSE("minecraft:skeleton_horse", 1.39648, 1.6, SkeletonHorseMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    SLIME("minecraft:slime", 2.04, 2.04, SlimeMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    SMALL_FIREBALL("minecraft:small_fireball", 0.3125, 0.3125, SmallFireballMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    SNOW_GOLEM("minecraft:snow_golem", 0.7, 1.9, SnowGolemMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    SNOWBALL("minecraft:snowball", 0.25, 0.25, SnowballMeta::new, EntitySpawnType.BASE, 0.03, 0.01),

    SPECTRAL_ARROW("minecraft:spectral_arrow", 0.5, 0.5, SpectralArrowMeta::new, EntitySpawnType.BASE, 0.05, 0.01),

    SPIDER("minecraft:spider", 1.4, 0.9, SpiderMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    SQUID("minecraft:squid", 0.8, 0.8, SquidMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    STRAY("minecraft:stray", 0.6, 1.99, StrayMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    STRIDER("minecraft:strider", 0.9, 1.7, StriderMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    EGG("minecraft:egg", 0.25, 0.25, ThrownEggMeta::new, EntitySpawnType.BASE, 0.03, 0.01),

    ENDER_PEARL("minecraft:ender_pearl", 0.25, 0.25, ThrownEnderPearlMeta::new, EntitySpawnType.BASE, 0.03, 0.01),

    EXPERIENCE_BOTTLE("minecraft:experience_bottle", 0.25, 0.25, ThrownExperienceBottleMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    POTION("minecraft:potion", 0.25, 0.25, ThrownPotionMeta::new, EntitySpawnType.BASE, 0.03, 0.01),

    TRIDENT("minecraft:trident", 0.5, 0.5, ThrownTridentMeta::new, EntitySpawnType.BASE, 0.05, 0.01),

    TRADER_LLAMA("minecraft:trader_llama", 0.9, 1.87, TraderLlamaMeta::new, EntitySpawnType.BASE, 0.0784000015258789, 0.02),

    TROPICAL_FISH("minecraft:tropical_fish", 0.5, 0.4, TropicalFishMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    TURTLE("minecraft:turtle", 1.2, 0.4, TurtleMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    VEX("minecraft:vex", 0.4, 0.8, VexMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    VILLAGER("minecraft:villager", 0.6, 1.95, VillagerMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    VINDICATOR("minecraft:vindicator", 0.6, 1.95, VindicatorMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    WANDERING_TRADER("minecraft:wandering_trader", 0.6, 1.95, WanderingTraderMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    WITCH("minecraft:witch", 0.6, 1.95, WitchMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    WITHER("minecraft:wither", 0.9, 3.5, WitherMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    WITHER_SKELETON("minecraft:wither_skeleton", 0.7, 2.4, WitherSkeletonMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    WITHER_SKULL("minecraft:wither_skull", 0.3125, 0.3125, WitherSkullMeta::new, EntitySpawnType.BASE, 0.1, 0.02),

    WOLF("minecraft:wolf", 0.6, 0.85, WolfMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ZOGLIN("minecraft:zoglin", 1.39648, 1.4, ZoglinMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ZOMBIE("minecraft:zombie", 0.6, 1.95, ZombieMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ZOMBIE_HORSE("minecraft:zombie_horse", 1.39648, 1.6, ZombieHorseMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ZOMBIE_VILLAGER("minecraft:zombie_villager", 0.6, 1.95, ZombieVillagerMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    ZOMBIFIED_PIGLIN("minecraft:zombified_piglin", 0.6, 1.95, ZombifiedPiglinMeta::new, EntitySpawnType.LIVING, 0.0784000015258789, 0.02),

    PLAYER("minecraft:player", 0.6, 1.8, PlayerMeta::new, EntitySpawnType.PLAYER, 0.0784000015258789, 0.02),

    FISHING_BOBBER("minecraft:fishing_bobber", 0.25, 0.25, FishingHookMeta::new, EntitySpawnType.BASE, 0.03, 0.08);

    private static final EntityType[] VALUES = values();

    @NotNull
    private final String namespaceID;

    private final double width;

    private final double height;

    @NotNull
    private final BiFunction<Entity, Metadata, EntityMeta> metaConstructor;

    @NotNull
    private final EntitySpawnType spawnType;

    private final double gravityAcceleration;

    private final double gravityDrag;

    EntityType(@NotNull String namespaceID, double width, double height,
            @NotNull BiFunction<Entity, Metadata, EntityMeta> metaConstructor,
            @NotNull EntitySpawnType spawnType, double gravityAcceleration, double gravityDrag) {
        this.namespaceID = namespaceID;
        this.width = width;
        this.height = height;
        this.metaConstructor = metaConstructor;
        this.spawnType = spawnType;
        this.gravityAcceleration = gravityAcceleration;
        this.gravityDrag = gravityDrag;
        Registries.entityTypes.put(NamespaceID.from(namespaceID), this);
    }

    public short getId() {
        return (short) ordinal();
    }

    public String getNamespaceID() {
        return this.namespaceID;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public double getGravityAcceleration() {
        return this.gravityAcceleration;
    }

    public double getGravityDrag() {
        return this.gravityDrag;
    }

    public BiFunction<Entity, Metadata, EntityMeta> getMetaConstructor() {
        return this.metaConstructor;
    }

    public EntitySpawnType getSpawnType() {
        return this.spawnType;
    }

    public static EntityType fromId(short id) {
        if(id >= 0 && id < VALUES.length) {
            return VALUES[id];
        }
        return null;
    }
}
