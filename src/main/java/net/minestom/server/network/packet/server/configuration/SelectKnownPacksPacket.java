package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SelectKnownPacksPacket(
        @NotNull List<Entry> entries
) implements ServerPacket.Configuration {
    private static final int MAX_ENTRIES = 64;
    public static final Entry MINECRAFT_CORE = new Entry("minecraft", "core", MinecraftServer.VERSION_NAME);

    public static final NetworkBuffer.Type<SelectKnownPacksPacket> SERIALIZER = NetworkBufferTemplate.template(
            Entry.SERIALIZER.list(MAX_ENTRIES), SelectKnownPacksPacket::entries,
            SelectKnownPacksPacket::new);

    public SelectKnownPacksPacket {
        Check.argCondition(entries.size() > MAX_ENTRIES, "Too many known packs: {0} > {1}", entries.size(), MAX_ENTRIES);
        entries = List.copyOf(entries);
    }

    public record Entry(
            @NotNull String namespace,
            @NotNull String id,
            @NotNull String version
    ) {
        public static final NetworkBuffer.Type<Entry> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.STRING, Entry::namespace,
                NetworkBuffer.STRING, Entry::id,
                NetworkBuffer.STRING, Entry::version,
                Entry::new);
    }
}
