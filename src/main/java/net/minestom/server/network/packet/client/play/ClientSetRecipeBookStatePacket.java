package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record ClientSetRecipeBookStatePacket(BookType bookType,
                                             boolean bookOpen, boolean filterActive) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientSetRecipeBookStatePacket> SERIALIZER = NetworkBufferTemplate.template(
            BookType.NETWORK_TYPE, ClientSetRecipeBookStatePacket::bookType,
            BOOLEAN, ClientSetRecipeBookStatePacket::bookOpen,
            BOOLEAN, ClientSetRecipeBookStatePacket::filterActive,
            ClientSetRecipeBookStatePacket::new);

    public enum BookType {
        CRAFTING,
        FURNACE,
        BLAST_FURNACE,
        SMOKER;

        public static final NetworkBuffer.Type<BookType> NETWORK_TYPE = NetworkBuffer.Enum(BookType.class);
    }
}
