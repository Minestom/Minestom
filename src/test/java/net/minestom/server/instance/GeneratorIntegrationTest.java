package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.instance.block.Block;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class GeneratorIntegrationTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void loader(boolean data, Env env) {
        var manager = env.process().instance();
        var block = data ? Block.STONE.withNbt(NBT.Compound(Map.of("key", NBT.String("value")))) : Block.STONE;
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fill(block));
        instance.loadChunk(0, 0).join();
        assertEquals(block, instance.getBlock(0, 0, 0));
        assertEquals(block, instance.getBlock(15, 0, 0));
        assertEquals(block, instance.getBlock(0, 15, 0));
        assertEquals(block, instance.getBlock(0, 0, 15));
    }
}
