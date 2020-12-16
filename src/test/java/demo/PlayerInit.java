package demo;

import demo.generator.ChunkGeneratorDemo;
import demo.generator.NoiseTestGenerator;
import net.minestom.server.MinecraftServer;
import net.minestom.server.benchmark.BenchmarkManager;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.type.monster.EntityZombie;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.ping.ResponseDataConsumer;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerInit {

    private static final InstanceContainer instanceContainer;

    private static final Inventory inventory;

    static {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        //StorageLocation storageLocation = MinecraftServer.getStorageManager().getLocation("instance_data", new StorageOptions().setCompression(true));
        ChunkGeneratorDemo chunkGeneratorDemo = new ChunkGeneratorDemo();
        NoiseTestGenerator noiseTestGenerator = new NoiseTestGenerator();
        instanceContainer = instanceManager.createInstanceContainer(DimensionType.OVERWORLD);
        instanceContainer.enableAutoChunkLoad(true);
        instanceContainer.setChunkGenerator(chunkGeneratorDemo);

        // Load some chunks beforehand
        final int loopStart = -10;
        final int loopEnd = 10;
        for (int x = loopStart; x < loopEnd; x++)
            for (int z = loopStart; z < loopEnd; z++) {
                //instanceContainer.loadChunk(x, z);
            }

        inventory = new Inventory(InventoryType.CHEST_1_ROW, "Test inventory");
        /*inventory.addInventoryCondition((p, slot, clickType, inventoryConditionResult) -> {
            p.sendMessage("click type inventory: " + clickType);
            System.out.println("slot inv: " + slot)0;
            inventoryConditionResult.setCancel(slot == 3);
        });*/
        //inventory.setItemStack(3, new ItemStack(Material.DIAMOND, (byte) 34));
    }

    public static void init() {
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();

        MinecraftServer.getSchedulerManager().buildTask(() -> {

            Collection<Player> players = connectionManager.getOnlinePlayers();

            if (players.isEmpty())
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            final ColoredText header = ColoredText.of("RAM USAGE: " + ramUsage + " MB");
            final ColoredText footer = ColoredText.of(benchmarkManager.getCpuMonitoringMessage());

            {
                PlayerListHeaderAndFooterPacket playerListHeaderAndFooterPacket = new PlayerListHeaderAndFooterPacket();
                playerListHeaderAndFooterPacket.header = header;
                playerListHeaderAndFooterPacket.footer = footer;

                PacketUtils.sendGroupedPacket(players, playerListHeaderAndFooterPacket);
            }

        }).repeat(10, TimeUnit.TICK).schedule();

        connectionManager.onPacketReceive((player, packetController, packet) -> {
            // Listen to all received packet
            //System.out.println("PACKET: "+packet.getClass().getSimpleName());
            packetController.setCancel(false);
        });

        connectionManager.onPacketSend((players, packetController, packet) -> {
            // Listen to all sent packet
            //System.out.println("PACKET: " + packet.getClass().getSimpleName());
            packetController.setCancel(false);
        });

        // EVENT REGISTERING

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();

        globalEventHandler.addEventCallback(EntityAttackEvent.class, event -> {
            final Entity source = event.getEntity();
            final Entity entity = event.getTarget();
            if (entity instanceof EntityCreature) {
                EntityCreature creature = (EntityCreature) entity;
                creature.damage(DamageType.fromEntity(source), 0);
                Vector velocity = source.getPosition().clone().getDirection().multiply(3);
                velocity.setY(3f);
                entity.setVelocity(velocity);
            } else if (entity instanceof Player) {
                Player target = (Player) entity;
                Vector velocity = source.getPosition().clone().getDirection().multiply(4);
                velocity.setY(3.5f);
                target.setVelocity(velocity);
                target.damage(DamageType.fromEntity(source), 5);
            }

            if (source instanceof Player) {
                ((Player) source).sendMessage("You attacked something!");
            }
        });

        globalEventHandler.addEventCallback(PlayerBlockPlaceEvent.class, event -> {
            if (event.getHand() != Player.Hand.MAIN)
                return;

            final Block block = Block.fromStateId(event.getBlockStateId());

            if (block == Block.STONE) {
                event.setCustomBlock("custom_block");
                System.out.println("custom stone");
            }
            if (block == Block.TORCH) {
                event.setCustomBlock((short) 3); // custom torch block
            }

        });

        globalEventHandler.addEventCallback(PlayerBlockInteractEvent.class, event -> {
            if (event.getHand() != Player.Hand.MAIN)
                return;
            final Player player = event.getPlayer();

            final short blockStateId = player.getInstance().getBlockStateId(event.getBlockPosition());
            final CustomBlock customBlock = player.getInstance().getCustomBlock(event.getBlockPosition());
            final Block block = Block.fromStateId(blockStateId);
            player.sendMessage("You clicked at the block " + block + " " + customBlock);
            player.sendMessage("CHUNK COUNT " + instanceContainer.getChunks().size());
        });

        globalEventHandler.addEventCallback(PickupItemEvent.class, event -> {
            final Entity entity = event.getLivingEntity();
            if (entity instanceof Player) {
                // Cancel event if player does not have enough inventory space
                event.setCancelled(!((Player) entity).getInventory().addItemStack(event.getItemStack()));
            }
        });

        globalEventHandler.addEventCallback(ItemDropEvent.class, event -> {
            final Player player = event.getPlayer();
            ItemStack droppedItem = event.getItemStack();

            Position position = player.getPosition().clone().add(0, 1.5f, 0);
            ItemEntity itemEntity = new ItemEntity(droppedItem, position);
            itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
            itemEntity.setInstance(player.getInstance());
            Vector velocity = player.getPosition().clone().getDirection().multiply(6);
            itemEntity.setVelocity(velocity);

            EntityZombie entityZombie = new EntityZombie(new Position(0, 41, 0));
            entityZombie.setInstance(player.getInstance());
            entityZombie.setPathTo(player.getPosition());
        });

        globalEventHandler.addEventCallback(PlayerDisconnectEvent.class, event -> {
            final Player player = event.getPlayer();
            System.out.println("DISCONNECTION " + player.getUsername());
        });

        globalEventHandler.addEventCallback(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();

            event.setSpawningInstance(instanceContainer);
            int x = Math.abs(ThreadLocalRandom.current().nextInt()) % 1000 - 250;
            int z = Math.abs(ThreadLocalRandom.current().nextInt()) % 1000 - 250;
            player.setRespawnPoint(new Position(0, 42f, 0));

            player.getInventory().addInventoryCondition((p, slot, clickType, inventoryConditionResult) -> {
                if (slot == -999)
                    return;
                //ItemStack itemStack = p.getInventory().getItemStack(slot);
                //System.out.println("test " + itemStack.getIdentifier() + " " + itemStack.getData());
            });
        });

        globalEventHandler.addEventCallback(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            player.setGameMode(GameMode.SURVIVAL);

            PlayerInventory inventory = player.getInventory();
            ItemStack itemStack = new ItemStack(Material.STONE, (byte) 64);
            inventory.addItemStack(itemStack);

            {
                ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE, (byte) 1);
                inventory.setChestplate(item);
                item.setDisplayName(ColoredText.of("test"));

                inventory.refreshSlot((short) PlayerInventoryUtils.CHESTPLATE_SLOT);

            }

            //player.getInventory().addItemStack(new ItemStack(Material.STONE, (byte) 32));
        });

        globalEventHandler.addEventCallback(PlayerBlockBreakEvent.class, event -> {
            final short blockStateId = event.getBlockStateId();
            System.out.println("broke " + blockStateId + " " + Block.fromStateId(blockStateId));
        });

        globalEventHandler.addEventCallback(PlayerUseItemEvent.class, useEvent -> {
            final Player player = useEvent.getPlayer();
            player.sendMessage("Using item in air: " + useEvent.getItemStack().getMaterial());
        });

        globalEventHandler.addEventCallback(PlayerUseItemOnBlockEvent.class, useEvent -> {
            final Player player = useEvent.getPlayer();
            player.sendMessage("Main item: " + player.getInventory().getItemInMainHand().getMaterial());
            player.sendMessage("Using item on block: " + useEvent.getItemStack().getMaterial() + " at " + useEvent.getPosition() + " on face " + useEvent.getBlockFace());
        });

        globalEventHandler.addEventCallback(PlayerChunkUnloadEvent.class, event -> {
            final Player player = event.getPlayer();
            final Instance instance = player.getInstance();

            Chunk chunk = instance.getChunk(event.getChunkX(), event.getChunkZ());

            if (chunk == null)
                return;

            // Unload the chunk (save memory) if it has no remaining viewer
            if (chunk.getViewers().isEmpty()) {
                //player.getInstance().unloadChunk(chunk);
            }
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
