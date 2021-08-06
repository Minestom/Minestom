package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class LoginPluginRequestPacket implements ServerPacket {

    public int messageId;
    public String channel = "none";
    public byte[] data;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(messageId);
        writer.writeSizedString(channel);
        if (data != null && data.length > 0) {
            writer.writeBytes(data);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        messageId = reader.readVarInt();
        channel = reader.readSizedString();
        data = reader.readRemainingBytes();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.LOGIN_PLUGIN_REQUEST;
    }
}
