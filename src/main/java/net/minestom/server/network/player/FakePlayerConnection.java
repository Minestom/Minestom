package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.validate.Check;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class FakePlayerConnection extends PlayerConnection {

    @Override
    public void enableCompression(int threshold) {
        throw new UnsupportedOperationException("FakePlayer cannot enable compression");
    }

    @Override
    public void sendPacket(ByteBuf buffer, boolean copy) {
        throw new UnsupportedOperationException("FakePlayer cannot read Bytebuf");
    }

    @Override
    public void writePacket(ByteBuf buffer, boolean copy) {
        throw new UnsupportedOperationException("FakePlayer cannot write to Bytebuf");
    }

    @Override
    public void sendPacket(ServerPacket serverPacket) {
        getFakePlayer().getController().consumePacket(serverPacket);
    }

    @Override
    public void flush() {
        // Does nothing
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return new InetSocketAddress(0);
    }

    @Override
    public void disconnect() {
        if (getFakePlayer().getOption().isRegistered())
            MinecraftServer.getConnectionManager().removePlayer(this);
    }

    public FakePlayer getFakePlayer() {
        return (FakePlayer) getPlayer();
    }


    @Override
    public void setPlayer(Player player) {
        Check.argCondition(!(player instanceof FakePlayer), "FakePlayerController needs a FakePlayer object");
        super.setPlayer(player);
    }
}
