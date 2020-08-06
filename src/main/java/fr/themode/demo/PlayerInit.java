package fr.themode.demo;

import fr.themode.demo.entity.ChickenCreature;
import fr.themode.demo.generator.ChunkGeneratorDemo;
import fr.themode.demo.generator.NoiseTestGenerator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.*;
import net.minestom.server.benchmark.BenchmarkManager;
import net.minestom.server.benchmark.ThreadResult;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.MapMeta;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.ping.ResponseDataConsumer;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;

import java.util.Map;
import java.util.UUID;

public class PlayerInit {
    private static volatile InstanceContainer instanceContainer;
    private static volatile InstanceContainer netherTest;

    private static volatile Inventory inventory;

    static {
        //StorageFolder storageFolder = MinecraftServer.getStorageManager().getFolder("instance_data", new StorageOption().setCompression(true));
        ChunkGeneratorDemo chunkGeneratorDemo = new ChunkGeneratorDemo();
        NoiseTestGenerator noiseTestGenerator = new NoiseTestGenerator();
        //instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer(storageFolder);
        instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer(DimensionType.OVERWORLD);
        instanceContainer.enableAutoChunkLoad(true);
        instanceContainer.setChunkGenerator(noiseTestGenerator);

        netherTest = MinecraftServer.getInstanceManager().createInstanceContainer(DimensionType.NETHER);
        netherTest.enableAutoChunkLoad(true);
        netherTest.setChunkGenerator(noiseTestGenerator);

        InstanceContainer end = MinecraftServer.getInstanceManager().createInstanceContainer(DimensionType.END);
        end.enableAutoChunkLoad(true);
        end.setChunkGenerator(noiseTestGenerator);

        // Load some chunks beforehand
        final int loopStart = -10;
        final int loopEnd = 10;
        for (int x = loopStart; x < loopEnd; x++)
            for (int z = loopStart; z < loopEnd; z++) {
                instanceContainer.loadChunk(x, z);
                netherTest.loadChunk(x, z);
                end.loadChunk(x, z);
            }


        inventory = new Inventory(InventoryType.CHEST_1_ROW, "Test inventory");
        inventory.addInventoryCondition((p, slot, clickType, inventoryConditionResult) -> {
            p.sendMessage("click type inventory: " + clickType);
            System.out.println("slot inv: " + slot);
            inventoryConditionResult.setCancel(false);
        });
        inventory.setItemStack(0, new ItemStack(Material.DIAMOND, (byte) 34));
    }

    public static void init() {
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            String benchmarkMessage = "";
            for (Map.Entry<String, ThreadResult> resultEntry : benchmarkManager.getResultMap().entrySet()) {
                String name = resultEntry.getKey();
                ThreadResult result = resultEntry.getValue();
                benchmarkMessage += ChatColor.GRAY + name;
                benchmarkMessage += ": ";
                benchmarkMessage += ChatColor.YELLOW.toString() + MathUtils.round(result.getCpuPercentage(), 2) + "% CPU ";
                benchmarkMessage += ChatColor.RED.toString() + MathUtils.round(result.getUserPercentage(), 2) + "% USER ";
                benchmarkMessage += ChatColor.PINK.toString() + MathUtils.round(result.getBlockedPercentage(), 2) + "% BLOCKED ";
                benchmarkMessage += ChatColor.BRIGHT_GREEN.toString() + MathUtils.round(result.getWaitedPercentage(), 2) + "% WAITED ";
                benchmarkMessage += "\n";
            }

            for (Player player : connectionManager.getOnlinePlayers()) {
                ColoredText header = ColoredText.of("RAM USAGE: " + ramUsage + " MB");
                ColoredText footer = ColoredText.of(benchmarkMessage);
                player.sendHeaderFooter(header, footer);
            }
        }).repeat(10, TimeUnit.TICK).schedule();

        connectionManager.onPacketReceive((player, packetController, packet) -> {
            // Listen to all received packet
            //System.out.println("PACKET: "+packet.getClass().getSimpleName());
            packetController.setCancel(false);
        });

        connectionManager.addPlayerInitialization(player -> {
            player.addEventCallback(EntityAttackEvent.class, event -> {
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
                    event.setCustomBlock((short) 2); // custom stone block
                }
                if (event.getBlockId() == Block.TORCH.getBlockId()) {
                    event.setCustomBlock((short) 3); // custom torch block
                }

                /*for (Player p : player.getInstance().getPlayers()) {
                    if (p != player)
                        p.teleport(player.getPosition());
                }*/

                ChickenCreature chickenCreature = new ChickenCreature(player.getPosition());
                chickenCreature.setInstance(player.getInstance());

                /*EntityZombie zombie = new EntityZombie(player.getPosition());
                zombie.setAttribute(Attribute.MOVEMENT_SPEED, 0.25f);
                zombie.setInstance(player.getInstance());*/

                /*FakePlayer.initPlayer(UUID.randomUUID(), "test", fakePlayer -> {
                    //fakePlayer.setInstance(player.getInstance());
                    fakePlayer.teleport(player.getPosition());
                    fakePlayer.setSkin(PlayerSkin.fromUsername("TheMode911"));

                    fakePlayer.addEventCallback(EntityDeathEvent.class, e -> {
                        fakePlayer.getController().respawn();
                    });

                    fakePlayer.setArrowCount(25);
                    FakePlayerController controller = fakePlayer.getController();
                    controller.sendChatMessage("I am a bot!");
                });*/
                //Hologram hologram = new Hologram(player.getInstance(), player.getPosition(), "Hey guy");

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

                Position position = player.getPosition().clone().add(0, 1.5f, 0);
                ItemEntity itemEntity = new ItemEntity(droppedItem, position);
                itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
                itemEntity.setInstance(player.getInstance());
                Vector velocity = player.getPosition().clone().getDirection().multiply(6);
                itemEntity.setVelocity(velocity);
            });

            player.addEventCallback(PlayerDisconnectEvent.class, event -> {
                System.out.println("DISCONNECTION " + player.getUsername());
            });

            player.addEventCallback(PlayerLoginEvent.class, event -> {
                //final String url = "https://download.mc-packs.net/pack/a83a04f5d78061e0890e13519fea925550461c74.zip";
                //final String hash = "a83a04f5d78061e0890e13519fea925550461c74";
                //player.setResourcePack(new ResourcePack(url, hash));

                event.setSpawningInstance(instanceContainer);
                player.setEnableRespawnScreen(false);

                player.setPermissionLevel(4);

                player.getInventory().addInventoryCondition((p, slot, clickType, inventoryConditionResult) -> {
                    player.sendMessage("CLICK PLAYER INVENTORY");
                    System.out.println("slot player: " + slot);
                });

                /*Sidebar scoreboard = new Sidebar("Scoreboard Title");
                for (int i = 0; i < 15; i++) {
                    scoreboard.createLine(new Sidebar.ScoreboardLine("id" + i, "Hey guys " + i, i));
                }
                scoreboard.addViewer(player);
                scoreboard.updateLineContent("id3", "I HAVE BEEN UPDATED");

                scoreboard.setTitle("test");*/

                {
                    AdvancementManager advancementManager = MinecraftServer.getAdvancementManager();
                    AdvancementRoot root = new AdvancementRoot(ColoredText.of("title"), ColoredText.of(ChatColor.BLUE + "description"),
                            Material.APPLE, FrameType.TASK, 0, 0,
                            "minecraft:textures/block/red_wool.png");
                    root.setAchieved(true);
                    AdvancementTab tab = advancementManager.createTab("root", root);
                    Advancement advancement = new Advancement(ColoredText.of("adv"), ColoredText.of("desc"),
                            Material.WOODEN_AXE, FrameType.CHALLENGE, 1, 0)
                            .showToast(true).setHidden(false);
                    tab.createAdvancement("second", advancement, root);

                    tab.addViewer(player);

                    root.setTitle(ColoredText.of("test ttlechange"));

                    Advancement advancement2 = new Advancement(ColoredText.of(ChatColor.BLUE + "Title"),
                            ColoredText.of("description of the advancement"),
                            Material.GOLD_BLOCK, FrameType.CHALLENGE, 3, 0)
                            .showToast(true).setHidden(false);
                    tab.createAdvancement("second2", advancement2, root);

                    //player.getPlayerConnection().sendPacket(tab.removePacket());
                }
            });

            player.addEventCallback(PlayerSpawnEvent.class, event -> {
                player.setGameMode(GameMode.CREATIVE);
                player.teleport(new Position(0, 41f, 0));

                //player.setHeldItemSlot((byte) 5);

                player.setGlowing(true);
                player.getInventory().addItemStack(new ItemStack(Material.STONE, (byte) 127));
                /*for (int i = 0; i < 9; i++) {
                    player.getInventory().setItemStack(i, new ItemStack(Material.STONE, (byte) 127));
                }*/

                {
                    ItemStack map = new ItemStack(Material.FILLED_MAP, (byte) 1);
                    MapMeta mapMeta = (MapMeta) map.getItemMeta();
                    mapMeta.setMapId(1);
                    //player.getInventory().setItemStack(0, map);

                }


                ItemStack item = new ItemStack(Material.STONE_SWORD, (byte) 1);
                item.setDisplayName(ColoredText.of("Item name"));
                item.setDamage(5);
                //item.getLore().add(ColoredText.of(ChatColor.RED + "a lore line " + ChatColor.BLACK + " BLACK"));
                //item.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                //item.setEnchantment(Enchantment.SHARPNESS, (short) 50);
                player.getInventory().addItemStack(item);

                player.setHelmet(new ItemStack(Material.DIAMOND_HELMET, (byte) 1));

                player.getInventory().setItemStack(41, ItemStack.getAirItem());

                inventory.addItemStack(item.clone());
                //player.openInventory(inventory);

                //player.getInventory().addItemStack(new ItemStack(Material.STONE, (byte) 100));

                {
                    /*EntityItemFrame entityItemFrame = new EntityItemFrame(new Position(-5, 36, 9, 0, 180), EntityItemFrame.ItemFrameOrientation.DOWN);
                    entityItemFrame.setNoGravity(true);
                    entityItemFrame.setInstance(player.getInstance());
                    entityItemFrame.setItemStack(item);*/
                }

                Instance instance = player.getInstance();
                WorldBorder worldBorder = instance.getWorldBorder();
                worldBorder.setDiameter(30);

                //EntityBoat entityBoat = new EntityBoat(player.getPosition());
                //entityBoat.setInstance(player.getInstance());
                //entityBoat.addPassenger(player);

                //player.getInventory().addItemStack(new ItemStack(Material.DIAMOND_CHESTPLATE, (byte) 1));

            /*TeamManager teamManager = Main.getTeamManager();
            Team team = teamManager.createTeam(getUsername());
            team.setTeamDisplayName("display");
            team.setPrefix("[Test] ");
            team.setTeamColor(ChatColor.RED);
            setTeam(team);

            setAttribute(Attribute.MAX_HEALTH, 10);
            heal();

            BelowNameScoreboard belowNameScoreboard = new BelowNameScoreboard();
            setBelowNameScoreboard(belowNameScoreboard);
            belowNameScoreboard.updateScore(this, 50);*/

                //player.sendLegacyMessage("&aIm &bHere", '&');
                //player.sendMessage(ColoredText.of("{#ff55ff}" + ChatColor.RESET + "test"));

            });

            player.addEventCallback(PlayerRespawnEvent.class, event -> {
                event.setRespawnPosition(new Position(0f, 41f, 0f));
            });

            player.addEventCallback(PlayerCommandEvent.class, event -> {
                System.out.println("COMMAND EVENT");
            });

            player.addEventCallback(PlayerUseItemEvent.class, useEvent -> {
                player.sendMessage("Using item in air: " + useEvent.getItemStack().getMaterial());
            });

            player.addEventCallback(PlayerUseItemOnBlockEvent.class, useEvent -> {
                player.sendMessage("Main item: " + player.getInventory().getItemInMainHand().getMaterial());
                player.sendMessage("Using item on block: " + useEvent.getItemStack().getMaterial() + " at " + useEvent.getPosition() + " on face " + useEvent.getBlockFace());
            });

            player.addEventCallback(ItemUpdateStateEvent.class, event -> {
                System.out.println("ITEM UPDATE STATE");
            });

            player.addEventCallback(PlayerPreEatEvent.class, event -> {
                ItemStack itemStack = event.getFoodItem();
                Material material = itemStack.getMaterial();
                event.setEatingTime(material == Material.PORKCHOP ? 100 : 1000);
            });

            player.addEventCallback(PlayerEatEvent.class, event -> {
                System.out.println("PLAYER EAT EVENT");
            });

            /*player.addEventCallback(PlayerChunkUnloadEvent.class, event -> {
                Instance instance = player.getInstance();

                Chunk chunk = instance.getChunk(event.getChunkX(), event.getChunkZ());

                if (chunk == null)
                    return;

                // Unload the chunk (save memory) if it has no remaining viewer
                if (chunk.getViewers().isEmpty()) {
                    player.getInstance().unloadChunk(chunk);
                }
            });*/

        });
    }

    public static ResponseDataConsumer getResponseDataConsumer() {
        return (playerConnection, responseData) -> {
            responseData.setMaxPlayer(0);
            responseData.setOnline(MinecraftServer.getConnectionManager().getOnlinePlayers().size());
            responseData.addPlayer("A name", UUID.randomUUID());
            responseData.addPlayer("Could be some message", UUID.randomUUID());
            responseData.setDescription("IP test: " + playerConnection.getRemoteAddress());
        };
    }

}
