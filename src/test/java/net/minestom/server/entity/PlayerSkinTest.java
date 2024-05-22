package net.minestom.server.entity;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlayerSkinTest {

    @Disabled
    @Test
    void validName() {
        var skin = PlayerSkin.fromUsername("jeb_");
        assertNotNull(skin);
    }

    @Disabled
    @Test
    void invalidName() {
        var skin = PlayerSkin.fromUsername("jfdsa84vvcxadubasdfcvn");
        assertNull(skin);
    }
}
