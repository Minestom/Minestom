package net.minestom.server.instance.chunksystem;

import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.ApiStatus;

/**
 * Used to calculate how much the priority should drop for a chunk with a given distance from the center of the claim.
 * This is used to make closer chunks load first.
 * <p>
 * Custom implementations must take care:
 * <ul>
 *     <li>
 *         This must propagate to higher values the further the distance
 *     </li>
 *     <li>
 *         When propagating from chunk A, the updates propagate to all 8 neighboring chunks.
 *         If the calculated priority is higher than the priority of the update origin chunk,
 *         the update won't get propagated.
 *     </li>
 *     <li>
 *         It is allowed to use very small values, with differences larger than {@link Vec#EPSILON}.
 *         A possible implementation could calculate something like {@code max(deltaX, deltaZ)/100000D}
 *         to make sure the priority drop won't calculate a value larger than 1 for neighbors.
 *     </li>
 * </ul>
 */
@ApiStatus.Experimental
public interface PriorityDrop {
    /**
     * Calculates the drop-off for the chunk at x,z.
     * Returns a double, this should only be used internally.
     */
    double calculate(int claimX, int claimZ, int x, int z);

    /**
     * deltaX^2 + deltaZ^2
     */
    record HypotenuseSquared() implements PriorityDrop {
        @Override
        public double calculate(int claimX, int claimZ, int x, int z) {
            var dx = claimX - x;
            var dz = claimZ - z;
            return dx * dx + dz * dz;
        }
    }

    /**
     * sqrt(deltaX^2 + deltaZ^2)
     */
    record Hypotenuse() implements PriorityDrop {
        @Override
        public double calculate(int claimX, int claimZ, int x, int z) {
            var dx = claimX - x;
            var dz = claimZ - z;
            return Math.sqrt(dx * dx + dz * dz);
        }
    }

    /**
     * deltaX + deltaZ
     */
    record Simple() implements PriorityDrop {
        @Override
        public double calculate(int claimX, int claimZ, int x, int z) {
            return Math.abs(claimX - x) + Math.abs(claimZ - z);
        }
    }

    /**
     * max(deltaX, deltaZ)
     */
    record Square() implements PriorityDrop {
        @Override
        public double calculate(int claimX, int claimZ, int x, int z) {
            return Math.max(Math.abs(claimX - x), Math.abs(claimZ - z));
        }
    }
}