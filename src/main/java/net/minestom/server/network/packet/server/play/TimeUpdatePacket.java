package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record TimeUpdatePacket(long worldAge, long timeOfDay) implements ServerPacket {
    public TimeUpdatePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG), reader.read(LONG));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, worldAge);
        writer.write(LONG, timeOfDay);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.TIME_UPDATE;
    }
}
