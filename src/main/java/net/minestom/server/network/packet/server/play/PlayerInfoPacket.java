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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

public class PlayerInfoPacket implements ComponentHoldingServerPacket {

    public Action action;
    public List<PlayerInfo> playerInfos;

    PlayerInfoPacket() {
        this(Action.UPDATE_DISPLAY_NAME);
    }

    public PlayerInfoPacket(Action action) {
        this.action = action;
        this.playerInfos = new CopyOnWriteArrayList<>();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(action.ordinal());
        writer.writeVarInt(playerInfos.size());

        for (PlayerInfo playerInfo : this.playerInfos) {
            if (!playerInfo.getClass().equals(action.getClazz())) continue;
            writer.writeUuid(playerInfo.uuid);
            playerInfo.write(writer);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        action = Action.values()[reader.readVarInt()];
        int playerInfoCount = reader.readVarInt();

        playerInfos = new ArrayList<>(playerInfoCount);

        for (int i = 0; i < playerInfoCount; i++) {
            UUID uuid = reader.readUuid();
            PlayerInfo info = switch (action) {
                case ADD_PLAYER -> new AddPlayer(uuid, reader);
                case UPDATE_GAMEMODE -> new UpdateGamemode(uuid, reader);
                case UPDATE_LATENCY -> new UpdateLatency(uuid, reader);
                case UPDATE_DISPLAY_NAME -> new UpdateDisplayName(uuid, reader);
                case REMOVE_PLAYER -> new RemovePlayer(uuid);
            };
            playerInfos.set(i, info);
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
                for (PlayerInfo playerInfo : playerInfos) {
                    if (playerInfo instanceof ComponentHolder) {
                        components.addAll(((ComponentHolder<? extends PlayerInfo>) playerInfo).components());
                    }
                }
                return components;
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        switch (this.action) {
            case ADD_PLAYER:
            case UPDATE_DISPLAY_NAME:
                PlayerInfoPacket packet = new PlayerInfoPacket(action);
                packet.playerInfos = new ArrayList<>(playerInfos.size());
                for (PlayerInfo playerInfo : playerInfos) {
                    if (playerInfo instanceof ComponentHolder) {
                        playerInfos.add(((ComponentHolder<? extends PlayerInfo>) playerInfo).copyWithOperator(operator));
                    } else {
                        playerInfos.add(playerInfo);
                    }
                }
            default:
                return this;
        }
    }

    public enum Action {

        ADD_PLAYER(AddPlayer.class),
        UPDATE_GAMEMODE(UpdateGamemode.class),
        UPDATE_LATENCY(UpdateLatency.class),
        UPDATE_DISPLAY_NAME(UpdateDisplayName.class),
        REMOVE_PLAYER(RemovePlayer.class);

        private final Class<? extends PlayerInfo> clazz;

        Action(Class<? extends PlayerInfo> clazz) {
            this.clazz = clazz;
        }

        @NotNull
        public Class<? extends PlayerInfo> getClazz() {
            return clazz;
        }
    }

    public static abstract class PlayerInfo {

        public UUID uuid;

        public PlayerInfo(UUID uuid) {
            this.uuid = uuid;
        }

        public abstract void write(BinaryWriter writer);
    }

    public static class AddPlayer extends PlayerInfo implements ComponentHolder<AddPlayer> {

        public String name;
        public List<Property> properties;
        public GameMode gameMode;
        public int ping;
        public Component displayName;

        public AddPlayer(UUID uuid, String name, GameMode gameMode, int ping) {
            super(uuid);
            this.name = name;
            this.properties = new ArrayList<>();
            this.gameMode = gameMode;
            this.ping = ping;
        }

        AddPlayer(UUID uuid, BinaryReader reader) {
            super(uuid);
            name = reader.readSizedString();
            int propertyCount = reader.readVarInt();

            properties = new ArrayList<>(propertyCount);
            for (int i = 0; i < propertyCount; i++) {
                properties.set(i, new Property(reader));
            }

            gameMode = GameMode.fromId((byte) reader.readVarInt());
            ping = reader.readVarInt();
            boolean hasDisplayName = reader.readBoolean();

            if (hasDisplayName) {
                displayName = reader.readComponent();
            } else {
                displayName = null;
            }
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
            if (hasDisplayName)
                writer.writeComponent(displayName);
        }

        @Override
        public @NotNull Collection<Component> components() {
            if (displayName == null) {
                return Collections.emptyList();
            } else {
                return Collections.singleton(displayName);
            }
        }

        @Override
        public @NotNull AddPlayer copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            if (displayName == null) {
                return this;
            } else {
                AddPlayer addPlayer = new AddPlayer(uuid, name, gameMode, ping);
                addPlayer.displayName = operator.apply(displayName);
                return addPlayer;
            }
        }

        public static class Property {

            public String name;
            public String value;
            public String signature;

            public Property(String name, String value, String signature) {
                this.name = name;
                this.value = value;
                this.signature = signature;
            }

            public Property(String name, String value) {
                this(name, value, null);
            }

            Property(BinaryReader reader) {
                name = reader.readSizedString();
                value = reader.readSizedString();
                boolean hasSignature = reader.readBoolean();

                if (hasSignature) {
                    signature = reader.readSizedString();
                }
            }

            public void write(BinaryWriter writer) {
                writer.writeSizedString(name);
                writer.writeSizedString(value);

                final boolean signed = signature != null;
                writer.writeBoolean(signed);
                if (signed)
                    writer.writeSizedString(signature);
            }
        }
    }

    public static class UpdateGamemode extends PlayerInfo {

        public GameMode gameMode;

        public UpdateGamemode(UUID uuid, GameMode gameMode) {
            super(uuid);
            this.gameMode = gameMode;
        }

        UpdateGamemode(UUID uuid, BinaryReader reader) {
            super(uuid);
            gameMode = GameMode.fromId((byte) reader.readVarInt());
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(gameMode.getId());
        }
    }

    public static class UpdateLatency extends PlayerInfo {

        public int ping;

        public UpdateLatency(UUID uuid, int ping) {
            super(uuid);
            this.ping = ping;
        }

        UpdateLatency(UUID uuid, BinaryReader reader) {
            super(uuid);
            ping = reader.readVarInt();
        }

        @Override
        public void write(BinaryWriter writer) {
            writer.writeVarInt(ping);
        }
    }

    public static class UpdateDisplayName extends PlayerInfo implements ComponentHolder<UpdateDisplayName> {

        public Component displayName;

        public UpdateDisplayName(UUID uuid, Component displayName) {
            super(uuid);
            this.displayName = displayName;
        }

        UpdateDisplayName(UUID uuid, BinaryReader reader) {
            super(uuid);
            boolean hasDisplayName = reader.readBoolean();
            if (hasDisplayName) {
                displayName = reader.readComponent();
            } else {
                displayName = null;
            }
        }

        @Override
        public void write(BinaryWriter writer) {
            final boolean hasDisplayName = displayName != null;
            writer.writeBoolean(hasDisplayName);
            if (hasDisplayName)
                writer.writeComponent(displayName);
        }

        @Override
        public @NotNull Collection<Component> components() {
            if (displayName == null) {
                return Collections.emptyList();
            } else {
                return Collections.singleton(displayName);
            }
        }

        @Override
        public @NotNull UpdateDisplayName copyWithOperator(@NotNull UnaryOperator<Component> operator) {
            if (displayName == null) {
                return this;
            } else {
                return new UpdateDisplayName(uuid, operator.apply(displayName));
            }
        }
    }

    public static class RemovePlayer extends PlayerInfo {

        public RemovePlayer(UUID uuid) {
            super(uuid);
        }

        @Override
        public void write(BinaryWriter writer) {
        }
    }
}
