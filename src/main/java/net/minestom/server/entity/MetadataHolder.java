package net.minestom.server.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.ParrotMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.entity.metadata.avatar.PlayerMeta;
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
import java.util.HashMap;
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
    private final Int2ObjectMap<Metadata.Entry<?>> entries = new Int2ObjectOpenHashMap<>();

    @SuppressWarnings("FieldMayBeFinal")
    private volatile boolean notifyAboutChanges = true;
    private final Map<Integer, Metadata.Entry<?>> notNotifiedChanges = new HashMap<>();

    /**
     * @deprecated Use {@link #MetadataHolder(Consumer)} instead.
     */
    @Deprecated(forRemoval = true)
    public MetadataHolder(@Nullable Entity entity) {
        this(entity == null ? _ -> {} : entity::notifyMetadataChanges);
    }

    public MetadataHolder(Consumer<Map<Integer, Metadata.Entry<?>>> changesListener) {
        this.changesListener = Objects.requireNonNull(changesListener, "changesListener");
    }

    @SuppressWarnings("unchecked")
    public <T extends @UnknownNullability Object> T get(MetadataDef.Entry<T> entry) {
        final int id = entry.index();

        final Metadata.Entry<?> value = this.entries.get(id);
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
                Metadata.Entry<?> currentEntry = this.entries.get(id);
                byte maskValue = currentEntry != null ? (byte) currentEntry.value() : 0;
                maskValue = setMaskBit(maskValue, bitMask.bitMask(), (Boolean) value);
                yield Metadata.Byte(maskValue);
            }
            case MetadataDef.Entry.ByteMask byteMask -> {
                Metadata.Entry<?> currentEntry = this.entries.get(id);
                byte maskValue = currentEntry != null ? (byte) currentEntry.value() : 0;
                maskValue = setMaskByte(maskValue, byteMask.byteMask(), byteMask.offset(), (Byte) value);
                yield Metadata.Byte(maskValue);
            }
        };

        this.entries.put(id, result);

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
        Map<Integer, Metadata.Entry<?>> entries;
        synchronized (this.notNotifiedChanges) {
            Map<Integer, Metadata.Entry<?>> awaitingChanges = this.notNotifiedChanges;
            if (awaitingChanges.isEmpty()) return;
            entries = Map.copyOf(awaitingChanges);
            awaitingChanges.clear();
        }
        this.changesListener.accept(entries);
    }

    public Map<Integer, Metadata.Entry<?>> getEntries() {
        return Map.copyOf(this.entries);
    }

    static final Map<String, BiFunction<Entity, MetadataHolder, ? extends EntityMeta>> ENTITY_META_SUPPLIER = Map.ofEntries(
            entry("minecraft:acacia_boat", BoatMeta::new),
            entry("minecraft:acacia_chest_boat", BoatMeta::new),
            entry("minecraft:allay", AllayMeta::new),
            entry("minecraft:area_effect_cloud", AreaEffectCloudMeta::new),
            entry("minecraft:armadillo", ArmadilloMeta::new),
            entry("minecraft:armor_stand", ArmorStandMeta::new),
            entry("minecraft:arrow", ArrowMeta::new),
            entry("minecraft:axolotl", AxolotlMeta::new),
            entry("minecraft:bamboo_raft", BoatMeta::new),
            entry("minecraft:bamboo_chest_raft", BoatMeta::new),
            entry("minecraft:bat", BatMeta::new),
            entry("minecraft:bee", BeeMeta::new),
            entry("minecraft:birch_boat", BoatMeta::new),
            entry("minecraft:birch_chest_boat", BoatMeta::new),
            entry("minecraft:blaze", BlazeMeta::new),
            entry("minecraft:block_display", BlockDisplayMeta::new),
            entry("minecraft:bogged", BoggedMeta::new),
            entry("minecraft:breeze", BreezeMeta::new),
            entry("minecraft:breeze_wind_charge", BreezeWindChargeMeta::new),
            entry("minecraft:camel", CamelMeta::new),
            entry("minecraft:camel_husk", CamelHuskMeta::new),
            entry("minecraft:cat", CatMeta::new),
            entry("minecraft:cave_spider", CaveSpiderMeta::new),
            entry("minecraft:cherry_boat", BoatMeta::new),
            entry("minecraft:cherry_chest_boat", BoatMeta::new),
            entry("minecraft:chicken", ChickenMeta::new),
            entry("minecraft:cod", CodMeta::new),
            entry("minecraft:copper_golem", CopperGolemMeta::new),
            entry("minecraft:cow", CowMeta::new),
            entry("minecraft:creaking", CreakingMeta::new),
            entry("minecraft:creeper", CreeperMeta::new),
            entry("minecraft:dark_oak_boat", BoatMeta::new),
            entry("minecraft:dark_oak_chest_boat", BoatMeta::new),
            entry("minecraft:dolphin", DolphinMeta::new),
            entry("minecraft:donkey", DonkeyMeta::new),
            entry("minecraft:dragon_fireball", DragonFireballMeta::new),
            entry("minecraft:drowned", DrownedMeta::new),
            entry("minecraft:elder_guardian", ElderGuardianMeta::new),
            entry("minecraft:end_crystal", EndCrystalMeta::new),
            entry("minecraft:ender_dragon", EnderDragonMeta::new),
            entry("minecraft:enderman", EndermanMeta::new),
            entry("minecraft:endermite", EndermiteMeta::new),
            entry("minecraft:evoker", EvokerMeta::new),
            entry("minecraft:evoker_fangs", EvokerFangsMeta::new),
            entry("minecraft:experience_orb", ExperienceOrbMeta::new),
            entry("minecraft:eye_of_ender", EyeOfEnderMeta::new),
            entry("minecraft:falling_block", FallingBlockMeta::new),
            entry("minecraft:fireball", FireballMeta::new),
            entry("minecraft:firework_rocket", FireworkRocketMeta::new),
            entry("minecraft:fox", FoxMeta::new),
            entry("minecraft:frog", FrogMeta::new),
            entry("minecraft:ghast", GhastMeta::new),
            entry("minecraft:giant", GiantMeta::new),
            entry("minecraft:glow_item_frame", GlowItemFrameMeta::new),
            entry("minecraft:glow_squid", GlowSquidMeta::new),
            entry("minecraft:goat", GoatMeta::new),
            entry("minecraft:guardian", GuardianMeta::new),
            entry("minecraft:happy_ghast", HappyGhastMeta::new),
            entry("minecraft:hoglin", HoglinMeta::new),
            entry("minecraft:horse", HorseMeta::new),
            entry("minecraft:husk", HuskMeta::new),
            entry("minecraft:illusioner", IllusionerMeta::new),
            entry("minecraft:interaction", InteractionMeta::new),
            entry("minecraft:iron_golem", IronGolemMeta::new),
            entry("minecraft:item", ItemEntityMeta::new),
            entry("minecraft:item_display", ItemDisplayMeta::new),
            entry("minecraft:item_frame", ItemFrameMeta::new),
            entry("minecraft:jungle_boat", BoatMeta::new),
            entry("minecraft:jungle_chest_boat", BoatMeta::new),
            entry("minecraft:leash_knot", LeashKnotMeta::new),
            entry("minecraft:lightning_bolt", LightningBoltMeta::new),
            entry("minecraft:lingering_potion", LingeringPotionMeta::new),
            entry("minecraft:llama", LlamaMeta::new),
            entry("minecraft:llama_spit", LlamaSpitMeta::new),
            entry("minecraft:magma_cube", MagmaCubeMeta::new),
            entry("minecraft:mangrove_boat", BoatMeta::new),
            entry("minecraft:mangrove_chest_boat", BoatMeta::new),
            entry("minecraft:mannequin", MannequinMeta::new),
            entry("minecraft:marker", MarkerMeta::new),
            entry("minecraft:minecart", MinecartMeta::new),
            entry("minecraft:nautilus", NautilusMeta::new),
            entry("minecraft:chest_minecart", ChestMinecartMeta::new),
            entry("minecraft:command_block_minecart", CommandBlockMinecartMeta::new),
            entry("minecraft:furnace_minecart", FurnaceMinecartMeta::new),
            entry("minecraft:hopper_minecart", HopperMinecartMeta::new),
            entry("minecraft:spawner_minecart", SpawnerMinecartMeta::new),
            entry("minecraft:text_display", TextDisplayMeta::new),
            entry("minecraft:tnt_minecart", TntMinecartMeta::new),
            entry("minecraft:mooshroom", MooshroomMeta::new),
            entry("minecraft:mule", MuleMeta::new),
            entry("minecraft:oak_boat", BoatMeta::new),
            entry("minecraft:oak_chest_boat", BoatMeta::new),
            entry("minecraft:ocelot", OcelotMeta::new),
            entry("minecraft:ominous_item_spawner", OminousItemSpawnerMeta::new),
            entry("minecraft:painting", PaintingMeta::new),
            entry("minecraft:pale_oak_boat", BoatMeta::new),
            entry("minecraft:pale_oak_chest_boat", BoatMeta::new),
            entry("minecraft:panda", PandaMeta::new),
            entry("minecraft:parrot", ParrotMeta::new),
            entry("minecraft:parched", ParchedMeta::new),
            entry("minecraft:phantom", PhantomMeta::new),
            entry("minecraft:pig", PigMeta::new),
            entry("minecraft:piglin", PiglinMeta::new),
            entry("minecraft:piglin_brute", PiglinBruteMeta::new),
            entry("minecraft:pillager", PillagerMeta::new),
            entry("minecraft:polar_bear", PolarBearMeta::new),
            entry("minecraft:tnt", PrimedTntMeta::new),
            entry("minecraft:pufferfish", PufferfishMeta::new),
            entry("minecraft:rabbit", RabbitMeta::new),
            entry("minecraft:ravager", RavagerMeta::new),
            entry("minecraft:salmon", SalmonMeta::new),
            entry("minecraft:sheep", SheepMeta::new),
            entry("minecraft:shulker", ShulkerMeta::new),
            entry("minecraft:shulker_bullet", ShulkerBulletMeta::new),
            entry("minecraft:silverfish", SilverfishMeta::new),
            entry("minecraft:skeleton", SkeletonMeta::new),
            entry("minecraft:skeleton_horse", SkeletonHorseMeta::new),
            entry("minecraft:slime", SlimeMeta::new),
            entry("minecraft:small_fireball", SmallFireballMeta::new),
            entry("minecraft:sniffer", SnifferMeta::new),
            entry("minecraft:snow_golem", SnowGolemMeta::new),
            entry("minecraft:snowball", SnowballMeta::new),
            entry("minecraft:spectral_arrow", SpectralArrowMeta::new),
            entry("minecraft:spider", SpiderMeta::new),
            entry("minecraft:splash_potion", SplashPotionMeta::new),
            entry("minecraft:spruce_boat", BoatMeta::new),
            entry("minecraft:spruce_chest_boat", BoatMeta::new),
            entry("minecraft:squid", SquidMeta::new),
            entry("minecraft:stray", StrayMeta::new),
            entry("minecraft:strider", StriderMeta::new),
            entry("minecraft:tadpole", TadpoleMeta::new),
            entry("minecraft:egg", ThrownEggMeta::new),
            entry("minecraft:ender_pearl", ThrownEnderPearlMeta::new),
            entry("minecraft:experience_bottle", ThrownExperienceBottleMeta::new),
            entry("minecraft:potion", SplashPotionMeta::new),
            entry("minecraft:trident", ThrownTridentMeta::new),
            entry("minecraft:trader_llama", TraderLlamaMeta::new),
            entry("minecraft:tropical_fish", TropicalFishMeta::new),
            entry("minecraft:turtle", TurtleMeta::new),
            entry("minecraft:vex", VexMeta::new),
            entry("minecraft:villager", VillagerMeta::new),
            entry("minecraft:vindicator", VindicatorMeta::new),
            entry("minecraft:wandering_trader", WanderingTraderMeta::new),
            entry("minecraft:warden", WardenMeta::new),
            entry("minecraft:wind_charge", WindChargeMeta::new),
            entry("minecraft:witch", WitchMeta::new),
            entry("minecraft:wither", WitherMeta::new),
            entry("minecraft:wither_skeleton", WitherSkeletonMeta::new),
            entry("minecraft:wither_skull", WitherSkullMeta::new),
            entry("minecraft:wolf", WolfMeta::new),
            entry("minecraft:zoglin", ZoglinMeta::new),
            entry("minecraft:zombie", ZombieMeta::new),
            entry("minecraft:zombie_horse", ZombieHorseMeta::new),
            entry("minecraft:zombie_nautilus", ZombieNautilusMeta::new),
            entry("minecraft:zombie_villager", ZombieVillagerMeta::new),
            entry("minecraft:zombified_piglin", ZombifiedPiglinMeta::new),
            entry("minecraft:player", PlayerMeta::new),
            entry("minecraft:fishing_bobber", FishingHookMeta::new)
    );

    @ApiStatus.Internal
    public static EntityMeta createMeta(EntityType entityType, Entity entity, MetadataHolder metadata) {
        return ENTITY_META_SUPPLIER.get(entityType.name()).apply(entity, metadata);
    }
}
