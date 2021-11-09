package net.minestom.server.registry;

import com.google.gson.ToNumberPolicy;
import com.google.gson.stream.JsonReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntitySpawnType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.ObjectArray;
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
    public static BlockEntry block(String namespace, @NotNull Map<String, Object> jsonObject, Map<String, Object> override) {
        return new BlockEntry(namespace, jsonObject, override);
    }

    @ApiStatus.Internal
    public static MaterialEntry material(String namespace, @NotNull Map<String, Object> jsonObject, Map<String, Object> override) {
        return new MaterialEntry(namespace, jsonObject, override);
    }

    @ApiStatus.Internal
    public static EntityEntry entity(String namespace, @NotNull Map<String, Object> jsonObject, Map<String, Object> override) {
        return new EntityEntry(namespace, jsonObject, override);
    }

    @ApiStatus.Internal
    public static EnchantmentEntry enchantment(String namespace, @NotNull Map<String, Object> jsonObject, Map<String, Object> override) {
        return new EnchantmentEntry(namespace, jsonObject, override);
    }

    @ApiStatus.Internal
    public static PotionEffectEntry potionEffect(String namespace, @NotNull Map<String, Object> jsonObject, Map<String, Object> override) {
        return new PotionEffectEntry(namespace, jsonObject, override);
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

    public static class Container<T extends ProtocolObject> {
        // Maps do not need to be thread-safe as they are fully populated
        // in the static initializer, should not be modified during runtime

        // namespace -> registry data
        private final Map<String, T> namespaceMap = new HashMap<>();
        // id -> registry data
        private final ObjectArray<T> ids = new ObjectArray<>();
        private final Collection<T> objects = Collections.unmodifiableCollection(namespaceMap.values());

        private final boolean initialized;

        @ApiStatus.Internal
        public Container(Resource resource, Loader<T> loader) {
            for (var entry : Registry.load(resource).entrySet()) {
                final String namespace = entry.getKey();
                final Map<String, Object> object = entry.getValue();
                loader.accept(this, namespace, object);
            }
            this.initialized = true;
            this.ids.trim();
        }

        public T get(@NotNull String namespace) {
            return namespaceMap.get(namespace);
        }

        public T getSafe(@NotNull String namespace) {
            return get(namespace.contains(":") ? namespace : "minecraft:" + namespace);
        }

        public T getId(int id) {
            return ids.get(id);
        }

        public Collection<T> values() {
            return objects;
        }

        public void register(@NotNull T value) {
            Check.stateCondition(initialized, "Registering is only available within the loader lambda.");
            this.ids.set(value.id(), value);
            this.namespaceMap.put(value.name(), value);
        }

        public interface Loader<T extends ProtocolObject> {
            void accept(Container<T> container, String namespace, Map<String, Object> object);
        }
    }

    @ApiStatus.Internal
    public enum Resource {
        BLOCKS("blocks.json"),
        ITEMS("items.json"),
        ENTITIES("entities.json"),
        ENCHANTMENTS("enchantments.json"),
        SOUNDS("sounds.json"),
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

    public static class BlockEntry extends Entry {
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
        private final Supplier<Material> materialSupplier;

        private BlockEntry(String namespace, Map<String, Object> main, Map<String, Object> override) {
            super(main, override);
            this.namespace = NamespaceID.from(namespace);
            this.id = getInt("id");
            this.stateId = getInt("stateId");
            this.translationKey = getString("translationKey");
            this.hardness = getDouble("hardness");
            this.explosionResistance = getDouble("explosionResistance");
            this.friction = getDouble("friction");
            this.speedFactor = getDouble("speedFactor", 1);
            this.jumpFactor = getDouble("jumpFactor", 1);
            this.air = getBoolean("air", false);
            this.solid = getBoolean("solid");
            this.liquid = getBoolean("liquid", false);
            this.blockEntity = getString("blockEntity", null);
            {
                final String materialNamespace = getString("correspondingItem", null);
                this.materialSupplier = materialNamespace != null ? () -> Material.fromNamespaceId(materialNamespace) : () -> null;
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

        public @Nullable Material material() {
            return materialSupplier.get();
        }
    }

    public static class MaterialEntry extends Entry {
        private final NamespaceID namespace;
        private final int id;
        private final String translationKey;
        private final int maxStackSize;
        private final int maxDamage;
        private final boolean isFood;
        private final Supplier<Block> blockSupplier;
        private final EquipmentSlot equipmentSlot;

        private MaterialEntry(String namespace, Map<String, Object> main, Map<String, Object> override) {
            super(main, override);
            this.namespace = NamespaceID.from(namespace);
            this.id = getInt("id");
            this.translationKey = getString("translationKey");
            this.maxStackSize = getInt("maxStackSize", 64);
            this.maxDamage = getInt("maxDamage", 0);
            this.isFood = getBoolean("edible", false);
            {
                final String blockNamespace = getString("correspondingBlock", null);
                this.blockSupplier = blockNamespace != null ? () -> Block.fromNamespaceId(blockNamespace) : () -> null;
            }

            {
                final Map<String, Object> armorProperties = element("armorProperties");
                if (armorProperties != null) {
                    final String slot = (String) armorProperties.get("slot");
                    switch (slot) {
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
    }

    public static class EntityEntry extends Entry {
        private final NamespaceID namespace;
        private final int id;
        private final String translationKey;
        private final double width;
        private final double height;
        private final EntitySpawnType spawnType;

        private EntityEntry(String namespace, Map<String, Object> main, Map<String, Object> override) {
            super(main, override);
            this.namespace = NamespaceID.from(namespace);
            this.id = getInt("id");
            this.translationKey = getString("translationKey");
            this.width = getDouble("width");
            this.height = getDouble("height");
            this.spawnType = EntitySpawnType.valueOf(getString("packetType").toUpperCase(Locale.ROOT));
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

        public double width() {
            return width;
        }

        public double height() {
            return height;
        }

        public EntitySpawnType spawnType() {
            return spawnType;
        }
    }

    public static class EnchantmentEntry extends Entry {
        private final NamespaceID namespace;
        private final int id;
        private final String translationKey;
        private final double maxLevel;
        private final boolean isCursed;
        private final boolean isDiscoverable;
        private final boolean isTradeable;
        private final boolean isTreasureOnly;

        private EnchantmentEntry(String namespace, Map<String, Object> main, Map<String, Object> override) {
            super(main, override);
            this.namespace = NamespaceID.from(namespace);
            this.id = getInt("id");
            this.translationKey = getString("translationKey");
            this.maxLevel = getDouble("maxLevel");
            this.isCursed = getBoolean("curse", false);
            this.isDiscoverable = getBoolean("discoverable", true);
            this.isTradeable = getBoolean("tradeable", true);
            this.isTreasureOnly = getBoolean("treasureOnly", false);
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

        public double maxLevel() {
            return maxLevel;
        }

        public boolean isCursed() {
            return isCursed;
        }

        public boolean isDiscoverable() {
            return isDiscoverable;
        }

        public boolean isTradeable() {
            return isTradeable;
        }

        public boolean isTreasureOnly() {
            return isTreasureOnly;
        }
    }

    public static class PotionEffectEntry extends Entry {
        private final NamespaceID namespace;
        private final int id;
        private final String translationKey;
        private final int color;
        private final boolean isInstantaneous;

        private PotionEffectEntry(String namespace, Map<String, Object> main, Map<String, Object> override) {
            super(main, override);
            this.namespace = NamespaceID.from(namespace);
            this.id = getInt("id");
            this.translationKey = getString("translationKey");
            this.color = getInt("color");
            this.isInstantaneous = getBoolean("instantaneous");
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

        public int color() {
            return color;
        }

        public boolean isInstantaneous() {
            return isInstantaneous;
        }
    }

    public static class Entry {
        private final Map<String, Object> main, override;

        private Entry(Map<String, Object> main, Map<String, Object> override) {
            this.main = main;
            this.override = override;
        }

        public String getString(String name, String defaultValue) {
            var element = element(name);
            return element != null ? (String) element : defaultValue;
        }

        public String getString(String name) {
            return element(name);
        }

        public double getDouble(String name, double defaultValue) {
            var element = element(name);
            return element != null ? ((Number) element).doubleValue() : defaultValue;
        }

        public double getDouble(String name) {
            return ((Number) element(name)).doubleValue();
        }

        public int getInt(String name, int defaultValue) {
            var element = element(name);
            return element != null ? ((Number) element).intValue() : defaultValue;
        }

        public int getInt(String name) {
            return ((Number) element(name)).intValue();
        }

        public boolean getBoolean(String name, boolean defaultValue) {
            var element = element(name);
            return element != null ? (boolean) element : defaultValue;
        }

        public boolean getBoolean(String name) {
            return element(name);
        }

        protected <T> T element(String name) {
            Object result;
            if (override != null && (result = override.get(name)) != null) {
                return (T) result;
            }
            return (T) main.get(name);
        }
    }

    private static Object readObject(JsonReader reader) throws IOException {
        return switch (reader.peek()) {
            case BEGIN_ARRAY -> {
                ObjectArrayList<Object> list = new ObjectArrayList<>();
                reader.beginArray();
                while (reader.hasNext()) list.add(readObject(reader));
                reader.endArray();
                yield new ObjectArrayList<>(list);
            }
            case BEGIN_OBJECT -> {
                Object2ObjectArrayMap<String, Object> map = new Object2ObjectArrayMap<>();
                reader.beginObject();
                while (reader.hasNext()) map.put(reader.nextName().intern(), readObject(reader));
                reader.endObject();
                yield new Object2ObjectArrayMap<>(map);
            }
            case STRING -> reader.nextString().intern();
            case NUMBER -> ToNumberPolicy.LONG_OR_DOUBLE.readNumber(reader);
            case BOOLEAN -> reader.nextBoolean();
            default -> throw new IllegalStateException("Invalid peek: " + reader.peek());
        };
    }
}
