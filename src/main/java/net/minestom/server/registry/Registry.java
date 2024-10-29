package net.minestom.server.registry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.ToNumberPolicy;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.Shape;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EntitySpawnType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.Material;
import net.minestom.server.message.ChatTypeDecoration;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.collection.ObjectArray;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
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
    @ApiStatus.Internal
    public static BlockEntry block(String namespace, @NotNull Properties main) {
        return new BlockEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static DimensionTypeEntry dimensionType(String namespace, @NotNull Properties main) {
        return new DimensionTypeEntry(namespace, main, null);
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
    public static FeatureFlagEntry featureFlag(String namespace, @NotNull Properties main) {
        return new FeatureFlagEntry(namespace, main, null);
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
    public static BannerPatternEntry bannerPattern(String namespace, @NotNull Properties main) {
        return new BannerPatternEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static WolfVariantEntry wolfVariant(String namespace, @NotNull Properties main) {
        return new WolfVariantEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static ChatTypeEntry chatType(String namespace, @NotNull Properties main) {
        return new ChatTypeEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static EnchantmentEntry enchantment(String namespace, @NotNull Properties main) {
        return new EnchantmentEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static PaintingVariantEntry paintingVariant(String namespace, @NotNull Properties main) {
        return new PaintingVariantEntry(namespace, main, null);
    }

    @ApiStatus.Internal
    public static JukeboxSongEntry jukeboxSong(String namespace, @NotNull Properties main) {
        return new JukeboxSongEntry(namespace, main, null);
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
    public static <T extends StaticProtocolObject> Container<T> createStaticContainer(Resource resource, Container.Loader<T> loader) {
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
        ITEM_TAGS("tags/item.json"),
        ENCHANTMENT_TAGS("tags/enchantment.json"),
        BIOME_TAGS("tags/biome.json"),
        DIMENSION_TYPES("dimension_types.json"),
        BIOMES("biomes.json"),
        ATTRIBUTES("attributes.json"),
        BANNER_PATTERNS("banner_patterns.json"),
        WOLF_VARIANTS("wolf_variants.json"),
        CHAT_TYPES("chat_types.json"),
        ENCHANTMENTS("enchantments.snbt"),
        PAINTING_VARIANTS("painting_variants.json"),
        JUKEBOX_SONGS("jukebox_songs.json");

        private final String name;

        Resource(String name) {
            this.name = name;
        }

        public @NotNull String fileName() {
            return name;
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
        private final boolean requiresTool;
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
            this.requiresTool = main.getBoolean("requiresTool", true);
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

        @Override
        public Properties custom() {
            return custom;
        }
    }

    public record DimensionTypeEntry(
            NamespaceID namespace,
            boolean ultrawarm,
            boolean natural,
            double coordinateScale,
            boolean hasSkylight,
            boolean hasCeiling,
            float ambientLight,
            Long fixedTime,
            boolean piglinSafe,
            boolean bedWorks,
            boolean respawnAnchorWorks,
            boolean hasRaids,
            int logicalHeight,
            int minY,
            int height,
            String infiniburn,
            String effects,
            Properties custom
    ) implements Entry {

        public DimensionTypeEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    main.getBoolean("ultrawarm"),
                    main.getBoolean("natural"),
                    main.getDouble("coordinate_scale"),
                    main.getBoolean("has_skylight"),
                    main.getBoolean("has_ceiling"),
                    main.getFloat("ambient_light"),
                    (Long) main.asMap().get("fixed_time"),
                    main.getBoolean("piglin_safe"),
                    main.getBoolean("bed_works"),
                    main.getBoolean("respawn_anchor_works"),
                    main.getBoolean("has_raids"),
                    main.getInt("logical_height"),
                    main.getInt("min_y"),
                    main.getInt("height"),
                    main.getString("infiniburn"),
                    main.getString("effects"),
                    custom);
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
            this.namespace = NamespaceID.from(namespace);
            this.id = main.getInt("id");
            this.translationKey = main.getString("translationKey");
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

        public @NotNull String translationKey() {
            return translationKey;
        }

        public @Nullable Block block() {
            return blockSupplier.get();
        }

        public @NotNull DataComponentMap prototype() {
            if (prototype == null) {
                try {
                    BinaryTagSerializer.Context context = new BinaryTagSerializer.ContextWithRegistries(MinecraftServer.process(), false);
                    DataComponentMap.Builder builder = DataComponentMap.builder();
                    for (Map.Entry<String, Object> entry : main.section("components")) {
                        //noinspection unchecked
                        DataComponent<Object> component = (DataComponent<Object>) ItemComponent.fromNamespaceId(entry.getKey());
                        Check.notNull(component, "Unknown component {0} in {1}", entry.getKey(), namespace);

                        BinaryTag tag = TagStringIOExt.readTag((String) entry.getValue());
                        builder.set(component, component.read(context, tag));
                    }
                    this.prototype = builder.build();
                } catch (IOException e) {
                    throw new RuntimeException("failed to parse material registry: " + namespace, e);
                }
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
        private final NamespaceID namespace;
        private final int id;
        private final String translationKey;
        private final double drag;
        private final double acceleration;
        private final EntitySpawnType spawnType;
        private final double width;
        private final double height;
        private final double eyeHeight;
        private final BoundingBox boundingBox;
        private final Properties custom;

        public EntityEntry(String namespace, Properties main, Properties custom) {
            this.namespace = NamespaceID.from(namespace);
            this.id = main.getInt("id");
            this.translationKey = main.getString("translationKey");
            this.drag = main.getDouble("drag", 0.02);
            this.acceleration = main.getDouble("acceleration", 0.08);
            this.spawnType = EntitySpawnType.valueOf(main.getString("packetType").toUpperCase(Locale.ROOT));

            // Dimensions
            this.width = main.getDouble("width");
            this.height = main.getDouble("height");
            this.eyeHeight = main.getDouble("eyeHeight");
            this.boundingBox = new BoundingBox(this.width, this.height, this.width);

            // Attachments
            Properties attachments = main.section("attachments");
            if (attachments != null) {
                //todo
            }

            this.custom = custom;
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

        public double drag() {
            return drag;
        }

        public double acceleration() {
            return acceleration;
        }

        public @NotNull EntitySpawnType spawnType() {
            return spawnType;
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

        public @NotNull BoundingBox boundingBox() {
            return boundingBox;
        }

        @Override
        public Properties custom() {
            return custom;
        }
    }

    public record FeatureFlagEntry(NamespaceID namespace, int id, Properties custom) implements Entry {
        public FeatureFlagEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    main.getInt("id"),
                    null
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
                                    @NotNull Map<String, String> overrideArmorMaterials,
                                    @NotNull Component description,
                                    Properties custom) implements Entry {
        public TrimMaterialEntry(@NotNull String namespace, @NotNull Properties main, Properties custom) {
            this(
                    NamespaceID.from(namespace),
                    main.getString("asset_name"),
                    Objects.requireNonNull(Material.fromNamespaceId(main.getString("ingredient"))),
                    (float) main.getDouble("item_model_index"),
                    Objects.requireNonNullElse(main.section("override_armor_materials"), new PropertiesMap(Map.of()))
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

    public record AttributeEntry(NamespaceID namespace, int id,
                                 String translationKey, double defaultValue,
                                 boolean clientSync,
                                 double maxValue, double minValue,
                                 Properties custom) implements Entry {
        public AttributeEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    main.getInt("id"),
                    main.getString("translationKey"),
                    main.getDouble("defaultValue"),
                    main.getBoolean("clientSync"),
                    main.getDouble("maxValue"),
                    main.getDouble("minValue"),
                    custom);
        }
    }

    public record BannerPatternEntry(NamespaceID namespace, NamespaceID assetId, String translationKey, Properties custom) implements Entry {
        public BannerPatternEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    NamespaceID.from(main.getString("asset_id")),
                    main.getString("translation_key"),
                    custom);
        }
    }

    public record WolfVariantEntry(NamespaceID namespace, NamespaceID wildTexture, NamespaceID tameTexture, NamespaceID angryTexture, List<String> biomes, Properties custom) implements Entry {
        public WolfVariantEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    NamespaceID.from(main.getString("wild_texture")),
                    NamespaceID.from(main.getString("tame_texture")),
                    NamespaceID.from(main.getString("angry_texture")),
                    readBiomesList(main.asMap().get("biomes")),
                    custom);
        }

        private static @NotNull List<String> readBiomesList(Object biomes) {
            if (biomes instanceof List<?> list) {
                return list.stream().map(Object::toString).collect(Collectors.toList());
            } else if (biomes instanceof String single) {
                return List.of(single);
            } else {
                throw new IllegalArgumentException("invalid biomes entry: " + biomes);
            }
        }
    }

    public static final class ChatTypeEntry implements Entry {
        private final NamespaceID namespace;
        private final ChatTypeDecoration chat;
        private final ChatTypeDecoration narration;
        private final Properties custom;

        public ChatTypeEntry(String namespace, Properties main, Properties custom) {
            this.namespace = NamespaceID.from(namespace);
            this.chat = readChatTypeDecoration(main.section("chat"));
            this.narration = readChatTypeDecoration(main.section("narration"));
            this.custom = custom;
        }

        public NamespaceID namespace() {
            return namespace;
        }

        public ChatTypeDecoration chat() {
            return chat;
        }

        public ChatTypeDecoration narration() {
            return narration;
        }

        @Override
        public Properties custom() {
            return custom;
        }

        private static @NotNull ChatTypeDecoration readChatTypeDecoration(Properties properties) {
            List<ChatTypeDecoration.Parameter> parameters = new ArrayList<>();
            if (properties.asMap().get("parameters") instanceof List<?> paramList) {
                for (Object param : paramList) {
                    parameters.add(ChatTypeDecoration.Parameter.valueOf(param.toString().toUpperCase(Locale.ROOT)));
                }
            }
            Style style = Style.empty();
            if (properties.containsKey("style")) {
                // This is admittedly a pretty cursed way to handle deserialization here, however I do not want to
                // write a standalone JSON style deserializer for this one use case.
                // The methodology is to just convert it to a valid text component and deserialize that, then take the style.
                Gson gson = GsonComponentSerializer.gson().serializer();
                JsonObject textComponentJson = gson.toJsonTree(properties.section("style").asMap()).getAsJsonObject();
                textComponentJson.addProperty("text", "IGNORED_VALUE");
                Component textComponent = GsonComponentSerializer.gson().deserializeFromTree(textComponentJson);
                style = textComponent.style();
            }
            return new ChatTypeDecoration(properties.getString("translation_key"), parameters, style);
        }

    }

    public record EnchantmentEntry(NamespaceID namespace, String raw, Properties custom) implements Entry {
        public EnchantmentEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace), main.getString("raw"), custom);
        }
    }

    public record PaintingVariantEntry(NamespaceID namespace, NamespaceID assetId, int width, int height, Properties custom) implements Entry {
        public PaintingVariantEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    NamespaceID.from(main.getString("asset_id")),
                    main.getInt("width"),
                    main.getInt("height"),
                    custom);
        }
    }

    public record JukeboxSongEntry(NamespaceID namespace, SoundEvent soundEvent, Component description,
                                   float lengthInSeconds, int comparatorOutput, Properties custom) implements Entry {
        public JukeboxSongEntry(String namespace, Properties main, Properties custom) {
            this(NamespaceID.from(namespace),
                    SoundEvent.fromNamespaceId(main.getString("sound_event")),
                    GsonComponentSerializer.gson().deserialize(main.section("description").toString()),
                    (float) main.getDouble("length_in_seconds"),
                    main.getInt("comparator_output"),
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
