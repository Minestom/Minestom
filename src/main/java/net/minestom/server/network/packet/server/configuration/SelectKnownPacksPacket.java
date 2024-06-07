package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SelectKnownPacksPacket(
        @NotNull List<Entry> entries
) implements ServerPacket.Configuration {
    private static final int MAX_ENTRIES = 64;

    public SelectKnownPacksPacket {
        Check.argCondition(entries.size() > MAX_ENTRIES, "Too many known packs: {0} > {1}", entries.size(), MAX_ENTRIES);
    }

    public SelectKnownPacksPacket(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(Entry::new, MAX_ENTRIES));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(entries);
    }

    @Override
    public int configurationId() {
        return ServerPacketIdentifier.CONFIGURATION_SELECT_KNOWN_PACKS;
    }

    public record Entry(
            @NotNull String namespace,
            @NotNull String id,
            @NotNull String version
    ) implements NetworkBuffer.Writer {
        public Entry(@NotNull NetworkBuffer reader) {
            this(reader.read(NetworkBuffer.STRING),
                    reader.read(NetworkBuffer.STRING),
                    reader.read(NetworkBuffer.STRING));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(NetworkBuffer.STRING, namespace);
            writer.write(NetworkBuffer.STRING, id);
            writer.write(NetworkBuffer.STRING, version);
        }
    }
}
