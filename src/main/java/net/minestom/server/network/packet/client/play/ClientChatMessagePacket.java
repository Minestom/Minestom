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
        public void write(@NotNull NetworkBuffer writer, ClientChatMessagePacket value) {
            writer.write(STRING, value.message);
            writer.write(LONG, value.timestamp);
            writer.write(LONG, value.salt);
            writer.writeOptional(BYTE_ARRAY, value.signature);
            writer.write(VAR_INT, value.ackOffset);
            writer.write(RAW_BYTES, Arrays.copyOf(value.ackList.toByteArray(), 3));
        }

        @Override
        public ClientChatMessagePacket read(@NotNull NetworkBuffer reader) {
            return new ClientChatMessagePacket(reader.read(STRING), reader.read(LONG),
                    reader.read(LONG), reader.readOptional(r -> r.readBytes(256)),
                    reader.read(VAR_INT), BitSet.valueOf(reader.readBytes(3)));
        }
    };
}
