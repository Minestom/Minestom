package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientUseItemPacket extends ClientPlayPacket {

    public Player.Hand hand = Player.Hand.MAIN;

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.hand = Player.Hand.values()[reader.readVarInt()];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(hand.ordinal());
    }
}
