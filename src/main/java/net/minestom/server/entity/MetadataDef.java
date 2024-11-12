package net.minestom.server.entity;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.animal.ArmadilloMeta;
import net.minestom.server.entity.metadata.animal.FrogMeta;
import net.minestom.server.entity.metadata.animal.SnifferMeta;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static net.minestom.server.entity.MetadataDefImpl.*;

/**
 * List of all entity metadata.
 * <p>
 * Classes must be used (and not interfaces) to enforce loading order.
 */
@SuppressWarnings({"unused", "SpellCheckingInspection"})
public sealed class MetadataDef {
    public static final Entry<Byte> ENTITY_FLAGS = index(0, Metadata::Byte, (byte) 0);
    public static final Entry<Boolean> IS_ON_FIRE = bitMask(0, (byte) 0x01, false);
    public static final Entry<Boolean> IS_CROUCHING = bitMask(0, (byte) 0x02, false);
    public static final Entry<Boolean> UNUSED_RIDING = bitMask(0, (byte) 0x04, false);
    public static final Entry<Boolean> IS_SPRINTING = bitMask(0, (byte) 0x08, false);
    public static final Entry<Boolean> IS_SWIMMING = bitMask(0, (byte) 0x10, false);
    public static final Entry<Boolean> IS_INVISIBLE = bitMask(0, (byte) 0x20, false);
    public static final Entry<Boolean> HAS_GLOWING_EFFECT = bitMask(0, (byte) 0x40, false);
    public static final Entry<Boolean> IS_FLYING_WITH_ELYTRA = bitMask(0, (byte) 0x80, false);
    public static final Entry<Integer> AIR_TICKS = index(1, Metadata::VarInt, 300);
    public static final Entry<@Nullable Component> CUSTOM_NAME = index(2, Metadata::OptChat, null);
    public static final Entry<Boolean> CUSTOM_NAME_VISIBLE = index(3, Metadata::Boolean, false);
    public static final Entry<Boolean> IS_SILENT = index(4, Metadata::Boolean, false);
    public static final Entry<Boolean> HAS_NO_GRAVITY = index(5, Metadata::Boolean, false);
    public static final Entry<EntityPose> POSE = index(6, Metadata::Pose, EntityPose.STANDING);
    public static final Entry<Integer> TICKS_FROZEN = index(7, Metadata::VarInt, 0);

    public static final class Interaction extends MetadataDef {
        public static final Entry<Float> WIDTH = index(0, Metadata::Float, 1f);
        public static final Entry<Float> HEIGHT = index(1, Metadata::Float, 1f);
        public static final Entry<Boolean> RESPONSIVE = index(2, Metadata::Boolean, false);
    }

    public static sealed class Display extends MetadataDef {
        public static final Entry<Integer> INTERPOLATION_DELAY = index(0, Metadata::VarInt, 0);
        public static final Entry<Integer> TRANSFORMATION_INTERPOLATION_DURATION = index(1, Metadata::VarInt, 0);
        public static final Entry<Integer> POSITION_ROTATION_INTERPOLATION_DURATION = index(2, Metadata::VarInt, 0);
        public static final Entry<Point> TRANSLATION = index(3, Metadata::Vector3, Vec.ZERO);
        public static final Entry<Point> SCALE = index(4, Metadata::Vector3, Vec.ONE);
        public static final Entry<float[]> ROTATION_LEFT = index(5, Metadata::Quaternion, new float[]{0, 0, 0, 1});
        public static final Entry<float[]> ROTATION_RIGHT = index(6, Metadata::Quaternion, new float[]{0, 0, 0, 1});
        public static final Entry<Byte> BILLBOARD_CONSTRAINTS = index(7, Metadata::Byte, (byte) 0);
        public static final Entry<Integer> BRIGHTNESS_OVERRIDE = index(8, Metadata::VarInt, -1);
        public static final Entry<Float> VIEW_RANGE = index(9, Metadata::Float, 1f);
        public static final Entry<Float> SHADOW_RADIUS = index(10, Metadata::Float, 0f);
        public static final Entry<Float> SHADOW_STRENGTH = index(11, Metadata::Float, 1f);
        public static final Entry<Float> WIDTH = index(12, Metadata::Float, 0f);
        public static final Entry<Float> HEIGHT = index(13, Metadata::Float, 0f);
        public static final Entry<Integer> GLOW_COLOR_OVERRIDE = index(14, Metadata::VarInt, -1);
    }

    public static final class BlockDisplay extends Display {
        public static final Entry<Block> DISPLAYED_BLOCK_STATE = index(0, Metadata::BlockState, Block.AIR);
    }

    public static final class ItemDisplay extends Display {
        public static final Entry<ItemStack> DISPLAYED_ITEM = index(0, Metadata::ItemStack, ItemStack.AIR);
        public static final Entry<Byte> DISPLAY_TYPE = index(1, Metadata::Byte, (byte) 0);
    }

    public static final class TextDisplay extends Display {
        public static final Entry<Component> TEXT = index(0, Metadata::Chat, Component.empty());
        public static final Entry<Integer> LINE_WIDTH = index(1, Metadata::VarInt, 200);
        public static final Entry<Integer> BACKGROUND_COLOR = index(2, Metadata::VarInt, 0x40000000);
        public static final Entry<Byte> TEXT_OPACITY = index(3, Metadata::Byte, (byte) -1);
        public static final Entry<Byte> TEXT_DISPLAY_FLAGS = index(4, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> HAS_SHADOW = bitMask(4, (byte) 0x01, false);
        public static final Entry<Boolean> IS_SEE_THROUGH = bitMask(4, (byte) 0x02, false);
        public static final Entry<Boolean> USE_DEFAULT_BACKGROUND_COLOR = bitMask(4, (byte) 0x04, false);
        public static final Entry<Byte> ALIGNMENT = byteMask(4, (byte) 0x18, 3, (byte) 0);
        public static final Entry<Boolean> ALIGN_LEFT = bitMask(4, (byte) 0x08, false);
        public static final Entry<Boolean> ALIGN_RIGHT = bitMask(4, (byte) 0x10, false);
    }

    public static final class ThrownItemProjectile extends MetadataDef {
        public static final Entry<ItemStack> ITEM = index(0, Metadata::ItemStack, ItemStack.AIR);
    }

    public static final class EyeOfEnder extends MetadataDef {
        public static final Entry<ItemStack> ITEM = index(0, Metadata::ItemStack, ItemStack.AIR);
    }

    public static final class FallingBlock extends MetadataDef {
        public static final Entry<Point> SPAWN_POSITION = index(0, Metadata::BlockPosition, Vec.ZERO);
    }

    public static final class AreaEffectCloud extends MetadataDef {
        public static final Entry<Float> RADIUS = index(0, Metadata::Float, 0.5f);
        public static final Entry<Integer> COLOR = index(1, Metadata::VarInt, 0);
        public static final Entry<Boolean> IGNORE_RADIUS_AND_SINGLE_POINT = index(2, Metadata::Boolean, false);
        public static final Entry<Particle> PARTICLE = index(3, Metadata::Particle, Particle.EFFECT);
    }

    public static final class FishingHook extends MetadataDef {
        public static final Entry<Integer> HOOKED = index(0, Metadata::VarInt, 0);
        public static final Entry<Boolean> IS_CATCHABLE = index(1, Metadata::Boolean, false);
    }

    public static sealed class AbstractArrow extends MetadataDef {
        public static final Entry<Byte> ARROW_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_CRITICAL = bitMask(0, (byte) 0x01, false);
        public static final Entry<Boolean> IS_NO_CLIP = bitMask(0, (byte) 0x02, false);
        public static final Entry<Byte> PIERCING_LEVEL = index(1, Metadata::Byte, (byte) 0);
    }

    public static final class Arrow extends AbstractArrow {
        public static final Entry<Integer> COLOR = index(0, Metadata::VarInt, -1);
    }

    public static final class ThrownTrident extends AbstractArrow {
        public static final Entry<Byte> LOYALTY_LEVEL = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> HAS_ENCHANTMENT_GLINT = index(1, Metadata::Boolean, false);
    }

    public static sealed class AbstractVehicle extends MetadataDef {
        public static final Entry<Integer> SHAKING_POWER = index(0, Metadata::VarInt, 0);
        public static final Entry<Integer> SHAKING_DIRECTION = index(1, Metadata::VarInt, 1);
        public static final Entry<Float> SHAKING_MULTIPLIER = index(2, Metadata::Float, 0f);
    }

    public static final class Boat extends AbstractVehicle {
        public static final Entry<Integer> TYPE = index(0, Metadata::VarInt, 0);
        public static final Entry<Boolean> IS_LEFT_PADDLE_TURNING = index(1, Metadata::Boolean, false);
        public static final Entry<Boolean> IS_RIGHT_PADDLE_TURNING = index(2, Metadata::Boolean, false);
        public static final Entry<Integer> SPLASH_TIMER = index(3, Metadata::VarInt, 0);
    }

    public static sealed class AbstractMinecart extends AbstractVehicle {
        public static final Entry<Integer> CUSTOM_BLOCK_ID_AND_DAMAGE = index(0, Metadata::VarInt, 0);
        public static final Entry<Integer> CUSTOM_BLOCK_Y_POSITION = index(1, Metadata::VarInt, 6);
        public static final Entry<Boolean> SHOW_CUSTOM_BLOCK = index(2, Metadata::Boolean, false);
    }

    public static final class MinecartFurnace extends AbstractMinecart {
        public static final Entry<Boolean> HAS_FUEL = index(0, Metadata::Boolean, false);
    }

    public static final class MinecartCommandBlock extends AbstractMinecart {
        public static final Entry<String> COMMAND = index(0, Metadata::String, "false");
        public static final Entry<Component> LAST_OUTPUT = index(1, Metadata::Chat, Component.empty());
    }

    public static final class EndCrystal extends MetadataDef {
        public static final Entry<@Nullable Point> BEAM_TARGET = index(0, Metadata::OptBlockPosition, null);
        public static final Entry<Boolean> SHOW_BOTTOM = index(1, Metadata::Boolean, true);
    }

    public static final class SmartFireball extends MetadataDef {
        public static final Entry<ItemStack> ITEM = index(0, Metadata::ItemStack, ItemStack.AIR);
    }

    public static final class Fireball extends MetadataDef {
        public static final Entry<ItemStack> ITEM = index(0, Metadata::ItemStack, ItemStack.AIR);
    }

    public static final class WitherSkull extends MetadataDef {
        public static final Entry<Boolean> IS_INVULNERABLE = index(0, Metadata::Boolean, false);
    }

    public static final class FireworkRocketEntity extends MetadataDef {
        public static final Entry<ItemStack> ITEM = index(0, Metadata::ItemStack, ItemStack.AIR);
        public static final Entry<@Nullable Integer> SHOOTER_ENTITY_ID = index(1, Metadata::OptVarInt, null);
        public static final Entry<Boolean> IS_SHOT_AT_ANGLE = index(2, Metadata::Boolean, false);
    }

    public static final class ItemFrame extends MetadataDef {
        public static final Entry<ItemStack> ITEM = index(0, Metadata::ItemStack, ItemStack.AIR);
        public static final Entry<Integer> ROTATION = index(0, Metadata::VarInt, 0);
    }

    public static final class Painting extends MetadataDef {
        public static final Entry<DynamicRegistry.Key<PaintingMeta.Variant>> VARIANT = index(0, Metadata::PaintingVariant, PaintingMeta.Variant.KEBAB);
    }

    public static final class ItemEntity extends MetadataDef {
        public static final Entry<ItemStack> ITEM = index(0, Metadata::ItemStack, ItemStack.AIR);
    }

    public static sealed class LivingEntity extends MetadataDef {
        public static final Entry<Byte> LIVING_ENTITY_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_HAND_ACTIVE = bitMask(0, (byte) 0x01, false);
        public static final Entry<Boolean> ACTIVE_HAND = bitMask(0, (byte) 0x02, false);
        public static final Entry<Boolean> IS_RIPTIDE_SPIN_ATTACK = bitMask(0, (byte) 0x04, false);
        public static final Entry<Float> HEALTH = index(1, Metadata::Float, 1f);
        public static final Entry<List<Particle>> POTION_EFFECT_PARTICLES = index(2, Metadata::ParticleList, List.of());
        public static final Entry<Boolean> IS_POTION_EFFECT_AMBIANT = index(3, Metadata::Boolean, false);
        public static final Entry<Integer> NUMBER_OF_ARROWS = index(4, Metadata::VarInt, 0);
        public static final Entry<Integer> NUMBER_OF_BEE_STINGERS = index(5, Metadata::VarInt, 0);
        public static final Entry<@Nullable Point> LOCATION_OF_BED = index(6, Metadata::OptBlockPosition, null);
    }

    public static final class Player extends LivingEntity {
        public static final Entry<Float> ADDITIONAL_HEARTS = index(0, Metadata::Float, 1f);
        public static final Entry<Integer> SCORE = index(1, Metadata::VarInt, 0);
        public static final Entry<Byte> DISPLAYED_SKIN_PARTS_FLAGS = index(2, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_CAPE_ENABLED = bitMask(2, (byte) 0x01, false);
        public static final Entry<Boolean> IS_JACKET_ENABLED = bitMask(2, (byte) 0x02, false);
        public static final Entry<Boolean> IS_LEFT_SLEEVE_ENABLED = bitMask(2, (byte) 0x04, false);
        public static final Entry<Boolean> IS_RIGHT_SLEEVE_ENABLED = bitMask(2, (byte) 0x08, false);
        public static final Entry<Boolean> IS_LEFT_PANTS_LEG_ENABLED = bitMask(2, (byte) 0x10, false);
        public static final Entry<Boolean> IS_RIGHT_PANTS_LEG_ENABLED = bitMask(2, (byte) 0x20, false);
        public static final Entry<Boolean> IS_HAT_ENABLED = bitMask(2, (byte) 0x40, false);
        public static final Entry<Byte> MAIN_HAND = index(3, Metadata::Byte, (byte) 1);
        public static final Entry<BinaryTag> LEFT_SHOULDER_ENTITY_DATA = index(4, Metadata::NBT, CompoundBinaryTag.empty());
        public static final Entry<BinaryTag> RIGHT_SHOULDER_ENTITY_DATA = index(5, Metadata::NBT, CompoundBinaryTag.empty());
    }

    public static final class ArmorStand extends LivingEntity {
        public static final Entry<Byte> ARMOR_STAND_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_SMALL = bitMask(0, (byte) 0x01, false);
        public static final Entry<Boolean> HAS_ARMS = bitMask(0, (byte) 0x04, false);
        public static final Entry<Boolean> HAS_NO_BASE_PLATE = bitMask(0, (byte) 0x08, false);
        public static final Entry<Boolean> IS_MARKER = bitMask(0, (byte) 0x10, false);
        public static final Entry<Point> HEAD_ROTATION = index(1, Metadata::Rotation, Vec.ZERO);
        public static final Entry<Point> BODY_ROTATION = index(2, Metadata::Rotation, Vec.ZERO);
        public static final Entry<Point> LEFT_ARM_ROTATION = index(3, Metadata::Rotation, new Vec(-10, 0, -10));
        public static final Entry<Point> RIGHT_ARM_ROTATION = index(4, Metadata::Rotation, new Vec(-15, 0, 10));
        public static final Entry<Point> LEFT_LEG_ROTATION = index(5, Metadata::Rotation, new Vec(-1, 0, -1));
        public static final Entry<Point> RIGHT_LEG_ROTATION = index(6, Metadata::Rotation, new Vec(1, 0, 1));
    }

    public static sealed class Mob extends LivingEntity {
        public static final Entry<Byte> MOB_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> NO_AI = bitMask(0, (byte) 0x01, false);
        public static final Entry<Boolean> IS_LEFT_HANDED = bitMask(0, (byte) 0x02, false);
        public static final Entry<Boolean> IS_AGGRESSIVE = bitMask(0, (byte) 0x04, false);
    }

    public static final class Allay extends Mob {
        public static final Entry<Boolean> IS_DANCING = index(0, Metadata::Boolean, false);
        public static final Entry<Boolean> CAN_DUPLICATE = index(1, Metadata::Boolean, true);
    }

    public static final class Armadillo extends Mob {
        public static final Entry<ArmadilloMeta.State> STATE = index(0, Metadata::ArmadilloState, ArmadilloMeta.State.IDLE);
    }

    public static final class Bat extends Mob {
        public static final Entry<Byte> BAT_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_HANGING = bitMask(0, (byte) 0x01, false);
    }

    public static final class Dolphin extends Mob {
        public static final Entry<Point> TREASURE_POSITION = index(0, Metadata::BlockPosition, Vec.ZERO);
        public static final Entry<Boolean> HAS_FISH = index(1, Metadata::Boolean, false);
        public static final Entry<Integer> MOISTURE_LEVEL = index(2, Metadata::VarInt, 2400);
    }

    public static sealed class AbstractFish extends Mob {
        public static final Entry<Boolean> FROM_BUCKET = index(0, Metadata::Boolean, false);
    }

    public static final class PufferFish extends AbstractFish {
        public static final Entry<Integer> PUFF_STATE = index(0, Metadata::VarInt, 0);
    }

    public static final class Salmon extends AbstractFish {
        public static final Entry<String> VARIANT = index(0, Metadata::String, "");
    }

    public static final class TropicalFish extends AbstractFish {
        public static final Entry<Integer> VARIANT = index(0, Metadata::VarInt, 0);
    }

    public static sealed class AgeableMob extends Mob {
        public static final Entry<Boolean> IS_BABY = index(0, Metadata::Boolean, false);
    }

    public static final class Sniffer extends AgeableMob {
        public static final Entry<SnifferMeta.State> STATE = index(0, Metadata::SnifferState, SnifferMeta.State.IDLING);
        public static final Entry<Integer> DROP_SEED_AT_TICK = index(1, Metadata::VarInt, 0);
    }

    public static sealed class AbstractHorse extends AgeableMob {
        public static final Entry<Byte> ABSTRACT_HORSE_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> UNUSED = bitMask(0, (byte) 0x01, false);
        public static final Entry<Boolean> IS_TAME = bitMask(0, (byte) 0x02, false);
        public static final Entry<Boolean> IS_SADDLED = bitMask(0, (byte) 0x04, false);
        public static final Entry<Boolean> HAS_BRED = bitMask(0, (byte) 0x08, false);
        public static final Entry<Boolean> IS_EATING = bitMask(0, (byte) 0x10, false);
        public static final Entry<Boolean> IS_REARING = bitMask(0, (byte) 0x20, false);
        public static final Entry<Boolean> IS_MOUTH_OPEN = bitMask(0, (byte) 0x40, false);
    }

    public static final class Horse extends AbstractHorse {
        public static final Entry<Integer> VARIANT = index(0, Metadata::VarInt, 0);
    }

    public static final class Camel extends AbstractHorse {
        public static final Entry<Boolean> DASHING = index(0, Metadata::Boolean, false);
        public static final Entry<Long> LAST_POSE_CHANGE_TICK = index(1, Metadata::VarLong, 0L);
    }

    public static sealed class ChestedHorse extends AbstractHorse {
        public static final Entry<Boolean> HAS_CHEST = index(0, Metadata::Boolean, false);
    }

    public static final class Llama extends ChestedHorse {
        public static final Entry<Integer> STRENGTH = index(0, Metadata::VarInt, 0);
        public static final Entry<Integer> CARPET_COLOR = index(0, Metadata::VarInt, -1);
        public static final Entry<Integer> VARIANT = index(0, Metadata::VarInt, 0);
    }

    public static final class Axolotl extends AgeableMob {
        public static final Entry<Integer> VARIANT = index(0, Metadata::VarInt, 0);
        public static final Entry<Boolean> IS_PLAYING_DEAD = index(1, Metadata::Boolean, false);
        public static final Entry<Boolean> IS_FROM_BUCKET = index(2, Metadata::Boolean, false);
    }

    public static final class Bee extends AgeableMob {
        public static final Entry<Byte> BEE_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_ANGRY = bitMask(0, (byte) 0x02, false);
        public static final Entry<Boolean> HAS_STUNG = bitMask(0, (byte) 0x04, false);
        public static final Entry<Boolean> HAS_NECTAR = bitMask(0, (byte) 0x08, false);
        public static final Entry<Integer> ANGER_TIME_TICKS = index(1, Metadata::VarInt, 0);
    }

    public static final class GlowSquid extends AgeableMob {
        public static final Entry<Integer> DARK_TICKS_REMAINING = index(0, Metadata::VarInt, 0);
    }

    public static final class Fox extends AgeableMob {
        public static final Entry<Integer> TYPE = index(0, Metadata::VarInt, 0);
        public static final Entry<Byte> FOX_FLAGS = index(1, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_SITTING = bitMask(1, (byte) 0x01, false);
        public static final Entry<Boolean> IS_CROUCHING = bitMask(1, (byte) 0x04, false);
        public static final Entry<Boolean> IS_INTERESTED = bitMask(1, (byte) 0x08, false);
        public static final Entry<Boolean> IS_POUNCING = bitMask(1, (byte) 0x10, false);
        public static final Entry<Boolean> IS_SLEEPING = bitMask(1, (byte) 0x20, false);
        public static final Entry<Boolean> IS_FACEPLANTED = bitMask(1, (byte) 0x40, false);
        public static final Entry<Boolean> IS_DEFENDING = bitMask(1, (byte) 0x80, false);
        public static final Entry<@Nullable UUID> FIRST_UUID = index(2, Metadata::OptUUID, null);
        public static final Entry<@Nullable UUID> SECOND_UUID = index(3, Metadata::OptUUID, null);
    }

    public static final class Frog extends AgeableMob {
        public static final Entry<FrogMeta.Variant> VARIANT = index(0, Metadata::FrogVariant, FrogMeta.Variant.TEMPERATE);
        public static final Entry<@Nullable Integer> TONGUE_TARGET = index(1, Metadata::OptVarInt, 0);
    }

    public static final class Ocelot extends AgeableMob {
        public static final Entry<Boolean> IS_TRUSTING = index(0, Metadata::Boolean, false);
    }

    public static final class Panda extends AgeableMob {
        public static final Entry<Integer> BREED_TIMER = index(0, Metadata::VarInt, 0);
        public static final Entry<Integer> SNEEZE_TIMER = index(1, Metadata::VarInt, 0);
        public static final Entry<Integer> EAT_TIMER = index(2, Metadata::VarInt, 0);
        public static final Entry<Byte> MAIN_GENE = index(3, Metadata::Byte, (byte) 0);
        public static final Entry<Byte> HIDDEN_GENE = index(4, Metadata::Byte, (byte) 0);
        public static final Entry<Byte> PANDA_FLAGS = index(5, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_SNEEZING = bitMask(5, (byte) 0x02, false);
        public static final Entry<Boolean> IS_ROLLING = bitMask(5, (byte) 0x04, false);
        public static final Entry<Boolean> IS_SITTING = bitMask(5, (byte) 0x08, false);
        public static final Entry<Boolean> IS_ON_BACK = bitMask(5, (byte) 0x10, false);
    }

    public static final class Pig extends AgeableMob {
        public static final Entry<Boolean> HAS_SADDLE = index(0, Metadata::Boolean, false);
        public static final Entry<Integer> BOOST_TIME = index(1, Metadata::VarInt, 0);
    }

    public static final class Rabbit extends AgeableMob {
        public static final Entry<Integer> TYPE = index(0, Metadata::VarInt, 0);
    }

    public static final class Turtle extends AgeableMob {
        public static final Entry<Point> HOME_POS = index(0, Metadata::BlockPosition, Vec.ZERO);
        public static final Entry<Boolean> HAS_EGG = index(1, Metadata::Boolean, false);
        public static final Entry<Boolean> IS_LAYING_EGG = index(2, Metadata::Boolean, false);
        public static final Entry<Point> TRAVEL_POS = index(3, Metadata::BlockPosition, Vec.ZERO);
        public static final Entry<Boolean> IS_GOING_HOME = index(4, Metadata::Boolean, false);
        public static final Entry<Boolean> IS_TRAVELING = index(5, Metadata::Boolean, false);
    }

    public static final class PolarBear extends AgeableMob {
        public static final Entry<Boolean> IS_STANDING_UP = index(0, Metadata::Boolean, false);
    }

    public static final class Mooshroom extends AgeableMob {
        public static final Entry<String> VARIANT = index(0, Metadata::String, "red");
    }

    public static final class Hoglin extends AgeableMob {
        public static final Entry<Boolean> IMMUNE_ZOMBIFICATION = index(0, Metadata::Boolean, false);
    }

    public static final class Sheep extends AgeableMob {
        public static final Entry<Byte> SHEEP_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Byte> COLOR_ID = byteMask(0, (byte) 0x0F, 0, (byte) 0);
        public static final Entry<Boolean> IS_SHEARED = bitMask(0, (byte) 0x10, false);
    }

    public static final class Strider extends AgeableMob {
        public static final Entry<Integer> FUNGUS_BOOST = index(0, Metadata::VarInt, 0);
        public static final Entry<Boolean> IS_SHAKING = index(1, Metadata::Boolean, false);
        public static final Entry<Boolean> HAS_SADDLE = index(2, Metadata::Boolean, false);
    }

    public static final class Goat extends AgeableMob {
        public static final Entry<Boolean> IS_SCREAMING_GOAT = index(0, Metadata::Boolean, false);
        public static final Entry<Boolean> HAS_LEFT_HORN = index(1, Metadata::Boolean, true);
        public static final Entry<Boolean> HAS_RIGHT_HORN = index(2, Metadata::Boolean, true);
    }

    public static sealed class TameableAnimal extends AgeableMob {
        public static final Entry<Byte> TAMEABLE_ANIMAL_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_SITTING = bitMask(0, (byte) 0x01, false);
        public static final Entry<Boolean> UNUSED = bitMask(0, (byte) 0x02, false);
        public static final Entry<Boolean> IS_TAMED = bitMask(0, (byte) 0x04, false);
        public static final Entry<@Nullable UUID> OWNER = index(1, Metadata::OptUUID, null);
    }

    public static final class Cat extends TameableAnimal {
        public static final Entry<CatMeta.Variant> VARIANT = index(0, Metadata::CatVariant, CatMeta.Variant.BLACK);
        public static final Entry<Boolean> IS_LYING = index(2, Metadata::Boolean, false);
        public static final Entry<Boolean> IS_RELAXED = index(3, Metadata::Boolean, false);
        public static final Entry<Integer> COLLAR_COLOR = index(4, Metadata::VarInt, 14);
    }

    public static final class Wolf extends TameableAnimal {
        public static final Entry<Boolean> IS_BEGGING = index(0, Metadata::Boolean, false);
        public static final Entry<Integer> COLLAR_COLOR = index(1, Metadata::VarInt, 14);
        public static final Entry<Integer> ANGER_TIME = index(2, Metadata::VarInt, 0);
        public static final Entry<DynamicRegistry.Key<WolfMeta.Variant>> VARIANT = index(3, Metadata::WolfVariant, WolfMeta.Variant.PALE);
    }

    public static final class Parrot extends TameableAnimal {
        public static final Entry<Integer> VARIANT = index(0, Metadata::VarInt, 0);
    }

    public static sealed class AbstractVillager extends AgeableMob {
        public static final Entry<Integer> HEAD_SHAKE_TIMER = index(0, Metadata::VarInt, 0);
    }

    public static final class Villager extends AbstractVillager {
        public static final Entry<int[]> VARIANT = index(0, Metadata::VillagerData, new int[]{0, 0, 0});
    }

    public static final class IronGolem extends Mob {
        public static final Entry<Byte> IRON_GOLEM_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_PLAYER_CREATED = bitMask(0, (byte) 0x01, false);
    }

    public static final class SnowGolem extends Mob {
        public static final Entry<Byte> SNOW_GOLEM_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> PUMPKIN_HAT = bitMask(0, (byte) 0x10, true);
    }

    public static final class Shulker extends Mob {
        public static final Entry<Direction> ATTACH_FACE = index(0, Metadata::Direction, Direction.DOWN);
        public static final Entry<Byte> SHIELD_HEIGHT = index(1, Metadata::Byte, (byte) 0);
        public static final Entry<Byte> COLOR = index(2, Metadata::Byte, (byte) 16);
    }

    public static sealed class BasePiglin extends Mob {
        public static final Entry<Boolean> IMMUNE_ZOMBIFICATION = index(0, Metadata::Boolean, false);
    }

    public static final class Piglin extends BasePiglin {
        public static final Entry<Boolean> IS_BABY = index(0, Metadata::Boolean, false);
        public static final Entry<Boolean> IS_CHARGING_CROSSBOW = index(1, Metadata::Boolean, false);
        public static final Entry<Boolean> IS_DANCING = index(2, Metadata::Boolean, false);
    }

    public static final class Blaze extends Mob {
        public static final Entry<Byte> BLAZE_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_ON_FIRE = bitMask(0, (byte) 0x01, false);
    }

    public static final class Bogged extends Mob {
        public static final Entry<Boolean> IS_SHEARED = index(0, Metadata::Boolean, false);
    }

    public static final class Creaking extends Mob {
        public static final Entry<Boolean> CAN_MOVE = index(0, Metadata::Boolean, false);
        public static final Entry<Boolean> IS_ACTIVE = index(1, Metadata::Boolean, false);
    }

    public static final class Creeper extends Mob {
        public static final Entry<Integer> STATE = index(0, Metadata::VarInt, -1);
        public static final Entry<Boolean> IS_CHARGED = index(1, Metadata::Boolean, false);
        public static final Entry<Boolean> IS_IGNITED = index(2, Metadata::Boolean, false);
    }

    public static final class Guardian extends Mob {
        public static final Entry<Boolean> IS_RETRACTING_SPIKES = index(0, Metadata::Boolean, false);
        public static final Entry<Integer> TARGET_EID = index(1, Metadata::VarInt, 0);
    }

    public static sealed class Raider extends Mob {
        public static final Entry<Boolean> IS_CELEBRATING = index(0, Metadata::Boolean, false);
    }

    public static final class Pillager extends Raider {
        public static final Entry<Boolean> IS_CHARGING = index(0, Metadata::Boolean, false);
    }

    public static final class SpellcasterIllager extends Raider {
        public static final Entry<Byte> SPELL = index(0, Metadata::Byte, (byte) 0);
    }

    public static final class Witch extends Raider {
        public static final Entry<Boolean> IS_DRINKING_POTION = index(0, Metadata::Boolean, false);
    }

    public static final class Spider extends Mob {
        public static final Entry<Byte> SPIDER_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_CLIMBING = bitMask(0, (byte) 0x01, false);
    }

    public static final class Vex extends Mob {
        public static final Entry<Byte> VEX_FLAGS = index(0, Metadata::Byte, (byte) 0);
        public static final Entry<Boolean> IS_ATTACKING = bitMask(0, (byte) 0x01, false);
    }

    public static final class Warden extends Mob {
        public static final Entry<Integer> ANGER_LEVEL = index(0, Metadata::VarInt, 0);
    }

    public static final class Wither extends Mob {
        public static final Entry<Integer> CENTER_HEAD_TARGET = index(0, Metadata::VarInt, 0);
        public static final Entry<Integer> LEFT_HEAD_TARGET = index(1, Metadata::VarInt, 0);
        public static final Entry<Integer> RIGHT_HEAD_TARGET = index(2, Metadata::VarInt, 0);
        public static final Entry<Integer> INVULNERABLE_TIME = index(3, Metadata::VarInt, 0);
    }

    public static final class Zoglin extends Mob {
        public static final Entry<Boolean> IS_BABY = index(0, Metadata::Boolean, false);
    }

    public static final class Zombie extends Mob {
        public static final Entry<Boolean> IS_BABY = index(0, Metadata::Boolean, false);
        public static final Entry<Integer> UNUSED = index(1, Metadata::VarInt, 0);
        public static final Entry<Boolean> IS_BECOMING_DROWNED = index(2, Metadata::Boolean, false);
    }

    public static final class ZombieVillager extends Mob {
        public static final Entry<Boolean> IS_CONVERTING = index(0, Metadata::Boolean, false);
        public static final Entry<int[]> VILLAGER_DATA = index(1, Metadata::VillagerData, new int[]{0, 0, 0});
    }

    public static final class Enderman extends Mob {
        public static final Entry<@Nullable Integer> CARRIED_BLOCK = index(0, Metadata::OptBlockState, null);
        public static final Entry<Boolean> IS_SCREAMING = index(1, Metadata::Boolean, false);
        public static final Entry<Boolean> IS_STARING = index(2, Metadata::Boolean, false);
    }

    public static final class EnderDragon extends Mob {
        public static final Entry<Integer> DRAGON_PHASE = index(0, Metadata::VarInt, 10);
    }

    public static final class Ghast extends Mob {
        public static final Entry<Boolean> IS_ATTACKING = index(0, Metadata::Boolean, false);
    }

    public static final class Phantom extends Mob {
        public static final Entry<Integer> SIZE = index(0, Metadata::VarInt, 0);
    }

    public static final class Slime extends Mob {
        public static final Entry<Integer> SIZE = index(0, Metadata::VarInt, 1);
    }

    public static final class PrimedTnt extends Mob {
        public static final Entry<Integer> FUSE_TIME = index(0, Metadata::VarInt, 80);
    }

    public static final class OminousItemSpawner extends MetadataDef {
        public static final Entry<ItemStack> ITEM = index(0, Metadata::ItemStack, ItemStack.AIR);
    }

    /**
     * Get the number of metadata entries for a specific class.
     * <p>
     * Useful if you want to pre-allocate the metadata array.
     */
    public static <T extends MetadataDef> int count(Class<T> clazz) {
        return MetadataDefImpl.count(clazz);
    }

    public sealed interface Entry<T> {
        int index();

        T defaultValue();

        record Index<T>(int index, Function<T, Metadata.Entry<T>> function, T defaultValue) implements Entry<T> {
        }

        record BitMask(int index, byte bitMask, Boolean defaultValue) implements Entry<Boolean> {
        }

        record ByteMask(int index, byte byteMask, int offset, Byte defaultValue) implements Entry<Byte> {
        }
    }
}
