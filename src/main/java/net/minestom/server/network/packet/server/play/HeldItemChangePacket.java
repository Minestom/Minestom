package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class HeldItemChangePacket implements ServerPacket {

    public byte slot;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte(slot);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.HELD_ITEM_CHANGE;
    }
}
