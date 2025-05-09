package net.minestom.server.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.Shape;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockSoundType;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.collection.ObjectArray;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Handles registry data, used by {@link StaticProtocolObject} implementations and is strictly internal.
 * Use at your own risk.
 */
public final class Registry {
    static final Gson GSON = new GsonBuilder().disableHtmlEscaping().disableJdkUnsafe().create();

    @ApiStatus.Internal
    public static BlockEntry block(String namespace, @NotNull Properties main) {
        return new BlockEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static MaterialEntry material(String namespace, @NotNull Properties main) {
        return new MaterialEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static EntityEntry entity(String namespace, @NotNull Properties main) {
        return new EntityEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static VillagerProfessionEntry villagerProfession(String namespace, @NotNull Properties main) {
        return new VillagerProfessionEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static FeatureFlagEntry featureFlag(String namespace, @NotNull Properties main) {
        return new FeatureFlagEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static PotionEffectEntry potionEffect(String namespace, @NotNull Properties main) {
        return new PotionEffectEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static AttributeEntry attribute(String namespace, @NotNull Properties main) {
        return new AttributeEntry(namespace, main, null);
    }

    public static GameEventEntry gameEventEntry(String namespace, Properties properties) {
        return new GameEventEntry(namespace, properties, null);
    }

    public static BlockSoundTypeEntry blockSoundTypeEntry(String namespace, Properties properties) {
        return new BlockSoundTypeEntry(namespace, properties);
    }

    public static @NotNull InputStream loadRegistryFile(@NotNull Resource resource) throws IOException {
        // 1. Try to load from jar resources
        InputStream resourceStream = Registry.class.getClassLoader().getResourceAsStream(resource.name);

        // 2. Try to load from working directory
        if (resourceStream == null && Files.exists(Path.of(resource.name))) {
            resourceStream = Files.newInputStream(Path.of(resource.name));
        }

        // 3. Not found :(
        Check.notNull(resourceStream, "Resource {0} does not exist!", resource);
        return resourceStream;
    }

    @ApiStatus.Internal
    public static Map<String, Map<String, Object>> load(Resource resource) {
        Map<String, Map<String, Object>> map = new HashMap<>();
        try (InputStream resourceStream = loadRegistryFile(resource)) {
            try (JsonReader reader = new JsonReader(new InputStreamReader(resourceStream))) {
                reader.beginObject();
                while (reader.hasNext()) map.put(reader.nextName(), (Map<String, Object>) readObject(reader));
                reader.endObject();
            }
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
        return map;
    }

    @ApiStatus.Internal
    public static <T extends StaticProtocolObject> Container<T> createStaticContainer(Resource resource, Container.Loader<T> loader) {
        var entries = Registry.load(resource);
        final var requiresId = resource.requiresId();
        Map<String, T> namespaces = new HashMap<>(entries.size());
        ObjectArray<T> ids = requiresId ? ObjectArray.singleThread(entries.size()) : null;
        for (var entry : entries.entrySet()) {
            final String namespace = entry.getKey();
            final Properties properties = Properties.fromMap(entry.getValue());
            final T value = loader.get(namespace, properties);
            namespaces.put(value.name(), value);
            if (!requiresId) continue;
            assert ids.get(value.id()) == null: "Duplicate id " + value.id() + " for " + value.name() + " in " + resource.name();
            ids.set(value.id(), value);
        }
        //noinspection unchecked
        final List<T> computedIds = requiresId
                ? List.of(ids.arrayCopy((Class<T>) StaticProtocolObject.class))
                : List.of();

        return new Container<>(resource, namespaces, computedIds);
    }

    @ApiStatus.Internal
    public record Container<T extends StaticProtocolObject>(@NotNull Resource resource,
                                                            @NotNull Map<String, T> namespaces,
                                                            @NotNull List<T> ids) {
        public Container {
            namespaces = Map.copyOf(namespaces);
            ids = List.copyOf(ids);
        }

        public T get(@NotNull String namespace) {
            return namespaces.get(namespace);
        }

        public T getSafe(@NotNull String namespace) {
            return get(namespace.contains(":") ? namespace : "minecraft:" + namespace);
        }

        public T getId(int id) {
            return ids.get(id);
        }

        public int toId(@NotNull String namespace) {
            return get(namespace).id();
        }

        public Collection<T> values() {
            return namespaces.values();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Container<?> container)) return false;
            return resource == container.resource;
        }

        @Override
        public int hashCode() {
            return Objects.hash(resource);
        }

        public interface Loader<T extends ProtocolObject> {
            T get(String namespace, Properties properties);
        }
    }

    @ApiStatus.Internal
    public enum Resource {
        BLOCKS("blocks.json"),
        ITEMS("items.json"),
        ENTITIES("entities.json"),
        FEATURE_FLAGS("feature_flags.json"),
        SOUNDS("sounds.json"),
        STATISTICS("custom_statistics.json"),
        POTION_EFFECTS("potion_effects.json"),
        POTION_TYPES("potions.json"),
        PARTICLES("particles.json"),
        DAMAGE_TYPES("damage_types.json"),
        TRIM_MATERIALS("trim_materials.json"),
        TRIM_PATTERNS("trim_patterns.json"),
        BLOCK_TAGS("tags/block.json"),
        ENTITY_TYPE_TAGS("tags/entity_type.json"),
        FLUID_TAGS("tags/fluid.json"),
        GAMEPLAY_TAGS("tags/game_event.json"),
        GAME_EVENTS("game_events.json"),
        ITEM_TAGS("tags/item.json"),
        ENCHANTMENT_TAGS("tags/enchantment.json"),
        BIOME_TAGS("tags/biome.json"),
        DIMENSION_TYPES("dimension_types.json"),
        BIOMES("biomes.json"),
        ATTRIBUTES("attributes.json"),
        BANNER_PATTERNS("banner_patterns.json"),
        PAINTING_VARIANTS("painting_variants.json"),
        WOLF_VARIANTS("wolf_variants.json"),
        WOLF_SOUND_VARIANTS("wolf_sound_variants.json"),
        CAT_VARIANTS("cat_variants.json"),
        CHICKEN_VARIANTS("chicken_variants.json"),
        COW_VARIANTS("cow_variants.json"),
        FROG_VARIANTS("frog_variants.json"),
        PIG_VARIANTS("pig_variants.json"),
        CHAT_TYPES("chat_types.json"),
        ENCHANTMENTS("enchantments.json"),
        JUKEBOX_SONGS("jukebox_songs.json"),
        VILLAGER_PROFESSIONS("villager_professions.json"),
        INSTRUMENTS("instruments.json"),
        INSTRUMENT_TAGS("tags/instrument.json"),
        BLOCK_SOUND_TYPES("block_sound_types.json");

        private final String name;

        Resource(String name) {
            this.name = name;
        }

        public @NotNull String fileName() {
            return name;
        }

        public boolean requiresId() {
            // no reason to add another field for this is only used for BLOCK_SOUND_TYPES
            return this != BLOCK_SOUND_TYPES;
        }
    }

    public record GameEventEntry(Key key, Properties main, Properties custom) implements Entry {
        public GameEventEntry(String key, Properties main, Properties custom) {
            this(Key.key(key), main, custom);
        }
    }

    public static final class BlockEntry implements Entry {
        private final Key key;
        private final int id;
        private final int stateId;
        private final String translationKey;
        private final double hardness;
        private final double explosionResistance;
        private final double friction;
        private final double speedFactor;
        private final double jumpFactor;
        private final boolean air;
        private final boolean solid;
        private final boolean liquid;
        private final boolean occludes;
        private final boolean requiresTool;
        private final int lightEmission;
        private final boolean replaceable;
        private final String blockEntity;
        private final int blockEntityId;
        private final Supplier<Material> materialSupplier;
        private final BlockSoundType blockSoundType;
        private final Shape shape;
        private final boolean redstoneConductor;
        private final boolean signalSource;
        private final Properties custom;

        private BlockEntry(String namespace, Properties main, Properties custom) {
            this.custom = custom;
            this.key = Key.key(namespace);
            this.id = main.getInt("id");
            this.stateId = main.getInt("stateId");
            this.translationKey = main.getString("translationKey");
            this.hardness = main.getDouble("hardness");
            this.explosionResistance = main.getDouble("explosionResistance");
            this.friction = main.getDouble("friction");
            this.speedFactor = main.getDouble("speedFactor", 1);
            this.jumpFactor = main.getDouble("jumpFactor", 1);
            this.air = main.getBoolean("air", false);
            this.solid = main.getBoolean("solid");
            this.liquid = main.getBoolean("liquid", false);
            this.occludes = main.getBoolean("occludes", true);
            this.requiresTool = main.getBoolean("requiresTool", true);
            this.lightEmission = main.getInt("lightEmission", 0);
            this.replaceable = main.getBoolean("replaceable", false);
            this.blockSoundType = BlockSoundType.fromKey(main.getString("soundType"));
            {
                Properties blockEntity = main.section("blockEntity");
                if (blockEntity != null) {
                    this.blockEntity = blockEntity.getString("namespace");
                    this.blockEntityId = blockEntity.getInt("id");
                } else {
                    this.blockEntity = null;
                    this.blockEntityId = 0;
                }
            }
            {
                final String materialNamespace = main.getString("correspondingItem", null);
                this.materialSupplier = materialNamespace != null ? () -> Material.fromKey(materialNamespace) : () -> null;
            }
            {
                final String collision = main.getString("collisionShape");
                final String occlusion = main.getString("occlusionShape");
                this.shape = CollisionUtils.parseBlockShape(collision, occlusion, this);
            }
            this.redstoneConductor = main.getBoolean("redstoneConductor");
            this.signalSource = main.getBoolean("signalSource", false);
        }

        public @NotNull Key key() {
            return key;
        }

        public int id() {
            return id;
        }

        public int stateId() {
            return stateId;
        }

        public String translationKey() {
            return translationKey;
        }

        public double hardness() {
            return hardness;
        }

        public double explosionResistance() {
            return explosionResistance;
        }

        public double friction() {
            return friction;
        }

        public double speedFactor() {
            return speedFactor;
        }

        public double jumpFactor() {
            return jumpFactor;
        }

        public boolean isAir() {
            return air;
        }

        public boolean isSolid() {
            return solid;
        }

        public boolean isLiquid() {
            return liquid;
        }

        public boolean occludes() {
            return occludes;
        }

        public boolean requiresTool() {
            return requiresTool;
        }

        public int lightEmission() {
            return lightEmission;
        }

        public boolean isReplaceable() {
            return replaceable;
        }

        public boolean isBlockEntity() {
            return blockEntity != null;
        }

        public @Nullable String blockEntity() {
            return blockEntity;
        }

        public int blockEntityId() {
            return blockEntityId;
        }

        public @Nullable Material material() {
            return materialSupplier.get();
        }

        public boolean isRedstoneConductor() {
            return redstoneConductor;
        }

        public boolean isSignalSource() {
            return signalSource;
        }

        public Shape collisionShape() {
            return shape;
        }

        public @Nullable BlockSoundType getBlockSoundType() {
            return this.blockSoundType;
        }

        @Override
        public Properties custom() {
            return custom;
        }
    }

    public static final class MaterialEntry implements Entry {
        private final Key key;
        private final Properties main;
        private final int id;
        private final String translationKey;
        private final Supplier<Block> blockSupplier;
        private DataComponentMap prototype;

        private final EquipmentSlot equipmentSlot;
        private final EntityType entityType;
        private final Properties custom;

        private MaterialEntry(String namespace, Properties main, Properties custom) {
            this.main = main;
            this.custom = custom;
            this.key = Key.key(namespace);
            this.id = main.getInt("id");
            this.translationKey = main.getString("translationKey");
            {
                final String blockNamespace = main.getString("correspondingBlock", null);
                this.blockSupplier = blockNamespace != null ? () -> Block.fromKey(blockNamespace) : () -> null;
            }
            {
                final Properties armorProperties = main.section("armorProperties");
                if (armorProperties != null) {
                    switch (armorProperties.getString("slot")) {
                        case "feet" -> this.equipmentSlot = EquipmentSlot.BOOTS;
                        case "legs" -> this.equipmentSlot = EquipmentSlot.LEGGINGS;
                        case "chest" -> this.equipmentSlot = EquipmentSlot.CHESTPLATE;
                        case "head" -> this.equipmentSlot = EquipmentSlot.HELMET;
                        default -> this.equipmentSlot = null;
                    }
                } else {
                    this.equipmentSlot = null;
                }
            }
            {
                final Properties spawnEggProperties = main.section("spawnEggProperties");
                if (spawnEggProperties != null) {
                    this.entityType = EntityType.fromKey(spawnEggProperties.getString("entityType"));
                } else {
                    this.entityType = null;
                }
            }
        }

        public @NotNull Key key() {
            return key;
        }

        public int id() {
            return id;
        }

        public @NotNull String translationKey() {
            return translationKey;
        }

        public @Nullable Block block() {
            return blockSupplier.get();
        }

        public @NotNull DataComponentMap prototype() {
            if (prototype == null) {
                final Transcoder<Object> coder = new RegistryTranscoder<>(Transcoder.JAVA, MinecraftServer.process());
                DataComponentMap.Builder builder = DataComponentMap.builder();
                for (Map.Entry<String, Object> entry : main.section("components")) {
                    //noinspection unchecked
                    DataComponent<Object> component = (DataComponent<Object>) DataComponent.fromKey(entry.getKey());
                    Check.notNull(component, "Unknown component {0} in {1}", entry.getKey(), key);

                    final Result<Object> result = component.decode(coder, entry.getValue());
                    switch (result) {
                        case Result.Ok(Object ok) -> builder.set(component, ok);
                        case Result.Error(String message) ->
                                throw new IllegalStateException("Failed to decode component " + entry.getKey() + " in " + key + ": " + message);
                    }
                }
                this.prototype = builder.build();
            }

            return prototype;
        }

        public boolean isArmor() {
            return equipmentSlot != null;
        }

        public @Nullable EquipmentSlot equipmentSlot() {
            return equipmentSlot;
        }

        /**
         * Gets the entity type this item can spawn. Only present for spawn eggs (e.g. wolf spawn egg, skeleton spawn egg)
         *
         * @return The entity type it can spawn, or null if it is not a spawn egg
         */
        public @Nullable EntityType spawnEntityType() {
            return entityType;
        }

        @Override
        public Properties custom() {
            return custom;
        }
    }

    public static final class EntityEntry implements Entry {
        private final Key key;
        private final int id;
        private final String translationKey;
        private final double drag;
        private final double acceleration;
        private final boolean isLiving;
        private final double width;
        private final double height;
        private final double eyeHeight;
        private final int clientTrackingRange;
        private final boolean fireImmune;
        private final Map<String, List<Double>> entityOffsets;
        private final BoundingBox boundingBox;
        private final Properties custom;

        public EntityEntry(String namespace, Properties main, Properties custom) {
            this.key = Key.key(namespace);
            this.id = main.getInt("id");
            this.translationKey = main.getString("translationKey");
            this.drag = main.getDouble("drag", 0.02);
            this.acceleration = main.getDouble("acceleration", 0.08);
            final String packetType = main.getString("packetType").toUpperCase(Locale.ROOT);
            this.isLiving = "LIVING".equals(packetType) || "PLAYER".equals(packetType);
            this.fireImmune = main.getBoolean("fireImmune", false);
            this.clientTrackingRange = main.getInt("clientTrackingRange");

            // Dimensions
            this.width = main.getDouble("width");
            this.height = main.getDouble("height");
            this.eyeHeight = main.getDouble("eyeHeight");
            this.boundingBox = new BoundingBox(this.width, this.height, this.width);

            // Attachments
            this.entityOffsets = new HashMap<>();
            Properties attachments = main.section("attachments");
            if (attachments != null) {
                var allAttachments = attachments.asMap().keySet();
                for (String key : allAttachments) {
                    var offset = attachments.getNestedDoubleArray(key);
                    this.entityOffsets.put(key, offset.getFirst()); // It's an array of an array with a single element, as of 1.21.3 we only need to grab a single array of 3 doubles
                }
            }

            this.custom = custom;
        }

        public @NotNull Key key() {
            return key;
        }

        public int id() {
            return id;
        }

        public String translationKey() {
            return translationKey;
        }

        public double drag() {
            return drag;
        }

        public double acceleration() {
            return acceleration;
        }

        public double horizontalAirResistance() {
            return isLiving ? 0.91 : 0.98;
        }

        public double verticalAirResistance() {
            return 1 - drag();
        }

        public boolean shouldSendAttributes() {
            return isLiving;
        }

        public double width() {
            return width;
        }

        public double height() {
            return height;
        }

        public double eyeHeight() {
            return eyeHeight;
        }

        public boolean fireImmune() {
            return fireImmune;
        }

        public int clientTrackingRange() {
            return clientTrackingRange;
        }

        /**
         *
         * Gets the entity attachment by name. Typically, will be PASSENGER or VEHICLE, but some entities have custom attachments (e.g. WARDEN_CHEST, NAMETAG)
         * @param attachmentName The attachment to retrieve
         * @return A list of 3 doubles if the attachment is defined for this entity, or null if it is not defined
         */
        public @Nullable List<Double> entityAttachment(@NotNull String attachmentName) {
            return entityOffsets.get(attachmentName);
        }

        public @NotNull BoundingBox boundingBox() {
            return boundingBox;
        }

        @Override
        public Properties custom() {
            return custom;
        }
    }

    public static final class VillagerProfessionEntry implements Entry {
        private final Key key;
        private final int id;
        private final SoundEvent workSound;
        private final Properties custom;

        public VillagerProfessionEntry(String namespace, Properties main, Properties custom) {
            this.key = Key.key(namespace);
            this.id = main.getInt("id");
            if (main.containsKey("workSound")) {
                this.workSound = SoundEvent.fromKey(main.getString("workSound"));
            } else {
                this.workSound = null;
            }
            this.custom = custom;
        }

        public @NotNull Key key() {
            return key;
        }

        public int id() {
            return id;
        }

        public @Nullable SoundEvent workSound() {
            return workSound;
        }

        @Override
        public Properties custom() {
            return custom;
        }
    }

    public record FeatureFlagEntry(Key key, int id, Properties custom) implements Entry {
        public FeatureFlagEntry(String namespace, Properties main, Properties custom) {
            this(Key.key(namespace),
                    main.getInt("id"),
                    null
            );
        }
    }

    public record PotionEffectEntry(Key key, int id,
                                    String translationKey,
                                    int color,
                                    boolean isInstantaneous,
                                    Properties custom) implements Entry {
        public PotionEffectEntry(String namespace, Properties main, Properties custom) {
            this(Key.key(namespace),
                    main.getInt("id"),
                    main.getString("translationKey"),
                    main.getInt("color"),
                    main.getBoolean("instantaneous"),
                    custom);
        }
    }

    public record AttributeEntry(Key key, int id,
                                 String translationKey, double defaultValue,
                                 boolean clientSync,
                                 double maxValue, double minValue,
                                 Properties custom) implements Entry {
        public AttributeEntry(String namespace, Properties main, Properties custom) {
            this(Key.key(namespace),
                    main.getInt("id"),
                    main.getString("translationKey"),
                    main.getDouble("defaultValue"),
                    main.getBoolean("clientSync"),
                    main.getDouble("maxValue"),
                    main.getDouble("minValue"),
                    custom);
        }
    }

    public record BlockSoundTypeEntry(@NotNull Key key, float volume, float pitch,
                                      SoundEvent breakSound, SoundEvent hitSound, SoundEvent fallSound,
                                      SoundEvent placeSound, SoundEvent stepSound) {
        public BlockSoundTypeEntry(String namespace, Properties main) {
            this(Key.key(namespace), main.getFloat("volume"),
                    main.getFloat("pitch"), SoundEvent.fromKey(main.getString("breakSound")), SoundEvent.fromKey(main.getString("hitSound")),
                    SoundEvent.fromKey(main.getString("fallSound")), SoundEvent.fromKey(main.getString("placeSound")), SoundEvent.fromKey(main.getString("stepSound")));
        }
    }

    public interface Entry {
        @ApiStatus.Experimental
        Properties custom();
    }

    private static Object readObject(JsonReader reader) throws IOException {
        return switch (reader.peek()) {
            case BEGIN_ARRAY -> {
                List<Object> list = new ArrayList<>();
                reader.beginArray();
                while (reader.hasNext()) list.add(readObject(reader));
                reader.endArray();
                yield list;
            }
            case BEGIN_OBJECT -> {
                Map<String, Object> map = new HashMap<>();
                reader.beginObject();
                while (reader.hasNext()) map.put(reader.nextName(), readObject(reader));
                reader.endObject();
                yield map;
            }
            case STRING -> reader.nextString();
            case NUMBER -> ToNumberPolicy.LONG_OR_DOUBLE.readNumber(reader);
            case BOOLEAN -> reader.nextBoolean();
            default -> throw new IllegalStateException("Invalid peek: " + reader.peek());
        };
    }

    record PropertiesMap(Map<String, Object> map) implements Properties {
        @Override
        public String getString(String name, String defaultValue) {
            var element = element(name);
            return element != null ? (String) element : defaultValue;
        }

        @Override
        public String getString(String name) {
            return element(name);
        }

        @Override
        public double getDouble(String name, double defaultValue) {
            var element = element(name);
            return element != null ? ((Number) element).doubleValue() : defaultValue;
        }

        @Override
        public double getDouble(String name) {
            return ((Number) element(name)).doubleValue();
        }

        @Override
        public int getInt(String name, int defaultValue) {
            var element = element(name);
            return element != null ? ((Number) element).intValue() : defaultValue;
        }

        @Override
        public int getInt(String name) {
            return ((Number) element(name)).intValue();
        }

        @Override
        public float getFloat(String name, float defaultValue) {
            var element = element(name);
            return element != null ? ((Number) element).floatValue() : defaultValue;
        }

        @Override
        public float getFloat(String name) {
            return ((Number) element(name)).floatValue();
        }

        @Override
        public boolean getBoolean(String name, boolean defaultValue) {
            var element = element(name);
            return element != null ? (boolean) element : defaultValue;
        }

        @Override
        public List<List<Double>> getNestedDoubleArray(String name) {
            var element = element(name);
            return element != null ? (List<List<Double>>) element : List.of();
        }

        @Override
        public boolean getBoolean(String name) {
            return element(name);
        }

        @Override
        public Properties section(String name) {
            Map<String, Object> map = element(name);
            if (map == null) return null;
            return new PropertiesMap(map);
        }

        @Override
        public boolean containsKey(String name) {
            return map.containsKey(name);
        }

        @Override
        public Map<String, Object> asMap() {
            return map;
        }

        private <T> T element(String name) {
            //noinspection unchecked
            return (T) map.get(name);
        }

        @Override
        public String toString() {
            AtomicReference<String> string = new AtomicReference<>("{ ");
            this.map.forEach((s, object) -> string.set(string.get() + " , " + "\"" + s + "\"" + " : " + "\"" + object.toString() + "\""));
            return string.updateAndGet(s -> s.replaceFirst(" , ", "") + "}");
        }

    }

    public interface Properties extends Iterable<Map.Entry<String, Object>> {
        static Properties fromMap(Map<String, Object> map) {
            return new PropertiesMap(map);
        }

        String getString(String name, String defaultValue);

        String getString(String name);

        double getDouble(String name, double defaultValue);

        double getDouble(String name);

        int getInt(String name, int defaultValue);

        int getInt(String name);

        float getFloat(String name, float defaultValue);

        float getFloat(String name);

        boolean getBoolean(String name, boolean defaultValue);

        boolean getBoolean(String name);

        List<List<Double>> getNestedDoubleArray(String name);

        Properties section(String name);

        boolean containsKey(String name);

        Map<String, Object> asMap();

        @Override
        default @NotNull Iterator<Map.Entry<String, Object>> iterator() {
            return asMap().entrySet().iterator();
        }

        default int size() {
            return asMap().size();
        }
    }
}
