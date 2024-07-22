package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.BitSet;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientChatMessagePacket(String message, long timestamp,
                                      long salt, byte @Nullable [] signature,
                                      int ackOffset, BitSet ackList) implements ClientPacket {
    public static NetworkBuffer.Type<ClientChatMessagePacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ClientChatMessagePacket value) {
            buffer.write(STRING, value.message);
            buffer.write(LONG, value.timestamp);
            buffer.write(LONG, value.salt);
            buffer.writeOptional(BYTE_ARRAY, value.signature);
            buffer.write(VAR_INT, value.ackOffset);
            buffer.write(RAW_BYTES, Arrays.copyOf(value.ackList.toByteArray(), 3));
        }

        @Override
        public ClientChatMessagePacket read(@NotNull NetworkBuffer buffer) {
            return new ClientChatMessagePacket(buffer.read(STRING), buffer.read(LONG),
                    buffer.read(LONG), buffer.readOptional(r -> r.readBytes(256)),
                    buffer.read(VAR_INT), BitSet.valueOf(buffer.readBytes(3)));
        }
    };
}
