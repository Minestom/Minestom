package net.minestom.server.utils.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;
import java.util.Objects;

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
        if (query.length() == 2) return Map.of();

        final int entries = StringUtils.countMatches(query, ',') + 1;
        assert entries > 0;
        String[] keys = new String[entries];
        String[] values = new String[entries];
        int entryCount = 0;

        final int length = query.length() - 1;
        int start = 1;
        int index = 1;
        while (index <= length) {
            if (query.charAt(index) == ',' || index == length) {
                final int equalIndex = query.indexOf('=', start);
                if (equalIndex != -1) {
                    final String key = query.substring(start, equalIndex).trim();
                    final String value = query.substring(equalIndex + 1, index).trim();
                    keys[entryCount] = key;
                    values[entryCount++] = value;
                }
                start = index + 1;
            }
            index++;
        }
        return new Object2ObjectArrayMap<>(keys, values, entryCount);
    }

    public static @Nullable NBTCompound extractClientNbt(@NotNull Block block) {
        if (!block.registry().isBlockEntity()) return null;
        // Append handler tags
        final BlockHandler handler = block.handler();
        final NBTCompound blockNbt = Objects.requireNonNullElseGet(block.nbt(), NBTCompound::new);
        if (handler != null) {
            // Extract explicitly defined tags and keep the rest server-side
            return NBT.Compound(nbt -> {
                for (Tag<?> tag : handler.getBlockEntityTags()) {
                    final var value = tag.read(blockNbt);
                    if (value != null) {
                        // Tag is present and valid
                        tag.writeUnsafe(nbt, value);
                    }
                }
            });
        }
        // Complete nbt shall be sent if the block has no handler
        // Necessary to support all vanilla blocks
        return blockNbt;
    }
}
