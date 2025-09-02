package net.minestom.server.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GameEventTest {

    @Test
    public void load() {
        assertNotNull(GameEventImpl.REGISTRY);
    }
}
