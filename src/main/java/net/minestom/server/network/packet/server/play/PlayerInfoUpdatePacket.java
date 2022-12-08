package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.ChatSession;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerInfoUpdatePacket(@NotNull EnumSet<@NotNull Action> actions,
                                     @NotNull List<@NotNull Entry> entries) implements ServerPacket {
    public PlayerInfoUpdatePacket(@NotNull Action action, @NotNull Entry entry) {
        this(EnumSet.of(action), List.of(entry));
    }

    public PlayerInfoUpdatePacket {
        actions = EnumSet.copyOf(actions);
        entries = List.copyOf(entries);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeEnumSet(actions, Action.class);
        writer.writeCollection(entries, (buffer, entry) -> {
            buffer.write(NetworkBuffer.UUID, entry.uuid);
            for (Action action : actions) {
                action.writer.write(buffer, entry);
            }
        });
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_INFO_UPDATE;
    }

    public record Entry(UUID uuid, String username, List<Property> properties,
                        boolean listed, int latency, GameMode gameMode,
                        @Nullable Component displayName, @Nullable ChatSession chatSession) {
        public Entry {
            properties = List.copyOf(properties);
        }
    }

    public record Property(@NotNull String name, @NotNull String value,
                           @Nullable String signature) implements NetworkBuffer.Writer {
        public Property(@NotNull String name, @NotNull String value) {
            this(name, value, null);
        }

        public Property(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), reader.read(STRING),
                    reader.readOptional(STRING));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, name);
            writer.write(STRING, value);
            writer.writeOptional(STRING, signature);
        }
    }

    public enum Action {
        ADD_PLAYER((writer, entry) -> {
            writer.write(STRING, entry.username);
            writer.writeCollection(entry.properties);
        }),
        INITIALIZE_CHAT((writer, entry) -> writer.writeOptional(entry.chatSession)),
        UPDATE_GAME_MODE((writer, entry) -> writer.write(VAR_INT, entry.gameMode.ordinal())),
        UPDATE_LISTED((writer, entry) -> writer.write(BOOLEAN, entry.listed)),
        UPDATE_LATENCY((writer, entry) -> writer.write(VAR_INT, entry.latency)),
        UPDATE_DISPLAY_NAME((writer, entry) -> writer.writeOptional(COMPONENT, entry.displayName));

        final Writer writer;

        Action(Writer writer) {
            this.writer = writer;
        }

        interface Writer {
            void write(NetworkBuffer writer, Entry entry);
        }
    }
}
