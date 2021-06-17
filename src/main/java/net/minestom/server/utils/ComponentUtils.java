package net.minestom.server.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minestom.server.adventure.MinestomAdventure;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * {@link Component} utilities.
 */
public final class ComponentUtils {
    /**
     * Checks if a component can be translated server-side. This is done by running the
     * component through the translator and seeing if the translated component is equal
     * to the non translated component.
     *
     * @param component the component
     * @return {@code true} if the component can be translated server-side, {@code false} otherwise
     */
    public static boolean isTranslatable(@NotNull Component component) {
        return !component.equals(GlobalTranslator.render(component, MinestomAdventure.getDefaultLocale()));
    }

    /**
     * Checks if any of a series of components are translatable server-side.
     *
     * @param components the components
     * @return {@code true} if any of the components can be translated server-side, {@code false} otherwise
     */
    public static boolean areAnyTranslatable(@NotNull Collection<Component> components) {
        for (Component component : components) {
            if (isTranslatable(component)) {
                return true;
            }
        }

        return false;
    }
}
