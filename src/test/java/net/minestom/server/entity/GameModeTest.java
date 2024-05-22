package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameModeTest {

    @Test
    void toId() {
        assertEquals(0, GameMode.SURVIVAL.id());
        assertEquals(1, GameMode.CREATIVE.id());
        assertEquals(2, GameMode.ADVENTURE.id());
        assertEquals(3, GameMode.SPECTATOR.id());
    }

    @Test
    void fromId() {
        assertEquals(GameMode.SURVIVAL, GameMode.fromId(0));
        assertEquals(GameMode.CREATIVE, GameMode.fromId(1));
        assertEquals(GameMode.ADVENTURE, GameMode.fromId(2));
        assertEquals(GameMode.SPECTATOR, GameMode.fromId(3));
    }
}
