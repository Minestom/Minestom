package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.crypto.PlayerPublicKey;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerInfoPacket(@NotNull Action action,
                               @NotNull List<Entry> entries) implements ComponentHoldingServerPacket {
    public PlayerInfoPacket {
        entries = List.copyOf(entries);
        for (Entry entry : entries) {
            if (!entry.getClass().equals(action.getClazz()))
                throw new IllegalArgumentException("Invalid entry class for action " + action);
        }
    }

    public PlayerInfoPacket(@NotNull Action action, @NotNull Entry entry) {
        this(action, List.of(entry));
    }

    public PlayerInfoPacket(@NotNull NetworkBuffer reader) {
        this(read(reader));
    }

    private PlayerInfoPacket(PlayerInfoPacket packet) {
        this(packet.action, packet.entries);
    }

    private static PlayerInfoPacket read(@NotNull NetworkBuffer reader) {
        var action = Action.values()[reader.read(VAR_INT)];
        final int playerInfoCount = reader.read(VAR_INT);
        List<Entry> entries = new ArrayList<>(playerInfoCount);
        for (int i = 0; i < playerInfoCount; i++) {
            final UUID uuid = reader.read(UUID);
            entries.add(switch (action) {
                case ADD_PLAYER -> new AddPlayer(uuid, reader);
                case UPDATE_GAMEMODE -> new UpdateGameMode(uuid, reader);
                case UPDATE_LATENCY -> new UpdateLatency(uuid, reader);
                case UPDATE_DISPLAY_NAME -> new UpdateDisplayName(uuid, reader);
                case REMOVE_PLAYER -> new RemovePlayer(uuid);
            });
        }
        return new PlayerInfoPacket(action, entries);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, action.ordinal());
        writer.writeCollection(entries, (w, entry) -> {
            w.write(UUID, entry.uuid());
            entry.write(w);
        });
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_INFO;
    }

    @Override
    public @NotNull Collection<Component> components() {
        switch (this.action) {
            case ADD_PLAYER, UPDATE_DISPLAY_NAME -> {
                List<Component> components = new ArrayList<>();
                for (Entry entry : entries) {
                    if (entry instanceof ComponentHolder) {
                        components.addAll(((ComponentHolder<? extends Entry>) entry).components());
                    }
                }
                return components;
            }
            default -> {
                return List.of();
            }
        }
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return switch (action) {
            case ADD_PLAYER, UPDATE_DISPLAY_NAME -> {
                List<Entry> entries = new ArrayList<>(this.entries.size());
                for (Entry entry : this.entries) {
                    if (entry instanceof ComponentHolder) {
                        entries.add(((ComponentHolder<? extends Entry>) entry).copyWithOperator(operator));
                    } else {
                        entries.add(entry);
                    }
                }
                yield new PlayerInfoPacket(action, entries);
            }
            default -> this;
        };
    }

    public enum Action {
        ADD_PLAYER(AddPlayer.class),
        UPDATE_GAMEMODE(UpdateGameMode.class),
        UPDATE_LATENCY(UpdateLatency.class),
        UPDATE_DISPLAY_NAME(UpdateDisplayName.class),
        REMOVE_PLAYER(RemovePlayer.class);

        private final Class<? extends Entry> clazz;

        Action(Class<? extends Entry> clazz) {
            this.clazz = clazz;
        }

        @NotNull
        public Class<? extends Entry> getClazz() {
            return clazz;
        }
    }

    public sealed interface Entry extends NetworkBuffer.Writer
            permits AddPlayer, UpdateGameMode, UpdateLatency, UpdateDisplayName, RemovePlayer {
        UUID uuid();
    }

    public record AddPlayer(UUID uuid, String name, List<Property> properties, GameMode gameMode, int ping,
                            @Nullable Component displayName,
                            @Nullable PlayerPublicKey playerPublicKey) implements Entry, ComponentHolder<AddPlayer> {
        public AddPlayer {
            properties = List.copyOf(properties);
        }

        public AddPlayer(UUID uuid, NetworkBuffer reader) {
            this(uuid, reader.read(STRING),
                    reader.readCollection(Property::new),
                    GameMode.values()[reader.read(VAR_INT)], reader.read(VAR_INT),
                    reader.read(BOOLEAN) ? reader.read(COMPONENT) : null,
                    reader.read(BOOLEAN) ? new PlayerPublicKey(reader) : null);
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, name);
            writer.writeCollection(properties);
            writer.write(VAR_INT, (int) gameMode.id());
            writer.write(VAR_INT, ping);
            writer.writeOptional(COMPONENT, displayName);
            writer.writeOptional(playerPublicKey);
        }

        @Override
        public @NotNull Collection<Component> components() {
            return displayName != null ? List.of(displayName) : List.of();
        }

        @Override
        public @NotNull AddPlayer copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return displayName != null ?
                    new AddPlayer(uuid, name, properties, gameMode, ping, operator.apply(displayName), playerPublicKey) : this;
        }

        public record Property(@NotNull String name, @NotNull String value,
                               @Nullable String signature) implements NetworkBuffer.Writer {
            public Property(String name, String value) {
                this(name, value, null);
            }

            public Property(@NotNull NetworkBuffer reader) {
                this(reader.read(STRING), reader.read(STRING),
                        reader.read(BOOLEAN) ? reader.read(STRING) : null);
            }

            @Override
            public void write(@NotNull NetworkBuffer writer) {
                writer.write(STRING, name);
                writer.write(STRING, value);
                writer.write(BOOLEAN, signature != null);
                if (signature != null) writer.write(STRING, signature);
            }
        }
    }

    public record UpdateGameMode(UUID uuid, GameMode gameMode) implements Entry {
        public UpdateGameMode(UUID uuid, NetworkBuffer reader) {
            this(uuid, GameMode.fromId(reader.read(VAR_INT).byteValue()));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(VAR_INT, (int) gameMode.id());
        }
    }

    public record UpdateLatency(UUID uuid, int ping) implements Entry {
        public UpdateLatency(UUID uuid, NetworkBuffer reader) {
            this(uuid, reader.read(VAR_INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(VAR_INT, ping);
        }
    }

    public record UpdateDisplayName(@NotNull UUID uuid,
                                    @Nullable Component displayName) implements Entry, ComponentHolder<UpdateDisplayName> {
        public UpdateDisplayName(UUID uuid, NetworkBuffer reader) {
            this(uuid, reader.read(BOOLEAN) ? reader.read(COMPONENT) : null);
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(BOOLEAN, displayName != null);
            if (displayName != null) writer.write(COMPONENT, displayName);
        }

        @Override
        public @NotNull Collection<Component> components() {
            return displayName != null ? List.of(displayName) : List.of();
        }

        @Override
        public @NotNull UpdateDisplayName copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return displayName != null ? new UpdateDisplayName(uuid, operator.apply(displayName)) : this;
        }
    }

    public record RemovePlayer(@NotNull UUID uuid) implements Entry {
        @Override
        public void write(@NotNull NetworkBuffer writer) {
        }
    }
}
