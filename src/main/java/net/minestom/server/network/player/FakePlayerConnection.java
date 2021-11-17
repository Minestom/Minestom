package net.minestom.server.network.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.fakeplayer.FakePlayerController;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.FramedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class FakePlayerConnection extends PlayerConnection {

    @Override
    public void sendPacket(@NotNull SendablePacket packet) {
        FakePlayerController controller = getFakePlayer().getController();
        if (packet instanceof ServerPacket serverPacket) {
            if (!shouldSendPacket(serverPacket)) return;
            controller.consumePacket(serverPacket);
        } else if (packet instanceof FramedPacket framedPacket) {
            controller.consumePacket(framedPacket.packet());
        } else if (packet instanceof CachedPacket cachedPacket) {
            controller.consumePacket(cachedPacket.packet());
        } else {
            throw new RuntimeException("Unknown packet type: " + packet.getClass().getName());
        }
    }

    @NotNull
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
