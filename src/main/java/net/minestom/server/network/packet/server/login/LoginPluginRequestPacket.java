package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record LoginPluginRequestPacket(int messageId, @NotNull String channel,
                                       byte @Nullable [] data) implements ServerPacket {
    public LoginPluginRequestPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readSizedString(),
                reader.readRemainingBytes());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(messageId);
        writer.writeSizedString(channel);
        if (data != null && data.length > 0) {
            writer.writeBytes(data);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.LOGIN_PLUGIN_REQUEST;
    }
}
