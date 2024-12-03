package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientNameItemPacket(@NotNull String itemName) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientNameItemPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientNameItemPacket::itemName,
            ClientNameItemPacket::new);

    public ClientNameItemPacket {
        if (itemName.length() > Short.MAX_VALUE) {
            throw new IllegalArgumentException("ItemStack name cannot be longer than Short.MAX_VALUE characters!");
        }
    }
}
