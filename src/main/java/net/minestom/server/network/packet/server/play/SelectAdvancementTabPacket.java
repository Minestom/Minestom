package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record SelectAdvancementTabPacket(@Nullable String identifier) implements ServerPacket {
    public SelectAdvancementTabPacket(@NotNull NetworkBuffer reader) {
        this(reader.readOptional(STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeOptional(STRING, identifier);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SELECT_ADVANCEMENT_TAB;
    }
}
