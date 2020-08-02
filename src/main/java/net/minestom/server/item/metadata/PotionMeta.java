package net.minestom.server.item.metadata;

import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.Registries;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class PotionMeta implements ItemMeta {

    private PotionType potionType;

    /**
     * Get the potion type
     *
     * @return the potion type
     */
    public PotionType getPotionType() {
        return potionType;
    }

    /**
     * Change the potion type
     *
     * @param potionType the new potion type
     */
    public void setPotionType(PotionType potionType) {
        this.potionType = potionType;
    }

    @Override
    public boolean hasNbt() {
        return potionType != null;
    }

    @Override
    public boolean isSimilar(ItemMeta itemMeta) {
        return itemMeta instanceof PotionMeta && ((PotionMeta) itemMeta).potionType == potionType;
    }

    @Override
    public void read(NBTCompound compound) {
        if (compound.containsKey("Potion")) {
            this.potionType = Registries.getPotionType(compound.getString("Potion"));
        }
    }

    @Override
    public void write(NBTCompound compound) {
        if (potionType != null) {
            compound.setString("Potion", potionType.getNamespaceID());
        }
    }

    @Override
    public ItemMeta clone() {
        PotionMeta potionMeta = new PotionMeta();
        potionMeta.potionType = potionType;

        return potionMeta;
    }
}
