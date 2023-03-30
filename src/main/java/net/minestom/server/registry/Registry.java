package net.minestom.server.registry;

import com.google.gson.ToNumberPolicy;
import com.google.gson.stream.JsonReader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.Shape;
import net.minestom.server.entity.EntitySpawnType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
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
import java.util.function.Supplier;

/**
 * Handles registry data, used by {@link ProtocolObject} implementations and is strictly internal.
 * Use at your own risk.
 */
public final class Registry {
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
    public static EnchantmentEntry enchantment(String namespace, @NotNull Properties main) {
        return new EnchantmentEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static PotionEffectEntry potionEffect(String namespace, @NotNull Properties main) {
        return new PotionEffectEntry(namespace, main, null);
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
    public static <T extends ProtocolObject> Container<T> createContainer(Resource resource, Container.Loader<T> loader) {
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
    public record Container<T extends ProtocolObject>(Resource resource,
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
    public enum Resource {
        BLOCKS("blocks.json"),
        ITEMS("items.json"),
        ENTITIES("entities.json"),
        ENCHANTMENTS("enchantments.json"),
        SOUNDS("sounds.json"),
        COMMAND_ARGUMENTS("command_arguments.json"),
        STATISTICS("custom_statistics.json"),
        POTION_EFFECTS("potion_effects.json"),
        POTION_TYPES("potions.json"),
        PARTICLES("particles.json"),

        BLOCK_TAGS("tags/block_tags.json"),
        ENTITY_TYPE_TAGS("tags/entity_type_tags.json"),
        FLUID_TAGS("tags/fluid_tags.json"),
        GAMEPLAY_TAGS("tags/gameplay_tags.json"),
        ITEM_TAGS("tags/item_tags.json");

        private final String name;

        Resource(String name) {
            this.name = name;
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
        private final String blockEntity;
        private final int blockEntityId;
        private final Supplier<Material> materialSupplier;
        private final Shape shape;
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
                final String string = main.getString("collisionShape");
                this.shape = CollisionUtils.parseBlockShape(string, this);
            }
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

        public Shape collisionShape() {
            return shape;
        }

        @Override
        public Properties custom() {
            return custom;
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
        private final Properties custom;

        private MaterialEntry(String namespace, Properties main, Properties custom) {
            this.custom = custom;
            this.namespace = NamespaceID.from(namespace);
            this.id = main.getInt("id");
            this.translationKey = main.getString("translationKey");
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
                    main.getString("translationKey"),
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
                    main.getString("translationKey"),
                    main.getDouble("maxLevel"),
                    main.getBoolean("isCursed", false),
                    main.getBoolean("isDiscoverable", true),
                    main.getBoolean("isTradeable", true),
                    main.getBoolean("isTreasureOnly", false),
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
                    main.getString("translationKey"),
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
        public Map<String, Object> asMap() {
            return map;
        }

        private <T> T element(String name) {
            //noinspection unchecked
            return (T) map.get(name);
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
