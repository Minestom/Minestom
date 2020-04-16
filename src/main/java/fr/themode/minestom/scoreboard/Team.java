package fr.themode.minestom.scoreboard;

import fr.themode.minestom.chat.ChatColor;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.server.play.TeamsPacket;
import fr.themode.minestom.utils.PacketUtils;
import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Team {

    private String teamName;
    private String teamDisplayName = "";
    private byte friendlyFlags = 0x00;
    private TeamsPacket.NameTagVisibility nameTagVisibility = TeamsPacket.NameTagVisibility.ALWAYS;
    private TeamsPacket.CollisionRule collisionRule = TeamsPacket.CollisionRule.NEVER;
    private ChatColor teamColor = ChatColor.WHITE;
    private String prefix = "", suffix = "";
    private String[] entities = new String[0];
    private Set<Player> players = new CopyOnWriteArraySet<>();

    private TeamsPacket teamsCreationPacket;

    private ByteBuf teamsDestroyPacket;

    protected Team(String teamName) {
        this.teamName = teamName;

        teamsCreationPacket = new TeamsPacket();
        teamsCreationPacket.teamName = teamName;
        teamsCreationPacket.action = TeamsPacket.Action.CREATE_TEAM;
        teamsCreationPacket.teamDisplayName = teamDisplayName;
        teamsCreationPacket.friendlyFlags = friendlyFlags;
        teamsCreationPacket.nameTagVisibility = nameTagVisibility;
        teamsCreationPacket.collisionRule = collisionRule;
        teamsCreationPacket.teamColor = teamColor.getId();
        teamsCreationPacket.teamPrefix = prefix;
        teamsCreationPacket.teamSuffix = suffix;
        teamsCreationPacket.entities = entities;

        TeamsPacket destroyPacket = new TeamsPacket();
        destroyPacket.teamName = teamName;
        destroyPacket.action = TeamsPacket.Action.REMOVE_TEAM;
        teamsDestroyPacket = PacketUtils.writePacket(destroyPacket); // Directly write packet since it will not change
    }

    public void addPlayer(Player player) {
        String newElement = player.getUsername();

        TeamsPacket addPlayerPacket = new TeamsPacket();
        addPlayerPacket.teamName = teamName;
        addPlayerPacket.action = TeamsPacket.Action.ADD_PLAYERS_TEAM;
        addPlayerPacket.entities = new String[]{newElement};
        for (Player p : players) {
            p.getPlayerConnection().sendPacket(addPlayerPacket);
        }

        String[] entitiesCache = new String[entities.length + 1];
        System.arraycopy(entities, 0, entitiesCache, 0, entities.length);
        entitiesCache[entities.length] = newElement;
        this.entities = entitiesCache;
        this.teamsCreationPacket.entities = entities;

        this.players.add(player);
        player.getPlayerConnection().sendPacket(teamsCreationPacket);
    }

    public void removePlayer(Player player) {
        TeamsPacket removePlayerPacket = new TeamsPacket();
        removePlayerPacket.teamName = teamName;
        removePlayerPacket.action = TeamsPacket.Action.REMOVE_PLAYERS_TEAM;
        removePlayerPacket.entities = new String[]{player.getUsername()};
        for (Player p : players) {
            p.getPlayerConnection().sendPacket(removePlayerPacket);
        }

        this.players.remove(player);
        player.getPlayerConnection().sendPacket(teamsDestroyPacket); // TODO do not destroy, simply remove the player from the team

        String[] entitiesCache = new String[entities.length - 1];
        int count = 0;
        for (Player p : players) {
            entitiesCache[count++] = p.getUsername();
        }
        this.entities = entitiesCache;
        this.teamsCreationPacket.entities = entities;
    }

    public void setTeamDisplayName(String teamDisplayName) {
        this.teamDisplayName = teamDisplayName;
        this.teamsCreationPacket.teamDisplayName = teamDisplayName;
        sendUpdatePacket();
    }

    public void setNameTagVisibility(TeamsPacket.NameTagVisibility nameTagVisibility) {
        this.nameTagVisibility = nameTagVisibility;
        this.teamsCreationPacket.nameTagVisibility = nameTagVisibility;
        sendUpdatePacket();
    }

    public void setCollisionRule(TeamsPacket.CollisionRule collisionRule) {
        this.collisionRule = collisionRule;
        this.teamsCreationPacket.collisionRule = collisionRule;
        sendUpdatePacket();
    }

    public void setTeamColor(ChatColor teamColor) {
        this.teamColor = teamColor;
        this.teamsCreationPacket.teamColor = teamColor.getId();
        sendUpdatePacket();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.teamsCreationPacket.teamPrefix = prefix;
        sendUpdatePacket();
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
        this.teamsCreationPacket.teamSuffix = suffix;
        sendUpdatePacket();
    }

    public String getTeamName() {
        return teamName;
    }

    public TeamsPacket getTeamsCreationPacket() {
        return teamsCreationPacket;
    }

    public TeamsPacket createTeamDestructionPacket() {
        TeamsPacket teamsPacket = new TeamsPacket();
        teamsPacket.teamName = teamName;
        teamsPacket.action = TeamsPacket.Action.REMOVE_TEAM;
        return teamsPacket;
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    private void sendUpdatePacket() {
        TeamsPacket updatePacket = new TeamsPacket();
        updatePacket.teamName = teamName;
        updatePacket.action = TeamsPacket.Action.UPDATE_TEAM_INFO;
        updatePacket.teamDisplayName = teamDisplayName;
        updatePacket.friendlyFlags = friendlyFlags;
        updatePacket.nameTagVisibility = nameTagVisibility;
        updatePacket.collisionRule = collisionRule;
        updatePacket.teamColor = teamColor.getId();
        updatePacket.teamPrefix = prefix;
        updatePacket.teamSuffix = suffix;
        ByteBuf buffer = PacketUtils.writePacket(updatePacket);
        players.forEach(p -> p.getPlayerConnection().sendPacket(buffer));
    }
}
