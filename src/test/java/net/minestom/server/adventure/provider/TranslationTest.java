package net.minestom.server.adventure.provider;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.MinestomAdventure;
import org.junit.jupiter.api.Test;

public class TranslationTest {

    @Test
    public void testUnregisteredTranslation() {
        MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = true;
        try {
            MinestomFlattenerProvider.INSTANCE.flatten(Component.translatable("key.unregistered"), text -> {
            });
        } finally {
            MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION = false;
        }
    }

}
