package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.validate.Check;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientTabCompletePacket(int transactionId, String text) implements ClientPacket.Play {
    public static final int MAX_TEXT_LENGTH = 32500;

    public static final NetworkBuffer.Type<ClientTabCompletePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientTabCompletePacket::transactionId,
            STRING, ClientTabCompletePacket::text,
            ClientTabCompletePacket::new);

    public ClientTabCompletePacket {
        Check.argCondition(text.length() > MAX_TEXT_LENGTH, "Text length cannot be greater than {0}", MAX_TEXT_LENGTH);
    }
}
