package net.minestom.server.entity.features;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.utils.PacketUtils;

public class EntityFeatureTeams extends EntityFeatureBase {

    private Team team;

    public EntityFeatureTeams(Entity entity) {
        super(entity);
    }

    @Override
    public void onAddViewer(Player player) {
        Team team = getTeam();
        if (team != null) {
            player.getPlayerConnection().sendPacket(team.createTeamsCreationPacket());
        }
    }

    /**
     * Changes the {@link Team} for the entity.
     *
     * @param team The new team
     */
    public void setTeam(Team team) {
        if (this.team == team) return;

        String member;

        boolean isPlayer = entity instanceof Player;

        if (isPlayer) {
            Player player = (Player) entity;
            member = player.getUsername();
        } else {
            member = entity.getUuid().toString();
        }

        if (this.team != null) {
            this.team.removeMember(member);
        }

        this.team = team;
        if (team != null) {
            team.addMember(member);
            if (isPlayer) {
                var players = MinecraftServer.getConnectionManager().getOnlinePlayers();
                PacketUtils.sendGroupedPacket(players, team.createTeamsCreationPacket());
            }
        }
    }

    /**
     * Gets the {@link Team} of the entity.
     *
     * @return the {@link Team}
     */
    public Team getTeam() {
        return team;
    }
}
