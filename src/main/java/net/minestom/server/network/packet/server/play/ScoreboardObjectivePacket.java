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

public class ScoreboardObjectivePacket implements ComponentHoldingServerPacket {

    /**
     * An unique name for the objective
     */
    public String objectiveName;
    /**
     * 0 = create the scoreboard <br>
     * 1 = to remove the scoreboard<br>
     * 2 = to update the display text
     */
    public byte mode;
    /**
     * The text to be displayed for the score
     */
    public Component objectiveValue; // Only text
    /**
     * The type how the score is displayed
     */
    public Type type;

    public ScoreboardObjectivePacket() {
        objectiveName = "";
        objectiveValue = Component.empty();
        type = Type.INTEGER;
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
    public void read(@NotNull BinaryReader reader) {
        objectiveName = reader.readSizedString();
        mode = reader.readByte();

        if (mode == 0 || mode == 2) {
            objectiveValue = reader.readComponent();
            type = Type.values()[reader.readVarInt()];
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
            ScoreboardObjectivePacket packet = new ScoreboardObjectivePacket();
            packet.objectiveName = objectiveName;
            packet.mode = mode;
            packet.objectiveValue = operator.apply(objectiveValue);
            packet.type = type;
            return packet;
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
