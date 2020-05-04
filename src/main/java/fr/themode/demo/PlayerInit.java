package fr.themode.demo;

import fr.themode.demo.entity.ChickenCreature;
import fr.themode.demo.generator.ChunkGeneratorDemo;
import fr.themode.demo.generator.NoiseTestGenerator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.benchmark.BenchmarkManager;
import net.minestom.server.benchmark.ThreadResult;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.*;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.ping.ResponseDataConsumer;
import net.minestom.server.storage.StorageFolder;
import net.minestom.server.timer.TaskRunnable;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;

import java.util.Map;
import java.util.UUID;

public class PlayerInit {

    private static volatile InstanceContainer instanceContainer;

    static {
        StorageFolder storageFolder = MinecraftServer.getStorageManager().getFolder("chunk_data");
        ChunkGeneratorDemo chunkGeneratorDemo = new ChunkGeneratorDemo();
        NoiseTestGenerator noiseTestGenerator = new NoiseTestGenerator();
        instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer(storageFolder);
        //instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer();
        instanceContainer.enableAutoChunkLoad(true);
        instanceContainer.setChunkGenerator(noiseTestGenerator);

        // Load some chunks beforehand
        int loopStart = -2;
        int loopEnd = 2;
        for (int x = loopStart; x < loopEnd; x++)
            for (int z = loopStart; z < loopEnd; z++) {
                instanceContainer.loadChunk(x, z);
            }
    }

    public static void init() {
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();

        MinecraftServer.getSchedulerManager().addRepeatingTask(new TaskRunnable() {
            @Override
            public void run() {
                long ramUsage = benchmarkManager.getUsedMemory();
                ramUsage /= 1e6; // bytes to MB

                String benchmarkMessage = "";
                for (Map.Entry<String, ThreadResult> resultEntry : benchmarkManager.getResultMap().entrySet()) {
                    String name = resultEntry.getKey();
                    ThreadResult result = resultEntry.getValue();
                    benchmarkMessage += "&7" + name;
                    benchmarkMessage += ": ";
                    benchmarkMessage += "&e" + MathUtils.round(result.getCpuPercentage(), 2) + "% CPU ";
                    benchmarkMessage += "&c" + MathUtils.round(result.getUserPercentage(), 2) + "% USER ";
                    benchmarkMessage += "&d" + MathUtils.round(result.getBlockedPercentage(), 2) + "% BLOCKED ";
                    benchmarkMessage += "\n";
                }
                // if (benchmarkMessage.length() > 0)
                //    System.out.println(benchmarkMessage);

                for (Player player : connectionManager.getOnlinePlayers()) {
                    player.sendHeaderFooter("RAM USAGE: " + ramUsage + " MB", benchmarkMessage, '&');
                }
            }
        }, new UpdateOption(5, TimeUnit.TICK));

        connectionManager.addPacketConsumer((player, packet) -> {
            // Listen to all received packet
            // Returning true means cancelling the packet
            return false;
        });

        connectionManager.addPlayerInitialization(player -> {

            player.addEventCallback(AttackEvent.class, event -> {
                Entity entity = event.getTarget();
                if (entity instanceof EntityCreature) {
                    EntityCreature creature = (EntityCreature) entity;
                    creature.damage(DamageType.fromPlayer(player), -1);
                    Vector velocity = player.getPosition().clone().getDirection().multiply(6);
                    velocity.setY(4f);
                    entity.setVelocity(velocity);
                    player.sendMessage("You attacked an entity!");
                } else if (entity instanceof Player) {
                    Player target = (Player) entity;
                    Vector velocity = player.getPosition().clone().getDirection().multiply(4);
                    velocity.setY(3.5f);
                    target.setVelocity(velocity);
                    target.damage(DamageType.fromPlayer(player), 5);
                    player.sendMessage("ATTACK");
                }
            });

            player.addEventCallback(PlayerBlockPlaceEvent.class, event -> {
                if (event.getHand() != Player.Hand.MAIN)
                    return;

                if (event.getBlockId() == Block.STONE.getBlockId()) {
                    event.setCustomBlockId((short) 2); // custom stone block
                }

                /*for (Player p : player.getInstance().getPlayers()) {
                    if (p != player)
                        p.teleport(player.getPosition());
                }*/

                ChickenCreature chickenCreature = new ChickenCreature(player.getPosition());
                chickenCreature.setInstance(player.getInstance());

            });

            player.addEventCallback(PlayerBlockInteractEvent.class, event -> {
                if (event.getHand() != Player.Hand.MAIN)
                    return;

                short blockId = player.getInstance().getBlockId(event.getBlockPosition());
                Block block = Block.fromId(blockId);
                player.sendMessage("You clicked at the block " + block);
            });

            player.addEventCallback(PickupItemEvent.class, event -> {
                event.setCancelled(!player.getInventory().addItemStack(event.getItemStack())); // Cancel event if player does not have enough inventory space
            });

            player.addEventCallback(ItemDropEvent.class, event -> {
                ItemStack droppedItem = event.getItemStack();

                ItemEntity itemEntity = new ItemEntity(droppedItem);
                itemEntity.setPickupDelay(500);
                itemEntity.refreshPosition(player.getPosition().clone().add(0, 1.5f, 0));
                itemEntity.setInstance(player.getInstance());
                Vector velocity = player.getPosition().clone().getDirection().multiply(6);
                itemEntity.setVelocity(velocity);
            });

            player.addEventCallback(PlayerDisconnectEvent.class, event -> {
                System.out.println("DISCONNECTION " + player.getUsername());
            });

            player.addEventCallback(PlayerLoginEvent.class, event -> {
                event.setSpawningInstance(instanceContainer);
            });

            player.addEventCallback(PlayerSpawnEvent.class, event -> {
                player.setGameMode(GameMode.SURVIVAL);
                player.teleport(new Position(0, 75, 0));

                ItemStack item = new ItemStack(Material.STONE, (byte) 43);
                item.setDisplayName("Item name");
                item.getLore().add("a lore line");
                //item.setEnchantment(Enchantment.SHARPNESS, 2);
                player.getInventory().addItemStack(item);

                Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "Test inventory");
                inventory.addInventoryCondition((p, slot, clickType, inventoryConditionResult) -> {
                    player.sendMessage("click type inventory: " + clickType);
                    System.out.println("slot inv: " + slot);
                    inventoryConditionResult.setCancel(false);
                });
                inventory.setItemStack(0, item.clone());

                player.getInventory().addInventoryCondition((p, slot, clickType, inventoryConditionResult) -> {
                    player.sendMessage("CLICK PLAYER INVENTORY");
                    System.out.println("slot player: " + slot);
                });

                player.openInventory(inventory);

                player.getInventory().addItemStack(new ItemStack(Material.STONE, (byte) 100));
                player.getInventory().addItemStack(new ItemStack(Material.DIAMOND_CHESTPLATE, (byte) 1));

            /*TeamManager teamManager = Main.getTeamManager();
            Team team = teamManager.createTeam(getUsername());
            team.setTeamDisplayName("display");
            team.setPrefix("[Test] ");
            team.setTeamColor(ChatColor.RED);
            setTeam(team);

            setAttribute(Attribute.MAX_HEALTH, 10);
            heal();

            Sidebar scoreboard = new Sidebar("Scoreboard Title");
            for (int i = 0; i < 15; i++) {
                scoreboard.createLine(new Sidebar.ScoreboardLine("id" + i, "Hey guys " + i, i));
            }
            scoreboard.addViewer(this);
            scoreboard.updateLineContent("id3", "I HAVE BEEN UPDATED");

            BelowNameScoreboard belowNameScoreboard = new BelowNameScoreboard();
            setBelowNameScoreboard(belowNameScoreboard);
            belowNameScoreboard.updateScore(this, 50);*/

                player.addEventCallback(PlayerUseItemEvent.class, useEvent -> {
                    player.sendMessage("Using item in air: " + useEvent.getItemStack().getMaterial());
                });

                player.addEventCallback(PlayerUseItemOnBlockEvent.class, useEvent -> {
                    player.sendMessage("Main item: " + player.getInventory().getItemInMainHand().getMaterial());
                    player.sendMessage("Using item on block: " + useEvent.getItemStack().getMaterial() + " at " + useEvent.getPosition() + " on face " + useEvent.getBlockFace());
                });
            });
        });
    }

    public static ResponseDataConsumer getResponseDataConsumer() {
        return (playerConnection, responseData) -> {
            responseData.setName("1.15.2");
            responseData.setProtocol(578);
            responseData.setMaxPlayer(100);
            responseData.setOnline(MinecraftServer.getConnectionManager().getOnlinePlayers().size());
            responseData.addPlayer("A name", UUID.randomUUID());
            responseData.addPlayer("Could be some message", UUID.randomUUID());
            responseData.setDescription("IP test: " + playerConnection.getRemoteAddress());
            responseData.setFavicon("data:image/png;base64,<data>");
        };
    }

}
