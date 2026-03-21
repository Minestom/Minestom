package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.network.packet.client.play.ClientSetGameRulesPacket;

import java.util.List;
import java.util.Objects;

public class PlayerSetGameRulesEvent implements PlayerInstanceEvent {
    private final Player player;
    private final List<ClientSetGameRulesPacket.Entry> requestedRules;

    public PlayerSetGameRulesEvent(Player player, List<ClientSetGameRulesPacket.Entry> requestedRules) {
        this.player = Objects.requireNonNull(player, "player");
        Objects.requireNonNull(requestedRules, "requestedRules");
        this.requestedRules = List.copyOf(requestedRules);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    /// The requested new rules by the client.
    public List<ClientSetGameRulesPacket.Entry> getRequestedRules() {
        return requestedRules;
    }
}
