package net.minestom.server.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.entity.EntitySpawnType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Supplier;

@ApiStatus.Internal
public class Registry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static BlockEntry block(String namespace, @NotNull JsonObject jsonObject, JsonObject override) {
        return new BlockEntry(namespace, jsonObject, override);
    }

    public static MaterialEntry material(String namespace, @NotNull JsonObject jsonObject, JsonObject override) {
        return new MaterialEntry(namespace, jsonObject, override);
    }

    public static EntityEntry entity(String namespace, @NotNull JsonObject jsonObject, JsonObject override) {
        return new EntityEntry(namespace, jsonObject, override);
    }

    public static EnchantmentEntry enchantment(String namespace, @NotNull JsonObject jsonObject, JsonObject override) {
        return new EnchantmentEntry(namespace, jsonObject, override);
    }

    public static PotionEffectEntry potionEffect(String namespace, @NotNull JsonObject jsonObject, JsonObject override) {
        return new PotionEffectEntry(namespace, jsonObject, override);
    }

    public static JsonObject load(Resource resource) {
        final String path = String.format("/%s.json", resource.name);
        final var resourceStream = Registry.class.getResourceAsStream(path);
        return GSON.fromJson(new InputStreamReader(resourceStream), JsonObject.class);
    }

    public static class Loader<T extends ProtocolObject> {
        // Maps do not need to be thread-safe as they are fully populated
        // in the static initializer, should not be modified during runtime

        // namespace -> registry data
        private final Map<String, T> namespaceMap = new HashMap<>();
        // id -> registry data
        private final Int2ObjectMap<T> idMap = new Int2ObjectOpenHashMap<>();

        public T get(@NotNull String namespace) {
            return namespaceMap.get(namespace);
        }

        public T getSafe(@NotNull String namespace) {
            return get(namespace.contains(":") ? namespace : "minecraft:" + namespace);
        }

        public T getId(int id) {
            return idMap.get(id);
        }

        public Collection<T> values() {
            return Collections.unmodifiableCollection(namespaceMap.values());
        }

        public void register(@NotNull T value) {
            idMap.put(value.id(), value);
            namespaceMap.put(value.name(), value);
        }
    }

    public enum Resource {
        BLOCKS("blocks"),
        ITEMS("items"),
        ENTITIES("entities"),
        ENCHANTMENTS("enchantments"),
        SOUNDS("sounds"),
        STATISTICS("custom_statistics"),
        POTION_EFFECTS("potion_effects"),
        POTION_TYPES("potions"),
        PARTICLES("particles"),

        BLOCK_TAGS("tags/block_tags"),
        ENTITY_TYPE_TAGS("tags/entity_type_tags"),
        FLUID_TAGS("tags/fluid_tags"),
        GAMEPLAY_TAGS("tags/gameplay_tags"),
        ITEM_TAGS("tags/item_tags");

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

        private BlockEntry(String namespace, JsonObject main, JsonObject override) {
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

        private MaterialEntry(String namespace, JsonObject main, JsonObject override) {
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
                final var armorProperties = element("armorProperties");
                if (armorProperties != null) {
                    final String slot = armorProperties.getAsJsonObject().get("slot").getAsString();
                    switch (slot) {
                        case "feet":
                            this.equipmentSlot = EquipmentSlot.BOOTS;
                            break;
                        case "legs":
                            this.equipmentSlot = EquipmentSlot.LEGGINGS;
                            break;
                        case "chest":
                            this.equipmentSlot = EquipmentSlot.CHESTPLATE;
                            break;
                        case "head":
                            this.equipmentSlot = EquipmentSlot.HELMET;
                            break;
                        default:
                            this.equipmentSlot = null;
                            break;
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

        private EntityEntry(String namespace, JsonObject main, JsonObject override) {
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

        private EnchantmentEntry(String namespace, JsonObject main, JsonObject override) {
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

        private PotionEffectEntry(String namespace, JsonObject main, JsonObject override) {
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
        private final JsonObject main, override;

        private Entry(JsonObject main, JsonObject override) {
            this.main = main;
            this.override = override;
        }

        public String getString(String name, String defaultValue) {
            var element = element(name);
            return element != null ? element.getAsString() : defaultValue;
        }

        public String getString(String name) {
            return element(name).getAsString();
        }

        public double getDouble(String name, double defaultValue) {
            var element = element(name);
            return element != null ? element.getAsDouble() : defaultValue;
        }

        public double getDouble(String name) {
            return element(name).getAsDouble();
        }

        public int getInt(String name, int defaultValue) {
            var element = element(name);
            return element != null ? element.getAsInt() : defaultValue;
        }

        public int getInt(String name) {
            return element(name).getAsInt();
        }

        public boolean getBoolean(String name, boolean defaultValue) {
            var element = element(name);
            return element != null ? element.getAsBoolean() : defaultValue;
        }

        public boolean getBoolean(String name) {
            return element(name).getAsBoolean();
        }

        protected JsonElement element(String name) {
            if (override != null && override.has(name)) {
                return override.get(name);
            }
            return main.get(name);
        }
    }
}
