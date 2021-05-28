package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectSortedMap;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.math.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

class BlockRegistry {

    private static final Map<NamespaceID, Block> namespaceMap = new HashMap<>();
    private static final Int2ObjectSortedMap<Block> blockSet = new Int2ObjectAVLTreeMap<>();
    private static final Short2ObjectSortedMap<Block.Supplier> stateSet = new Short2ObjectAVLTreeMap<>();

    public static synchronized @Nullable Block fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return namespaceMap.get(namespaceID);
    }

    public static synchronized @Nullable Block fromStateId(short stateId) {
        Block.Supplier supplier = stateSet.get(stateId);
        if (supplier == null) {
            return null;
        }
        return supplier.get(stateId);
    }

    public static synchronized @Nullable Block fromBlockId(int blockId) {
        return blockSet.get(blockId);
    }

    public static synchronized void register(@NotNull NamespaceID namespaceID, @NotNull Block block,
                                             @NotNull IntRange range, @NotNull Block.Supplier blockSupplier) {
        namespaceMap.put(namespaceID, block);
        IntStream.range(range.getMinimum(), range.getMaximum() + 1).forEach(value -> stateSet.put((short) value, blockSupplier));
        blockSet.put(block.getId(), block);
    }
}
