package net.minestom.server.network.packet.client.configuration;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.configuration.SelectKnownPacksPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ClientSelectKnownPacksPacket(
        @NotNull List<SelectKnownPacksPacket.Entry> entries
) implements ClientPacket {
    private static final int MAX_ENTRIES = 64;

    public static final NetworkBuffer.Type<ClientSelectKnownPacksPacket> SERIALIZER = NetworkBufferTemplate.template(
            SelectKnownPacksPacket.Entry.SERIALIZER.list(MAX_ENTRIES), ClientSelectKnownPacksPacket::entries,
            ClientSelectKnownPacksPacket::new);

    public ClientSelectKnownPacksPacket {
        Check.argCondition(entries.size() > MAX_ENTRIES, "Too many known packs: {0} > {1}", entries.size(), MAX_ENTRIES);
        entries = List.copyOf(entries);
    }
}
