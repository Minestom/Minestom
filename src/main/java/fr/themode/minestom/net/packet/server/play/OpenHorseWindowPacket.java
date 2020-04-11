package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class OpenHorseWindowPacket implements ServerPacket {

    public byte windowId;
    public int slotCount;
    public int entityId;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte(windowId);
        writer.writeVarInt(slotCount);
        writer.writeInt(entityId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_HORSE_WINDOW;
    }
}
