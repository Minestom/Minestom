package fr.themode.minestom.listener;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;
import fr.themode.minestom.net.packet.client.play.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class PacketListenerManager {

    private Map<Class<? extends ClientPlayPacket>, BiConsumer<? extends ClientPlayPacket, Player>> listeners = new ConcurrentHashMap<>();

    public PacketListenerManager() {
        addListener(ClientKeepAlivePacket.class, KeepAliveListener::listener);
        addListener(ClientChatMessagePacket.class, ChatMessageListener::listener);
        addListener(ClientClickWindowPacket.class, WindowListener::clickWindowListener);
        addListener(ClientCloseWindow.class, WindowListener::closeWindowListener);
        addListener(ClientEntityActionPacket.class, EntityActionListener::listener);
        addListener(ClientHeldItemChangePacket.class, PlayerHeldListener::heldListener);
        addListener(ClientPlayerBlockPlacementPacket.class, BlockPlacementListener::listener);
        addListener(ClientSteerVehiclePacket.class, PlayerVehicleListener::steerVehicleListener);
        addListener(ClientPlayerPacket.class, PlayerPositionListener::playerPacketListener);
        addListener(ClientPlayerLookPacket.class, PlayerPositionListener::playerLookListener);
        addListener(ClientPlayerPositionPacket.class, PlayerPositionListener::playerPositionListener);
        addListener(ClientPlayerPositionAndLookPacket.class, PlayerPositionListener::playerPositionAndLookListener);
        addListener(ClientPlayerDiggingPacket.class, PlayerDiggingListener::playerDiggingListener);
        addListener(ClientAnimationPacket.class, AnimationListener::animationListener);
        addListener(ClientUseEntityPacket.class, UseEntityListener::useEntityListener);
        addListener(ClientUseItemPacket.class, UseItemListener::useItemListener);
        addListener(ClientStatusPacket.class, StatusListener::listener);
        addListener(ClientSettingsPacket.class, SettingsListener::listener);
    }

    public <T extends ClientPlayPacket> void process(T packet, Player player) {
        BiConsumer<T, Player> biConsumer = (BiConsumer<T, Player>) listeners.get(packet.getClass());
        if (biConsumer == null) {
            // System.err.println("Packet " + packet.getClass() + " does not have any listener!");
            return;
        }
        biConsumer.accept(packet, player);
    }

    public <T extends ClientPlayPacket> void addListener(Class<T> packetClass, BiConsumer<T, Player> consumer) {
        this.listeners.put(packetClass, consumer);
    }

}
