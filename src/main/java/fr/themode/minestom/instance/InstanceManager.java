package fr.themode.minestom.instance;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InstanceManager {

    private static volatile int lastInstanceId;

    private Set<Instance> instances = Collections.synchronizedSet(new HashSet<>());

    private static int generateId() {
        return ++lastInstanceId;
    }

    public Instance createInstance() {
        Instance instance = new Instance(generateId());
        this.instances.add(instance);
        return instance;
    }

    public Set<Instance> getInstances() {
        return Collections.unmodifiableSet(instances);
    }

}
