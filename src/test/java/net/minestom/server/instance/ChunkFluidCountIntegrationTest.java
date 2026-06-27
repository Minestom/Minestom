package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class ChunkFluidCountIntegrationTest {
    @Test
    public void sectionFluidCount(Env env) {
        var instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        var chunk = instance.getChunk(0, 0);

        final int water = 5;
        for (int y = 40; y < 40 + water; y++) instance.setBlock(0, y, 0, Block.WATER);
        assertEquals(water, sectionFluidCount(chunk, 40));

        // a removed fluid block leaves the rest counted (the client decrements from this value)
        instance.setBlock(0, 40, 0, Block.STONE);
        assertEquals(water - 1, sectionFluidCount(chunk, 40));
    }

    private static int sectionFluidCount(Chunk chunk, int blockY) {
        var packet = (ChunkDataPacket) SendablePacket.extractServerPacket(ConnectionState.PLAY, chunk.getFullDataPacket());
        final byte[] data = packet.chunkData().data();
        final var sectionType = ChunkData.Section.networkType(MinecraftServer.getBiomeRegistry().size());
        final NetworkBuffer buffer = NetworkBuffer.wrap(data, 0, data.length);
        ChunkData.Section section = null;
        for (int i = (blockY >> 4) - chunk.getMinSection(); i >= 0; i--) section = buffer.read(sectionType);
        return section.liquidCount();
    }
}
