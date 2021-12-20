package net.minestom.server.utils;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Produces a random element from a given set, with weights applied.
 *
 * @param <E>
 */
public class WeightedRandom<E extends WeightedRandomItem> {

    private final List<E> entries;
    private final DoubleList weightSums;
    private final double totalWeight;

    public WeightedRandom(Collection<E> items) {
        if (items.isEmpty())
            throw new IllegalArgumentException("items must not be empty");
        this.entries = new ArrayList<>(items);
        this.weightSums = new DoubleArrayList(items.size());
        double sum = 0.0;
        for (E item : items) {
            sum += item.getWeight();
            weightSums.add(sum);
        }
        this.totalWeight = sum;
    }

    /**
     * Gets a random element from this set.
     *
     * @param random a random number in range 0-1, e.g. output of {@link Random#nextDouble()}
     * @return a random element from this set
     */
    public E get(double random) {
        final double p = random * totalWeight;
        for (int i = 0; i < entries.size(); i++) {
            final double weightSum = weightSums.getDouble(i);
            if (weightSum >= p) {
                return entries.get(i);
            }
        }
        return entries.get(entries.size() - 1);
    }
}
