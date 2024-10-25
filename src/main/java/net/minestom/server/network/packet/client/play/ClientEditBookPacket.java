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
    public static final int MAX_PAGES = 100;
    public static final int MAX_TITLE_LENGTH = 32;
    public static final int MAX_PAGE_LENGTH = 1024;

    public static final NetworkBuffer.Type<ClientEditBookPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientEditBookPacket::slot,
            STRING.list(MAX_PAGES), ClientEditBookPacket::pages,
            STRING.optional(), ClientEditBookPacket::title,
            ClientEditBookPacket::new);

    public ClientEditBookPacket {
        for (var page : pages) {
            if (page.length() > MAX_PAGE_LENGTH) {
                throw new IllegalArgumentException("Page length cannot be greater than " + MAX_PAGE_LENGTH);
            }
        }
        if (title != null && title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title length cannot be greater than " + MAX_TITLE_LENGTH);
        }
        pages = List.copyOf(pages);
    }
}
