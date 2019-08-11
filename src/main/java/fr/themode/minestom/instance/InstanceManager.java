package fr.themode.minestom.instance;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InstanceManager {

    private Set<Instance> instances = Collections.synchronizedSet(new HashSet<>());

    public Instance createInstance() {
        Instance instance = new Instance(UUID.randomUUID());
        this.instances.add(instance);
        return instance;
    }

    public Set<Instance> getInstances() {
        return Collections.unmodifiableSet(instances);
    }

}
