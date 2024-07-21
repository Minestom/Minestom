package net.minestom.server.network.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.ListenerHandle;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.server.*;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.network.socket.Worker;
import net.minestom.server.utils.ObjectPool;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.binary.BinaryBuffer;
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
    private static final ObjectPool<BinaryBuffer> POOL = ObjectPool.BUFFER_POOL;

    private final Worker worker;
    private final MessagePassingQueue<Runnable> workerQueue;
    private final SocketChannel channel;
    private SocketAddress remoteAddress;

    private volatile boolean compressed = false;

    //Could be null. Only used for Mojang Auth
    private volatile EncryptionContext encryptionContext;
    private byte[] nonce = new byte[4];

    // Data from client packets
    private String loginUsername;
    private GameProfile gameProfile;
    private String serverAddress;
    private int serverPort;
    private int protocolVersion;

    private final List<BinaryBuffer> waitingBuffers = new ArrayList<>();
    private final AtomicReference<BinaryBuffer> tickBuffer = new AtomicReference<>(POOL.get());
    private BinaryBuffer cacheBuffer;

    private final ListenerHandle<PlayerPacketOutEvent> outgoing = EventDispatcher.getHandle(PlayerPacketOutEvent.class);

    public PlayerSocketConnection(@NotNull Worker worker, @NotNull SocketChannel channel, SocketAddress remoteAddress) {
        super();
        this.worker = worker;
        this.workerQueue = worker.queue();
        this.channel = channel;
        this.remoteAddress = remoteAddress;
    }

    public void processPackets(BinaryBuffer readBuffer, PacketParser.Client packetParser) {
        // Decrypt data
        {
            final EncryptionContext encryptionContext = this.encryptionContext;
            if (encryptionContext != null) {
                ByteBuffer input = readBuffer.asByteBuffer(0, readBuffer.writerOffset());
                try {
                    encryptionContext.decrypt().update(input, input.duplicate());
                } catch (ShortBufferException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                    return;
                }
            }
        }
        // Read all packets
        try {
            this.cacheBuffer = PacketUtils.readPackets(readBuffer, compressed,
                    (id, payload) -> {
                        if (!isOnline())
                            return; // Prevent packet corruption
                        ClientPacket packet = null;
                        try {
                            NetworkBuffer networkBuffer = new NetworkBuffer(payload);
                            packet = packetParser.parse(getConnectionState(), id, networkBuffer);
                            payload.position(networkBuffer.readIndex());
                            // Process the packet
                            if (packet.processImmediately()) {
                                MinecraftServer.getPacketListenerManager().processClientPacket(packet, this);
                            } else {
                                // To be processed during the next player tick
                                final Player player = getPlayer();
                                assert player != null;
                                player.addPacketToQueue(packet);
                            }
                        } catch (Exception e) {
                            // Error while reading the packet
                            MinecraftServer.getExceptionManager().handleException(e);
                        } finally {
                            if (payload.position() != payload.limit()) {
                                LOGGER.warn("WARNING: Packet ({}) 0x{} not fully read ({}) {}", getConnectionState(), Integer.toHexString(id), payload, packet);
                            }
                        }
                    });
        } catch (DataFormatException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            disconnect();
        }
    }

    public void consumeCache(BinaryBuffer buffer) {
        final BinaryBuffer cache = this.cacheBuffer;
        if (cache != null) {
            buffer.write(cache);
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
        Check.stateCondition(encryptionContext != null, "Encryption is already enabled!");
        this.encryptionContext = new EncryptionContext(MojangCrypt.getCipher(1, secretKey), MojangCrypt.getCipher(2, secretKey));
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
        sendPacket(new SetCompressionPacket(threshold));
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
        super.disconnect();
        this.workerQueue.relaxedOffer(() -> {
            this.worker.disconnect(this, channel);
            final BinaryBuffer tick = tickBuffer.getAndSet(null);
            if (tick != null) POOL.add(tick);
            for (BinaryBuffer buffer : waitingBuffers) POOL.add(buffer);
            this.waitingBuffers.clear();
        });
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

    private void writePacketSync(SendablePacket packet, boolean compressed) {
        if (!channel.isConnected()) return;
        final Player player = getPlayer();
        // Outgoing event
        if (player != null && outgoing.hasListener()) {
            final ServerPacket serverPacket = SendablePacket.extractServerPacket(getConnectionState(), packet);
            PlayerPacketOutEvent event = new PlayerPacketOutEvent(player, serverPacket);
            outgoing.call(event);
            if (event.isCancelled()) return;
        }
        // Write packet
        if (packet instanceof ServerPacket serverPacket) {
            writeServerPacketSync(serverPacket, compressed);
        } else if (packet instanceof FramedPacket framedPacket) {
            var buffer = framedPacket.body();
            writeBufferSync(buffer, 0, buffer.limit());
        } else if (packet instanceof CachedPacket cachedPacket) {
            var buffer = cachedPacket.body(getConnectionState());
            if (buffer != null) writeBufferSync(buffer, buffer.position(), buffer.remaining());
            else writeServerPacketSync(cachedPacket.packet(getConnectionState()), compressed);
        } else if (packet instanceof LazyPacket lazyPacket) {
            writeServerPacketSync(lazyPacket.packet(), compressed);
        } else {
            throw new RuntimeException("Unknown packet type: " + packet.getClass().getName());
        }
    }

    private void writeServerPacketSync(ServerPacket serverPacket, boolean compressed) {
        final Player player = getPlayer();
        if (player != null) {
            if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && serverPacket instanceof ServerPacket.ComponentHolding) {
                serverPacket = ((ServerPacket.ComponentHolding) serverPacket).copyWithOperator(component ->
                        MinestomAdventure.COMPONENT_TRANSLATOR.apply(component, Objects.requireNonNullElseGet(player.getLocale(), MinestomAdventure::getDefaultLocale)));
            }
        }
        try (var hold = ObjectPool.PACKET_POOL.hold()) {
            var buffer = PacketUtils.createFramedPacket(getConnectionState(), hold.get(), serverPacket, compressed);
            writeBufferSync(buffer, 0, buffer.limit());
        }
    }

    private void writeBufferSync(@NotNull ByteBuffer buffer, int index, int length) {
        // Encrypt data
        final EncryptionContext encryptionContext = this.encryptionContext;
        if (encryptionContext != null) { // Encryption support
            try (var hold = ObjectPool.PACKET_POOL.hold()) {
                ByteBuffer output = hold.get();
                try {
                    length = encryptionContext.encrypt().update(buffer.slice(index, length), output);
                    writeBufferSync0(output, 0, length);
                } catch (ShortBufferException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }
                return;
            }
        }
        writeBufferSync0(buffer, index, length);
    }

    private void writeBufferSync0(@NotNull ByteBuffer buffer, int index, int length) {
        BinaryBuffer localBuffer = tickBuffer.getPlain();
        if (localBuffer == null)
            return; // Socket is closed
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

    public void flushSync() throws IOException {
        final SocketChannel channel = this.channel;
        final List<BinaryBuffer> waitingBuffers = this.waitingBuffers;
        if (!channel.isConnected()) throw new ClosedChannelException();
        if (waitingBuffers.isEmpty()) {
            BinaryBuffer localBuffer = tickBuffer.getPlain();
            if (localBuffer == null)
                return; // Socket is closed
            localBuffer.writeChannel(channel);
        } else {
            // Write as much as possible from the waiting list
            Iterator<BinaryBuffer> iterator = waitingBuffers.iterator();
            while (iterator.hasNext()) {
                BinaryBuffer waitingBuffer = iterator.next();
                if (!waitingBuffer.writeChannel(channel)) break;
                iterator.remove();
                POOL.add(waitingBuffer);
            }
        }
    }

    private BinaryBuffer updateLocalBuffer() {
        BinaryBuffer newBuffer = POOL.get();
        this.waitingBuffers.add(tickBuffer.getPlain());
        this.tickBuffer.setPlain(newBuffer);
        return newBuffer;
    }

    record EncryptionContext(Cipher encrypt, Cipher decrypt) {
    }
}
