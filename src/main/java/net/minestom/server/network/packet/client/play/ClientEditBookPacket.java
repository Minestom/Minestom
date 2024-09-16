package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientEditBookPacket(int slot, @NotNull List<String> pages,
                                   @Nullable String title) implements ClientPacket {
    public static final int MAX_PAGES = 200;

    public static final NetworkBuffer.Type<ClientEditBookPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientEditBookPacket::slot,
            STRING.list(MAX_PAGES), ClientEditBookPacket::pages,
            STRING.optional(), ClientEditBookPacket::title,
            ClientEditBookPacket::new);

    public ClientEditBookPacket {
        pages = List.copyOf(pages);
        if (title != null && title.length() > 128) {
            throw new IllegalArgumentException("Title length cannot be greater than 128");
        }
    }
}
