package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.block.BlockChangeEvent;
import net.minestom.server.event.trait.BlockEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(BlockManager.class);

    private final static EventNode<BlockEvent> BLOCK_EVENT_NODE = EventNode.type("blockmanager-node", EventFilter.BLOCK)
            .setPriority(50);


    private final Int2ObjectMap<BlockHandler> blockHandlerMap = new Int2ObjectOpenHashMap<>();

    private final Set<Key> dummyWarning = ConcurrentHashMap.newKeySet();

    public BlockManager(GlobalEventHandler eventHandler) {
        eventHandler.addChild(BLOCK_EVENT_NODE);

        BLOCK_EVENT_NODE.addListener(BlockChangeEvent.class, event -> {
            blockHandlerMap.get(event.getBlock().id());
        });
    }

    public void registerHandler(int id, @NotNull BlockHandler blockHandler) {
        blockHandlerMap.put(id, blockHandler);
    }

    public void registerHandler(@NotNull Block block, @NotNull BlockHandler blockHandler) {
        registerHandler(block.stateId(), blockHandler);
    }

    public @Nullable BlockHandler getHandler(@NotNull Block block) {
        final var handler = blockHandlerMap.get(block.id());
        return handler != null ? handler : null;
    }

    @ApiStatus.Internal
    public @NotNull BlockHandler getHandlerOrDummy(@NotNull Block block) {
        BlockHandler handler = getHandler(block);
        if (handler == null) {
            if (dummyWarning.add(block.key())) {
                LOGGER.warn("""
                        Block {} does not have any corresponding handler, default to dummy.
                        You may want to register a handler for this key to prevent any data loss.""", block.namespace());
            }
            handler = BlockHandler.Dummy.get(block.namespace().toString());
        }
        return handler;
    }
}
