package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record ScoreboardObjectivePacket(@NotNull String objectiveName, byte mode,
                                        @Nullable Component objectiveValue,
                                        @Nullable Type type) implements ComponentHoldingServerPacket {
    public ScoreboardObjectivePacket(@NotNull NetworkBuffer reader) {
        this(read(reader));
    }

    private ScoreboardObjectivePacket(ScoreboardObjectivePacket packet) {
        this(packet.objectiveName, packet.mode, packet.objectiveValue, packet.type);
    }

    private static ScoreboardObjectivePacket read(@NotNull NetworkBuffer reader) {
        var objectiveName = reader.read(STRING);
        var mode = reader.read(BYTE);
        Component objectiveValue = null;
        Type type = null;
        if (mode == 0 || mode == 2) {
            objectiveValue = reader.read(COMPONENT);
            type = Type.values()[reader.read(VAR_INT)];
        }
        return new ScoreboardObjectivePacket(objectiveName, mode, objectiveValue, type);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, objectiveName);
        writer.write(BYTE, mode);
        if (mode == 0 || mode == 2) {
            assert objectiveValue != null;
            writer.write(COMPONENT, objectiveValue);
            assert type != null;
            writer.write(VAR_INT, type.ordinal());
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SCOREBOARD_OBJECTIVE;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return mode == 0 || mode == 2 ? List.of(objectiveValue) :
                List.of();
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return mode == 0 || mode == 2 ? new ScoreboardObjectivePacket(objectiveName, mode,
                operator.apply(objectiveValue), type) : this;
    }

    /**
     * This enumeration represents all available types for the scoreboard objective
     */
    public enum Type {
        INTEGER,
        HEARTS
    }
}
