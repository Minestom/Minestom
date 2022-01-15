package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.UnaryOperator;

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

    public PlayerInfoPacket(BinaryReader reader) {
        this(read(reader));
    }

    private PlayerInfoPacket(PlayerInfoPacket packet) {
        this(packet.action, packet.entries);
    }

    private static PlayerInfoPacket read(BinaryReader reader) {
        var action = Action.values()[reader.readVarInt()];
        final int playerInfoCount = reader.readVarInt();
        List<Entry> entries = new ArrayList<>(playerInfoCount);
        for (int i = 0; i < playerInfoCount; i++) {
            final UUID uuid = reader.readUuid();
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
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());
        writer.writeVarIntList(entries, (w, entry) -> {
            w.writeUuid(entry.uuid());
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
            case ADD_PLAYER:
            case UPDATE_DISPLAY_NAME:
                List<Component> components = new ArrayList<>();
                for (Entry entry : entries) {
                    if (entry instanceof ComponentHolder) {
                        components.addAll(((ComponentHolder<? extends Entry>) entry).components());
                    }
                }
                return components;
            default:
                return Collections.emptyList();
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

    public sealed interface Entry extends Writeable
            permits AddPlayer, UpdateGameMode, UpdateLatency, UpdateDisplayName, RemovePlayer {
        UUID uuid();
    }

    public record AddPlayer(UUID uuid, String name, List<Property> properties, GameMode gameMode, int ping,
                            Component displayName) implements Entry, ComponentHolder<AddPlayer> {
        public AddPlayer {
            properties = List.copyOf(properties);
        }

        public AddPlayer(UUID uuid, BinaryReader reader) {
            this(uuid, reader.readSizedString(),
                    reader.readVarIntList(Property::new),
                    GameMode.values()[reader.readVarInt()], reader.readVarInt(),
                    reader.readBoolean() ? reader.readComponent() : null);
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeSizedString(name);
            writer.writeVarIntList(properties, BinaryWriter::write);
            writer.writeVarInt(gameMode.getId());
            writer.writeVarInt(ping);
            writer.writeBoolean(displayName != null);
            if (displayName != null) writer.writeComponent(displayName);
        }

        @Override
        public @NotNull Collection<Component> components() {
            return displayName != null ? Collections.singleton(displayName) : Collections.emptyList();
        }

        @Override
        public @NotNull AddPlayer copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return displayName != null ?
                    new AddPlayer(uuid, name, properties, gameMode, ping, operator.apply(displayName)) : this;
        }

        public record Property(@NotNull String name, @NotNull String value,
                               @Nullable String signature) implements Writeable {
            public Property(String name, String value) {
                this(name, value, null);
            }

            public Property(BinaryReader reader) {
                this(reader.readSizedString(), reader.readSizedString(),
                        reader.readBoolean() ? reader.readSizedString() : null);
            }

            @Override
            public void write(BinaryWriter writer) {
                writer.writeSizedString(name);
                writer.writeSizedString(value);
                writer.writeBoolean(signature != null);
                if (signature != null) writer.writeSizedString(signature);
            }
        }
    }

    public record UpdateGameMode(UUID uuid, GameMode gameMode) implements Entry {
        public UpdateGameMode(UUID uuid, BinaryReader reader) {
            this(uuid, GameMode.fromId((byte) reader.readVarInt()));
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(gameMode.getId());
        }
    }

    public record UpdateLatency(UUID uuid, int ping) implements Entry {
        public UpdateLatency(UUID uuid, BinaryReader reader) {
            this(uuid, reader.readVarInt());
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(ping);
        }
    }

    public record UpdateDisplayName(@NotNull UUID uuid,
                                    @Nullable Component displayName) implements Entry, ComponentHolder<UpdateDisplayName> {
        public UpdateDisplayName(UUID uuid, BinaryReader reader) {
            this(uuid, reader.readBoolean() ? reader.readComponent() : null);
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeBoolean(displayName != null);
            if (displayName != null) writer.writeComponent(displayName);
        }

        @Override
        public @NotNull Collection<Component> components() {
            return displayName != null ? Collections.singleton(displayName) : Collections.emptyList();
        }

        @Override
        public @NotNull UpdateDisplayName copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            return displayName != null ? new UpdateDisplayName(uuid, operator.apply(displayName)) : this;
        }
    }

    public record RemovePlayer(@NotNull UUID uuid) implements Entry {
        @Override
        public void write(BinaryWriter writer) {
        }
    }
}
