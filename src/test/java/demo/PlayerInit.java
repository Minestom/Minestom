package demo;

import demo.generator.ChunkGeneratorDemo;
import demo.generator.NoiseTestGenerator;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.ItemTag;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.CompassMeta;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;

import java.time.Duration;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerInit {

    private static final Inventory inventory;

    private static final EventNode<Event> DEMO_NODE = EventNode.all("demo")
            .addListener(EntityAttackEvent.class, event -> {
                final Entity source = event.getEntity();
                final Entity entity = event.getTarget();

                entity.takeKnockback(0.4f, Math.sin(source.getPosition().getYaw() * 0.017453292), -Math.cos(source.getPosition().getYaw() * 0.017453292));

                if (entity instanceof Player) {
                    Player target = (Player) entity;
                    target.damage(DamageType.fromEntity(source), 5);
                }

                if (source instanceof Player) {
                    ((Player) source).sendMessage("You attacked something!");
                }
            })
            .addListener(PlayerDeathEvent.class, event -> event.setChatMessage(Component.text("custom death message")))
            .addListener(PickupItemEvent.class, event -> {
                final Entity entity = event.getLivingEntity();
                if (entity instanceof Player) {
                    // Cancel event if player does not have enough inventory space
                    final ItemStack itemStack = event.getItemEntity().getItemStack();
                    event.setCancelled(!((Player) entity).getInventory().addItemStack(itemStack));
                }
            })
            .addListener(ItemDropEvent.class, event -> {
                final Player player = event.getPlayer();
                ItemStack droppedItem = event.getItemStack();

                Position position = player.getPosition().clone().add(0, 1.5f, 0);
                ItemEntity itemEntity = new ItemEntity(droppedItem, position);
                itemEntity.setPickupDelay(Duration.of(500, TimeUnit.MILLISECOND));
                itemEntity.setInstance(player.getInstance());
                Vector velocity = player.getPosition().clone().getDirection().multiply(6);
                itemEntity.setVelocity(velocity);
            })
            .addListener(PlayerDisconnectEvent.class, event -> System.out.println("DISCONNECTION " + event.getPlayer().getUsername()))
            .addListener(PlayerLoginEvent.class, event -> {
                final Player player = event.getPlayer();

                var instances = MinecraftServer.getInstanceManager().getInstances();
                Instance instance = instances.stream().skip(new Random().nextInt(instances.size())).findFirst().orElse(null);
                event.setSpawningInstance(instance);
                int x = Math.abs(ThreadLocalRandom.current().nextInt()) % 500 - 250;
                int z = Math.abs(ThreadLocalRandom.current().nextInt()) % 500 - 250;
                player.setRespawnPoint(new Position(0, 42f, 0));
            })
            .addListener(PlayerSpawnEvent.class, event -> {
                final Player player = event.getPlayer();
                player.setGameMode(GameMode.CREATIVE);
                player.setPermissionLevel(4);
                ItemStack itemStack = ItemStack.builder(Material.STONE)
                        .amount(64)
                        .meta(itemMetaBuilder ->
                                itemMetaBuilder.canPlaceOn(Set.of(Block.STONE))
                                        .canDestroy(Set.of(Block.DIAMOND_ORE)))
                        .build();
                player.getInventory().addItemStack(itemStack);
            });

    static {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        //StorageLocation storageLocation = MinecraftServer.getStorageManager().getLocation("instance_data", new StorageOptions().setCompression(true));
        ChunkGeneratorDemo chunkGeneratorDemo = new ChunkGeneratorDemo();
        NoiseTestGenerator noiseTestGenerator = new NoiseTestGenerator();

        InstanceContainer instanceContainer = instanceManager.createInstanceContainer(DimensionType.OVERWORLD);
        instanceContainer.enableAutoChunkLoad(true);
        instanceContainer.setChunkGenerator(chunkGeneratorDemo);

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
        var eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addChild(DEMO_NODE);
        var children = eventHandler.findChildren("demo", Event.class);

        eventHandler.replaceChildren("demo", PlayerEvent.class, EventNode.type("random", EventFilter.PLAYER));

        MinecraftServer.getUpdateManager().addTickMonitor(tickMonitor ->
                LAST_TICK.set(tickMonitor));

        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
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
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
    }


}
