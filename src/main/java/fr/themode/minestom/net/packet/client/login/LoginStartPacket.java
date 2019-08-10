package fr.themode.minestom.net.packet.client.login;

import fr.adamaq01.ozao.net.Buffer;
import fr.adamaq01.ozao.net.packet.Packet;
import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.entity.demo.ChickenCreature;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.packet.server.login.JoinGamePacket;
import fr.themode.minestom.net.packet.server.login.LoginSuccessPacket;
import fr.themode.minestom.net.packet.server.play.PlayerPositionAndLookPacket;
import fr.themode.minestom.net.packet.server.play.SpawnPositionPacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.Utils;
import fr.themode.minestom.world.*;

public class LoginStartPacket implements ClientPreplayPacket {

    private String username;

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        // TODO send encryption request OR directly login success
        LoginSuccessPacket successPacket = new LoginSuccessPacket(username);
        connection.sendPacket(successPacket);

        connection.setConnectionState(ConnectionState.PLAY);
        connectionManager.createPlayer(connection);
        Player player = connectionManager.getPlayer(connection);
        player.addToWorld();

        // TODO complete login sequence with optionals packets
        JoinGamePacket joinGamePacket = new JoinGamePacket();
        joinGamePacket.entityId = player.getEntityId();
        joinGamePacket.gameMode = GameMode.CREATIVE;
        joinGamePacket.dimension = Dimension.OVERWORLD;
        joinGamePacket.maxPlayers = 0;
        joinGamePacket.levelType = "default";
        joinGamePacket.reducedDebugInfo = false;

        connection.sendPacket(joinGamePacket);

        // TODO minecraft:brand plugin message

        // TODO send server difficulty

        // TODO player abilities

        CustomChunk customChunk = new CustomChunk(CustomBiome.VOID);
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
                customChunk.setBlock(x, 4, z, new CustomBlock(1)); // Stone

        for (int x = -5; x < 5; x++) {
            for (int z = -5; z < 5; z++) {
                Packet packet = ChunkPacketCreator.create(x, z, customChunk, 0, 15);
                connection.getConnection().sendPacket(packet);
            }
        }

        SpawnPositionPacket spawnPositionPacket = new SpawnPositionPacket();
        spawnPositionPacket.x = 0;
        spawnPositionPacket.y = 18;
        spawnPositionPacket.z = 0;
        connection.sendPacket(spawnPositionPacket);

        PlayerPositionAndLookPacket playerPositionAndLookPacket = new PlayerPositionAndLookPacket();
        playerPositionAndLookPacket.x = 0;
        playerPositionAndLookPacket.y = 5;
        playerPositionAndLookPacket.z = 0;
        playerPositionAndLookPacket.yaw = 0;
        playerPositionAndLookPacket.pitch = 0;
        playerPositionAndLookPacket.flags = 0;
        playerPositionAndLookPacket.teleportId = 42;
        connection.sendPacket(playerPositionAndLookPacket);

        for (int x = -20; x < 20; x++)
            for (int z = -20; z < 20; z++) {
                // TODO test entity
                ChickenCreature chickenCreature = new ChickenCreature();
                chickenCreature.setPosition((double) x * 1, 5, (double) z * 1);
                connectionManager.getOnlinePlayers().forEach(p -> chickenCreature.addViewer(p));
                chickenCreature.addToWorld();
            }
    }

    @Override
    public void read(Buffer buffer) {
        this.username = Utils.readString(buffer);
    }
}
