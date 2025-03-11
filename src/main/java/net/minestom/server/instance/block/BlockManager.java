package net.minestom.server.instance.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.block.BlockChangeEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(BlockManager.class);

    private final static EventNode<Event> BLOCK_EVENT_NODE = EventNode.type("blockmanager-node", EventFilter.ALL)
            .setPriority(50);

    private final Int2ObjectMap<BlockHandler> blockHandlerMap = new Int2ObjectOpenHashMap<>();

    private final Set<NamespaceID> dummyWarning = ConcurrentHashMap.newKeySet();

    public BlockManager(GlobalEventHandler eventHandler) {
        eventHandler.addChild(BLOCK_EVENT_NODE);

        BLOCK_EVENT_NODE.addListener(BlockChangeEvent.class, event -> {
            BlockHandler handler;

            //Block is air, assume that it is a destruction event
            if(event.getBlock() == Block.AIR) {
                handler = event.getPreviousBlock().handler();
                if(handler == null)
                    handler = blockHandlerMap.get(event.getPreviousBlock().id());
            } else {
                handler = event.getBlock().handler();
                if(handler == null)
                    handler = blockHandlerMap.get(event.getBlock().id());
            }

            if(handler == null) { return; }

            handler.onBlockChange(event);
        });

        BLOCK_EVENT_NODE.addListener(PlayerBlockInteractEvent.class, event -> {
            BlockHandler handler;

            handler = event.getBlock().handler();

            if(handler == null)
                handler = blockHandlerMap.get(event.getBlock().id());

            if(handler == null) { return; }

            handler.onInteract(event);
        });
    }

    public void registerHandler(int id, @NotNull BlockHandler blockHandler) {
        blockHandlerMap.put(id, blockHandler);
    }

    public void registerHandler(@NotNull Block block, @NotNull BlockHandler blockHandler) {
        registerHandler(block.id(), blockHandler);
    }

    public @Nullable BlockHandler getHandler(@NotNull Block block) {
        return blockHandlerMap.get(block.id());
    }

    @ApiStatus.Internal
    public @NotNull BlockHandler getHandlerOrDummy(@NotNull Block block) {
        BlockHandler handler = getHandler(block);
        if (handler == null) {
            if (dummyWarning.add(block.namespace())) {
                LOGGER.warn("""
                        Block {} does not have any corresponding handler, default to dummy.
                        You may want to register a handler for this key to prevent any data loss.""", block.namespace());
            }
            handler = BlockHandler.Dummy.get(block);
        }
        return handler;
    }
}
