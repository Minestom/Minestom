package fr.themode.minestom.net.packet.client.login;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.Main;
import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.packet.server.login.JoinGamePacket;
import fr.themode.minestom.net.packet.server.login.LoginSuccessPacket;
import fr.themode.minestom.net.packet.server.play.PlayerInfoPacket;
import fr.themode.minestom.net.packet.server.play.PlayerPositionAndLookPacket;
import fr.themode.minestom.net.packet.server.play.SpawnPositionPacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.Utils;
import fr.themode.minestom.world.Dimension;
import fr.themode.minestom.world.LevelType;

import java.util.UUID;

public class LoginStartPacket implements ClientPreplayPacket {

    public String username;

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        String property = "eyJ0aW1lc3RhbXAiOjE1NjU0ODMwODQwOTYsInByb2ZpbGVJZCI6ImFiNzBlY2I0MjM0NjRjMTRhNTJkN2EwOTE1MDdjMjRlIiwicHJvZmlsZU5hbWUiOiJUaGVNb2RlOTExIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RkOTE2NzJiNTE0MmJhN2Y3MjA2ZTRjN2IwOTBkNzhlM2Y1ZDc2NDdiNWFmZDIyNjFhZDk4OGM0MWI2ZjcwYTEifX19";
        /*HashMap<String, UUID> uuids = new HashMap<>();
        uuids.put("TheMode911", UUID.fromString("ab70ecb4-2346-4c14-a52d-7a091507c24e"));
        uuids.put("Adamaq01", UUID.fromString("58ffa9d8-aee1-4587-8b79-41b754f6f238"));
        HashMap<String, String> properties = new HashMap<>();
        properties.put("TheMode911", "eyJ0aW1lc3RhbXAiOjE1NjU0ODMwODQwOTYsInByb2ZpbGVJZCI6ImFiNzBlY2I0MjM0NjRjMTRhNTJkN2EwOTE1MDdjMjRlIiwicHJvZmlsZU5hbWUiOiJUaGVNb2RlOTExIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RkOTE2NzJiNTE0MmJhN2Y3MjA2ZTRjN2IwOTBkNzhlM2Y1ZDc2NDdiNWFmZDIyNjFhZDk4OGM0MWI2ZjcwYTEifX19");
        properties.put("Adamaq01", "eyJ0aW1lc3RhbXAiOjE1NjU0NzgyODU4MTksInByb2ZpbGVJZCI6IjU4ZmZhOWQ4YWVlMTQ1ODc4Yjc5NDFiNzU0ZjZmMjM4IiwicHJvZmlsZU5hbWUiOiJBZGFtYXEwMSIsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lMTNiNmMyMjNlMTFiYjM1Nzc5OTdkZWY3YzA2ZDUwZmM4NzMxYjBkZWQyOTRlZDQ2ZmM4ZDczNDI1NGM5ZTkifSwiQ0FQRSI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2IwY2MwODg0MDcwMDQ0NzMyMmQ5NTNhMDJiOTY1ZjFkNjVhMTNhNjAzYmY2NGIxN2M4MDNjMjE0NDZmZTE2MzUifX19");*/

        // TODO send encryption request OR directly login success
        UUID playerUuid = UUID.randomUUID();//UUID.fromString("OfflinePlayer:" + username);
        LoginSuccessPacket successPacket = new LoginSuccessPacket(playerUuid, username);//new LoginSuccessPacket(uuids.get(username), username);
        connection.sendPacket(successPacket);

        connection.setConnectionState(ConnectionState.PLAY);
        connectionManager.createPlayer(playerUuid, username, connection);
        Player player = connectionManager.getPlayer(connection);
        GameMode gameMode = GameMode.SURVIVAL;
        Dimension dimension = Dimension.OVERWORLD;
        LevelType levelType = LevelType.DEFAULT;
        float x = 0;
        float y = 0;
        float z = 0;

        player.refreshDimension(dimension);
        player.refreshGameMode(gameMode);
        player.refreshLevelType(levelType);
        player.refreshPosition(x, y, z);

        // TODO complete login sequence with optionals packets
        JoinGamePacket joinGamePacket = new JoinGamePacket();
        joinGamePacket.entityId = player.getEntityId();
        joinGamePacket.gameMode = gameMode;
        joinGamePacket.dimension = dimension;
        joinGamePacket.maxPlayers = 0; // Unused
        joinGamePacket.levelType = levelType;
        joinGamePacket.viewDistance = Main.CHUNK_VIEW_DISTANCE;
        joinGamePacket.reducedDebugInfo = false;
        connection.sendPacket(joinGamePacket);

        // TODO minecraft:brand plugin message

        // TODO send server difficulty

        // TODO player abilities


        SpawnPositionPacket spawnPositionPacket = new SpawnPositionPacket();
        spawnPositionPacket.x = 0;
        spawnPositionPacket.y = 18;
        spawnPositionPacket.z = 0;
        connection.sendPacket(spawnPositionPacket);

        PlayerPositionAndLookPacket playerPositionAndLookPacket = new PlayerPositionAndLookPacket();
        playerPositionAndLookPacket.position = player.getPosition();
        playerPositionAndLookPacket.flags = 0;
        playerPositionAndLookPacket.teleportId = 42;
        connection.sendPacket(playerPositionAndLookPacket);

        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);
        PlayerInfoPacket.AddPlayer addPlayer = new PlayerInfoPacket.AddPlayer(player.getUuid(), username, GameMode.CREATIVE, 10);
        PlayerInfoPacket.AddPlayer.Property prop = new PlayerInfoPacket.AddPlayer.Property("textures", property); //new PlayerInfoPacket.AddPlayer.Property("textures", properties.get(username));
        addPlayer.properties.add(prop);
        playerInfoPacket.playerInfos.add(addPlayer);
        connection.sendPacket(playerInfoPacket);

        Main.getEntityManager().addWaitingPlayer(player);

        /*DeclareCommandsPacket declareCommandsPacket = new DeclareCommandsPacket();
        DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();
        argumentNode.flags = 0b1010;
        argumentNode.children = new int[0];
        argumentNode.name = "arg name";
        argumentNode.parser = "minecraft:nbt_path";
        DeclareCommandsPacket.Node literalNode = new DeclareCommandsPacket.Node();
        literalNode.flags = 0b1;
        literalNode.children = new int[]{2};
        literalNode.name = "hey";
        DeclareCommandsPacket.Node rootNode = new DeclareCommandsPacket.Node();
        rootNode.flags = 0;
        rootNode.children = new int[]{1};

        declareCommandsPacket.nodes = new DeclareCommandsPacket.Node[]{rootNode, literalNode, argumentNode};
        declareCommandsPacket.rootIndex = 0;


        connection.sendPacket(declareCommandsPacket);*/
    }

    @Override
    public void read(Buffer buffer) {
        this.username = Utils.readString(buffer);
    }
}
