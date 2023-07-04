package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.network.packet.server.play.data.ChunkBiomeData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ChunkBiomeDataPacket(@NotNull List<ChunkBiomeData> sections) implements ServerPacket {

    public ChunkBiomeDataPacket(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(ChunkBiomeData::new));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(sections);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHUNK_BIOME_DATA;
    }

}
