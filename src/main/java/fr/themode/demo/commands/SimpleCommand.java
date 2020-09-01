package fr.themode.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.*;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biomes.Biome;

import java.util.Arrays;
import java.util.List;

public class SimpleCommand implements CommandProcessor {
    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"alias"};
    }

    @Override
    public boolean process(CommandSender sender, String command, String[] args) {
        if (!sender.isPlayer())
            return false;

        final int word = 2;
        ChunkGenerator chunkGeneratorDemo = new ChunkGenerator() {
            @Override
            public void generateChunkData(ChunkBatch batch, int chunkX, int chunkZ) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        batch.setBlockStateId(x, 1, z, (short) word);
                    }
                }
            }

            @Override
            public void fillBiomes(Biome[] biomes, int chunkX, int chunkZ) {
                Arrays.fill(biomes, Biome.PLAINS);
            }

            @Override
            public List<ChunkPopulator> getPopulators() {
                return null;
            }
        };

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer(DimensionType.OVERWORLD);
        instanceContainer.enableAutoChunkLoad(true);
        instanceContainer.setChunkGenerator(chunkGeneratorDemo);

        SharedInstance instance = instanceManager.createSharedInstance(instanceContainer);
        sender.asPlayer().setInstance(instance);
        System.out.println(MinecraftServer.getInstanceManager().getInstances().size());

        System.gc();

        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }

    @Override
    public boolean enableWritingTracking() {
        return true;
    }

    @Override
    public String[] onWrite(String text) {
        return new String[]{"Complete1", "Complete2"};
    }
}
