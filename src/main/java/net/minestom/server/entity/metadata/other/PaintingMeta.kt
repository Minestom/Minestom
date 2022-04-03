package net.minestom.server.entity.metadata.other

import net.minestom.server.utils.validate.Check.argCondition
import net.minestom.server.entity.metadata.item.ItemContainingMeta
import net.minestom.server.item.Material
import net.minestom.server.entity.metadata.ObjectDataProvider
import net.minestom.server.entity.metadata.ProjectileMeta
import net.minestom.server.entity.metadata.item.FireballMeta
import net.minestom.server.entity.metadata.item.SnowballMeta
import net.minestom.server.entity.metadata.item.ThrownEggMeta
import net.minestom.server.entity.metadata.item.EyeOfEnderMeta
import net.minestom.server.entity.metadata.item.ItemEntityMeta
import net.minestom.server.entity.metadata.item.ThrownPotionMeta
import net.minestom.server.entity.metadata.item.SmallFireballMeta
import net.minestom.server.entity.metadata.EntityMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.entity.metadata.item.ThrownEnderPearlMeta
import net.minestom.server.entity.metadata.item.ThrownExperienceBottleMeta
import net.minestom.server.entity.metadata.arrow.AbstractArrowMeta
import net.minestom.server.entity.metadata.arrow.ArrowMeta
import net.minestom.server.entity.metadata.arrow.SpectralArrowMeta
import net.minestom.server.entity.metadata.arrow.ThrownTridentMeta
import net.minestom.server.entity.metadata.golem.AbstractGolemMeta
import net.minestom.server.entity.metadata.golem.ShulkerMeta
import net.minestom.server.entity.metadata.golem.IronGolemMeta
import net.minestom.server.entity.metadata.golem.SnowGolemMeta
import net.minestom.server.entity.metadata.PathfinderMobMeta
import net.minestom.server.entity.metadata.other.BoatMeta
import net.minestom.server.entity.metadata.MobMeta
import net.minestom.server.entity.metadata.other.SlimeMeta
import net.minestom.server.entity.metadata.other.MarkerMeta
import net.minestom.server.entity.metadata.other.PaintingMeta.Motive
import java.util.Locale
import net.minestom.server.entity.metadata.other.PaintingMeta
import net.minestom.server.entity.metadata.other.ItemFrameMeta
import net.minestom.server.entity.metadata.other.LeashKnotMeta
import net.minestom.server.entity.metadata.other.LlamaSpitMeta
import net.minestom.server.entity.metadata.other.MagmaCubeMeta
import net.minestom.server.entity.metadata.other.PrimedTntMeta
import net.minestom.server.entity.metadata.LivingEntityMeta
import net.minestom.server.entity.metadata.other.ArmorStandMeta
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.metadata.other.EndCrystalMeta
import net.minestom.server.entity.metadata.other.EnderDragonMeta
import net.minestom.server.entity.metadata.other.EvokerFangsMeta
import net.minestom.server.entity.metadata.other.FishingHookMeta
import net.minestom.server.entity.metadata.other.TraderLlamaMeta
import net.minestom.server.entity.metadata.other.WitherSkullMeta
import net.minestom.server.entity.metadata.other.FallingBlockMeta
import net.minestom.server.entity.metadata.other.ExperienceOrbMeta
import net.minestom.server.entity.metadata.other.GlowItemFrameMeta
import net.minestom.server.entity.metadata.other.LightningBoltMeta
import net.minestom.server.entity.metadata.other.ShulkerBulletMeta
import net.minestom.server.entity.metadata.other.DragonFireballMeta
import net.minestom.server.entity.metadata.other.FireworkRocketMeta
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta
import net.minestom.server.entity.metadata.water.fish.AbstractFishMeta
import net.minestom.server.entity.metadata.water.fish.CodMeta
import net.minestom.server.entity.metadata.water.fish.SalmonMeta
import net.minestom.server.entity.metadata.water.fish.PufferfishMeta
import net.minestom.server.entity.metadata.water.WaterAnimalMeta
import net.minestom.server.entity.metadata.water.fish.TropicalFishMeta
import net.minestom.server.entity.metadata.water.SquidMeta
import net.minestom.server.entity.metadata.animal.AnimalMeta
import net.minestom.server.entity.metadata.water.AxolotlMeta
import net.minestom.server.entity.metadata.water.DolphinMeta
import net.minestom.server.entity.metadata.water.GlowSquidMeta
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta
import net.minestom.server.entity.metadata.animal.tameable.CatMeta
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta
import net.minestom.server.entity.metadata.animal.tameable.ParrotMeta
import net.minestom.server.entity.metadata.animal.BeeMeta
import net.minestom.server.entity.metadata.animal.CowMeta
import net.minestom.server.entity.metadata.animal.FoxMeta
import net.minestom.server.entity.metadata.animal.PigMeta
import net.minestom.server.entity.metadata.animal.GoatMeta
import net.minestom.server.entity.metadata.animal.ChestedHorseMeta
import net.minestom.server.entity.metadata.animal.MuleMeta
import net.minestom.server.entity.metadata.animal.AbstractHorseMeta
import net.minestom.server.entity.metadata.animal.HorseMeta
import net.minestom.server.entity.metadata.animal.HorseMeta.Marking
import net.minestom.server.entity.metadata.animal.LlamaMeta
import net.minestom.server.entity.metadata.animal.PandaMeta
import net.minestom.server.entity.metadata.animal.PandaMeta.Gene
import net.minestom.server.entity.metadata.animal.SheepMeta
import net.minestom.server.entity.metadata.AgeableMobMeta
import net.minestom.server.entity.metadata.animal.DonkeyMeta
import net.minestom.server.entity.metadata.animal.HoglinMeta
import net.minestom.server.entity.metadata.animal.OcelotMeta
import net.minestom.server.entity.metadata.animal.RabbitMeta
import net.minestom.server.entity.metadata.animal.TurtleMeta
import net.minestom.server.entity.metadata.animal.ChickenMeta
import net.minestom.server.entity.metadata.animal.StriderMeta
import net.minestom.server.entity.metadata.animal.MooshroomMeta
import net.minestom.server.entity.metadata.animal.PolarBearMeta
import net.minestom.server.entity.metadata.animal.ZombieHorseMeta
import net.minestom.server.entity.metadata.animal.SkeletonHorseMeta
import net.minestom.server.entity.metadata.flying.FlyingMeta
import net.minestom.server.entity.metadata.flying.GhastMeta
import net.minestom.server.entity.metadata.flying.PhantomMeta
import net.minestom.server.entity.metadata.ambient.AmbientCreatureMeta
import net.minestom.server.entity.metadata.ambient.BatMeta
import net.minestom.server.entity.metadata.monster.raider.RaiderMeta
import net.minestom.server.entity.metadata.monster.raider.WitchMeta
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta
import net.minestom.server.entity.metadata.monster.raider.EvokerMeta
import net.minestom.server.entity.metadata.monster.MonsterMeta
import net.minestom.server.entity.metadata.monster.raider.RavagerMeta
import net.minestom.server.entity.metadata.monster.raider.AbstractIllagerMeta
import net.minestom.server.entity.metadata.monster.raider.PillagerMeta
import net.minestom.server.entity.metadata.monster.raider.IllusionerMeta
import net.minestom.server.entity.metadata.monster.raider.VindicatorMeta
import net.minestom.server.entity.metadata.monster.raider.SpellcasterIllagerMeta.Spell
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta
import net.minestom.server.entity.metadata.monster.zombie.HuskMeta
import net.minestom.server.collision.BoundingBox
import net.minestom.server.entity.Entity
import net.minestom.server.entity.metadata.monster.zombie.DrownedMeta
import net.minestom.server.entity.metadata.monster.zombie.ZombieVillagerMeta
import net.minestom.server.entity.metadata.villager.VillagerMeta.VillagerData
import net.minestom.server.entity.metadata.villager.VillagerMeta.Profession
import net.minestom.server.entity.metadata.monster.zombie.ZombifiedPiglinMeta
import net.minestom.server.entity.metadata.monster.skeleton.AbstractSkeletonMeta
import net.minestom.server.entity.metadata.monster.skeleton.StrayMeta
import net.minestom.server.entity.metadata.monster.skeleton.SkeletonMeta
import net.minestom.server.entity.metadata.monster.skeleton.WitherSkeletonMeta
import net.minestom.server.entity.metadata.monster.VexMeta
import net.minestom.server.entity.metadata.monster.BlazeMeta
import net.minestom.server.entity.metadata.monster.GiantMeta
import net.minestom.server.entity.metadata.monster.BasePiglinMeta
import net.minestom.server.entity.metadata.monster.PiglinMeta
import net.minestom.server.entity.metadata.monster.SpiderMeta
import net.minestom.server.entity.metadata.monster.WitherMeta
import net.minestom.server.entity.metadata.monster.ZoglinMeta
import net.minestom.server.entity.metadata.monster.CreeperMeta
import net.minestom.server.entity.metadata.monster.EndermanMeta
import net.minestom.server.entity.metadata.monster.GuardianMeta
import net.minestom.server.entity.metadata.monster.EndermiteMeta
import net.minestom.server.entity.metadata.monster.CaveSpiderMeta
import net.minestom.server.entity.metadata.monster.SilverfishMeta
import net.minestom.server.entity.metadata.monster.PiglinBruteMeta
import net.minestom.server.entity.metadata.monster.ElderGuardianMeta
import net.minestom.server.entity.metadata.minecart.AbstractMinecartMeta
import net.minestom.server.entity.metadata.minecart.MinecartMeta
import net.minestom.server.entity.metadata.minecart.TntMinecartMeta
import net.minestom.server.entity.metadata.minecart.AbstractMinecartContainerMeta
import net.minestom.server.entity.metadata.minecart.ChestMinecartMeta
import net.minestom.server.entity.metadata.minecart.HopperMinecartMeta
import net.minestom.server.entity.metadata.minecart.FurnaceMinecartMeta
import net.minestom.server.entity.metadata.minecart.SpawnerMinecartMeta
import net.minestom.server.entity.metadata.minecart.CommandBlockMinecartMeta
import net.minestom.server.entity.metadata.villager.AbstractVillagerMeta
import net.minestom.server.entity.metadata.villager.VillagerMeta
import net.minestom.server.entity.metadata.villager.WanderingTraderMeta
import java.lang.ref.WeakReference
import net.minestom.server.entity.Entity.Pose
import net.minestom.server.entity.Metadata
import net.minestom.server.entity.metadata.PlayerMeta
import org.jglrxavpok.hephaistos.nbt.NBT
import net.minestom.server.entity.Player.Hand
import net.minestom.server.utils.Direction

class PaintingMeta(entity: Entity, metadata: Metadata) : EntityMeta(entity, metadata) {
    /**
     * Sets motive of a painting.
     * This is possible only before spawn packet is sent.
     *
     * @param motive motive of a painting.
     */
    var motive = Motive.KEBAB

    /**
     * Sets direction of a painting.
     * This is possible only before spawn packet is sent.
     *
     * @param direction direction of a painting.
     */
    var direction = Direction.SOUTH
        set(direction) {
            argCondition(direction == Direction.UP || direction == Direction.DOWN, "Painting can't look up or down!")
            field = direction
        }

    /*
      TODO: write a parser?
      Currently none of existing ones support it.
     */
    enum class Motive {
        KEBAB(0, 0, 16, 16), AZTEC(16, 0, 16, 16), ALBAN(32, 0, 16, 16), AZTEC2(48, 0, 16, 16), BOMB(
            64,
            0,
            16,
            16
        ),
        PLANT(80, 0, 16, 16), WASTELAND(96, 0, 16, 16), POOL(0, 32, 32, 16), COURBET(32, 32, 32, 16), SEA(
            64,
            32,
            32,
            16
        ),
        SUNSET(96, 32, 32, 16), CREEBET(128, 32, 32, 16), WANDERER(0, 64, 16, 32), GRAHAM(16, 64, 16, 32), MATCH(
            0,
            128,
            32,
            32
        ),
        BUST(32, 128, 32, 32), STAGE(64, 128, 32, 32), VOID(96, 128, 32, 32), SKULL_AND_ROSES(
            "skull_and_roses",
            128,
            128,
            32,
            32
        ),
        WITHER(160, 128, 32, 32), FIGHTERS(0, 96, 64, 32), POINTER(0, 192, 64, 64), PIGSCENE(
            64,
            192,
            64,
            64
        ),
        BURNING_SKULL(128, 192, 64, 64), SKELETON(192, 64, 64, 48), DONKEY_KONG(192, 112, 64, 48);

        val name: String
        val x: Int
        val y: Int
        val width: Int
        val height: Int

        constructor(name: String, x: Int, y: Int, width: Int, height: Int) {
            this.name = name
            this.x = x
            this.y = y
            this.width = width
            this.height = height
        }

        constructor(x: Int, y: Int, width: Int, height: Int) {
            name = "minecraft:" + name.toLowerCase(Locale.ROOT)
            this.x = x
            this.y = y
            this.width = width
            this.height = height
        }
    }

    companion object {
        val OFFSET: Byte = EntityMeta.Companion.MAX_OFFSET
        val MAX_OFFSET = (OFFSET + 0).toByte()
    }
}