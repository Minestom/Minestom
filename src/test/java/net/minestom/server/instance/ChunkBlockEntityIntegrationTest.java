package net.minestom.server.instance;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static net.minestom.server.coordinate.CoordConversion.chunkBlockIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@EnvTest
public class ChunkBlockEntityIntegrationTest {
    @Test
    public void chestEntity(Env env) {
        var instance = env.createFlatInstance();
        var chunk = instance.loadChunk(0, 0).join();
        var vec = new BlockVec(8, 44, 0);
        instance.setBlock(vec, Block.CHEST);

        ChunkDataPacket packet = (ChunkDataPacket) SendablePacket.extractServerPacket(ConnectionState.PLAY, chunk.getFullDataPacket());
        assert packet != null;
        Map<Integer, Block> blockEntities = packet.chunkData().blockEntities();
        assertEquals(1, blockEntities.size());
        int chunkBlockIndex = chunkBlockIndex(vec.blockX(), vec.blockY(), vec.blockZ());
        assertEquals(Block.CHEST, blockEntities.get(chunkBlockIndex),
                "Expected block entity at " + vec + " to be a chest, but got: " + blockEntities);


        chunk = instance.loadChunk(0, 1).join();
        vec = new BlockVec(8, 44, 16);
        instance.setBlock(vec, Block.CHEST);
        packet = (ChunkDataPacket) SendablePacket.extractServerPacket(ConnectionState.PLAY, chunk.getFullDataPacket());
        assert packet != null;
        blockEntities = packet.chunkData().blockEntities();
        assertEquals(1, blockEntities.size());
        chunkBlockIndex = chunkBlockIndex(vec.blockX(), vec.blockY(), vec.blockZ());
        assertEquals(Block.CHEST, blockEntities.get(chunkBlockIndex),
                "Expected block entity at " + vec + " to be a chest, but got: " + blockEntities);
    }

    @Test
    public void generator(Env env) {
        var instance = env.createEmptyInstance();
        instance.setGenerator(unit -> unit.modifier().fill(Block.CHEST));

        var chunk = instance.loadChunk(0, 0).join();
        ChunkDataPacket packet = (ChunkDataPacket) SendablePacket.extractServerPacket(ConnectionState.PLAY, chunk.getFullDataPacket());
        assert packet != null;
        Map<Integer, Block> blockEntities = packet.chunkData().blockEntities();
        assertNotEquals(0, blockEntities.size());
    }
}
