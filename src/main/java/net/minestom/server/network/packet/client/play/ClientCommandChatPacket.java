package net.minestom.server.network.packet.client.play;

import net.minestom.server.command.ArgumentsSignature;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientCommandChatPacket(@NotNull String message, @NotNull ArgumentsSignature argumentsSignature)
        implements ClientPacket {
    public ClientCommandChatPacket(BinaryReader reader) {
        this(reader.readSizedString(256), new ArgumentsSignature(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(message);
        writer.write(argumentsSignature);
    }

}
