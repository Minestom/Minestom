package net.minestom.server.listener.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.listener.*;
import net.minestom.server.listener.common.*;
import net.minestom.server.listener.preplay.HandshakeListener;
import net.minestom.server.listener.preplay.LoginListener;
import net.minestom.server.listener.preplay.StatusListener;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.*;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.configuration.ClientSelectKnownPacksPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketListenerManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(PacketListenerManager.class);

    private final Map<Class<? extends ClientPacket>, PacketPrePlayListenerConsumer>[] listeners = new Map[ConnectionState.values().length];

    public PacketListenerManager() {
        for (int i = 0; i < listeners.length; i++) {
            listeners[i] = new ConcurrentHashMap<>();
        }

        setListener(ConnectionState.HANDSHAKE, ClientHandshakePacket.class, HandshakeListener::listener);

        setListener(ConnectionState.STATUS, StatusRequestPacket.class, StatusListener::requestListener);
        setListener(ConnectionState.STATUS, ClientPingRequestPacket.class, StatusListener::pingRequestListener);

        setListener(ConnectionState.LOGIN, ClientLoginStartPacket.class, LoginListener::loginStartListener);
        setListener(ConnectionState.LOGIN, ClientEncryptionResponsePacket.class, LoginListener::loginEncryptionResponseListener);
        setListener(ConnectionState.LOGIN, ClientLoginPluginResponsePacket.class, LoginListener::loginPluginResponseListener);
        setListener(ConnectionState.LOGIN, ClientLoginAcknowledgedPacket.class, LoginListener::loginAckListener);
        setListener(ConnectionState.LOGIN, ClientCookieResponsePacket.class, CookieListener::handleCookieResponse);

        setConfigurationListener(ClientSettingsPacket.class, SettingsListener::listener);
        setConfigurationListener(ClientPluginMessagePacket.class, PluginMessageListener::listener);
        setConfigurationListener(ClientKeepAlivePacket.class, KeepAliveListener::listener);
        setConfigurationListener(ClientPongPacket.class, (packet, player) -> {/* empty */});
        setConfigurationListener(ClientResourcePackStatusPacket.class, ResourcePackListener::listener);
        setConfigurationListener(ClientSelectKnownPacksPacket.class, LoginListener::selectKnownPacks);
        setConfigurationListener(ClientFinishConfigurationPacket.class, LoginListener::finishConfigListener);
        setListener(ConnectionState.CONFIGURATION, ClientCookieResponsePacket.class, CookieListener::handleCookieResponse);
        setConfigurationListener(ClientCustomClickActionPacket.class, CustomClickListener::listener);

        setPlayListener(ClientKeepAlivePacket.class, KeepAliveListener::listener);
        setPlayListener(ClientCommandChatPacket.class, ChatMessageListener::commandChatListener);
        setPlayListener(ClientChatMessagePacket.class, ChatMessageListener::chatMessageListener);
        setPlayListener(ClientClickWindowPacket.class, WindowListener::clickWindowListener);
        setPlayListener(ClientCloseWindowPacket.class, WindowListener::closeWindowListener);
        setPlayListener(ClientConfigurationAckPacket.class, LoginListener::configAckListener);
        setPlayListener(ClientPongPacket.class, WindowListener::pong);
        setPlayListener(ClientEntityActionPacket.class, EntityActionListener::listener);
        setPlayListener(ClientHeldItemChangePacket.class, PlayerHeldListener::heldListener);
        setPlayListener(ClientPlayerBlockPlacementPacket.class, BlockPlacementListener::listener);
        setPlayListener(ClientInputPacket.class, PlayerInputListener::listener);
        setPlayListener(ClientChangeGameModePacket.class, PlayerGameModeChangeListener::listener);
        setPlayListener(ClientVehicleMovePacket.class, PlayerVehicleListener::vehicleMoveListener);
        setPlayListener(ClientSteerBoatPacket.class, PlayerVehicleListener::boatSteerListener);
        setPlayListener(ClientPlayerPositionStatusPacket.class, PlayerPositionListener::playerPacketListener);
        setPlayListener(ClientPlayerRotationPacket.class, PlayerPositionListener::playerLookListener);
        setPlayListener(ClientPlayerPositionPacket.class, PlayerPositionListener::playerPositionListener);
        setPlayListener(ClientPlayerPositionAndRotationPacket.class, PlayerPositionListener::playerPositionAndLookListener);
        setPlayListener(ClientTeleportConfirmPacket.class, PlayerPositionListener::teleportConfirmListener);
        setPlayListener(ClientPlayerDiggingPacket.class, PlayerDiggingListener::playerDiggingListener);
        setPlayListener(ClientAnimationPacket.class, AnimationListener::animationListener);
        setPlayListener(ClientInteractEntityPacket.class, UseEntityListener::useEntityListener);
        setPlayListener(ClientUseItemPacket.class, UseItemListener::useItemListener);
        setPlayListener(ClientPickItemFromBlockPacket.class, PlayerPickListener::playerPickBlockListener);
        setPlayListener(ClientPickItemFromEntityPacket.class, PlayerPickListener::playerPickEntityListener);
        setPlayListener(ClientStatusPacket.class, PlayStatusListener::listener);
        setPlayListener(ClientSettingsPacket.class, SettingsListener::listener);
        setPlayListener(ClientCreativeInventoryActionPacket.class, CreativeInventoryActionListener::listener);
        setPlayListener(ClientSetRecipeBookStatePacket.class, (packet, player) -> {/* empty */});
        setPlayListener(ClientPlaceRecipePacket.class, RecipeListener::listener);
        setPlayListener(ClientTabCompletePacket.class, TabCompleteListener::listener);
        setPlayListener(ClientPluginMessagePacket.class, PluginMessageListener::listener);
        setPlayListener(ClientPlayerAbilitiesPacket.class, AbilitiesListener::listener);
        setPlayListener(ClientResourcePackStatusPacket.class, ResourcePackListener::listener);
        setPlayListener(ClientAdvancementTabPacket.class, AdvancementTabListener::listener);
        setPlayListener(ClientSpectatePacket.class, SpectateListener::listener);
        setPlayListener(ClientEditBookPacket.class, BookListener::listener);
        setPlayListener(ClientChatSessionUpdatePacket.class, (packet, player) -> {/* empty */});
        setPlayListener(ClientChunkBatchReceivedPacket.class, ChunkBatchListener::batchReceivedListener);
        setPlayListener(ClientPingRequestPacket.class, PlayPingListener::requestListener);
        setListener(ConnectionState.PLAY, ClientCookieResponsePacket.class, CookieListener::handleCookieResponse);
        setPlayListener(ClientNameItemPacket.class, AnvilListener::nameItemListener);
        setPlayListener(ClientTickEndPacket.class, PlayerTickListener::listener);
        setPlayListener(ClientPlayerLoadedPacket.class, PlayerLoadedListener::listener);
        setPlayListener(ClientSelectBundleItemPacket.class, (packet, player) -> {/* noop for now */});
        setPlayListener(ClientSignedCommandChatPacket.class, ChatMessageListener::signedCommandChatListener);
        setPlayListener(ClientCustomClickActionPacket.class, CustomClickListener::listener);
        setPlayListener(ClientUpdateSignPacket.class, EditSignListener::listener);
    }

    /**
     * Processes a packet by getting its {@link PacketPlayListenerConsumer} and calling all the packet listeners.
     *
     * @param packet     the received packet
     * @param connection the connection of the player who sent the packet
     * @param <T>        the packet type
     */
    public <T extends ClientPacket> void processClientPacket(@NotNull T packet, @NotNull PlayerConnection connection, @NotNull ConnectionState state) {
        final Class clazz = packet.getClass();
        PacketPrePlayListenerConsumer<T> packetListenerConsumer = listeners[state.ordinal()].get(clazz);

        // Listener can be null if none has been set before, call PacketConsumer anyway
        if (packetListenerConsumer == null) {
            LOGGER.warn("Packet {}:{} does not have any default listener! (The issue likely comes from Minestom)", clazz, state);
            return;
        }

        // Event
        if (state == ConnectionState.PLAY) {
            PlayerPacketEvent playerPacketEvent = new PlayerPacketEvent(connection.getPlayer(), packet);
            EventDispatcher.call(playerPacketEvent);
            if (playerPacketEvent.isCancelled()) {
                return;
            }
        }

        // Finally execute the listener
        try {
            packetListenerConsumer.accept(packet, connection);
        } catch (Exception e) {
            // Packet is likely invalid
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    /**
     * Sets the listener of a packet.
     * <p>
     * WARNING: this will overwrite the default minestom listener, this is not reversible.
     *
     * @param state       the state of the packet
     * @param packetClass the class of the packet
     * @param consumer    the new packet's listener
     * @param <T>         the type of the packet
     */
    public <T extends ClientPacket> void setListener(@NotNull ConnectionState state, @NotNull Class<T> packetClass, @NotNull PacketPrePlayListenerConsumer<T> consumer) {
        this.listeners[state.ordinal()].put(packetClass, consumer);
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
    public <T extends ClientPacket> void setPlayListener(@NotNull Class<T> packetClass, @NotNull PacketPlayListenerConsumer<T> consumer) {
        setListener(ConnectionState.PLAY, packetClass, (packet, playerConnection) -> consumer.accept(packet, playerConnection.getPlayer()));
    }

    public <T extends ClientPacket> void setConfigurationListener(@NotNull Class<T> packetClass, @NotNull PacketPlayListenerConsumer<T> consumer) {
        setListener(ConnectionState.CONFIGURATION, packetClass, (packet, playerConnection) -> consumer.accept(packet, playerConnection.getPlayer()));
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
    @Deprecated
    public <T extends ClientPacket> void setListener(@NotNull Class<T> packetClass, @NotNull PacketPlayListenerConsumer<T> consumer) {
        setPlayListener(packetClass, consumer);
    }

}
