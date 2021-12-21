package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class BlockManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(BlockManager.class);
    // Namespace -> handler supplier
    private final Map<String, Supplier<BlockHandler>> blockHandlerMap = new ConcurrentHashMap<>();
    // block id -> block placement rule
    private final Int2ObjectMap<BlockPlacementRule> placementRuleMap = new Int2ObjectOpenHashMap<>();

    private final Set<String> dummyWarning = ConcurrentHashMap.newKeySet(); // Prevent warning spam

    public void registerHandler(@NotNull String namespace, @NotNull Supplier<@NotNull BlockHandler> handlerSupplier) {
        blockHandlerMap.put(namespace, handlerSupplier);
    }

    public void registerHandler(@NotNull NamespaceID namespace, @NotNull Supplier<@NotNull BlockHandler> handlerSupplier) {
        registerHandler(namespace.toString(), handlerSupplier);
    }

    public @Nullable BlockHandler getHandler(@NotNull String namespace) {
        final var handler = blockHandlerMap.get(namespace);
        return handler != null ? handler.get() : null;
    }

    @ApiStatus.Internal
    public @NotNull BlockHandler getHandlerOrDummy(@NotNull String namespace) {
        BlockHandler handler = getHandler(namespace);
        if (handler == null) {
            if (dummyWarning.add(namespace)) {
                LOGGER.warn("""
                        Block {} does not have any corresponding handler, default to dummy.
                        You may want to register a handler for this namespace to prevent any data loss.""", namespace);
            }
            handler = BlockHandler.Dummy.get(namespace);
        }
        return handler;
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
        placementRuleMap.put(id, blockPlacementRule);
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
