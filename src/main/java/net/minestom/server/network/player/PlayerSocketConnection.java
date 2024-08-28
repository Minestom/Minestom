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
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.PacketReading;
import net.minestom.server.network.packet.PacketVanilla;
import net.minestom.server.network.packet.PacketWriting;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientCookieResponsePacket;
import net.minestom.server.network.packet.client.common.ClientKeepAlivePacket;
import net.minestom.server.network.packet.client.common.ClientPingRequestPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.*;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.utils.validate.Check;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;

/**
 * Represents a socket connection.
 * <p>
 * It is the implementation used for all network client.
 */
@ApiStatus.Internal
public class PlayerSocketConnection extends PlayerConnection {
    private static final Set<Class<? extends ClientPacket>> IMMEDIATE_PROCESS_PACKETS = Set.of(
            ClientCookieResponsePacket.class,
            StatusRequestPacket.class,
            ClientLoginStartPacket.class,
            ClientPingRequestPacket.class,
            ClientKeepAlivePacket.class,
            ClientEncryptionResponsePacket.class,
            ClientHandshakePacket.class,
            ClientLoginPluginResponsePacket.class,
            ClientLoginAcknowledgedPacket.class
    );

    private final SocketChannel channel;
    private SocketAddress remoteAddress;

    //Could be null. Only used for Mojang Auth
    private volatile EncryptionContext encryptionContext;
    private byte[] nonce = new byte[4];

    // Data from client packets
    private String loginUsername;
    private GameProfile gameProfile;
    private String serverAddress;
    private int serverPort;
    private int protocolVersion;

    private final NetworkBuffer readBuffer = NetworkBuffer.resizableBuffer(ServerFlag.POOLED_BUFFER_SIZE, MinecraftServer.process());
    private final MpscUnboundedXaddArrayQueue<SendablePacket> packetQueue = new MpscUnboundedXaddArrayQueue<>(1024);

    private final AtomicLong sentPacketCounter = new AtomicLong();
    // Index where compression starts, linked to `sentPacketCounter`
    // Used instead of a simple boolean so we can get proper timing for serialization
    private volatile long compressionStart = Long.MAX_VALUE;

    final ReentrantLock writeLock = new ReentrantLock();
    final Condition writeCondition = writeLock.newCondition();

    private final ListenerHandle<PlayerPacketOutEvent> outgoing = EventDispatcher.getHandle(PlayerPacketOutEvent.class);

    public PlayerSocketConnection(@NotNull SocketChannel channel, SocketAddress remoteAddress) {
        super();
        this.channel = channel;
        this.remoteAddress = remoteAddress;
    }

    public void read(PacketParser<ClientPacket> packetParser) throws IOException {
        NetworkBuffer readBuffer = this.readBuffer;
        final long writeIndex = readBuffer.writeIndex();
        final int length = readBuffer.readChannel(channel);
        // Decrypt newly read data
        final EncryptionContext encryptionContext = this.encryptionContext;
        if (encryptionContext != null) {
            readBuffer.cipher(encryptionContext.decrypt(), writeIndex, length);
        }
        // Process packets
        processPackets(readBuffer, packetParser);
    }

    private boolean compression() {
        return compressionStart != Long.MAX_VALUE;
    }

    private void processPackets(NetworkBuffer readBuffer, PacketParser<ClientPacket> packetParser) {
        // Read all packets
        final PacketReading.Result<ClientPacket> result;
        try {
            result = PacketReading.readPackets(
                    readBuffer,
                    packetParser,
                    getConnectionState(), PacketVanilla::nextClientState,
                    compression()
            );
        } catch (DataFormatException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            disconnect();
            return;
        }
        switch (result) {
            case PacketReading.Result.Success<ClientPacket> success -> {
                for (ClientPacket packet : success.packets()) {
                    try {
                        final boolean processImmediately = IMMEDIATE_PROCESS_PACKETS.contains(packet.getClass());
                        if (processImmediately) {
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
                // Compact in case of incomplete read
                readBuffer.compact();
            }
            case PacketReading.Result.Empty<ClientPacket> ignored -> {
                // Empty
            }
            case PacketReading.Result.Failure<ClientPacket> failure -> {
                // Resize for next read
                final long requiredCapacity = failure.requiredCapacity();
                assert requiredCapacity > readBuffer.capacity() :
                        "New capacity should be greater than the current one: " + requiredCapacity + " <= " + readBuffer.capacity();
                readBuffer.resize(requiredCapacity);
            }
        }
    }

    /**
     * Sets the encryption key and add the codecs to the pipeline.
     *
     * @param secretKey the secret key to use in the encryption
     * @throws IllegalStateException if encryption is already enabled for this connection
     */
    public void setEncryptionKey(@NotNull SecretKey secretKey) {
        Check.stateCondition(encryptionContext != null, "Encryption is already enabled!");
        this.encryptionContext = new EncryptionContext(MojangCrypt.getCipher(1, secretKey), MojangCrypt.getCipher(2, secretKey));
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
    public void sendPacket(@NotNull SendablePacket packet) {
        this.packetQueue.relaxedOffer(packet);
        signalWrite();
    }

    @Override
    public void sendPackets(@NotNull Collection<SendablePacket> packets) {
        for (SendablePacket packet : packets) this.packetQueue.relaxedOffer(packet);
        signalWrite();
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

    public @NotNull SocketChannel getChannel() {
        return channel;
    }

    public @Nullable GameProfile gameProfile() {
        return gameProfile;
    }

    public void UNSAFE_setProfile(@NotNull GameProfile gameProfile) {
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

    private boolean writeSendable(NetworkBuffer buffer, SendablePacket sendable, boolean compressed) {
        final long start = buffer.writeIndex();
        final boolean result = writePacketSync(buffer, sendable, compressed);
        if (!result) return false;
        // Encrypt data
        final long length = buffer.writeIndex() - start;
        final EncryptionContext encryptionContext = this.encryptionContext;
        if (encryptionContext != null && length > 0) { // Encryption support
            buffer.cipher(encryptionContext.encrypt(), start, length);
        }
        return true;
    }

    private boolean writePacketSync(NetworkBuffer buffer, SendablePacket packet, boolean compressed) {
        final Player player = getPlayer();
        final ConnectionState state = getConnectionState();
        if (player != null) {
            // Outgoing event
            if (outgoing.hasListener()) {
                final ServerPacket serverPacket = SendablePacket.extractServerPacket(state, packet);
                if (serverPacket != null) { // Events are not called for buffered packets
                    PlayerPacketOutEvent event = new PlayerPacketOutEvent(player, serverPacket);
                    outgoing.call(event);
                    if (event.isCancelled()) return true;
                }
            }
            // Translation
            if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && packet instanceof ServerPacket.ComponentHolding) {
                packet = ((ServerPacket.ComponentHolding) packet).copyWithOperator(component ->
                        MinestomAdventure.COMPONENT_TRANSLATOR.apply(component, Objects.requireNonNullElseGet(player.getLocale(), MinestomAdventure::getDefaultLocale)));
            }
        }
        // Write packet
        final long start = buffer.writeIndex();
        final int compressionThreshold = compressed ? MinecraftServer.getCompressionThreshold() : 0;
        try {
            return switch (packet) {
                case ServerPacket serverPacket -> {
                    PacketWriting.writeFramedPacket(buffer, state, serverPacket, compressionThreshold);
                    yield true;
                }
                case FramedPacket framedPacket -> {
                    final NetworkBuffer body = framedPacket.body();
                    yield writeBuffer(buffer, body, 0, body.capacity());
                }
                case CachedPacket cachedPacket -> {
                    final NetworkBuffer body = cachedPacket.body(state);
                    if (body != null) {
                        yield writeBuffer(buffer, body, 0, body.capacity());
                    } else {
                        PacketWriting.writeFramedPacket(buffer, state, cachedPacket.packet(state), compressionThreshold);
                        yield true;
                    }
                }
                case LazyPacket lazyPacket -> {
                    PacketWriting.writeFramedPacket(buffer, state, lazyPacket.packet(), compressionThreshold);
                    yield true;
                }
                case BufferedPacket bufferedPacket -> {
                    final NetworkBuffer rawBuffer = bufferedPacket.buffer();
                    final long index = bufferedPacket.index();
                    final long length = bufferedPacket.length();
                    yield writeBuffer(buffer, rawBuffer, index, length);
                }
            };
        } catch (IndexOutOfBoundsException exception) {
            buffer.writeIndex(start);
            return false;
        }
    }

    private boolean writeBuffer(NetworkBuffer buffer, NetworkBuffer body, long index, long length) {
        if (buffer.writableBytes() < length) {
            // Not enough space in the buffer
            return false;
        }
        NetworkBuffer.copy(body, index, buffer, buffer.writeIndex(), length);
        buffer.advanceWrite(length);
        return true;
    }

    private NetworkBuffer writeLeftover = null;

    public void flushSync() throws IOException {
        // Write leftover if any
        NetworkBuffer leftover = this.writeLeftover;
        if (leftover != null) {
            final boolean success = leftover.writeChannel(channel);
            if (success) {
                this.writeLeftover = null;
                PacketVanilla.PACKET_POOL.add(leftover);
            } else {
                // Failed to write the whole leftover, try again next flush
                return;
            }
        }
        // Consume queued packets
        var packetQueue = this.packetQueue;
        if (packetQueue.isEmpty()) {
            awaitWrite();
        }
        if (!channel.isConnected()) throw new EOFException("Channel is closed");
        NetworkBuffer buffer = PacketVanilla.PACKET_POOL.get();
        // Write to buffer
        PacketWriting.writeQueue(buffer, packetQueue, 1, (b, packet) -> {
            final boolean compressed = sentPacketCounter.get() > compressionStart;
            final boolean success = writeSendable(b, packet, compressed);
            if (success) sentPacketCounter.getAndIncrement();
            return success;
        });
        // Write to channel
        final boolean success = buffer.writeChannel(channel);
        // Keep the buffer if not fully written
        if (success) PacketVanilla.PACKET_POOL.add(buffer);
        else this.writeLeftover = buffer;
    }

    public void awaitWrite() {
        try {
            this.writeLock.lock();
            //noinspection ResultOfMethodCallIgnored
            this.writeCondition.await(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            this.writeLock.unlock();
        }
    }

    public void signalWrite() {
        try {
            this.writeLock.lock();
            this.writeCondition.signal();
        } finally {
            this.writeLock.unlock();
        }
    }

    record EncryptionContext(Cipher encrypt, Cipher decrypt) {
    }
}
