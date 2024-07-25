package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.ChatSession;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.GameProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerInfoUpdatePacket(
        @NotNull EnumSet<@NotNull Action> actions,
        @NotNull List<@NotNull Entry> entries
) implements ServerPacket.Play {
    public static final int MAX_ENTRIES = 1024;

    public PlayerInfoUpdatePacket(@NotNull Action action, @NotNull Entry entry) {
        this(EnumSet.of(action), List.of(entry));
    }

    public PlayerInfoUpdatePacket {
        actions = EnumSet.copyOf(actions);
        entries = List.copyOf(entries);
    }

    public static final NetworkBuffer.Type<PlayerInfoUpdatePacket> SERIALIZER = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, PlayerInfoUpdatePacket value) {
            writer.writeEnumSet(value.actions, Action.class);
            writer.writeCollection(value.entries, (buffer, entry) -> {
                buffer.write(NetworkBuffer.UUID, entry.uuid);
                for (Action action : value.actions) {
                    action.writer.write(buffer, entry);
                }
            });
        }

        @Override
        public PlayerInfoUpdatePacket read(@NotNull NetworkBuffer reader) {
            var actions = reader.readEnumSet(Action.class);
            var entries = reader.readCollection(buffer -> {
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
                            properties = reader.readCollection(Property.SERIALIZER, GameProfile.MAX_PROPERTIES);
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
            return new PlayerInfoUpdatePacket(actions, entries);
        }
    };

    public record Entry(UUID uuid, String username, List<Property> properties,
                        boolean listed, int latency, GameMode gameMode,
                        @Nullable Component displayName, @Nullable ChatSession chatSession) {
        public Entry {
            properties = List.copyOf(properties);
        }
    }

    public record Property(@NotNull String name, @NotNull String value, @Nullable String signature) {
        public Property(@NotNull String name, @NotNull String value) {
            this(name, value, null);
        }

        public static final NetworkBuffer.Type<Property> SERIALIZER = NetworkBufferTemplate.template(
                STRING, Property::name,
                STRING, Property::value,
                STRING.optional(), Property::signature,
                Property::new);
    }

    public enum Action {
        ADD_PLAYER((writer, entry) -> {
            writer.write(STRING, entry.username);
            writer.writeCollection(Property.SERIALIZER, entry.properties);
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
