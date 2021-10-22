package net.minestom.server.utils.instance;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.SharedInstance;

public final class InstanceUtils {

    /**
     * Gets if two instances share the same chunks.
     *
     * @param instance1 the first instance
     * @param instance2 the second instance
     * @return true if the two instances share the same chunks
     */
    public static boolean areLinked(Instance instance1, Instance instance2) {
        // SharedInstance check
        if (instance1 instanceof InstanceContainer && instance2 instanceof SharedInstance) {
            return ((SharedInstance) instance2).getInstanceContainer().equals(instance1);
        } else if (instance2 instanceof InstanceContainer && instance1 instanceof SharedInstance) {
            return ((SharedInstance) instance1).getInstanceContainer().equals(instance2);
        } else if (instance1 instanceof SharedInstance && instance2 instanceof SharedInstance) {
            final InstanceContainer container1 = ((SharedInstance) instance1).getInstanceContainer();
            final InstanceContainer container2 = ((SharedInstance) instance2).getInstanceContainer();
            return container1.equals(container2);
        }

        // InstanceContainer check (copied from)
        if (instance1 instanceof InstanceContainer container1 && instance2 instanceof InstanceContainer container2) {
            if (container1.getSrcInstance() != null) {
                return container1.getSrcInstance().equals(container2)
                        && container1.getLastBlockChangeTime() == container2.getLastBlockChangeTime();
            } else if (container2.getSrcInstance() != null) {
                return container2.getSrcInstance().equals(container1)
                        && container2.getLastBlockChangeTime() == container1.getLastBlockChangeTime();
            }
        }
        return false;
    }

}
