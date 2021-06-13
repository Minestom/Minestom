package net.minestom.server.block;

import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;

class BlockImpl implements Block {

    private NamespaceID namespaceID;
    private int blockId;
    private short minStateId, stateId;
    private List<BlockProperty<?>> properties;
    protected BlockImpl original = null;
    private LinkedHashMap<BlockProperty<?>, Object> propertiesMap;
    private BlockHandler handler;
    private NBTCompound compound;

    private BlockImpl() {
    }

    private BlockImpl(NamespaceID namespaceID,
                      int blockId,
                      short minStateId, short stateId,
                      List<BlockProperty<?>> properties,
                      LinkedHashMap<BlockProperty<?>, Object> propertiesMap,
                      NBTCompound compound) {
        this.namespaceID = namespaceID;
        this.blockId = blockId;
        this.minStateId = minStateId;
        this.stateId = stateId;
        this.properties = properties;
        this.propertiesMap = propertiesMap;
        this.compound = compound;
    }

    private BlockImpl(NamespaceID namespaceID,
                      int blockId, short minStateId, short stateId,
                      List<BlockProperty<?>> properties,
                      LinkedHashMap<BlockProperty<?>, Object> propertiesMap) {
        this(namespaceID, blockId, minStateId, stateId, properties, propertiesMap, null);
    }

    @Override
    public @NotNull <T> Block withProperty(@NotNull BlockProperty<T> property, @NotNull T value) {
        if (properties.isEmpty()) {
            // This block doesn't have any state
            return this;
        }
        final int index = properties.indexOf(property);
        if (index == -1) {
            // Invalid state
            return this;
        }

        // Find properties map
        LinkedHashMap<BlockProperty<?>, Object> map;
        if (propertiesMap == null) {
            // Represents the first id, create a new map
            map = new LinkedHashMap<>();
            properties.forEach(prop -> map.put(prop, prop.equals(property) ? value : null));
        } else {
            // Change property
            map = (LinkedHashMap<BlockProperty<?>, Object>) propertiesMap.clone();
            map.put(property, value);
        }

        var block = shallowClone();
        block.stateId = computeId(minStateId, properties, map);
        block.propertiesMap = map;
        return block;
    }

    @Override
    public @NotNull Block withProperty(@NotNull String property, @NotNull String value) {
        // TODO
        return null;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return tag.read(compound);
    }

    @Override
    public boolean hasTag(@NotNull Tag<?> tag) {
        return compound.containsKey(tag.getKey());
    }

    @Override
    public @NotNull <T> Block withTag(@NotNull Tag<T> tag, @Nullable T value) {
        if ((compound == null || compound.getKeys().isEmpty()) && value == null) {
            // No change
            return this;
        }

        // Apply tag
        NBTCompound compound = Objects.requireNonNullElseGet(this.compound, NBTCompound::new);
        tag.write(compound, value);
        if (compound.getKeys().isEmpty()) {
            compound = null;
        }
        return withNbt(compound);
    }

    @Override
    public @NotNull Block withNbt(@Nullable NBTCompound compound) {
        var block = shallowClone();
        block.compound = compound;
        return block;
    }

    @Override
    public @NotNull Block withHandler(@Nullable BlockHandler handler) {
        var block = shallowClone();
        block.handler = handler;
        return block;
    }

    @Override
    public <T> @NotNull T getProperty(@NotNull BlockProperty<T> property) {
        return (T) propertiesMap.get(property);
    }

    @Override
    public @NotNull String getProperty(@NotNull String property) {
        // TODO
        return null;
    }

    @Override
    public @Nullable NBTCompound getNbt() {
        return compound != null ? compound.deepClone() : null;
    }

    @Override
    public @NotNull Block getDefaultBlock() {
        return original;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return namespaceID;
    }

    @Override
    public @NotNull Map<String, String> createPropertiesMap() {
        Map<String, String> properties = new HashMap<>();
        propertiesMap.forEach((blockProperty, o) -> properties.put(blockProperty.getName(), o.toString()));
        return properties;
    }

    @Override
    public int getId() {
        return blockId;
    }

    @Override
    public short getStateId() {
        return stateId;
    }

    @Override
    public @Nullable BlockHandler getHandler() {
        return handler;
    }

    private @NotNull BlockImpl shallowClone() {
        var block = new BlockImpl();
        block.namespaceID = namespaceID;
        block.blockId = blockId;
        block.minStateId = minStateId;
        block.stateId = stateId;
        block.properties = properties;
        block.original = original;
        block.propertiesMap = propertiesMap;
        block.handler = handler;
        block.compound = compound;
        return block;
    }

    protected static BlockImpl create(NamespaceID namespaceID, short blockId, short minStateId, short maxStateId,
                                      short defaultStateId, List<BlockProperty<?>> properties) {
        var block = new BlockImpl(namespaceID, blockId, minStateId, defaultStateId,
                properties, computeMap(minStateId, defaultStateId, properties));
        block.original = block;
        Block.register(namespaceID, block,
                new IntRange((int) minStateId, (int) maxStateId), requestedStateId -> {
                    var requestedBlock = new BlockImpl(namespaceID, blockId, minStateId, requestedStateId,
                            properties, computeMap(minStateId, requestedStateId, properties));
                    requestedBlock.original = block;
                    return requestedBlock;
                });
        return block;
    }

    private static short computeId(short id, List<BlockProperty<?>> properties,
                                   LinkedHashMap<BlockProperty<?>, Object> propertiesMap) {
        int[] factors = computeFactors(properties);
        int index = 0;
        for (var entry : propertiesMap.entrySet()) {
            var property = entry.getKey();
            var value = entry.getValue();
            if (value != null) {
                var values = property.getPossibleValues();
                id += values.indexOf(value) * factors[index++];
            }
        }
        return id;
    }

    private static LinkedHashMap<BlockProperty<?>, Object> computeMap(short minStateId, short stateId, List<BlockProperty<?>> properties) {
        // Computes a filled property map from a state id

        LinkedHashMap<BlockProperty<?>, Object> result = new LinkedHashMap<>();
        int[] factors = computeFactors(properties);
        short deltaId = (short) (stateId - minStateId);
        int index = 0;
        for (var property : properties) {
            final var possibilities = property.getPossibleValues();
            final int factor = factors[index++];

            // TODO optimize
            int value = 0;
            int valueIndex = 0;
            while (value < deltaId) {
                if (value + factor > deltaId)
                    break;
                value += factor;
                valueIndex++;
            }
            deltaId -= value;

            result.put(property, possibilities.get(valueIndex));
        }
        return result;
    }

    private static int[] computeFactors(List<BlockProperty<?>> properties) {
        final int size = properties.size();
        int[] result = new int[size];
        int factor = 1;
        ListIterator<BlockProperty<?>> li = properties.listIterator(properties.size());
        // Iterate in reverse.
        int i = size;
        while (li.hasPrevious()) {
            var property = li.previous();
            result[--i] = factor;
            factor *= property.getPossibleValues().size();
        }

        return result;
    }
}