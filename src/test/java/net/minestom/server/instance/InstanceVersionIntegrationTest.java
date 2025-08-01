package net.minestom.server.instance;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static net.minestom.server.coordinate.Area.single;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class InstanceVersionIntegrationTest {
    @Test
    public void global(Env env) {
        var instance = env.createFlatInstance();
        var version = instance.version();
        var version2 = instance.version();
        assertCompatible(version, version2);
    }

    @Test
    public void globalMutate(Env env) {
        var instance = env.createFlatInstance();
        var version = instance.version();
        instance.setBlock(BlockVec.ZERO, Block.STONE);
        var version2 = instance.version();
        assertNotCompatible(version, version2);
    }

    @Test
    public void singleZero(Env env) {
        var vec = BlockVec.ZERO;
        var instance = env.createFlatInstance();
        var version = instance.version(single(vec));
        var version2 = instance.version(single(vec));
        assertCompatible(version, version2);
    }

    @Test
    public void singleZeroMutate(Env env) {
        var vec = BlockVec.ZERO;
        var instance = env.createFlatInstance();
        var version = instance.version(single(vec));
        instance.setBlock(vec, Block.STONE);
        var version2 = instance.version(single(vec));
        assertNotCompatible(version, version2);
    }

    @Test
    public void singleZeroMutateOther(Env env) {
        var vec = BlockVec.ZERO;
        var instance = env.createFlatInstance();
        var version = instance.version(single(vec));
        instance.setBlock(vec.add(31, 0, 0), Block.STONE);
        var version2 = instance.version(single(vec));
        assertCompatible(version, version2);
    }

    private void assertCompatible(BlockVersion v1, BlockVersion v2) {
        assertTrue(v1.compatible(v2), "Versions should be compatible");
    }

    private void assertNotCompatible(BlockVersion v1, BlockVersion v2) {
        assertFalse(v1.compatible(v2), "Versions should not be compatible");
    }
}
