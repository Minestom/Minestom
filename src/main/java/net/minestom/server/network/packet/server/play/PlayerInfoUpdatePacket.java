package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.ChatSession;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.GameProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public final class PlayerInfoUpdatePacket implements ServerPacket.Play {
    public static final int MAX_ENTRIES = 1024;

    private final @NotNull EnumSet<@NotNull Action> actions;
    private final @NotNull List<@NotNull Entry> entries;

    public PlayerInfoUpdatePacket(@NotNull EnumSet<@NotNull Action> actions, @NotNull List<@NotNull Entry> entries) {
        this.actions = EnumSet.copyOf(actions);
        this.entries = List.copyOf(entries);
    }

    public PlayerInfoUpdatePacket(@NotNull Action action, @NotNull Entry entry) {
        this.actions = EnumSet.of(action);
        this.entries = List.of(entry);
    }

    public PlayerInfoUpdatePacket(@NotNull NetworkBuffer reader) {
        this.actions = reader.readEnumSet(Action.class);
        this.entries = reader.readCollection(buffer -> {
            UUID uuid = buffer.read(NetworkBuffer.UUID);
            String username = "";
            List<Property> properties = List.of();
            boolean listed = false;
            int latency = 0;
            GameMode gameMode = GameMode.SURVIVAL;
            Component displayName = null;
            ChatSession chatSession = null;
            for (Action action : actions) {
                switch (action) {
                    case ADD_PLAYER -> {
                        username = reader.read(STRING);
                        properties = reader.readCollection(Property::new, GameProfile.MAX_PROPERTIES);
                    }
                    case INITIALIZE_CHAT -> chatSession = new ChatSession(reader);
                    case UPDATE_GAME_MODE -> gameMode = reader.readEnum(GameMode.class);
                    case UPDATE_LISTED -> listed = reader.read(BOOLEAN);
                    case UPDATE_LATENCY -> latency = reader.read(VAR_INT);
                    case UPDATE_DISPLAY_NAME -> displayName = reader.readOptional(COMPONENT);
                }
            }
            return new Entry(uuid, username, properties, listed, latency, gameMode, displayName, chatSession);
        }, MAX_ENTRIES);
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

    public @NotNull EnumSet<Action> actions() {
        return actions;
    }

    public @NotNull List<Entry> entries() {
        return entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerInfoUpdatePacket that = (PlayerInfoUpdatePacket) o;
        return actions.equals(that.actions) && entries.equals(that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actions, entries);
    }

    @Override
    public String toString() {
        return "PlayerInfoUpdatePacket{" +
                "actions=" + actions +
                ", entries=" + entries +
                '}';
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
