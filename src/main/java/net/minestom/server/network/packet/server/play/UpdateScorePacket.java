package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public final class UpdateScorePacket implements ServerPacket {
    public final String entityName;
    public final byte action;
    public final String objectiveName;
    public final int value;

    public UpdateScorePacket(String entityName, byte action, String objectiveName, int value) {
        this.entityName = entityName;
        this.action = action;
        this.objectiveName = objectiveName;
        this.value = value;
    }

    public UpdateScorePacket(BinaryReader reader) {
        this.entityName = reader.readSizedString();
        this.action = reader.readByte();
        this.objectiveName = reader.readSizedString();
        this.value = action != 1 ? reader.readVarInt() : 0;
    }

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
