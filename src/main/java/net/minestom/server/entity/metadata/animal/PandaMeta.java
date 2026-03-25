package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class PandaMeta extends AnimalMeta {
    public PandaMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getBreedTimer() {
        return get(MetadataDef.Panda.BREED_TIMER);
    }

    public void setBreedTimer(int value) {
        set(MetadataDef.Panda.BREED_TIMER, value);
    }

    public int getSneezeTimer() {
        return get(MetadataDef.Panda.SNEEZE_TIMER);
    }

    public void setSneezeTimer(int value) {
        set(MetadataDef.Panda.SNEEZE_TIMER, value);
    }

    public int getEatTimer() {
        return get(MetadataDef.Panda.EAT_TIMER);
    }

    public void setEatTimer(int value) {
        set(MetadataDef.Panda.EAT_TIMER, value);
    }

    public Gene getMainGene() {
        return Gene.VALUES[get(MetadataDef.Panda.MAIN_GENE)];
    }

    public void setMainGene(Gene value) {
        set(MetadataDef.Panda.MAIN_GENE, (byte) value.ordinal());
    }

    public Gene getHiddenGene() {
        return Gene.VALUES[get(MetadataDef.Panda.HIDDEN_GENE)];
    }

    public void setHiddenGene(Gene value) {
        set(MetadataDef.Panda.HIDDEN_GENE, (byte) value.ordinal());
    }

    public boolean isSneezing() {
        return get(MetadataDef.Panda.IS_SNEEZING);
    }

    public void setSneezing(boolean value) {
        set(MetadataDef.Panda.IS_SNEEZING, value);
    }

    public boolean isRolling() {
        return get(MetadataDef.Panda.IS_ROLLING);
    }

    public void setRolling(boolean value) {
        set(MetadataDef.Panda.IS_ROLLING, value);
    }

    public boolean isSitting() {
        return get(MetadataDef.Panda.IS_SITTING);
    }

    public void setSitting(boolean value) {
        set(MetadataDef.Panda.IS_SITTING, value);
    }

    public boolean isOnBack() {
        return get(MetadataDef.Panda.IS_ON_BACK);
    }

    public void setOnBack(boolean value) {
        set(MetadataDef.Panda.IS_ON_BACK, value);
    }

    public enum Gene {
        NORMAL,
        AGGRESSIVE,
        LAZY,
        WORRIED,
        PLAYFUL,
        WEAK,
        BROWN;

        private final static Gene[] VALUES = values();
    }

}
