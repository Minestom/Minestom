package net.minestom.server.network.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.ListenerHandle;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.ProtocolSession;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientCookieResponsePacket;
import net.minestom.server.network.packet.client.common.ClientKeepAlivePacket;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.configuration.ClientSelectKnownPacksPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.crypto.SecretKey;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.zip.DataFormatException;

/**
 * Represents a socket connection.
 * <p>
 * It is the implementation used for all network client.
 */
@ApiStatus.Internal
public class PlayerSocketConnection extends PlayerConnection {
    private static final Set<Class<? extends ClientPacket>> IMMEDIATE_PROCESS_PACKETS = Set.of(
            ClientHandshakePacket.class, // First received packet
            ClientCookieResponsePacket.class,
            StatusRequestPacket.class,
            ClientPingRequestPacket.class,
            ClientKeepAlivePacket.class, // Used to calculate latency
            ClientLoginStartPacket.class,
            ClientEncryptionResponsePacket.class, // Auth request
            ClientLoginPluginResponsePacket.class,
            ClientSelectKnownPacksPacket.class, // Immediate answer to server request on config
            ClientLoginAcknowledgedPacket.class, // Handle config state
            ClientFinishConfigurationPacket.class // Enter play state
    );

    private final SocketChannel channel;
    private SocketAddress remoteAddress;

    private byte[] nonce = new byte[4];

    // Data from client packets
    private String loginUsername;
    private GameProfile gameProfile;
    private String serverAddress;
    private int serverPort;
    private int protocolVersion;

    private final ProtocolSession session = ProtocolSession.builder(MinecraftServer.process())
            .readBufferSize(ServerFlag.POOLED_BUFFER_SIZE)
            .writeBufferSize(ServerFlag.POOLED_BUFFER_SIZE)
            .build();
    private final Thread readThread, writeThread;

    private final AtomicLong sentPacketCounter = new AtomicLong();
    // Index where compression starts, linked to `sentPacketCounter`
    // Used instead of a simple boolean so we can get proper timing for serialization
    private volatile long compressionStart = Long.MAX_VALUE;

    // Write lock as the default behavior of the writing thread is to park itself
    // Requires ServerFlag.FASTER_SOCKET_WRITES to be enabled
    private final AtomicBoolean writeSignaled = new AtomicBoolean(false);

    private final ListenerHandle<PlayerPacketOutEvent> outgoing = EventDispatcher.getHandle(PlayerPacketOutEvent.class);

    public PlayerSocketConnection(SocketChannel channel, SocketAddress remoteAddress, Thread readThread, Thread writeThread) {
        super();
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        this.writeThread = writeThread;
        this.readThread = readThread;
    }

    public void read(PacketParser<ClientPacket> packetParser) throws IOException {
        session.readFrom(channel);
        processPackets(packetParser);
    }

    private boolean compression() {
        return compressionStart != Long.MAX_VALUE;
    }

    private void processPackets(PacketParser<ClientPacket> packetParser) {
        final PacketReading.Result<ClientPacket> result;
        try {
            session.compressionThreshold(compression() ? MinecraftServer.getCompressionThreshold() : 0);
            result = session.readPackets(packetParser);
        } catch (DataFormatException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            disconnect();
            return;
        }
        if (!(result instanceof PacketReading.Result.Success<ClientPacket> success)) return;
        for (PacketReading.ParsedPacket<ClientPacket> parsedPacket : success.packets()) {
            final ClientPacket packet = parsedPacket.packet();

            try {
                final boolean processImmediately = IMMEDIATE_PROCESS_PACKETS.contains(packet.getClass());
                if (processImmediately) {
                    // Interpret the packet using the connection state we received it.
                    MinecraftServer.getPacketListenerManager().processClientPacket(packet, this);
                } else {
                    // To be processed during the next player tick
                    final Player player = getPlayer();
                    assert player != null;
                    player.addPacketToQueue(packet);
                }
            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }
    }

    /**
     * Sets the encryption key and add the codecs to the pipeline.
     *
     * @param secretKey the secret key to use in the encryption
     * @throws IllegalStateException if encryption is already enabled for this connection
     */
    public void setEncryptionKey(SecretKey secretKey) {
        Check.stateCondition(session.encryptionEnabled(), "Encryption is already enabled!");
        session.encryption(
                MojangCrypt.getCipher(1, secretKey),
                MojangCrypt.getCipher(2, secretKey)
        );
    }

    /**
     * Enables compression and add a new codec to the pipeline.
     *
     * @throws IllegalStateException if encryption is already enabled for this connection
     */
    public void startCompression() {
        Check.stateCondition(compression(), "Compression is already enabled!");
        this.compressionStart = sentPacketCounter.get();
        final int threshold = MinecraftServer.getCompressionThreshold();
        Check.stateCondition(threshold == 0, "Compression cannot be enabled because the threshold is equal to 0");
        sendPacket(new SetCompressionPacket(threshold));
    }

    @Override
    public void sendPacket(SendablePacket packet) {
        this.session.send(packet);
        unlockWriteThread();
    }

    @Override
    public void sendPackets(Collection<SendablePacket> packets) {
        this.session.sendAll(packets);
        unlockWriteThread();
    }

    // Requires ServerFlag.FASTER_SOCKET_WRITES
    private void unlockWriteThread() {
        if (!ServerFlag.FASTER_SOCKET_WRITES) return;
        if (!this.writeSignaled.compareAndExchange(false, true)) {
            LockSupport.unpark(writeThread);
        }
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Changes the internal remote address field.
     * <p>
     * Mostly unsafe, used internally when interacting with a proxy.
     *
     * @param remoteAddress the new connection remote address
     */
    @ApiStatus.Internal
    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void setClientState(ConnectionState clientState) {
        super.setClientState(clientState);
        session.clientState(clientState);
    }

    @Override
    public void setServerState(ConnectionState serverState) {
        super.setServerState(serverState);
        session.serverState(serverState);
    }

    public @Nullable GameProfile gameProfile() {
        return gameProfile;
    }

    public void UNSAFE_setProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    /**
     * Retrieves the username received from the client during connection.
     * <p>
     * This value has not been checked and could be anything.
     *
     * @return the username given by the client, unchecked
     */
    public @Nullable String getLoginUsername() {
        return loginUsername;
    }

    /**
     * Sets the internal login username field.
     *
     * @param loginUsername the new login username field
     */
    public void UNSAFE_setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    /**
     * Gets the server address that the client used to connect.
     * <p>
     * WARNING: it is given by the client, it is possible for it to be wrong.
     *
     * @return the server address used
     */
    @Override
    public @Nullable String getServerAddress() {
        return serverAddress;
    }

    /**
     * Gets the server port that the client used to connect.
     * <p>
     * WARNING: it is given by the client, it is possible for it to be wrong.
     *
     * @return the server port used
     */
    @Override
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Gets the protocol version of a client.
     *
     * @return protocol version of client.
     */
    @Override
    public int getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Used in {@link ClientHandshakePacket} to change the internal fields.
     *
     * @param serverAddress   the server address which the client used
     * @param serverPort      the server port which the client used
     * @param protocolVersion the protocol version which the client used
     */
    public void refreshServerInformation(@Nullable String serverAddress, int serverPort, int protocolVersion) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.protocolVersion = protocolVersion;
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    private @Nullable SendablePacket preparePacket(ConnectionState state, SendablePacket packet) {
        final boolean compressed = sentPacketCounter.get() > compressionStart;
        session.compressionThreshold(compressed ? MinecraftServer.getCompressionThreshold() : 0);
        final Player player = getPlayer();
        if (player != null) {
            // Outgoing event
            if (outgoing.hasListener()) {
                final ServerPacket serverPacket = SendablePacket.extractServerPacket(state, packet);
                if (serverPacket != null) { // Events are not called for buffered packets
                    PlayerPacketOutEvent event = new PlayerPacketOutEvent(player, serverPacket);
                    outgoing.call(event);
                    if (event.isCancelled()) return null;
                }
            }
            // Translation
            if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && packet instanceof ServerPacket.ComponentHolding) {
                packet = ((ServerPacket.ComponentHolding) packet).copyWithOperator(component ->
                        MinestomAdventure.COMPONENT_TRANSLATOR.apply(component, Objects.requireNonNullElseGet(player.getLocale(), MinestomAdventure::getDefaultLocale)));
            }
        }
        return packet;
    }

    public void flushSync() throws IOException {
        // Consume queued packets
        if (!session.hasPendingWrite()) {
            if (!ServerFlag.FASTER_SOCKET_WRITES) {
                try {
                    // Can probably be improved by waking up at the end of the tick
                    // But this work well enough and without additional state.
                    Thread.sleep(1000 / ServerFlag.SERVER_TICKS_PER_SECOND / 2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                assert this.writeThread == Thread.currentThread() : "writeThread should be the current thread";
                this.writeSignaled.set(false);
                if (!isOnline()) return; // already offline, don't park
                LockSupport.park(this);
                if (!session.hasPendingWrite()) return; // woken by disconnect signal, not by packets
            }
        }
        if (!channel.isConnected()) throw new EOFException("Channel is closed");
        session.flushTo(channel, this::preparePacket, sentPacketCounter::getAndIncrement);
        final ConnectionState serverState = session.serverState();
        if (serverState != getServerState()) setServerState(serverState);
    }

    @Override
    public void disconnect() {
        super.disconnect();
        LockSupport.unpark(writeThread);
    }

    public Thread readThread() {
        return readThread;
    }

    public Thread writeThread() {
        return writeThread;
    }
}
