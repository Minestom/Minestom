package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.crypto.ChatSession;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.player.GameProfile;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerInfoUpdatePacket(
        EnumSet<Action> actions,
        List<Entry> entries
) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final int MAX_ENTRIES = 1024;

    public PlayerInfoUpdatePacket {
        actions = EnumSet.copyOf(actions);
        entries = List.copyOf(entries);
    }

    public PlayerInfoUpdatePacket(Action action, Entry entry) {
        this(EnumSet.of(action), List.of(entry));
    }

    public PlayerInfoUpdatePacket(EnumSet<Action> action, Entry entry) {
        this(action, List.of(entry));
    }

    public PlayerInfoUpdatePacket(Action action, List<Entry> entry) {
        this(EnumSet.of(action), entry);
    }

    public static final NetworkBuffer.Type<PlayerInfoUpdatePacket> SERIALIZER = new NetworkBuffer.Type<>() {
        static final Type<EnumSet<Action>> ACTION_ENUM_SET = EnumSet(Action.class);
        @Override
        public void write(NetworkBuffer writer, PlayerInfoUpdatePacket value) {
            writer.write(ACTION_ENUM_SET, value.actions);
            writer.write(Entry.serializer(value.actions).list(MAX_ENTRIES), value.entries);
        }

        @Override
        public PlayerInfoUpdatePacket read(NetworkBuffer reader) {
            var actions = reader.read(ACTION_ENUM_SET);
            var entries = reader.read(Entry.serializer(actions).list(MAX_ENTRIES));
            return new PlayerInfoUpdatePacket(actions, entries);
        }
    };

    @Override
    public Collection<Component> components() {
        final List<Component> components = new ArrayList<>();
        for (final Entry entry : entries) {
            if (entry.displayName() == null) continue;
            components.add(entry.displayName());
        }
        return components;
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        final List<Entry> newEntries = new ArrayList<>(entries.size());
        for (final Entry entry : entries) {
            final Component displayName = entry.displayName();
            if (displayName != null) {
                newEntries.add(new Entry(entry.uuid, entry.username,
                        entry.properties, entry.listed, entry.latency,
                        entry.gameMode, operator.apply(displayName),
                        entry.chatSession, entry.listOrder, entry.displayHat));
            } else {
                newEntries.add(entry);
            }
        }
        return new PlayerInfoUpdatePacket(actions, newEntries);
    }

    public record Entry(UUID uuid, @Nullable String username, @Nullable List<GameProfile.Property> properties,
                        boolean listed, int latency, GameMode gameMode,
                        @Nullable Component displayName, @Nullable ChatSession chatSession,
                        int listOrder, boolean displayHat) {
        public Entry {
            properties = properties != null ? List.copyOf(properties) : null;
        }

        public static NetworkBuffer.Type<Entry> serializer(EnumSet<Action> actions) {
            return new NetworkBuffer.Type<>() {
                @Override
                public void write(NetworkBuffer buffer, Entry value) {
                    buffer.write(NetworkBuffer.UUID, value.uuid);
                    for (Action action : actions) action.writer.write(buffer, value);
                }

                @Override
                public Entry read(NetworkBuffer buffer) {
                    UUID uuid = buffer.read(NetworkBuffer.UUID);
                    String username = null;
                    List<GameProfile.Property> properties = null;
                    boolean listed = false;
                    int latency = 0;
                    GameMode gameMode = GameMode.SURVIVAL;
                    Component displayName = null;
                    ChatSession chatSession = null;
                    int listOrder = 0;
                    boolean displayHat = true;
                    for (Action action : actions) {
                        switch (action) {
                            case ADD_PLAYER -> {
                                username = buffer.read(STRING);
                                properties = buffer.read(GameProfile.Property.SERIALIZER.list(GameProfile.MAX_PROPERTIES));
                            }
                            case INITIALIZE_CHAT -> chatSession = ChatSession.SERIALIZER.optional().read(buffer);
                            case UPDATE_GAME_MODE -> gameMode = buffer.read(GameMode.NETWORK_TYPE);
                            case UPDATE_LISTED -> listed = buffer.read(BOOLEAN);
                            case UPDATE_LATENCY -> latency = buffer.read(VAR_INT);
                            case UPDATE_DISPLAY_NAME -> displayName = buffer.read(OPT_CHAT);
                            case UPDATE_LIST_ORDER -> listOrder = buffer.read(VAR_INT);
                            case UPDATE_HAT -> displayHat = buffer.read(BOOLEAN);
                        }
                    }
                    return new Entry(uuid, username, properties, listed, latency, gameMode, displayName, chatSession, listOrder, displayHat);
                }
            };
        }
    }

    public enum Action {
        ADD_PLAYER((writer, entry) -> {
            writer.write(STRING, entry.username);
            writer.write(GameProfile.Property.SERIALIZER.list(), entry.properties);
        }),
        INITIALIZE_CHAT((writer, entry) -> writer.write(ChatSession.SERIALIZER.optional(), entry.chatSession)),
        UPDATE_GAME_MODE((writer, entry) -> writer.write(GameMode.NETWORK_TYPE, entry.gameMode)),
        UPDATE_LISTED((writer, entry) -> writer.write(BOOLEAN, entry.listed)),
        UPDATE_LATENCY((writer, entry) -> writer.write(VAR_INT, entry.latency)),
        UPDATE_DISPLAY_NAME((writer, entry) -> writer.write(OPT_CHAT, entry.displayName)),
        UPDATE_LIST_ORDER((writer, entry) -> writer.write(VAR_INT, entry.listOrder)),
        UPDATE_HAT((writer, entry) -> writer.write(BOOLEAN, entry.displayHat));

        final Writer writer;

        Action(Writer writer) {
            this.writer = writer;
        }

        interface Writer {
            void write(NetworkBuffer writer, Entry entry);
        }
    }
}
