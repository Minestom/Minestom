package net.minestom.server.listener.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.listener.*;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.play.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketListenerManager {

    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    private Map<Class<? extends ClientPlayPacket>, PacketListenerConsumer> listeners = new ConcurrentHashMap<>();

    public PacketListenerManager() {
        setListener(ClientKeepAlivePacket.class, KeepAliveListener::listener);
        setListener(ClientChatMessagePacket.class, ChatMessageListener::listener);
        setListener(ClientClickWindowPacket.class, WindowListener::clickWindowListener);
        setListener(ClientCloseWindow.class, WindowListener::closeWindowListener);
        setListener(ClientWindowConfirmationPacket.class, WindowListener::windowConfirmationListener);
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
        setListener(ClientTeleportConfirmPacket.class, TeleportListener::listener);
        setListener(ClientResourcePackStatusPacket.class, ResourcePackListener::listener);
        setListener(ClientAdvancementTabPacket.class, AdvancementTabListener::listener);
    }

    public <T extends ClientPlayPacket> void process(T packet, Player player) {

        final Class clazz = packet.getClass();

        PacketListenerConsumer<T> packetListenerConsumer = listeners.get(clazz);

        // Listener can be null if none has been set before, call PacketConsumer anyway
        if (packetListenerConsumer == null) {
            System.err.println("Packet " + clazz + " does not have any default listener!");
        }


        final PacketController packetController = new PacketController(packetListenerConsumer);
        for (PacketConsumer packetConsumer : CONNECTION_MANAGER.getReceivePacketConsumers()) {
            packetConsumer.accept(player, packetController, packet);
        }

        if (packetController.isCancel())
            return;

        // Get the new listener (or the same) from the packet controller
        packetListenerConsumer = packetController.getPacketListenerConsumer();

        // Call the listener if not null
        // (can be null because no listener is set, or because it has been changed by the controller)
        if (packetListenerConsumer != null) {
            packetListenerConsumer.accept(packet, player);
        }
    }

    /**
     * Set the listener of a packet
     * <p>
     * WARNING: this will overwrite the default minestom listener, this is not reversible
     *
     * @param packetClass the class of the packet
     * @param consumer    the new packet's listener
     * @param <T>         the type of the packet
     */
    public <T extends ClientPlayPacket> void setListener(Class<T> packetClass, PacketListenerConsumer<T> consumer) {
        this.listeners.put(packetClass, consumer);
    }

}
