package demo;

import de.articdive.jnoise.JNoise;
import de.articdive.jnoise.noise.opensimplex.FastSimplexBuilder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.math.IntRange;
import net.minestom.server.world.biomes.Biome;
import net.minestom.server.world.generator.BiomeGenerator;
import net.minestom.server.world.generator.BlockPool;
import net.minestom.server.world.generator.WorldGenerator;
import net.minestom.server.world.generator.stages.pregeneration.BiomeLayout2DStage;
import net.minestom.server.world.generator.stages.generation.TerrainStage;
import net.minestom.server.world.generator.stages.pregeneration.HeightMapStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class MainDemo {
    private final static Logger LOGGER = LoggerFactory.getLogger(MainDemo.class);

    public static void main(String[] args) {
        // Initialization
        MinecraftServer minecraftServer = MinecraftServer.init();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        // Create the instance
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        // Set the ChunkGenerator
        final Random random = new Random(1);
        final JNoise plainsHeight = new FastSimplexBuilder().setSeed(random.nextLong()).setFrequency(.5).build();
        final JNoise hotDeepBlock = new FastSimplexBuilder().setSeed(random.nextLong()).setFrequency(.2).build();
        final JNoise hotDeepHeight = new FastSimplexBuilder().setSeed(random.nextLong()).setFrequency(.1).build();
        final JNoise tempNoise = new FastSimplexBuilder().setSeed(random.nextLong()).setFrequency(.45).build();
        instanceContainer.setWorldGenerator(new WorldGenerator(
                Set.of(
                        new BiomeGenerator(
                                Biome.PLAINS,
                                null,
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
                                null,
                                new BlockPool(hotDeepBlock::getNoise) {{
                                    addBlock(Block.LAVA, .3f, new IntRange(-5, 0));
                                    addBlock(Block.NETHERRACK, .5f, new IntRange(Integer.MIN_VALUE, 0));
                                    addBlock(Block.NETHER_QUARTZ_ORE, .05f, new IntRange(-20, -9));
                                }},
                                hotDeepHeight::getNoise
                        )
                ),
                List.of(
                        new BiomeLayout2DStage(tempNoise::getNoise, null, 1),
                        new HeightMapStage()
                ),
                List.of(
                        new TerrainStage()
                )
        ));

        int r = 10;
        final int total = MathUtils.square(r * 2);
        final CountDownLatch latch = new CountDownLatch(total);
        for (int x = -r; x < r; x++) {
            for (int z = -r; z < r; z++) {
                instanceContainer.loadChunk(x,z).whenComplete((c,t) -> {
                    latch.countDown();
                    LOGGER.info("Generating spawn region {}%", (total- latch.getCount())/(double)total*100);
                });
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Add an event callback to specify the spawning instance (and the spawn position)
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        // Start the server on port 25565
        minecraftServer.start("0.0.0.0", 25565);
    }
}