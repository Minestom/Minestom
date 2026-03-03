package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * A property associated with some type {@code T}.
 * Some valid values for {@code T} may not be valid for certain blocks.
 */
public sealed interface Property<T> extends Properties permits BooleanProperty, EnumProperty, IntegerProperty {
    /**
     * @return the key this property is associated with.
     */
    @Contract(pure = true)
    String key();

    /**
     * Gets the typed value associated with the untyped string value.
     *
     * @param value the untyped value
     * @return the typed value equivalent of the untyped value, or null if there is no typed value associated with {@code value}.
     */
    @Contract(pure = true)
    @Nullable
    T parse(String value);

    /**
     * Gets the untyped string value associated with the typed value.
     *
     * @param value the typed value
     * @return the untyped value equivalent of the typed value.
     */
    @Contract(pure = true)
    String valueOf(T value);
}
