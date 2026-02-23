package net.minestom.server.instance.block.property;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;

/**
 * A property enum, associated with one or more properties in {@link Property}.
 * Only subsets of values are valid for some blocks.
 * <p>
 * Implementations are expected to be {@link Enum}s
 */
public interface PropertyEnum {
    /**
     * @return the property this enum is associated with, or null if it's associated with multiple properties.
     */
    @Contract(pure = true)
    @UnknownNullability
    String property();

    /**
     * @return the string value associated with this enum constant.
     */
    @Contract(pure = true)
    String untypedValue();
}
