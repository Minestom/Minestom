package net.minestom.server.adventure.provider;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.minestom.server.adventure.MinestomAdventure;

final class MinestomFlattenerProvider {
    static final ComponentFlattener INSTANCE;
    static {
        final ComponentFlattener.Builder builder = ComponentFlattener.basic().toBuilder();

        // handle server-side translations if needed
        builder.complexMapper(TranslatableComponent.class, ((component, consumer) -> {
            if (MinestomAdventure.AUTOMATIC_COMPONENT_TRANSLATION) {
                final Component translated = MinestomAdventure.COMPONENT_TRANSLATOR.apply(component, MinestomAdventure.getDefaultLocale());

                // In case the translated component is also a translatable component, we just leave the key to avoid infinite recursion
                if (translated instanceof TranslatableComponent translatable) {
                    consumer.accept(Component.text(translatable.key()));
                } else {
                    consumer.accept(translated);
                }
            }
        }));

        INSTANCE = builder.build();
    }
}
