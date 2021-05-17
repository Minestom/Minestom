package net.minestom.server.instance.block.incubator;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;

class BlockImpl implements BlockType {

    protected BlockImpl original = null;

    private final NamespaceID namespaceID;
    private final short minStateId, stateId;
    private final List<BlockProperty<?>> properties;

    private LinkedHashMap<BlockProperty<?>, Object> propertiesMap;

    private BlockImpl(NamespaceID namespaceID, short minStateId, short stateId, List<BlockProperty<?>> properties) {
        this.namespaceID = namespaceID;
        this.minStateId = minStateId;
        this.stateId = stateId;
        this.properties = properties;
    }

    protected static BlockImpl create(NamespaceID namespaceID, short minStateId, short stateId,
                                      List<BlockProperty<?>> properties) {
        var block = new BlockImpl(namespaceID, minStateId, stateId, properties);
        block.original = block;
        block.propertiesMap = computeMap(stateId, properties);
        return block;
    }

    @Override
    public @NotNull <T> BlockType withProperty(@NotNull BlockProperty<T> property, @NotNull T value) {
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

        var block = new BlockImpl(namespaceID, minStateId, computeId(minStateId, properties, map), properties);
        block.original = original;
        block.propertiesMap = map;
        return block;
    }

    @Override
    public @NotNull BlockType getDefaultBlock() {
        return original;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return namespaceID;
    }

    @Override
    public short getProtocolId() {
        return stateId;
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

    private static LinkedHashMap<BlockProperty<?>, Object> computeMap(short deltaId, List<BlockProperty<?>> properties) {
        LinkedHashMap<BlockProperty<?>, Object> result = new LinkedHashMap<>();
        int[] factors = computeFactors(properties);
        int index = 0;
        for (var property : properties) {
            final int factor = factors[index++];
            final int valueIndex = deltaId / factor;
            final var possibilities = property.getPossibleValues();
            final var value = possibilities.get(valueIndex);
            result.put(property, value);
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
