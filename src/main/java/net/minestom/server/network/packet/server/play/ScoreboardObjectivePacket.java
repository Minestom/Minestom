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

public record ScoreboardObjectivePacket(String objectiveName, Mode mode) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<ScoreboardObjectivePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ScoreboardObjectivePacket::objectiveName,
            BYTE.unionType(Mode::typeFromId, Mode::id), ScoreboardObjectivePacket::mode,
            ScoreboardObjectivePacket::new
    );

    public ScoreboardObjectivePacket {
        Objects.requireNonNull(objectiveName, "objectiveName");
        Objects.requireNonNull(mode, "mode");
    }

    @Override
    public Collection<Component> components() {
        if (mode instanceof ComponentHolder<?> componentHolder) return componentHolder.components();
        return List.of();
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        if (!(mode instanceof ComponentHolder<?> componentHolder)) return this;
        return new ScoreboardObjectivePacket(objectiveName, (Mode) componentHolder.copyWithOperator(operator));
    }

    /**
     * Represents a mode in the protocol.
     */
    public sealed interface Mode {
        @ApiStatus.Internal
        byte id();

        private static NetworkBuffer.Type<? extends Mode> typeFromId(byte id) {
            return switch (id) {
                case 0 -> Create.SERIALIZER;
                case 1 -> Destroy.SERIALIZER;
                case 2 -> Update.SERIALIZER;
                default -> throw new IllegalStateException("Unexpected value: " + id);
            };
        }
    }

    public record Create(Component objectiveValue, Type type, @Nullable Sidebar.NumberFormat numberFormat) implements Mode, ComponentHolder<Create> {
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

        @ApiStatus.Internal
        @Override
        public byte id() {
            return 0;
        }
    }

    public record Destroy() implements Mode {
        public static final Destroy INSTANCE = new Destroy();
        public static final NetworkBuffer.Type<Destroy> SERIALIZER = NetworkBufferTemplate.template(INSTANCE);

        @ApiStatus.Internal
        @Override
        public byte id() {
            return 1;
        }
    }

    public record Update(Component objectiveValue, Type type, @Nullable Sidebar.NumberFormat numberFormat) implements Mode, ComponentHolder<Update> {
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

        @ApiStatus.Internal
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
