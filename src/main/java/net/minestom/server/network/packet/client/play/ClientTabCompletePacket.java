package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientTabCompletePacket(int transactionId, @NotNull String text) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientTabCompletePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientTabCompletePacket::transactionId,
            STRING, ClientTabCompletePacket::text,
            ClientTabCompletePacket::new);
}
