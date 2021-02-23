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
import net.minestom.server.entity.metadata.object.EyeOfEnderMeta;
import net.minestom.server.entity.metadata.object.FireballMeta;
import net.minestom.server.entity.metadata.object.ItemEntityMeta;
import net.minestom.server.entity.metadata.object.SmallFireballMeta;
import net.minestom.server.entity.metadata.object.SnowballMeta;
import net.minestom.server.entity.metadata.object.ThrownEggMeta;
import net.minestom.server.entity.metadata.object.ThrownEnderPearlMeta;
import net.minestom.server.entity.metadata.object.ThrownExperienceBottleMeta;
import net.minestom.server.entity.metadata.object.ThrownPotionMeta;
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
    AREA_EFFECT_CLOUD("minecraft:area_effect_cloud", 6.0, 0.5, AreaEffectCloudMeta::new),

    ARMOR_STAND("minecraft:armor_stand", 0.5, 1.975, ArmorStandMeta::new),

    ARROW("minecraft:arrow", 0.5, 0.5, ArrowMeta::new),

    BAT("minecraft:bat", 0.5, 0.9, BatMeta::new),

    BEE("minecraft:bee", 0.7, 0.6, BeeMeta::new),

    BLAZE("minecraft:blaze", 0.6, 1.8, BlazeMeta::new),

    BOAT("minecraft:boat", 1.375, 0.5625, BoatMeta::new),

    CAT("minecraft:cat", 0.6, 0.7, CatMeta::new),

    CAVE_SPIDER("minecraft:cave_spider", 0.7, 0.5, CaveSpiderMeta::new),

    CHICKEN("minecraft:chicken", 0.4, 0.7, ChickenMeta::new),

    COD("minecraft:cod", 0.5, 0.3, CodMeta::new),

    COW("minecraft:cow", 0.9, 1.4, CowMeta::new),

    CREEPER("minecraft:creeper", 0.6, 1.7, CreeperMeta::new),

    DOLPHIN("minecraft:dolphin", 0.9, 0.6, DolphinMeta::new),

    DONKEY("minecraft:donkey", 1.39648, 1.5, DonkeyMeta::new),

    DRAGON_FIREBALL("minecraft:dragon_fireball", 1.0, 1.0, DragonFireballMeta::new),

    DROWNED("minecraft:drowned", 0.6, 1.95, DrownedMeta::new),

    ELDER_GUARDIAN("minecraft:elder_guardian", 1.9975, 1.9975, ElderGuardianMeta::new),

    END_CRYSTAL("minecraft:end_crystal", 2.0, 2.0, EndCrystalMeta::new),

    ENDER_DRAGON("minecraft:ender_dragon", 16.0, 8.0, EnderDragonMeta::new),

    ENDERMAN("minecraft:enderman", 0.6, 2.9, EndermanMeta::new),

    ENDERMITE("minecraft:endermite", 0.4, 0.3, EndermiteMeta::new),

    EVOKER("minecraft:evoker", 0.6, 1.95, EvokerMeta::new),

    EVOKER_FANGS("minecraft:evoker_fangs", 0.5, 0.8, EvokerFangsMeta::new),

    EXPERIENCE_ORB("minecraft:experience_orb", 0.5, 0.5, ExperienceOrbMeta::new),

    EYE_OF_ENDER("minecraft:eye_of_ender", 0.25, 0.25, EyeOfEnderMeta::new),

    FALLING_BLOCK("minecraft:falling_block", 0.98, 0.98, FallingBlockMeta::new),

    FIREWORK_ROCKET("minecraft:firework_rocket", 0.25, 0.25, FireworkRocketMeta::new),

    FOX("minecraft:fox", 0.6, 0.7, FoxMeta::new),

    GHAST("minecraft:ghast", 4.0, 4.0, GhastMeta::new),

    GIANT("minecraft:giant", 3.6, 12.0, GiantMeta::new),

    GUARDIAN("minecraft:guardian", 0.85, 0.85, GuardianMeta::new),

    HOGLIN("minecraft:hoglin", 1.39648, 1.4, HoglinMeta::new),

    HORSE("minecraft:horse", 1.39648, 1.6, HorseMeta::new),

    HUSK("minecraft:husk", 0.6, 1.95, HuskMeta::new),

    ILLUSIONER("minecraft:illusioner", 0.6, 1.95, IllusionerMeta::new),

    IRON_GOLEM("minecraft:iron_golem", 1.4, 2.7, IronGolemMeta::new),

    ITEM("minecraft:item", 0.25, 0.25, ItemEntityMeta::new),

    ITEM_FRAME("minecraft:item_frame", 0.5, 0.5, ItemFrameMeta::new),

    FIREBALL("minecraft:fireball", 1.0, 1.0, FireballMeta::new),

    LEASH_KNOT("minecraft:leash_knot", 0.5, 0.5, LeashKnotMeta::new),

    LIGHTNING_BOLT("minecraft:lightning_bolt", 0.0, 0.0, LightningBoltMeta::new),

    LLAMA("minecraft:llama", 0.9, 1.87, LlamaMeta::new),

    LLAMA_SPIT("minecraft:llama_spit", 0.25, 0.25, LlamaSpitMeta::new),

    MAGMA_CUBE("minecraft:magma_cube", 2.04, 2.04, MagmaCubeMeta::new),

    MINECART("minecraft:minecart", 0.98, 0.7, MinecartMeta::new),

    CHEST_MINECART("minecraft:chest_minecart", 0.98, 0.7, ChestMinecartMeta::new),

    COMMAND_BLOCK_MINECART("minecraft:command_block_minecart", 0.98, 0.7, CommandBlockMinecartMeta::new),

    FURNACE_MINECART("minecraft:furnace_minecart", 0.98, 0.7, FurnaceMinecartMeta::new),

    HOPPER_MINECART("minecraft:hopper_minecart", 0.98, 0.7, HopperMinecartMeta::new),

    SPAWNER_MINECART("minecraft:spawner_minecart", 0.98, 0.7, SpawnerMinecartMeta::new),

    TNT_MINECART("minecraft:tnt_minecart", 0.98, 0.7, TntMinecartMeta::new),

    MULE("minecraft:mule", 1.39648, 1.6, MuleMeta::new),

    MOOSHROOM("minecraft:mooshroom", 0.9, 1.4, MooshroomMeta::new),

    OCELOT("minecraft:ocelot", 0.6, 0.7, OcelotMeta::new),

    PAINTING("minecraft:painting", 0.5, 0.5, PaintingMeta::new),

    PANDA("minecraft:panda", 1.3, 1.25, PandaMeta::new),

    PARROT("minecraft:parrot", 0.5, 0.9, ParrotMeta::new),

    PHANTOM("minecraft:phantom", 0.9, 0.5, PhantomMeta::new),

    PIG("minecraft:pig", 0.9, 0.9, PigMeta::new),

    PIGLIN("minecraft:piglin", 0.6, 1.95, PiglinMeta::new),

    PIGLIN_BRUTE("minecraft:piglin_brute", 0.6, 1.95, PiglinBruteMeta::new),

    PILLAGER("minecraft:pillager", 0.6, 1.95, PillagerMeta::new),

    POLAR_BEAR("minecraft:polar_bear", 1.4, 1.4, PolarBearMeta::new),

    TNT("minecraft:tnt", 0.98, 0.98, PrimedTntMeta::new),

    PUFFERFISH("minecraft:pufferfish", 0.7, 0.7, PufferfishMeta::new),

    RABBIT("minecraft:rabbit", 0.4, 0.5, RabbitMeta::new),

    RAVAGER("minecraft:ravager", 1.95, 2.2, RavagerMeta::new),

    SALMON("minecraft:salmon", 0.7, 0.4, SalmonMeta::new),

    SHEEP("minecraft:sheep", 0.9, 1.3, SheepMeta::new),

    SHULKER("minecraft:shulker", 1.0, 1.0, ShulkerMeta::new),

    SHULKER_BULLET("minecraft:shulker_bullet", 0.3125, 0.3125, ShulkerBulletMeta::new),

    SILVERFISH("minecraft:silverfish", 0.4, 0.3, SilverfishMeta::new),

    SKELETON("minecraft:skeleton", 0.6, 1.99, SkeletonMeta::new),

    SKELETON_HORSE("minecraft:skeleton_horse", 1.39648, 1.6, SkeletonHorseMeta::new),

    SLIME("minecraft:slime", 2.04, 2.04, SlimeMeta::new),

    SMALL_FIREBALL("minecraft:small_fireball", 0.3125, 0.3125, SmallFireballMeta::new),

    SNOW_GOLEM("minecraft:snow_golem", 0.7, 1.9, SnowGolemMeta::new),

    SNOWBALL("minecraft:snowball", 0.25, 0.25, SnowballMeta::new),

    SPECTRAL_ARROW("minecraft:spectral_arrow", 0.5, 0.5, SpectralArrowMeta::new),

    SPIDER("minecraft:spider", 1.4, 0.9, SpiderMeta::new),

    SQUID("minecraft:squid", 0.8, 0.8, SquidMeta::new),

    STRAY("minecraft:stray", 0.6, 1.99, StrayMeta::new),

    STRIDER("minecraft:strider", 0.9, 1.7, StriderMeta::new),

    EGG("minecraft:egg", 0.25, 0.25, ThrownEggMeta::new),

    ENDER_PEARL("minecraft:ender_pearl", 0.25, 0.25, ThrownEnderPearlMeta::new),

    EXPERIENCE_BOTTLE("minecraft:experience_bottle", 0.25, 0.25, ThrownExperienceBottleMeta::new),

    POTION("minecraft:potion", 0.25, 0.25, ThrownPotionMeta::new),

    TRIDENT("minecraft:trident", 0.5, 0.5, ThrownTridentMeta::new),

    TRADER_LLAMA("minecraft:trader_llama", 0.9, 1.87, TraderLlamaMeta::new),

    TROPICAL_FISH("minecraft:tropical_fish", 0.5, 0.4, TropicalFishMeta::new),

    TURTLE("minecraft:turtle", 1.2, 0.4, TurtleMeta::new),

    VEX("minecraft:vex", 0.4, 0.8, VexMeta::new),

    VILLAGER("minecraft:villager", 0.6, 1.95, VillagerMeta::new),

    VINDICATOR("minecraft:vindicator", 0.6, 1.95, VindicatorMeta::new),

    WANDERING_TRADER("minecraft:wandering_trader", 0.6, 1.95, WanderingTraderMeta::new),

    WITCH("minecraft:witch", 0.6, 1.95, WitchMeta::new),

    WITHER("minecraft:wither", 0.9, 3.5, WitherMeta::new),

    WITHER_SKELETON("minecraft:wither_skeleton", 0.7, 2.4, WitherSkeletonMeta::new),

    WITHER_SKULL("minecraft:wither_skull", 0.3125, 0.3125, WitherSkullMeta::new),

    WOLF("minecraft:wolf", 0.6, 0.85, WolfMeta::new),

    ZOGLIN("minecraft:zoglin", 1.39648, 1.4, ZoglinMeta::new),

    ZOMBIE("minecraft:zombie", 0.6, 1.95, ZombieMeta::new),

    ZOMBIE_HORSE("minecraft:zombie_horse", 1.39648, 1.6, ZombieHorseMeta::new),

    ZOMBIE_VILLAGER("minecraft:zombie_villager", 0.6, 1.95, ZombieVillagerMeta::new),

    ZOMBIFIED_PIGLIN("minecraft:zombified_piglin", 0.6, 1.95, ZombifiedPiglinMeta::new),

    PLAYER("minecraft:player", 0.6, 1.8, PlayerMeta::new),

    FISHING_BOBBER("minecraft:fishing_bobber", 0.25, 0.25, FishingHookMeta::new);

    @NotNull
    private String namespaceID;

    private double width;

    private double height;

    @NotNull
    private BiFunction<Entity, Metadata, EntityMeta> metaConstructor;

    EntityType(@NotNull String namespaceID, double width, double height,
            @NotNull BiFunction<Entity, Metadata, EntityMeta> metaConstructor) {
        this.namespaceID = namespaceID;
        this.width = width;
        this.height = height;
        this.metaConstructor = metaConstructor;
        Registries.entityTypes.put(NamespaceID.from(namespaceID), this);
    }

    public short getId() {
        return (short)ordinal();
    }

    public String getNamespaceID() {
        return namespaceID;
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

    public static EntityType fromId(short id) {
        if(id >= 0 && id < values().length) {
            return values()[id];
        }
        return PIG;
    }
}
