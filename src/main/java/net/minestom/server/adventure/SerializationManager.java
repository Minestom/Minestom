package net.minestom.server.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;

/**
 * Manager class for handling Adventure serialization. By default this will simply
 * serialize components to Strings using {@link GsonComponentSerializer}. However, this
 * class can be used to change the way text is serialized. For example, a pre-JSON
 * implementation of Minestom could change this to the plain component serializer.
 * <br><br>
 * This manager also provides the ability to wrap the serializer in a renderer that
 * performs operations on each component before the final serialization. This can be
 * done using {@link #addRenderer(Function)} and {@link #removeRenderer(Function)}.
 * <br><br>
 * Finally, this manager also performs translation on all messages and the {@code serialize}
 * method should be used when converting {@link Component}s into strings. This allows for
 * messages with {@link TranslatableComponent} to be automatically translated into the locale
 * of specific players, or other elements which implement {@link Localizable}. To add your
 * own translations, use {@link GlobalTranslator#addSource(Translator)} with a
 * {@link TranslationRegistry} or your own implementation of {@link Translator}.
 */
public class SerializationManager {
    private final Set<Function<Component, Component>> renderers = new CopyOnWriteArraySet<>();
    private Function<Component, String> serializer = component -> GsonComponentSerializer.gson().serialize(component);
    private Locale defaultLocale = Locale.US;

    /**
     * Gets the root serializer that is used to convert Components into Strings.
     *
     * @return the serializer
     */
    public @NotNull Function<Component, String> getSerializer() {
        return this.serializer;
    }

    /**
     * Sets the root serializer that is used to convert Components into Strings. This
     * method does not replace any existing renderers set with {@link #addRenderer(Function)}.
     *
     * @param serializer the serializer
     */
    public void setSerializer(@NotNull Function<Component, String> serializer) {
        this.serializer = serializer;
    }

    /**
     * Gets the default locale used to translate {@link TranslatableComponent} if
     * serialized using {@link #serialize(Component)} or when {@link #serialize(Component, Localizable)}
     * is used but no translation is found. Note that this is just shorthand for
     *
     * @return the default locale
     */
    public @NotNull Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Sets the default locale used to translate {@link TranslatableComponent} if
     * serialized using {@link #serialize(Component)} or when {@link #serialize(Component, Localizable)}
     * is used but no translation is found.
     *
     * @param defaultLocale the new default locale
     */
    public void setDefaultLocale(@NotNull Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * Adds a renderer that will be applied to each serializer. The order in which
     * each renderer will be applied is arbitrary. If you want control over the order
     * of renderers, create a multi-function using {@link Function#andThen(Function)}.
     *
     * @param renderer the renderer
     */
    public void addRenderer(@NotNull Function<Component, Component> renderer) {
        this.renderers.add(renderer);
    }

    /**
     * Removes a renderer.
     *
     * @param renderer the renderer
     */
    public void removeRenderer(@NotNull Function<Component, Component> renderer) {
        this.renderers.remove(renderer);
    }

    /**
     * Removes all current renderers.
     */
    public void clearRenderers() {
        this.renderers.clear();
    }

    /**
     * Gets the global translator object used by this manager. This is just shorthand for
     * {@link GlobalTranslator#get()}.
     *
     * @return the global translator
     */
    public @NotNull GlobalTranslator getTranslator() {
        return GlobalTranslator.get();
    }

    /**
     * Serializes a component into a String using the current serializer. Any registered
     * renderers are applied first, followed by the global translator. Finally, the
     * serializer set with {@link #setSerializer(Function)} is used to convert the
     * component into a String.
     *
     * @param component the component
     *
     * @return the serialized string
     */
    @Contract("null -> null")
    public @Nullable String serialize(@Nullable Component component) {
        return this.serialize(component, this.defaultLocale);
    }

    /**
     * Serializes a component into a String using the current serializer. Any registered
     * renderers are applied first, followed by the global translator. Finally, the
     * serializer set with {@link #setSerializer(Function)} is used to convert the
     * component into a String.
     *
     * @param component the component
     * @param localizable a localizable object used to translate components
     *
     * @return the serialized string
     */
    @Contract("null, _ -> null")
    public @Nullable String serialize(@Nullable Component component, @NotNull Localizable localizable) {
        return this.serialize(component, Objects.requireNonNullElse(localizable.getLocale(), this.defaultLocale));
    }

    /**
     * Serializes a component into a String using the current serializer. Any registered
     * renderers are applied first, followed by the global translator. Finally, the
     * serializer set with {@link #setSerializer(Function)} is used to convert the
     * component into a String.
     *
     * @param component the component
     * @param locale the locale used to translate components
     *
     * @return the serialized string
     */
    @Contract("null, _ -> null")
    public @Nullable String serialize(@Nullable Component component, @NotNull Locale locale) {
        if (component == null) {
            return null;
        }

        // apply renderers
        for (Function<Component, Component> renderer : this.renderers) {
            component = renderer.apply(component);
        }

        // apply translation
        component = GlobalTranslator.render(component, locale);

        // apply serialisation
        return this.serializer.apply(component);
    }
}
