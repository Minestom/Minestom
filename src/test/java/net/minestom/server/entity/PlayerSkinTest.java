package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PlayerSkinTest {

    @Test
    public void validName() {
        var skin = PlayerSkin.fromUsername("jeb_");
        assertNotNull(skin);
    }

    @Test
    public void invalidName() {
        var skin = PlayerSkin.fromUsername("jfdsa84vvcxadubasdfcvn");
        assertNull(skin);
    }
}
