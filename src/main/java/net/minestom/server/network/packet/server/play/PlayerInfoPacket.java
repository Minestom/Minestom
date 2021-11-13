package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

public final class PlayerInfoPacket implements ComponentHoldingServerPacket {
    public final Action action;
    public final List<Info> infos;

    public PlayerInfoPacket(Action action, List<Info> infos) {
        this.action = action;
        this.infos = infos;
    }

    public PlayerInfoPacket(BinaryReader reader) {
        this.action = Action.values()[reader.readVarInt()];
        final int playerInfoCount = reader.readVarInt();
        this.infos = new ArrayList<>(playerInfoCount);
        for (int i = 0; i < playerInfoCount; i++) {
            UUID uuid = reader.readUuid();
            Info info = switch (action) {
                case ADD_PLAYER -> new AddPlayer(uuid, reader);
                case UPDATE_GAMEMODE -> new UpdateGameMode(uuid, reader);
                case UPDATE_LATENCY -> new UpdateLatency(uuid, reader);
                case UPDATE_DISPLAY_NAME -> new UpdateDisplayName(uuid, reader);
                case REMOVE_PLAYER -> new RemovePlayer(uuid);
            };
            infos.add(info);
        }
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());
        writer.writeVarInt(infos.size());

        for (Info info : this.infos) {
            if (!info.getClass().equals(action.getClazz())) continue;
            writer.writeUuid(info.uuid());
            info.write(writer);
        }
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
                for (Info info : infos) {
                    if (info instanceof ComponentHolder) {
                        components.addAll(((ComponentHolder<? extends Info>) info).components());
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
                List<Info> infos = new ArrayList<>(this.infos.size());
                for (Info info : this.infos) {
                    if (info instanceof ComponentHolder) {
                        infos.add(((ComponentHolder<? extends Info>) info).copyWithOperator(operator));
                    } else {
                        infos.add(info);
                    }
                }
                yield new PlayerInfoPacket(action, infos);
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

        private final Class<? extends Info> clazz;

        Action(Class<? extends Info> clazz) {
            this.clazz = clazz;
        }

        @NotNull
        public Class<? extends Info> getClazz() {
            return clazz;
        }
    }

    public sealed interface Info
            permits AddPlayer, UpdateGameMode, UpdateLatency, UpdateDisplayName, RemovePlayer {
        void write(BinaryWriter writer);

        UUID uuid();
    }

    public record AddPlayer(UUID uuid, String name, List<Property> properties, GameMode gameMode, int ping,
                            Component displayName) implements Info, ComponentHolder<AddPlayer> {
        public AddPlayer(UUID uuid, BinaryReader reader) {
            this(uuid, reader.readSizedString(),
                    reader.readVarIntList(Property::new),
                    GameMode.values()[reader.readVarInt()], reader.readVarInt(),
                    reader.readBoolean() ? reader.readComponent() : null);
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeSizedString(name);
            writer.writeVarInt(properties.size());
            for (Property property : properties) {
                property.write(writer);
            }
            writer.writeVarInt(gameMode.getId());
            writer.writeVarInt(ping);

            final boolean hasDisplayName = displayName != null;
            writer.writeBoolean(hasDisplayName);
            if (hasDisplayName) writer.writeComponent(displayName);
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

        public record Property(String name, String value, String signature) {
            public Property(String name, String value) {
                this(name, value, null);
            }

            public Property(BinaryReader reader) {
                this(reader.readSizedString(), reader.readSizedString(), reader.readBoolean() ? reader.readSizedString() : null);
            }

            public void write(BinaryWriter writer) {
                writer.writeSizedString(name);
                writer.writeSizedString(value);

                final boolean signed = signature != null;
                writer.writeBoolean(signed);
                if (signed) writer.writeSizedString(signature);
            }
        }
    }

    public record UpdateGameMode(UUID uuid, GameMode gameMode) implements Info {
        public UpdateGameMode(UUID uuid, BinaryReader reader) {
            this(uuid, GameMode.fromId((byte) reader.readVarInt()));
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(gameMode.getId());
        }
    }

    public record UpdateLatency(UUID uuid, int ping) implements Info {
        public UpdateLatency(UUID uuid, BinaryReader reader) {
            this(uuid, reader.readVarInt());
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(ping);
        }
    }

    public record UpdateDisplayName(UUID uuid,
                                    Component displayName) implements Info, ComponentHolder<UpdateDisplayName> {
        public UpdateDisplayName(UUID uuid, BinaryReader reader) {
            this(uuid, reader.readBoolean() ? reader.readComponent() : null);
        }

        @Override
        public void write(BinaryWriter writer) {
            final boolean hasDisplayName = displayName != null;
            writer.writeBoolean(hasDisplayName);
            if (hasDisplayName) writer.writeComponent(displayName);
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

    public record RemovePlayer(UUID uuid) implements Info {
        @Override
        public void write(BinaryWriter writer) {
        }
    }
}
