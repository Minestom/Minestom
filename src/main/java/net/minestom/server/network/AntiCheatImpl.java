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

import java.util.ArrayList;
import java.util.List;

final class AntiCheatImpl implements AntiCheat {
    private static final Action VALID = new Action.Valid();
    private boolean joined = false;

    private int estimatedLatency = 300;
    private long lastKeepAlive = -1;

    private Player player = new Player(null);
    private final List<Change> changes = new ArrayList<>();

    @Override
    public void consume(ServerPacket.Play serverPacket) {
        switch (serverPacket) {
            case JoinGamePacket joinGamePacket -> {
                this.joined = true;
                addChange(new Change.SetGameMode(joinGamePacket.gameMode()));
            }
            case KeepAlivePacket keepAlivePacket -> {
                this.lastKeepAlive = System.currentTimeMillis();
            }
            case RespawnPacket respawnPacket -> {
                addChange(new Change.SetGameMode(respawnPacket.gameMode()));
            }
            case ChangeGameStatePacket changeGameStatePacket -> {
                switch (changeGameStatePacket.reason()) {
                    case CHANGE_GAMEMODE -> {
                        addChange(new Change.SetGameMode(GameMode.fromId((int) changeGameStatePacket.value())));
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
                if (gameMode() != GameMode.CREATIVE)
                    yield new Action.InvalidCritical("Creative inventory action packet received in non-creative game mode.");
                yield VALID;
            }
            case ClientKeepAlivePacket packet -> {
                final long lastKeepAlive = this.lastKeepAlive;
                if (lastKeepAlive == -1) yield new Action.InvalidIgnore("Stray keep alive packet received.");
                this.estimatedLatency = (int) (System.currentTimeMillis() - lastKeepAlive);
                this.lastKeepAlive = -1;
                yield VALID;
            }
            default -> VALID;
        };
    }

    private void addChange(Change change) {
        this.changes.add(change);
        // TODO: delay
        this.player = this.player.merge(this.changes);
        this.changes.clear();
    }

    GameMode gameMode() {
        // TODO: account for latency, loop over changes if not empty
        return player.gameMode;
    }

    record Player(GameMode gameMode) {
        Player merge(List<Change> changes) {
            Player result = this;
            for (Change change : changes) {
                switch (change) {
                    case Change.SetGameMode setGameMode -> result = new Player(setGameMode.gameMode());
                }
            }
            return result;
        }
    }

    sealed interface Change {
        record SetGameMode(GameMode gameMode) implements Change {
        }
    }
}
