package net.minestom.server.network.player;

import com.google.gson.JsonParser;
import net.minestom.server.codec.Transcoder;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameProfileTest {

    @Test
    void decodeMojangSessionResponse() {
        final var element = JsonParser.parseString("""
                {
                  "id": "ab70ecb423464c14a52d7a091507c24e",
                  "name": "Notch",
                  "properties": [
                    { "name": "textures", "value": "ewogICJ0aW1lc3RhbXAi", "signature": "abc123" }
                  ]
                }
                """);
        final GameProfile profile = GameProfile.CODEC.decode(Transcoder.JSON, element).orElseThrow();
        assertEquals(UUID.fromString("ab70ecb4-2346-4c14-a52d-7a091507c24e"), profile.uuid());
        assertEquals("Notch", profile.name());
        assertEquals(1, profile.properties().size());
        final GameProfile.Property property = profile.properties().getFirst();
        assertEquals("textures", property.name());
        assertEquals("ewogICJ0aW1lc3RhbXAi", property.value());
        assertEquals("abc123", property.signature());
    }

    @Test
    void decodeUnsignedProperty() {
        final var element = JsonParser.parseString("""
                {
                  "id": "ab70ecb423464c14a52d7a091507c24e",
                  "name": "Notch",
                  "properties": [
                    { "name": "textures", "value": "ewogICJ0aW1lc3RhbXAi" }
                  ]
                }
                """);
        final GameProfile profile = GameProfile.CODEC.decode(Transcoder.JSON, element).orElseThrow();
        assertNull(profile.properties().getFirst().signature());
    }
}
