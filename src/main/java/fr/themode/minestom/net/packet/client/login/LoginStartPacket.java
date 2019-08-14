package fr.themode.minestom.net.packet.client.login;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.Main;
import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.entity.demo.ChickenCreature;
import fr.themode.minestom.instance.Block;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.inventory.Inventory;
import fr.themode.minestom.inventory.InventoryType;
import fr.themode.minestom.inventory.PlayerInventory;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.ConnectionManager;
import fr.themode.minestom.net.ConnectionState;
import fr.themode.minestom.net.packet.client.ClientPreplayPacket;
import fr.themode.minestom.net.packet.server.login.JoinGamePacket;
import fr.themode.minestom.net.packet.server.login.LoginSuccessPacket;
import fr.themode.minestom.net.packet.server.play.PlayerInfoPacket;
import fr.themode.minestom.net.packet.server.play.PlayerPositionAndLookPacket;
import fr.themode.minestom.net.packet.server.play.SpawnPlayerPacket;
import fr.themode.minestom.net.packet.server.play.SpawnPositionPacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.Utils;
import fr.themode.minestom.world.Dimension;

import java.util.HashMap;
import java.util.UUID;

public class LoginStartPacket implements ClientPreplayPacket {

    private String username;

    // Test
    private static Instance instance;

    static {
        instance = Main.getInstanceManager().createInstance();
        for (int x = -64; x < 64; x++)
            for (int z = -64; z < 64; z++) {
                instance.setBlock(x, 4, z, new Block(10));
            }
    }

    @Override
    public void process(PlayerConnection connection, ConnectionManager connectionManager) {
        HashMap<String, UUID> uuids = new HashMap<>();
        uuids.put("TheMode911", UUID.fromString("ab70ecb4-2346-4c14-a52d-7a091507c24e"));
        uuids.put("Adamaq01", UUID.fromString("58ffa9d8-aee1-4587-8b79-41b754f6f238"));
        HashMap<String, String> properties = new HashMap<>();
        properties.put("TheMode911", "eyJ0aW1lc3RhbXAiOjE1NjU0ODMwODQwOTYsInByb2ZpbGVJZCI6ImFiNzBlY2I0MjM0NjRjMTRhNTJkN2EwOTE1MDdjMjRlIiwicHJvZmlsZU5hbWUiOiJUaGVNb2RlOTExIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RkOTE2NzJiNTE0MmJhN2Y3MjA2ZTRjN2IwOTBkNzhlM2Y1ZDc2NDdiNWFmZDIyNjFhZDk4OGM0MWI2ZjcwYTEifX19");
        properties.put("Adamaq01", "eyJ0aW1lc3RhbXAiOjE1NjU0NzgyODU4MTksInByb2ZpbGVJZCI6IjU4ZmZhOWQ4YWVlMTQ1ODc4Yjc5NDFiNzU0ZjZmMjM4IiwicHJvZmlsZU5hbWUiOiJBZGFtYXEwMSIsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lMTNiNmMyMjNlMTFiYjM1Nzc5OTdkZWY3YzA2ZDUwZmM4NzMxYjBkZWQyOTRlZDQ2ZmM4ZDczNDI1NGM5ZTkifSwiQ0FQRSI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2IwY2MwODg0MDcwMDQ0NzMyMmQ5NTNhMDJiOTY1ZjFkNjVhMTNhNjAzYmY2NGIxN2M4MDNjMjE0NDZmZTE2MzUifX19");

        // TODO send encryption request OR directly login success
        LoginSuccessPacket successPacket = new LoginSuccessPacket(uuids.get(username), username);
        connection.sendPacket(successPacket);

        connection.setConnectionState(ConnectionState.PLAY);
        connectionManager.createPlayer(uuids.get(username), username, connection);
        Player player = connectionManager.getPlayer(connection);
        GameMode gameMode = GameMode.SURVIVAL;

        player.refreshGameMode(gameMode);

        // TODO complete login sequence with optionals packets
        JoinGamePacket joinGamePacket = new JoinGamePacket();
        joinGamePacket.entityId = player.getEntityId();
        joinGamePacket.gameMode = gameMode;
        joinGamePacket.dimension = Dimension.OVERWORLD;
        joinGamePacket.maxPlayers = 0; // Unused
        joinGamePacket.levelType = "default";
        joinGamePacket.viewDistance = 14;
        joinGamePacket.reducedDebugInfo = false;
        connection.sendPacket(joinGamePacket);

        // TODO minecraft:brand plugin message

        // TODO send server difficulty

        // TODO player abilities

        player.setInstance(instance);


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

        PlayerInfoPacket playerInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);
        PlayerInfoPacket.AddPlayer addPlayer = new PlayerInfoPacket.AddPlayer(uuids.get(username), username, GameMode.CREATIVE, 10);
        PlayerInfoPacket.AddPlayer.Property property = new PlayerInfoPacket.AddPlayer.Property("textures", properties.get(username));
        addPlayer.properties.add(property);
        playerInfoPacket.playerInfos.add(addPlayer);
        connection.sendPacket(playerInfoPacket);

        for (int x = 0; x < 4; x++)
            for (int z = 0; z < 4; z++) {
                // TODO test entity
                ChickenCreature chickenCreature = new ChickenCreature();
                chickenCreature.refreshPosition(0 + (double) x * 1, 5, 0 + (double) z * 1);
                chickenCreature.setInstance(instance);
            }


        SpawnPlayerPacket spawnPlayerPacket = new SpawnPlayerPacket();
        spawnPlayerPacket.entityId = player.getEntityId();
        spawnPlayerPacket.playerUuid = uuids.get(username);
        spawnPlayerPacket.x = 0;
        spawnPlayerPacket.y = 5;
        spawnPlayerPacket.z = 0;
        for (Player onlinePlayer : connectionManager.getOnlinePlayers()) {
            if (onlinePlayer.getUsername().equals(username)) continue;
            onlinePlayer.getPlayerConnection().sendPacket(playerInfoPacket);
            onlinePlayer.getPlayerConnection().sendPacket(spawnPlayerPacket);

            SpawnPlayerPacket spawnPacket = new SpawnPlayerPacket();
            spawnPacket.entityId = onlinePlayer.getEntityId();
            spawnPacket.playerUuid = uuids.get(onlinePlayer.getUsername());
            spawnPacket.x = onlinePlayer.getX();
            spawnPacket.y = onlinePlayer.getY();
            spawnPacket.z = onlinePlayer.getZ();

            PlayerInfoPacket pInfoPacket = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER);
            PlayerInfoPacket.AddPlayer addP = new PlayerInfoPacket.AddPlayer(uuids.get(onlinePlayer.getUsername()), onlinePlayer.getUsername(), GameMode.CREATIVE, 10);
            PlayerInfoPacket.AddPlayer.Property p = new PlayerInfoPacket.AddPlayer.Property("textures", properties.get(onlinePlayer.getUsername()));
            addP.properties.add(p);
            pInfoPacket.playerInfos.add(addP);
            connection.sendPacket(pInfoPacket);
            connection.sendPacket(spawnPacket);
        }
        //System.out.println("HAHAHAHHAHHAH               " + player.getUuid());

        PlayerInventory inventory = player.getInventory();
        inventory.addItemStack(new ItemStack(1, (byte) 60));
        inventory.addItemStack(new ItemStack(1, (byte) 50));

        Inventory inv = new Inventory(InventoryType.WINDOW_3X3, "Salut je suis le titre");
        inv.setItemStack(0, new ItemStack(1, (byte) 1));
        player.openInventory(inv);
        inv.setItemStack(1, new ItemStack(1, (byte) 2));
        inv.updateItems();

    }

    @Override
    public void read(Buffer buffer) {
        this.username = Utils.readString(buffer);
    }
}
