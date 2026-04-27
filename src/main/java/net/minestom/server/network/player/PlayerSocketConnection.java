package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
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
import net.minestom.server.network.packet.client.configuration.ClientFinishConfigurationPacket;
import net.minestom.server.network.packet.client.configuration.ClientSelectKnownPacksPacket;
import net.minestom.server.network.packet.client.handshake.ClientHandshakePacket;
import net.minestom.server.network.packet.client.login.ClientEncryptionResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginAcknowledgedPacket;
import net.minestom.server.network.packet.client.login.ClientLoginPluginResponsePacket;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.client.status.StatusRequestPacket;
import net.minestom.server.network.packet.server.*;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.utils.collection.ConcurrentMessageQueues;
import net.minestom.server.utils.validate.Check;
import org.jctools.queues.MessagePassingQueue;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DataFormatException;

/**
 * Represents a single player's network connection, driven entirely by the
 * Netty event loop.
 *
 * <p>Key differences from the previous implementation:
 * <ul>
 *   <li>{@code java.nio.channels.SocketChannel} → Netty {@link Channel}.</li>
 *   <li>Manual read/write virtual-thread loops → Netty
 *       {@link ChannelDuplexHandler} ({@link ConnectionHandler}).</li>
 *   <li>Raw {@code NetworkBuffer.readChannel} / {@code writeChannel} replaced
 *       by {@link NetworkBuffer#readFromByteBuf} / {@link NetworkBuffer#writeToByteBuf}.</li>
 *   <li>No {@code java.nio.*} imports outside the whitelist.</li>
 * </ul>
 */
@ApiStatus.Internal
public class PlayerSocketConnection extends PlayerConnection {

    private static final Set<Class<? extends ClientPacket>> IMMEDIATE_PROCESS_PACKETS = Set.of(
            ClientHandshakePacket.class,
            ClientCookieResponsePacket.class,
            StatusRequestPacket.class,
            ClientPingRequestPacket.class,
            ClientKeepAlivePacket.class,
            ClientLoginStartPacket.class,
            ClientEncryptionResponsePacket.class,
            ClientLoginPluginResponsePacket.class,
            ClientSelectKnownPacksPacket.class,
            ClientLoginAcknowledgedPacket.class,
            ClientFinishConfigurationPacket.class
    );

    private final Channel channel;
    private SocketAddress remoteAddress;
    private final PacketParser<ClientPacket> packetParser;

    /** Cipher context for AES-CFB8 Mojang encryption (nullable until login). */
    private volatile EncryptionContext encryptionContext;
    private byte[] nonce = new byte[4];

    // Data from client packets
    private String loginUsername;
    private GameProfile gameProfile;
    private String serverAddress;
    private int serverPort;
    private int protocolVersion;

    /** Accumulation buffer for fragmented inbound data. */
    private final NetworkBuffer readBuffer =
            NetworkBuffer.resizableBuffer(ServerFlag.POOLED_BUFFER_SIZE, MinecraftServer.process());

    /** Outbound packet queue — filled by any thread, drained by the Netty I/O thread. */
    private final MessagePassingQueue<SendablePacket> packetQueue =
            ConcurrentMessageQueues.mpscUnboundedArrayQueue(1024);

    private final AtomicLong sentPacketCounter = new AtomicLong();
    /**
     * Compression begins after the packet at index {@code compressionStart} has
     * been sent. {@code Long.MAX_VALUE} means compression is not yet enabled.
     */
    private volatile long compressionStart = Long.MAX_VALUE;

    private final ListenerHandle<PlayerPacketOutEvent> outgoing =
            EventDispatcher.getHandle(PlayerPacketOutEvent.class);

    /** The Netty handler that bridges channel events into this connection. */
    private final ConnectionHandler handler = new ConnectionHandler();

    public PlayerSocketConnection(Channel channel,
                                  SocketAddress remoteAddress,
                                  PacketParser<ClientPacket> packetParser) {
        super();
        this.channel       = channel;
        this.remoteAddress = remoteAddress;
        this.packetParser  = packetParser;
    }

    /** Returns the Netty {@link ChannelDuplexHandler} to be added to the pipeline. */
    public ConnectionHandler channelHandler() {
        return handler;
    }

    private void handleRead(ByteBuf frame) {
        final NetworkBuffer readBuffer = this.readBuffer;

        // Append frame bytes to our accumulation buffer
        final long writeIndexBefore = readBuffer.writeIndex();
        readBuffer.readFromByteBuf(frame);

        // Decrypt newly appended bytes
        final EncryptionContext ctx = this.encryptionContext;
        if (ctx != null) {
            final long written = readBuffer.writeIndex() - writeIndexBefore;
            readBuffer.cipher(ctx.decrypt(), writeIndexBefore, written);
        }

        processPackets(readBuffer);
    }

    private boolean compression() {
        return compressionStart != Long.MAX_VALUE;
    }

    private void processPackets(NetworkBuffer readBuffer) {
        final ConnectionState startingState = getClientState();
        final PacketReading.Result<ClientPacket> result;
        try {
            result = PacketReading.readPackets(
                    readBuffer,
                    packetParser,
                    startingState, PacketVanilla::nextClientState,
                    compression()
            );
        } catch (DataFormatException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            disconnect();
            return;
        }

        switch (result) {
            case PacketReading.Result.Success<ClientPacket> success -> {
                for (PacketReading.ParsedPacket<ClientPacket> parsed : success.packets()) {
                    final ClientPacket packet = parsed.packet();
                    try {
                        if (IMMEDIATE_PROCESS_PACKETS.contains(packet.getClass())) {
                            MinecraftServer.getPacketListenerManager()
                                    .processClientPacket(packet, this);
                        } else {
                            final Player player = getPlayer();
                            assert player != null;
                            player.addPacketToQueue(packet);
                        }
                    } catch (Exception e) {
                        MinecraftServer.getExceptionManager().handleException(e);
                    }
                }
                readBuffer.compact();
            }
            case PacketReading.Result.Empty<ClientPacket> ignored -> { /* nothing yet */ }
            case PacketReading.Result.Failure<ClientPacket> failure -> {
                final long required = failure.requiredCapacity();
                assert required > readBuffer.capacity();
                readBuffer.resize(required);
            }
        }
    }

    @Override
    public void sendPacket(SendablePacket packet) {
        packetQueue.relaxedOffer(packet);
        channel.flush(); // schedule a write on the event loop
    }

    @Override
    public void sendPackets(Collection<SendablePacket> packets) {
        for (SendablePacket p : packets) packetQueue.relaxedOffer(p);
        channel.flush();
    }

    /**
     * Drains {@link #packetQueue} into a single Netty {@link ByteBuf} and writes
     * it to the channel. Called exclusively from the Netty I/O thread.
     */
    private void flushQueue() {
        if (packetQueue.isEmpty()) return;

        final NetworkBuffer buffer = PacketVanilla.PACKET_POOL.get();
        PacketWriting.writeQueue(buffer, packetQueue, 1, (b, packet) -> {
            final boolean compressed = sentPacketCounter.get() > compressionStart;
            final boolean ok = writePacketSync(b, packet, compressed);
            if (ok) sentPacketCounter.getAndIncrement();
            return ok;
        });

        // Transfer buffer contents to a Netty ByteBuf and write to channel
        final long readable = buffer.readableBytes();
        if (readable > 0) {
            final ByteBuf out = channel.alloc().buffer((int) readable);
            buffer.writeToByteBuf(out);

            // Encrypt if needed
            final EncryptionContext ctx = this.encryptionContext;
            if (ctx != null && out.isReadable()) {
                // cipher() works on the NetworkBuffer; re-apply on raw bytes
                final byte[] raw = new byte[out.readableBytes()];
                out.getBytes(out.readerIndex(), raw);
                try {
                    final byte[] encrypted = ctx.encrypt().update(raw);
                    out.clear();
                    out.writeBytes(encrypted);
                } catch (Exception e) {
                    out.release();
                    throw new RuntimeException(e);
                }
            }

            channel.writeAndFlush(out);
        }

        PacketVanilla.PACKET_POOL.add(buffer);
    }

    private boolean writePacketSync(NetworkBuffer buffer, SendablePacket packet, boolean compressed) {
        final Player player = getPlayer();
        final ConnectionState state = getServerState();

        if (player != null) {
            // Outgoing event
            if (outgoing.hasListener()) {
                final ServerPacket sp = SendablePacket.extractServerPacket(state, packet);
                if (sp != null) {
                    final PlayerPacketOutEvent event = new PlayerPacketOutEvent(player, sp);
                    outgoing.call(event);
                    if (event.isCancelled()) return true;
                }
            }
            // Adventure translation
            if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION
                    && packet instanceof ServerPacket.ComponentHolding ch) {
                packet = ch.copyWithOperator(component ->
                        MinestomAdventure.COMPONENT_TRANSLATOR.apply(component,
                                Objects.requireNonNullElseGet(player.getLocale(),
                                        MinestomAdventure::getDefaultLocale)));
            }
        }

        final long start = buffer.writeIndex();
        final int threshold = compressed ? MinecraftServer.getCompressionThreshold() : 0;
        try {
            return switch (packet) {
                case ServerPacket sp -> {
                    final var next = PacketVanilla.nextServerState(sp, state);
                    if (next != state) setServerState(next);
                    PacketWriting.writeFramedPacket(buffer, state, sp, threshold);
                    yield true;
                }
                case FramedPacket fp -> {
                    final NetworkBuffer body = fp.body();
                    yield writeBuffer(buffer, body, 0, body.capacity());
                }
                case CachedPacket cp -> {
                    final NetworkBuffer body = cp.body(state);
                    if (body != null) {
                        yield writeBuffer(buffer, body, 0, body.capacity());
                    } else {
                        PacketWriting.writeFramedPacket(buffer, state, cp.packet(state), threshold);
                        yield true;
                    }
                }
                case LazyPacket lp -> {
                    PacketWriting.writeFramedPacket(buffer, state, lp.packet(), threshold);
                    yield true;
                }
                case BufferedPacket bp -> {
                    yield writeBuffer(buffer, bp.buffer(), bp.index(), bp.length());
                }
            };
        } catch (IndexOutOfBoundsException e) {
            buffer.writeIndex(start);
            return false;
        }
    }

    private boolean writeBuffer(NetworkBuffer dst, NetworkBuffer src,
                                long index, long length) {
        if (dst.writableBytes() < length) return false;
        NetworkBuffer.copy(src, index, dst, dst.writeIndex(), length);
        dst.advanceWrite(length);
        return true;
    }

    public void setEncryptionKey(SecretKey secretKey) {
        Check.stateCondition(encryptionContext != null, "Encryption is already enabled!");
        this.encryptionContext = new EncryptionContext(
                MojangCrypt.getCipher(1, secretKey),
                MojangCrypt.getCipher(2, secretKey));
    }

    public void startCompression() {
        Check.stateCondition(compression(), "Compression is already enabled!");
        this.compressionStart = sentPacketCounter.get();
        final int threshold = MinecraftServer.getCompressionThreshold();
        Check.stateCondition(threshold == 0,
                "Compression cannot be enabled because the threshold is equal to 0");
        sendPacket(new SetCompressionPacket(threshold));
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @ApiStatus.Internal
    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public Channel getChannel() {
        return channel;
    }

    public @Nullable GameProfile gameProfile() {
        return gameProfile;
    }

    public void UNSAFE_setProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    public @Nullable String getLoginUsername() {
        return loginUsername;
    }

    public void UNSAFE_setLoginUsername(String loginUsername) {
        this.loginUsername = loginUsername;
    }

    @Override public @Nullable String getServerAddress()  { return serverAddress; }
    @Override public int            getServerPort()        { return serverPort; }
    @Override public int            getProtocolVersion()   { return protocolVersion; }

    public void refreshServerInformation(@Nullable String serverAddress,
                                         int serverPort,
                                         int protocolVersion) {
        this.serverAddress   = serverAddress;
        this.serverPort      = serverPort;
        this.protocolVersion = protocolVersion;
    }

    public byte[] getNonce()            { return nonce; }
    public void   setNonce(byte[] n)    { this.nonce = n; }

    @Override
    public void disconnect() {
        super.disconnect();
        channel.close();
    }

    /**
     * Bridges Netty channel lifecycle events into {@link PlayerSocketConnection}.
     * This handler is added to the Netty pipeline by {@code Server} and must
     * be annotated {@code @ChannelHandler.Sharable} only if shared — here each
     * connection gets its own instance.
     */
    public final class ConnectionHandler extends ChannelDuplexHandler {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof ByteBuf frame) {
                try {
                    handleRead(frame);
                } finally {
                    frame.release();
                }
            }
        }

        @Override
        public void flush(ChannelHandlerContext ctx) {
            flushQueue();
            ctx.flush();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            final ChannelPromise promise = ctx.newPromise();

            try {
                disconnect(ctx, promise);
            } catch (Exception e) {
                ctx.close(promise);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            final boolean expected =
                    cause instanceof IOException &&
                            (cause.getMessage() != null &&
                                    (cause.getMessage().contains("Connection reset") ||
                                            cause.getMessage().contains("Broken pipe")));
            if (!expected) {
                MinecraftServer.getExceptionManager().handleException(cause);
            }

            final ChannelPromise promise = ctx.newPromise();

            try {
                disconnect(ctx, promise);
            } catch (Exception e) {
                ctx.close(promise);
            }
        }
    }

    record EncryptionContext(Cipher encrypt, Cipher decrypt) {}
}