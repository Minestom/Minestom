package net.minestom.server.scoreboard;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

// TODO
public final class TeamManager {

    // Represents all registered teams
    private Set<Team> teams = new CopyOnWriteArraySet<>();

    public Team createTeam(String teamName) {
        Team team = new Team(teamName);
        this.teams.add(team);
        return team;
    }

    public Set<Team> getTeams() {
        return teams;
    }
}
