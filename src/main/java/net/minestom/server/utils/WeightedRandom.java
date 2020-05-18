package net.minestom.server.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Produces a random element from a given set, with weights applied
 * @param <E>
 */
public class WeightedRandom<E extends WeightedRandomItem> {

    private final List<E> entries;
    private final List<Double> weightSums;
    private final double totalWeight;

    public WeightedRandom(Collection<E> items) {
        if(items.isEmpty())
            throw new IllegalArgumentException("items must not be empty");
        this.entries = new ArrayList<>(items);
        this.weightSums = new ArrayList<>(items.size());
        double sum = 0.0;
        for(E item : items) {
            sum += item.getWeight();
            weightSums.add(sum);
        }
        this.totalWeight = sum;
    }

    /**
     * Gets a random element from this set
     * @param rng Random Number Generator to generate random numbers with
     * @return
     */
    public E get(Random rng) {
        double p = rng.nextDouble()*totalWeight;
        for (int i = 0; i < entries.size(); i++) {
            double weightSum = weightSums.get(i);
            if(weightSum >= p) {
                return entries.get(i);
            }
        }
        return entries.get(entries.size()-1);
    }
}
