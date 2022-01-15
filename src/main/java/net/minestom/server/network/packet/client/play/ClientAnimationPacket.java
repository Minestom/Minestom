package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientAnimationPacket(@NotNull Player.Hand hand) implements ClientPacket {
    public ClientAnimationPacket(BinaryReader reader) {
        this(Player.Hand.values()[reader.readVarInt()]);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(hand.ordinal());
    }
}
