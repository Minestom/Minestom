package net.minestom.server.scoreboard;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket.CollisionRule;
import net.minestom.server.network.packet.server.play.TeamsPacket.NameTagVisibility;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This object represents a team on a scoreboard that has a common display theme and other properties.
 */
public class Team implements PacketGroupingAudience {
    private static final byte ALLOW_FRIENDLY_FIRE_BIT = 0x01;
    private static final byte SEE_INVISIBLE_PLAYERS_BIT = 0x02;

    /**
     * A collection of all registered entities who are on the team.
     */
    private final Set<String> members;

    /**
     * The registry name of the team.
     */
    private final String teamName;
    /**
     * The display name of the team.
     */
    private Component teamDisplayName;
    /**
     * A BitMask.
     */
    private byte friendlyFlags;
    /**
     * The visibility of the team.
     */
    private NameTagVisibility nameTagVisibility;
    /**
     * The collision rule of the team.
     */
    private CollisionRule collisionRule;

    /**
     * Used to color the name of players on the team <br>
     * The color of a team defines how the names of the team members are visualized.
     */
    private NamedTextColor teamColor;

    /**
     * Shown before the names of the players who belong to this team.
     */
    private Component prefix;
    /**
     * Shown after the names of the player who belong to this team.
     */
    private Component suffix;

    private final Set<Player> playerMembers = ConcurrentHashMap.newKeySet();
    private boolean isPlayerMembersUpToDate;

    // Adventure
    private final Pointers pointers;

    /**
     * Default constructor to creates a team.
     *
     * @param teamName The registry name for the team
     */
    protected Team(@NotNull String teamName) {
        this.teamName = teamName;

        this.teamDisplayName = Component.empty();
        this.friendlyFlags = 0x00;
        this.nameTagVisibility = NameTagVisibility.ALWAYS;
        this.collisionRule = CollisionRule.ALWAYS;

        this.teamColor = NamedTextColor.WHITE;
        this.prefix = Component.empty();
        this.suffix = Component.empty();

        this.members = new CopyOnWriteArraySet<>();

        this.pointers = Pointers.builder()
                .withDynamic(Identity.NAME, this::getTeamName)
                .withDynamic(Identity.DISPLAY_NAME, this::getTeamDisplayName)
                .build();
    }

    /**
     * Adds a member to the {@link Team}.
     * <br>
     * This member can be a {@link Player} or an {@link LivingEntity}.
     *
     * @param member The member to be added
     */
    public void addMember(@NotNull String member) {
        // Adds a new member to the team
        this.members.add(member);

        // Initializes add player packet
        final TeamsPacket addPlayerPacket = new TeamsPacket(teamName,
                new TeamsPacket.AddEntitiesToTeamAction(members));
        // Sends to all online players the add player packet
        PacketUtils.broadcastPacket(addPlayerPacket);

        // invalidate player members
        this.isPlayerMembersUpToDate = false;
    }

    /**
     * Removes a member from the {@link Team}.
     *
     * @param member The member to be removed
     */
    public void removeMember(@NotNull String member) {
        // Initializes remove player packet
        final TeamsPacket removePlayerPacket = new TeamsPacket(teamName,
                new TeamsPacket.RemoveEntitiesToTeamAction(new String[]{member}));
        // Sends to all online player teh remove player packet
        PacketUtils.broadcastPacket(removePlayerPacket);

        // Removes the member from the team
        this.members.remove(member);

        // invalidate player members
        this.isPlayerMembersUpToDate = false;
    }

    /**
     * Changes the display name of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed <b>server side</b>.
     *
     * @param teamDisplayName The new display name
     */
    public void setTeamDisplayName(Component teamDisplayName) {
        this.teamDisplayName = teamDisplayName;
    }

    /**
     * Changes the display name of the team and sends an update packet.
     *
     * @param teamDisplayName The new display name
     */
    public void updateTeamDisplayName(Component teamDisplayName) {
        this.setTeamDisplayName(teamDisplayName);
        sendUpdatePacket();
    }

    /**
     * Changes the {@link NameTagVisibility} of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param visibility The new tag visibility
     * @see #updateNameTagVisibility(NameTagVisibility)
     */
    public void setNameTagVisibility(@NotNull NameTagVisibility visibility) {
        this.nameTagVisibility = visibility;
    }

    /**
     * Changes the {@link NameTagVisibility} of the team and sends an update packet.
     *
     * @param nameTagVisibility The new tag visibility
     */
    public void updateNameTagVisibility(@NotNull NameTagVisibility nameTagVisibility) {
        this.setNameTagVisibility(nameTagVisibility);
        sendUpdatePacket();
    }

    /**
     * Changes the {@link CollisionRule} of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param rule The new rule
     * @see #updateCollisionRule(CollisionRule)
     */
    public void setCollisionRule(@NotNull CollisionRule rule) {
        this.collisionRule = rule;
    }

    /**
     * Changes the collision rule of the team and sends an update packet.
     *
     * @param collisionRule The new collision rule
     */
    public void updateCollisionRule(@NotNull CollisionRule collisionRule) {
        this.setCollisionRule(collisionRule);
        sendUpdatePacket();
    }

    /**
     * Changes the color of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param color The new team color
     * @see #updateTeamColor(NamedTextColor)
     */
    public void setTeamColor(@NotNull NamedTextColor color) {
        this.teamColor = color;
    }

    /**
     * Changes the color of the team and sends an update packet.
     *
     * @param color The new team color
     */
    public void updateTeamColor(@NotNull NamedTextColor color) {
        this.setTeamColor(color);
        sendUpdatePacket();
    }

    /**
     * Changes the prefix of the team.
     * <br><br>
     * <b>Warning:</b> This is only changed on the <b>server side</b>.
     *
     * @param prefix The new prefix
     */
    public void setPrefix(Component prefix) {
        this.prefix = prefix;
    }

    /**
     * Changes the prefix of the team and sends an update packet.
     *
     * @param prefix The new prefix
     */
    public void updatePrefix(Component prefix) {
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
    public void setSuffix(Component suffix) {
        this.suffix = suffix;
    }

    /**
     * Changes the suffix of the team and sends an update packet.
     *
     * @param suffix The new suffix
     */
    public void updateSuffix(Component suffix) {
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

    private boolean getFriendlyFlagBit(byte index) {
        return (this.friendlyFlags & index) == index;
    }

    private void setFriendlyFlagBit(byte index, boolean value) {
        if (value) {
            this.friendlyFlags |= index;
        } else {
            this.friendlyFlags &= ~index;
        }
    }

    public void setAllowFriendlyFire(boolean value) {
        this.setFriendlyFlagBit(ALLOW_FRIENDLY_FIRE_BIT, value);
    }

    public void updateAllowFriendlyFire(boolean value) {
        this.setAllowFriendlyFire(value);
        this.sendUpdatePacket();
    }

    public boolean isAllowFriendlyFire() {
        return this.getFriendlyFlagBit(ALLOW_FRIENDLY_FIRE_BIT);
    }

    public void setSeeInvisiblePlayers(boolean value) {
        this.setFriendlyFlagBit(SEE_INVISIBLE_PLAYERS_BIT, value);
    }

    public void updateSeeInvisiblePlayers(boolean value) {
        this.setSeeInvisiblePlayers(value);
        this.sendUpdatePacket();
    }

    public boolean isSeeInvisiblePlayers() {
        return this.getFriendlyFlagBit(SEE_INVISIBLE_PLAYERS_BIT);
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
    public @NotNull TeamsPacket createTeamsCreationPacket() {
        final var info = new TeamsPacket.CreateTeamAction(teamDisplayName, friendlyFlags,
                nameTagVisibility, collisionRule, teamColor, prefix, suffix, members);
        return new TeamsPacket(teamName, info);
    }

    /**
     * Creates an destruction packet to remove the team.
     *
     * @return the packet to remove the team
     */
    public @NotNull TeamsPacket createTeamDestructionPacket() {
        return new TeamsPacket(teamName, new TeamsPacket.RemoveTeamAction());
    }

    /**
     * Obtains an unmodifiable {@link Set} of registered players who are on the team.
     *
     * @return an unmodifiable {@link Set} of registered players
     */
    public @NotNull Set<String> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Gets the display name of the team.
     *
     * @return the display name
     */
    public Component getTeamDisplayName() {
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
    public @NotNull NameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    /**
     * Gets the collision rule of the team.
     *
     * @return the collision rule
     */
    public @NotNull CollisionRule getCollisionRule() {
        return collisionRule;
    }

    /**
     * Gets the color of the team.
     *
     * @return the team color
     */
    public @NotNull NamedTextColor getTeamColor() {
        return teamColor;
    }

    /**
     * Gets the prefix of the team.
     *
     * @return the team prefix
     */
    public Component getPrefix() {
        return prefix;
    }

    /**
     * Gets the suffix of the team.
     *
     * @return the suffix team
     */
    public Component getSuffix() {
        return suffix;
    }

    /**
     * Sends an {@link TeamsPacket.UpdateTeamAction} action packet.
     */
    public void sendUpdatePacket() {
        final var info = new TeamsPacket.UpdateTeamAction(teamDisplayName, friendlyFlags,
                nameTagVisibility, collisionRule, teamColor, prefix, suffix);
        PacketUtils.broadcastPacket(new TeamsPacket(teamName, info));
    }

    @Override
    public @NotNull Collection<Player> getPlayers() {
        if (!this.isPlayerMembersUpToDate) {
            this.playerMembers.clear();

            for (String member : this.members) {
                Player player = MinecraftServer.getConnectionManager().getPlayer(member);

                if (player != null) {
                    this.playerMembers.add(player);
                }
            }

            this.isPlayerMembersUpToDate = true;
        }

        return this.playerMembers;
    }

    @Override
    public @NotNull Pointers pointers() {
        return this.pointers;
    }
}
