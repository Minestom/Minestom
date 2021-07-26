package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.extras.mojangAuth.Decrypter;
import net.minestom.server.extras.mojangAuth.Encrypter;
import net.minestom.server.extras.mojangAuth.MojangCrypt;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.netty.NettyServer;
import net.minestom.server.network.netty.codec.PacketCompressor;
import net.minestom.server.network.netty.packet.FramedPacket;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.login.SetCompressionPacket;
import net.minestom.server.utils.BufUtils;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.cache.CacheablePacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.SecretKey;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a networking connection with Netty.
 * <p>
 * It is the implementation used for all network client.
 */
public class NettyPlayerConnection extends PlayerConnection {

    private final SocketChannel channel;

    private SocketAddress remoteAddress;

    private boolean encrypted = false;
    private boolean compressed = false;

    //Could be null. Only used for Mojang Auth
    private byte[] nonce = new byte[4];

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

    private final Object tickBufferLock = new Object();
    private volatile ByteBuf tickBuffer = BufUtils.direct();

    public NettyPlayerConnection(@NotNull SocketChannel channel) {
        super();
        this.channel = channel;
        this.remoteAddress = channel.remoteAddress();
    }

    /**
     * Sets the encryption key and add the codecs to the pipeline.
     *
     * @param secretKey the secret key to use in the encryption
     * @throws IllegalStateException if encryption is already enabled for this connection
     */
    public void setEncryptionKey(@NotNull SecretKey secretKey) {
        Check.stateCondition(encrypted, "Encryption is already enabled!");
        this.encrypted = true;
        channel.pipeline().addBefore(NettyServer.GROUPED_PACKET_HANDLER_NAME, NettyServer.DECRYPT_HANDLER_NAME,
                new Decrypter(MojangCrypt.getCipher(2, secretKey)));
        channel.pipeline().addBefore(NettyServer.GROUPED_PACKET_HANDLER_NAME, NettyServer.ENCRYPT_HANDLER_NAME,
                new Encrypter(MojangCrypt.getCipher(1, secretKey)));
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

        this.compressed = true;
        writeAndFlush(new SetCompressionPacket(threshold));
        channel.pipeline().addAfter(NettyServer.FRAMER_HANDLER_NAME, NettyServer.COMPRESSOR_HANDLER_NAME,
                new PacketCompressor(threshold));
    }

    /**
     * Writes a packet to the connection channel.
     * <p>
     * All packets are flushed during {@link net.minestom.server.entity.Player#update(long)}.
     *
     * @param serverPacket the packet to write
     */
    @Override
    public void sendPacket(@NotNull ServerPacket serverPacket, boolean skipTranslating) {
        if (!channel.isActive())
            return;

        if (shouldSendPacket(serverPacket)) {
            if (getPlayer() != null) {
                // Flush happen during #update()
                if (serverPacket instanceof CacheablePacket && MinecraftServer.hasPacketCaching()) {
                    synchronized (tickBufferLock) {
                        if (tickBuffer.refCnt() == 0)
                            return;
                        CacheablePacket.writeCache(tickBuffer, serverPacket);
                    }
                } else {
                    write(serverPacket, skipTranslating);
                }
            } else {
                // Player is probably not logged yet
                writeAndFlush(serverPacket);
            }
        }
    }

    public void write(@NotNull Object message) {
        this.write(message, false);
    }

    public void write(@NotNull Object message, boolean skipTranslating) {
        if (message instanceof FramedPacket) {
            final FramedPacket framedPacket = (FramedPacket) message;
            synchronized (tickBufferLock) {
                if (tickBuffer.refCnt() == 0)
                    return;
                final ByteBuf body = framedPacket.getBody();
                tickBuffer.writeBytes(body, body.readerIndex(), body.readableBytes());
            }
            return;
        } else if (message instanceof ServerPacket) {
            ServerPacket serverPacket = (ServerPacket) message;

            if ((MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION && !skipTranslating) && getPlayer() != null && serverPacket instanceof ComponentHoldingServerPacket) {
                serverPacket = ((ComponentHoldingServerPacket) serverPacket).copyWithOperator(component ->
                        GlobalTranslator.render(component, Objects.requireNonNullElseGet(getPlayer().getLocale(), MinestomAdventure::getDefaultLocale)));
            }

            synchronized (tickBufferLock) {
                if (tickBuffer.refCnt() == 0)
                    return;
                PacketUtils.writeFramedPacket(tickBuffer, serverPacket);
            }
            return;
        } else if (message instanceof ByteBuf) {
            synchronized (tickBufferLock) {
                if (tickBuffer.refCnt() == 0)
                    return;
                tickBuffer.writeBytes((ByteBuf) message);
            }
            return;
        }
        throw new UnsupportedOperationException("type " + message.getClass() + " is not supported");
    }

    public void writeAndFlush(@NotNull Object message) {
        writeWaitingPackets();
        ChannelFuture channelFuture = channel.writeAndFlush(message);

        if (MinecraftServer.shouldProcessNettyErrors()) {
            channelFuture.addListener(future -> {
                if (!future.isSuccess() && channel.isActive()) {
                    MinecraftServer.getExceptionManager().handleException(future.cause());
                }
            });
        }
    }

    public void writeWaitingPackets() {
        if (tickBuffer.writerIndex() == 0) {
            // Nothing to write
            return;
        }

        // Retrieve safe copy
        final ByteBuf copy;
        synchronized (tickBufferLock) {
            if (tickBuffer.refCnt() == 0)
                return;
            copy = tickBuffer;
            tickBuffer = tickBuffer.alloc().buffer(tickBuffer.writerIndex());
        }

        // Write copied buffer to netty
        ChannelFuture channelFuture = channel.write(new FramedPacket(copy));
        channelFuture.addListener(future -> copy.release());

        // Netty debug
        if (MinecraftServer.shouldProcessNettyErrors()) {
            channelFuture.addListener(future -> {
                if (!future.isSuccess() && channel.isActive()) {
                    MinecraftServer.getExceptionManager().handleException(future.cause());
                }
            });
        }
    }

    public void flush() {
        final int bufferSize = tickBuffer.writerIndex();
        if (bufferSize > 0) {
            if (channel.isActive()) {
                writeWaitingPackets();
                channel.flush();
            }
        }
    }

    @NotNull
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
    public void setRemoteAddress(@NotNull SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }


    @Override
    public void disconnect() {
        refreshOnline(false);
        this.channel.close();
    }

    @NotNull
    public Channel getChannel() {
        return channel;
    }

    /**
     * Retrieves the username received from the client during connection.
     * <p>
     * This value has not been checked and could be anything.
     *
     * @return the username given by the client, unchecked
     */
    @Nullable
    public String getLoginUsername() {
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


    @Nullable
    public UUID getBungeeUuid() {
        return bungeeUuid;
    }

    public void UNSAFE_setBungeeUuid(UUID bungeeUuid) {
        this.bungeeUuid = bungeeUuid;
    }

    @Nullable
    public PlayerSkin getBungeeSkin() {
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
    @Nullable
    public String getPluginRequestChannel(int messageId) {
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

    public void releaseTickBuffer() {
        synchronized (tickBufferLock) {
            tickBuffer.release();
        }
    }

    public byte[] getNonce() {
        return nonce;
    }

    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }
}
