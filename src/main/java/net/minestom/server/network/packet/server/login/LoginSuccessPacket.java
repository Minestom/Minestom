package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record LoginSuccessPacket(@NotNull UUID uuid, @NotNull String username) implements ServerPacket {
    public LoginSuccessPacket(BinaryReader reader) {
        this(reader.readUuid(), reader.readSizedString());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeUuid(uuid);
        writer.writeSizedString(username);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.LOGIN_SUCCESS;
    }
}
