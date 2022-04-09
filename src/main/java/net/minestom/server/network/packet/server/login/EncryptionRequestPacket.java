package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record EncryptionRequestPacket(@NotNull String serverId,
                                      byte @NotNull [] publicKey,
                                      byte @NotNull [] verifyToken) implements ServerPacket {
    public EncryptionRequestPacket(BinaryReader reader) {
        this(reader.readSizedString(),
                reader.readByteArray(),
                reader.readByteArray());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(serverId);
        writer.writeByteArray(publicKey);
        writer.writeByteArray(verifyToken);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.LOGIN_ENCRYPTION_REQUEST;
    }
}
