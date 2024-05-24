package net.minestom.server.registry;

import com.google.gson.ToNumberPolicy;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.Shape;
import net.minestom.server.entity.EntitySpawnType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.collection.ObjectArray;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Handles registry data, used by {@link StaticProtocolObject} implementations and is strictly internal.
 * Use at your own risk.
 */
public final class Registry {

    private static final String TRANSLATION_KEY = "translationKey";

    @ApiStatus.Internal
    public static BlockEntry block(String namespace, @NotNull Properties main) {
        return new BlockEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static BiomeEntry biome(String namespace, Properties properties) {
        return new BiomeEntry(namespace, properties, null);
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
    public static EnchantmentEntry enchantment(String namespace, @NotNull Properties main) {
        return new EnchantmentEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static PotionEffectEntry potionEffect(String namespace, @NotNull Properties main) {
        return new PotionEffectEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static DamageTypeEntry damageType(String namespace, @NotNull Properties main) {
        return new DamageTypeEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static TrimMaterialEntry trimMaterial(String namespace, @NotNull Properties main) {
        return new TrimMaterialEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static TrimPatternEntry trimPattern(String namespace, @NotNull Properties main) {
        return new TrimPatternEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static AttributeEntry attribute(String namespace, @NotNull Properties main) {
        return new AttributeEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static VillagerProfession villagerProfession(String namespace, @NotNull Properties main) {
        return new VillagerProfession(namespace, main, null);
    }

    @ApiStatus.Internal
    public static VillagerType villagerType(String namespace, @NotNull Properties main) {
        return new VillagerType(namespace, main, null);
    }

    @ApiStatus.Internal
    public static Map<String, Map<String, Object>> load(Resource resource) {
        Map<String, Map<String, Object>> map = new HashMap<>();
        try (InputStream resourceStream = Registry.class.getClassLoader().getResourceAsStream(resource.name)) {
            Check.notNull(resourceStream, "Resource {0} does not exist!", resource);
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
    public static <T extends StaticProtocolObject> Container<T> createStaticContainer(Resource resource, Loader<T> loader) {
        var entries = Registry.load(resource);
        Map<String, T> namespaces = new HashMap<>(entries.size());
        ObjectArray<T> ids = ObjectArray.singleThread(entries.size());
        for (var entry : entries.entrySet()) {
            final String namespace = entry.getKey();
            final Properties properties = Properties.fromMap(entry.getValue());
            final T value = loader.get(namespace, properties);
            ids.set(value.id(), value);
            namespaces.put(value.name(), value);
        }
        return new Container<>(resource, namespaces, ids);
    }

    @ApiStatus.Internal
    public record Container<T extends StaticProtocolObject>(Resource resource,
                                                            Map<String, T> namespaces,
                                                            ObjectArray<T> ids) {
        public Container {
            namespaces = Map.copyOf(namespaces);
            ids.trim();
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
    public static <T extends ProtocolObject> DynamicContainer<T> createDynamicContainer(Resource resource, Loader<T> loader) {
        var entries = Registry.load(resource);
        Map<String, T> namespaces = new HashMap<>(entries.size());
        for (var entry : entries.entrySet()) {
            final String namespace = entry.getKey();
            final Properties properties = Properties.fromMap(entry.getValue());
            final T value = loader.get(namespace, properties);
            namespaces.put(value.name(), value);
        }
        return new DynamicContainer<>(resource, namespaces);
    }

    @ApiStatus.Internal
    public record DynamicContainer<T>(Resource resource, Map<String, T> namespaces) {
        public DynamicContainer {
            namespaces = Map.copyOf(namespaces);
        }

        public T get(@NotNull String namespace) {
            return namespaces.get(namespace);
        }

        public T getSafe(@NotNull String namespace) {
            return get(namespace.contains(":") ? namespace : "minecraft:" + namespace);
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

    @FunctionalInterface
    public interface Loader<T extends ProtocolObject> {
        T get(String namespace, Properties properties);
    }

    @ApiStatus.Internal
    public enum Resource {
        BLOCKS("blocks.json"),
        ITEMS("items.json"),
        ATTRIBUTES("attributes.json"),
        ENTITIES("entities.json"),
        ENCHANTMENTS("enchantments.json"),
        SOUNDS("sounds.json"),
        COMMAND_ARGUMENTS("command_arguments.json"),
        STATISTICS("custom_statistics.json"),
        POTION_EFFECTS("potion_effects.json"),
        POTION_TYPES("potions.json"),
        PARTICLES("particles.json"),
        DAMAGE_TYPES("damage_types.json"),
        TRIM_MATERIALS("trim_materials.json"),
        TRIM_PATTERNS("trim_patterns.json"),
        BANNER_PATTERNS("banner_patterns.json"), // Microtus -  Banner and Shield Meta
        BLOCK_TAGS("tags/block_tags.json"),
        ENTITY_TYPE_TAGS("tags/entity_type_tags.json"),
        FLUID_TAGS("tags/fluid_tags.json"),
        GAMEPLAY_TAGS("tags/gameplay_tags.json"),
        ITEM_TAGS("tags/item_tags.json"),
        BIOMES("biomes.json"),
        VILLAGER_PROFESSION("villager_professions.json"),
        VILLAGER_TYPES("villager_types.json"),
        ;

        private final String name;

        Resource(String name) {
            this.name = name;
        }
    }

    public record AttributeEntry(
            @NotNull NamespaceID namespace,
            int id,
            @NotNull String translationKey,
            float defaultValue,
            boolean clientSync,
            float maxValue,
            float minValue,
            @Nullable Properties custom
    ) implements Entry {

        public AttributeEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    main.getInt("id"),
                    main.getString(TRANSLATION_KEY),
                    (float) main.getDouble("defaultValue"),
                    main.getBoolean("clientSync"),
                    (float) main.getDouble("maxValue"),
                    (float) main.getDouble("minValue"),
                    custom
                    );
        }
    }

    public static final class BlockEntry implements Entry {
        private final NamespaceID namespace;
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
        private final int lightEmission;
        private final boolean replaceable;
        private final String blockEntity;
        private final int blockEntityId;
        private final Supplier<Material> materialSupplier;
        private final Shape shape;
        private final boolean redstoneConductor;
        private final boolean signalSource;
        private final Properties custom;

        private BlockEntry(String namespace, Properties main, Properties custom) {
            this.custom = custom;
            this.namespace = NamespaceID.from(namespace);
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
            this.lightEmission = main.getInt("lightEmission", 0);
            this.replaceable = main.getBoolean("replaceable", false);
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
                this.materialSupplier = materialNamespace != null ? () -> Material.fromNamespaceId(materialNamespace) : () -> null;
            }
            {
                final String collision = main.getString("collisionShape");
                final String occlusion = main.getString("occlusionShape");
                this.shape = CollisionUtils.parseBlockShape(collision, occlusion, this);
            }
            this.redstoneConductor = main.getBoolean("redstoneConductor");
            this.signalSource = main.getBoolean("signalSource", false);
        }

        public @NotNull NamespaceID namespace() {
            return namespace;
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

        @Override
        public Properties custom() {
            return custom;
        }
    }

    public static final class BiomeEntry implements Entry {
        private final Properties custom;
        private final NamespaceID namespace;
        private final Integer foliageColor;
        private final Integer grassColor;
        private final Integer skyColor;
        private final Integer waterColor;
        private final Integer waterFogColor;
        private final Integer fogColor;
        private final float temperature;
        private final float downfall;
        private final boolean hasPrecipitation;

        private BiomeEntry(String namespace, Properties main, Properties custom) {
            this.custom = custom;
            this.namespace = NamespaceID.from(namespace);

            this.foliageColor = main.containsKey("foliageColor") ? main.getInt("foliageColor") : null;
            this.grassColor = main.containsKey("grassColor") ? main.getInt("grassColor") : null;
            this.skyColor = main.containsKey("skyColor") ? main.getInt("skyColor") : null;
            this.waterColor = main.containsKey("waterColor") ? main.getInt("waterColor") : null;
            this.waterFogColor = main.containsKey("waterFogColor") ? main.getInt("waterFogColor") : null;
            this.fogColor = main.containsKey("fogColor") ? main.getInt("fogColor") : null;

            this.temperature = (float) main.getDouble("temperature", 0.5F);
            this.downfall = (float) main.getDouble("downfall", 0.5F);
            this.hasPrecipitation = main.getBoolean("has_precipitation", true);
        }

        @Override
        public Properties custom() {
            return custom;
        }

        public @NotNull NamespaceID namespace() {
            return namespace;
        }

        public @Nullable Integer foliageColor() {
            return foliageColor;
        }

        public @Nullable Integer grassColor() {
            return grassColor;
        }

        public @Nullable Integer skyColor() {
            return skyColor;
        }

        public @Nullable Integer waterColor() {
            return waterColor;
        }

        public @Nullable Integer waterFogColor() {
            return waterFogColor;
        }

        public @Nullable Integer fogColor() {
            return fogColor;
        }

        public float temperature() {
            return temperature;
        }

        public float downfall() {
            return downfall;
        }

        public boolean hasPrecipitation() {
            return hasPrecipitation;
        }
    }

    public static final class MaterialEntry implements Entry {
        private final NamespaceID namespace;
        private final int id;
        private final String translationKey;
        private final int maxStackSize;
        private final int maxDamage;
        private final boolean isFood;
        private final Supplier<Block> blockSupplier;
        private final EquipmentSlot equipmentSlot;
        private final EntityType entityType;
        private final Properties custom;

        private MaterialEntry(String namespace, Properties main, Properties custom) {
            this.custom = custom;
            this.namespace = NamespaceID.from(namespace);
            this.id = main.getInt("id");
            this.translationKey = main.getString(TRANSLATION_KEY);
            this.maxStackSize = main.getInt("maxStackSize", 64);
            this.maxDamage = main.getInt("maxDamage", 0);
            this.isFood = main.getBoolean("edible", false);
            {
                final String blockNamespace = main.getString("correspondingBlock", null);
                this.blockSupplier = blockNamespace != null ? () -> Block.fromNamespaceId(blockNamespace) : () -> null;
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
                    this.entityType = EntityType.fromNamespaceId(spawnEggProperties.getString("entityType"));
                } else {
                    this.entityType = null;
                }
            }
        }

        public @NotNull NamespaceID namespace() {
            return namespace;
        }

        public int id() {
            return id;
        }

        public String translationKey() {
            return translationKey;
        }

        public int maxStackSize() {
            return maxStackSize;
        }

        public int maxDamage() {
            return maxDamage;
        }

        public boolean isFood() {
            return isFood;
        }

        public @Nullable Block block() {
            return blockSupplier.get();
        }

        public boolean isArmor() {
            return equipmentSlot != null;
        }

        public @Nullable EquipmentSlot equipmentSlot() {
            return equipmentSlot;
        }

        /**
         * Gets the entity type this item can spawn. Only present for spawn eggs (e.g. wolf spawn egg, skeleton spawn egg)
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

    public record EntityEntry(NamespaceID namespace, int id,
                              String translationKey,
                              double width, double height,
                              double drag, double acceleration,
                              EntitySpawnType spawnType,
                              BoundingBox boundingBox,
                              Properties custom) implements Entry {
        public EntityEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    main.getInt("id"),
                    main.getString(TRANSLATION_KEY),
                    main.getDouble("width"),
                    main.getDouble("height"),
                    main.getDouble("drag", 0.02),
                    main.getDouble("acceleration", 0.08),
                    EntitySpawnType.valueOf(main.getString("packetType").toUpperCase(Locale.ROOT)),
                    new BoundingBox(
                            main.getDouble("width"),
                            main.getDouble("height"),
                            main.getDouble("width")),
                    custom
            );
        }
    }

    public record DamageTypeEntry(NamespaceID namespace, float exhaustion,
                                  String messageId,
                                  String scaling,
                                  @Nullable String effects,
                                  @Nullable String deathMessageType,
                                  Properties custom) implements Entry {
        public DamageTypeEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    (float) main.getDouble("exhaustion"),
                    main.getString("message_id"),
                    main.getString("scaling"),
                    main.getString("effects"),
                    main.getString("death_message_type"),
                    custom);
        }
    }
    public record TrimMaterialEntry(@NotNull NamespaceID namespace,
                                    @NotNull String assetName,
                                    @NotNull Material ingredient,
                                    float itemModelIndex,
                                    @NotNull Map<String,String> overrideArmorMaterials,
                                    @NotNull Component description,
                                    Properties custom) implements Entry {
        public TrimMaterialEntry(@NotNull String namespace, @NotNull Properties main, Properties custom) {
            this(
                    NamespaceID.from(namespace),
                    main.getString("asset_name"),
                    Objects.requireNonNull(Material.fromNamespaceId(main.getString("ingredient"))),
                    (float) main.getDouble("item_model_index"),
                    Objects.requireNonNullElse(main.section("override_armor_materials"),new PropertiesMap(Map.of()))
                            .asMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue())),
                    JSONComponentSerializer.json().deserialize(main.section("description").toString()),
                    custom
            );
        }
    }

    public record TrimPatternEntry(@NotNull NamespaceID namespace,
                                   @NotNull NamespaceID assetID,
                                   @NotNull Material template,
                                   @NotNull Component description,
                                   boolean decal,
                                   Properties custom) implements Entry {
        public TrimPatternEntry(@NotNull String namespace, @NotNull Properties main, Properties custom) {
            this(
                    NamespaceID.from(namespace),
                    NamespaceID.from(main.getString("asset_id")),
                    Objects.requireNonNull(Material.fromNamespaceId(main.getString("template_item"))),
                    JSONComponentSerializer.json().deserialize(main.section("description").toString()),
                    main.getBoolean("decal"),
                    custom
            );
        }
    }

    public record VillagerProfession(NamespaceID namespace, int id, SoundEvent soundEvent, Properties custom) implements Entry {
        public VillagerProfession(String namespace,
                                  Properties main,
                                  Properties custom) {
            this(NamespaceID.from(namespace),
                    main.getInt("id"),
                    SoundEvent.fromNamespaceId(main.getString("workSound")),
                    custom);
        }
    }

    public record VillagerType(NamespaceID namespace, int id, Properties custom) implements Entry {
        public VillagerType(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    main.getInt("id"),
                    custom);
        }
    }

    public record EnchantmentEntry(NamespaceID namespace, int id,
                                   String translationKey,
                                   double maxLevel,
                                   boolean isCursed,
                                   boolean isDiscoverable,
                                   boolean isTradeable,
                                   boolean isTreasureOnly,
                                   Properties custom) implements Entry {
        public EnchantmentEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    main.getInt("id"),
                    main.getString(TRANSLATION_KEY),
                    main.getDouble("maxLevel"),
                    main.getBoolean("curse", false),
                    main.getBoolean("discoverable", true),
                    main.getBoolean("tradeable", true),
                    main.getBoolean("treasureOnly", false),
                    custom);
        }
    }

    public record PotionEffectEntry(NamespaceID namespace, int id,
                                    String translationKey,
                                    int color,
                                    boolean isInstantaneous,
                                    Properties custom) implements Entry {
        public PotionEffectEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    main.getInt("id"),
                    main.getString(TRANSLATION_KEY),
                    main.getInt("color"),
                    main.getBoolean("instantaneous"),
                    custom);
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
        public boolean getBoolean(String name, boolean defaultValue) {
            var element = element(name);
            return element != null ? (boolean) element : defaultValue;
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
            return string.updateAndGet(s -> s.replaceFirst(" , ","") + "}");
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

        boolean getBoolean(String name, boolean defaultValue);

        boolean getBoolean(String name);

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
