package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.Player;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

public record OpenBookPacket(@NotNull Player.Hand hand) implements ServerPacket {
    public OpenBookPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(Player.Hand.class));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(Player.Hand.class, hand);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_BOOK;
    }
}
