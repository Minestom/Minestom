package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record UpdateScorePacket(@NotNull String entityName, byte action,
                                @NotNull String objectiveName, int value) implements ServerPacket {
    public UpdateScorePacket(BinaryReader reader) {
        this(read(reader));
    }

    private UpdateScorePacket(UpdateScorePacket packet) {
        this(packet.entityName, packet.action, packet.objectiveName, packet.value);
    }

    private static UpdateScorePacket read(BinaryReader reader) {
        var entityName = reader.readSizedString();
        var action = reader.readByte();
        var objectiveName = reader.readSizedString();
        var value = action != 1 ? reader.readVarInt() : 0;
        return new UpdateScorePacket(entityName, action, objectiveName, value);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(entityName);
        writer.writeByte(action);
        writer.writeSizedString(objectiveName);
        if (action != 1) writer.writeVarInt(value);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_SCORE;
    }
}
