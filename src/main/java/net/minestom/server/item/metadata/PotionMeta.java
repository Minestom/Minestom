package net.minestom.server.item.metadata;

import net.minestom.server.potion.PotionType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PotionMeta extends ItemMeta {

    private Set<PotionType> potionTypes = new HashSet<>();

    /**
     * Get the item potion types
     *
     * @return an unmodifiable {@link Set} containing the item potion types
     */
    public Set<PotionType> getPotionTypes() {
        return Collections.unmodifiableSet(potionTypes);
    }

    /**
     * Add a potion type to the item
     *
     * @param potionType the potion type to add
     */
    public void addPotionType(PotionType potionType) {
        this.potionTypes.add(potionType);
    }

    /**
     * Remove a potion type to the item
     *
     * @param potionType the potion type to remove
     */
    public void removePotionType(PotionType potionType) {
        this.potionTypes.remove(potionType);
    }

    @Override
    public boolean hasNbt() {
        return !potionTypes.isEmpty();
    }

    @Override
    public boolean isSimilar(ItemMeta itemMeta) {
        return itemMeta instanceof PotionMeta && ((PotionMeta) itemMeta).potionTypes.equals(potionTypes);
    }

    @Override
    public ItemMeta clone() {
        PotionMeta potionMeta = new PotionMeta();
        potionMeta.potionTypes = new HashSet<>(potionTypes);

        return potionMeta;
    }
}
