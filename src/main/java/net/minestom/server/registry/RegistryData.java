package net.minestom.server.registry;

import com.google.gson.ToNumberPolicy;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
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
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

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
public final class RegistryData {

    @ApiStatus.Internal
    public static MaterialEntry material(Keyed namespace, Properties main) {
        return new MaterialEntry(namespace.key(), main);
    }

    @ApiStatus.Internal
    public static EntityEntry entity(Keyed namespace, Properties main) {
        return new EntityEntry(namespace.key(), main);
    }

    @ApiStatus.Internal
    public static VillagerProfessionEntry villagerProfession(Keyed namespace, Properties main) {
        return new VillagerProfessionEntry(namespace.key(), main);
    }

    @ApiStatus.Internal
    public static FeatureFlagEntry featureFlag(Keyed namespace, Properties main) {
        return new FeatureFlagEntry(namespace.key(), main);
    }

    @ApiStatus.Internal
    public static FluidEntry fluid(Keyed namespace, Properties main) {
        return new FluidEntry(namespace.key(), main);
    }

    @ApiStatus.Internal
    public static PotionEffectEntry potionEffect(Keyed namespace, Properties main) {
        return new PotionEffectEntry(namespace.key(), main);
    }

    @ApiStatus.Internal
    public static AttributeEntry attribute(Keyed namespace, Properties main) {
        return new AttributeEntry(namespace.key(), main);
    }

    @ApiStatus.Internal
    public static GameEventEntry gameEventEntry(Keyed namespace, Properties properties) {
        return new GameEventEntry(namespace.key(), properties);
    }

    @ApiStatus.Internal
    public static BlockSoundTypeEntry blockSoundTypeEntry(Keyed namespace, Properties properties) {
        return new BlockSoundTypeEntry(namespace.key(), properties);
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
     *
     * @throws IllegalStateException when MinecraftServer isn't initializing.
     */
    @ApiStatus.Internal
    public static <T extends StaticProtocolObject<T>> Registry<T> createStaticRegistry(RegistryKey<Registry<T>> registryKey, Loader<T> loader) {
        Check.stateCondition(!MinecraftServer.isInitializing(),
                "Static registry `{0}` cannot be created before its unsealed. Did you forget to do MinecraftServer#init before using?", registryKey.name());
        // Create the registry (data)
        var entries = RegistryData.load(String.format("%s.json", registryKey.key().value()), true);
        Map<Key, T> namespaces = new HashMap<>(entries.size());
        ObjectArray<T> ids = ObjectArray.singleThread(entries.size());
        final DetourRegistry detourRegistry = DetourRegistry.detourRegistry();
        for (var entry : entries.asMap().keySet()) {
            final RegistryKey<T> key = RegistryKey.unsafeOf(entry);
            final Properties properties = entries.section(entry);
            T value = loader.get(key, properties);
            value = detourRegistry.consume(key, value);
            ids.set(value.id(), value);
            namespaces.put(value.key(), value);
        }
        // Load tags if they exist
        Map<TagKey<T>, RegistryTag<T>> tags = loadTags(detourRegistry, registryKey.key());
        final Registry<T> staticRegistry = new StaticRegistry<>(registryKey, namespaces, ids, tags);
        return detourRegistry.consume(registryKey, staticRegistry);
    }

    @ApiStatus.Internal
    static <T> @Unmodifiable Map<TagKey<T>, RegistryTag<T>> loadTags(DetourRegistry detourRegistry, Key registryKey) {
        final var tagJson = RegistryData.load(String.format("tags/%s.json", registryKey.value()), false);
        final Map<TagKey<T>, RegistryTag<T>> tags = new HashMap<>(tagJson.size());
        for (String tagName : tagJson.asMap().keySet()) {
            final TagKey<T> tagKey = new TagKeyImpl<>(Key.key(tagName)); // Value excludes hash.
            loadTag(detourRegistry, tags, tagKey, tagJson); // loadTag will add the tag to the map if it doesn't exist
        }
        return Map.copyOf(tags);
    }

    private static <T> RegistryTag<T> loadTag(DetourRegistry detourRegistry, Map<TagKey<T>, RegistryTag<T>> currentTags, TagKey<T> tagKey, Properties main) {
        final RegistryTag<T> registryTag = currentTags.get(tagKey);
        if (registryTag != null) return registryTag;
        // If the tag doesnt exist, we create it
        Properties section = main.section(tagKey.key().asString());
        final RegistryTag<T> computedTag = RegistryTag.builder(tagKey, builder -> {
            for (var tagString: section.<String>getList("values")) {
                if (tagString.startsWith("#")) {
                    for (var key : loadTag(detourRegistry, currentTags, TagKey.ofHash(tagString), main)) {
                        builder.add(key);
                    }
                } else {
                    builder.add(RegistryKey.unsafeOf(tagString));
                }
            }
            detourRegistry.consume(tagKey, builder);
        });
        currentTags.put(tagKey, computedTag);
        return computedTag;
    }

    @FunctionalInterface
    public interface Loader<T extends StaticProtocolObject<T>> {
        T get(RegistryKey<T> key, Properties properties);
    }

    public record GameEventEntry(Key key, int id, int notificationRadius) implements Entry {
        public GameEventEntry(Key key, Properties main) {
            this(key, main.getInt("id"), main.getInt("notificationRadius"));
        }
    }

    public static final class MaterialEntry implements Entry {
        private final Key key;
        private final int id;
        private final String translationKey;
        private final Supplier<Block> blockSupplier;
        private @Nullable Either<Properties, DataComponentMap> prototype;

        private final @Nullable EntityType entityType;

        private MaterialEntry(Key namespace, Properties main) {
            this.prototype = Either.left(main.section("components"));
            this.key = namespace;
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

        public EntityEntry(Key namespace, Properties main) {
            this.key = namespace;
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
        private final @Nullable SoundEvent workSound;

        public VillagerProfessionEntry(Key namespace, Properties main) {
            this.key = namespace;
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
        public FeatureFlagEntry(Key namespace, Properties main) {
            this(namespace, main.getInt("id"));
        }
    }

    public record FluidEntry(Key key, int id) implements Entry {
        public FluidEntry(Key namespace, Properties main) {
            this(namespace, main.getInt("id"));
        }
    }

    public record PotionEffectEntry(Key key, int id,
                                    String translationKey,
                                    int color,
                                    boolean isInstantaneous) implements Entry {
        public PotionEffectEntry(Key namespace, Properties main) {
            this(namespace,
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
        public AttributeEntry(Key namespace, Properties main) {
            this(namespace,
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
        public BlockSoundTypeEntry(Key namespace, Properties main) {
            this(namespace, main.getFloat("volume"),
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
        public byte getByte(String name) {
            return element(name);
        }

        @Override
        public byte getByte(String name, byte defaultValue) {
            var element = element(name);
            return element != null ? ((Number) element).byteValue() : defaultValue;
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

        byte getByte(String name);

        byte getByte(String name, byte defaultValue);

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

        @Nullable Properties section(String name);

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
