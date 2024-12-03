package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientChatMessagePacket(String message, long timestamp,
                                      long salt, byte @Nullable [] signature,
                                      int ackOffset, BitSet ackList) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientChatMessagePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientChatMessagePacket::message,
            LONG, ClientChatMessagePacket::timestamp,
            LONG, ClientChatMessagePacket::salt,
            FixedRawBytes(256).optional(), ClientChatMessagePacket::signature,
            VAR_INT, ClientChatMessagePacket::ackOffset,
            FixedBitSet(20), ClientChatMessagePacket::ackList,
            ClientChatMessagePacket::new
    );
}
