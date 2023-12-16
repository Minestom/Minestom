package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;

public record UnloadChunkPacket(int chunkX, int chunkZ) implements ServerPacket {
    public UnloadChunkPacket(@NotNull NetworkBuffer reader) {
        this(read(reader));
    }

    private UnloadChunkPacket(@NotNull UnloadChunkPacket other) {
        this(other.chunkX(), other.chunkZ());
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        // Client reads this as a single long in big endian, so we have to write it backwards
        writer.write(INT, chunkZ);
        writer.write(INT, chunkX);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.UNLOAD_CHUNK;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }

    private static UnloadChunkPacket read(@NotNull NetworkBuffer reader) {
        int z = reader.read(INT);
        int x = reader.read(INT);
        return new UnloadChunkPacket(x, z);
    }
}
