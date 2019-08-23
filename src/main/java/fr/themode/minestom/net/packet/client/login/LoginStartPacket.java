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

    // Test
    /*private static Instance instance;

    static {
        ChunkGeneratorDemo chunkGeneratorDemo = new ChunkGeneratorDemo();
        instance = Main.getInstanceManager().createInstance(new File("C:\\Users\\themo\\OneDrive\\Bureau\\Minestom data"));
        //instance = Main.getInstanceManager().createInstance();
        instance.setChunkGenerator(chunkGeneratorDemo);
        int loopStart = -2;
        int loopEnd = 2;
        long time = System.currentTimeMillis();
        for (int x = loopStart; x < loopEnd; x++)
            for (int z = loopStart; z < loopEnd; z++) {
                instance.loadChunk(x, z, chunk -> {
                    System.out.println("JE SUIS LE CALLBACK CHUNK");
                });
            }
        System.out.println("Time to load all chunks: " + (System.currentTimeMillis() - time) + " ms");
    }*/

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
        joinGamePacket.viewDistance = 14;
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

        // Next is optional TODO put all that somewhere else (LoginEvent)
        // TODO LoginEvent in another thread (here we are in netty thread)
        Main.getEntityManager().addWaitingPlayer(player);


        // TODO REMOVE EVERYTHING DOWN THERE
        //player.setInstance(instance);

        /*for (int cx = 0; cx < 4; cx++)
            for (int cz = 0; cz < 4; cz++) {
                ChickenCreature chickenCreature = new ChickenCreature();
                chickenCreature.refreshPosition(0 + (float) cx * 1, 65, 0 + (float) cz * 1);
                //chickenCreature.setOnFire(true);
                chickenCreature.setInstance(instance);
                //chickenCreature.addPassenger(player);
            }

        PlayerInventory inventory = player.getInventory();
        inventory.addItemStack(new ItemStack(Material.BOW, (byte) 1));
        inventory.addItemStack(new ItemStack(Material.ARROW, (byte) 100));

        /*Inventory inv = new Inventory(InventoryType.WINDOW_3X3, "Salut je suis le titre");
        inv.setItemStack(0, new ItemStack(1, (byte) 1));
        player.openInventory(inv);
        inv.setItemStack(1, new ItemStack(1, (byte) 2));

        BossBar bossBar = new BossBar("Bossbar Title", BarColor.BLUE, BarDivision.SEGMENT_12);
        bossBar.setProgress(0.75f);
        bossBar.addViewer(player);

        for (int ix = 0; ix < 4; ix++)
            for (int iz = 0; iz < 4; iz++) {
                ItemEntity itemEntity = new ItemEntity(new ItemStack(1, (byte) 32));
                itemEntity.refreshPosition(ix, 66, iz);
                itemEntity.setNoGravity(true);
                itemEntity.setInstance(instance);
                //itemEntity.remove();
            }

        TestArrow arrow = new TestArrow(player);
        arrow.refreshPosition(5, 65, 5);
        arrow.setInstance(instance);
        arrow.setNoGravity(true);

        DeclareCommandsPacket declareCommandsPacket = new DeclareCommandsPacket();
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
