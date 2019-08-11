package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

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
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, this.action.ordinal());
        Utils.writeVarInt(buffer, this.playerInfos.size());
        for (PlayerInfo playerInfo : this.playerInfos) {
            if (!playerInfo.getClass().equals(action.getClazz())) continue;
            buffer.putLong(playerInfo.uuid.getMostSignificantBits());
            buffer.putLong(playerInfo.uuid.getLeastSignificantBits());
            playerInfo.write(buffer);
        }
    }

    @Override
    public int getId() {
        return 0x33;
    }

    public static enum Action {

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

        public abstract void write(Buffer buffer);
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
        public void write(Buffer buffer) {
            Utils.writeString(buffer, name);
            Utils.writeVarInt(buffer, properties.size());
            for (Property property : properties) {
                property.write(buffer);
            }
            Utils.writeVarInt(buffer, gameMode.getId());
            Utils.writeVarInt(buffer, ping);
            buffer.putBoolean(hasDisplayName);
            if (hasDisplayName)
                Utils.writeString(buffer, displayName);
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

            public void write(Buffer buffer) {
                Utils.writeString(buffer, name);
                Utils.writeString(buffer, value);
                buffer.putBoolean(signed);
                if (signed)
                    Utils.writeString(buffer, signature);
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
        public void write(Buffer buffer) {
            Utils.writeVarInt(buffer, gameMode.getId());
        }
    }

    public static class UpdateLatency extends PlayerInfo {

        public int ping;

        public UpdateLatency(UUID uuid, int ping) {
            super(uuid);
            this.ping = ping;
        }

        @Override
        public void write(Buffer buffer) {
            Utils.writeVarInt(buffer, ping);
        }
    }

    public static class UpdateDisplayName extends PlayerInfo {

        public String displayName;

        public UpdateDisplayName(UUID uuid, String displayName) {
            super(uuid);
            this.displayName = displayName;
        }

        @Override
        public void write(Buffer buffer) {
            buffer.putBoolean(true); // ????
            Utils.writeString(buffer, displayName);
        }
    }

    public static class RemovePlayer extends PlayerInfo {

        public RemovePlayer(UUID uuid) {
            super(uuid);
        }

        @Override
        public void write(Buffer buffer) {
        }
    }
}
