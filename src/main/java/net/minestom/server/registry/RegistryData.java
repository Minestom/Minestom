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
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockSoundType;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Equippable;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Either;
import net.minestom.server.utils.collection.ObjectArray;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Handles registry data, used by {@link StaticProtocolObject} implementations and is strictly internal.
 * Use at your own risk.
 */
public final class RegistryData {
    static final Gson GSON = new GsonBuilder().disableHtmlEscaping().disableJdkUnsafe().create();

    @ApiStatus.Internal
    public static BlockEntry block(String namespace, Properties main) {
        return new BlockEntry(namespace, main, new HashMap<>(), null, null);
    }

    @ApiStatus.Internal
    public static BlockEntry block(String namespace, Properties main, HashMap<Object, Object> internCache, @Nullable BlockEntry parent, @Nullable Properties parentProperties) {
        return new BlockEntry(namespace, main, internCache, parent, parentProperties);
    }

    @ApiStatus.Internal
    public static MaterialEntry material(String namespace, Properties main) {
        return new MaterialEntry(namespace, main);
    }

    @ApiStatus.Internal
    public static EntityEntry entity(String namespace, Properties main) {
        return new EntityEntry(namespace, main);
    }

    @ApiStatus.Internal
    public static VillagerProfessionEntry villagerProfession(String namespace, Properties main) {
        return new VillagerProfessionEntry(namespace, main);
    }

    @ApiStatus.Internal
    public static FeatureFlagEntry featureFlag(String namespace, Properties main) {
        return new FeatureFlagEntry(namespace, main);
    }

    @ApiStatus.Internal
    public static FluidEntry fluid(String namespace, Properties main) {
        return new FluidEntry(namespace, main);
    }

    @ApiStatus.Internal
    public static PotionEffectEntry potionEffect(String namespace, Properties main) {
        return new PotionEffectEntry(namespace, main);
    }

    @ApiStatus.Internal
    public static AttributeEntry attribute(String namespace, Properties main) {
        return new AttributeEntry(namespace, main);
    }

    public static GameEventEntry gameEventEntry(String namespace, Properties properties) {
        return new GameEventEntry(namespace, properties);
    }

    public static BlockSoundTypeEntry blockSoundTypeEntry(String namespace, Properties properties) {
        return new BlockSoundTypeEntry(namespace, properties);
    }

    /**
     * @param path The path without a leading slash, e.g. "blocks.json"
     */
    public static @Nullable InputStream loadRegistryFile(String path) throws IOException {
        // 1. Try to load from jar resources
        InputStream resourceStream = RegistryData.class.getClassLoader().getResourceAsStream(path);

        // 2. Try to load from working directory
        final Path filesystemPath = Path.of(path);
        if (resourceStream == null && Files.exists(filesystemPath)) {
            resourceStream = Files.newInputStream(filesystemPath);
        }

        // 3. Not found :(
        return resourceStream;
    }

    @ApiStatus.Internal
    public static Properties load(String resourcePath, boolean required) {
        try (InputStream resourceStream = loadRegistryFile(resourcePath)) {
            if (resourceStream != null) {
                final Map<String, Object> map = new HashMap<>();
                try (JsonReader reader = new JsonReader(new InputStreamReader(resourceStream))) {
                    reader.beginObject();
                    while (reader.hasNext()) map.put(reader.nextName(), readObject(reader));
                    reader.endObject();
                }
                return Properties.fromMap(map);
            }
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
        if (required) Check.fail("Failed to load required registry file: {0}", resourcePath);
        return Properties.fromMap(Map.of());
    }

    /**
     * Instantiates a static registry from a resource file. The resource file is resolved using the registryKey
     * first from the classpath, then from the working directory.
     *
     * <p>The data file should be at <code>/{registryKey.path()}.json</code></p>.
     *
     * <p>Tags will be loaded from <code>/tags/{registryKey.path()}.json</code></p>
     */
    @ApiStatus.Internal
    public static <T extends StaticProtocolObject<T>> Registry<T> createStaticRegistry(Key registryKey, Loader<T> loader) {
        // Create the registry (data)
        var entries = RegistryData.load(String.format("%s.json", registryKey.value()), true);
        Map<Key, T> namespaces = new HashMap<>(entries.size());
        ObjectArray<T> ids = ObjectArray.singleThread(entries.size());
        for (var entry : entries.asMap().keySet()) {
            final Properties properties = entries.section(entry);
            final T value = loader.get(entry, properties);
            ids.set(value.id(), value);
            namespaces.put(value.key(), value);
        }
        // Load tags if they exist
        Map<TagKey<T>, RegistryTagImpl.Backed<T>> tags = loadTags(registryKey);
        return new StaticRegistry<>(registryKey, namespaces, ids, tags);
    }

    @ApiStatus.Internal
    static <T> @Unmodifiable Map<TagKey<T>, RegistryTagImpl.Backed<T>> loadTags(Key registryKey) {
        final var tagJson = RegistryData.load(String.format("tags/%s.json", registryKey.value()), false);
        final HashMap<TagKey<T>, RegistryTagImpl.Backed<T>> tags = new HashMap<>(tagJson.size());
        for (String tagName : tagJson.asMap().keySet()) {
            final TagKeyImpl<T> tagKey = new TagKeyImpl<>(Key.key(tagName));
            final RegistryTagImpl.Backed<T> tagValue = tags.computeIfAbsent(tagKey, RegistryTagImpl.Backed::new);
            getTagValues(tagValue, tagJson, tagName);
        }
        return Map.copyOf(tags);
    }

    private static <T> void getTagValues(RegistryTagImpl.Backed<T> tag, Properties main, String value) {
        Properties section = main.section(value);
        final List<String> tagValues = section.getList("values");
        tagValues.forEach(tagString -> {
            if (tagString.startsWith("#")) {
                getTagValues(tag, main, tagString.substring(1));
            } else {
                tag.add(RegistryKey.unsafeOf(tagString));
            }
        });
    }

    public interface Loader<T extends StaticProtocolObject<T>> {
        T get(String namespace, Properties properties);
    }

    @ApiStatus.Internal
    public enum Resource {
        // Dynamic Registries
        BANNER_PATTERNS("banner_pattern.json"),
        BIOMES("biome.json"),
        CAT_VARIANTS("cat_variant.json"),
        CHAT_TYPES("chat_type.json"),
        CHICKEN_VARIANTS("chicken_variant.json"),
        COW_VARIANTS("cow_variant.json"),
        DAMAGE_TYPES("damage_type.json"),
        DIALOGS("dialog.json"),
        DIMENSION_TYPES("dimension_type.json"),
        ENCHANTMENTS("enchantment.json"),
        FROG_VARIANTS("frog_variant.json"),
        JUKEBOX_SONGS("jukebox_song.json"),
        INSTRUMENTS("instrument.json"),
        PAINTING_VARIANTS("painting_variant.json"),
        PIG_VARIANTS("pig_variant.json"),
        TRIM_MATERIALS("trim_material.json"),
        TRIM_PATTERNS("trim_pattern.json"),
        WOLF_VARIANTS("wolf_variant.json"),
        WOLF_SOUND_VARIANTS("wolf_sound_variant.json");

        private final String name;

        Resource(String name) {
            this.name = name;
        }

        public String fileName() {
            return name;
        }
    }

    public record GameEventEntry(Key key, Properties main) implements Entry {
        public GameEventEntry(String key, Properties main) {
            this(Key.key(key), main);
        }
    }

    public static final class BlockEntry implements Entry {
        private static final byte AIR_OFFSET = 1 << 0;
        private static final byte LIQUID_OFFSET = 1 << 1;
        private static final byte SOLID_OFFSET = 1 << 2;
        private static final byte OCCLUDES_OFFSET = 1 << 3;
        private static final byte REQUIRES_TOOL_OFFSET = 1 << 4;
        private static final byte REPLACEABLE_OFFSET = 1 << 5;
        private static final byte REDSTONE_CONDUCTOR_OFFSET = 1 << 6;
        private static final byte SIGNAL_SOURCE_OFFSET = -1 << 7; // 2's complement

        private final Key key;
        private final int id;
        private final int stateId;
        private final String translationKey;
        private final float hardness;
        private final float explosionResistance;
        private final float friction;
        private final float speedFactor;
        private final float jumpFactor;
        private final byte packedFlags;
        private final byte lightEmission;
        private final @Nullable Key blockEntity;
        private final int blockEntityId;
        private final @Nullable Material material;
        private final @Nullable BlockSoundType blockSoundType;
        private final Shape collisionShape;
        private final Shape occlusionShape;

        private BlockEntry(String namespace, Properties main, Map<Object, Object> internCache, @Nullable BlockEntry parent, @Nullable Properties parentProperties) {
            assert parent == null || !main.asMap().isEmpty() : "BlockEntry cannot be empty if it has a parent";
            this.key = parent != null ? parent.key : Key.key(namespace);
            this.id = fromParent(parent, BlockEntry::id, main, "id", Properties::getInt, null);
            this.stateId = fromParent(parent, BlockEntry::stateId, main, "stateId", Properties::getInt, 0); // Parent doesnt have stateId; so we default to 0
            this.translationKey = fromParent(parent, BlockEntry::translationKey, main, "translationKey", Properties::getString, null);
            this.hardness = fromParent(parent, BlockEntry::hardness, main, "hardness", Properties::getFloat, null);
            this.explosionResistance = fromParent(parent, BlockEntry::explosionResistance, main, "explosionResistance", Properties::getFloat, null);
            this.friction = fromParent(parent, BlockEntry::friction, main, "friction", Properties::getFloat, 0.6f);
            this.speedFactor = fromParent(parent, BlockEntry::speedFactor, main, "speedFactor", Properties::getFloat, 1.0f);
            this.jumpFactor = fromParent(parent, BlockEntry::jumpFactor, main, "jumpFactor", Properties::getFloat, 1.0f);
            var air = fromParent(parent, BlockEntry::isAir, main, "air", Properties::getBoolean, false);
            var solid = fromParent(parent, BlockEntry::isSolid, main, "solid", Properties::getBoolean, null);
            var liquid = fromParent(parent, BlockEntry::isLiquid, main, "liquid", Properties::getBoolean, false);
            var occludes = fromParent(parent, BlockEntry::occludes, main, "occludes", Properties::getBoolean, true);
            var requiresTool = fromParent(parent, BlockEntry::requiresTool, main, "requiresTool", Properties::getBoolean, true);
            this.lightEmission = fromParent(parent, BlockEntry::lightEmission, main, "lightEmission", Properties::getInt, 0).byteValue();
            var replaceable = fromParent(parent, BlockEntry::isReplaceable, main, "replaceable", Properties::getBoolean, false);
            this.blockSoundType = fromParent(parent, BlockEntry::getBlockSoundType, main, "soundType", (properties, string) -> {
                final String soundTypeKey = properties.getString(string);
                return soundTypeKey != null ? BlockSoundType.fromKey(soundTypeKey) : null;
            }, null);
            {
                final Properties blockEntity = main.section("blockEntity");
                final Key blockEntityKey = fromParent(parent, BlockEntry::blockEntity, blockEntity, "namespace", (properties, string) -> Key.key(properties.getString(string)), null);
                this.blockEntity = blockEntityKey != null ? (Key) internCache.computeIfAbsent(blockEntityKey, key -> blockEntityKey) : null;
                this.blockEntityId = fromParent(parent, BlockEntry::blockEntityId, blockEntity, "id", Properties::getInt, 0);
            }
            {
                this.material = fromParent(parent, BlockEntry::material, main, "correspondingItem", (properties, string) -> {
                    final String materialNamespace = properties.getString(string);
                    return materialNamespace != null ? Material.fromKey(materialNamespace) : null;
                }, null);
            }
            { // Unique special case where the shape strings can mutate but arent saved after the parse.
                this.collisionShape = fromParent(parent, BlockEntry::collisionShape, main, "collisionShape", (properties, string) -> {
                    String shape = properties.getString(string);
                    return CollisionUtils.parseCollisionShape(internCache, shape);
                }, null);
                this.occlusionShape = fromParent(parent, BlockEntry::occlusionShape, main, "occlusionShape", (properties, string) -> {
                    String shape = properties.getString(string);
                    if (parent == null || parentProperties == null) // No parent, so we can just parse the shape
                        return CollisionUtils.parseOcclusionShape(internCache, shape, occludes, this.lightEmission);
                    // TODO make this condition just change the condition; like adding lightData if emission just changes.
                    if (shape != null || occludes != parent.occludes() || this.lightEmission != parent.lightEmission) {
                        if (shape == null) shape = parentProperties.getString(string);
                        return CollisionUtils.parseOcclusionShape(internCache, shape, occludes, this.lightEmission);
                    }
                    return parent.occlusionShape();
                }, null);
            }
            var redstoneConductor = fromParent(parent, BlockEntry::isRedstoneConductor, main, "redstoneConductor", Properties::getBoolean, null);
            var signalSource = fromParent(parent, BlockEntry::isSignalSource, main, "signalSource", Properties::getBoolean, false);
            this.packedFlags = (byte) (
                    (air ? AIR_OFFSET : 0) |
                    (liquid ? LIQUID_OFFSET : 0) |
                    (solid ? SOLID_OFFSET : 0) |
                    (occludes ? OCCLUDES_OFFSET : 0) |
                    (requiresTool ? REQUIRES_TOOL_OFFSET : 0) |
                    (replaceable ? REPLACEABLE_OFFSET : 0) |
                    (redstoneConductor ? REDSTONE_CONDUCTOR_OFFSET : 0) |
                    (signalSource ? SIGNAL_SOURCE_OFFSET : 0)
            );
        }

        private static <R>  R fromParent(@Nullable BlockEntry parent, Function<BlockEntry, R> parentProperty,
                                @Nullable Properties main, String name, BiFunction<Properties, String, R> function,
                                @Nullable R defaultValue) {
            R value = null;
            if (main != null && main.containsKey(name)) {  // Required to have a nullable properties method
                value = function.apply(main, name);
            }
            if (value == null) {
                if (parent != null) {
                    // If the value is not present in the current properties, we fallback to the parent property
                    value = parentProperty.apply(parent);
                } else {
                    value = defaultValue;
                }
            }
            if (value != defaultValue) Check.notNull(value, "{0}->{1} cannot be null", parent, name);
            return value;
        }

        public Key key() {
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

        public float hardness() {
            return hardness;
        }

        public float explosionResistance() {
            return explosionResistance;
        }

        public float friction() {
            return friction;
        }

        public float speedFactor() {
            return speedFactor;
        }

        public float jumpFactor() {
            return jumpFactor;
        }

        public boolean isAir() {
            return (packedFlags & AIR_OFFSET) != 0;
        }

        public boolean isSolid() {
            return (packedFlags & SOLID_OFFSET) != 0;
        }

        public boolean isLiquid() {
            return (packedFlags & LIQUID_OFFSET) != 0;
        }

        public boolean occludes() {
            return (packedFlags & OCCLUDES_OFFSET) != 0;
        }

        public boolean requiresTool() {
            return (packedFlags & REQUIRES_TOOL_OFFSET) != 0;
        }

        public int lightEmission() {
            return lightEmission;
        }

        public boolean isReplaceable() {
            return (packedFlags & REPLACEABLE_OFFSET) != 0;
        }

        public boolean isBlockEntity() {
            return blockEntity != null;
        }

        public @Nullable Key blockEntity() {
            return blockEntity;
        }

        public int blockEntityId() {
            return blockEntityId;
        }

        public @Nullable Material material() {
            return material;
        }

        public boolean isRedstoneConductor() {
            return (packedFlags & REDSTONE_CONDUCTOR_OFFSET) != 0;
        }

        public boolean isSignalSource() {
            return (packedFlags & SIGNAL_SOURCE_OFFSET) != 0;
        }

        public Shape collisionShape() {
            return collisionShape;
        }

        public Shape occlusionShape() {
            return occlusionShape;
        }

        public @Nullable BlockSoundType getBlockSoundType() {
            return this.blockSoundType;
        }
    }

    public static final class MaterialEntry implements Entry {
        private final Key key;
        private final int id;
        private final String translationKey;
        private final Supplier<Block> blockSupplier;
        private @Nullable Either<Properties, DataComponentMap> prototype;

        private final EntityType entityType;

        private MaterialEntry(String namespace, Properties main) {
            this.prototype = Either.left(main.section("components"));
            this.key = Key.key(namespace);
            this.id = main.getInt("id");
            this.translationKey = main.getString("translationKey");
            {
                final String blockNamespace = main.getString("correspondingBlock", null);
                this.blockSupplier = blockNamespace != null ? () -> Block.fromKey(blockNamespace) : () -> null;
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

        public Key key() {
            return key;
        }

        public int id() {
            return id;
        }

        public String translationKey() {
            return translationKey;
        }

        public @Nullable Block block() {
            return blockSupplier.get();
        }

        public DataComponentMap prototype() {
            if (prototype instanceof Either.Left(var components)) {
                final Transcoder<Object> coder = new RegistryTranscoder<>(Transcoder.JAVA, MinecraftServer.process());
                DataComponentMap.Builder builder = DataComponentMap.builder();
                for (Map.Entry<String, Object> entry : components) {
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
                final DataComponentMap prototype = builder.build();
                this.prototype = !prototype.isEmpty() ? Either.right(prototype) : null;
            }

            return prototype instanceof Either.Right(var dataComponentMap) ? dataComponentMap : DataComponentMap.EMPTY;
        }

        public boolean isArmor() {
            final Equippable equippableComponent = prototype().get(DataComponents.EQUIPPABLE);
            final EquipmentSlot equipmentSlot = equippableComponent == null ? null : equippableComponent.slot();
            return equipmentSlot != null && equipmentSlot.isArmor();
        }

        public @Nullable EquipmentSlot equipmentSlot() {
            final Equippable equippableComponent = prototype().get(DataComponents.EQUIPPABLE);
            return equippableComponent == null ? null : equippableComponent.slot();
        }

        /**
         * Gets the entity type this item can spawn. Only present for spawn eggs (e.g. wolf spawn egg, skeleton spawn egg)
         *
         * @return The entity type it can spawn, or null if it is not a spawn egg
         */
        public @Nullable EntityType spawnEntityType() {
            return entityType;
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

        public EntityEntry(String namespace, Properties main) {
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
            Map<String, List<Double>> entityOffsets = new HashMap<>();
            Properties attachments = main.section("attachments");
            if (attachments != null) {
                var allAttachments = attachments.asMap().keySet();
                for (String key : allAttachments) {
                    List<List<Double>> offset = attachments.getList(key);
                    entityOffsets.put(key, offset.getFirst()); // It's an array of an array with a single element, as of 1.21.3 we only need to grab a single array of 3 doubles
                }
            }
            this.entityOffsets = Map.copyOf(entityOffsets);
        }

        public Key key() {
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
         * Gets the entity attachment by name. Typically, will be PASSENGER or VEHICLE, but some entities have custom attachments (e.g. WARDEN_CHEST, NAMETAG)
         *
         * @param attachmentName The attachment to retrieve
         * @return A list of 3 doubles if the attachment is defined for this entity, or null if it is not defined
         */
        public @Nullable List<Double> entityAttachment(String attachmentName) {
            return entityOffsets.get(attachmentName);
        }

        public BoundingBox boundingBox() {
            return boundingBox;
        }
    }

    public static final class VillagerProfessionEntry implements Entry {
        private final Key key;
        private final int id;
        private final SoundEvent workSound;

        public VillagerProfessionEntry(String namespace, Properties main) {
            this.key = Key.key(namespace);
            this.id = main.getInt("id");
            if (main.containsKey("workSound")) {
                this.workSound = SoundEvent.fromKey(main.getString("workSound"));
            } else {
                this.workSound = null;
            }
        }

        public Key key() {
            return key;
        }

        public int id() {
            return id;
        }

        public @Nullable SoundEvent workSound() {
            return workSound;
        }
    }

    public record FeatureFlagEntry(Key key, int id) implements Entry {
        public FeatureFlagEntry(String namespace, Properties main) {
            this(Key.key(namespace), main.getInt("id"));
        }
    }

    public record FluidEntry(Key key, int id) implements Entry {
        public FluidEntry(String namespace, Properties main) {
            this(Key.key(namespace), main.getInt("id"));
        }
    }

    public record PotionEffectEntry(Key key, int id,
                                    String translationKey,
                                    int color,
                                    boolean isInstantaneous) implements Entry {
        public PotionEffectEntry(String namespace, Properties main) {
            this(Key.key(namespace),
                    main.getInt("id"),
                    main.getString("translationKey"),
                    main.getInt("color"),
                    main.getBoolean("instantaneous"));
        }
    }

    public record AttributeEntry(Key key, int id,
                                 String translationKey, double defaultValue,
                                 boolean clientSync,
                                 double maxValue, double minValue) implements Entry {
        public AttributeEntry(String namespace, Properties main) {
            this(Key.key(namespace),
                    main.getInt("id"),
                    main.getString("translationKey"),
                    main.getDouble("defaultValue"),
                    main.getBoolean("clientSync"),
                    main.getDouble("maxValue"),
                    main.getDouble("minValue"));
        }
    }

    public record BlockSoundTypeEntry(Key key, float volume, float pitch,
                                      SoundEvent breakSound, SoundEvent hitSound, SoundEvent fallSound,
                                      SoundEvent placeSound, SoundEvent stepSound) {
        public BlockSoundTypeEntry(String namespace, Properties main) {
            this(Key.key(namespace), main.getFloat("volume"),
                    main.getFloat("pitch"), SoundEvent.fromKey(main.getString("breakSound")), SoundEvent.fromKey(main.getString("hitSound")),
                    SoundEvent.fromKey(main.getString("fallSound")), SoundEvent.fromKey(main.getString("placeSound")), SoundEvent.fromKey(main.getString("stepSound")));
        }
    }

    public interface Entry {
    }

    private static Object readObject(JsonReader reader) throws IOException {
        return switch (reader.peek()) {
            case BEGIN_ARRAY -> {
                List<Object> list = new ArrayList<>();
                reader.beginArray();
                while (reader.hasNext()) list.add(readObject(reader));
                reader.endArray();
                yield List.copyOf(list);
            }
            case BEGIN_OBJECT -> {
                Map<String, Object> map = new HashMap<>();
                reader.beginObject();
                while (reader.hasNext()) map.put(reader.nextName(), readObject(reader));
                reader.endObject();
                yield Map.copyOf(map);
            }
            case STRING -> reader.nextString();
            case NUMBER -> ToNumberPolicy.LONG_OR_DOUBLE.readNumber(reader);
            case BOOLEAN -> reader.nextBoolean();
            default -> throw new IllegalStateException("Invalid peek: " + reader.peek());
        };
    }

    record PropertiesMap(Map<String, Object> map) implements Properties {
        PropertiesMap {
            map = Map.copyOf(map);
        }

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
        public boolean getBoolean(String name) {
            return element(name);
        }

        @Override
        public <T> List<T> getList(String name, List<T> defaultValue) {
            List<T> element = element(name);
            return element != null ? element : defaultValue;
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

        <T> List<T> getList(String name, List<T> defaultValue);

        default <T> List<T> getList(String name) {
            return getList(name, List.of());
        }

        @Deprecated(forRemoval = true)
        default List<List<Double>> getNestedDoubleArray(String name) {
            return getList(name);
        }

        Properties section(String name);

        boolean containsKey(String name);

        Map<String, Object> asMap();

        @Override
        default Iterator<Map.Entry<String, Object>> iterator() {
            return asMap().entrySet().iterator();
        }

        default int size() {
            return asMap().size();
        }
    }
}
