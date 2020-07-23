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
        addListener(ClientKeepAlivePacket.class, KeepAliveListener::listener);
        addListener(ClientChatMessagePacket.class, ChatMessageListener::listener);
        addListener(ClientClickWindowPacket.class, WindowListener::clickWindowListener);
        addListener(ClientCloseWindow.class, WindowListener::closeWindowListener);
        addListener(ClientWindowConfirmationPacket.class, WindowListener::windowConfirmationListener);
        addListener(ClientEntityActionPacket.class, EntityActionListener::listener);
        addListener(ClientHeldItemChangePacket.class, PlayerHeldListener::heldListener);
        addListener(ClientPlayerBlockPlacementPacket.class, BlockPlacementListener::listener);
        addListener(ClientSteerVehiclePacket.class, PlayerVehicleListener::steerVehicleListener);
        addListener(ClientVehicleMovePacket.class, PlayerVehicleListener::vehicleMoveListener);
        addListener(ClientSteerBoatPacket.class, PlayerVehicleListener::boatSteerListener);
        addListener(ClientPlayerPacket.class, PlayerPositionListener::playerPacketListener);
        addListener(ClientPlayerRotationPacket.class, PlayerPositionListener::playerLookListener);
        addListener(ClientPlayerPositionPacket.class, PlayerPositionListener::playerPositionListener);
        addListener(ClientPlayerPositionAndRotationPacket.class, PlayerPositionListener::playerPositionAndLookListener);
        addListener(ClientPlayerDiggingPacket.class, PlayerDiggingListener::playerDiggingListener);
        addListener(ClientAnimationPacket.class, AnimationListener::animationListener);
        addListener(ClientInteractEntityPacket.class, UseEntityListener::useEntityListener);
        addListener(ClientUseItemPacket.class, UseItemListener::useItemListener);
        addListener(ClientStatusPacket.class, StatusListener::listener);
        addListener(ClientSettingsPacket.class, SettingsListener::listener);
        addListener(ClientCreativeInventoryActionPacket.class, CreativeInventoryActionListener::listener);
        addListener(ClientCraftRecipeRequest.class, RecipeListener::listener);
        addListener(ClientTabCompletePacket.class, TabCompleteListener::listener);
        addListener(ClientPluginMessagePacket.class, PluginMessageListener::listener);
        addListener(ClientPlayerAbilitiesPacket.class, AbilitiesListener::listener);
        addListener(ClientTeleportConfirmPacket.class, TeleportListener::listener);
        addListener(ClientResourcePackStatusPacket.class, ResourcePackListener::listener);
    }

    public <T extends ClientPlayPacket> void process(T packet, Player player) {

        final Class clazz = packet.getClass();

        final PacketListenerConsumer<T> packetListenerConsumer = listeners.get(clazz);

        // Listener can be null if none has been set before, call PacketConsumer anyway
        if (packetListenerConsumer == null) {
            System.err.println("Packet " + clazz + " does not have any default listener!");
        }


        final PacketController packetController = new PacketController(packetListenerConsumer);
        for (PacketConsumer packetConsumer : CONNECTION_MANAGER.getPacketConsumers()) {
            packetConsumer.accept(player, packetController, packet);
        }

        if (packetController.isCancel())
            return;

        // Call the listener if not null
        // (can be null because no listener is set, or because it has been changed by the controller)
        if (packetListenerConsumer != null) {
            packetListenerConsumer.accept(packet, player);
        }
    }

    public <T extends ClientPlayPacket> void addListener(Class<T> packetClass, PacketListenerConsumer<T> consumer) {
        this.listeners.put(packetClass, consumer);
    }

}
