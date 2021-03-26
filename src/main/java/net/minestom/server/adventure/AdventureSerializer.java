package net.minestom.server.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * Manager class for handling Adventure serialization. By default AdventureSerializer will simply
 * serialize components to Strings using {@link GsonComponentSerializer}. However, AdventureSerializer
 * class can be used to change the way text is serialized. For example, a pre-JSON
 * implementation of Minestom could change AdventureSerializer to the plain component serializer.
 * <br><br>
 * This manager also performs translation on all messages and the {@code serialize}
 * method should be used when converting {@link Component}s into strings. This allows for
 * messages with {@link TranslatableComponent} to be automatically translated into the locale
 * of specific players, or other elements which implement {@link Localizable}. To add your
 * own translations, use {@link GlobalTranslator#addSource(Translator)} with a
 * {@link TranslationRegistry} or your own implementation of {@link Translator}.
 */
public class AdventureSerializer {
    /**
     * If components should be automatically translated in outgoing packets.
     */
    public static final boolean AUTOMATIC_COMPONENT_TRANSLATION = false;

    protected static final Localizable NULL_LOCALIZABLE = () -> null;

    private static Function<Component, String> serializer = component -> GsonComponentSerializer.gson().serialize(component);
    private static Locale defaultLocale = Locale.US;

    private AdventureSerializer() {}

    /**
     * Gets the root serializer that is used to convert components into strings.
     *
     * @return the serializer
     */
    public static @NotNull Function<Component, String> getSerializer() {
        return AdventureSerializer.serializer;
    }

    /**
     * Sets the root serializer that is used to convert components into strings.
     *
     * @param serializer the serializer
     */
    public static void setSerializer(@NotNull Function<Component, String> serializer) {
        AdventureSerializer.serializer = serializer;
    }

    /**
     * Gets the default locale used to translate {@link TranslatableComponent} if, when
     * {@link #translate(Component, Localizable)} is called with a localizable that
     * does not have a locale.
     *
     * @return the default locale
     */
    public static @NotNull Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Sets the default locale used to translate {@link TranslatableComponent} if, when
     * {@link #translate(Component, Localizable)} is called with a localizable that
     * does not have a locale.
     *
     * @param defaultLocale the new default locale
     */
    public static void setDefaultLocale(@NotNull Locale defaultLocale) {
        AdventureSerializer.defaultLocale = defaultLocale;
    }

    /**
     * Gets the global translator object used by AdventureSerializer manager. This is just shorthand for
     * {@link GlobalTranslator#get()}.
     *
     * @return the global translator
     */
    public static @NotNull GlobalTranslator getTranslator() {
        return GlobalTranslator.get();
    }

    /**
     * Prepares a component for serialization. This runs the component through the
     * translator for the localizable's locale.
     *
     * @param component the component
     * @param localizable the localizable
     *
     * @return the prepared component
     */
    public static @NotNull Component translate(@NotNull Component component, @NotNull Localizable localizable) {
        return GlobalTranslator.renderer().render(component, Objects.requireNonNullElse(localizable.getLocale(), AdventureSerializer.getDefaultLocale()));
    }

    /**
     * Prepares a component for serialization. This runs the component through the
     * translator for the locale.
     *
     * @param component the component
     * @param locale the locale
     *
     * @return the prepared component
     */
    public static @NotNull Component translate(@NotNull Component component, @NotNull Locale locale) {
        return GlobalTranslator.renderer().render(component, locale);
    }

    /**
     * Serializes a component into a string using {@link #getSerializer()}.
     *
     * @param component the component
     *
     * @return the serialized string
     */
    public static @NotNull String serialize(@NotNull Component component) {
        return AdventureSerializer.serializer.apply(component);
    }

    /**
     * Prepares and then serializes a component.
     *
     * @param component the component
     * @param localizable the localisable
     *
     * @return the string
     */
    public static String translateAndSerialize(@NotNull Component component, @NotNull Localizable localizable) {
        return AdventureSerializer.translateAndSerialize(component, Objects.requireNonNullElse(localizable.getLocale(), AdventureSerializer.getDefaultLocale()));
    }

    /**
     * Prepares and then serializes a component.
     *
     * @param component the component
     * @param locale the locale
     *
     * @return the string
     */
    public static String translateAndSerialize(@NotNull Component component, @NotNull Locale locale) {
        return AdventureSerializer.serialize(AdventureSerializer.translate(component, locale));
    }

    /**
     * Checks if a component can be translated server-side. This is done by running the
     * component through the translator and seeing if the translated component is equal
     * to the non translated component.
     * @param component the component
     * @return {@code true} if the component can be translated server-side,
     * {@code false} otherwise
     */
    public static boolean isTranslatable(@NotNull Component component) {
        return !component.equals(AdventureSerializer.translate(component, AdventureSerializer.getDefaultLocale()));
    }

    /**
     * Checks if any of a series of components are translatable server-side.
     * @param components the components
     * @return {@code true} if any of the components can be translated server-side,
     * {@code false} otherwise
     */
    public static boolean areAnyTranslatable(@NotNull Collection<Component> components) {
        for (Component component : components) {
            if (AdventureSerializer.isTranslatable(component)) {
                return true;
            }
        }

        return false;
    }
}
