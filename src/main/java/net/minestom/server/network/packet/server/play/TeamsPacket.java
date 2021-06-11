package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * The packet creates or updates teams
 */
public class TeamsPacket implements ComponentHoldingServerPacket {

    /**
     * The registry name of the team
     */
    public String teamName;
    /**
     * The action of the packet
     */
    public Action action;

    /**
     * The display name for the team
     */
    public Component teamDisplayName;
    /**
     * The friendly flags to
     */
    public byte friendlyFlags;
    /**
     * Visibility state for the name tag
     */
    public NameTagVisibility nameTagVisibility;
    /**
     * Rule for the collision
     */
    public CollisionRule collisionRule;
    /**
     * The color of the team
     */
    public NamedTextColor teamColor;
    /**
     * The prefix of the team
     */
    public Component teamPrefix;
    /**
     * The suffix of the team
     */
    public Component teamSuffix;
    /**
     * An array with all entities in the team
     */
    public String[] entities;

    public TeamsPacket() {
        teamName = "";
        action = Action.REMOVE_TEAM;
    }

    /**
     * Writes data into the {@link BinaryWriter}
     *
     * @param writer The writer to writes
     */
    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(this.teamName);
        writer.writeByte((byte) this.action.ordinal());

        switch (action) {
            case CREATE_TEAM:
            case UPDATE_TEAM_INFO:
                writer.writeComponent(this.teamDisplayName);
                writer.writeByte(this.friendlyFlags);
                writer.writeSizedString(this.nameTagVisibility.getIdentifier());
                writer.writeSizedString(this.collisionRule.getIdentifier());
                writer.writeVarInt(AdventurePacketConvertor.getNamedTextColorValue(this.teamColor));
                writer.writeComponent(this.teamPrefix);
                writer.writeComponent(this.teamSuffix);
                break;
            case REMOVE_TEAM:

                break;
        }

        if (action == Action.CREATE_TEAM || action == Action.ADD_PLAYERS_TEAM || action == Action.REMOVE_PLAYERS_TEAM) {
            if (entities == null || entities.length == 0) {
                writer.writeVarInt(0); // Empty
            } else {
                writer.writeStringArray(entities);
            }
        }

    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        teamName = reader.readSizedString();
        action = Action.values()[reader.readByte()];

        switch (action) {
            case CREATE_TEAM:
            case UPDATE_TEAM_INFO:
                this.teamDisplayName = reader.readComponent();
                this.friendlyFlags = reader.readByte();
                nameTagVisibility = NameTagVisibility.fromIdentifier(reader.readSizedString());
                collisionRule = CollisionRule.fromIdentifier(reader.readSizedString());
                this.teamColor = NamedTextColor.ofExact(reader.readVarInt());
                this.teamPrefix = reader.readComponent();
                this.teamSuffix = reader.readComponent();
                break;
            case REMOVE_TEAM:

                break;
        }

        if (action == Action.CREATE_TEAM || action == Action.ADD_PLAYERS_TEAM || action == Action.REMOVE_PLAYERS_TEAM) {
            entities = reader.readSizedStringArray();
        }
    }

    /**
     * Gets the identifier of the packet
     *
     * @return the identifier
     */
    @Override
    public int getId() {
        return ServerPacketIdentifier.TEAMS;
    }

    @Override
    public @NotNull Collection<Component> components() {
        if (this.action == Action.UPDATE_TEAM_INFO || this.action == Action.CREATE_TEAM) {
            return List.of(teamDisplayName, teamPrefix, teamSuffix);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        if (this.action == Action.UPDATE_TEAM_INFO || this.action == Action.CREATE_TEAM) {
            TeamsPacket packet = new TeamsPacket();
            packet.teamName = teamName;
            packet.action = action;
            packet.teamDisplayName = teamDisplayName == null ? null : operator.apply(teamDisplayName);
            packet.friendlyFlags = friendlyFlags;
            packet.nameTagVisibility = nameTagVisibility;
            packet.collisionRule = collisionRule;
            packet.teamColor = teamColor;
            packet.teamPrefix = teamPrefix == null ? null : operator.apply(teamPrefix);
            packet.teamSuffix = teamSuffix == null ? null : operator.apply(teamSuffix);
            packet.entities = entities;
            return packet;
        } else {
            return this;
        }
    }

    /**
     * An enumeration which representing all actions for the packet
     */
    public enum Action {
        /**
         * An action to create a new team
         */
        CREATE_TEAM,
        /**
         * An action to remove a team
         */
        REMOVE_TEAM,
        /**
         * An action to update the team information
         */
        UPDATE_TEAM_INFO,
        /**
         * An action to add player to the team
         */
        ADD_PLAYERS_TEAM,
        /**
         * An action to remove player from the team
         */
        REMOVE_PLAYERS_TEAM
    }

    /**
     * An enumeration which representing all visibility states for the name tags
     */
    public enum NameTagVisibility {
        /**
         * The name tag is visible
         */
        ALWAYS("always"),
        /**
         * Hides the name tag for other teams
         */
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        /**
         * Hides the name tag for the own team
         */
        HIDE_FOR_OWN_TEAM("hideForOwnTeam"),
        /**
         * The name tag is invisible
         */
        NEVER("never");

        /**
         * The identifier for the client
         */
        private final String identifier;

        /**
         * Default constructor
         *
         * @param identifier The client identifier
         */
        NameTagVisibility(String identifier) {
            this.identifier = identifier;
        }

        @NotNull
        public static NameTagVisibility fromIdentifier(String identifier) {
            for (NameTagVisibility v : values()) {
                if (v.getIdentifier().equals(identifier))
                    return v;
            }
            Check.fail("Identifier for NameTagVisibility is invalid: " + identifier);
            return null;
        }

        /**
         * Gets the client identifier
         *
         * @return the identifier
         */
        @NotNull
        public String getIdentifier() {
            return identifier;
        }
    }

    /**
     * An enumeration which representing all rules for the collision
     */
    public enum CollisionRule {
        /**
         * Can push all objects and can be pushed by all objects
         */
        ALWAYS("always"),
        /**
         * Can push objects of other teams, but teammates cannot
         */
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        /**
         * Can only push objects of the same team
         */
        PUSH_OWN_TEAM("pushOwnTeam"),
        /**
         * Cannot push an object, but neither can they be pushed
         */
        NEVER("never");

        /**
         * The identifier for the client
         */
        private final String identifier;

        /**
         * Default constructor
         *
         * @param identifier The identifier for the client
         */
        CollisionRule(String identifier) {
            this.identifier = identifier;
        }

        @NotNull
        public static CollisionRule fromIdentifier(String identifier) {
            for (CollisionRule v : values()) {
                if (v.getIdentifier().equals(identifier))
                    return v;
            }
            Check.fail("Identifier for CollisionRule is invalid: " + identifier);
            return null;
        }

        /**
         * Gets the identifier of the rule
         *
         * @return the identifier
         */
        @NotNull
        public String getIdentifier() {
            return identifier;
        }
    }

}
