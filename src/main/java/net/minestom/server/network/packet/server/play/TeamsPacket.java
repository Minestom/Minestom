package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.adventure.AdventurePacketConvertor;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * The packet creates or updates teams
 */
public record TeamsPacket(String teamName, Action action) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final int MAX_MEMBERS = 16384;

    public static final NetworkBuffer.Type<TeamsPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, TeamsPacket value) {
            buffer.write(STRING, value.teamName);
            buffer.write(BYTE, (byte) value.action.id());
            @SuppressWarnings("unchecked") final Type<Action> type = (Type<Action>) actionSerializer(value.action.id());
            buffer.write(type, value.action);
        }

        @Override
        public @NotNull TeamsPacket read(@NotNull NetworkBuffer buffer) {
            final String teamName = buffer.read(STRING);
            final byte actionId = buffer.read(BYTE);
            final var type = actionSerializer(actionId);
            return new TeamsPacket(teamName, type.read(buffer));
        }
    };

    @Override
    public @NotNull Collection<Component> components() {
        return this.action instanceof ComponentHolder<?> holder ? holder.components() : List.of();
    }

    private static Type<? extends Action> actionSerializer(int id) {
        return switch (id) {
            case 0 -> CreateTeamAction.SERIALIZER;
            case 1 -> RemoveTeamAction.SERIALIZER;
            case 2 -> UpdateTeamAction.SERIALIZER;
            case 3 -> AddEntitiesToTeamAction.SERIALIZER;
            case 4 -> RemoveEntitiesToTeamAction.SERIALIZER;
            default -> throw new RuntimeException("Unknown action id");
        };
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

    public sealed interface Action permits CreateTeamAction, RemoveTeamAction, UpdateTeamAction, AddEntitiesToTeamAction, RemoveEntitiesToTeamAction {
        int id();
    }

    public record CreateTeamAction(Component displayName, byte friendlyFlags,
                                   NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
                                   NamedTextColor teamColor, Component teamPrefix, Component teamSuffix,
                                   List<String> entities) implements Action, ComponentHolder<CreateTeamAction> {
        public CreateTeamAction {
            entities = List.copyOf(entities);
        }

        public static final NetworkBuffer.Type<CreateTeamAction> SERIALIZER = new Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, CreateTeamAction value) {
                buffer.write(COMPONENT, value.displayName);
                buffer.write(BYTE, value.friendlyFlags);
                buffer.write(STRING, value.nameTagVisibility.getIdentifier());
                buffer.write(STRING, value.collisionRule.getIdentifier());
                buffer.write(VAR_INT, AdventurePacketConvertor.getNamedTextColorValue(value.teamColor));
                buffer.write(COMPONENT, value.teamPrefix);
                buffer.write(COMPONENT, value.teamSuffix);
                buffer.write(STRING.list(), value.entities);
            }

            @Override
            public CreateTeamAction read(@NotNull NetworkBuffer buffer) {
                return new CreateTeamAction(buffer.read(COMPONENT), buffer.read(BYTE),
                        NameTagVisibility.fromIdentifier(buffer.read(STRING)), CollisionRule.fromIdentifier(buffer.read(STRING)),
                        NamedTextColor.namedColor(buffer.read(VAR_INT)), buffer.read(COMPONENT), buffer.read(COMPONENT),
                        buffer.read(STRING.list(MAX_MEMBERS)));
            }
        };

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
        public static final NetworkBuffer.Type<RemoveTeamAction> SERIALIZER = NetworkBufferTemplate.template(RemoveTeamAction::new);

        @Override
        public int id() {
            return 1;
        }
    }

    public record UpdateTeamAction(Component displayName, byte friendlyFlags,
                                   NameTagVisibility nameTagVisibility, CollisionRule collisionRule,
                                   NamedTextColor teamColor,
                                   Component teamPrefix,
                                   Component teamSuffix) implements Action, ComponentHolder<UpdateTeamAction> {

        public static final NetworkBuffer.Type<UpdateTeamAction> SERIALIZER = new Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, UpdateTeamAction value) {
                buffer.write(COMPONENT, value.displayName);
                buffer.write(BYTE, value.friendlyFlags);
                buffer.write(STRING, value.nameTagVisibility.getIdentifier());
                buffer.write(STRING, value.collisionRule.getIdentifier());
                buffer.write(VAR_INT, AdventurePacketConvertor.getNamedTextColorValue(value.teamColor));
                buffer.write(COMPONENT, value.teamPrefix);
                buffer.write(COMPONENT, value.teamSuffix);
            }

            @Override
            public UpdateTeamAction read(@NotNull NetworkBuffer buffer) {
                return new UpdateTeamAction(buffer.read(COMPONENT), buffer.read(BYTE),
                        NameTagVisibility.fromIdentifier(buffer.read(STRING)), CollisionRule.fromIdentifier(buffer.read(STRING)),
                        NamedTextColor.namedColor(buffer.read(VAR_INT)),
                        buffer.read(COMPONENT), buffer.read(COMPONENT));
            }
        };

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

    public record AddEntitiesToTeamAction(@NotNull List<@NotNull String> entities) implements Action {
        public AddEntitiesToTeamAction {
            entities = List.copyOf(entities);
        }

        public AddEntitiesToTeamAction(Collection<String> entities) {
            this(List.copyOf(entities));
        }

        public static final NetworkBuffer.Type<AddEntitiesToTeamAction> SERIALIZER = NetworkBufferTemplate.template(
                STRING.list(MAX_MEMBERS), AddEntitiesToTeamAction::entities,
                AddEntitiesToTeamAction::new
        );

        @Override
        public int id() {
            return 3;
        }
    }

    public record RemoveEntitiesToTeamAction(@NotNull List<@NotNull String> entities) implements Action {
        public RemoveEntitiesToTeamAction {
            entities = List.copyOf(entities);
        }

        public RemoveEntitiesToTeamAction(Collection<String> entities) {
            this(List.copyOf(entities));
        }

        public static final NetworkBuffer.Type<RemoveEntitiesToTeamAction> SERIALIZER = NetworkBufferTemplate.template(
                STRING.list(MAX_MEMBERS), RemoveEntitiesToTeamAction::entities,
                RemoveEntitiesToTeamAction::new
        );

        @Override
        public int id() {
            return 4;
        }
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
