package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class OpenHorseWindowPacket implements ServerPacket {

    public byte windowId;
    public int slotCount;
    public int entityId;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeVarInt(slotCount);
        writer.writeInt(entityId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_HORSE_WINDOW;
    }
}
