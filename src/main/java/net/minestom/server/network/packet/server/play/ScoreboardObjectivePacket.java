package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record ScoreboardObjectivePacket(@NotNull String objectiveName, byte mode,
                                        @Nullable Component objectiveValue,
                                        @Nullable Type type,
                                        @Nullable Sidebar.NumberFormat numberFormat) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<ScoreboardObjectivePacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ScoreboardObjectivePacket value) {
            buffer.write(STRING, value.objectiveName);
            buffer.write(BYTE, value.mode);
            if (value.mode == 0 || value.mode == 2) {
                assert value.objectiveValue != null;
                buffer.write(COMPONENT, value.objectiveValue);
                assert value.type != null;
                buffer.write(VAR_INT, value.type.ordinal());
                buffer.write(Sidebar.NumberFormat.SERIALIZER.optional(), value.numberFormat);
            }
        }

        @Override
        public ScoreboardObjectivePacket read(@NotNull NetworkBuffer buffer) {
            String objectiveName = buffer.read(STRING);
            byte mode = buffer.read(BYTE);
            Component objectiveValue = null;
            Type type = null;
            Sidebar.NumberFormat numberFormat = null;
            if (mode == 0 || mode == 2) {
                objectiveValue = buffer.read(COMPONENT);
                type = Type.values()[buffer.read(VAR_INT)];
                numberFormat = buffer.read(Sidebar.NumberFormat.SERIALIZER.optional());
            }
            return new ScoreboardObjectivePacket(objectiveName, mode, objectiveValue, type, numberFormat);
        }
    };

    @Override
    public @NotNull Collection<Component> components() {
        return mode == 0 || mode == 2 ? List.of(objectiveValue) :
                List.of();
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return mode == 0 || mode == 2 ? new ScoreboardObjectivePacket(objectiveName, mode,
                operator.apply(objectiveValue), type, numberFormat) : this;
    }

    /**
     * This enumeration represents all available types for the scoreboard objective
     */
    public enum Type {
        INTEGER,
        HEARTS
    }
}
