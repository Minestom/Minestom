package net.minestom.server.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;

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
 * performs operations on each component before the final serialization.
 *
 * @see #setSerializer(Function) (Function)
 * @see #addRenderer(Function)
 */
public class SerializationManager {
    private final Set<Function<Component, Component>> renderers = new CopyOnWriteArraySet<>();
    private Function<Component, String> serializer = component -> GsonComponentSerializer.gson().serialize(component);

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
     * Serializes a component into a String using the current serializer.
     *
     * @param component the component
     *
     * @return the serialized string
     */
    public String serialize(Component component) {
        for (Function<Component, Component> renderer : this.renderers) {
            component = renderer.apply(component);
        }

        return this.serializer.apply(component);
    }
}
