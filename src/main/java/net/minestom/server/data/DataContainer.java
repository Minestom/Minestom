package net.minestom.server.data;

/**
 * Represents an element which can have a {@link Data} attached to it.
 * <p>
 * The data will always be optional.
 */
public interface DataContainer {

    /**
     * Get the {@link Data} of this container.
     * <p>
     * A {@link DataContainer} data is always optional,
     * meaning that this will be null if no data has been defined.
     *
     * @return the {@link Data} of this container, can be null
     */
    Data getData();

    /**
     * Set the {@link Data} of this container.
     *
     * @param data the {@link Data} of this container, null to remove it
     */
    void setData(Data data);

}