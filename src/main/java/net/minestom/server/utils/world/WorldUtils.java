package net.minestom.server.utils.world;

import net.minestom.server.world.World;
import net.minestom.server.world.WorldContainer;
import net.minestom.server.world.SharedWorld;

public final class WorldUtils {

    /**
     * Gets if two Worlds share the same chunks.
     *
     * @param world1 the first World
     * @param world2 the second World
     * @return true if the two Worlds share the same chunks
     */
    public static boolean areLinked(World world1, World world2) {
        // SharedWorld check
        if (world1 instanceof WorldContainer && world2 instanceof SharedWorld) {
            return ((SharedWorld) world2).getWorldContainer().equals(world1);
        } else if (world2 instanceof WorldContainer && world1 instanceof SharedWorld) {
            return ((SharedWorld) world1).getWorldContainer().equals(world2);
        } else if (world1 instanceof SharedWorld && world2 instanceof SharedWorld) {
            final WorldContainer container1 = ((SharedWorld) world1).getWorldContainer();
            final WorldContainer container2 = ((SharedWorld) world2).getWorldContainer();
            return container1.equals(container2);
        }

        // WorldContainer check (copied from)
        if (world1 instanceof WorldContainer && world2 instanceof WorldContainer) {
            final WorldContainer container1 = (WorldContainer) world1;
            final WorldContainer container2 = (WorldContainer) world2;

            if (container1.getSrcWorld() != null) {
                return container1.getSrcWorld().equals(container2)
                        && container1.getLastBlockChangeTime() == container2.getLastBlockChangeTime();
            } else if (container2.getSrcWorld() != null) {
                return container2.getSrcWorld().equals(container1)
                        && container2.getLastBlockChangeTime() == container1.getLastBlockChangeTime();
            }
        }


        return false;
    }

}
