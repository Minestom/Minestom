package fr.themode.minestom.instance;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InstanceManager {

    private Set<Instance> instances = Collections.synchronizedSet(new HashSet<>());

    public InstanceContainer createInstanceContainer(File folder) {
        InstanceContainer instance = new InstanceContainer(UUID.randomUUID(), folder);
        this.instances.add(instance);
        return instance;
    }

    public InstanceContainer createInstanceContainer() {
        return createInstanceContainer(null);
    }

    public SharedInstance createSharedInstance(InstanceContainer instanceContainer) {
        if (instanceContainer == null)
            throw new IllegalArgumentException("Instance container cannot be null when creating a Shared instance!");

        SharedInstance sharedInstance = new SharedInstance(UUID.randomUUID(), instanceContainer);
        instanceContainer.addSharedInstance(sharedInstance);
        this.instances.add(sharedInstance);
        return sharedInstance;
    }

    public Set<Instance> getInstances() {
        return Collections.unmodifiableSet(instances);
    }

}
