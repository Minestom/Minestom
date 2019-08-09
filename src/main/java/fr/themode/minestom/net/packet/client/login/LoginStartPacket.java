package fr.themode.minestom.net.packet.client.login;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.packet.server.login.JoinGamePacket;
import fr.themode.minestom.net.packet.server.login.LoginSuccessPacket;
import fr.themode.minestom.net.packet.server.play.ChunkDataPacket;
import fr.themode.minestom.net.packet.server.play.PlayerPositionAndLookPacket;
import fr.themode.minestom.net.packet.server.play.SpawnPositionPacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.Utils;
import fr.themode.minestom.world.Dimension;

public class LoginStartPacket implements ClientPreplayPacket {

    private String username;

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        // TODO send encryption request OR directly login success
        LoginSuccessPacket successPacket = new LoginSuccessPacket(username);
        connection.sendPacket(successPacket);

        connection.setConnectionState(ConnectionState.PLAY);
        connectionManager.createPlayer(connection);

        // TODO complete login sequence with optionals packets
        JoinGamePacket joinGamePacket = new JoinGamePacket();
        joinGamePacket.entityId = 32;
        joinGamePacket.gameMode = GameMode.CREATIVE;
        joinGamePacket.dimension = Dimension.OVERWORLD;
        joinGamePacket.maxPlayers = 0;
        joinGamePacket.levelType = "default";
        joinGamePacket.reducedDebugInfo = false;

        connection.sendPacket(joinGamePacket);

        // TODO minecraft:brand plugin message

        // TODO send server difficulty

        // TODO player abilities

        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                ChunkDataPacket.ChunkSection chunkSection = new ChunkDataPacket.ChunkSection();
                chunkSection.bitsPerBlock = 13;
                chunkSection.data = new long[]{0x1001880C0060020L, 0x200D0068004C020L, 1111L};

                ChunkDataPacket chunkDataPacket = new ChunkDataPacket();
                chunkDataPacket.columnX = x;
                chunkDataPacket.columnZ = z;
                chunkDataPacket.fullChunk = true;
                chunkDataPacket.mask = 0xFFFF; // 16 bits
                ChunkDataPacket.ChunkSection[] sections = new ChunkDataPacket.ChunkSection[16];
                for (int i = 0; i < 16; i++) {
                    sections[i] = chunkSection;
                }
                chunkDataPacket.chunkSections = sections;

                connection.sendPacket(chunkDataPacket);
            }
        }

        SpawnPositionPacket spawnPositionPacket = new SpawnPositionPacket();
        spawnPositionPacket.x = 50;
        spawnPositionPacket.y = 5;
        spawnPositionPacket.z = 50;
        connection.sendPacket(spawnPositionPacket);

        PlayerPositionAndLookPacket playerPositionAndLookPacket = new PlayerPositionAndLookPacket();
        playerPositionAndLookPacket.x = 50;
        playerPositionAndLookPacket.y = 5;
        playerPositionAndLookPacket.z = 50;
        playerPositionAndLookPacket.yaw = 0;
        playerPositionAndLookPacket.pitch = 0;
        playerPositionAndLookPacket.flags = 0;
        playerPositionAndLookPacket.teleportId = 42;
        connection.sendPacket(playerPositionAndLookPacket);
    }

    @Override
    public void read(Buffer buffer) {
        this.username = Utils.readString(buffer);
    }
}
