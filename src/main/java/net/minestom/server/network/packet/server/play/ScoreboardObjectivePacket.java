package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record ScoreboardObjectivePacket(String objectiveName, Action action) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<ScoreboardObjectivePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ScoreboardObjectivePacket::objectiveName,
            BYTE.unionType(Action::typeFromId, Action::id), ScoreboardObjectivePacket::action,
            ScoreboardObjectivePacket::new
    );

    public ScoreboardObjectivePacket {
        Objects.requireNonNull(objectiveName, "objectiveName");
        Objects.requireNonNull(action, "action");
    }

    @Override
    public Collection<? extends Component> components() {
        if (action instanceof ComponentHolder<?> componentHolder) return componentHolder.components();
        return List.of();
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        if (!(action instanceof ComponentHolder<?> componentHolder)) return this;
        return new ScoreboardObjectivePacket(objectiveName, (Action) componentHolder.copyWithOperator(operator));
    }

    /**
     * Represents an action in the protocol.
     */
    public sealed interface Action {
        @ApiStatus.OverrideOnly
        byte id();

        private static NetworkBuffer.Type<? extends Action> typeFromId(byte id) {
            return switch (id) {
                case 0 -> Create.SERIALIZER;
                case 1 -> Destroy.SERIALIZER;
                case 2 -> Update.SERIALIZER;
                default -> throw new IllegalStateException("Unexpected value: " + id);
            };
        }
    }

    public record Create(Component objectiveValue, Type type, @Nullable Sidebar.NumberFormat numberFormat) implements Action, ComponentHolder<Create> {
        public static final NetworkBuffer.Type<Create> SERIALIZER = NetworkBufferTemplate.template(
                COMPONENT, Create::objectiveValue,
                Type.NETWORK_TYPE, Create::type,
                Sidebar.NumberFormat.SERIALIZER.optional(), Create::numberFormat,
                Create::new
        );

        public Create {
            Objects.requireNonNull(objectiveValue, "objectiveValue");
            Objects.requireNonNull(type, "type");
        }

        public Create(Component objectiveValue, Type type) {
            this(objectiveValue, type, null);
        }

        @Override
        public @Unmodifiable Collection<Component> components() {
            return List.of(objectiveValue);
        }

        @Override
        public Create copyWithOperator(UnaryOperator<Component> operator) {
            return new Create(operator.apply(objectiveValue), type, numberFormat);
        }

        @Override
        public byte id() {
            return 0;
        }
    }

    public record Destroy() implements Action {
        public static final NetworkBuffer.Type<Destroy> SERIALIZER = NetworkBufferTemplate.template(new Destroy());

        @Override
        public byte id() {
            return 1;
        }
    }

    public record Update(Component objectiveValue, Type type, @Nullable Sidebar.NumberFormat numberFormat) implements Action, ComponentHolder<Update> {
        public static final NetworkBuffer.Type<Update> SERIALIZER = NetworkBufferTemplate.template(
                COMPONENT, Update::objectiveValue,
                Type.NETWORK_TYPE, Update::type,
                Sidebar.NumberFormat.SERIALIZER.optional(), Update::numberFormat,
                Update::new
        );

        public Update {
            Objects.requireNonNull(objectiveValue, "objectiveValue");
            Objects.requireNonNull(type, "type");
        }

        public Update(Component objectiveValue, Type type) {
            this(objectiveValue, type, null);
        }

        @Override
        public @Unmodifiable Collection<Component> components() {
            return List.of(objectiveValue);
        }

        @Override
        public Update copyWithOperator(UnaryOperator<Component> operator) {
            return new Update(operator.apply(objectiveValue), type, numberFormat);
        }

        @Override
        public byte id() {
            return 2;
        }
    }

    /**
     * This enumeration represents all available types for the scoreboard objective
     */
    public enum Type {
        INTEGER,
        HEARTS;

        public static final NetworkBuffer.Type<Type> NETWORK_TYPE = NetworkBuffer.Enum(Type.class);
    }
}
