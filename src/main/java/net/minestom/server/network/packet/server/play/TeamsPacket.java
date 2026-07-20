package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.color.TeamColor;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.Tagged;

/**
 * The packet creates or updates teams
 */
public record TeamsPacket(String teamName, Action action) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final int MAX_MEMBERS = 16384;

    private static final NetworkBuffer.Type<Action> ACTION_NETWORK_TYPE = Tagged(
            NetworkBuffer.BYTE, Action::id,
            Map.of(
                    (byte) 0, CreateTeamAction.SERIALIZER,
                    (byte) 1, RemoveTeamAction.SERIALIZER,
                    (byte) 2, UpdateTeamAction.SERIALIZER,
                    (byte) 3, AddEntitiesToTeamAction.SERIALIZER,
                    (byte) 4, RemoveEntitiesToTeamAction.SERIALIZER
            )
    );

    public static final NetworkBuffer.Type<TeamsPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, TeamsPacket::teamName,
            ACTION_NETWORK_TYPE, TeamsPacket::action,
            TeamsPacket::new
    );

    @Override
    public Collection<? extends Component> components() {
        return this.action instanceof ComponentHolder<?> holder ? holder.components() : List.of();
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        return new TeamsPacket(
                this.teamName,
                this.action instanceof ComponentHolder<?> holder
                        ? (Action) holder.copyWithOperator(operator)
                        : this.action
        );
    }

    public record Settings(
            Component displayName,
            Component teamPrefix,
            Component teamSuffix,
            NameTagVisibility nameTagVisibility,
            CollisionRule collisionRule,
            @Nullable TeamColor color,
            byte friendlyFlags
    ) implements ComponentHolder<Settings> {
        public static final NetworkBuffer.Type<Settings> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.COMPONENT, Settings::displayName,
                NetworkBuffer.COMPONENT, Settings::teamPrefix,
                NetworkBuffer.COMPONENT, Settings::teamSuffix,
                NameTagVisibility.NETWORK_TYPE, Settings::nameTagVisibility,
                CollisionRule.NETWORK_TYPE, Settings::collisionRule,
                TeamColor.NETWORK_TYPE.optional(), Settings::color,
                NetworkBuffer.BYTE, Settings::friendlyFlags,
                Settings::new);

        @Override
        public List<Component> components() {
            return List.of(this.displayName, this.teamPrefix, this.teamSuffix);
        }

        @Override
        public Settings copyWithOperator(UnaryOperator<Component> operator) {
            return new Settings(
                    operator.apply(this.displayName),
                    operator.apply(this.teamPrefix),
                    operator.apply(this.teamSuffix),
                    this.nameTagVisibility,
                    this.collisionRule,
                    this.color,
                    this.friendlyFlags
            );
        }
    }

    public sealed interface Action permits CreateTeamAction, RemoveTeamAction, UpdateTeamAction, AddEntitiesToTeamAction, RemoveEntitiesToTeamAction {
        @ApiStatus.Internal
        @ApiStatus.OverrideOnly
        byte id();
    }

    public record CreateTeamAction(
            Settings settings,
            List<String> entities
    ) implements Action, ComponentHolder<CreateTeamAction> {
        public CreateTeamAction {
            entities = List.copyOf(entities);
        }

        public static final NetworkBuffer.Type<CreateTeamAction> SERIALIZER = NetworkBufferTemplate.template(
                Settings.SERIALIZER, CreateTeamAction::settings,
                STRING.list(MAX_MEMBERS), CreateTeamAction::entities,
                CreateTeamAction::new
        );

        @Override
        public byte id() {
            return 0;
        }

        @Override
        public List<Component> components() {
            return settings.components();
        }

        @Override
        public CreateTeamAction copyWithOperator(UnaryOperator<Component> operator) {
            return new CreateTeamAction(settings.copyWithOperator(operator), entities);
        }
    }

    public record RemoveTeamAction() implements Action {
        public static final NetworkBuffer.Type<RemoveTeamAction> SERIALIZER = NetworkBufferTemplate.template(
                new RemoveTeamAction());

        @Override
        public byte id() {
            return 1;
        }
    }

    public record UpdateTeamAction(Settings settings) implements Action, ComponentHolder<UpdateTeamAction> {

        public static final NetworkBuffer.Type<UpdateTeamAction> SERIALIZER = NetworkBufferTemplate.template(
                Settings.SERIALIZER, UpdateTeamAction::settings,
                UpdateTeamAction::new
        );

        @Override
        public byte id() {
            return 2;
        }

        @Override
        public List<Component> components() {
            return settings.components();
        }

        @Override
        public UpdateTeamAction copyWithOperator(UnaryOperator<Component> operator) {
            return new UpdateTeamAction(settings.copyWithOperator(operator));
        }
    }

    public record AddEntitiesToTeamAction(List<String> entities) implements Action {
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
        public byte id() {
            return 3;
        }
    }

    public record RemoveEntitiesToTeamAction(List<String> entities) implements Action {
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
        public byte id() {
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
         * The name tag is invisible
         */
        NEVER("never"),
        /**
         * Hides the name tag for other teams
         */
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        /**
         * Hides the name tag for the own team
         */
        HIDE_FOR_OWN_TEAM("hideForOwnTeam");

        public static final NetworkBuffer.Type<NameTagVisibility> NETWORK_TYPE = NetworkBuffer.Enum(
                NameTagVisibility.class);

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

        public static NameTagVisibility fromIdentifier(String identifier) {
            for (NameTagVisibility v : values()) {
                if (v.getIdentifier().equals(identifier))
                    return v;
            }
            Check.fail("Identifier for NameTagVisibility is invalid: {0}", identifier);
            return null;
        }

        /**
         * Gets the client identifier
         *
         * @return the identifier
         */
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
         * Cannot push an object, but neither can they be pushed
         */
        NEVER("never"),
        /**
         * Can push objects of other teams, but teammates cannot
         */
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        /**
         * Can only push objects of the same team
         */
        PUSH_OWN_TEAM("pushOwnTeam");

        public static final NetworkBuffer.Type<CollisionRule> NETWORK_TYPE = NetworkBuffer.Enum(CollisionRule.class);

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

        public static CollisionRule fromIdentifier(String identifier) {
            for (CollisionRule v : values()) {
                if (v.getIdentifier().equals(identifier))
                    return v;
            }
            Check.fail("Identifier for CollisionRule is invalid: {0}", identifier);
            return null;
        }

        /**
         * Gets the identifier of the rule
         *
         * @return the identifier
         */
        public String getIdentifier() {
            return identifier;
        }
    }
}
