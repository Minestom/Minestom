package net.minestom.server.scoreboard;

import net.minestom.server.entity.Player;

public class PlayerTeamBuilder extends TeamBuilder {
    public PlayerTeamBuilder(String name, TeamManager teamManager, Player player) {
        super(teamManager.exists(name) ? teamManager.getTeam(name) : new PlayerTeam(name, player), teamManager);
    }
}
