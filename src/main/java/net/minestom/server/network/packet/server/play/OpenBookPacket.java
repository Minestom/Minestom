package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

public record OpenBookPacket(@NotNull PlayerHand hand) implements ServerPacket.Play {
    public OpenBookPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(PlayerHand.class));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(PlayerHand.class, hand);
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.OPEN_BOOK;
    }
}
