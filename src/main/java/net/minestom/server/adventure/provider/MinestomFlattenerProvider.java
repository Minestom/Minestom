package net.minestom.server.adventure.provider;

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
                for (final Translator source : GlobalTranslator.translator().sources()) {
                    if (source instanceof TranslationRegistry registry && registry.contains(component.key())) {
                        consumer.accept(GlobalTranslator.render(component, MinestomAdventure.getDefaultLocale()));
                    }
                }
            }
        }));

        INSTANCE = builder.build();
    }
}
