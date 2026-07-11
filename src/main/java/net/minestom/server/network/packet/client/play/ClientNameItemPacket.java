package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.validate.Check;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientNameItemPacket(String itemName) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientNameItemPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientNameItemPacket::itemName,
            ClientNameItemPacket::new);

    public ClientNameItemPacket {
        Check.argCondition(itemName.length() > Short.MAX_VALUE, "Item name cannot be longer than Short.MAX_SIZE");
    }
}
