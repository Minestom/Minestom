package net.minestom.server.utils.block;

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
        if (!query.startsWith("[") || !query.endsWith("]") ||
                query.equals("[]")) {
            return Map.of();
        }
        final String propertiesString = query.substring(1, query.length() - 1).trim();
        if (propertiesString.isEmpty()) {
            return Map.of();
        }

        final int capacity = StringUtils.countMatches(propertiesString, ',') + 1;
        String[] entries = new String[capacity * 2];
        int entryIndex = 0;

        final int length = propertiesString.length();
        int start = 0;
        int end;
        int index = 0;
        while (index < length) {
            if (propertiesString.charAt(index) == ',' || index == length - 1) {
                if (index + 1 == length) index++;
                end = index;
                final String property = propertiesString.substring(start, end);
                final int equalIndex = property.indexOf('=');
                if (equalIndex != -1) {
                    final String key = property.substring(0, equalIndex).trim();
                    final String value = property.substring(equalIndex + 1).trim();
                    entries[entryIndex++] = key.intern();
                    entries[entryIndex++] = value.intern();
                }
                start = end + 1;
            }
            index++;
        }
        return switch (entryIndex / 2) {
            case 0 -> Map.of();
            case 1 -> Map.of(entries[0], entries[1]);
            case 2 -> Map.of(entries[0], entries[1], entries[2], entries[3]);
            case 3 -> Map.of(entries[0], entries[1],
                    entries[2], entries[3], entries[4], entries[5]);
            case 4 -> Map.of(entries[0], entries[1], entries[2], entries[3],
                    entries[4], entries[5], entries[6], entries[7]);
            case 5 -> Map.of(entries[0], entries[1], entries[2], entries[3],
                    entries[4], entries[5], entries[6], entries[7],
                    entries[8], entries[9]);
            case 6 -> Map.of(entries[0], entries[1], entries[2], entries[3],
                    entries[4], entries[5], entries[6], entries[7],
                    entries[8], entries[9], entries[10], entries[11]);
            case 7 -> Map.of(entries[0], entries[1], entries[2], entries[3],
                    entries[4], entries[5], entries[6], entries[7],
                    entries[8], entries[9], entries[10], entries[11],
                    entries[12], entries[13]);
            case 8 -> Map.of(entries[0], entries[1], entries[2], entries[3],
                    entries[4], entries[5], entries[6], entries[7],
                    entries[8], entries[9], entries[10], entries[11],
                    entries[12], entries[13], entries[14], entries[15]);
            case 9 -> Map.of(entries[0], entries[1], entries[2], entries[3],
                    entries[4], entries[5], entries[6], entries[7],
                    entries[8], entries[9], entries[10], entries[11],
                    entries[12], entries[13], entries[14], entries[15],
                    entries[16], entries[17]);
            case 10 -> Map.of(entries[0], entries[1], entries[2], entries[3],
                    entries[4], entries[5], entries[6], entries[7],
                    entries[8], entries[9], entries[10], entries[11],
                    entries[12], entries[13], entries[14], entries[15],
                    entries[16], entries[17], entries[18], entries[19]);
            default -> throw new IllegalArgumentException("Too many properties: " + (entryIndex / 2));
        };
    }
}
