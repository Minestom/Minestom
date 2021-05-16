package net.minestom.server.instance.block.incubator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

class BlockStateImpl implements BlockType {

    private final BlockImpl original;
    private final short id;

    private final Map<BlockProperty<?>, ?> properties;

    protected BlockStateImpl(BlockImpl original, LinkedHashMap<BlockProperty<?>, ?> properties) {
        this.original = original;
        this.properties = properties;
        this.id = computeId(properties);
    }

    @Override
    public @NotNull <T> BlockType withProperty(@NotNull BlockProperty<T> property, @NotNull T value) {
        if (!properties.containsKey(property)) {
            // Invalid property
            return this;
        }
        LinkedHashMap<BlockProperty<?>, T> map = new LinkedHashMap<>();
        properties.forEach((prop, o) -> map.put(prop, prop.equals(property) ? value : (T) o));
        return new BlockStateImpl(original, map);
    }

    @Override
    public @NotNull BlockType getDefaultBlock() {
        return original;
    }

    @Override
    public short getProtocolId() {
        return id;
    }

    private static short computeId(LinkedHashMap<BlockProperty<?>, ?> properties) {
        short id = 0;
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
