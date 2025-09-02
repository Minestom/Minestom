package net.minestom.server.instance.light;

/**
 * A unit of light calculation.
 * <p>
 * This unit is standalone and can run independently without threat of race conditions/threading issues.
 */
public interface LightCalculation {
    /**
     * Get the index of this calculation. Indices are generated sequentially to identify how
     * recent a calculation is, and always use the most recent one.
     *
     * @return the index of this calculation.
     */
    long index();

    /**
     * Runs the calculation.
     */
    void calculate();
}
