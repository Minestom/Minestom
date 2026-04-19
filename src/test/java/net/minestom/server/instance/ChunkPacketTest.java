package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.ChunkDataPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class ChunkPacketTest {

    @Test
    void testValidBlockEntity(Env env) {
        assertTrue(Block.CHEST.registry().isBlockEntity());
        var instance = env.createFlatInstance();
        var chunk = instance.loadChunk(0, 0).join();
        instance.setBlock(BlockVec.ZERO, Block.CHEST.withNbt(CompoundBinaryTag.builder().build()));
        var packet = assertDoesNotThrow(() -> (ChunkDataPacket) SendablePacket.extractServerPacket(chunk.getFullDataPacket(), ConnectionState.PLAY, MinecraftServer.getPacketWriter()));
        assertNotNull(packet);
        assertEquals(1, packet.chunkData().blockEntities().size());
    }

    @Test
    void testInvalidBlockEntity(Env env) {
        var instance = env.createFlatInstance();
        var chunk = instance.loadChunk(0, 0).join();
        instance.setBlock(BlockVec.ZERO, Block.BLACK_CONCRETE_POWDER.withNbt(CompoundBinaryTag.builder().build()));
        var packet = assertDoesNotThrow(() -> (ChunkDataPacket) SendablePacket.extractServerPacket(chunk.getFullDataPacket(), ConnectionState.PLAY, MinecraftServer.getPacketWriter()));
        assertNotNull(packet);
        assertEquals(0, packet.chunkData().blockEntities().size(), "Should of not counted as block entity");
    }
}
