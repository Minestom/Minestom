package net.minestom.server.network.player;

import io.netty.buffer.ByteBuf;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.network.packet.server.ServerPacket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class FakePlayerConnection extends PlayerConnection {

    private FakePlayer fakePlayer;

    @Override
    public void sendPacket(ByteBuf buffer) {
        throw new UnsupportedOperationException("FakePlayer cannot read Bytebuf");
    }

    @Override
    public void writePacket(ByteBuf buffer) {
        throw new UnsupportedOperationException("FakePlayer cannot read Bytebuf");
    }

    @Override
    public void sendPacket(ServerPacket serverPacket) {
        this.fakePlayer.getController().consumePacket(serverPacket);
    }

    @Override
    public void flush() {

    }

    @Override
    public SocketAddress getRemoteAddress() {
        return new InetSocketAddress(0);
    }

    @Override
    public void disconnect() {
        if (fakePlayer.isRegistered())
            MinecraftServer.getConnectionManager().removePlayer(this);
    }

    public FakePlayer getFakePlayer() {
        return fakePlayer;
    }

    public void setFakePlayer(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }
}
