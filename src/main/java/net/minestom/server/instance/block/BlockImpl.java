package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.Shape;
import net.minestom.server.item.Material;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.block.BlockUtils;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

record BlockImpl(Entry registry,
                 long propertiesArray,
                 @Nullable CompoundBinaryTag nbt,
                 @Nullable BlockHandler handler) implements Block {
    /**
     * Number of bits used to store the index of a property value.
     * <p>
     * Block states are all stored within a single number.
     */
    private static final int BITS_PER_INDEX = 5;

    private static final int MAX_STATES = Long.SIZE / BITS_PER_INDEX;
    private static final int MAX_VALUES = 1 << BITS_PER_INDEX;

    // Block state -> block object
    private static final List<Block> BLOCK_STATE_MAP;
    // Block id -> valid property keys (order is important for lookup)
    private static final List<PropertyType[]> PROPERTIES_TYPE;
    // Block id -> Map<Properties, Block>
    private static final List<Long2ObjectArrayMap<BlockImpl>> POSSIBLE_STATES;
    static final Registry<Block> REGISTRY;

    static {
        //TODO compute default sizes from the registry data
        ObjectArray<Block> blockStateMap = ObjectArray.singleThread();
        ObjectArray<PropertyType[]> propertiesType = ObjectArray.singleThread();
        ObjectArray<Long2ObjectArrayMap<BlockImpl>> possibleStates = ObjectArray.singleThread();
        Map<Object, Object> internCache = new HashMap<>();

        REGISTRY = RegistryData.createStaticRegistry(
                BuiltinRegistries.BLOCK,
                (namespace, properties) -> {
                    final int blockId = properties.getInt("id");
                    final RegistryData.Properties stateObject = properties.section("states");

                    // Retrieve properties
                    PropertyType[] propertyTypes;
                    {
                        RegistryData.Properties stateProperties = properties.section("properties");
                        if (stateProperties != null) {
                            final int stateCount = stateProperties.size();
                            if (stateCount > MAX_STATES) {
                                throw new IllegalStateException("Too many properties for block " + namespace);
                            }
                            propertyTypes = new PropertyType[stateCount];
                            int i = 0;
                            for (var entry : stateProperties) {
                                final var k = entry.getKey();
                                final var v = (List<String>) entry.getValue();
                                assert v.size() < MAX_VALUES;
                                propertyTypes[i++] = new PropertyType(k, v);
                            }
                        } else {
                            propertyTypes = PropertyType.NO_TYPES;
                        }
                    }
                    propertiesType.set(blockId, propertyTypes);

                    final EntryImpl baseBlockEntry = EntryImpl.parse(namespace, properties, internCache);
                    Block defaultBlock = null;

                    // Retrieve block states
                    {
                        final int propertiesCount = stateObject.size();
                        long[] propertiesKeys = new long[propertiesCount];
                        BlockImpl[] blocksValues = new BlockImpl[propertiesCount];
                        int propertiesOffset = 0;
                        for (var stateEntry : stateObject) {
                            final String query = stateEntry.getKey();
                            final var propertyMap = BlockUtils.parseProperties(query);
                            assert propertyTypes.length == propertyMap.size();
                            long propertiesValue = 0;
                            for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
                                final byte keyIndex = findKeyIndexThrow(propertyTypes, entry.getKey(), null);
                                final byte valueIndex = findValueIndexThrow(propertyTypes[keyIndex], entry.getValue(), null);
                                propertiesValue = updateIndex(propertiesValue, keyIndex, valueIndex);
                            }

                            final var stateOverride = RegistryData.Properties.fromMap((Map<String, Object>) stateEntry.getValue());
                            final Entry entryOverride = baseBlockEntry.withProperties(internCache, stateOverride);
                            final BlockImpl block = new BlockImpl(entryOverride,
                                    propertiesValue, null, null);
                            blockStateMap.set(block.stateId(), block);
                            if (block.stateId() == baseBlockEntry.stateId()) defaultBlock = block;
                            propertiesKeys[propertiesOffset] = propertiesValue;
                            blocksValues[propertiesOffset++] = block;
                        }
                        possibleStates.set(blockId, new Long2ObjectArrayMap<>(propertiesKeys, blocksValues, propertiesOffset));
                    }
                    // Register default state
                    return Objects.requireNonNull(defaultBlock);
                });
        BLOCK_STATE_MAP = blockStateMap.toList();
        PROPERTIES_TYPE = propertiesType.toList();
        POSSIBLE_STATES = possibleStates.toList();
    }

    static @UnknownNullability Block get(RegistryKey<Block> key) {
        return REGISTRY.get(key);
    }

    static int statesCount() {
        return BLOCK_STATE_MAP.size();
    }

    static Block getState(int stateId) {
        return BLOCK_STATE_MAP.get(stateId);
    }

    static @Nullable Block parseState(String input) {
        if (input.isEmpty()) return null;
        final int nbtIndex = input.indexOf("[");
        if (nbtIndex == 0) return null;
        if (nbtIndex == -1) return Block.fromKey(input);
        if (!input.endsWith("]")) return null;
        // Block state
        final String blockName = input.substring(0, nbtIndex);
        Block block = Block.fromKey(blockName);
        if (block == null) return null;
        // Compute properties
        final String query = input.substring(nbtIndex);
        final Map<String, String> propertyMap = BlockUtils.parseProperties(query);
        try {
            return block.withProperties(propertyMap);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public Block withProperty(String property, String value) {
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        assert propertyTypes != null;
        final byte keyIndex = findKeyIndexThrow(propertyTypes, property, this);
        final byte valueIndex = findValueIndexThrow(propertyTypes[keyIndex], value, this);
        final long updatedProperties = updateIndex(propertiesArray, keyIndex, valueIndex);
        return compute(updatedProperties);
    }

    @Override
    public Block withProperties(Map<String, String> properties) {
        if (properties.isEmpty()) return this;
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        assert propertyTypes != null;
        long updatedProperties = this.propertiesArray;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            final byte keyIndex = findKeyIndexThrow(propertyTypes, entry.getKey(), this);
            final byte valueIndex = findValueIndexThrow(propertyTypes[keyIndex], entry.getValue(), this);
            updatedProperties = updateIndex(updatedProperties, keyIndex, valueIndex);
        }
        return compute(updatedProperties);
    }

    @Override
    public <T> Block withTag(Tag<T> tag, @Nullable T value) {
        var builder = CompoundBinaryTag.builder();
        if (nbt != null) builder.put(nbt);
        tag.write(builder, value);
        final CompoundBinaryTag temporaryNbt = builder.build();
        final CompoundBinaryTag finalNbt = temporaryNbt.size() > 0 ? temporaryNbt : null;
        return new BlockImpl(registry, propertiesArray, finalNbt, handler);
    }

    @Override
    public Block withNbt(@Nullable CompoundBinaryTag compound) {
        return new BlockImpl(registry, propertiesArray, compound, handler);
    }

    @Override
    public Block withHandler(@Nullable BlockHandler handler) {
        return new BlockImpl(registry, propertiesArray, nbt, handler);
    }

    @Override
    public @Unmodifiable Map<String, String> properties() {
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        assert propertyTypes != null;
        final int length = propertyTypes.length;
        if (length == 0) return Map.of();
        String[] keys = new String[length];
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            PropertyType property = propertyTypes[i];
            keys[i] = property.key();
            final long index = extractIndex(propertiesArray, i);
            values[i] = property.values().get((int) index);
        }
        return Object2ObjectMaps.unmodifiable(new Object2ObjectArrayMap<>(keys, values, length));
    }

    @Override
    public String state() {
        final Map<String, String> properties = properties();
        if (properties.isEmpty()) return name();
        StringBuilder builder = new StringBuilder(name()).append('[');
        boolean first = true;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (first) first = false;
            else builder.append(',');
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        builder.append(']');
        return builder.toString();
    }

    @Override
    public Block defaultState() {
        return Block.fromBlockId(id());
    }

    @Override
    public @Nullable String getProperty(String property) {
        final PropertyType[] propertyTypes = PROPERTIES_TYPE.get(id());
        final int length = propertyTypes.length;
        if (length == 0) return null;
        final int key = findKeyIndex(propertyTypes, property);
        if (key == -1) return null; // Property not found
        final long index = extractIndex(propertiesArray, key);
        return propertyTypes[key].values().get((int) index);
    }

    @Override
    public Collection<Block> possibleStates() {
        return Collection.class.cast(possibleProperties().values());
    }

    @Override
    public <T> @UnknownNullability T getTag(Tag<T> tag) {
        return tag.read(Objects.requireNonNullElse(nbt, CompoundBinaryTag.empty()));
    }

    private Long2ObjectArrayMap<BlockImpl> possibleProperties() {
        return POSSIBLE_STATES.get(id());
    }

    @Override
    public String toString() {
        return String.format("%s{properties=%s, nbt=%s, handler=%s}", name(), properties(), nbt, handler);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockImpl block)) return false;
        return stateId() == block.stateId() && Objects.equals(nbt, block.nbt) && Objects.equals(handler, block.handler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stateId(), nbt, handler);
    }

    private Block compute(long updatedProperties) {
        if (updatedProperties == this.propertiesArray) return this;
        final BlockImpl block = possibleProperties().get(updatedProperties);
        assert block != null;
        // Reuse the same block instance if possible
        if (nbt == null && handler == null) return block;
        // Otherwise copy with the nbt and handler
        return new BlockImpl(block.registry(), block.propertiesArray, nbt, handler);
    }

    private static byte findKeyIndex(PropertyType[] properties, String key) {
        for (byte i = 0; i < properties.length; i++) {
            if (properties[i].key().equals(key)) return i;
        }
        return -1;
    }

    private static byte findValueIndex(PropertyType propertyType, String value) {
        final List<String> values = propertyType.values();
        return (byte) values.indexOf(value);
    }

    private static byte findKeyIndexThrow(PropertyType[] properties, String key, BlockImpl block) {
        final byte index = findKeyIndex(properties, key);
        if (index == -1) {
            if (block != null) {
                throw new IllegalArgumentException("Property " + key + " is not valid for block " + block);
            } else {
                throw new IllegalArgumentException("Unknown property key: " + key);
            }
        }
        return index;
    }

    private static byte findValueIndexThrow(PropertyType propertyType, String value, BlockImpl block) {
        final byte index = findValueIndex(propertyType, value);
        if (index == -1) {
            if (block != null) {
                throw new IllegalArgumentException("Property " + propertyType.key() + " value " + value + " is not valid for block " + block);
            } else {
                throw new IllegalArgumentException("Unknown property value: " + value);
            }
        }
        return index;
    }

    private record PropertyType(String key, List<String> values) {
        static final PropertyType[] NO_TYPES = new PropertyType[0];
    }

    static long updateIndex(long value, int index, byte newValue) {
        final int position = index * BITS_PER_INDEX;
        final int mask = (1 << BITS_PER_INDEX) - 1;
        value &= ~((long) mask << position); // Clear the bits at the specified position
        value |= (long) (newValue & mask) << position; // Set the new bits
        return value;
    }

    static long extractIndex(long value, int index) {
        final int position = index * BITS_PER_INDEX;
        final int mask = (1 << BITS_PER_INDEX) - 1;
        return ((value >> position) & mask);
    }

    public record EntryImpl(
            Key key,
            int id,
            int stateId,
            String translationKey,
            float hardness,
            float explosionResistance,
            float friction,
            float speedFactor,
            float jumpFactor,
            byte packedFlags,
            byte lightEmission,
            @Nullable Key blockEntity,
            int blockEntityId,
            @Nullable Material material,
            @Nullable BlockSoundType blockSoundType,
            Shape collisionShape,
            Shape occlusionShape
    ) implements Entry {
        private static final byte AIR_OFFSET = 1 << 0;
        private static final byte LIQUID_OFFSET = 1 << 1;
        private static final byte SOLID_OFFSET = 1 << 2;
        private static final byte OCCLUDES_OFFSET = 1 << 3;
        private static final byte REQUIRES_TOOL_OFFSET = 1 << 4;
        private static final byte REPLACEABLE_OFFSET = 1 << 5;
        private static final byte REDSTONE_CONDUCTOR_OFFSET = 1 << 6;
        private static final byte SIGNAL_SOURCE_OFFSET = -1 << 7; // 2's complement

        @Override
        public boolean isAir() {
            return (packedFlags & AIR_OFFSET) != 0;
        }

        @Override
        public boolean isLiquid() {
            return (packedFlags & LIQUID_OFFSET) != 0;
        }

        @Override
        public boolean isSolid() {
            return (packedFlags & SOLID_OFFSET) != 0;
        }

        @Override
        public boolean occludes() {
            return (packedFlags & OCCLUDES_OFFSET) != 0;
        }

        @Override
        public boolean requiresTool() {
            return (packedFlags & REQUIRES_TOOL_OFFSET) != 0;
        }

        @Override
        public boolean isReplaceable() {
            return (packedFlags & REPLACEABLE_OFFSET) != 0;
        }

        @Override
        public boolean isBlockEntity() {
            return blockEntity != null;
        }

        @Override
        public boolean isRedstoneConductor() {
            return (packedFlags & REDSTONE_CONDUCTOR_OFFSET) != 0;
        }

        @Override
        public boolean isSignalSource() {
            return (packedFlags & SIGNAL_SOURCE_OFFSET) != 0;
        }

        @Override
        public Entry withKey(Key key) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withId(int id) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withStateId(int stateId) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withTranslationKey(String translationKey) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withHardness(float hardness) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withExplosionResistance(float explosionResistance) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withFriction(float friction) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withSpeedFactor(float speedFactor) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withJumpFactor(float jumpFactor) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withAir(boolean air) {
            return withFlag(AIR_OFFSET, air);
        }

        @Override
        public Entry withLiquid(boolean liquid) {
            return withFlag(LIQUID_OFFSET, liquid);
        }

        @Override
        public Entry withSolid(boolean solid) {
            return withFlag(SOLID_OFFSET, solid);
        }

        @Override
        public Entry withOccludes(boolean occludes) {
            return withFlag(OCCLUDES_OFFSET, occludes);
        }

        @Override
        public Entry withRequiresTool(boolean requiresTool) {
            return withFlag(REQUIRES_TOOL_OFFSET, requiresTool);
        }

        @Override
        public Entry withLightEmission(byte lightEmission) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withReplaceable(boolean replaceable) {
            return withFlag(REPLACEABLE_OFFSET, replaceable);
        }

        @Override
        public Entry withBlockEntity(@Nullable Key blockEntity) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withBlockEntityId(int blockEntityId) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withMaterial(@Nullable Material material) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withBlockSoundType(@Nullable BlockSoundType blockSoundType) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withRedstoneConductor(boolean redstoneConductor) {
            return withFlag(REDSTONE_CONDUCTOR_OFFSET, redstoneConductor);
        }

        @Override
        public Entry withSignalSource(boolean signalSource) {
            return withFlag(SIGNAL_SOURCE_OFFSET, signalSource);
        }

        @Override
        public Entry withCollisionShape(Shape collisionShape) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        @Override
        public Entry withOcclusionShape(Shape occlusionShape) {
            return new EntryImpl(key, id, stateId, translationKey, hardness, explosionResistance, friction, speedFactor, jumpFactor, packedFlags, lightEmission, blockEntity, blockEntityId, material, blockSoundType, collisionShape, occlusionShape);
        }

        private Entry withFlag(byte flag, boolean enabled) {
            byte newFlags = enabled ? (byte) (packedFlags | flag) : (byte) (packedFlags & ~flag);
            return new EntryImpl(
                    key, id, stateId, translationKey, hardness, explosionResistance, friction,
                    speedFactor, jumpFactor, newFlags, lightEmission, blockEntity, blockEntityId,
                    material, blockSoundType, collisionShape, occlusionShape
            );
        }

        @SuppressWarnings({"unchecked", "PatternValidation"})
        private Entry withProperties(Map<Object, Object> internCache, RegistryData.Properties properties) {
            Entry entry = this;
            for (var mapEntry : properties) {
                var key = mapEntry.getKey();
                var value = mapEntry.getValue();
                entry = switch (key) {
                    case "key" -> entry.withKey(Key.key(value.toString())); // Ideally this should never happen
                    case "id" -> entry.withId(((Number) value).intValue());
                    case "stateId" -> entry.withStateId(((Number) value).intValue());
                    case "translationKey" -> entry.withTranslationKey(((String) value));
                    case "hardness" -> entry.withHardness(((Number) value).floatValue());
                    case "explosionResistance" -> entry.withExplosionResistance(((Number) value).floatValue());
                    case "friction" -> entry.withFriction(((Number) value).floatValue());
                    case "speedFactor" -> entry.withSpeedFactor(((Number) value).floatValue());
                    case "jumpFactor" -> entry.withJumpFactor(((Number) value).floatValue());
                    case "air" -> entry.withAir((boolean) value);
                    case "solid" -> entry.withSolid((boolean) value);
                    case "liquid" -> entry.withLiquid((boolean) value);
                    case "occludes" -> entry.withOccludes((boolean) value);
                    case "requiresTool" -> entry.withRequiresTool((boolean) value);
                    case "lightEmission" -> entry.withLightEmission(((Number) value).byteValue());
                    case "replaceable" -> entry.withReplaceable((boolean) value);
                    case "soundType" -> entry.withBlockSoundType(BlockSoundType.fromKey((String) value));
                    case "blockEntity" -> {
                        final RegistryData.Properties blockSection = RegistryData.Properties.fromMap((Map<String, Object>) value);
                        yield entry.withBlockEntity(Key.key(blockSection.getString("namespace")))
                                .withBlockEntityId(blockSection.getInt("blockEntityId"));
                    }
                    case "collisionShape" ->
                            entry.withCollisionShape(CollisionUtils.parseCollisionShape(internCache, (String) value));
                    case "occlusionShape" -> {
                        final boolean modifyingLight = properties.containsKey("lightEmission");
                        final boolean modifyingOcclusion = properties.containsKey("occludes");
                        final byte lightEmission = modifyingLight ? properties.getByte("lightEmission") : entry.lightEmission();
                        final boolean occludes = modifyingOcclusion ? properties.getBoolean("occludes") : entry.occludes();
                        yield entry.withOcclusionShape(CollisionUtils.parseOcclusionShape(internCache, (String) value, occludes, lightEmission));
                    }
                    case "correspondingItem" -> entry.withMaterial(Material.fromKey((String) value));
                    case "redstoneConductor" -> entry.withRedstoneConductor((boolean) value);
                    case "signalSource" -> entry.withSignalSource((boolean) value);
                    case "shape", "visualShape", "blocksMotion", "solidBlocking", "canRespawnIn", "interactionShape",
                         "mapColorId", "flammable" -> entry; //TODO we probably should parse these for line of sight.
                    default -> throw new IllegalArgumentException("Unknown key: " + key);
                };
            }
            return entry;
        }

        @SuppressWarnings("PatternValidation")
        private static EntryImpl parse(RegistryKey<Block> key, RegistryData.Properties main, Map<Object, Object> internCache) {
            var id = main.getInt("id");
            var stateId = main.getInt("defaultStateId");
            var translationKey = main.getString("translationKey");
            var hardness = main.getFloat("hardness");
            var explosionResistance = main.getFloat("explosionResistance");
            var friction = main.getFloat("friction");
            var speedFactor = main.getFloat("speedFactor", 1);
            var jumpFactor = main.getFloat("jumpFactor", 1);
            var air = main.getBoolean("air", false);
            var solid = main.getBoolean("solid");
            var liquid = main.getBoolean("liquid", false);
            var occludes = main.getBoolean("occludes", true);
            var requiresTool = main.getBoolean("requiresTool", true);
            var lightEmission = main.getByte("lightEmission", (byte) 0);
            var replaceable = main.getBoolean("replaceable", false);
            var blockSoundType = BlockSoundType.fromKey(main.getString("soundType"));
            var blockEntity = main.section("blockEntity");
            final Key blockEntityKey;
            final int blockEntityId;
            if (blockEntity != null) {
                blockEntityKey = Key.key(blockEntity.getString("namespace"));
                blockEntityId = blockEntity.getInt("id");
            } else {
                blockEntityKey = null;
                blockEntityId = 0;
            }
            var material = main.containsKey("correspondingItem") ? Material.fromKey(main.getString("correspondingItem")) : null;
            final String collision = main.getString("collisionShape");
            final String occlusion = main.getString("occlusionShape");
            var collisionShape = CollisionUtils.parseCollisionShape(internCache, collision);
            var occlusionShape = CollisionUtils.parseOcclusionShape(internCache, occlusion, occludes, lightEmission);
            var redstoneConductor = main.getBoolean("redstoneConductor");
            var signalSource = main.getBoolean("signalSource", false);
            byte packedFlags = (byte) ((air ? AIR_OFFSET : 0) |
                            (liquid ? LIQUID_OFFSET : 0) |
                            (solid ? SOLID_OFFSET : 0) |
                            (occludes ? OCCLUDES_OFFSET : 0) |
                            (requiresTool ? REQUIRES_TOOL_OFFSET : 0) |
                            (replaceable ? REPLACEABLE_OFFSET : 0) |
                            (redstoneConductor ? REDSTONE_CONDUCTOR_OFFSET : 0) |
                            (signalSource ? SIGNAL_SOURCE_OFFSET : 0)
            );
            return new EntryImpl(
                    key.key(), id, stateId, translationKey, hardness, explosionResistance, friction,
                    speedFactor, jumpFactor, packedFlags, lightEmission, blockEntityKey, blockEntityId,
                    material, blockSoundType, collisionShape, occlusionShape
            );
        }
    }
}
