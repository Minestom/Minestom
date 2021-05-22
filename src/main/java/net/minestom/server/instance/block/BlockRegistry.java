package net.minestom.server.instance.block;

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
    private static final Short2ObjectSortedMap<Block.Supplier> stateSet = new Short2ObjectAVLTreeMap<>();

    public static synchronized @Nullable Block fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return namespaceMap.get(namespaceID);
    }

    public static synchronized @Nullable Block fromStateId(short stateId) {
        Block.Supplier supplier = stateSet.get(stateId);
        return supplier.get(stateId);
    }

    public static synchronized void register(@NotNull NamespaceID namespaceID, @NotNull Block block,
                                             @NotNull IntRange range, @NotNull Block.Supplier blockSupplier) {
        namespaceMap.put(namespaceID, block);
        IntStream.range(range.getMinimum(), range.getMaximum()).forEach(value -> stateSet.put((short) value, blockSupplier));
    }
}
