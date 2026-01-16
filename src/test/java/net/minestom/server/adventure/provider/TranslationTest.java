package net.minestom.server.adventure.provider;

import net.kyori.adventure.text.Component;
import net.minestom.server.ServerFlag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TranslationTest {

    @Test
    public void testUnregisteredTranslation() {
        assumeTrue(ServerFlag.AUTOMATIC_COMPONENT_TRANSLATION, "Automatic component translation is required");
        MinestomFlattenerProvider.INSTANCE.flatten(Component.translatable("key.unregistered"), text -> {
        });

    }
    @Test
    public void testDisabledTranslation() {
        assumeFalse(ServerFlag.AUTOMATIC_COMPONENT_TRANSLATION, "Automatic component translation is disabled");
        MinestomFlattenerProvider.INSTANCE.flatten(Component.translatable("key.unregistered"), text -> {
        });
    }
}
