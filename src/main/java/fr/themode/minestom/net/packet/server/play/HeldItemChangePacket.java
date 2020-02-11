package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class HeldItemChangePacket implements ServerPacket {

    public short slot;

    @Override
    public void write(PacketWriter writer) {
        writer.writeShort(slot);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.HELD_ITEM_CHANGE;
    }
}
