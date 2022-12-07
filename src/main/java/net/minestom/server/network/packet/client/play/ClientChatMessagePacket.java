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

    public ClientChatMessagePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(LONG),
                reader.read(LONG), reader.readOptional(r -> r.readBytes(256)),
                reader.read(VAR_INT), BitSet.valueOf(reader.readBytes(3)));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, message);
        writer.write(LONG, timestamp);
        writer.write(LONG, salt);
        writer.writeOptional(BYTE_ARRAY, signature);
        writer.write(VAR_INT, ackOffset);
        writer.write(RAW_BYTES, Arrays.copyOf(ackList.toByteArray(), 3));
    }
}
