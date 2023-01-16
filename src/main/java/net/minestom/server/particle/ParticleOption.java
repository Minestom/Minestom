package net.minestom.server.particle;

import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Point;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.particle.ParticleOption.*;

public sealed interface ParticleOption extends Writeable permits AmbientEntityEffect, AngryVillager, Ash, Block, BlockMarker, Bubble, BubbleColumnUp, BubblePop, CampfireCosySmoke, CampfireSignalSmoke, Cloud, Composter, CrimsonSpore, Crit, CurrentDown, DamageIndicator, Dolphin, DragonBreath, DrippingDripstoneLava, DrippingDripstoneWater, DrippingHoney, DrippingLava, DrippingObsidianTear, DrippingWater, Dust, DustColorTransition, Effect, ElderGuardian, ElectricSpark, Enchant, EnchantedHit, EndRod, EntityEffect, Explosion, ExplosionEmitter, FallingDripstoneLava, FallingDripstoneWater, FallingDust, FallingHoney, FallingLava, FallingNectar, FallingObsidianTear, FallingSporeBlossom, FallingWater, Firework, Fishing, Flame, Flash, Glow, GlowSquidInk, HappyVillager, Heart, InstantEffect, Item, ItemSlime, ItemSnowball, LandingHoney, LandingLava, LandingObsidianTear, LargeSmoke, Lava, Mycelium, Nautilus, Note, Poof, Portal, Rain, ReversePortal, Scrape, SculkCharge, SculkChargePop, SculkSoul, Shriek, SmallFlame, Smoke, Sneeze, Snowflake, SonicBoom, Soul, SoulFireFlame, Spit, Splash, SporeBlossomAir, SquidInk, SweepAttack, TotemOfUndying, Underwater, Vibration, WarpedSpore, WaxOff, WaxOn, WhiteAsh, Witch {

    Particle type();

    default void write(@NotNull BinaryWriter writer) {

    }

    record AmbientEntityEffect() implements ParticleOption {
        static final AmbientEntityEffect INSTANCE = new AmbientEntityEffect();

        @Override
        public Particle type() {
            return Particle.AMBIENT_ENTITY_EFFECT;
        }
    }

    record AngryVillager() implements ParticleOption {
        static final AngryVillager INSTANCE = new AngryVillager();

        @Override
        public Particle type() {
            return Particle.ANGRY_VILLAGER;
        }
    }

    record Block(net.minestom.server.instance.block.Block block) implements ParticleOption {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(block.id());
        }

        @Override
        public Particle type() {
            return Particle.BLOCK;
        }
    }

    record BlockMarker(net.minestom.server.instance.block.Block block) implements ParticleOption {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(block.id());
        }

        @Override
        public Particle type() {
            return Particle.BLOCK_MARKER;
        }
    }

    record Bubble() implements ParticleOption {
        static final Bubble INSTANCE = new Bubble();

        @Override
        public Particle type() {
            return Particle.BUBBLE;
        }
    }

    record Cloud() implements ParticleOption {
        static final Cloud INSTANCE = new Cloud();

        @Override
        public Particle type() {
            return Particle.CLOUD;
        }
    }

    record Crit() implements ParticleOption {
        static final Crit INSTANCE = new Crit();

        @Override
        public Particle type() {
            return Particle.CRIT;
        }
    }

    record DamageIndicator() implements ParticleOption {
        static final DamageIndicator INSTANCE = new DamageIndicator();

        @Override
        public Particle type() {
            return Particle.DAMAGE_INDICATOR;
        }
    }

    record DragonBreath() implements ParticleOption {
        static final DragonBreath INSTANCE = new DragonBreath();

        @Override
        public Particle type() {
            return Particle.DRAGON_BREATH;
        }
    }

    record DrippingLava() implements ParticleOption {
        static final DrippingLava INSTANCE = new DrippingLava();

        @Override
        public Particle type() {
            return Particle.DRIPPING_LAVA;
        }
    }

    record FallingLava() implements ParticleOption {
        static final FallingLava INSTANCE = new FallingLava();

        @Override
        public Particle type() {
            return Particle.FALLING_LAVA;
        }
    }

    record LandingLava() implements ParticleOption {
        static final LandingLava INSTANCE = new LandingLava();

        @Override
        public Particle type() {
            return Particle.LANDING_LAVA;
        }
    }

    record DrippingWater() implements ParticleOption {
        static final DrippingWater INSTANCE = new DrippingWater();

        @Override
        public Particle type() {
            return Particle.DRIPPING_WATER;
        }
    }

    record FallingWater() implements ParticleOption {
        static final FallingWater INSTANCE = new FallingWater();

        @Override
        public Particle type() {
            return Particle.FALLING_WATER;
        }
    }

    record Dust(@NotNull Color color, float scale) implements ParticleOption {

        @Override
        public Particle type() {
            return Particle.DUST;
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeColor(color);
            writer.writeFloat(MathUtils.clamp(scale, 0.01f, 4f));
        }
    }

    record DustColorTransition(@NotNull Color from, @NotNull Color to, float scale) implements ParticleOption {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeColor(from);
            writer.writeFloat(MathUtils.clamp(scale, 0.01f, 4f));
            writer.writeColor(to);
        }

        @Override
        public Particle type() {
            return Particle.DUST_COLOR_TRANSITION;
        }
    }

    record Effect() implements ParticleOption {
        static final Effect INSTANCE = new Effect();

        @Override
        public Particle type() {
            return Particle.EFFECT;
        }
    }

    record ElderGuardian() implements ParticleOption {
        static final ElderGuardian INSTANCE = new ElderGuardian();

        @Override
        public Particle type() {
            return Particle.ELDER_GUARDIAN;
        }
    }

    record EnchantedHit() implements ParticleOption {
        static final EnchantedHit INSTANCE = new EnchantedHit();

        @Override
        public Particle type() {
            return Particle.ENCHANTED_HIT;
        }
    }

    record Enchant() implements ParticleOption {
        static final Enchant INSTANCE = new Enchant();

        @Override
        public Particle type() {
            return Particle.ENCHANT;
        }
    }

    record EndRod() implements ParticleOption {
        static final EndRod INSTANCE = new EndRod();

        @Override
        public Particle type() {
            return Particle.END_ROD;
        }
    }

    record EntityEffect() implements ParticleOption {
        static final EntityEffect INSTANCE = new EntityEffect();

        @Override
        public Particle type() {
            return Particle.ENTITY_EFFECT;
        }
    }

    record ExplosionEmitter() implements ParticleOption {
        static final ExplosionEmitter INSTANCE = new ExplosionEmitter();

        @Override
        public Particle type() {
            return Particle.EXPLOSION_EMITTER;
        }
    }

    record Explosion() implements ParticleOption {
        static final Explosion INSTANCE = new Explosion();

        @Override
        public Particle type() {
            return Particle.EXPLOSION;
        }
    }

    record SonicBoom() implements ParticleOption {
        static final SonicBoom INSTANCE = new SonicBoom();

        @Override
        public Particle type() {
            return Particle.SONIC_BOOM;
        }
    }

    record FallingDust(@NotNull net.minestom.server.instance.block.Block block) implements ParticleOption {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(block.id());
        }

        @Override
        public Particle type() {
            return Particle.FALLING_DUST;
        }
    }

    record Firework() implements ParticleOption {
        static final Firework INSTANCE = new Firework();

        @Override
        public Particle type() {
            return Particle.FIREWORK;
        }
    }

    record Fishing() implements ParticleOption {
        static final Fishing INSTANCE = new Fishing();

        @Override
        public Particle type() {
            return Particle.FISHING;
        }
    }

    record Flame() implements ParticleOption {
        static final Flame INSTANCE = new Flame();

        @Override
        public Particle type() {
            return Particle.FLAME;
        }
    }

    record SculkSoul() implements ParticleOption {
        static final SculkSoul INSTANCE = new SculkSoul();

        @Override
        public Particle type() {
            return Particle.SCULK_SOUL;
        }
    }

    record SculkCharge(float angle) implements ParticleOption {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeFloat(angle);
        }

        @Override
        public Particle type() {
            return Particle.SCULK_CHARGE;
        }
    }

    record SculkChargePop() implements ParticleOption {
        static final SculkChargePop INSTANCE = new SculkChargePop();

        @Override
        public Particle type() {
            return Particle.SCULK_CHARGE_POP;
        }
    }

    record SoulFireFlame() implements ParticleOption {
        static final SoulFireFlame INSTANCE = new SoulFireFlame();

        @Override
        public Particle type() {
            return Particle.SOUL_FIRE_FLAME;
        }
    }

    record Soul() implements ParticleOption {
        static final Soul INSTANCE = new Soul();

        @Override
        public Particle type() {
            return Particle.SOUL;
        }
    }

    record Flash() implements ParticleOption {
        static final Flash INSTANCE = new Flash();

        @Override
        public Particle type() {
            return Particle.FLASH;
        }
    }

    record HappyVillager() implements ParticleOption {
        static final HappyVillager INSTANCE = new HappyVillager();

        @Override
        public Particle type() {
            return Particle.HAPPY_VILLAGER;
        }
    }

    record Composter() implements ParticleOption {
        static final Composter INSTANCE = new Composter();

        @Override
        public Particle type() {
            return Particle.COMPOSTER;
        }
    }

    record Heart() implements ParticleOption {
        static final Heart INSTANCE = new Heart();

        @Override
        public Particle type() {
            return Particle.HEART;
        }
    }

    record InstantEffect() implements ParticleOption {
        static final InstantEffect INSTANCE = new InstantEffect();

        @Override
        public Particle type() {
            return Particle.INSTANT_EFFECT;
        }
    }

    record Item(@NotNull ItemStack item) implements ParticleOption {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeItemStack(item);
        }

        @Override
        public Particle type() {
            return Particle.ITEM;
        }
    }

    record Vibration(Target target, int ticks) implements ParticleOption {

        private sealed interface Target extends Writeable permits Block, Entity {
        }

        public record Block(@NotNull Point blockPosition) implements Target {

            @Override
            public void write(@NotNull BinaryWriter writer) {
                writer.writeSizedString("minecraft:block");
                writer.writeBlockPosition(blockPosition);
            }
        }

        public record Entity(int entityId, float eyeHeight) implements Target {

            public Entity(@NotNull net.minestom.server.entity.Entity e) {
                this(e.getEntityId(), (float) e.getEyeHeight());
            }

            @Override
            public void write(@NotNull BinaryWriter writer) {
                writer.writeSizedString("minecraft:entity");
                writer.writeVarInt(entityId);
                writer.writeFloat(eyeHeight);
            }
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            target.write(writer);
            writer.writeVarInt(ticks);
        }

        @Override
        public Particle type() {
            return Particle.VIBRATION;
        }
    }

    record ItemSlime() implements ParticleOption {
        static final ItemSlime INSTANCE = new ItemSlime();

        @Override
        public Particle type() {
            return Particle.ITEM_SLIME;
        }
    }

    record ItemSnowball() implements ParticleOption {
        static final ItemSnowball INSTANCE = new ItemSnowball();

        @Override
        public Particle type() {
            return Particle.ITEM_SNOWBALL;
        }
    }

    record LargeSmoke() implements ParticleOption {
        static final LargeSmoke INSTANCE = new LargeSmoke();

        @Override
        public Particle type() {
            return Particle.LARGE_SMOKE;
        }
    }

    record Lava() implements ParticleOption {
        static final Lava INSTANCE = new Lava();

        @Override
        public Particle type() {
            return Particle.LAVA;
        }
    }

    record Mycelium() implements ParticleOption {
        static final Mycelium INSTANCE = new Mycelium();

        @Override
        public Particle type() {
            return Particle.MYCELIUM;
        }
    }

    record Note() implements ParticleOption {
        static final Note INSTANCE = new Note();

        @Override
        public Particle type() {
            return Particle.NOTE;
        }
    }

    record Poof() implements ParticleOption {
        static final Poof INSTANCE = new Poof();

        @Override
        public Particle type() {
            return Particle.POOF;
        }
    }

    record Portal() implements ParticleOption {
        static final Portal INSTANCE = new Portal();

        @Override
        public Particle type() {
            return Particle.PORTAL;
        }
    }

    record Rain() implements ParticleOption {
        static final Rain INSTANCE = new Rain();

        @Override
        public Particle type() {
            return Particle.RAIN;
        }
    }

    record Smoke() implements ParticleOption {
        static final Smoke INSTANCE = new Smoke();

        @Override
        public Particle type() {
            return Particle.SMOKE;
        }
    }

    record Sneeze() implements ParticleOption {
        static final Sneeze INSTANCE = new Sneeze();

        @Override
        public Particle type() {
            return Particle.SNEEZE;
        }
    }

    record Spit() implements ParticleOption {
        static final Spit INSTANCE = new Spit();

        @Override
        public Particle type() {
            return Particle.SPIT;
        }
    }

    record SquidInk() implements ParticleOption {
        static final SquidInk INSTANCE = new SquidInk();

        @Override
        public Particle type() {
            return Particle.SQUID_INK;
        }
    }

    record SweepAttack() implements ParticleOption {
        static final SweepAttack INSTANCE = new SweepAttack();

        @Override
        public Particle type() {
            return Particle.SWEEP_ATTACK;
        }
    }

    record TotemOfUndying() implements ParticleOption {
        static final TotemOfUndying INSTANCE = new TotemOfUndying();

        @Override
        public Particle type() {
            return Particle.TOTEM_OF_UNDYING;
        }
    }

    record Underwater() implements ParticleOption {
        static final Underwater INSTANCE = new Underwater();

        @Override
        public Particle type() {
            return Particle.UNDERWATER;
        }
    }

    record Splash() implements ParticleOption {
        static final Splash INSTANCE = new Splash();

        @Override
        public Particle type() {
            return Particle.SPLASH;
        }
    }

    record Witch() implements ParticleOption {
        static final Witch INSTANCE = new Witch();

        @Override
        public Particle type() {
            return Particle.WITCH;
        }
    }

    record BubblePop() implements ParticleOption {
        static final BubblePop INSTANCE = new BubblePop();

        @Override
        public Particle type() {
            return Particle.BUBBLE_POP;
        }
    }

    record CurrentDown() implements ParticleOption {
        static final CurrentDown INSTANCE = new CurrentDown();

        @Override
        public Particle type() {
            return Particle.CURRENT_DOWN;
        }
    }

    record BubbleColumnUp() implements ParticleOption {
        static final BubbleColumnUp INSTANCE = new BubbleColumnUp();

        @Override
        public Particle type() {
            return Particle.BUBBLE_COLUMN_UP;
        }
    }

    record Nautilus() implements ParticleOption {
        static final Nautilus INSTANCE = new Nautilus();

        @Override
        public Particle type() {
            return Particle.NAUTILUS;
        }
    }

    record Dolphin() implements ParticleOption {
        static final Dolphin INSTANCE = new Dolphin();

        @Override
        public Particle type() {
            return Particle.DOLPHIN;
        }
    }

    record CampfireCosySmoke() implements ParticleOption {
        static final CampfireCosySmoke INSTANCE = new CampfireCosySmoke();

        @Override
        public Particle type() {
            return Particle.CAMPFIRE_COSY_SMOKE;
        }
    }

    record CampfireSignalSmoke() implements ParticleOption {
        static final CampfireSignalSmoke INSTANCE = new CampfireSignalSmoke();

        @Override
        public Particle type() {
            return Particle.CAMPFIRE_SIGNAL_SMOKE;
        }
    }

    record DrippingHoney() implements ParticleOption {
        static final DrippingHoney INSTANCE = new DrippingHoney();

        @Override
        public Particle type() {
            return Particle.DRIPPING_HONEY;
        }
    }

    record FallingHoney() implements ParticleOption {
        static final FallingHoney INSTANCE = new FallingHoney();

        @Override
        public Particle type() {
            return Particle.FALLING_HONEY;
        }
    }

    record LandingHoney() implements ParticleOption {
        static final LandingHoney INSTANCE = new LandingHoney();

        @Override
        public Particle type() {
            return Particle.LANDING_HONEY;
        }
    }

    record FallingNectar() implements ParticleOption {
        static final FallingNectar INSTANCE = new FallingNectar();

        @Override
        public Particle type() {
            return Particle.FALLING_NECTAR;
        }
    }

    record FallingSporeBlossom() implements ParticleOption {
        static final FallingSporeBlossom INSTANCE = new FallingSporeBlossom();

        @Override
        public Particle type() {
            return Particle.FALLING_SPORE_BLOSSOM;
        }
    }

    record Ash() implements ParticleOption {
        static final Ash INSTANCE = new Ash();

        @Override
        public Particle type() {
            return Particle.ASH;
        }
    }

    record CrimsonSpore() implements ParticleOption {
        static final CrimsonSpore INSTANCE = new CrimsonSpore();

        @Override
        public Particle type() {
            return Particle.CRIMSON_SPORE;
        }
    }

    record WarpedSpore() implements ParticleOption {
        static final WarpedSpore INSTANCE = new WarpedSpore();

        @Override
        public Particle type() {
            return Particle.WARPED_SPORE;
        }
    }

    record SporeBlossomAir() implements ParticleOption {
        static final SporeBlossomAir INSTANCE = new SporeBlossomAir();

        @Override
        public Particle type() {
            return Particle.SPORE_BLOSSOM_AIR;
        }
    }

    record DrippingObsidianTear() implements ParticleOption {
        static final DrippingObsidianTear INSTANCE = new DrippingObsidianTear();

        @Override
        public Particle type() {
            return Particle.DRIPPING_OBSIDIAN_TEAR;
        }
    }

    record FallingObsidianTear() implements ParticleOption {
        static final FallingObsidianTear INSTANCE = new FallingObsidianTear();

        @Override
        public Particle type() {
            return Particle.FALLING_OBSIDIAN_TEAR;
        }
    }

    record LandingObsidianTear() implements ParticleOption {
        static final LandingObsidianTear INSTANCE = new LandingObsidianTear();

        @Override
        public Particle type() {
            return Particle.LANDING_OBSIDIAN_TEAR;
        }
    }

    record ReversePortal() implements ParticleOption {
        static final ReversePortal INSTANCE = new ReversePortal();

        @Override
        public Particle type() {
            return Particle.REVERSE_PORTAL;
        }
    }

    record WhiteAsh() implements ParticleOption {
        static final WhiteAsh INSTANCE = new WhiteAsh();

        @Override
        public Particle type() {
            return Particle.WHITE_ASH;
        }
    }

    record SmallFlame() implements ParticleOption {
        static final SmallFlame INSTANCE = new SmallFlame();

        @Override
        public Particle type() {
            return Particle.SMALL_FLAME;
        }
    }

    record Snowflake() implements ParticleOption {
        static final Snowflake INSTANCE = new Snowflake();

        @Override
        public Particle type() {
            return Particle.SNOWFLAKE;
        }
    }

    record DrippingDripstoneLava() implements ParticleOption {
        static final DrippingDripstoneLava INSTANCE = new DrippingDripstoneLava();

        @Override
        public Particle type() {
            return Particle.DRIPPING_DRIPSTONE_LAVA;
        }
    }

    record FallingDripstoneLava() implements ParticleOption {
        static final FallingDripstoneLava INSTANCE = new FallingDripstoneLava();

        @Override
        public Particle type() {
            return Particle.FALLING_DRIPSTONE_LAVA;
        }
    }

    record DrippingDripstoneWater() implements ParticleOption {
        static final DrippingDripstoneWater INSTANCE = new DrippingDripstoneWater();

        @Override
        public Particle type() {
            return Particle.DRIPPING_DRIPSTONE_WATER;
        }
    }

    record FallingDripstoneWater() implements ParticleOption {
        static final FallingDripstoneWater INSTANCE = new FallingDripstoneWater();

        @Override
        public Particle type() {
            return Particle.FALLING_DRIPSTONE_WATER;
        }
    }

    record GlowSquidInk() implements ParticleOption {
        static final GlowSquidInk INSTANCE = new GlowSquidInk();

        @Override
        public Particle type() {
            return Particle.GLOW_SQUID_INK;
        }
    }

    record Glow() implements ParticleOption {
        static final Glow INSTANCE = new Glow();

        @Override
        public Particle type() {
            return Particle.GLOW;
        }
    }

    record WaxOn() implements ParticleOption {
        static final WaxOn INSTANCE = new WaxOn();

        @Override
        public Particle type() {
            return Particle.WAX_ON;
        }
    }

    record WaxOff() implements ParticleOption {
        static final WaxOff INSTANCE = new WaxOff();

        @Override
        public Particle type() {
            return Particle.WAX_OFF;
        }
    }

    record ElectricSpark() implements ParticleOption {
        static final ElectricSpark INSTANCE = new ElectricSpark();

        @Override
        public Particle type() {
            return Particle.ELECTRIC_SPARK;
        }
    }

    record Scrape() implements ParticleOption {
        static final Scrape INSTANCE = new Scrape();

        @Override
        public Particle type() {
            return Particle.SCRAPE;
        }
    }

    record Shriek(int ticks) implements ParticleOption {

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(ticks);
        }

        @Override
        public Particle type() {
            return Particle.SHRIEK;
        }
    }
}