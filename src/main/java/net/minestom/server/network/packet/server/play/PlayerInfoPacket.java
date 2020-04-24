package net.minestom.server.network.packet.server.play;

import net.minestom.server.entity.GameMode;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerInfoPacket implements ServerPacket {

    public Action action;
    public ArrayList<PlayerInfo> playerInfos;

    public PlayerInfoPacket(Action action) {
        this.action = action;
        this.playerInfos = new ArrayList<>();
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(action.ordinal());
        writer.writeVarInt(playerInfos.size());

        for (PlayerInfo playerInfo : this.playerInfos) {
            if (!playerInfo.getClass().equals(action.getClazz())) continue;
            writer.writeUuid(playerInfo.uuid);
            playerInfo.write(writer);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLAYER_INFO;
    }

    public enum Action {

        ADD_PLAYER(AddPlayer.class),
        UPDATE_GAMEMODE(UpdateGamemode.class),
        UPDATE_LATENCY(UpdateLatency.class),
        UPDATE_DISPLAY_NAME(UpdateDisplayName.class),
        REMOVE_PLAYER(RemovePlayer.class);

        private Class<? extends PlayerInfo> clazz;

        Action(Class<? extends PlayerInfo> clazz) {
            this.clazz = clazz;
        }

        public Class<? extends PlayerInfo> getClazz() {
            return clazz;
        }
    }

    public static abstract class PlayerInfo {

        public UUID uuid;

        public PlayerInfo(UUID uuid) {
            this.uuid = uuid;
        }

        public abstract void write(PacketWriter writer);
    }

    public static class AddPlayer extends PlayerInfo {

        public String name;
        public ArrayList<Property> properties;
        public GameMode gameMode;
        public int ping;
        public boolean hasDisplayName = false;
        public String displayName;

        public AddPlayer(UUID uuid, String name, GameMode gameMode, int ping) {
            super(uuid);
            this.name = name;
            this.properties = new ArrayList<>();
            this.gameMode = gameMode;
            this.ping = ping;
        }

        @Override
        public void write(PacketWriter writer) {
            writer.writeSizedString(name);
            writer.writeVarInt(properties.size());
            for (Property property : properties) {
                property.write(writer);
            }
            writer.writeVarInt(gameMode.getId());
            writer.writeVarInt(ping);
            writer.writeBoolean(hasDisplayName);
            if (hasDisplayName)
                writer.writeSizedString(displayName);
        }

        public static class Property {

            public String name;
            public String value;
            public boolean signed = false;
            public String signature;

            public Property(String name, String value) {
                this.name = name;
                this.value = value;
            }

            public void write(PacketWriter writer) {
                writer.writeSizedString(name);
                writer.writeSizedString(value);
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

        @Override
        public void write(PacketWriter writer) {
            writer.writeVarInt(gameMode.getId());
        }
    }

    public static class UpdateLatency extends PlayerInfo {

        public int ping;

        public UpdateLatency(UUID uuid, int ping) {
            super(uuid);
            this.ping = ping;
        }

        @Override
        public void write(PacketWriter writer) {
            writer.writeVarInt(ping);
        }
    }

    public static class UpdateDisplayName extends PlayerInfo {

        public String displayName;

        public UpdateDisplayName(UUID uuid, String displayName) {
            super(uuid);
            this.displayName = displayName;
        }

        @Override
        public void write(PacketWriter writer) {
            writer.writeBoolean(true); // ????
            writer.writeSizedString(displayName);
        }
    }

    public static class RemovePlayer extends PlayerInfo {

        public RemovePlayer(UUID uuid) {
            super(uuid);
        }

        @Override
        public void write(PacketWriter writer) {
        }
    }
}
