package net.minestom.server.utils.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.StringUtils;

import java.util.Map;

public class BlockUtils {

    private final Instance instance;
    private final Point position;

    public BlockUtils(Instance instance, Point position) {
        this.instance = instance;
        this.position = position;
    }

    public BlockUtils getRelativeTo(int x, int y, int z) {
        return new BlockUtils(instance, position.add(x, y, z));
    }

    public BlockUtils above() {
        return getRelativeTo(0, 1, 0);
    }

    public BlockUtils below() {
        return getRelativeTo(0, -1, 0);
    }

    public BlockUtils north() {
        return getRelativeTo(0, 0, -1);
    }

    public BlockUtils east() {
        return getRelativeTo(1, 0, 0);
    }

    public BlockUtils south() {
        return getRelativeTo(0, 0, 1);
    }

    public BlockUtils west() {
        return getRelativeTo(-1, 0, 0);
    }

    public Block getBlock() {
        return instance.getBlock(position);
    }

    public boolean equals(Block block) {
        return getBlock().compare(block);
    }

    public static Map<String, String> parseProperties(String query) {
        if (!query.startsWith("[") || !query.endsWith("]")) return Map.of();
        final String propertiesString = query.substring(1, query.length() - 1).trim();
        if (propertiesString.isEmpty()) return Map.of();

        final int entries = StringUtils.countMatches(propertiesString, ',') + 1;
        assert entries > 0;
        String[] keys = new String[entries];
        String[] values = new String[entries];
        int entryCount = 0;

        final int length = propertiesString.length();
        int start = 0;
        int index = 0;
        while (index < length) {
            if (propertiesString.charAt(index) == ',' || index == length - 1) {
                if (index + 1 == length) index++;
                final String property = propertiesString.substring(start, index).replace(',', '\0');
                final int equalIndex = property.indexOf('=');
                if (equalIndex != -1) {
                    final String key = property.substring(0, equalIndex).trim();
                    final String value = property.substring(equalIndex + 1).trim();
                    keys[entryCount] = key.intern();
                    values[entryCount++] = value.intern();
                }
                start = index + 1;
            }
            index++;
        }
        return switch (entryCount) {
            case 0 -> Map.of();
            case 1 -> Map.of(keys[0], values[0]);
            case 2 -> Map.of(keys[0], values[0], keys[1], values[1]);
            case 3 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2]);
            case 4 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3]);
            case 5 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4]);
            case 6 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4], keys[5], values[5]);
            case 7 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4], keys[5], values[5], keys[6], values[6]);
            case 8 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4], keys[5], values[5], keys[6], values[6],
                    keys[7], values[7]);
            case 9 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4], keys[5], values[5], keys[6], values[6],
                    keys[7], values[7], keys[8], values[8]);
            case 10 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4], keys[5], values[5], keys[6],
                    values[6], keys[7], values[7], keys[8], values[8], keys[9], values[9]);
            default -> {
                if (entryCount == keys.length) {
                    yield Map.copyOf(new Object2ObjectArrayMap<>(keys, values));
                } else {
                    // Arrays must be resized
                    final String[] newKeys = new String[entryCount];
                    final String[] newValues = new String[entryCount];
                    System.arraycopy(keys, 0, newKeys, 0, entryCount);
                    System.arraycopy(values, 0, newValues, 0, entryCount);
                    yield Map.copyOf(new Object2ObjectArrayMap<>(newKeys, newValues));
                }
            }
        };
    }
}
