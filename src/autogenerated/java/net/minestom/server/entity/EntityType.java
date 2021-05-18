package net.minestom.server.entity;

import java.util.List;
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
import net.minestom.server.raw_data.RawEntityTypeData;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * AUTOGENERATED by EntityTypeGenerator
 */
public class EntityType implements Keyed {
    public static final EntityType AREA_EFFECT_CLOUD = new EntityType(NamespaceID.from("minecraft:area_effect_cloud"), 6.0, 0.5, AreaEffectCloudMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(true));

    public static final EntityType ARMOR_STAND = new EntityType(NamespaceID.from("minecraft:armor_stand"), 0.5, 1.975, ArmorStandMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType ARROW = new EntityType(NamespaceID.from("minecraft:arrow"), 0.5, 0.5, ArrowMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType BAT = new EntityType(NamespaceID.from("minecraft:bat"), 0.5, 0.9, BatMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType BEE = new EntityType(NamespaceID.from("minecraft:bee"), 0.7, 0.6, BeeMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType BLAZE = new EntityType(NamespaceID.from("minecraft:blaze"), 0.6, 1.8, BlazeMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType BOAT = new EntityType(NamespaceID.from("minecraft:boat"), 1.375, 0.5625, BoatMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType CAT = new EntityType(NamespaceID.from("minecraft:cat"), 0.6, 0.7, CatMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType CAVE_SPIDER = new EntityType(NamespaceID.from("minecraft:cave_spider"), 0.7, 0.5, CaveSpiderMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType CHICKEN = new EntityType(NamespaceID.from("minecraft:chicken"), 0.4, 0.7, ChickenMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType COD = new EntityType(NamespaceID.from("minecraft:cod"), 0.5, 0.3, CodMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType COW = new EntityType(NamespaceID.from("minecraft:cow"), 0.9, 1.4, CowMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType CREEPER = new EntityType(NamespaceID.from("minecraft:creeper"), 0.6, 1.7, CreeperMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType DOLPHIN = new EntityType(NamespaceID.from("minecraft:dolphin"), 0.9, 0.6, DolphinMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType DONKEY = new EntityType(NamespaceID.from("minecraft:donkey"), 1.3964844, 1.5, DonkeyMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType DRAGON_FIREBALL = new EntityType(NamespaceID.from("minecraft:dragon_fireball"), 1.0, 1.0, DragonFireballMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType DROWNED = new EntityType(NamespaceID.from("minecraft:drowned"), 0.6, 1.95, DrownedMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType ELDER_GUARDIAN = new EntityType(NamespaceID.from("minecraft:elder_guardian"), 1.9975, 1.9975, ElderGuardianMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType END_CRYSTAL = new EntityType(NamespaceID.from("minecraft:end_crystal"), 2.0, 2.0, EndCrystalMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType ENDER_DRAGON = new EntityType(NamespaceID.from("minecraft:ender_dragon"), 16.0, 8.0, EnderDragonMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType ENDERMAN = new EntityType(NamespaceID.from("minecraft:enderman"), 0.6, 2.9, EndermanMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType ENDERMITE = new EntityType(NamespaceID.from("minecraft:endermite"), 0.4, 0.3, EndermiteMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType EVOKER = new EntityType(NamespaceID.from("minecraft:evoker"), 0.6, 1.95, EvokerMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType EVOKER_FANGS = new EntityType(NamespaceID.from("minecraft:evoker_fangs"), 0.5, 0.8, EvokerFangsMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType EXPERIENCE_ORB = new EntityType(NamespaceID.from("minecraft:experience_orb"), 0.5, 0.5, ExperienceOrbMeta::new, EntitySpawnType.EXPERIENCE_ORB, new RawEntityTypeData(false));

    public static final EntityType EYE_OF_ENDER = new EntityType(NamespaceID.from("minecraft:eye_of_ender"), 0.25, 0.25, EyeOfEnderMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType FALLING_BLOCK = new EntityType(NamespaceID.from("minecraft:falling_block"), 0.98, 0.98, FallingBlockMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType FIREWORK_ROCKET = new EntityType(NamespaceID.from("minecraft:firework_rocket"), 0.25, 0.25, FireworkRocketMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType FOX = new EntityType(NamespaceID.from("minecraft:fox"), 0.6, 0.7, FoxMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType GHAST = new EntityType(NamespaceID.from("minecraft:ghast"), 4.0, 4.0, GhastMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType GIANT = new EntityType(NamespaceID.from("minecraft:giant"), 3.6, 12.0, GiantMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType GUARDIAN = new EntityType(NamespaceID.from("minecraft:guardian"), 0.85, 0.85, GuardianMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType HOGLIN = new EntityType(NamespaceID.from("minecraft:hoglin"), 1.3964844, 1.4, HoglinMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType HORSE = new EntityType(NamespaceID.from("minecraft:horse"), 1.3964844, 1.6, HorseMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType HUSK = new EntityType(NamespaceID.from("minecraft:husk"), 0.6, 1.95, HuskMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType ILLUSIONER = new EntityType(NamespaceID.from("minecraft:illusioner"), 0.6, 1.95, IllusionerMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType IRON_GOLEM = new EntityType(NamespaceID.from("minecraft:iron_golem"), 1.4, 2.7, IronGolemMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType ITEM = new EntityType(NamespaceID.from("minecraft:item"), 0.25, 0.25, ItemEntityMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType ITEM_FRAME = new EntityType(NamespaceID.from("minecraft:item_frame"), 0.5, 0.5, ItemFrameMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType FIREBALL = new EntityType(NamespaceID.from("minecraft:fireball"), 1.0, 1.0, FireballMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType LEASH_KNOT = new EntityType(NamespaceID.from("minecraft:leash_knot"), 0.5, 0.5, LeashKnotMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType LIGHTNING_BOLT = new EntityType(NamespaceID.from("minecraft:lightning_bolt"), 0.0, 0.0, LightningBoltMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType LLAMA = new EntityType(NamespaceID.from("minecraft:llama"), 0.9, 1.87, LlamaMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType LLAMA_SPIT = new EntityType(NamespaceID.from("minecraft:llama_spit"), 0.25, 0.25, LlamaSpitMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType MAGMA_CUBE = new EntityType(NamespaceID.from("minecraft:magma_cube"), 2.04, 2.04, MagmaCubeMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType MINECART = new EntityType(NamespaceID.from("minecraft:minecart"), 0.98, 0.7, MinecartMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType CHEST_MINECART = new EntityType(NamespaceID.from("minecraft:chest_minecart"), 0.98, 0.7, ChestMinecartMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType COMMAND_BLOCK_MINECART = new EntityType(NamespaceID.from("minecraft:command_block_minecart"), 0.98, 0.7, CommandBlockMinecartMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType FURNACE_MINECART = new EntityType(NamespaceID.from("minecraft:furnace_minecart"), 0.98, 0.7, FurnaceMinecartMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType HOPPER_MINECART = new EntityType(NamespaceID.from("minecraft:hopper_minecart"), 0.98, 0.7, HopperMinecartMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType SPAWNER_MINECART = new EntityType(NamespaceID.from("minecraft:spawner_minecart"), 0.98, 0.7, SpawnerMinecartMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType TNT_MINECART = new EntityType(NamespaceID.from("minecraft:tnt_minecart"), 0.98, 0.7, TntMinecartMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType MULE = new EntityType(NamespaceID.from("minecraft:mule"), 1.3964844, 1.6, MuleMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType MOOSHROOM = new EntityType(NamespaceID.from("minecraft:mooshroom"), 0.9, 1.4, MooshroomMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType OCELOT = new EntityType(NamespaceID.from("minecraft:ocelot"), 0.6, 0.7, OcelotMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType PAINTING = new EntityType(NamespaceID.from("minecraft:painting"), 0.5, 0.5, PaintingMeta::new, EntitySpawnType.PAINTING, new RawEntityTypeData(false));

    public static final EntityType PANDA = new EntityType(NamespaceID.from("minecraft:panda"), 1.3, 1.25, PandaMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType PARROT = new EntityType(NamespaceID.from("minecraft:parrot"), 0.5, 0.9, ParrotMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType PHANTOM = new EntityType(NamespaceID.from("minecraft:phantom"), 0.9, 0.5, PhantomMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType PIG = new EntityType(NamespaceID.from("minecraft:pig"), 0.9, 0.9, PigMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType PIGLIN = new EntityType(NamespaceID.from("minecraft:piglin"), 0.6, 1.95, PiglinMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType PIGLIN_BRUTE = new EntityType(NamespaceID.from("minecraft:piglin_brute"), 0.6, 1.95, PiglinBruteMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType PILLAGER = new EntityType(NamespaceID.from("minecraft:pillager"), 0.6, 1.95, PillagerMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType POLAR_BEAR = new EntityType(NamespaceID.from("minecraft:polar_bear"), 1.4, 1.4, PolarBearMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType TNT = new EntityType(NamespaceID.from("minecraft:tnt"), 0.98, 0.98, PrimedTntMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(true));

    public static final EntityType PUFFERFISH = new EntityType(NamespaceID.from("minecraft:pufferfish"), 0.7, 0.7, PufferfishMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType RABBIT = new EntityType(NamespaceID.from("minecraft:rabbit"), 0.4, 0.5, RabbitMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType RAVAGER = new EntityType(NamespaceID.from("minecraft:ravager"), 1.95, 2.2, RavagerMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType SALMON = new EntityType(NamespaceID.from("minecraft:salmon"), 0.7, 0.4, SalmonMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType SHEEP = new EntityType(NamespaceID.from("minecraft:sheep"), 0.9, 1.3, SheepMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType SHULKER = new EntityType(NamespaceID.from("minecraft:shulker"), 1.0, 1.0, ShulkerMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType SHULKER_BULLET = new EntityType(NamespaceID.from("minecraft:shulker_bullet"), 0.3125, 0.3125, ShulkerBulletMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType SILVERFISH = new EntityType(NamespaceID.from("minecraft:silverfish"), 0.4, 0.3, SilverfishMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType SKELETON = new EntityType(NamespaceID.from("minecraft:skeleton"), 0.6, 1.99, SkeletonMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType SKELETON_HORSE = new EntityType(NamespaceID.from("minecraft:skeleton_horse"), 1.3964844, 1.6, SkeletonHorseMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType SLIME = new EntityType(NamespaceID.from("minecraft:slime"), 2.04, 2.04, SlimeMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType SMALL_FIREBALL = new EntityType(NamespaceID.from("minecraft:small_fireball"), 0.3125, 0.3125, SmallFireballMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType SNOW_GOLEM = new EntityType(NamespaceID.from("minecraft:snow_golem"), 0.7, 1.9, SnowGolemMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType SNOWBALL = new EntityType(NamespaceID.from("minecraft:snowball"), 0.25, 0.25, SnowballMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType SPECTRAL_ARROW = new EntityType(NamespaceID.from("minecraft:spectral_arrow"), 0.5, 0.5, SpectralArrowMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType SPIDER = new EntityType(NamespaceID.from("minecraft:spider"), 1.4, 0.9, SpiderMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType SQUID = new EntityType(NamespaceID.from("minecraft:squid"), 0.8, 0.8, SquidMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType STRAY = new EntityType(NamespaceID.from("minecraft:stray"), 0.6, 1.99, StrayMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType STRIDER = new EntityType(NamespaceID.from("minecraft:strider"), 0.9, 1.7, StriderMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType EGG = new EntityType(NamespaceID.from("minecraft:egg"), 0.25, 0.25, ThrownEggMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType ENDER_PEARL = new EntityType(NamespaceID.from("minecraft:ender_pearl"), 0.25, 0.25, ThrownEnderPearlMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType EXPERIENCE_BOTTLE = new EntityType(NamespaceID.from("minecraft:experience_bottle"), 0.25, 0.25, ThrownExperienceBottleMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType POTION = new EntityType(NamespaceID.from("minecraft:potion"), 0.25, 0.25, ThrownPotionMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType TRIDENT = new EntityType(NamespaceID.from("minecraft:trident"), 0.5, 0.5, ThrownTridentMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType TRADER_LLAMA = new EntityType(NamespaceID.from("minecraft:trader_llama"), 0.9, 1.87, TraderLlamaMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType TROPICAL_FISH = new EntityType(NamespaceID.from("minecraft:tropical_fish"), 0.5, 0.4, TropicalFishMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType TURTLE = new EntityType(NamespaceID.from("minecraft:turtle"), 1.2, 0.4, TurtleMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType VEX = new EntityType(NamespaceID.from("minecraft:vex"), 0.4, 0.8, VexMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType VILLAGER = new EntityType(NamespaceID.from("minecraft:villager"), 0.6, 1.95, VillagerMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType VINDICATOR = new EntityType(NamespaceID.from("minecraft:vindicator"), 0.6, 1.95, VindicatorMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType WANDERING_TRADER = new EntityType(NamespaceID.from("minecraft:wandering_trader"), 0.6, 1.95, WanderingTraderMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType WITCH = new EntityType(NamespaceID.from("minecraft:witch"), 0.6, 1.95, WitchMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType WITHER = new EntityType(NamespaceID.from("minecraft:wither"), 0.9, 3.5, WitherMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType WITHER_SKELETON = new EntityType(NamespaceID.from("minecraft:wither_skeleton"), 0.7, 2.4, WitherSkeletonMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType WITHER_SKULL = new EntityType(NamespaceID.from("minecraft:wither_skull"), 0.3125, 0.3125, WitherSkullMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    public static final EntityType WOLF = new EntityType(NamespaceID.from("minecraft:wolf"), 0.6, 0.85, WolfMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType ZOGLIN = new EntityType(NamespaceID.from("minecraft:zoglin"), 1.3964844, 1.4, ZoglinMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType ZOMBIE = new EntityType(NamespaceID.from("minecraft:zombie"), 0.6, 1.95, ZombieMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType ZOMBIE_HORSE = new EntityType(NamespaceID.from("minecraft:zombie_horse"), 1.3964844, 1.6, ZombieHorseMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType ZOMBIE_VILLAGER = new EntityType(NamespaceID.from("minecraft:zombie_villager"), 0.6, 1.95, ZombieVillagerMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(false));

    public static final EntityType ZOMBIFIED_PIGLIN = new EntityType(NamespaceID.from("minecraft:zombified_piglin"), 0.6, 1.95, ZombifiedPiglinMeta::new, EntitySpawnType.LIVING, new RawEntityTypeData(true));

    public static final EntityType PLAYER = new EntityType(NamespaceID.from("minecraft:player"), 0.6, 1.8, PlayerMeta::new, EntitySpawnType.PLAYER, new RawEntityTypeData(false));

    public static final EntityType FISHING_BOBBER = new EntityType(NamespaceID.from("minecraft:fishing_bobber"), 0.25, 0.25, FishingHookMeta::new, EntitySpawnType.BASE, new RawEntityTypeData(false));

    static {
        Registry.ENTITY_TYPE_REGISTRY.register(AREA_EFFECT_CLOUD);
        Registry.ENTITY_TYPE_REGISTRY.register(ARMOR_STAND);
        Registry.ENTITY_TYPE_REGISTRY.register(ARROW);
        Registry.ENTITY_TYPE_REGISTRY.register(BAT);
        Registry.ENTITY_TYPE_REGISTRY.register(BEE);
        Registry.ENTITY_TYPE_REGISTRY.register(BLAZE);
        Registry.ENTITY_TYPE_REGISTRY.register(BOAT);
        Registry.ENTITY_TYPE_REGISTRY.register(CAT);
        Registry.ENTITY_TYPE_REGISTRY.register(CAVE_SPIDER);
        Registry.ENTITY_TYPE_REGISTRY.register(CHICKEN);
        Registry.ENTITY_TYPE_REGISTRY.register(COD);
        Registry.ENTITY_TYPE_REGISTRY.register(COW);
        Registry.ENTITY_TYPE_REGISTRY.register(CREEPER);
        Registry.ENTITY_TYPE_REGISTRY.register(DOLPHIN);
        Registry.ENTITY_TYPE_REGISTRY.register(DONKEY);
        Registry.ENTITY_TYPE_REGISTRY.register(DRAGON_FIREBALL);
        Registry.ENTITY_TYPE_REGISTRY.register(DROWNED);
        Registry.ENTITY_TYPE_REGISTRY.register(ELDER_GUARDIAN);
        Registry.ENTITY_TYPE_REGISTRY.register(END_CRYSTAL);
        Registry.ENTITY_TYPE_REGISTRY.register(ENDER_DRAGON);
        Registry.ENTITY_TYPE_REGISTRY.register(ENDERMAN);
        Registry.ENTITY_TYPE_REGISTRY.register(ENDERMITE);
        Registry.ENTITY_TYPE_REGISTRY.register(EVOKER);
        Registry.ENTITY_TYPE_REGISTRY.register(EVOKER_FANGS);
        Registry.ENTITY_TYPE_REGISTRY.register(EXPERIENCE_ORB);
        Registry.ENTITY_TYPE_REGISTRY.register(EYE_OF_ENDER);
        Registry.ENTITY_TYPE_REGISTRY.register(FALLING_BLOCK);
        Registry.ENTITY_TYPE_REGISTRY.register(FIREWORK_ROCKET);
        Registry.ENTITY_TYPE_REGISTRY.register(FOX);
        Registry.ENTITY_TYPE_REGISTRY.register(GHAST);
        Registry.ENTITY_TYPE_REGISTRY.register(GIANT);
        Registry.ENTITY_TYPE_REGISTRY.register(GUARDIAN);
        Registry.ENTITY_TYPE_REGISTRY.register(HOGLIN);
        Registry.ENTITY_TYPE_REGISTRY.register(HORSE);
        Registry.ENTITY_TYPE_REGISTRY.register(HUSK);
        Registry.ENTITY_TYPE_REGISTRY.register(ILLUSIONER);
        Registry.ENTITY_TYPE_REGISTRY.register(IRON_GOLEM);
        Registry.ENTITY_TYPE_REGISTRY.register(ITEM);
        Registry.ENTITY_TYPE_REGISTRY.register(ITEM_FRAME);
        Registry.ENTITY_TYPE_REGISTRY.register(FIREBALL);
        Registry.ENTITY_TYPE_REGISTRY.register(LEASH_KNOT);
        Registry.ENTITY_TYPE_REGISTRY.register(LIGHTNING_BOLT);
        Registry.ENTITY_TYPE_REGISTRY.register(LLAMA);
        Registry.ENTITY_TYPE_REGISTRY.register(LLAMA_SPIT);
        Registry.ENTITY_TYPE_REGISTRY.register(MAGMA_CUBE);
        Registry.ENTITY_TYPE_REGISTRY.register(MINECART);
        Registry.ENTITY_TYPE_REGISTRY.register(CHEST_MINECART);
        Registry.ENTITY_TYPE_REGISTRY.register(COMMAND_BLOCK_MINECART);
        Registry.ENTITY_TYPE_REGISTRY.register(FURNACE_MINECART);
        Registry.ENTITY_TYPE_REGISTRY.register(HOPPER_MINECART);
        Registry.ENTITY_TYPE_REGISTRY.register(SPAWNER_MINECART);
        Registry.ENTITY_TYPE_REGISTRY.register(TNT_MINECART);
        Registry.ENTITY_TYPE_REGISTRY.register(MULE);
        Registry.ENTITY_TYPE_REGISTRY.register(MOOSHROOM);
        Registry.ENTITY_TYPE_REGISTRY.register(OCELOT);
        Registry.ENTITY_TYPE_REGISTRY.register(PAINTING);
        Registry.ENTITY_TYPE_REGISTRY.register(PANDA);
        Registry.ENTITY_TYPE_REGISTRY.register(PARROT);
        Registry.ENTITY_TYPE_REGISTRY.register(PHANTOM);
        Registry.ENTITY_TYPE_REGISTRY.register(PIG);
        Registry.ENTITY_TYPE_REGISTRY.register(PIGLIN);
        Registry.ENTITY_TYPE_REGISTRY.register(PIGLIN_BRUTE);
        Registry.ENTITY_TYPE_REGISTRY.register(PILLAGER);
        Registry.ENTITY_TYPE_REGISTRY.register(POLAR_BEAR);
        Registry.ENTITY_TYPE_REGISTRY.register(TNT);
        Registry.ENTITY_TYPE_REGISTRY.register(PUFFERFISH);
        Registry.ENTITY_TYPE_REGISTRY.register(RABBIT);
        Registry.ENTITY_TYPE_REGISTRY.register(RAVAGER);
        Registry.ENTITY_TYPE_REGISTRY.register(SALMON);
        Registry.ENTITY_TYPE_REGISTRY.register(SHEEP);
        Registry.ENTITY_TYPE_REGISTRY.register(SHULKER);
        Registry.ENTITY_TYPE_REGISTRY.register(SHULKER_BULLET);
        Registry.ENTITY_TYPE_REGISTRY.register(SILVERFISH);
        Registry.ENTITY_TYPE_REGISTRY.register(SKELETON);
        Registry.ENTITY_TYPE_REGISTRY.register(SKELETON_HORSE);
        Registry.ENTITY_TYPE_REGISTRY.register(SLIME);
        Registry.ENTITY_TYPE_REGISTRY.register(SMALL_FIREBALL);
        Registry.ENTITY_TYPE_REGISTRY.register(SNOW_GOLEM);
        Registry.ENTITY_TYPE_REGISTRY.register(SNOWBALL);
        Registry.ENTITY_TYPE_REGISTRY.register(SPECTRAL_ARROW);
        Registry.ENTITY_TYPE_REGISTRY.register(SPIDER);
        Registry.ENTITY_TYPE_REGISTRY.register(SQUID);
        Registry.ENTITY_TYPE_REGISTRY.register(STRAY);
        Registry.ENTITY_TYPE_REGISTRY.register(STRIDER);
        Registry.ENTITY_TYPE_REGISTRY.register(EGG);
        Registry.ENTITY_TYPE_REGISTRY.register(ENDER_PEARL);
        Registry.ENTITY_TYPE_REGISTRY.register(EXPERIENCE_BOTTLE);
        Registry.ENTITY_TYPE_REGISTRY.register(POTION);
        Registry.ENTITY_TYPE_REGISTRY.register(TRIDENT);
        Registry.ENTITY_TYPE_REGISTRY.register(TRADER_LLAMA);
        Registry.ENTITY_TYPE_REGISTRY.register(TROPICAL_FISH);
        Registry.ENTITY_TYPE_REGISTRY.register(TURTLE);
        Registry.ENTITY_TYPE_REGISTRY.register(VEX);
        Registry.ENTITY_TYPE_REGISTRY.register(VILLAGER);
        Registry.ENTITY_TYPE_REGISTRY.register(VINDICATOR);
        Registry.ENTITY_TYPE_REGISTRY.register(WANDERING_TRADER);
        Registry.ENTITY_TYPE_REGISTRY.register(WITCH);
        Registry.ENTITY_TYPE_REGISTRY.register(WITHER);
        Registry.ENTITY_TYPE_REGISTRY.register(WITHER_SKELETON);
        Registry.ENTITY_TYPE_REGISTRY.register(WITHER_SKULL);
        Registry.ENTITY_TYPE_REGISTRY.register(WOLF);
        Registry.ENTITY_TYPE_REGISTRY.register(ZOGLIN);
        Registry.ENTITY_TYPE_REGISTRY.register(ZOMBIE);
        Registry.ENTITY_TYPE_REGISTRY.register(ZOMBIE_HORSE);
        Registry.ENTITY_TYPE_REGISTRY.register(ZOMBIE_VILLAGER);
        Registry.ENTITY_TYPE_REGISTRY.register(ZOMBIFIED_PIGLIN);
        Registry.ENTITY_TYPE_REGISTRY.register(PLAYER);
        Registry.ENTITY_TYPE_REGISTRY.register(FISHING_BOBBER);
    }

    @NotNull
    private final NamespaceID id;

    private final double width;

    private final double height;

    @NotNull
    private final BiFunction<Entity, Metadata, EntityMeta> metaConstructor;

    @NotNull
    private final EntitySpawnType spawnType;

    @NotNull
    private volatile RawEntityTypeData entityTypeData;

    protected EntityType(@NotNull NamespaceID id, double width, double height,
            @NotNull BiFunction<Entity, Metadata, EntityMeta> metaConstructor,
            @NotNull EntitySpawnType spawnType, @NotNull RawEntityTypeData entityTypeData) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.metaConstructor = metaConstructor;
        this.spawnType = spawnType;
        this.entityTypeData = entityTypeData;
    }

    @Override
    @NotNull
    public Key key() {
        return this.id;
    }

    @NotNull
    public NamespaceID getId() {
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

    public int getNumericalId() {
        return Registry.ENTITY_TYPE_REGISTRY.getId(this);
    }

    @Nullable
    public static EntityType fromId(int id) {
        return Registry.ENTITY_TYPE_REGISTRY.get((short) id);
    }

    @NotNull
    public static EntityType fromId(Key id) {
        return Registry.ENTITY_TYPE_REGISTRY.get(id);
    }

    @NotNull
    public final RawEntityTypeData getEntityTypeData() {
        return this.entityTypeData;
    }

    public final void setEntityTypeData(@NotNull RawEntityTypeData entityTypeData) {
        this.entityTypeData = entityTypeData;
    }

    @NotNull
    @Override
    public String toString() {
        return "[" + this.id + "]";
    }

    @NotNull
    public static List<EntityType> values() {
        return Registry.ENTITY_TYPE_REGISTRY.values();
    }
}
