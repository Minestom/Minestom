package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record SelectAdvancementTabPacket(@Nullable String identifier) implements ServerPacket.Play {
    public SelectAdvancementTabPacket(@NotNull NetworkBuffer reader) {
        this(reader.readOptional(STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeOptional(STRING, identifier);
    }

}
