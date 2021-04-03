package net.minestom.server.adventure;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Represents an object that holds some amount of components.
 *
 * @param <T> the holding class
 */
public interface ComponentHolder<T> {

    /**
     * Gets the components held by this object.
     *
     * @return the components
     */
    @NotNull Collection<Component> components();

    /**
     * Returns a copy of this object. For each component this object holds, the operator
     * is applied to the copy before returning.
     *
     * @param operator the operator
     * @return the copy
     */
    @NotNull T copyWithOperator(@NotNull UnaryOperator<Component> operator);

    /**
     * Visits each component held by this object.
     *
     * @param visitor the visitor
     */
    default void visitComponents(@NotNull Consumer<Component> visitor) {
        for (Component component : this.components()) {
            visitor.accept(component);
        }
    }
}
