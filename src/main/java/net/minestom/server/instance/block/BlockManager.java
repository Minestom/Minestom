package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class BlockManager {

    // Namespace -> handler supplier
    private final Map<String, Supplier<BlockHandler>> blockHandlerMap = new ConcurrentHashMap<>();

    // block id -> block placement rule
    private final Int2ObjectMap<BlockPlacementRule> placementRuleMap = new Int2ObjectOpenHashMap<>();

    public synchronized void registerHandler(@NotNull String namespace, @NotNull Supplier<@NotNull BlockHandler> handlerSupplier) {
        this.blockHandlerMap.put(namespace, handlerSupplier);
    }

    public synchronized @Nullable BlockHandler getHandler(@NotNull String namespace) {
        final var handler = blockHandlerMap.get(namespace);
        return handler != null ? handler.get() : null;
    }

    /**
     * Registers a {@link BlockPlacementRule}.
     *
     * @param blockPlacementRule the block placement rule to register
     * @throws IllegalArgumentException if <code>blockPlacementRule</code> block id is negative
     */
    public synchronized void registerBlockPlacementRule(@NotNull BlockPlacementRule blockPlacementRule) {
        final int id = blockPlacementRule.getBlock().id();
        Check.argCondition(id < 0, "Block ID must be >= 0, got: " + id);
        this.placementRuleMap.put(id, blockPlacementRule);
    }

    /**
     * Gets the {@link BlockPlacementRule} of the specific block.
     *
     * @param block the block to check
     * @return the block placement rule associated with the block, null if not any
     */
    public synchronized @Nullable BlockPlacementRule getBlockPlacementRule(@NotNull Block block) {
        return placementRuleMap.get(block.id());
    }
}
