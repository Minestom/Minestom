package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameModeTest {

    @Test
    public void toId() {
        assertEquals(GameMode.SURVIVAL.ordinal(), 0);
        assertEquals(GameMode.CREATIVE.ordinal(), 1);
        assertEquals(GameMode.ADVENTURE.ordinal(), 2);
        assertEquals(GameMode.SPECTATOR.ordinal(), 3);
    }
}
