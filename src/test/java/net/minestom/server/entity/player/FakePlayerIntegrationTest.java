package net.minestom.server.entity.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.fakeplayer.FakePlayerOption;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class FakePlayerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(FakePlayerIntegrationTest.class);

    @Test
    public void removalTest(Env env) {
        var instance = env.createFlatInstance();

        env.listen(AsyncPlayerConfigurationEvent.class).followup(event -> {
            // Spawning instance is required to be set in configuration event
            event.setSpawningInstance(instance);
            log.info("Spawning instance set for {}", event.getPlayer().getUsername());
        });

        FakePlayer player = initializeFakePlayer(env, UUID.randomUUID(), "FakePlayer");

        assertTrue(player.isOnline(), "The FakePlayer should currently be considered as online");
        assertEquals(1, MinecraftServer.getConnectionManager().getOnlinePlayerCount(), "Should only be 1 player online (the FakePlayer)");

        // One extra tick just in case something is missed
        env.tick();

        // Disconnects the FakePlayer
        player.remove();

        // FakePlayer should be removed after two ticks
        env.tick();
        env.tick();

        assertFalse(player.isOnline(), "The FakePlayer should currently be considered as offline");
        assertEquals(0, MinecraftServer.getConnectionManager().getOnlinePlayerCount(), "There should be no players online");
    }

    private FakePlayer initializeFakePlayer(Env env, UUID uuid, String username) {
        return initializeFakePlayer(env, uuid, username, new FakePlayerOption().setInTabList(true).setRegistered(true));
    }

    private FakePlayer initializeFakePlayer(Env env, UUID uuid, String username, FakePlayerOption options) {
        log.info("Initializing new FakePlayer: {}", username);
        AtomicReference<FakePlayer> reference = new AtomicReference<>();
        FakePlayer.initPlayer(uuid, username, options, fp -> {
            log.info("Spawn callback called for {}", fp.getUsername());
            reference.set(fp);
        });
        env.tickWhile(() -> Objects.isNull(reference.get()), Duration.ofSeconds(15));
        FakePlayer fp = reference.get();
        assertNotNull(fp, "No FakePlayer spawned");
        return fp;
    }

}
