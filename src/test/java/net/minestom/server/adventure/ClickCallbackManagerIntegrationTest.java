package net.minestom.server.adventure;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.common.ClientCustomClickActionPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class ClickCallbackManagerIntegrationTest {
    private static final Key KEY = Key.key("minestom", "click_callback");
    private static final Duration FOREVER = Duration.ofNanos(Long.MAX_VALUE);

    private final ClickCallbackManager manager = new ClickCallbackManager();
    private final AtomicInteger runs = new AtomicInteger();
    private Player player;

    @BeforeEach
    public void setup(Env env) {
        player = env.createPlayer(env.createFlatInstance(), new Pos(0, 42, 0));
    }

    @Test
    public void unlimitedUsesWithLifetime() {
        var event = create(ClickCallback.UNLIMITED_USES, Duration.ofMinutes(1));
        manager.tick(System.nanoTime());
        click(event, 5);
        assertEquals(5, runs.get());

        manager.tick(System.nanoTime() + Duration.ofMinutes(2).toNanos());
        click(event, 1);
        assertEquals(5, runs.get(), "the callback must be removed once its lifetime has passed");
    }

    @Test
    public void unlimitedUsesWithoutExpiry() {
        var event = create(ClickCallback.UNLIMITED_USES, FOREVER);
        manager.tick(Long.MAX_VALUE);
        click(event, 5);
        assertEquals(5, runs.get());
    }

    @Test
    public void limitedUsesWithLifetime() {
        var event = create(3, Duration.ofMinutes(1));
        manager.tick(System.nanoTime());
        click(event, 5);
        assertEquals(3, runs.get());
    }

    @Test
    public void limitedUsesWithoutExpiry() {
        var event = create(3, FOREVER);
        manager.tick(System.nanoTime());
        click(event, 5);
        assertEquals(3, runs.get());
    }

    @Test
    public void lifetimeThatIsNotPositive() {
        click(create(ClickCallback.UNLIMITED_USES, Duration.ZERO), 1);
        click(create(3, Duration.ofSeconds(-1)), 1);
        click(create(ClickCallback.UNLIMITED_USES, Duration.ofSeconds(Long.MIN_VALUE)), 1);
        assertEquals(0, runs.get());
    }

    @Test
    public void zeroUses() {
        click(create(0, Duration.ofMinutes(1)), 1);
        assertEquals(0, runs.get());
    }

    @Test
    public void unrelatedPayload() {
        create(ClickCallback.UNLIMITED_USES, Duration.ofMinutes(1));
        manager.consumeCustomClick(player, new ClientCustomClickActionPacket(KEY, StringBinaryTag.stringBinaryTag("test")));
        manager.consumeCustomClick(player, new ClientCustomClickActionPacket(KEY, IntArrayBinaryTag.intArrayBinaryTag(1, 2)));
        manager.consumeCustomClick(player, new ClientCustomClickActionPacket(KEY, IntArrayBinaryTag.intArrayBinaryTag(1, 2, 3, 4)));
        manager.consumeCustomClick(player, new ClientCustomClickActionPacket(Key.key("minestom", "other"), IntArrayBinaryTag.intArrayBinaryTag(1, 2, 3, 4)));
        assertEquals(0, runs.get());
    }

    private ClickEvent<ClickEvent.Payload.Custom> create(int uses, Duration lifetime) {
        return manager.createClickEvent(
                _ -> runs.incrementAndGet(),
                ClickCallback.Options.builder().uses(uses).lifetime(lifetime).build());
    }

    private void click(ClickEvent<ClickEvent.Payload.Custom> event, int times) {
        final ClickEvent.Payload.Custom payload = (ClickEvent.Payload.Custom) event.payload();
        final var packet = new ClientCustomClickActionPacket(payload.key(), MinestomAdventure.unwrapNbt(payload.nbt()));
        for (int i = 0; i < times; i++) manager.consumeCustomClick(player, packet);
    }
}
