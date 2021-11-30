package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record OpenHorseWindowPacket(byte windowId, int slotCount, int entityId) implements ServerPacket {
    public OpenHorseWindowPacket(BinaryReader reader) {
        this(reader.readByte(), reader.readVarInt(), reader.readInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeVarInt(slotCount);
        writer.writeInt(entityId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_HORSE_WINDOW;
    }
}
