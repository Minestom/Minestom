package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

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
