package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class OpenBookPacket implements ServerPacket {

    public Player.Hand hand;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(hand.ordinal());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_BOOK;
    }
}
