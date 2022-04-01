package net.minestom.server.entity.metadata.animal

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

class PandaMeta(entity: Entity, metadata: Metadata) : AnimalMeta(entity, metadata) {
    fun getBreedTimer(): Int {
        return super.metadata.getIndex(OFFSET.toInt(), 0)
    }

    fun setBreedTimer(value: Int) {
        super.metadata.setIndex(OFFSET.toInt(), Metadata.VarInt(value))
    }

    fun getSneezeTimer(): Int {
        return super.metadata.getIndex(OFFSET + 1, 0)
    }

    fun setSneezeTimer(value: Int) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value))
    }

    fun getEatTimer(): Int {
        return super.metadata.getIndex(OFFSET + 2, 0)
    }

    fun setEatTimer(value: Int) {
        super.metadata.setIndex(OFFSET + 2, Metadata.VarInt(value))
    }

    fun getMainGene(): Gene {
        return Gene.VALUES[super.metadata.getIndex(OFFSET + 3, 0.toByte()).toInt()]
    }

    fun setMainGene(value: Gene) {
        super.metadata.setIndex(OFFSET + 3, Metadata.Byte(value.ordinal.toByte()))
    }

    fun getHiddenGene(): Gene {
        return Gene.VALUES[super.metadata.getIndex(OFFSET + 4, 0.toByte()).toInt()]
    }

    fun setHiddenGene(value: Gene) {
        super.metadata.setIndex(OFFSET + 4, Metadata.Byte(value.ordinal.toByte()))
    }

    fun isSneezing(): Boolean {
        return getMaskBit(OFFSET + 5, SNEEZING_BIT)
    }

    fun setSneezing(value: Boolean) {
        setMaskBit(OFFSET + 5, SNEEZING_BIT, value)
    }

    fun isRolling(): Boolean {
        return getMaskBit(OFFSET + 5, ROLLING_BIT)
    }

    fun setRolling(value: Boolean) {
        setMaskBit(OFFSET + 5, ROLLING_BIT, value)
    }

    fun isSitting(): Boolean {
        return getMaskBit(OFFSET + 5, SITTING_BIT)
    }

    fun setSitting(value: Boolean) {
        setMaskBit(OFFSET + 5, SITTING_BIT, value)
    }

    fun isOnBack(): Boolean {
        return getMaskBit(OFFSET + 5, ON_BACK_BIT)
    }

    fun setOnBack(value: Boolean) {
        setMaskBit(OFFSET + 5, ON_BACK_BIT, value)
    }

    enum class Gene {
        NORMAL, AGGRESSIVE, LAZY, WORRIED, PLAYFUL, WEAK, BROWN;

        companion object {
            private val VALUES = values()
        }
    }

    companion object {
        val OFFSET: Byte = AnimalMeta.Companion.MAX_OFFSET
        val MAX_OFFSET = (OFFSET + 6).toByte()
        private const val SNEEZING_BIT: Byte = 0x02
        private const val ROLLING_BIT: Byte = 0x04
        private const val SITTING_BIT: Byte = 0x08
        private const val ON_BACK_BIT: Byte = 0x10
    }
}