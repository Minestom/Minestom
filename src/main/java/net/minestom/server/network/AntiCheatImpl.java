package net.minestom.server.network;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.common.ClientKeepAlivePacket;
import net.minestom.server.network.packet.client.play.ClientCreativeInventoryActionPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.common.KeepAlivePacket;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.network.packet.server.play.JoinGamePacket;
import net.minestom.server.network.packet.server.play.RespawnPacket;

final class AntiCheatImpl implements AntiCheat {
    private static final Action VALID = new Action.Valid();
    private boolean joined = false;

    // TODO: should use a circular buffer to keep previous game modes to account for latency.
    private GameMode gameMode;

    private int estimedLatency = 300;
    private long lastKeepAlive = -1;

    @Override
    public void consume(ServerPacket.Play serverPacket) {
        switch (serverPacket) {
            case JoinGamePacket joinGamePacket -> {
                this.joined = true;
                this.gameMode = joinGamePacket.gameMode();
            }
            case KeepAlivePacket keepAlivePacket -> {
                this.lastKeepAlive = System.currentTimeMillis();
            }
            case RespawnPacket respawnPacket -> {
                this.gameMode = respawnPacket.gameMode();
            }
            case ChangeGameStatePacket changeGameStatePacket -> {
                switch (changeGameStatePacket.reason()) {
                    case CHANGE_GAMEMODE -> {
                        this.gameMode = GameMode.fromId((int) changeGameStatePacket.value());
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + changeGameStatePacket);
                }
            }
            default -> {
                // Empty
            }
        }
    }

    @Override
    public Action consume(ClientPacket clientPacket) {
        if (!joined) return new Action.InvalidCritical("Client packet received before join game packet.");

        return switch (clientPacket) {
            case ClientCreativeInventoryActionPacket packet -> {
                if (gameMode != GameMode.CREATIVE)
                    yield new Action.InvalidCritical("Creative inventory action packet received in non-creative game mode.");
                yield VALID;
            }
            case ClientKeepAlivePacket packet -> {
                final long lastKeepAlive = this.lastKeepAlive;
                if (lastKeepAlive == -1) yield new Action.InvalidIgnore("Stray keep alive packet received.");
                this.estimedLatency = (int) (System.currentTimeMillis() - lastKeepAlive);
                this.lastKeepAlive = -1;
                yield VALID;
            }
            default -> VALID;
        };
    }
}
