package fr.themode.minestom.net.netty.channel;

import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.PacketProcessor;
import fr.themode.minestom.net.netty.packet.PacketHandler;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.status.LegacyServerListPingPacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientChannel extends ChannelInboundHandlerAdapter {

    private ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
    private PacketProcessor packetProcessor;

    public ClientChannel(PacketProcessor packetProcessor) {
        this.packetProcessor = packetProcessor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("CONNECTION");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        PacketHandler packetHandler = (PacketHandler) msg;
        int packetLength = packetHandler.length;
        ByteBuf buffer = packetHandler.buffer;

        if (packetLength == 0xFE) { // Legacy server ping
            LegacyServerListPingPacket legacyServerListPingPacket = new LegacyServerListPingPacket();
            legacyServerListPingPacket.read(new PacketReader(buffer, 0));
            legacyServerListPingPacket.process(null, null);
            return;
        }

        final int varIntLength = Utils.lengthVarInt(packetLength);
        int packetId = Utils.readVarInt(buffer);

        int offset = varIntLength + Utils.lengthVarInt(packetId);
        packetProcessor.process(ctx, buffer, packetId, packetLength, offset);

        buffer.release();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("DISCONNECTION");
        PlayerConnection playerConnection = packetProcessor.getPlayerConnection(ctx);
        if (playerConnection != null) {
            playerConnection.refreshOnline(false);
            Player player = connectionManager.getPlayer(playerConnection);
            if (player != null) {
                player.remove();

                connectionManager.removePlayer(playerConnection);
            }
            packetProcessor.removePlayerConnection(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
