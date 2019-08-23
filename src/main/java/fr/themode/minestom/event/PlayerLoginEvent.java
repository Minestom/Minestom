package fr.themode.minestom.event;

import fr.themode.minestom.instance.Instance;

public class PlayerLoginEvent extends Event {

    private Instance spawningInstance;

    public Instance getSpawningInstance() {
        return spawningInstance;
    }

    public void setSpawningInstance(Instance instance) {
        this.spawningInstance = instance;
    }

}
