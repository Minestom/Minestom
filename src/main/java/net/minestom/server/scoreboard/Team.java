package net.minestom.server.scoreboard;

import io.netty.buffer.ByteBuf;
import net.minestom.server.MinecraftServer;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.PacketWriterUtils;
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

    /**
     * A collection of all registered entities who are on the team
     */
    private final Set<String> members;
    /**
     * Creation packet for the team to create
     */
    private final TeamsPacket teamsCreationPacket;
    /**
     * A byte buf to destroy the team
     */
    private final ByteBuf teamsDestroyPacket;

    /**
     * The registry name of the team
     */
    private final String teamName;
    /**
     * The display name of the team
     */
    private ColoredText teamDisplayName;
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
    private ColoredText prefix;
    /**
     * Shown after the names of the player who belong to this team
     */
    private ColoredText suffix;

    /**
     * Identifiers for the entities in this team
     */
    private String[] entities;

    /**
     * Default constructor to creates a team
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

        // Initializes creation packet
        this.teamsCreationPacket = new TeamsPacket();
        this.teamsCreationPacket.teamName = teamName;
        this.teamsCreationPacket.action = TeamsPacket.Action.CREATE_TEAM;
        this.teamsCreationPacket.teamDisplayName = this.teamDisplayName.toString();
        this.teamsCreationPacket.friendlyFlags = this.friendlyFlags;
        this.teamsCreationPacket.nameTagVisibility = this.nameTagVisibility;
        this.teamsCreationPacket.collisionRule = this.collisionRule;
        this.teamsCreationPacket.teamColor = this.teamColor.getId();
        this.teamsCreationPacket.teamPrefix = this.prefix.toString();
        this.teamsCreationPacket.teamSuffix = this.suffix.toString();
        this.teamsCreationPacket.entities = this.entities;

        // Directly write packet since it will not change
        this.teamsDestroyPacket = PacketUtils.writePacket(this.createTeamDestructionPacket());
    }

    /**
     * Adds a member to the {@link Team}
     * <br>
     * This member can be a {@link Player} or an {@link LivingEntity}
     *
     * @param member The member to be added
     */
    public void addMember(String member) {
        final String[] entitiesCache = new String[this.entities.length + 1];
        System.arraycopy(this.entities, 0, entitiesCache, 0, this.entities.length);
        entitiesCache[this.entities.length] = member;
        this.entities = entitiesCache;
        this.teamsCreationPacket.entities = this.entities;

        // Adds a new member to the team
        this.members.add(member);

        // Initializes add player packet
        final TeamsPacket addPlayerPacket = new TeamsPacket();
        addPlayerPacket.teamName = this.teamName;
        addPlayerPacket.action = TeamsPacket.Action.ADD_PLAYERS_TEAM;
        addPlayerPacket.entities = new String[]{member};
        // Sends to all online players the add player packet
        PacketWriterUtils.writeAndSend(MinecraftServer.getConnectionManager().getOnlinePlayers(), addPlayerPacket);
    }

    /**
     * Removes a member from the {@link Team}
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
        PacketWriterUtils.writeAndSend(MinecraftServer.getConnectionManager().getOnlinePlayers(), removePlayerPacket);

        // Removes the player from the
        this.members.remove(member);

        final String[] entitiesCache = new String[this.entities.length - 1];
        int count = 0;
        for (String teamMember : this.members) {
            entitiesCache[count++] = teamMember;
        }
        this.entities = entitiesCache;
        this.teamsCreationPacket.entities = this.entities;
    }

    /**
     * Change the display name of the team
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>
     *
     * @param teamDisplayName The new display name
     */
    public void setTeamDisplayName(ColoredText teamDisplayName) {
        this.teamDisplayName = teamDisplayName;
        this.teamsCreationPacket.teamDisplayName = teamDisplayName.toString();
    }

    /**
     * Change the display name of the team and sends an update packet
     *
     * @param teamDisplayName The new display name
     */
    public void updateTeamDisplayName(ColoredText teamDisplayName) {
        this.setTeamDisplayName(teamDisplayName);
        sendUpdatePacket();
    }

    /**
     * Change the {@link NameTagVisibility} of the team
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>
     *
     * @param visibility The new tag visibility
     */
    public void setNameTagVisibility(NameTagVisibility visibility) {
        this.nameTagVisibility = visibility;
        this.teamsCreationPacket.nameTagVisibility = visibility;
    }

    /**
     * Change the {@link NameTagVisibility} of the team and sends an update packet
     *
     * @param nameTagVisibility The new tag visibility
     */
    public void updateNameTagVisibility(NameTagVisibility nameTagVisibility) {
        this.setNameTagVisibility(nameTagVisibility);
        sendUpdatePacket();
    }

    /**
     * Change the {@link CollisionRule} of the team
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>
     *
     * @param rule The new rule
     */
    public void setCollisionRule(CollisionRule rule) {
        this.collisionRule = rule;
        this.teamsCreationPacket.collisionRule = rule;
    }

    /**
     * Change the collision rule of the team and sends an update packet
     *
     * @param collisionRule The new collision rule
     */
    public void updateCollisionRule(CollisionRule collisionRule) {
        this.setCollisionRule(collisionRule);
        sendUpdatePacket();
    }

    /**
     * Change the color of the team
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>
     *
     * @param color The new team color
     */
    public void setTeamColor(ChatColor color) {
        this.teamColor = color;
        this.teamsCreationPacket.teamColor = color.getId();
    }

    /**
     * Change the color of the team and sends an update packet
     *
     * @param teamColor The new team color
     */
    public void updateTeamColor(ChatColor teamColor) {
        this.setTeamColor(teamColor);
        sendUpdatePacket();
    }

    /**
     * Change the prefix of the team
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>
     *
     * @param prefix The new prefix
     */
    public void setPrefix(ColoredText prefix) {
        this.prefix = prefix;
        this.teamsCreationPacket.teamPrefix = prefix.toString();
    }

    /**
     * Change the prefix of the team and sends an update packet
     *
     * @param prefix The new prefix
     */
    public void updatePrefix(ColoredText prefix) {
        this.setPrefix(prefix);
        sendUpdatePacket();
    }

    /**
     * Change the suffix of the team
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>
     *
     * @param suffix The new suffix
     */
    public void setSuffix(ColoredText suffix) {
        this.suffix = suffix;
        this.teamsCreationPacket.teamSuffix = suffix.toString();
    }

    /**
     * Change the suffix of the team and sends an update packet
     *
     * @param suffix The new suffix
     */
    public void updateSuffix(ColoredText suffix) {
        this.setSuffix(suffix);
        sendUpdatePacket();
    }

    /**
     * Change the friendly flags of the team
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>
     *
     * @param flag The new friendly flag
     */
    public void setFriendlyFlags(byte flag) {
        this.friendlyFlags = flag;
        this.teamsCreationPacket.friendlyFlags = flag;
    }

    /**
     * Change the friendly flags of the team and sends an update packet
     *
     * @param flag The new friendly flag
     */
    public void updateFriendlyFlags(byte flag) {
        this.setFriendlyFlags(flag);
        this.sendUpdatePacket();
    }

    /**
     * Gets the registry name of the team
     *
     * @return the registry name
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Gets the creation packet to add a team
     *
     * @return the packet to add the team
     */
    public TeamsPacket getTeamsCreationPacket() {
        return teamsCreationPacket;
    }

    public ByteBuf getTeamsDestroyPacket() {
        return teamsDestroyPacket;
    }

    /**
     * Creates an destruction packet to remove the team
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
     * Obtains an unmodifiable {@link Set} of registered players who are on the team
     *
     * @return an unmodifiable {@link Set} of registered players
     */
    public Set<String> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Gets the display name of the team
     *
     * @return the display name
     */
    public ColoredText getTeamDisplayName() {
        return teamDisplayName;
    }

    /**
     * Gets the friendly flags of the team
     *
     * @return the friendly flags
     */
    public byte getFriendlyFlags() {
        return friendlyFlags;
    }

    /**
     * Gets the tag visibility of the team
     *
     * @return the tag visibility
     */
    public NameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    /**
     * Gets the collision rule of the team
     *
     * @return the collision rule
     */
    public CollisionRule getCollisionRule() {
        return collisionRule;
    }

    /**
     * Gets the color of the team
     *
     * @return the team color
     */
    public ChatColor getTeamColor() {
        return teamColor;
    }

    /**
     * Gets the prefix of the team
     *
     * @return the team prefix
     */
    public ColoredText getPrefix() {
        return prefix;
    }

    /**
     * Gets the suffix of the team
     *
     * @return the suffix team
     */
    public ColoredText getSuffix() {
        return suffix;
    }

    public String[] getEntities() {
        return entities;
    }

    /**
     * Sends an {@link TeamsPacket.Action#UPDATE_TEAM_INFO} packet
     */
    public void sendUpdatePacket() {
        final TeamsPacket updatePacket = new TeamsPacket();
        updatePacket.teamName = this.teamName;
        updatePacket.action = TeamsPacket.Action.UPDATE_TEAM_INFO;
        updatePacket.teamDisplayName = this.teamDisplayName.toString();
        updatePacket.friendlyFlags = this.friendlyFlags;
        updatePacket.nameTagVisibility = this.nameTagVisibility;
        updatePacket.collisionRule = this.collisionRule;
        updatePacket.teamColor = this.teamColor.getId();
        updatePacket.teamPrefix = this.prefix.toString();
        updatePacket.teamSuffix = this.suffix.toString();
        ByteBuf buffer = PacketUtils.writePacket(updatePacket);
        for (Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            onlinePlayer.getPlayerConnection().sendPacket(buffer, true);
        }
    }
}
