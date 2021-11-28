package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.UnaryOperator;

public record ScoreboardObjectivePacket(@NotNull String objectiveName, byte mode,
                                        @Nullable Component objectiveValue,
                                        @Nullable Type type) implements ComponentHoldingServerPacket {
    public ScoreboardObjectivePacket(BinaryReader reader) {
        this(read(reader));
    }

    private ScoreboardObjectivePacket(ScoreboardObjectivePacket packet) {
        this(packet.objectiveName, packet.mode, packet.objectiveValue, packet.type);
    }

    private static ScoreboardObjectivePacket read(BinaryReader reader) {
        var objectiveName = reader.readSizedString();
        var mode = reader.readByte();
        Component objectiveValue = null;
        Type type = null;
        if (mode == 0 || mode == 2) {
            objectiveValue = reader.readComponent();
            type = Type.values()[reader.readVarInt()];
        }
        return new ScoreboardObjectivePacket(objectiveName, mode, objectiveValue, type);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(objectiveName);
        writer.writeByte(mode);
        if (mode == 0 || mode == 2) {
            assert objectiveValue != null;
            writer.writeComponent(objectiveValue);
            assert type != null;
            writer.writeVarInt(type.ordinal());
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SCOREBOARD_OBJECTIVE;
    }

    @Override
    public @NotNull Collection<Component> components() {
        return mode == 0 || mode == 2 ? Collections.singleton(objectiveValue) :
                Collections.emptyList();
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
