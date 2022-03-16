package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record ClientCommandChatPacket(@NotNull String message, long timestamp,
                                      long salt, Map<String, byte[]> signatures,
                                      boolean signed) implements ClientPacket {
    public ClientCommandChatPacket {
        signatures = Map.copyOf(signatures);
    }

    public ClientCommandChatPacket(BinaryReader reader) {
        this(reader.readSizedString(256), reader.readLong(),
                reader.readLong(), readSignature(reader), reader.readBoolean());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(message);
        writer.writeLong(timestamp);
        writer.writeLong(salt);
        this.signatures.forEach((s, bytes) -> {
            writer.writeSizedString(s);
            writer.writeByteArray(bytes);
        });
        writer.writeBoolean(signed);
    }

    private static Map<String, byte[]> readSignature(BinaryReader reader) {
        final int length = reader.readVarInt();
        Map<String, byte[]> signatures = new HashMap<>();
        for (int i = 0; i < length; i++) {
            final String s = reader.readSizedString(256);
            final byte[] bytes = reader.readByteArray();
            signatures.put(s, bytes);
        }
        return Map.copyOf(signatures);
    }
}
