package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class UpdateScorePacket implements ServerPacket {

    public String entityName;
    public byte action;
    public String objectiveName;
    public int value;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(entityName);
        writer.writeByte(action);
        writer.writeSizedString(objectiveName);
        if (action != 1) {
            writer.writeVarInt(value);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_SCORE;
    }
}
