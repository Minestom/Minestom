package net.minestom.server.instance;

import net.minestom.server.api.Env;
import net.minestom.server.api.EnvTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.block.Block;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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

    @Test
    public void exceptionCatch(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();

        var ref = new AtomicReference<Throwable>();
        env.process().exception().setExceptionHandler(ref::set);

        var exception = new RuntimeException();
        instance.setGenerator(unit -> {
            unit.modifier().fill(Block.STONE);
            throw exception;
        });
        instance.loadChunk(0, 0).join();

        assertSame(exception, ref.get());
    }

    @Test
    public void fillHeightNegative(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(-64, -60, Block.STONE));
        instance.loadChunk(0, 0).join();
        for (int y = -64; y < -60; y++) {
            assertEquals(Block.STONE, instance.getBlock(0, y, 0), "y=" + y);
        }
        for (int y = -60; y < 100; y++) {
            assertEquals(Block.AIR, instance.getBlock(0, y, 0), "y=" + y);
        }
    }

    @Test
    public void fillHeightSingleSectionFull(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 16, Block.GRASS_BLOCK));
        instance.loadChunk(0, 0).join();
        for (int y = 0; y < 16; y++) {
            assertEquals(Block.GRASS_BLOCK, instance.getBlock(0, y, 0), "y=" + y);
        }
    }

    @Test
    public void fillHeightSingleSection(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(4, 5, Block.GRASS_BLOCK));
        instance.loadChunk(0, 0).join();
        for (int y = 0; y < 5; y++) {
            assertEquals(y == 4 ? Block.GRASS_BLOCK : Block.AIR, instance.getBlock(0, y, 0), "y=" + y);
        }
    }

    @Test
    public void fillHeightOverride(Env env) {
        var manager = env.process().instance();
        var instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 39, Block.GRASS_BLOCK);
            unit.modifier().fillHeight(39, 40, Block.STONE);
        });
        instance.loadChunk(0, 0).join();
        for (int y = 0; y < 40; y++) {
            assertEquals(y == 39 ? Block.STONE : Block.GRASS_BLOCK, instance.getBlock(0, y, 0), "y=" + y);
        }
    }

//    @Test
//    public void temp(Env env) {
//        Instance instance = env.createFlatInstance();
//
//        instance.loadChunk(0, 0).join();
//        instance.loadChunk(0, -1).join();
//        instance.loadChunk(-1, 0).join();
//        instance.loadChunk(-1, -1).join();
//
//        env.process().eventHandler().addListener(PlayerLoginEvent.class, event -> {
//            event.setSpawningInstance(instance);
//            event.getPlayer().setRespawnPoint(new Pos(0, 0, 0));
//        });
//
//        env.process().start(new InetSocketAddress(25565));
//        while (true) {
//            try {
//                Thread.sleep(50);
//                env.tick();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
}
