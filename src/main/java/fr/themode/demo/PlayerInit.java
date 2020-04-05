package fr.themode.demo;

import fr.themode.demo.entity.ChickenCreature;
import fr.themode.demo.generator.ChunkGeneratorDemo;
import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.entity.GameMode;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.event.*;
import fr.themode.minestom.instance.InstanceContainer;
import fr.themode.minestom.inventory.Inventory;
import fr.themode.minestom.inventory.InventoryType;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Vector;

public class PlayerInit {

    private static InstanceContainer instanceContainer;

    static {
        ChunkGeneratorDemo chunkGeneratorDemo = new ChunkGeneratorDemo();
        //instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer(new File("chunk_data"));
        instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer();
        instanceContainer.enableAutoChunkLoad(true);
        instanceContainer.setChunkGenerator(chunkGeneratorDemo);
        int loopStart = -2;
        int loopEnd = 2;
        long time = System.currentTimeMillis();
        for (int x = loopStart; x < loopEnd; x++)
            for (int z = loopStart; z < loopEnd; z++) {
                instanceContainer.loadChunk(x, z);
            }
        System.out.println("Time to load all chunks: " + (System.currentTimeMillis() - time) + " ms");
    }

    public static void init() {
        MinecraftServer.getConnectionManager().setPlayerInitialization(player -> {
            player.setEventCallback(AttackEvent.class, event -> {
                Entity entity = event.getTarget();
                if (entity instanceof EntityCreature) {
                    ((EntityCreature) entity).damage(-1);
                    Vector velocity = player.getPosition().clone().getDirection().multiply(6);
                    velocity.setY(4f);
                    entity.setVelocity(velocity, 150);
                    player.sendMessage("You attacked an entity!");
                } else if (entity instanceof Player) {
                    Player target = (Player) entity;
                    Vector velocity = player.getPosition().clone().getDirection().multiply(4);
                    velocity.setY(3.5f);
                    target.setVelocity(velocity, 150);
                    target.damage(1);
                    player.sendMessage("ATTACK");
                }
            });

            player.setEventCallback(PlayerBlockPlaceEvent.class, event -> {
                if (event.getHand() != Player.Hand.MAIN)
                    return;

                for (Player p : player.getInstance().getPlayers()) {
                    if (p != player)
                        p.teleport(player.getPosition());
                }

                ChickenCreature chickenCreature = new ChickenCreature(player.getPosition());
                chickenCreature.setInstance(player.getInstance());

            });

            player.setEventCallback(PickupItemEvent.class, event -> {
                event.setCancelled(!player.getInventory().addItemStack(event.getItemStack())); // Cancel event if player does not have enough inventory space
            });

            player.setEventCallback(PlayerLoginEvent.class, event -> {
                event.setSpawningInstance(instanceContainer);
            });

            player.setEventCallback(PlayerSpawnEvent.class, event -> {
                player.setGameMode(GameMode.SURVIVAL);
                player.teleport(new Position(0, 66, 0));

            /*Random random = new Random();
            for (int i = 0; i < 50; i++) {
                ChickenCreature chickenCreature = new ChickenCreature();
                chickenCreature.refreshPosition(random.nextInt(100), 65, random.nextInt(100));
                chickenCreature.setInstance(getInstance());
            }*/
                //chickenCreature.addPassenger(this);

            /*for (int ix = 0; ix < 4; ix++)
                for (int iz = 0; iz < 4; iz++) {
                    ItemEntity itemEntity = new ItemEntity(new ItemStack(1, (byte) 32));
                    itemEntity.refreshPosition(ix, 68, iz);
                    //itemEntity.setNoGravity(true);
                    itemEntity.setInstance(getInstance());
                    //itemEntity.remove();
                }*/

                ItemStack item = new ItemStack(1, (byte) 43);
                item.setDisplayName("LE NOM DE L'ITEM");
                //item.getLore().add("lol le lore");
                player.getInventory().addItemStack(item);

                Inventory inventory = new Inventory(InventoryType.CHEST_1_ROW, "Test inventory");
                inventory.setInventoryCondition((p, slot, inventoryConditionResult) -> {
                    inventoryConditionResult.setCancel(false);
                });
                inventory.setItemStack(0, item.clone());

                player.openInventory(inventory);

                player.getInventory().addItemStack(new ItemStack(1, (byte) 100));

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
            });
        });
    }

}
