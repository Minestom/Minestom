package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientQueryBlockNbtPacket(int transactionId, @NotNull Point blockPosition) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientQueryBlockNbtPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientQueryBlockNbtPacket::transactionId,
            BLOCK_POSITION, ClientQueryBlockNbtPacket::blockPosition,
            ClientQueryBlockNbtPacket::new);
}
