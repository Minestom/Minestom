package fr.themode.minestom.instance;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InstanceManager {

    private Set<Instance> instances = Collections.synchronizedSet(new HashSet<>());

    public Instance createInstance(File folder) {
        Instance instance = new InstanceContainer(UUID.randomUUID(), folder);
        this.instances.add(instance);
        return instance;
    }

    public Instance createInstance() {
        return createInstance(null);
    }

    public Set<Instance> getInstances() {
        return Collections.unmodifiableSet(instances);
    }

}
