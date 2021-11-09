package net.minestom.server.data;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an element which can have a {@link Data} attached to it.
 * <p>
 * The data will always be optional and can therefore be null.
 *
 * @deprecated switch to the Tag API instead
 */
@Deprecated
public interface DataContainer {

    /**
     * Gets the {@link Data} of this container.
     * <p>
     * A {@link DataContainer} data is always optional,
     * meaning that this will be null if no data has been defined.
     *
     * @return the {@link Data} of this container, can be null
     * @deprecated use the tag API https://wiki.minestom.net/feature/tags
     */
    @Deprecated
    @Nullable Data getData();

    /**
     * Sets the {@link Data} of this container.
     * <p>
     * Default implementations are {@link DataImpl} and {@link SerializableDataImpl} depending
     * on your use-case.
     *
     * @param data the new {@link Data} of this container, null to remove it
     * @deprecated use the tag API https://wiki.minestom.net/feature/tags
     */
    @Deprecated
    void setData(@Nullable Data data);
}
