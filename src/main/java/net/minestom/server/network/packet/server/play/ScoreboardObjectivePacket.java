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
import java.util.function.UnaryOperator;

public final class ScoreboardObjectivePacket implements ComponentHoldingServerPacket {
    public final String objectiveName;
    public final byte mode;
    public final Component objectiveValue;
    public final Type type;

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

    @Override
    public int getId() {
        return ServerPacketIdentifier.SCOREBOARD_OBJECTIVE;
    }

    @Override
    public @NotNull Collection<Component> components() {
        if (mode == 0 || mode == 2) {
            return Collections.singleton(objectiveValue);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        if (mode == 0 || mode == 2) {
            return new ScoreboardObjectivePacket(objectiveName, mode, operator.apply(objectiveValue), type);
        } else {
            return this;
        }
    }

    /**
     * This enumeration represents all available types for the scoreboard objective
     */
    public enum Type {
        INTEGER,
        HEARTS
    }
}
