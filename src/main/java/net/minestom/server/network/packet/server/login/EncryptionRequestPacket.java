package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record EncryptionRequestPacket(@NotNull String serverId,
                                      byte @NotNull [] publicKey,
                                      byte @NotNull [] verifyToken) implements ServerPacket {
    public EncryptionRequestPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING),
                reader.read(BYTE_ARRAY),
                reader.read(BYTE_ARRAY));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, serverId);
        writer.write(BYTE_ARRAY, publicKey);
        writer.write(BYTE_ARRAY, verifyToken);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case LOGIN -> ServerPacketIdentifier.LOGIN_ENCRYPTION_REQUEST;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.LOGIN);
        };
    }
}
