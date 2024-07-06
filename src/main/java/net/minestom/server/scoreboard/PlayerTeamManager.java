package net.minestom.server.scoreboard;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

public class PlayerTeamManager extends TeamManager {
    private final Player player;

    public PlayerTeamManager(Player player) {
        super();

        this.player = player;
    }

    @Override
    protected void registerNewTeam(@NotNull Team team) {
        this.getTeams().add(team);
        PacketUtils.sendPacket(this.player, team.createTeamsCreationPacket());
    }

    @Override
    public boolean deleteTeam(@NotNull Team team) {
        PacketUtils.sendPacket(this.player, team.createTeamDestructionPacket());
        return this.getTeams().remove(team);
    }

    @Override
    public TeamBuilder createBuilder(@NotNull String name) {
        return new PlayerTeamBuilder(name, this, this.player);
    }
}
