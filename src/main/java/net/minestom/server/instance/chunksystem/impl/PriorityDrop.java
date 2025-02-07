package net.minestom.server.instance.chunksystem.impl;

/**
 * Used to calculate how much the priority should drop for a chunk with a given distance from the center of the claim.
 * This is used to make closer chunks load first.
 */
sealed interface PriorityDrop {
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
            var dx = claimX - x;
            var dz = claimZ - z;
            return Math.abs(dx) + Math.abs(dz);
        }
    }
}