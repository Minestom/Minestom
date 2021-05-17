package net.minestom.server.instance.block.incubator;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class BlockImpl implements BlockType {

    protected BlockImpl original = null;

    private final NamespaceID namespaceID;
    private final short minStateId, stateId;
    private final List<BlockProperty<Object>> properties;

    private LinkedHashMap<BlockProperty<Object>, Object> propertiesMap;

    private BlockImpl(NamespaceID namespaceID, short minStateId, short stateId, List<BlockProperty<Object>> properties) {
        this.namespaceID = namespaceID;
        this.minStateId = minStateId;
        this.stateId = stateId;
        this.properties = properties;
    }

    protected static BlockImpl firstBlock(NamespaceID namespaceID, short id, List<BlockProperty<Object>> properties) {
        var block = new BlockImpl(namespaceID, id, id, properties);
        block.original = block;
        return block;
    }

    protected static BlockImpl defaultBlock(NamespaceID namespaceID, short minStateId, short stateId,
                                            List<BlockProperty<Object>> properties) {
        var block = new BlockImpl(namespaceID, minStateId, stateId, properties);
        block.original = block;
        return block;
    }

    protected static BlockImpl defaultBlock(NamespaceID namespaceID, short minStateId,
                                            List<BlockProperty<Object>> properties,
                                            LinkedHashMap<BlockProperty<Object>, Object> propertiesMap) {
        var block = new BlockImpl(namespaceID, minStateId, computeId(minStateId, propertiesMap), properties);
        block.original = block;
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
        LinkedHashMap<BlockProperty<Object>, Object> map;
        if (propertiesMap == null) {
            // Represents the first id, create a new map
            map = new LinkedHashMap<>();
            properties.forEach(prop -> map.put(prop, prop.equals(property) ? value : null));
        } else {
            // Change property
            map = (LinkedHashMap<BlockProperty<Object>, Object>) propertiesMap.clone();
            map.put((BlockProperty<Object>) property, value);
        }

        var block = new BlockImpl(namespaceID, minStateId, computeId(minStateId, map), properties);
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

    private static short computeId(short id, LinkedHashMap<BlockProperty<Object>, Object> properties) {
        var reverse = reverse(properties);
        int factor = 1;
        for (var entry : reverse.entrySet()) {
            var property = entry.getKey();
            var value = entry.getValue();
            var values = property.getPossibleValues();
            if (value != null) {
                id += values.indexOf(value) * factor;
            }
            factor *= values.size();
        }
        return id;
    }

    private static <K, V> LinkedHashMap<K, V> reverse(LinkedHashMap<K, V> map) {
        LinkedHashMap<K, V> reversedMap = new LinkedHashMap<>();
        ListIterator<Map.Entry<K, V>> it = new ArrayList<>(map.entrySet()).listIterator(map.entrySet().size());
        while (it.hasPrevious()) {
            Map.Entry<K, V> el = it.previous();
            reversedMap.put(el.getKey(), el.getValue());
        }
        return reversedMap;
    }
}
