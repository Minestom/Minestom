package net.minestom.server.network.player;

import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.ListenerHandle;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.packet.server.*;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.network.socket.Worker;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.PooledBuffers;
import net.minestom.server.utils.validate.Check;
import org.jctools.queues.MessagePassingQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.DataFormatException;

/**
 * Represents a socket connection.
 * <p>
 * It is the implementation used for all network client.
 */
@ApiStatus.Internal
public class PlayerSocketConnection extends PlayerConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(PlayerSocketConnection.class);

    private final Worker worker;
    private final MessagePassingQueue<Runnable> workerQueue;
    private final SocketChannel channel;
    private SocketAddress remoteAddress;

    private volatile boolean encrypted = false;
    private volatile boolean compressed = false;

    //Could be null. Only used for Mojang Auth
    private byte[] nonce = new byte[4];
    private Cipher decryptCipher;
    private Cipher encryptCipher;

    // Data from client packets
    private String loginUsername;
    private String serverAddress;
    private int serverPort;
    private int protocolVersion;

    // Used for the login plugin request packet, to retrieve the channel from a message id,
    // cleared once the player enters the play state
    private final Map<Integer, String> pluginRequestMap = new ConcurrentHashMap<>();

    // Bungee
    private UUID bungeeUuid;
    private PlayerSkin bungeeSkin;

    private final List<BinaryBuffer> waitingBuffers = new ArrayList<>();
    private final AtomicReference<BinaryBuffer> tickBuffer = new AtomicReference<>(PooledBuffers.get());
    private volatile BinaryBuffer cacheBuffer;

    private final ListenerHandle<PlayerPacketOutEvent> outgoing = MinecraftServer.getGlobalEventHandler().getHandle(PlayerPacketOutEvent.class);

    public PlayerSocketConnection(@NotNull Worker worker, @NotNull SocketChannel channel, SocketAddress remoteAddress) {
        super();
        this.worker = worker;
        this.workerQueue = worker.queue();
        this.channel = channel;
        this.remoteAddress = remoteAddress;
        PooledBuffers.registerBuffer(this, tickBuffer);
        PooledBuffers.registerBuffers(this, waitingBuffers);
    }

    public void processPackets(BinaryBuffer readBuffer, PacketProcessor packetProcessor) {
        // Decrypt data
        if (encrypted) {
            final Cipher cipher = decryptCipher;
            ByteBuffer input = readBuffer.asByteBuffer(0, readBuffer.writerOffset());
            try {
                cipher.update(input, input.duplicate());
            } catch (ShortBufferException e) {
                MinecraftServer.getExceptionManager().handleException(e);
                return;
            }
        }
        // Read all packets
        try {
            this.cacheBuffer = PacketUtils.readPackets(readBuffer, compressed,
                    (id, payload) -> {
                        try {
                            packetProcessor.process(this, id, payload);
                        } catch (Exception e) {
                            // Error while reading the packet
                            MinecraftServer.getExceptionManager().handleException(e);
                        } finally {
                            if (payload.position() != payload.limit()) {
                                LOGGER.warn("WARNING: Packet 0x{} not fully read ({})", Integer.toHexString(id), payload);
                            }
                        }
                    });
        } catch (DataFormatException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            disconnect();
        }
    }

    public void consumeCache(BinaryBuffer buffer) {
        if (cacheBuffer != null) {
            buffer.write(cacheBuffer);
            this.cacheBuffer = null;
        }
    }

    /**
     * Sets the encryption key and add the codecs to the pipeline.
     *
     * @param secretKey the secret key to use in the encryption
     * @throws IllegalStateException if encryption is already enabled for this connection
     */
    public void setEncryptionKey(@NotNull SecretKey secretKey) {
        Check.stateCondition(encrypted, "Encryption is already enabled!");
        this.decryptCipher = MojangCrypt.getCipher(2, secretKey);
        this.encryptCipher = MojangCrypt.getCipher(1, secretKey);
        this.encrypted = true;
    }

    /**
     * Enables compression and add a new codec to the pipeline.
     *
     * @throws IllegalStateException if encryption is already enabled for this connection
     */
    public void startCompression() {
        Check.stateCondition(compressed, "Compression is already enabled!");
        final int threshold = MinecraftServer.getCompressionThreshold();
        Check.stateCondition(threshold == 0, "Compression cannot be enabled because the threshold is equal to 0");
        writeAndFlush(new SetCompressionPacket(threshold));
        this.compressed = true;
    }

    @Override
    public void sendPacket(@NotNull SendablePacket packet) {
        final boolean compressed = this.compressed;
        this.workerQueue.relaxedOffer(() -> writePacketSync(packet, compressed));
    }

    @Override
    public void sendPackets(@NotNull Collection<SendablePacket> packets) {
        final List<SendablePacket> packetsCopy = List.copyOf(packets);
        final boolean compressed = this.compressed;
        this.workerQueue.relaxedOffer(() -> {
            for (SendablePacket packet : packetsCopy) writePacketSync(packet, compressed);
        });
    }

    @ApiStatus.Internal
    public void write(@NotNull ByteBuffer buffer, int index, int length) {
        this.workerQueue.relaxedOffer(() -> writeBufferSync(buffer, index, length));
    }

    @ApiStatus.Internal
    public void write(@NotNull ByteBuffer buffer) {
        write(buffer, buffer.position(), buffer.remaining());
    }

    public void writeAndFlush(@NotNull ServerPacket packet) {
        final boolean compressed = this.compressed;
        this.workerQueue.relaxedOffer(() -> {
            writeServerPacketSync(packet, compressed);
            flushSync();
        });
    }

    @Override
    public void flush() {
        this.workerQueue.relaxedOffer(this::flushSync);
    }

    @Override
    public @NotNull SocketAddress getRemoteAddress() {
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
    public void setRemoteAddress(@NotNull SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void disconnect() {
        this.workerQueue.relaxedOffer(() -> this.worker.disconnect(this, channel));
    }

    public @NotNull SocketChannel getChannel() {
        return channel;
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
    public void UNSAFE_setLoginUsername(@NotNull String loginUsername) {
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
     * Used in {@link net.minestom.server.network.packet.client.handshake.HandshakePacket} to change the internal fields.
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

    public @Nullable UUID getBungeeUuid() {
        return bungeeUuid;
    }

    public void UNSAFE_setBungeeUuid(UUID bungeeUuid) {
        this.bungeeUuid = bungeeUuid;
    }

    public @Nullable PlayerSkin getBungeeSkin() {
        return bungeeSkin;
    }

    public void UNSAFE_setBungeeSkin(PlayerSkin bungeeSkin) {
        this.bungeeSkin = bungeeSkin;
    }

    /**
     * Adds an entry to the plugin request map.
     * <p>
     * Only working if {@link #getConnectionState()} is {@link net.minestom.server.network.ConnectionState#LOGIN}.
     *
     * @param messageId the message id
     * @param channel   the packet channel
     * @throws IllegalStateException if a messageId with the value {@code messageId} already exists for this connection
     */
    public void addPluginRequestEntry(int messageId, @NotNull String channel) {
        if (!getConnectionState().equals(ConnectionState.LOGIN)) {
            return;
        }
        Check.stateCondition(pluginRequestMap.containsKey(messageId), "You cannot have two messageId with the same value");
        this.pluginRequestMap.put(messageId, channel);
    }

    /**
     * Gets a request channel from a message id, previously cached using {@link #addPluginRequestEntry(int, String)}.
     * <p>
     * Be aware that the internal map is cleared once the player enters the play state.
     *
     * @param messageId the message id
     * @return the channel linked to the message id, null if not found
     */
    public @Nullable String getPluginRequestChannel(int messageId) {
        return pluginRequestMap.get(messageId);
    }

    @Override
    public void setConnectionState(@NotNull ConnectionState connectionState) {
        super.setConnectionState(connectionState);
        // Clear the plugin request map (since it is not used anymore)
        if (connectionState.equals(ConnectionState.PLAY)) {
            this.pluginRequestMap.clear();
        }
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    private void writePacketSync(SendablePacket packet, boolean compressed) {
        if (!channel.isConnected()) return;
        final Player player = getPlayer();
        // Outgoing event
        if (player != null && outgoing.hasListener()) {
            final ServerPacket serverPacket = SendablePacket.extractServerPacket(packet);
            outgoing.call(new PlayerPacketOutEvent(player, serverPacket));
        }
        // Write packet
        if (packet instanceof ServerPacket serverPacket) {
            writeServerPacketSync(serverPacket, compressed);
        } else if (packet instanceof FramedPacket framedPacket) {
            var buffer = framedPacket.body();
            writeBufferSync0(buffer, 0, buffer.limit());
        } else if (packet instanceof CachedPacket cachedPacket) {
            var buffer = cachedPacket.body();
            writeBufferSync0(buffer, buffer.position(), buffer.remaining());
        } else if (packet instanceof LazyPacket lazyPacket) {
            writeServerPacketSync(lazyPacket.packet(), compressed);
        } else {
            throw new RuntimeException("Unknown packet type: " + packet.getClass().getName());
        }
    }

    private void writeServerPacketSync(ServerPacket serverPacket, boolean compressed) {
        final Player player = getPlayer();
        if (player != null) {
            if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && serverPacket instanceof ComponentHoldingServerPacket) {
                serverPacket = ((ComponentHoldingServerPacket) serverPacket).copyWithOperator(component ->
                        GlobalTranslator.render(component, Objects.requireNonNullElseGet(player.getLocale(), MinestomAdventure::getDefaultLocale)));
            }
        }
        var buffer = PacketUtils.createFramedPacket(serverPacket, compressed);
        writeBufferSync0(buffer, 0, buffer.limit());
        if (player == null) flushSync(); // Player is probably not logged yet
    }

    private void writeBufferSync(@NotNull ByteBuffer buffer, int index, int length) {
        // TODO read buffer for outgoing event
        writeBufferSync0(buffer, index, length);
    }

    private void writeBufferSync0(@NotNull ByteBuffer buffer, int index, int length) {
        if (encrypted) { // Encryption support
            ByteBuffer output = PooledBuffers.tempBuffer();
            try {
                this.encryptCipher.update(buffer.slice(index, length), output);
                buffer = output.flip();
                index = 0;
            } catch (ShortBufferException e) {
                MinecraftServer.getExceptionManager().handleException(e);
                return;
            }
        }

        BinaryBuffer localBuffer = tickBuffer.getPlain();
        final int capacity = localBuffer.capacity();
        if (length <= capacity) {
            if (!localBuffer.canWrite(length)) localBuffer = updateLocalBuffer();
            localBuffer.write(buffer, index, length);
        } else {
            final int bufferCount = length / capacity + 1;
            for (int i = 0; i < bufferCount; i++) {
                final int sliceStart = i * capacity;
                final int sliceLength = Math.min(length, sliceStart + capacity) - sliceStart;
                if (!localBuffer.canWrite(sliceLength)) localBuffer = updateLocalBuffer();
                localBuffer.write(buffer, sliceStart, sliceLength);
            }
        }
    }

    public void flushSync() {
        try {
            if (!channel.isConnected()) throw new ClosedChannelException();
            try {
                if (waitingBuffers.isEmpty() && tickBuffer.getPlain().writeChannel(channel))
                    return; // Fast exit if the tick buffer can be reused

                try {
                    updateLocalBuffer();
                } catch (OutOfMemoryError e) {
                    this.waitingBuffers.clear();
                    System.gc(); // Explicit gc forcing buffers to be collected
                    throw new ClosedChannelException();
                }

                // Write as much as possible from the waiting list
                Iterator<BinaryBuffer> iterator = waitingBuffers.iterator();
                while (iterator.hasNext()) {
                    BinaryBuffer waitingBuffer = iterator.next();
                    if (!waitingBuffer.writeChannel(channel)) break;
                    iterator.remove();
                    PooledBuffers.add(waitingBuffer);
                }
            } catch (IOException e) { // Couldn't write to the socket
                MinecraftServer.getExceptionManager().handleException(e);
                throw new ClosedChannelException();
            }
        } catch (ClosedChannelException e) {
            disconnect();
        }
    }

    private BinaryBuffer updateLocalBuffer() {
        BinaryBuffer newBuffer = PooledBuffers.get();
        this.waitingBuffers.add(tickBuffer.getPlain());
        this.tickBuffer.setPlain(newBuffer);
        return newBuffer;
    }
}
