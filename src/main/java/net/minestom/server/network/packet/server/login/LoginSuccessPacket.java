package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LoginSuccessPacket implements ServerPacket {

    public UUID uuid;
    public String username;

    /**
     * DO NOT USE.
     */
    private LoginSuccessPacket() {
        this(new UUID(0, 0), "");
    }

    public LoginSuccessPacket(@NotNull UUID uuid, @NotNull String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeUuid(uuid);
        writer.writeSizedString(username);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        uuid = reader.readUuid();
        username = reader.readSizedString();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.LOGIN_SUCCESS;
    }
}
