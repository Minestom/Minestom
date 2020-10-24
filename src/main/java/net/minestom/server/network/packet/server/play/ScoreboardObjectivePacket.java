package net.minestom.server.network.packet.server.play;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class ScoreboardObjectivePacket implements ServerPacket {

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
    public JsonMessage objectiveValue; // Only text
    /**
     * The type how the score is displayed
     */
    public Type type;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeSizedString(objectiveName);
        writer.writeByte(mode);

        if (mode == 0 || mode == 2) {
            writer.writeSizedString(objectiveValue.toString());
            writer.writeVarInt(type.ordinal());
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SCOREBOARD_OBJECTIVE;
    }

    /**
     * This enumeration represents all available types for the scoreboard objective
     */
    public enum Type {
        INTEGER,
        HEARTS
    }
}
