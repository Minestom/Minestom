package net.minestom.server.instance;

import net.kyori.adventure.key.Key;
import net.minestom.server.world.clock.WorldClock;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class InstanceTimeIntegrationTest {

    @Test
    void overworldTicking(Env env) {
        var instance = env.createEmptyInstance();
        var clock = instance.defaultClock();
        assertNotNull(clock);

        assertEquals(0, clock.time());

        for (int i = 0; i < 100; i++) {
            env.tick();
            assertEquals(i + 1, clock.time());
        }
    }

    @Test
    void pausing(Env env) {
        var instance = env.createEmptyInstance();
        var clock = instance.defaultClock();
        assertNotNull(clock);

        assertEquals(0, clock.time());
        for (int i = 0; i < 5; i++) env.tick();
        assertEquals(5, clock.time());

        clock.pause();
        assertTrue(clock.paused());

        for (int i = 0; i < 5; i++) env.tick();
        assertEquals(5, clock.time());
    }

    @Test
    void partialTickRate(Env env) {
        var instance = env.createEmptyInstance();
        var clock = instance.defaultClock();
        assertNotNull(clock);

        clock.rate(0.2f);
        for (int i = 0; i < 10; i++) env.tick();
        assertEquals(2, clock.time());
    }

    @Test
    void multipleClocks(Env env) {
        var myOtherClock = env.process().worldClock().register(Key.key("minestom:clock"), WorldClock.create());

        var instance = env.createEmptyInstance();
        var defaultClock = Objects.requireNonNull(instance.defaultClock());
        var otherClock = instance.clock(myOtherClock);

        for (int i = 0; i < 5; i++) env.tick();
        defaultClock.pause();
        for (int i = 0; i < 5; i++) env.tick();

        assertEquals(5, defaultClock.time());
        assertEquals(10, otherClock.time());
    }
}
