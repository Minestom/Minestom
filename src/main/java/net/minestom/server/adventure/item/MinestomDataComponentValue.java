package net.minestom.server.adventure.item;

import net.kyori.adventure.text.event.DataComponentValue;
import net.minestom.server.component.DataComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a {@linkplain DataComponentValue data component value}, which holds a {@linkplain DataComponent data
 * component} and its value.
 *
 * @param component the data component
 * @param value the value
 * @param <T> the type of the value
 */
public record MinestomDataComponentValue<T>(@NotNull DataComponent<T> component, @NotNull T value)
        implements DataComponentValue {}