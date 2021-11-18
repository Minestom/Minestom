package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.UnaryOperator;

public final class ScoreboardObjectivePacket implements ComponentHoldingServerPacket {
    private final String objectiveName;
    private final byte mode;
    private final Component objectiveValue;
    private final Type type;

    public ScoreboardObjectivePacket(String objectiveName, byte mode, Component objectiveValue, Type type) {
        this.objectiveName = objectiveName;
        this.mode = mode;
        this.objectiveValue = objectiveValue;
        this.type = type;
    }

    public ScoreboardObjectivePacket(BinaryReader reader) {
        this.objectiveName = reader.readSizedString();
        this.mode = reader.readByte();
        if (mode == 0 || mode == 2) {
            this.objectiveValue = reader.readComponent();
            this.type = Type.values()[reader.readVarInt()];
        } else {
            this.objectiveValue = null;
            this.type = null;
        }
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(objectiveName);
        writer.writeByte(mode);
        if (mode == 0 || mode == 2) {
            writer.writeComponent(objectiveValue);
            writer.writeVarInt(type.ordinal());
        }
    }

    public String objectiveName() {
        return objectiveName;
    }

    public byte mode() {
        return mode;
    }

    public Component objectiveValue() {
        return objectiveValue;
    }

    public Type type() {
        return type;
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SCOREBOARD_OBJECTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScoreboardObjectivePacket that)) return false;
        return mode == that.mode && objectiveName.equals(that.objectiveName) &&
                Objects.equals(objectiveValue, that.objectiveValue) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectiveName, mode, objectiveValue, type);
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
