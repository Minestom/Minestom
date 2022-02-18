package net.minestom.server.listener.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.listener.*;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketListenerManager {

    public final static Logger LOGGER = LoggerFactory.getLogger(PacketListenerManager.class);
    private final ServerProcess serverProcess;

    private final Map<Class<? extends ClientPacket>, PacketListenerConsumer> listeners = new ConcurrentHashMap<>();

    public PacketListenerManager(ServerProcess serverProcess) {
        this.serverProcess = serverProcess;

        setListener(ClientKeepAlivePacket.class, KeepAliveListener::listener);
        setListener(ClientChatMessagePacket.class, ChatMessageListener::listener);
        setListener(ClientClickWindowPacket.class, WindowListener::clickWindowListener);
        setListener(ClientCloseWindowPacket.class, WindowListener::closeWindowListener);
        setListener(ClientPongPacket.class, WindowListener::pong);
        setListener(ClientEntityActionPacket.class, EntityActionListener::listener);
        setListener(ClientHeldItemChangePacket.class, PlayerHeldListener::heldListener);
        setListener(ClientPlayerBlockPlacementPacket.class, BlockPlacementListener::listener);
        setListener(ClientSteerVehiclePacket.class, PlayerVehicleListener::steerVehicleListener);
        setListener(ClientVehicleMovePacket.class, PlayerVehicleListener::vehicleMoveListener);
        setListener(ClientSteerBoatPacket.class, PlayerVehicleListener::boatSteerListener);
        setListener(ClientPlayerPacket.class, PlayerPositionListener::playerPacketListener);
        setListener(ClientPlayerRotationPacket.class, PlayerPositionListener::playerLookListener);
        setListener(ClientPlayerPositionPacket.class, PlayerPositionListener::playerPositionListener);
        setListener(ClientPlayerPositionAndRotationPacket.class, PlayerPositionListener::playerPositionAndLookListener);
        setListener(ClientTeleportConfirmPacket.class, PlayerPositionListener::teleportConfirmListener);
        setListener(ClientPlayerDiggingPacket.class, PlayerDiggingListener::playerDiggingListener);
        setListener(ClientAnimationPacket.class, AnimationListener::animationListener);
        setListener(ClientInteractEntityPacket.class, UseEntityListener::useEntityListener);
        setListener(ClientUseItemPacket.class, UseItemListener::useItemListener);
        setListener(ClientStatusPacket.class, StatusListener::listener);
        setListener(ClientSettingsPacket.class, SettingsListener::listener);
        setListener(ClientCreativeInventoryActionPacket.class, CreativeInventoryActionListener::listener);
        setListener(ClientCraftRecipeRequest.class, RecipeListener::listener);
        setListener(ClientTabCompletePacket.class, TabCompleteListener::listener);
        setListener(ClientPluginMessagePacket.class, PluginMessageListener::listener);
        setListener(ClientPlayerAbilitiesPacket.class, AbilitiesListener::listener);
        setListener(ClientResourcePackStatusPacket.class, ResourcePackListener::listener);
        setListener(ClientAdvancementTabPacket.class, AdvancementTabListener::listener);
        setListener(ClientSpectatePacket.class, SpectateListener::listener);
    }

    /**
     * Processes a packet by getting its {@link PacketListenerConsumer} and calling all the packet listeners.
     *
     * @param packet the received packet
     * @param player the player who sent the packet
     * @param <T>    the packet type
     */
    public <T extends ClientPacket> void processClientPacket(@NotNull T packet, @NotNull Player player) {

        final Class clazz = packet.getClass();

        PacketListenerConsumer<T> packetListenerConsumer = listeners.get(clazz);

        // Listener can be null if none has been set before, call PacketConsumer anyway
        if (packetListenerConsumer == null) {
            LOGGER.warn("Packet " + clazz + " does not have any default listener! (The issue comes from Minestom)");
        }

        // Event
        PlayerPacketEvent playerPacketEvent = new PlayerPacketEvent(player, packet);
        EventDispatcher.call(playerPacketEvent);
        if (playerPacketEvent.isCancelled()) {
            return;
        }

        // Finally execute the listener
        if (packetListenerConsumer != null) {
            try {
                packetListenerConsumer.accept(packet, player);
            } catch (Exception e) {
                // Packet is likely invalid
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }
    }

    /**
     * Sets the listener of a packet.
     * <p>
     * WARNING: this will overwrite the default minestom listener, this is not reversible.
     *
     * @param packetClass the class of the packet
     * @param consumer    the new packet's listener
     * @param <T>         the type of the packet
     */
    public <T extends ClientPacket> void setListener(@NotNull Class<T> packetClass, @NotNull PacketListenerConsumer<T> consumer) {
        this.listeners.put(packetClass, consumer);
    }

}
