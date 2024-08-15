package net.minestom.server.network.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.ListenerHandle;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.server.*;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.validate.Check;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.BufferOverflowException;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DataFormatException;

/**
 * Represents a socket connection.
 * <p>
 * It is the implementation used for all network client.
 */
@ApiStatus.Internal
public class PlayerSocketConnection extends PlayerConnection {
    private final static Logger LOGGER = LoggerFactory.getLogger(PlayerSocketConnection.class);

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

    private final NetworkBuffer readBuffer = NetworkBuffer.resizableBuffer(1024, MinecraftServer.process());
    private final MpscUnboundedXaddArrayQueue<Receivable> packetQueue = new MpscUnboundedXaddArrayQueue<>(1024);

    private final AtomicLong sentPacketCounter = new AtomicLong();
    // Index where compression starts, linked to `sentPacketCounter`
    // Used instead of a simple boolean so we can get proper timing for serialization
    private volatile long compressionStart = Long.MAX_VALUE;

    private final ListenerHandle<PlayerPacketOutEvent> outgoing = EventDispatcher.getHandle(PlayerPacketOutEvent.class);

    public PlayerSocketConnection(@NotNull SocketChannel channel, SocketAddress remoteAddress) {
        super();
        this.channel = channel;
        this.remoteAddress = remoteAddress;
    }

    public void read(PacketParser.Client packetParser) throws IOException {
        NetworkBuffer readBuffer = this.readBuffer;
        final int writeIndex = readBuffer.writeIndex();
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

    private void processPackets(NetworkBuffer readBuffer, PacketParser.Client packetParser) {
        // Read all packets
        try {
            final int missingLength = PacketUtils.readPackets(readBuffer, compression(),
                    (id, payload) -> {
                        if (!isOnline())
                            return; // Prevent packet corruption
                        ClientPacket packet;
                        try {
                            packet = packetParser.parse(getConnectionState(), id, payload);
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
                            if (payload.readableBytes() != 0) {
                                var info = packetParser.stateRegistry(getConnectionState()).packetInfo(id);
                                LOGGER.warn("WARNING: Packet ({}) 0x{} not fully read ({})", info.packetClass().getSimpleName(), Integer.toHexString(id), payload);
                            }
                        }
                    });
            if (missingLength > 0) {
                // Resize for next read
                final int newSize = readBuffer.size() + missingLength;
                readBuffer.resize(newSize);
            } else {
                // Compact in case of incomplete read
                readBuffer.compact();
            }
        } catch (DataFormatException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            disconnect();
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

    sealed interface Receivable {
        record Packet(SendablePacket packet) implements Receivable {
        }

        record Buffer(NetworkBuffer buffer, int index, int length) implements Receivable {
        }
    }

    @Override
    public void sendPacket(@NotNull SendablePacket packet) {
        offer(new Receivable.Packet(packet));
    }

    @Override
    public void sendPackets(@NotNull Collection<SendablePacket> packets) {
        for (SendablePacket packet : packets) offer(new Receivable.Packet(packet));
    }

    @ApiStatus.Internal
    public void write(@NotNull NetworkBuffer buffer, int index, int length) {
        offer(new Receivable.Buffer(buffer, index, length));
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

    private void offer(Receivable receivable) {
        this.packetQueue.relaxedOffer(receivable);
    }

    private boolean writeReceivable(NetworkBuffer buffer, Receivable receivable, boolean compressed) {
        return switch (receivable) {
            case Receivable.Buffer receivableBuffer -> {
                final NetworkBuffer rawBuffer = receivableBuffer.buffer();
                final int index = receivableBuffer.index();
                final int length = receivableBuffer.length();
                if (buffer.size() - buffer.writeIndex() < length) {
                    // Not enough space in the buffer
                    yield false;
                }
                NetworkBuffer.copy(rawBuffer, index, buffer, buffer.writeIndex(), length);
                buffer.advanceWrite(length);
                yield true;
            }
            case Receivable.Packet packet -> writePacketSync(buffer, packet.packet(), compressed);
        };
    }

    private boolean writePacketSync(NetworkBuffer buffer, SendablePacket packet, boolean compressed) {
        final Player player = getPlayer();
        final ConnectionState state = getConnectionState();
        if (player != null) {
            // Outgoing event
            if (outgoing.hasListener()) {
                final ServerPacket serverPacket = SendablePacket.extractServerPacket(state, packet);
                PlayerPacketOutEvent event = new PlayerPacketOutEvent(player, serverPacket);
                outgoing.call(event);
                if (event.isCancelled()) return true;
            }
            // Translation
            if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && packet instanceof ServerPacket.ComponentHolding) {
                packet = ((ServerPacket.ComponentHolding) packet).copyWithOperator(component ->
                        MinestomAdventure.COMPONENT_TRANSLATOR.apply(component, Objects.requireNonNullElseGet(player.getLocale(), MinestomAdventure::getDefaultLocale)));
            }
        }
        // Write packet
        final int start = buffer.writeIndex();
        try {
            switch (packet) {
                case ServerPacket serverPacket ->
                        PacketUtils.writeFramedPacket(state, buffer, serverPacket, compressed);
                case FramedPacket framedPacket -> {
                    final NetworkBuffer body = framedPacket.body();
                    final int length = body.size();
                    NetworkBuffer.copy(body, 0, buffer, start, length);
                    buffer.advanceWrite(length);
                }
                case CachedPacket cachedPacket -> {
                    final NetworkBuffer body = cachedPacket.body(state);
                    if (body != null) {
                        final int length = body.size();
                        NetworkBuffer.copy(body, 0, buffer, start, length);
                        buffer.advanceWrite(length);
                    } else PacketUtils.writeFramedPacket(state, buffer, cachedPacket.packet(state), compressed);
                }
                case LazyPacket lazyPacket ->
                        PacketUtils.writeFramedPacket(state, buffer, lazyPacket.packet(), compressed);
            }
            final int end = buffer.writeIndex();
            // Encrypt data
            final EncryptionContext encryptionContext = this.encryptionContext;
            if (encryptionContext != null) { // Encryption support
                buffer.cipher(encryptionContext.encrypt(), start, end - start);
            }
            return true;
        } catch (IllegalArgumentException | IndexOutOfBoundsException | BufferOverflowException exception) {
            buffer.writeIndex(start);
            return false;
        }
    }

    private NetworkBuffer writeLeftover = null;

    public void flushSync() throws IOException {
        // Write leftover if any
        NetworkBuffer leftover = this.writeLeftover;
        if (leftover != null) {
            final boolean success = leftover.writeChannel(channel);
            if (success) {
                this.writeLeftover = null;
            } else {
                // Failed to write the whole leftover, try again next flush
                return;
            }
        }
        // Consume queued packets
        var packetQueue = this.packetQueue;
        if (packetQueue.isEmpty()) return;
        try (var hold = PacketUtils.PACKET_POOL.hold()) {
            NetworkBuffer buffer = hold.get();
            // Write to buffer
            Receivable packet;
            while ((packet = packetQueue.peek()) != null) {
                final boolean compressed = sentPacketCounter.getAndIncrement() > compressionStart;
                final boolean success = writeReceivable(buffer, packet, compressed);
                if (success) packetQueue.poll();
                else break;
            }
            // Write to channel
            final boolean success = buffer.writeChannel(channel);
            if (!success) {
                this.writeLeftover = buffer.copy(buffer.readIndex(), buffer.readableBytes());
            }
        }
    }

    record EncryptionContext(Cipher encrypt, Cipher decrypt) {
    }
}
