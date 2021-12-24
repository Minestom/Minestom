package demo;

import de.articdive.jnoise.JNoise;
import de.articdive.jnoise.noise.opensimplex.FastSimplexBuilder;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.BundleMeta;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.math.IntRange;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.BiomeGenerator;
import net.minestom.server.world.generator.BlockPool;
import net.minestom.server.world.generator.InMemoryGenerationContext;
import net.minestom.server.world.generator.WorldGenerator;
import net.minestom.server.world.generator.stages.generation.BedrockStage;
import net.minestom.server.world.generator.stages.generation.BiomeFillStage;
import net.minestom.server.world.generator.stages.generation.TerrainStage;
import net.minestom.server.world.generator.stages.pregeneration.HeightMapStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerInit {
    private final static Logger LOGGER = LoggerFactory.getLogger(MainDemo.class);

    private static final Inventory inventory;

    private static final EventNode<Event> DEMO_NODE = EventNode.all("demo")
            .addListener(EntityAttackEvent.class, event -> {
                final Entity source = event.getEntity();
                final Entity entity = event.getTarget();

                entity.takeKnockback(0.4f, Math.sin(source.getPosition().yaw() * 0.017453292), -Math.cos(source.getPosition().yaw() * 0.017453292));

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

                Pos playerPos = player.getPosition();
                ItemEntity itemEntity = new ItemEntity(droppedItem);
                itemEntity.setPickupDelay(Duration.of(500, TimeUnit.MILLISECOND));
                itemEntity.setInstance(player.getInstance(), playerPos.withY(y -> y + 1.5));
                Vec velocity = playerPos.direction().mul(6);
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
                player.setRespawnPoint(new Pos(0, 42f, 0));
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

                ItemStack bundle = ItemStack.builder(Material.BUNDLE)
                        .meta(BundleMeta.class, bundleMetaBuilder -> {
                            bundleMetaBuilder.addItem(ItemStack.of(Material.DIAMOND, 5));
                            bundleMetaBuilder.addItem(ItemStack.of(Material.RABBIT_FOOT, 5));
                        })
                        .build();
                player.getInventory().addItemStack(bundle);
            });

    static {
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        InstanceContainer instanceContainer = instanceManager.createInstanceContainer(DimensionType.OVERWORLD);

        final Random random = new Random(1);
        final JNoise plainsHeight = new FastSimplexBuilder().setSeed(random.nextLong()).setFrequency(.5).build();
        final JNoise hotDeepBlock = new FastSimplexBuilder().setSeed(random.nextLong()).setFrequency(.2).build();
        final JNoise hotDeepHeight = new FastSimplexBuilder().setSeed(random.nextLong()).setFrequency(.1).build();
        final JNoise tempNoise = new FastSimplexBuilder().setSeed(random.nextLong()).setFrequency(.45).build();
        instanceContainer.setSectionSupplier(new WorldGenerator(
                Set.of(
                        new BiomeGenerator(
                                Biome.PLAINS,
                                Collections.emptySet(),
                                new BlockPool((x, y, z) -> random.nextFloat()) {{
                                    addBlock(Block.TALL_GRASS, .5f, new IntRange(0));
                                    addBlock(Block.AIR, .5f, new IntRange(0));
                                    addBlock(Block.GRASS, 1, new IntRange(-1));
                                    addBlock(Block.DIRT, 1, new IntRange(-3, -2));
                                    addBlock(Block.STONE, 1, new IntRange(Integer.MIN_VALUE, -4));
                                }},
                                plainsHeight::getNoise
                        ),
                        new BiomeGenerator(
                                Biome.builder()
                                        .name(NamespaceID.from("custom:hot_deep"))
                                        .temperature(2)
                                        .depth(.05f)
                                        .build(),
                                Collections.emptySet(),
                                new BlockPool(hotDeepBlock::getNoise) {{
                                    addBlock(Block.LAVA, .3f, new IntRange(-5, 0));
                                    addBlock(Block.NETHERRACK, .5f, new IntRange(Integer.MIN_VALUE, 0));
                                    addBlock(Block.NETHER_QUARTZ_ORE, .05f, new IntRange(-20, -9));
                                }},
                                hotDeepHeight::getNoise
                        )
                ),
                List.of(
//                        new BiomeLayout2DStage(tempNoise::getNoise, null, 1),
                        new HeightMapStage()
                ),
                List.of(
                        new TerrainStage(),
                        new BedrockStage(),
                        new BiomeFillStage()
                ),
                InMemoryGenerationContext.factory()
        ));

//        instanceContainer.setSectionSupplier(new WorldGenerator(
//                List.of((context, blockCache, biomePalette, sectionX, sectionY, sectionZ) -> {
//                    int absHeight = 35;
//                    if (Math.ceil(absHeight / 16d) >= sectionY) {
//                        int h = Math.floor(absHeight / 16d) > sectionY * 16 ? 16 : absHeight - sectionY * 16;
//                            for (int x = 0; x < 16; x++) {
//                                for (int y = 0; y < h; y++) {
//                                    for (int z = 0; z < 16; z++) {
//                                        blockCache.setBlock(x, y, z, Block.STONE);
//                                    }
//                                }
//                            }
//                        }
//                })
//        ));

        int r = 9;
        final int total = 100;
        final CountDownLatch latch = new CountDownLatch(total);
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                instanceContainer.loadChunk(x, z).thenRun(() -> {
                    latch.countDown();
                    LOGGER.info("Generating spawn region {}%", (total - latch.getCount()) / (double) total * 100);
                });
            }
        }

        inventory = new Inventory(InventoryType.CHEST_1_ROW, Component.text("Test inventory"));
        inventory.setItemStack(3, ItemStack.of(Material.DIAMOND, 34));
    }

    private static final AtomicReference<TickMonitor> LAST_TICK = new AtomicReference<>();

    public static void init() {
        var eventHandler = MinecraftServer.getGlobalEventHandler();
        eventHandler.addChild(DEMO_NODE);

        MinecraftServer.getUpdateManager().addTickMonitor(LAST_TICK::set);

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
