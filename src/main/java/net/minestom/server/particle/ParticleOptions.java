package net.minestom.server.particle;

import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.particle.ParticleOption.*;

public interface ParticleOptions {

    static ParticleOption AmbientEntityEffect() {
        return AmbientEntityEffect.INSTANCE;
    }

    static ParticleOption AngryVillager() {
        return AngryVillager.INSTANCE;
    }

    static ParticleOption Block(@NotNull Block block) {
        return new ParticleOption.Block(block);
    }

    static ParticleOption BlockMarker(@NotNull Block block) {
        return new BlockMarker(block);
    }

    static ParticleOption Bubble() {
        return Bubble.INSTANCE;
    }

    static ParticleOption Cloud() {
        return Cloud.INSTANCE;
    }

    static ParticleOption Crit() {
        return Crit.INSTANCE;
    }

    static ParticleOption DamageIndicator() {
        return DamageIndicator.INSTANCE;
    }

    static ParticleOption DragonBreath() {
        return DragonBreath.INSTANCE;
    }

    static ParticleOption DrippingLava() {
        return DrippingLava.INSTANCE;
    }

    static ParticleOption FallingLava() {
        return FallingLava.INSTANCE;
    }

    static ParticleOption LandingLava() {
        return LandingLava.INSTANCE;
    }

    static ParticleOption DrippingWater() {
        return DrippingWater.INSTANCE;
    }

    static ParticleOption FallingWater() {
        return FallingWater.INSTANCE;
    }

    static ParticleOption Dust(@NotNull Color color, float scale) {
        return new Dust(color, scale);
    }

    static ParticleOption DustColorTransition(@NotNull Color from, @NotNull Color to, float scale) {
        return new DustColorTransition(from, to, scale);
    }

    static ParticleOption Effect() {
        return Effect.INSTANCE;
    }

    static ParticleOption ElderGuardian() {
        return ElderGuardian.INSTANCE;
    }

    static ParticleOption EnchantedHit() {
        return EnchantedHit.INSTANCE;
    }

    static ParticleOption Enchant() {
        return Enchant.INSTANCE;
    }

    static ParticleOption EndRod() {
        return EndRod.INSTANCE;
    }

    static ParticleOption EntityEffect() {
        return EntityEffect.INSTANCE;
    }

    static ParticleOption ExplosionEmitter() {
        return ExplosionEmitter.INSTANCE;
    }

    static ParticleOption Explosion() {
        return Explosion.INSTANCE;
    }

    static ParticleOption SonicBoom() {
        return SonicBoom.INSTANCE;
    }

    static ParticleOption FallingDust(@NotNull Block block) {
        return new FallingDust(block);
    }

    static ParticleOption Firework() {
        return Firework.INSTANCE;
    }

    static ParticleOption Fishing() {
        return Fishing.INSTANCE;
    }

    static ParticleOption Flame() {
        return Flame.INSTANCE;
    }

    static ParticleOption SculkSoul() {
        return SculkSoul.INSTANCE;
    }

    static ParticleOption SculkCharge(float angle) {
        return new SculkCharge(angle);
    }

    static ParticleOption SculkChargePop() {
        return SculkChargePop.INSTANCE;
    }

    static ParticleOption SoulFireFlame() {
        return SoulFireFlame.INSTANCE;
    }

    static ParticleOption Soul() {
        return Soul.INSTANCE;
    }

    static ParticleOption Flash() {
        return Flash.INSTANCE;
    }

    static ParticleOption HappyVillager() {
        return HappyVillager.INSTANCE;
    }

    static ParticleOption Composter() {
        return Composter.INSTANCE;
    }

    static ParticleOption Heart() {
        return Heart.INSTANCE;
    }

    static ParticleOption InstantEffect() {
        return InstantEffect.INSTANCE;
    }

    static ParticleOption Item(@NotNull ItemStack item) {
        return new Item(item);
    }

    static ParticleOption VibrationForEntity(@NotNull Entity entity, int ticks) {
        Vibration.Entity target = new Vibration.Entity(entity);
        return new Vibration(target, ticks);
    }

    static ParticleOption VibrationForBlock(@NotNull Point position, int ticks) {
        Vibration.Block target = new Vibration.Block(position);
        return new Vibration(target, ticks);
    }

    static ParticleOption ItemSlime() {
        return ItemSlime.INSTANCE;
    }

    static ParticleOption ItemSnowball() {
        return ItemSnowball.INSTANCE;
    }

    static ParticleOption LargeSmoke() {
        return LargeSmoke.INSTANCE;
    }

    static ParticleOption Lava() {
        return Lava.INSTANCE;
    }

    static ParticleOption Mycelium() {
        return Mycelium.INSTANCE;
    }

    static ParticleOption Note() {
        return Note.INSTANCE;
    }

    static ParticleOption Poof() {
        return Poof.INSTANCE;
    }

    static ParticleOption Portal() {
        return Portal.INSTANCE;
    }

    static ParticleOption Rain() {
        return Rain.INSTANCE;
    }

    static ParticleOption Smoke() {
        return Smoke.INSTANCE;
    }

    static ParticleOption Sneeze() {
        return Sneeze.INSTANCE;
    }

    static ParticleOption Spit() {
        return Spit.INSTANCE;
    }

    static ParticleOption SquidInk() {
        return SquidInk.INSTANCE;
    }

    static ParticleOption SweepAttack() {
        return SweepAttack.INSTANCE;
    }

    static ParticleOption TotemOfUndying() {
        return TotemOfUndying.INSTANCE;
    }

    static ParticleOption Underwater() {
        return Underwater.INSTANCE;
    }

    static ParticleOption Splash() {
        return Splash.INSTANCE;
    }

    static ParticleOption Witch() {
        return Witch.INSTANCE;
    }

    static ParticleOption BubblePop() {
        return BubblePop.INSTANCE;
    }

    static ParticleOption CurrentDown() {
        return CurrentDown.INSTANCE;
    }

    static ParticleOption BubbleColumnUp() {
        return BubbleColumnUp.INSTANCE;
    }

    static ParticleOption Nautilus() {
        return Nautilus.INSTANCE;
    }

    static ParticleOption Dolphin() {
        return Dolphin.INSTANCE;
    }

    static ParticleOption CampfireCosySmoke() {
        return CampfireCosySmoke.INSTANCE;
    }

    static ParticleOption CampfireSignalSmoke() {
        return CampfireSignalSmoke.INSTANCE;
    }

    static ParticleOption DrippingHoney() {
        return DrippingHoney.INSTANCE;
    }

    static ParticleOption FallingHoney() {
        return FallingHoney.INSTANCE;
    }

    static ParticleOption LandingHoney() {
        return LandingHoney.INSTANCE;
    }

    static ParticleOption FallingNectar() {
        return FallingNectar.INSTANCE;
    }

    static ParticleOption FallingSporeBlossom() {
        return FallingSporeBlossom.INSTANCE;
    }

    static ParticleOption Ash() {
        return Ash.INSTANCE;
    }

    static ParticleOption CrimsonSpore() {
        return CrimsonSpore.INSTANCE;
    }

    static ParticleOption WarpedSpore() {
        return WarpedSpore.INSTANCE;
    }

    static ParticleOption SporeBlossomAir() {
        return SporeBlossomAir.INSTANCE;
    }

    static ParticleOption DrippingObsidianTear() {
        return DrippingObsidianTear.INSTANCE;
    }

    static ParticleOption FallingObsidianTear() {
        return FallingObsidianTear.INSTANCE;
    }

    static ParticleOption LandingObsidianTear() {
        return LandingObsidianTear.INSTANCE;
    }

    static ParticleOption ReversePortal() {
        return ReversePortal.INSTANCE;
    }

    static ParticleOption WhiteAsh() {
        return WhiteAsh.INSTANCE;
    }

    static ParticleOption SmallFlame() {
        return SmallFlame.INSTANCE;
    }

    static ParticleOption Snowflake() {
        return Snowflake.INSTANCE;
    }

    static ParticleOption DrippingDripstoneLava() {
        return DrippingDripstoneLava.INSTANCE;
    }

    static ParticleOption FallingDripstoneLava() {
        return FallingDripstoneLava.INSTANCE;
    }

    static ParticleOption DrippingDripstoneWater() {
        return DrippingDripstoneWater.INSTANCE;
    }

    static ParticleOption FallingDripstoneWater() {
        return FallingDripstoneWater.INSTANCE;
    }

    static ParticleOption GlowSquidInk() {
        return GlowSquidInk.INSTANCE;
    }

    static ParticleOption Glow() {
        return Glow.INSTANCE;
    }

    static ParticleOption WaxOn() {
        return WaxOn.INSTANCE;
    }

    static ParticleOption WaxOff() {
        return WaxOff.INSTANCE;
    }

    static ParticleOption ElectricSpark() {
        return ElectricSpark.INSTANCE;
    }

    static ParticleOption Scrape() {
        return Scrape.INSTANCE;
    }

    static ParticleOption Shriek(int ticks) {
        return new Shriek(ticks);
    }
}