package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * The packet creates or updates teams
 */
public record TeamsPacket(String teamName, Action action) implements ComponentHoldingServerPacket {
    public TeamsPacket(BinaryReader reader) {
        this(reader.readSizedString(), switch (reader.readByte()) {
            case 0 -> new CreateTeamAction(reader);
            case 1 -> new RemoveTeamAction();
            case 2 -> new UpdateTeamAction(reader);
            case 3 -> new AddEntitiesToTeamAction(reader);
            case 4 -> new RemoveEntitiesToTeamAction(reader);
            default -> throw new RuntimeException("Unknown action id");
        });
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(teamName);
        writer.writeByte((byte) action.id());
        writer.write(action);
    }

    @Override
    public @NotNull Collection<Component> components() {
        return this.action instanceof ComponentHolder<?> holder ? holder.components() : List.of();
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new TeamsPacket(
                this.teamName,
                this.action instanceof ComponentHolder<?> holder
                        ? (Action) holder.copyWithOperator(operator)
                        : this.action
        );
    }

    public sealed interface Action extends Writeable
            permits CreateTeamAction, RemoveTeamAction, UpdateTeamAction, AddEntitiesToTeamAction, RemoveEntitiesToTeamAction {
        int id();
    }

    public record CreateTeamAction(Component displayName, byte friendlyFlags,
                                   NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
                                   NamedTextColor teamColor, Component teamPrefix, Component teamSuffix,
                                   Collection<String> entities) implements Action, ComponentHolder<CreateTeamAction> {
        public CreateTeamAction {
            entities = List.copyOf(entities);
        }

        public CreateTeamAction(BinaryReader reader) {
            this(reader.readComponent(), reader.readByte(),
                    NameTagVisibility.fromIdentifier(reader.readSizedString()), CollisionRule.fromIdentifier(reader.readSizedString()),
                    NamedTextColor.ofExact(reader.readVarInt()), reader.readComponent(), reader.readComponent(),
                    reader.readVarIntList(BinaryReader::readSizedString));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeComponent(displayName);
            writer.writeByte(friendlyFlags);
            writer.writeSizedString(nameTagVisibility.getIdentifier());
            writer.writeSizedString(collisionRule.getIdentifier());
            writer.writeVarInt(AdventurePacketConvertor.getNamedTextColorValue(teamColor));
            writer.writeComponent(teamPrefix);
            writer.writeComponent(teamSuffix);
            writer.writeVarIntList(entities, BinaryWriter::writeSizedString);
        }

        @Override
        public int id() {
            return 0;
        }

        @Override
        public @NotNull Collection<Component> components() {
            return List.of(this.displayName, this.teamPrefix, this.teamSuffix);
        }

        @Override
        public @NotNull CreateTeamAction copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return new CreateTeamAction(
                    operator.apply(this.displayName),
                    this.friendlyFlags,
                    this.nameTagVisibility,
                    this.collisionRule,
                    this.teamColor,
                    operator.apply(this.teamPrefix),
                    operator.apply(this.teamSuffix),
                    entities
            );
        }
    }

    public record RemoveTeamAction() implements Action {
        @Override
        public void write(@NotNull BinaryWriter writer) {
        }

        @Override
        public int id() {
            return 1;
        }
    }

    public record UpdateTeamAction(Component displayName, byte friendlyFlags,
                                   NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
                                   NamedTextColor teamColor,
                                   Component teamPrefix, Component teamSuffix) implements Action, ComponentHolder<UpdateTeamAction> {

        public UpdateTeamAction(BinaryReader reader) {
            this(reader.readComponent(), reader.readByte(),
                    NameTagVisibility.fromIdentifier(reader.readSizedString()), CollisionRule.fromIdentifier(reader.readSizedString()),
                    NamedTextColor.ofExact(reader.readVarInt()),
                    reader.readComponent(), reader.readComponent());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeComponent(displayName);
            writer.writeByte(friendlyFlags);
            writer.writeSizedString(nameTagVisibility.getIdentifier());
            writer.writeSizedString(collisionRule.getIdentifier());
            writer.writeVarInt(AdventurePacketConvertor.getNamedTextColorValue(teamColor));
            writer.writeComponent(teamPrefix);
            writer.writeComponent(teamSuffix);
        }

        @Override
        public int id() {
            return 2;
        }

        @Override
        public @NotNull Collection<Component> components() {
            return List.of(this.displayName, this.teamPrefix, this.teamSuffix);
        }

        @Override
        public @NotNull UpdateTeamAction copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return new UpdateTeamAction(
                    operator.apply(this.displayName),
                    this.friendlyFlags,
                    this.nameTagVisibility,
                    this.collisionRule,
                    this.teamColor,
                    operator.apply(this.teamPrefix),
                    operator.apply(this.teamSuffix)
            );
        }
    }

    public record AddEntitiesToTeamAction(Collection<String> entities) implements Action {
        public AddEntitiesToTeamAction {
            entities = List.copyOf(entities);
        }

        public AddEntitiesToTeamAction(BinaryReader reader) {
            this(reader.readVarIntList(BinaryReader::readSizedString));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarIntList(entities, BinaryWriter::writeSizedString);
        }

        @Override
        public int id() {
            return 3;
        }
    }

    public record RemoveEntitiesToTeamAction(String[] entities) implements Action {
        public RemoveEntitiesToTeamAction(BinaryReader reader) {
            this(reader.readSizedStringArray());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeStringArray(entities);
        }

        @Override
        public int id() {
            return 4;
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

        public static @NotNull CollisionRule fromIdentifier(String identifier) {
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
        public @NotNull String getIdentifier() {
            return identifier;
        }
    }
}
