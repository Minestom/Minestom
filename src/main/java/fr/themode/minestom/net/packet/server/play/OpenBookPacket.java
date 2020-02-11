package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class OpenBookPacket implements ServerPacket {

    public Player.Hand hand;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(hand.ordinal());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_BOOK;
    }
}
