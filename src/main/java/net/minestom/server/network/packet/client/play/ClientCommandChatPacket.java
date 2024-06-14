package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientCommandChatPacket(@NotNull String message) implements ClientPacket {
    public ClientCommandChatPacket {
        Check.argCondition(message.length() > 256, "Message length cannot be greater than 256");
    }

    public ClientCommandChatPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, message);
    }
}
