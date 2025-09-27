package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.BitSet;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientChatMessagePacket(String message, long timestamp,
                                      long salt, byte @Nullable [] signature,
                                      int ackOffset, BitSet ackList, byte checksum) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientChatMessagePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientChatMessagePacket::message,
            LONG, ClientChatMessagePacket::timestamp,
            LONG, ClientChatMessagePacket::salt,
            FixedRawBytes(256).optional(), ClientChatMessagePacket::signature,
            VAR_INT, ClientChatMessagePacket::ackOffset,
            FixedBitSet(20), ClientChatMessagePacket::ackList,
            BYTE, ClientChatMessagePacket::checksum,
            ClientChatMessagePacket::new
    );

    public ClientChatMessagePacket {
        signature = signature != null ? signature.clone() : null;
        ackList = (BitSet) ackList.clone();
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClientChatMessagePacket(String message1, long timestamp1, long salt1, byte[] signature1, int offset, BitSet list, byte checksum1))) return false;
        return salt() == salt1 && ackOffset() == offset && checksum() == checksum1 && timestamp() == timestamp1 && message().equals(message1) && ackList().equals(list) && Arrays.equals(signature(), signature1);
    }

    @Override
    public int hashCode() {
        int result = message().hashCode();
        result = 31 * result + Long.hashCode(timestamp());
        result = 31 * result + Long.hashCode(salt());
        result = 31 * result + Arrays.hashCode(signature());
        result = 31 * result + ackOffset();
        result = 31 * result + ackList().hashCode();
        result = 31 * result + checksum();
        return result;
    }
}
