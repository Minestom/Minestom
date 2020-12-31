package net.minestom.server.scoreboard;

import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket.CollisionRule;
import net.minestom.server.network.packet.server.play.TeamsPacket.NameTagVisibility;
import net.minestom.server.utils.PacketUtils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This object represents a team on a scoreboard that has a common display theme and other properties.
 */
public class Team {

    private static final ConnectionManager CONNECTION_MANAGER = MinecraftServer.getConnectionManager();

    /**
     * A collection of all registered entities who are on the team
     */
    private final Set<String> members;

    /**
     * The registry name of the team
     */
    private final String teamName;
    /**
     * The display name of the team
     */
    private JsonMessage teamDisplayName;
    /**
     * A BitMask
     */
    private byte friendlyFlags;
    /**
     * The visibility of the team
     */
    private NameTagVisibility nameTagVisibility;
    /**
     * The collision rule of the team
     */
    private CollisionRule collisionRule;

    /**
     * Used to color the name of players on the team <br>
     * The color of a team defines how the names of the team members are visualized
     */
    private ChatColor teamColor;

    /**
     * Shown before the names of the players who belong to this team
     */
    private JsonMessage prefix;
    /**
     * Shown after the names of the player who belong to this team
     */
    private JsonMessage suffix;

    /**
     * Identifiers for the entities in this team
     */
    private String[] entities;

    /**
     * Default constructor to creates a team.
     *
     * @param teamName The registry name for the team
     */
    protected Team(String teamName) {
        this.teamName = teamName;

        this.teamDisplayName = ColoredText.of("");
        this.friendlyFlags = 0x00;
        this.nameTagVisibility = NameTagVisibility.ALWAYS;
        this.collisionRule = CollisionRule.ALWAYS;

        this.teamColor = ChatColor.WHITE;
        this.prefix = ColoredText.of("");
        this.suffix = ColoredText.of("");

        this.entities = new String[0];
        this.members = new CopyOnWriteArraySet<>();
    }

    /**
     * Adds a member to the {@link Team}.
     * <br>
     * This member can be a {@link Player} or an {@link LivingEntity}.
     *
     * @param member The member to be added
     */
    public void addMember(String member) {
        final String[] entitiesCache = new String[this.entities.length + 1];
        System.arraycopy(this.entities, 0, entitiesCache, 0, this.entities.length);
        entitiesCache[this.entities.length] = member;
        this.entities = entitiesCache;

        // Adds a new member to the team
        this.members.add(member);

        // Initializes add player packet
        final TeamsPacket addPlayerPacket = new TeamsPacket();
        addPlayerPacket.teamName = this.teamName;
        addPlayerPacket.action = TeamsPacket.Action.ADD_PLAYERS_TEAM;
        addPlayerPacket.entities = new String[]{member};

        // Sends to all online players the add player packet
        PacketUtils.sendGroupedPacket(CONNECTION_MANAGER.getOnlinePlayers(), addPlayerPacket);
    }

    /**
     * Removes a member from the {@link Team}.
     *
     * @param member The member to be removed
     */
    public void removeMember(String member) {
        // Initializes remove player packet
        final TeamsPacket removePlayerPacket = new TeamsPacket();
        removePlayerPacket.teamName = this.teamName;
        removePlayerPacket.action = TeamsPacket.Action.REMOVE_PLAYERS_TEAM;
        removePlayerPacket.entities = new String[]{member};
        // Sends to all online player teh remove player packet
        PacketUtils.sendGroupedPacket(CONNECTION_MANAGER.getOnlinePlayers(), removePlayerPacket);

        // Removes the player from the
        this.members.remove(member);

        final String[] entitiesCache = new String[this.entities.length - 1];
        int count = 0;
        for (String teamMember : this.members) {
            entitiesCache[count++] = teamMember;
        }
        this.entities = entitiesCache;
    }

    /**
     * Changes the display name of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param teamDisplayName The new display name
     */
    public void setTeamDisplayName(JsonMessage teamDisplayName) {
        this.teamDisplayName = teamDisplayName;
    }

    /**
     * Changes the display name of the team and sends an update packet.
     *
     * @param teamDisplayName The new display name
     */
    public void updateTeamDisplayName(JsonMessage teamDisplayName) {
        this.setTeamDisplayName(teamDisplayName);
        sendUpdatePacket();
    }

    /**
     * Changes the {@link NameTagVisibility} of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param visibility The new tag visibility
     */
    public void setNameTagVisibility(NameTagVisibility visibility) {
        this.nameTagVisibility = visibility;
    }

    /**
     * Changes the {@link NameTagVisibility} of the team and sends an update packet.
     *
     * @param nameTagVisibility The new tag visibility
     */
    public void updateNameTagVisibility(NameTagVisibility nameTagVisibility) {
        this.setNameTagVisibility(nameTagVisibility);
        sendUpdatePacket();
    }

    /**
     * Changes the {@link CollisionRule} of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param rule The new rule
     */
    public void setCollisionRule(CollisionRule rule) {
        this.collisionRule = rule;
    }

    /**
     * Changes the collision rule of the team and sends an update packet.
     *
     * @param collisionRule The new collision rule
     */
    public void updateCollisionRule(CollisionRule collisionRule) {
        this.setCollisionRule(collisionRule);
        sendUpdatePacket();
    }

    /**
     * Changes the color of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param color The new team color
     */
    public void setTeamColor(ChatColor color) {
        this.teamColor = color;
    }

    /**
     * Changes the color of the team and sends an update packet.
     *
     * @param teamColor The new team color
     */
    public void updateTeamColor(ChatColor teamColor) {
        this.setTeamColor(teamColor);
        sendUpdatePacket();
    }

    /**
     * Changes the prefix of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param prefix The new prefix
     */
    public void setPrefix(JsonMessage prefix) {
        this.prefix = prefix;
    }

    /**
     * Changes the prefix of the team and sends an update packet.
     *
     * @param prefix The new prefix
     */
    public void updatePrefix(JsonMessage prefix) {
        this.setPrefix(prefix);
        sendUpdatePacket();
    }

    /**
     * Changes the suffix of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param suffix The new suffix
     */
    public void setSuffix(JsonMessage suffix) {
        this.suffix = suffix;
    }

    /**
     * Changes the suffix of the team and sends an update packet.
     *
     * @param suffix The new suffix
     */
    public void updateSuffix(JsonMessage suffix) {
        this.setSuffix(suffix);
        sendUpdatePacket();
    }

    /**
     * Changes the friendly flags of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param flag The new friendly flag
     */
    public void setFriendlyFlags(byte flag) {
        this.friendlyFlags = flag;
    }

    /**
     * Changes the friendly flags of the team and sends an update packet.
     *
     * @param flag The new friendly flag
     */
    public void updateFriendlyFlags(byte flag) {
        this.setFriendlyFlags(flag);
        this.sendUpdatePacket();
    }

    /**
     * Gets the registry name of the team.
     *
     * @return the registry name
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Creates the creation packet to add a team.
     *
     * @return the packet to add the team
     */
    public TeamsPacket createTeamsCreationPacket() {
        TeamsPacket teamsCreationPacket = new TeamsPacket();
        teamsCreationPacket.teamName = teamName;
        teamsCreationPacket.action = TeamsPacket.Action.CREATE_TEAM;
        teamsCreationPacket.teamDisplayName = this.teamDisplayName;
        teamsCreationPacket.friendlyFlags = this.friendlyFlags;
        teamsCreationPacket.nameTagVisibility = this.nameTagVisibility;
        teamsCreationPacket.collisionRule = this.collisionRule;
        teamsCreationPacket.teamColor = this.teamColor.getId();
        teamsCreationPacket.teamPrefix = this.prefix;
        teamsCreationPacket.teamSuffix = this.suffix;
        teamsCreationPacket.entities = this.entities;

        return teamsCreationPacket;
    }

    /**
     * Creates an destruction packet to remove the team.
     *
     * @return the packet to remove the team
     */
    public TeamsPacket createTeamDestructionPacket() {
        TeamsPacket teamsPacket = new TeamsPacket();
        teamsPacket.teamName = teamName;
        teamsPacket.action = TeamsPacket.Action.REMOVE_TEAM;
        return teamsPacket;
    }

    /**
     * Obtains an unmodifiable {@link Set} of registered players who are on the team.
     *
     * @return an unmodifiable {@link Set} of registered players
     */
    public Set<String> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Gets the display name of the team.
     *
     * @return the display name
     */
    public JsonMessage getTeamDisplayName() {
        return teamDisplayName;
    }

    /**
     * Gets the friendly flags of the team.
     *
     * @return the friendly flags
     */
    public byte getFriendlyFlags() {
        return friendlyFlags;
    }

    /**
     * Gets the tag visibility of the team.
     *
     * @return the tag visibility
     */
    public NameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    /**
     * Gets the collision rule of the team.
     *
     * @return the collision rule
     */
    public CollisionRule getCollisionRule() {
        return collisionRule;
    }

    /**
     * Gets the color of the team.
     *
     * @return the team color
     */
    public ChatColor getTeamColor() {
        return teamColor;
    }

    /**
     * Gets the prefix of the team.
     *
     * @return the team prefix
     */
    public JsonMessage getPrefix() {
        return prefix;
    }

    /**
     * Gets the suffix of the team.
     *
     * @return the suffix team
     */
    public JsonMessage getSuffix() {
        return suffix;
    }

    public String[] getEntities() {
        return entities;
    }

    /**
     * Sends an {@link TeamsPacket.Action#UPDATE_TEAM_INFO} packet.
     */
    public void sendUpdatePacket() {
        final TeamsPacket updatePacket = new TeamsPacket();
        updatePacket.teamName = this.teamName;
        updatePacket.action = TeamsPacket.Action.UPDATE_TEAM_INFO;
        updatePacket.teamDisplayName = this.teamDisplayName;
        updatePacket.friendlyFlags = this.friendlyFlags;
        updatePacket.nameTagVisibility = this.nameTagVisibility;
        updatePacket.collisionRule = this.collisionRule;
        updatePacket.teamColor = this.teamColor.getId();
        updatePacket.teamPrefix = this.prefix;
        updatePacket.teamSuffix = this.suffix;

        PacketUtils.sendGroupedPacket(MinecraftServer.getConnectionManager().getOnlinePlayers(), updatePacket);
    }
}
