package net.minestom.server.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.ParrotMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.entity.metadata.avatar.PlayerMeta;
import net.minestom.server.entity.metadata.cube.MagmaCubeMeta;
import net.minestom.server.entity.metadata.cube.SlimeMeta;
import net.minestom.server.entity.metadata.cube.SulfurCubeMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.entity.metadata.flying.GhastMeta;
import net.minestom.server.entity.metadata.flying.PhantomMeta;
import net.minestom.server.entity.metadata.golem.CopperGolemMeta;
import net.minestom.server.entity.metadata.golem.IronGolemMeta;
import net.minestom.server.entity.metadata.golem.ShulkerMeta;
import net.minestom.server.entity.metadata.golem.SnowGolemMeta;
import net.minestom.server.entity.metadata.item.*;
import net.minestom.server.entity.metadata.minecart.*;
import net.minestom.server.entity.metadata.monster.*;
import net.minestom.server.entity.metadata.monster.raider.*;
import net.minestom.server.entity.metadata.monster.skeleton.*;
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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static java.util.Map.entry;

public final class MetadataHolder {
    private static final VarHandle NOTIFIED_CHANGES;

    static {
        try {
            NOTIFIED_CHANGES = MethodHandles.lookup().findVarHandle(MetadataHolder.class, "notifyAboutChanges", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Consumer<Map<Integer, Metadata.Entry<?>>> changesListener;
    private Metadata.@Nullable Entry<?>[] entries = new Metadata.Entry<?>[0]; // indexed by metadata id, resized on demand

    @SuppressWarnings("FieldMayBeFinal")
    private volatile boolean notifyAboutChanges = true;
    private final Int2ObjectArrayMap<Metadata.Entry<?>> notNotifiedChanges = new Int2ObjectArrayMap<>();

    /**
     * @deprecated Use {@link #MetadataHolder(Consumer)} instead.
     */
    @Deprecated(forRemoval = true)
    public MetadataHolder(@Nullable Entity entity) {
        this(entity == null ? _ -> {
        } : entity::notifyMetadataChanges);
    }

    public MetadataHolder(Consumer<Map<Integer, Metadata.Entry<?>>> changesListener) {
        this.changesListener = Objects.requireNonNull(changesListener, "changesListener");
    }

    @SuppressWarnings("unchecked")
    public <T extends @UnknownNullability Object> T get(MetadataDef.Entry<T> entry) {
        final int id = entry.index();

        final Metadata.Entry<?> value = entryAt(id);
        if (value == null) return entry.defaultValue();
        return switch (entry) {
            case MetadataDef.Entry.Index<T> _ -> (T) value.value();
            case MetadataDef.Entry.BitMask bitMask -> {
                final byte maskValue = (byte) value.value();
                yield (T) ((Boolean) getMaskBit(maskValue, bitMask.bitMask()));
            }
            case MetadataDef.Entry.ByteMask byteMask -> {
                final byte maskValue = (byte) value.value();
                yield (T) ((Byte) getMaskByte(maskValue, byteMask.byteMask(), byteMask.offset()));
            }
        };
    }

    public <T extends @UnknownNullability Object> void set(MetadataDef.Entry<T> entry, T value) {
        final int id = entry.index();

        T current = get(entry);

        // If a metadata value is unchanged we should not send it. In particular we need to be careful with
        //  sending bitmasks which will overwrite client-predicted values. See PR 3089 for more info.
        // However, interpolation delay is expected to be sent regularly with the same value to begin
        //  interpolation so we always send it for now.
        if (Objects.equals(current, value) && entry != MetadataDef.Display.INTERPOLATION_DELAY) {
            return;
        }

        Metadata.Entry<?> result = switch (entry) {
            case MetadataDef.Entry.Index<T> v -> v.function().apply(value);
            case MetadataDef.Entry.BitMask bitMask -> {
                Metadata.Entry<?> currentEntry = entryAt(id);
                byte maskValue = currentEntry != null ? (byte) currentEntry.value() : 0;
                maskValue = setMaskBit(maskValue, bitMask.bitMask(), (Boolean) value);
                yield Metadata.Byte(maskValue);
            }
            case MetadataDef.Entry.ByteMask byteMask -> {
                Metadata.Entry<?> currentEntry = entryAt(id);
                byte maskValue = currentEntry != null ? (byte) currentEntry.value() : 0;
                maskValue = setMaskByte(maskValue, byteMask.byteMask(), byteMask.offset(), (Byte) value);
                yield Metadata.Byte(maskValue);
            }
        };

        putEntry(id, result);

        if (!this.notifyAboutChanges) {
            synchronized (this.notNotifiedChanges) {
                this.notNotifiedChanges.put(id, result);
            }
        } else {
            this.changesListener.accept(Map.of(id, result));
        }
    }

    private boolean getMaskBit(byte maskValue, byte bit) {
        return (maskValue & bit) == bit;
    }

    private byte setMaskBit(byte mask, byte bit, boolean value) {
        return value ? (byte) (mask | bit) : (byte) (mask & ~bit);
    }

    private byte getMaskByte(byte data, byte byteMask, int offset) {
        return (byte) ((data & byteMask) >> offset);
    }

    private byte setMaskByte(byte data, byte byteMask, int offset, byte newValue) {
        return (byte) ((data & ~byteMask) | ((newValue << offset) & byteMask));
    }

    public void setNotifyAboutChanges(boolean notifyAboutChanges) {
        if (!NOTIFIED_CHANGES.compareAndSet(this, !notifyAboutChanges, notifyAboutChanges))
            return;
        if (!notifyAboutChanges) {
            // Ask future metadata changes to be cached
            return;
        }
        Int2ObjectArrayMap<Metadata.Entry<?>> entries;
        synchronized (this.notNotifiedChanges) {
            if (this.notNotifiedChanges.isEmpty()) return;
            entries = new Int2ObjectArrayMap<>(this.notNotifiedChanges);
            this.notNotifiedChanges.clear();
        }
        this.changesListener.accept(entries);
    }

    private Metadata.@Nullable Entry<?> entryAt(int id) {
        final var entries = this.entries;
        return id >= 0 && id < entries.length ? entries[id] : null;
    }

    private void putEntry(int id, Metadata.Entry<?> entry) {
        var entries = this.entries;
        if (id >= entries.length) this.entries = entries = Arrays.copyOf(entries, id + 1);
        entries[id] = entry;
    }

    public Map<Integer, Metadata.Entry<?>> getEntries() {
        final var entries = this.entries;
        int[] ids = new int[entries.length];
        Metadata.Entry<?>[] values = new Metadata.Entry<?>[entries.length];
        int count = 0;
        for (int id = 0; id < entries.length; id++) {
            final var entry = entries[id];
            if (entry == null) continue; // skip gaps between metadata ids
            ids[count] = id;
            values[count] = entry;
            count++;
        }
        return Map.copyOf(new Int2ObjectArrayMap<>(ids, values, count));
    }

    @SuppressWarnings("JavacQuirks")
    static final Map<EntityType, BiFunction<@Nullable Entity, MetadataHolder, ? extends EntityMeta>> ENTITY_META_SUPPLIER = Map.ofEntries(
            entry(EntityType.ACACIA_BOAT, BoatMeta::new),
            entry(EntityType.ACACIA_CHEST_BOAT, BoatMeta::new),
            entry(EntityType.ALLAY, AllayMeta::new),
            entry(EntityType.AREA_EFFECT_CLOUD, AreaEffectCloudMeta::new),
            entry(EntityType.ARMADILLO, ArmadilloMeta::new),
            entry(EntityType.ARMOR_STAND, ArmorStandMeta::new),
            entry(EntityType.ARROW, ArrowMeta::new),
            entry(EntityType.AXOLOTL, AxolotlMeta::new),
            entry(EntityType.BAMBOO_RAFT, BoatMeta::new),
            entry(EntityType.BAMBOO_CHEST_RAFT, BoatMeta::new),
            entry(EntityType.BAT, BatMeta::new),
            entry(EntityType.BEE, BeeMeta::new),
            entry(EntityType.BIRCH_BOAT, BoatMeta::new),
            entry(EntityType.BIRCH_CHEST_BOAT, BoatMeta::new),
            entry(EntityType.BLAZE, BlazeMeta::new),
            entry(EntityType.BLOCK_DISPLAY, BlockDisplayMeta::new),
            entry(EntityType.BOGGED, BoggedMeta::new),
            entry(EntityType.BREEZE, BreezeMeta::new),
            entry(EntityType.BREEZE_WIND_CHARGE, BreezeWindChargeMeta::new),
            entry(EntityType.CAMEL, CamelMeta::new),
            entry(EntityType.CAMEL_HUSK, CamelHuskMeta::new),
            entry(EntityType.CAT, CatMeta::new),
            entry(EntityType.CAVE_SPIDER, CaveSpiderMeta::new),
            entry(EntityType.CHERRY_BOAT, BoatMeta::new),
            entry(EntityType.CHERRY_CHEST_BOAT, BoatMeta::new),
            entry(EntityType.CHICKEN, ChickenMeta::new),
            entry(EntityType.COD, CodMeta::new),
            entry(EntityType.COPPER_GOLEM, CopperGolemMeta::new),
            entry(EntityType.COW, CowMeta::new),
            entry(EntityType.CREAKING, CreakingMeta::new),
            entry(EntityType.CREEPER, CreeperMeta::new),
            entry(EntityType.DARK_OAK_BOAT, BoatMeta::new),
            entry(EntityType.DARK_OAK_CHEST_BOAT, BoatMeta::new),
            entry(EntityType.DOLPHIN, DolphinMeta::new),
            entry(EntityType.DONKEY, DonkeyMeta::new),
            entry(EntityType.DRAGON_FIREBALL, DragonFireballMeta::new),
            entry(EntityType.DROWNED, DrownedMeta::new),
            entry(EntityType.ELDER_GUARDIAN, ElderGuardianMeta::new),
            entry(EntityType.END_CRYSTAL, EndCrystalMeta::new),
            entry(EntityType.ENDER_DRAGON, EnderDragonMeta::new),
            entry(EntityType.ENDERMAN, EndermanMeta::new),
            entry(EntityType.ENDERMITE, EndermiteMeta::new),
            entry(EntityType.EVOKER, EvokerMeta::new),
            entry(EntityType.EVOKER_FANGS, EvokerFangsMeta::new),
            entry(EntityType.EXPERIENCE_ORB, ExperienceOrbMeta::new),
            entry(EntityType.EYE_OF_ENDER, EyeOfEnderMeta::new),
            entry(EntityType.FALLING_BLOCK, FallingBlockMeta::new),
            entry(EntityType.FIREBALL, FireballMeta::new),
            entry(EntityType.FIREWORK_ROCKET, FireworkRocketMeta::new),
            entry(EntityType.FOX, FoxMeta::new),
            entry(EntityType.FROG, FrogMeta::new),
            entry(EntityType.GHAST, GhastMeta::new),
            entry(EntityType.GIANT, GiantMeta::new),
            entry(EntityType.GLOW_ITEM_FRAME, GlowItemFrameMeta::new),
            entry(EntityType.GLOW_SQUID, GlowSquidMeta::new),
            entry(EntityType.GOAT, GoatMeta::new),
            entry(EntityType.GUARDIAN, GuardianMeta::new),
            entry(EntityType.HAPPY_GHAST, HappyGhastMeta::new),
            entry(EntityType.HOGLIN, HoglinMeta::new),
            entry(EntityType.HORSE, HorseMeta::new),
            entry(EntityType.HUSK, HuskMeta::new),
            entry(EntityType.ILLUSIONER, IllusionerMeta::new),
            entry(EntityType.INTERACTION, InteractionMeta::new),
            entry(EntityType.IRON_GOLEM, IronGolemMeta::new),
            entry(EntityType.ITEM, ItemEntityMeta::new),
            entry(EntityType.ITEM_DISPLAY, ItemDisplayMeta::new),
            entry(EntityType.ITEM_FRAME, ItemFrameMeta::new),
            entry(EntityType.JUNGLE_BOAT, BoatMeta::new),
            entry(EntityType.JUNGLE_CHEST_BOAT, BoatMeta::new),
            entry(EntityType.LEASH_KNOT, LeashKnotMeta::new),
            entry(EntityType.LIGHTNING_BOLT, LightningBoltMeta::new),
            entry(EntityType.LINGERING_POTION, LingeringPotionMeta::new),
            entry(EntityType.LLAMA, LlamaMeta::new),
            entry(EntityType.LLAMA_SPIT, LlamaSpitMeta::new),
            entry(EntityType.MAGMA_CUBE, MagmaCubeMeta::new),
            entry(EntityType.MANGROVE_BOAT, BoatMeta::new),
            entry(EntityType.MANGROVE_CHEST_BOAT, BoatMeta::new),
            entry(EntityType.MANNEQUIN, MannequinMeta::new),
            entry(EntityType.MARKER, MarkerMeta::new),
            entry(EntityType.MINECART, MinecartMeta::new),
            entry(EntityType.NAUTILUS, NautilusMeta::new),
            entry(EntityType.CHEST_MINECART, ChestMinecartMeta::new),
            entry(EntityType.COMMAND_BLOCK_MINECART, CommandBlockMinecartMeta::new),
            entry(EntityType.FURNACE_MINECART, FurnaceMinecartMeta::new),
            entry(EntityType.HOPPER_MINECART, HopperMinecartMeta::new),
            entry(EntityType.SPAWNER_MINECART, SpawnerMinecartMeta::new),
            entry(EntityType.TEXT_DISPLAY, TextDisplayMeta::new),
            entry(EntityType.TNT_MINECART, TntMinecartMeta::new),
            entry(EntityType.MOOSHROOM, MooshroomMeta::new),
            entry(EntityType.MULE, MuleMeta::new),
            entry(EntityType.OAK_BOAT, BoatMeta::new),
            entry(EntityType.OAK_CHEST_BOAT, BoatMeta::new),
            entry(EntityType.OCELOT, OcelotMeta::new),
            entry(EntityType.OMINOUS_ITEM_SPAWNER, OminousItemSpawnerMeta::new),
            entry(EntityType.PAINTING, PaintingMeta::new),
            entry(EntityType.PALE_OAK_BOAT, BoatMeta::new),
            entry(EntityType.PALE_OAK_CHEST_BOAT, BoatMeta::new),
            entry(EntityType.PANDA, PandaMeta::new),
            entry(EntityType.PARROT, ParrotMeta::new),
            entry(EntityType.PARCHED, ParchedMeta::new),
            entry(EntityType.PHANTOM, PhantomMeta::new),
            entry(EntityType.PIG, PigMeta::new),
            entry(EntityType.PIGLIN, PiglinMeta::new),
            entry(EntityType.PIGLIN_BRUTE, PiglinBruteMeta::new),
            entry(EntityType.PILLAGER, PillagerMeta::new),
            entry(EntityType.POLAR_BEAR, PolarBearMeta::new),
            entry(EntityType.TNT, PrimedTntMeta::new),
            entry(EntityType.PUFFERFISH, PufferfishMeta::new),
            entry(EntityType.RABBIT, RabbitMeta::new),
            entry(EntityType.RAVAGER, RavagerMeta::new),
            entry(EntityType.SALMON, SalmonMeta::new),
            entry(EntityType.SHEEP, SheepMeta::new),
            entry(EntityType.SHULKER, ShulkerMeta::new),
            entry(EntityType.SHULKER_BULLET, ShulkerBulletMeta::new),
            entry(EntityType.SILVERFISH, SilverfishMeta::new),
            entry(EntityType.SKELETON, SkeletonMeta::new),
            entry(EntityType.SKELETON_HORSE, SkeletonHorseMeta::new),
            entry(EntityType.SLIME, SlimeMeta::new),
            entry(EntityType.SMALL_FIREBALL, SmallFireballMeta::new),
            entry(EntityType.SNIFFER, SnifferMeta::new),
            entry(EntityType.SNOW_GOLEM, SnowGolemMeta::new),
            entry(EntityType.SNOWBALL, SnowballMeta::new),
            entry(EntityType.SPECTRAL_ARROW, SpectralArrowMeta::new),
            entry(EntityType.SPIDER, SpiderMeta::new),
            entry(EntityType.SPLASH_POTION, SplashPotionMeta::new),
            entry(EntityType.SPRUCE_BOAT, BoatMeta::new),
            entry(EntityType.SPRUCE_CHEST_BOAT, BoatMeta::new),
            entry(EntityType.SQUID, SquidMeta::new),
            entry(EntityType.STRAY, StrayMeta::new),
            entry(EntityType.STRIDER, StriderMeta::new),
            entry(EntityType.SULPHER_CUBE, SulfurCubeMeta::new),
            entry(EntityType.TADPOLE, TadpoleMeta::new),
            entry(EntityType.EGG, ThrownEggMeta::new),
            entry(EntityType.ENDER_PEARL, ThrownEnderPearlMeta::new),
            entry(EntityType.EXPERIENCE_BOTTLE, ThrownExperienceBottleMeta::new),
            entry(EntityType.TRIDENT, ThrownTridentMeta::new),
            entry(EntityType.TRADER_LLAMA, TraderLlamaMeta::new),
            entry(EntityType.TROPICAL_FISH, TropicalFishMeta::new),
            entry(EntityType.TURTLE, TurtleMeta::new),
            entry(EntityType.VEX, VexMeta::new),
            entry(EntityType.VILLAGER, VillagerMeta::new),
            entry(EntityType.VINDICATOR, VindicatorMeta::new),
            entry(EntityType.WANDERING_TRADER, WanderingTraderMeta::new),
            entry(EntityType.WARDEN, WardenMeta::new),
            entry(EntityType.WIND_CHARGE, WindChargeMeta::new),
            entry(EntityType.WITCH, WitchMeta::new),
            entry(EntityType.WITHER, WitherMeta::new),
            entry(EntityType.WITHER_SKELETON, WitherSkeletonMeta::new),
            entry(EntityType.WITHER_SKULL, WitherSkullMeta::new),
            entry(EntityType.WOLF, WolfMeta::new),
            entry(EntityType.ZOGLIN, ZoglinMeta::new),
            entry(EntityType.ZOMBIE, ZombieMeta::new),
            entry(EntityType.ZOMBIE_HORSE, ZombieHorseMeta::new),
            entry(EntityType.ZOMBIE_NAUTILUS, ZombieNautilusMeta::new),
            entry(EntityType.ZOMBIE_VILLAGER, ZombieVillagerMeta::new),
            entry(EntityType.ZOMBIFIED_PIGLIN, ZombifiedPiglinMeta::new),
            entry(EntityType.PLAYER, PlayerMeta::new),
            entry(EntityType.FISHING_BOBBER, FishingHookMeta::new)
    );

    @ApiStatus.Internal
    public static EntityMeta createMeta(EntityType entityType, @Nullable Entity entity, MetadataHolder metadata) {
        return ENTITY_META_SUPPLIER.get(entityType).apply(entity, metadata);
    }
}
