package net.minestom.server.listener.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalHandles;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.listener.*;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketListenerManager {

    public final static Logger LOGGER = LoggerFactory.getLogger(PacketListenerManager.class);
    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    private final Map<Class<? extends ClientPlayPacket>, PacketListenerConsumer> listeners = new ConcurrentHashMap<>();

    public PacketListenerManager() {
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
    public <T extends ClientPlayPacket> void processClientPacket(@NotNull T packet, @NotNull Player player) {

        final Class clazz = packet.getClass();

        PacketListenerConsumer<T> packetListenerConsumer = listeners.get(clazz);

        // Listener can be null if none has been set before, call PacketConsumer anyway
        if (packetListenerConsumer == null) {
            LOGGER.warn("Packet " + clazz + " does not have any default listener! (The issue comes from Minestom)");
        }

        // TODO remove legacy
        {
            final PacketController packetController = new PacketController();
            for (ClientPacketConsumer clientPacketConsumer : CONNECTION_MANAGER.getReceivePacketConsumers()) {
                clientPacketConsumer.accept(player, packetController, packet);
            }

            if (packetController.isCancel())
                return;
        }

        // Event
        PlayerPacketEvent playerPacketEvent = new PlayerPacketEvent(player, packet);
        GlobalHandles.PLAYER_PACKET.call(playerPacketEvent);
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
     * Executes the consumers from {@link ConnectionManager#onPacketSend(ServerPacketConsumer)}.
     *
     * @param packet  the packet to process
     * @param players the players which should receive the packet
     * @return true if the packet is not cancelled, false otherwise
     */
    public boolean processServerPacket(@NotNull ServerPacket packet, @NotNull Collection<Player> players) {
        final List<ServerPacketConsumer> consumers = CONNECTION_MANAGER.getSendPacketConsumers();
        if (consumers.isEmpty()) {
            return true;
        }

        final PacketController packetController = new PacketController();
        for (ServerPacketConsumer serverPacketConsumer : consumers) {
            serverPacketConsumer.accept(players, packetController, packet);
        }

        return !packetController.isCancel();
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
    public <T extends ClientPlayPacket> void setListener(@NotNull Class<T> packetClass, @NotNull PacketListenerConsumer<T> consumer) {
        this.listeners.put(packetClass, consumer);
    }

}
