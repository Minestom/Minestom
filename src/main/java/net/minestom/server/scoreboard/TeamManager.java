package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.UniqueIdUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * An object which manages all the {@link Team}'s
 */
public final class TeamManager {

    /**
     * Represents all registered teams
     */
    private final Set<Team> teams;

    /**
     * Default constructor
     */
    public TeamManager() {
        this.teams = new CopyOnWriteArraySet<>();
    }

    /**
     * Registers a new {@link Team}
     *
     * @param team The team to be registered
     */
    protected void registerNewTeam(@NotNull Team team) {
        this.teams.add(team);
        PacketUtils.broadcastPacket(team.createTeamsCreationPacket());
    }

    /**
     * Deletes a {@link Team}
     *
     * @param registryName The registry name of team
     * @return {@code true} if the team was deleted, otherwise {@code false}
     */
    public boolean deleteTeam(@NotNull String registryName) {
        Team team = this.getTeam(registryName);
        if (team == null) return false;
        return this.deleteTeam(team);
    }

    /**
     * Deletes a {@link Team}
     *
     * @param team The team to be deleted
     * @return {@code true} if the team was deleted, otherwise {@code false}
     */
    public boolean deleteTeam(@NotNull Team team) {
        // Sends to all online players a team destroy packet
        PacketUtils.broadcastPacket(team.createTeamDestructionPacket());
        return this.teams.remove(team);
    }

    /**
     * Initializes a new {@link TeamBuilder} for creating a team
     *
     * @param name The registry name of the team
     * @return the team builder
     */
    public TeamBuilder createBuilder(@NotNull String name) {
        return new TeamBuilder(name, this);
    }

    /**
     * Creates a {@link Team} with only the registry name
     *
     * @param name The registry name
     * @return the created {@link Team}
     */
    public Team createTeam(@NotNull String name) {
        return this.createBuilder(name).build();
    }

    /**
     * Creates a {@link Team} with the registry name, prefix, suffix and the team format
     *
     * @param name      The registry name
     * @param prefix    The team prefix
     * @param teamColor The team format
     * @param suffix    The team suffix
     * @return the created {@link Team} with a prefix, teamColor and suffix
     */
    public Team createTeam(String name, Component prefix, NamedTextColor teamColor, Component suffix) {
        return this.createBuilder(name).prefix(prefix).teamColor(teamColor).suffix(suffix).updateTeamPacket().build();
    }

    /**
     * Creates a {@link Team} with the registry name, display name, prefix, suffix and the team colro
     *
     * @param name        The registry name
     * @param displayName The display name
     * @param prefix      The team prefix
     * @param teamColor   The team color
     * @param suffix      The team suffix
     * @return the created {@link Team} with a prefix, teamColor, suffix and the display name
     */
    public Team createTeam(String name, Component displayName, Component prefix, NamedTextColor teamColor, Component suffix) {
        return this.createBuilder(name).teamDisplayName(displayName).prefix(prefix).teamColor(teamColor).suffix(suffix).updateTeamPacket().build();
    }

    /**
     * Gets a {@link Team} with the given name
     *
     * @param teamName The registry name of the team
     * @return a registered {@link Team} or {@code null}
     */
    public Team getTeam(String teamName) {
        for (Team team : this.teams) {
            if (team.getTeamName().equals(teamName)) return team;
        }
        return null;
    }

    /**
     * Checks if the given name a registry name of a registered {@link Team}
     *
     * @param teamName The name of the team
     * @return {@code true} if the team is registered, otherwise {@code false}
     */
    public boolean exists(String teamName) {
        for (Team team : this.teams) {
            if (team.getTeamName().equals(teamName)) return true;
        }
        return false;
    }

    /**
     * Checks if the given {@link Team} registered
     *
     * @param team The searched team
     * @return {@code true} if the team is registered, otherwise {@code false}
     */
    public boolean exists(Team team) {
        return this.exists(team.getTeamName());
    }

    /**
     * Gets a {@link List} with all registered {@link Player} in the team
     * <br>
     * <b>Note:</b> The list exclude all entities. To get all entities of the team, you can use {@link #getEntities(Team)}
     *
     * @param team The team
     * @return a {@link List} with all registered {@link Player}
     */
    public List<String> getPlayers(Team team) {
        List<String> players = new ArrayList<>();
        for (String member : team.getMembers()) {
            boolean match = UniqueIdUtils.isUniqueId(member);

            if (!match) players.add(member);
        }
        return players;
    }

    /**
     * Gets a {@link List} with all registered {@link LivingEntity} in the team
     * <br>
     * <b>Note:</b> The list exclude all players. To get all players of the team, you can use {@link #getPlayers(Team)}
     *
     * @param team The team
     * @return a {@link List} with all registered {@link LivingEntity}
     */
    public List<String> getEntities(Team team) {
        List<String> entities = new ArrayList<>();
        for (String member : team.getMembers()) {
            boolean match = UniqueIdUtils.isUniqueId(member);

            if (match) entities.add(member);
        }
        return entities;
    }

    /**
     * Gets a {@link Set} with all registered {@link Team}'s
     *
     * @return a {@link Set} with all registered {@link Team}'s
     */
    public Set<Team> getTeams() {
        return this.teams;
    }
}
