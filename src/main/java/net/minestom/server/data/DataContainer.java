package net.minestom.server.data;

public interface DataContainer {

    /**
     * Get the data of this container
     *
     * @return the data of this container, can be null
     */
    Data getData();

    /**
     * Set the data object of this container
     *
     * @param data the data of this container
     */
    void setData(Data data);

}