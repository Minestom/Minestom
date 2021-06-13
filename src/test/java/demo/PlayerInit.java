package demo;

import demo.block.CampfireHandler;
import demo.generator.ChunkGeneratorDemo;
import demo.generator.NoiseTestGenerator;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.world.Chunk;
import net.minestom.server.world.World;
import net.minestom.server.world.WorldContainer;
import net.minestom.server.world.WorldManager;
import net.minestom.server.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.ItemTag;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.CompassMeta;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerInit {

    private static final Inventory inventory;

    static {
        WorldManager worldManager = MinecraftServer.getWorldManager();
        //StorageLocation storageLocation = MinecraftServer.getStorageManager().getLocation("instance_data", new StorageOptions().setCompression(true));
        ChunkGeneratorDemo chunkGeneratorDemo = new ChunkGeneratorDemo();
        NoiseTestGenerator noiseTestGenerator = new NoiseTestGenerator();

        WorldContainer worldContainer = worldManager.createWorldContainer(DimensionType.OVERWORLD);
        worldContainer.enableAutoChunkLoad(true);
        worldContainer.setChunkGenerator(chunkGeneratorDemo);

        worldContainer.setBlock(0, 45, 3, Block.CAMPFIRE
                .withHandler(new CampfireHandler())
                .withTag(CampfireHandler.ITEMS, List.of(ItemStack.of(Material.DIAMOND, 1))));

        inventory = new Inventory(InventoryType.CHEST_1_ROW, Component.text("Test inventory"));
        /*inventory.addInventoryCondition((p, slot, clickType, inventoryConditionResult) -> {
            p.sendMessage("click type inventory: " + clickType);
            System.out.println("slot inv: " + slot)0;
            inventoryConditionResult.setCancel(slot == 3);
        });*/
        inventory.setItemStack(3, ItemStack.of(Material.DIAMOND, 34));

        {
            CompassMeta compassMeta = new CompassMeta.Builder()
                    .lodestonePosition(new Position(0, 0, 0))
                    .build();

            ItemStack itemStack = ItemStack.builder(Material.COMPASS)
                    .meta(CompassMeta.class, builder -> {
                        builder.lodestonePosition(new Position(0, 0, 0));
                        builder.set(ItemTag.Integer("int"), 25);
                    })
                    .build();

            itemStack = itemStack.with(itemBuilder -> itemBuilder
                    .amount(10)
                    .meta(CompassMeta.class, builder -> {
                        builder.lodestonePosition(new Position(5, 0, 0));
                    })
                    .lore(Component.text("Lore")));
        }

    }

    private static AtomicReference<TickMonitor> LAST_TICK = new AtomicReference<>();

    public static void init() {
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();

        MinecraftServer.getUpdateManager().addTickMonitor(tickMonitor ->
                LAST_TICK.set(tickMonitor));

        MinecraftServer.getSchedulerManager().buildTask(() -> {

            Collection<Player> players = connectionManager.getOnlinePlayers();

            if (players.isEmpty())
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            TickMonitor tickMonitor = LAST_TICK.get();

            final Component header = Component.text("RAM USAGE: " + ramUsage + " MB")
                    .append(Component.newline())
                    .append(Component.text("TICK TIME: " + MathUtils.round(tickMonitor.getTickTime(), 2) + "ms"))
                    .append(Component.newline())
                    .append(Component.text("ACQ TIME: " + MathUtils.round(tickMonitor.getAcquisitionTime(), 2) + "ms"));
            final Component footer = benchmarkManager.getCpuMonitoringMessage();
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);

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
            if (entity instanceof Player) {
                Player target = (Player) entity;
                Vector velocity = source.getPosition().clone().getDirection().multiply(4);
                velocity.setY(3.5f);
                target.setVelocity(velocity);
                target.damage(DamageType.fromEntity(source), 5);
            } else {
                Vector velocity = source.getPosition().clone().getDirection().multiply(3);
                velocity.setY(3f);
                entity.setVelocity(velocity);
            }

            if (source instanceof Player) {
                ((Player) source).sendMessage("You attacked something!");
            }
        });

        globalEventHandler.addEventCallback(PlayerDeathEvent.class, event -> {
            event.setChatMessage(ColoredText.of("custom death message"));
        });

        globalEventHandler.addEventCallback(PlayerBlockInteractEvent.class, event -> {
            if (event.getHand() != Player.Hand.MAIN)
                return;
            final Player player = event.getPlayer();

            final Block block = event.getBlock();
            player.sendMessage("You clicked at the block " + block);
            player.sendMessage("CHUNK COUNT " + player.getWorld().getChunks().size());
        });

        globalEventHandler.addEventCallback(PickupItemEvent.class, event -> {
            final Entity entity = event.getLivingEntity();
            if (entity instanceof Player) {
                // Cancel event if player does not have enough inventory space
                final ItemStack itemStack = event.getItemEntity().getItemStack();
                event.setCancelled(!((Player) entity).getInventory().addItemStack(itemStack));
            }
        });

        globalEventHandler.addEventCallback(ItemDropEvent.class, event -> {
            final Player player = event.getPlayer();
            ItemStack droppedItem = event.getItemStack();

            Position position = player.getPosition().clone().add(0, 1.5f, 0);
            ItemEntity itemEntity = new ItemEntity(droppedItem, position);
            itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
            itemEntity.setWorld(player.getWorld());
            Vector velocity = player.getPosition().clone().getDirection().multiply(6);
            itemEntity.setVelocity(velocity);
        });

        globalEventHandler.addEventCallback(PlayerDisconnectEvent.class, event -> {
            final Player player = event.getPlayer();
            System.out.println("DISCONNECTION " + player.getUsername());
        });

        globalEventHandler.addEventCallback(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();

            var worlds = MinecraftServer.getWorldManager().getWorlds();
            World world = worlds.stream().skip(new Random().nextInt(worlds.size())).findFirst().orElse(null);
            event.setSpawningWorld(world);
            int x = Math.abs(ThreadLocalRandom.current().nextInt()) % 500 - 250;
            int z = Math.abs(ThreadLocalRandom.current().nextInt()) % 500 - 250;
            player.setRespawnPoint(new Position(0, 42f, 0));
        });

        globalEventHandler.addEventCallback(PlayerSpawnEvent.class, event -> {
            final Player player = event.getPlayer();
            player.setGameMode(GameMode.CREATIVE);

            player.setPermissionLevel(4);

            PlayerInventory inventory = player.getInventory();
            ItemStack itemStack = ItemStack.builder(Material.STONE)
                    .amount(64)
                    .meta(itemMetaBuilder ->
                            itemMetaBuilder.canPlaceOn(Set.of(Block.STONE))
                                    .canDestroy(Set.of(Block.DIAMOND_ORE)))
                    .build();

            //itemStack = itemStack.withStore(storeBuilder -> storeBuilder.set("key2", 25, Integer::sum));

            inventory.addItemStack(itemStack);

            {
                ItemStack item = ItemStack.builder(Material.DIAMOND_CHESTPLATE)
                        .displayName(Component.text("test"))
                        .lore(Component.text("lore"))
                        .build();

                //inventory.setChestplate(item);

                inventory.setChestplate(item.with(itemStackBuilder -> {
                    itemStackBuilder.lore(Collections.emptyList());
                }));
            }
        });

        globalEventHandler.addEventCallback(PlayerBlockBreakEvent.class, event -> {
            final Block block = event.getBlock();
            System.out.println("broke " + block);
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
            final World world = player.getWorld();

            Chunk chunk = world.getChunk(event.getChunkX(), event.getChunkZ());

            if (chunk == null)
                return;

            // Unload the chunk (save memory) if it has no remaining viewer
            if (chunk.getViewers().isEmpty()) {
                //player.getWorld().unloadChunk(chunk);
            }
        });
    }


}
