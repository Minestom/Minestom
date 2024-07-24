package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record CustomChatCompletionPacket(@NotNull Action action,
                                         @NotNull List<@NotNull String> entries) implements ServerPacket.Play {
    public static final int MAX_ENTRIES = Short.MAX_VALUE;

    public static final NetworkBuffer.Type<CustomChatCompletionPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.Enum(Action.class), CustomChatCompletionPacket::action,
            STRING.list(MAX_ENTRIES), CustomChatCompletionPacket::entries,
            CustomChatCompletionPacket::new);

    public CustomChatCompletionPacket {
        entries = List.copyOf(entries);
    }

    public enum Action {
        ADD, REMOVE, SET
    }
}
