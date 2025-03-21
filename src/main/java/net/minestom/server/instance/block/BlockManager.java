package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
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

    private final Set<String> dummyWarning = ConcurrentHashMap.newKeySet(); // Prevent warning spam

    public void registerHandler(@NotNull String namespace, @NotNull Supplier<@NotNull BlockHandler> handlerSupplier) {
        blockHandlerMap.put(namespace, handlerSupplier);
    }

    public void registerHandler(@NotNull Key key, @NotNull Supplier<@NotNull BlockHandler> handlerSupplier) {
        registerHandler(key.toString(), handlerSupplier);
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
}
