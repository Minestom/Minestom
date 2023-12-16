package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record CustomChatCompletionPacket(@NotNull Action action,
                                         @NotNull List<@NotNull String> entries) implements ServerPacket {
    public CustomChatCompletionPacket {
        entries = List.copyOf(entries);
    }

    public CustomChatCompletionPacket(@NotNull NetworkBuffer reader) {
        this(reader.readEnum(Action.class), reader.readCollection(STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnum(Action.class, action);
        writer.writeCollection(STRING, entries);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.CUSTOM_CHAT_COMPLETIONS;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }

    public enum Action {
        ADD, REMOVE, SET
    }
}
