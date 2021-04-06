package net.minestom.server.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.Packet;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.network.packet.client.ClientPreplayPacket;
import net.minestom.server.network.packet.client.handler.ClientLoginPacketsHandler;
import net.minestom.server.network.packet.client.handler.ClientPlayPacketsHandler;
import net.minestom.server.network.packet.client.handler.ClientStatusPacketsHandler;
import net.minestom.server.network.packet.handler.PacketsHandler;
import net.minestom.server.network.player.NettyPlayerConnection;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for processing client packets.
 * <p>
 * You can retrieve the different packet handlers per state (status/login/play)
 * from the {@link net.minestom.server.network.packet.client.handler.ClientPacketsHandler} class.
 * <p>
 * Packet handlers are cached here and can be retrieved with {@link #getStatusPacketsHandler()}, {@link #getLoginPacketsHandler()}
 * and {@link #getPlayPacketsHandler()}. The one to use depend on the type of packet you need to retrieve (the packet id 0 does not have
 * the same meaning as it is a login or play packet).
 */
public final class ServerSidePacketProcessor extends PacketProcessor<ClientPlayPacket, ClientPreplayPacket> {

    private final Map<ChannelHandlerContext, PlayerConnection> connectionPlayerConnectionMap = new ConcurrentHashMap<>();

    public ServerSidePacketProcessor() {
    }

    @Override
    protected PacketsHandler<ClientPlayPacket> createPlayPacketsHandler() {
        return new ClientPlayPacketsHandler();
    }

    @Override
    protected PacketsHandler<ClientPreplayPacket> createLoginPacketsHandler() {
        return new ClientLoginPacketsHandler();
    }

    @Override
    protected PacketsHandler<ClientPreplayPacket> createStatusPacketsHandler() {
        return new ClientStatusPacketsHandler();
    }

    @Override
    protected void processPlayPacket(PlayerConnection playerConnection, ClientPlayPacket clientPlayPacket) {
        Player player = playerConnection.getPlayer();
        assert player != null;
        player.addPacketToQueue(clientPlayPacket);
    }

    @Override
    protected void processLoginPacket(PlayerConnection playerConnection, ClientPreplayPacket loginPacket) {
        loginPacket.process(playerConnection);
    }

    @Override
    protected void processStatusPacket(PlayerConnection playerConnection, ClientPreplayPacket statusPacket) {
        statusPacket.process(playerConnection);
    }

    @Override
    public @Nullable PlayerConnection getPlayerConnection(ChannelHandlerContext context) {
        return connectionPlayerConnectionMap.get(context);
    }

    public void createPlayerConnection(@NotNull ChannelHandlerContext context) {
        final PlayerConnection playerConnection = new NettyPlayerConnection((SocketChannel) context.channel());
        connectionPlayerConnectionMap.put(context, playerConnection);
    }

    public PlayerConnection removePlayerConnection(@NotNull ChannelHandlerContext context) {
        return connectionPlayerConnectionMap.remove(context);
    }
}
