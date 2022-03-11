package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameModeTest {

    @Test
    public void toId() {
        assertEquals(GameMode.SURVIVAL.id(), 0);
        assertEquals(GameMode.CREATIVE.id(), 1);
        assertEquals(GameMode.ADVENTURE.id(), 2);
        assertEquals(GameMode.SPECTATOR.id(), 3);
    }

    @Test
    public void fromId() {
        assertEquals(GameMode.SURVIVAL, GameMode.fromId(0));
        assertEquals(GameMode.CREATIVE, GameMode.fromId(1));
        assertEquals(GameMode.ADVENTURE, GameMode.fromId(2));
        assertEquals(GameMode.SPECTATOR, GameMode.fromId(3));
    }
}
