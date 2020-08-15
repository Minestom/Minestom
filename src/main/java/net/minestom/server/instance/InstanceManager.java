package net.minestom.server.instance;

import net.minestom.server.storage.StorageFolder;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Used to register instances
 */
public final class InstanceManager {

    private final Set<Instance> instances = new CopyOnWriteArraySet<>();

    /**
     * Register an {@link InstanceContainer}
     *
     * @param instanceContainer the instance to register
     * @return the registered {@link InstanceContainer}
     */
    public InstanceContainer registerInstanceContainer(InstanceContainer instanceContainer) {
        registerInstance(instanceContainer);
        return instanceContainer;
    }

    /**
     * Create and register an {@link InstanceContainer} with the specified dimension and storage folder
     *
     * @param dimensionType the dimension of the instance
     * @param storageFolder the storage folder of the instance, can be null
     * @return the created {@link InstanceContainer}
     */
    public InstanceContainer createInstanceContainer(DimensionType dimensionType, StorageFolder storageFolder) {
        final InstanceContainer instance = new InstanceContainer(UUID.randomUUID(), dimensionType, storageFolder);
        return registerInstanceContainer(instance);
    }

    /**
     * Create and register an {@link InstanceContainer} with the specified storage folder
     *
     * @param storageFolder the storage folder of the instance, can be null
     * @return the created {@link InstanceContainer}
     */
    public InstanceContainer createInstanceContainer(StorageFolder storageFolder) {
        return createInstanceContainer(DimensionType.OVERWORLD, storageFolder);
    }

    /**
     * Create and register an {@link InstanceContainer} with the specified dimension
     *
     * @param dimensionType the dimension of the instance
     * @return the created {@link InstanceContainer}
     */
    public InstanceContainer createInstanceContainer(DimensionType dimensionType) {
        return createInstanceContainer(dimensionType, null);
    }

    /**
     * Create and register an {@link InstanceContainer}
     *
     * @return the created {@link InstanceContainer}
     */
    public InstanceContainer createInstanceContainer() {
        return createInstanceContainer(DimensionType.OVERWORLD);
    }

    /**
     * Register a {@link SharedInstance}
     * <p>
     * WARNING: the shared instance needs to have an {@link InstanceContainer} assigned to it
     *
     * @param sharedInstance the instance to register
     * @return the registered {@link SharedInstance}
     * @throws NullPointerException if the shared instance doesn't have an {@link InstanceContainer} assigned to it
     */
    public SharedInstance registerSharedInstance(SharedInstance sharedInstance) {
        final InstanceContainer instanceContainer = sharedInstance.getInstanceContainer();
        Check.notNull(instanceContainer, "SharedInstance needs to have an InstanceContainer to be created!");

        instanceContainer.addSharedInstance(sharedInstance);
        registerInstance(sharedInstance);
        return sharedInstance;
    }

    /**
     * Create and register a {@link SharedInstance}
     *
     * @param instanceContainer the container assigned to the shared instance
     * @return the created {@link SharedInstance}
     * @throws IllegalStateException if {@code instanceContainer} is not registered
     */
    public SharedInstance createSharedInstance(InstanceContainer instanceContainer) {
        Check.notNull(instanceContainer, "Instance container cannot be null when creating a SharedInstance!");
        Check.stateCondition(!instanceContainer.isRegistered(), "The container needs to be register in the InstanceManager");

        final SharedInstance sharedInstance = new SharedInstance(UUID.randomUUID(), instanceContainer);
        return registerSharedInstance(sharedInstance);
    }

    /**
     * Get all the registered instances
     *
     * @return an unmodifiable set containing all the registered instances
     */
    public Set<Instance> getInstances() {
        return Collections.unmodifiableSet(instances);
    }

    /**
     * Register the instance internally
     *
     * @param instance the instance to register
     */
    private void registerInstance(Instance instance) {
        instance.setRegistered(true);
        this.instances.add(instance);
    }

}
