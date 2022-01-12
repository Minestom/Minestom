package net.minestom.server.utils;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ViewEngineTest {

    @Test
    public void empty() {
        ViewEngine viewEngine = new ViewEngine();
        var set = viewEngine.asSet();
        assertEquals(0, set.size());
        // forEach
        set.forEach(player -> fail("The engine should have no entity"));
        // Iterator
        var iterator = set.iterator();
        assertFalse(iterator.hasNext());
        // Array
        assertArrayEquals(new Entity[0], set.toArray());
    }

    @Test
    public void playerEmpty() {
        Player owner = createPlayer();
        ViewEngine viewEngine = new ViewEngine(owner);
        var set = viewEngine.asSet();
        assertEquals(0, set.size());
        // forEach
        set.forEach(player -> fail("The engine should have no entity"));
        // Iterator
        var iterator = set.iterator();
        assertFalse(iterator.hasNext());
        // Array
        assertArrayEquals(new Entity[0], set.toArray());
    }

    static Player createPlayer() {
        return new Player(UUID.randomUUID(), "test", null);
    }
}
