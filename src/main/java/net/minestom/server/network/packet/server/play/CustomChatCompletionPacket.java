package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CustomChatCompletionPacket(@NotNull Action action,
                                         @NotNull List<@NotNull String> entries) implements ServerPacket {
    public CustomChatCompletionPacket {
        entries = List.copyOf(entries);
    }

    public CustomChatCompletionPacket(BinaryReader reader) {
        this(Action.values()[reader.readVarInt()], reader.readVarIntList(BinaryReader::readSizedString));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());
        writer.writeVarIntList(entries, BinaryWriter::writeSizedString);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CUSTOM_CHAT_COMPLETIONS;
    }

    public enum Action {
        ADD, REMOVE, SET
    }
}
