package net.minestom.server.scoreboard;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PlayerTeam extends Team {
    private final Player player;

    protected PlayerTeam(@NotNull String teamName, Player player) {
        super(teamName);
        this.player = player;
    }

    @Override
    public void addMembers(@NotNull Collection<@NotNull String> toAdd) {
        // Adds a new member to the team
        this.getMembersObject().addAll(toAdd);

        // Initializes add player packet
        final TeamsPacket addPlayerPacket = new TeamsPacket(this.getTeamName(),
                new TeamsPacket.AddEntitiesToTeamAction(toAdd));
        // Sends to all online players the add player packet
        PacketUtils.sendPacket(this.player, addPlayerPacket);

        // invalidate player members
        this.setPlayerMembersUpToDate(false);
    }

    @Override
    public void removeMembers(@NotNull Collection<@NotNull String> toRemove) {
        // Initializes remove player packet
        final TeamsPacket removePlayerPacket = new TeamsPacket(this.getTeamName(),
                new TeamsPacket.RemoveEntitiesToTeamAction(toRemove));
        // Sends to all online player the remove player packet
        PacketUtils.sendPacket(this.player, removePlayerPacket);

        // Removes the member from the team
        this.getMembersObject().removeAll(toRemove);

        // invalidate player members
        this.setPlayerMembersUpToDate(false);
    }

    @Override
    public void sendUpdatePacket() {
        final var info = new TeamsPacket.UpdateTeamAction(this.getTeamDisplayName(), this.getFriendlyFlags(),
                this.getNameTagVisibility(), this.getCollisionRule(), this.getTeamColor(), this.getPrefix(), this.getSuffix());
        PacketUtils.sendPacket(this.player, new TeamsPacket(this.getTeamName(), info));
    }
}
