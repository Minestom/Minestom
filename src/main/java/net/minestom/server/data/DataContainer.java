package net.minestom.server.data;

/**
 * Represent an element which can have a {@link Data} attached to it
 */
public interface DataContainer {

    /**
     * Get the {@link Data} of this container
     *
     * @return the {@link Data} of this container, can be null
     */
    Data getData();

    /**
     * Set the {@link Data} of this container
     *
     * @param data the {@link Data} of this container, null to remove it
     */
    void setData(Data data);

}