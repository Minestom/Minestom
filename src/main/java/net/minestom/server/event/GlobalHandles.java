package net.minestom.server.event;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.inventory.PlayerInventoryItemChangeEvent;
import net.minestom.server.event.player.*;
import org.jetbrains.annotations.ApiStatus;

/**
 * Contains handles to the main node {@link MinecraftServer#getGlobalEventHandler()}
 * (compatible with {@link EventDispatcher}).
 */
@ApiStatus.Internal
public final class GlobalHandles {
    public static final ListenerHandle<PlayerTickEvent> PLAYER_TICK = EventDispatcher.getHandle(PlayerTickEvent.class);
    public static final ListenerHandle<PlayerPacketEvent> PLAYER_PACKET = EventDispatcher.getHandle(PlayerPacketEvent.class);
    public static final ListenerHandle<PlayerMoveEvent> PLAYER_MOVE = EventDispatcher.getHandle(PlayerMoveEvent.class);
    public static final ListenerHandle<EntityTickEvent> ENTITY_TICK = EventDispatcher.getHandle(EntityTickEvent.class);
    public static final ListenerHandle<InstanceTickEvent> INSTANCE_TICK = EventDispatcher.getHandle(InstanceTickEvent.class);
    public static final ListenerHandle<PlayerChunkLoadEvent> PLAYER_CHUNK_LOAD = EventDispatcher.getHandle(PlayerChunkLoadEvent.class);
    public static final ListenerHandle<PlayerChunkUnloadEvent> PLAYER_CHUNK_UNLOAD = EventDispatcher.getHandle(PlayerChunkUnloadEvent.class);
    public static final ListenerHandle<InstanceChunkLoadEvent> INSTANCE_CHUNK_LOAD = EventDispatcher.getHandle(InstanceChunkLoadEvent.class);
    public static final ListenerHandle<InventoryItemChangeEvent> INVENTORY_ITEM_CHANGE_EVENT = EventDispatcher.getHandle(InventoryItemChangeEvent.class);
    public static final ListenerHandle<PlayerInventoryItemChangeEvent> PLAYER_INVENTORY_ITEM_CHANGE_EVENT = EventDispatcher.getHandle(PlayerInventoryItemChangeEvent.class);
}
