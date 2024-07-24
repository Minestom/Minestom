package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record ClientSetRecipeBookStatePacket(@NotNull BookType bookType,
                                             boolean bookOpen, boolean filterActive) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSetRecipeBookStatePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.Enum(BookType.class), ClientSetRecipeBookStatePacket::bookType,
            BOOLEAN, ClientSetRecipeBookStatePacket::bookOpen,
            BOOLEAN, ClientSetRecipeBookStatePacket::filterActive,
            ClientSetRecipeBookStatePacket::new);

    public enum BookType {
        CRAFTING, FURNACE, BLAST_FURNACE, SMOKER
    }
}
