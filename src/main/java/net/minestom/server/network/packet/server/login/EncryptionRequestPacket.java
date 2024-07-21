package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EncryptionRequestPacket(
        @NotNull String serverId,
        byte @NotNull [] publicKey,
        byte @NotNull [] verifyToken,
        boolean shouldAuthenticate
) implements ServerPacket.Login {

    public EncryptionRequestPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING),
                reader.read(BYTE_ARRAY),
                reader.read(BYTE_ARRAY),
                reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, serverId);
        writer.write(BYTE_ARRAY, publicKey);
        writer.write(BYTE_ARRAY, verifyToken);
        writer.write(BOOLEAN, shouldAuthenticate);
    }

}
