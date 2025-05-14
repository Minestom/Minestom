package net.minestom.server.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.entity.metadata.ambient.BatMeta;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.ParrotMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.entity.metadata.flying.GhastMeta;
import net.minestom.server.entity.metadata.flying.PhantomMeta;
import net.minestom.server.entity.metadata.golem.IronGolemMeta;
import net.minestom.server.entity.metadata.golem.ShulkerMeta;
import net.minestom.server.entity.metadata.golem.SnowGolemMeta;
import net.minestom.server.entity.metadata.item.*;
import net.minestom.server.entity.metadata.minecart.*;
import net.minestom.server.entity.metadata.monster.*;
import net.minestom.server.entity.metadata.monster.raider.*;
import net.minestom.server.entity.metadata.monster.skeleton.BoggedMeta;
import net.minestom.server.entity.metadata.monster.skeleton.SkeletonMeta;
import net.minestom.server.entity.metadata.monster.skeleton.StrayMeta;
import net.minestom.server.entity.metadata.monster.skeleton.WitherSkeletonMeta;
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
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class MetadataHolder {
    private static final VarHandle NOTIFIED_CHANGES;

    static {
        try {
            NOTIFIED_CHANGES = MethodHandles.lookup().findVarHandle(MetadataHolder.class, "notifyAboutChanges", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Entity entity;
    private final Int2ObjectMap<Metadata.Entry<?>> entries = new Int2ObjectOpenHashMap<>();

    @SuppressWarnings("FieldMayBeFinal")
    private volatile boolean notifyAboutChanges = true;
    private final Map<Integer, Metadata.Entry<?>> notNotifiedChanges = new HashMap<>();

    public MetadataHolder(@Nullable Entity entity) {
        this.entity = entity;
    }

    public <T> T get(MetadataDef.@NotNull Entry<T> entry) {
        final int id = entry.index();

        final Metadata.Entry<?> value = this.entries.get(id);
        if (value == null) return entry.defaultValue();
        return switch (entry) {
            case MetadataDef.Entry.Index<T> v -> (T) value.value();
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

    public <T> void set(MetadataDef.@NotNull Entry<T> entry, T value) {
        final int id = entry.index();

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
        final Entity entity = this.entity;
        if (entity != null && entity.isActive()) {
            if (!this.notifyAboutChanges) {
                synchronized (this.notNotifiedChanges) {
                    this.notNotifiedChanges.put(id, result);
                }
            } else {
                entity.sendPacketToViewersAndSelf(new EntityMetaDataPacket(entity.getEntityId(), Map.of(id, result)));
            }
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
        final Entity entity = this.entity;
        if (entity == null || !entity.isActive()) return;
        Map<Integer, Metadata.Entry<?>> entries;
        synchronized (this.notNotifiedChanges) {
            Map<Integer, Metadata.Entry<?>> awaitingChanges = this.notNotifiedChanges;
            if (awaitingChanges.isEmpty()) return;
            entries = Map.copyOf(awaitingChanges);
            awaitingChanges.clear();
        }
        entity.sendPacketToViewersAndSelf(new EntityMetaDataPacket(entity.getEntityId(), entries));
    }

    public @NotNull Map<Integer, Metadata.Entry<?>> getEntries() {
        return Map.copyOf(this.entries);
    }

    static final Map<String, BiFunction<Entity, MetadataHolder, EntityMeta>> ENTITY_META_SUPPLIER = createMetaMap();

    @ApiStatus.Internal
    public static EntityMeta createMeta(
            @NotNull EntityType entityType,
            @Nullable Entity entity,
            @NotNull MetadataHolder metadata
    ) {
        return ENTITY_META_SUPPLIER.get(entityType.name()).apply(entity, metadata);
    }

    private static Map<String, BiFunction<Entity, MetadataHolder, EntityMeta>> createMetaMap() {
        final Map<String, BiFunction<Entity, MetadataHolder, EntityMeta>> map = new HashMap<>();
        map.put("minecraft:acacia_boat", BoatMeta::new);
        map.put("minecraft:acacia_chest_boat", BoatMeta::new);
        map.put("minecraft:allay", AllayMeta::new);
        map.put("minecraft:area_effect_cloud", AreaEffectCloudMeta::new);
        map.put("minecraft:armadillo", ArmadilloMeta::new);
        map.put("minecraft:armor_stand", ArmorStandMeta::new);
        map.put("minecraft:arrow", ArrowMeta::new);
        map.put("minecraft:axolotl", AxolotlMeta::new);
        map.put("minecraft:bamboo_raft", BoatMeta::new);
        map.put("minecraft:bamboo_chest_raft", BoatMeta::new);
        map.put("minecraft:bat", BatMeta::new);
        map.put("minecraft:bee", BeeMeta::new);
        map.put("minecraft:birch_boat", BoatMeta::new);
        map.put("minecraft:birch_chest_boat", BoatMeta::new);
        map.put("minecraft:blaze", BlazeMeta::new);
        map.put("minecraft:block_display", BlockDisplayMeta::new);
        map.put("minecraft:bogged", BoggedMeta::new);
        map.put("minecraft:breeze", BreezeMeta::new);
        map.put("minecraft:breeze_wind_charge", BreezeWindChargeMeta::new);
        map.put("minecraft:camel", CamelMeta::new);
        map.put("minecraft:cat", CatMeta::new);
        map.put("minecraft:cave_spider", CaveSpiderMeta::new);
        map.put("minecraft:cherry_boat", BoatMeta::new);
        map.put("minecraft:cherry_chest_boat", BoatMeta::new);
        map.put("minecraft:chicken", ChickenMeta::new);
        map.put("minecraft:cod", CodMeta::new);
        map.put("minecraft:cow", CowMeta::new);
        map.put("minecraft:creaking", CreakingMeta::new);
        map.put("minecraft:creeper", CreeperMeta::new);
        map.put("minecraft:dark_oak_boat", BoatMeta::new);
        map.put("minecraft:dark_oak_chest_boat", BoatMeta::new);
        map.put("minecraft:dolphin", DolphinMeta::new);
        map.put("minecraft:donkey", DonkeyMeta::new);
        map.put("minecraft:dragon_fireball", DragonFireballMeta::new);
        map.put("minecraft:drowned", DrownedMeta::new);
        map.put("minecraft:elder_guardian", ElderGuardianMeta::new);
        map.put("minecraft:end_crystal", EndCrystalMeta::new);
        map.put("minecraft:ender_dragon", EnderDragonMeta::new);
        map.put("minecraft:enderman", EndermanMeta::new);
        map.put("minecraft:endermite", EndermiteMeta::new);
        map.put("minecraft:evoker", EvokerMeta::new);
        map.put("minecraft:evoker_fangs", EvokerFangsMeta::new);
        map.put("minecraft:experience_orb", ExperienceOrbMeta::new);
        map.put("minecraft:eye_of_ender", EyeOfEnderMeta::new);
        map.put("minecraft:falling_block", FallingBlockMeta::new);
        map.put("minecraft:fireball", FireballMeta::new);
        map.put("minecraft:firework_rocket", FireworkRocketMeta::new);
        map.put("minecraft:fox", FoxMeta::new);
        map.put("minecraft:frog", FrogMeta::new);
        map.put("minecraft:ghast", GhastMeta::new);
        map.put("minecraft:giant", GiantMeta::new);
        map.put("minecraft:glow_item_frame", GlowItemFrameMeta::new);
        map.put("minecraft:glow_squid", GlowSquidMeta::new);
        map.put("minecraft:goat", GoatMeta::new);
        map.put("minecraft:guardian", GuardianMeta::new);
        map.put("minecraft:happy_ghast", HappyGhastMeta::new);
        map.put("minecraft:hoglin", HoglinMeta::new);
        map.put("minecraft:horse", HorseMeta::new);
        map.put("minecraft:husk", HuskMeta::new);
        map.put("minecraft:illusioner", IllusionerMeta::new);
        map.put("minecraft:interaction", InteractionMeta::new);
        map.put("minecraft:iron_golem", IronGolemMeta::new);
        map.put("minecraft:item", ItemEntityMeta::new);
        map.put("minecraft:item_display", ItemDisplayMeta::new);
        map.put("minecraft:item_frame", ItemFrameMeta::new);
        map.put("minecraft:jungle_boat", BoatMeta::new);
        map.put("minecraft:jungle_chest_boat", BoatMeta::new);
        map.put("minecraft:leash_knot", LeashKnotMeta::new);
        map.put("minecraft:lightning_bolt", LightningBoltMeta::new);
        map.put("minecraft:lingering_potion", LingeringPotionMeta::new);
        map.put("minecraft:llama", LlamaMeta::new);
        map.put("minecraft:llama_spit", LlamaSpitMeta::new);
        map.put("minecraft:magma_cube", MagmaCubeMeta::new);
        map.put("minecraft:mangrove_boat", BoatMeta::new);
        map.put("minecraft:mangrove_chest_boat", BoatMeta::new);
        map.put("minecraft:marker", MarkerMeta::new);
        map.put("minecraft:minecart", MinecartMeta::new);
        map.put("minecraft:chest_minecart", ChestMinecartMeta::new);
        map.put("minecraft:command_block_minecart", CommandBlockMinecartMeta::new);
        map.put("minecraft:furnace_minecart", FurnaceMinecartMeta::new);
        map.put("minecraft:hopper_minecart", HopperMinecartMeta::new);
        map.put("minecraft:spawner_minecart", SpawnerMinecartMeta::new);
        map.put("minecraft:text_display", TextDisplayMeta::new);
        map.put("minecraft:tnt_minecart", TntMinecartMeta::new);
        map.put("minecraft:mooshroom", MooshroomMeta::new);
        map.put("minecraft:mule", MuleMeta::new);
        map.put("minecraft:oak_boat", BoatMeta::new);
        map.put("minecraft:oak_chest_boat", BoatMeta::new);
        map.put("minecraft:ocelot", OcelotMeta::new);
        map.put("minecraft:ominous_item_spawner", OminousItemSpawnerMeta::new);
        map.put("minecraft:painting", PaintingMeta::new);
        map.put("minecraft:pale_oak_boat", BoatMeta::new);
        map.put("minecraft:pale_oak_chest_boat", BoatMeta::new);
        map.put("minecraft:panda", PandaMeta::new);
        map.put("minecraft:parrot", ParrotMeta::new);
        map.put("minecraft:phantom", PhantomMeta::new);
        map.put("minecraft:pig", PigMeta::new);
        map.put("minecraft:piglin", PiglinMeta::new);
        map.put("minecraft:piglin_brute", PiglinBruteMeta::new);
        map.put("minecraft:pillager", PillagerMeta::new);
        map.put("minecraft:polar_bear", PolarBearMeta::new);
        map.put("minecraft:tnt", PrimedTntMeta::new);
        map.put("minecraft:pufferfish", PufferfishMeta::new);
        map.put("minecraft:rabbit", RabbitMeta::new);
        map.put("minecraft:ravager", RavagerMeta::new);
        map.put("minecraft:salmon", SalmonMeta::new);
        map.put("minecraft:sheep", SheepMeta::new);
        map.put("minecraft:shulker", ShulkerMeta::new);
        map.put("minecraft:shulker_bullet", ShulkerBulletMeta::new);
        map.put("minecraft:silverfish", SilverfishMeta::new);
        map.put("minecraft:skeleton", SkeletonMeta::new);
        map.put("minecraft:skeleton_horse", SkeletonHorseMeta::new);
        map.put("minecraft:slime", SlimeMeta::new);
        map.put("minecraft:small_fireball", SmallFireballMeta::new);
        map.put("minecraft:sniffer", SnifferMeta::new);
        map.put("minecraft:snow_golem", SnowGolemMeta::new);
        map.put("minecraft:snowball", SnowballMeta::new);
        map.put("minecraft:spectral_arrow", SpectralArrowMeta::new);
        map.put("minecraft:spider", SpiderMeta::new);
        map.put("minecraft:splash_potion", SplashPotionMeta::new);
        map.put("minecraft:spruce_boat", BoatMeta::new);
        map.put("minecraft:spruce_chest_boat", BoatMeta::new);
        map.put("minecraft:squid", SquidMeta::new);
        map.put("minecraft:stray", StrayMeta::new);
        map.put("minecraft:strider", StriderMeta::new);
        map.put("minecraft:tadpole", TadpoleMeta::new);
        map.put("minecraft:egg", ThrownEggMeta::new);
        map.put("minecraft:ender_pearl", ThrownEnderPearlMeta::new);
        map.put("minecraft:experience_bottle", ThrownExperienceBottleMeta::new);
        map.put("minecraft:potion", SplashPotionMeta::new);
        map.put("minecraft:trident", ThrownTridentMeta::new);
        map.put("minecraft:trader_llama", TraderLlamaMeta::new);
        map.put("minecraft:tropical_fish", TropicalFishMeta::new);
        map.put("minecraft:turtle", TurtleMeta::new);
        map.put("minecraft:vex", VexMeta::new);
        map.put("minecraft:villager", VillagerMeta::new);
        map.put("minecraft:vindicator", VindicatorMeta::new);
        map.put("minecraft:wandering_trader", WanderingTraderMeta::new);
        map.put("minecraft:warden", WardenMeta::new);
        map.put("minecraft:wind_charge", WindChargeMeta::new);
        map.put("minecraft:witch", WitchMeta::new);
        map.put("minecraft:wither", WitherMeta::new);
        map.put("minecraft:wither_skeleton", WitherSkeletonMeta::new);
        map.put("minecraft:wither_skull", WitherSkullMeta::new);
        map.put("minecraft:wolf", WolfMeta::new);
        map.put("minecraft:zoglin", ZoglinMeta::new);
        map.put("minecraft:zombie", ZombieMeta::new);
        map.put("minecraft:zombie_horse", ZombieHorseMeta::new);
        map.put("minecraft:zombie_villager", ZombieVillagerMeta::new);
        map.put("minecraft:zombified_piglin", ZombifiedPiglinMeta::new);
        map.put("minecraft:player", PlayerMeta::new);
        map.put("minecraft:fishing_bobber", FishingHookMeta::new);
        return Map.copyOf(map);
    }
}
