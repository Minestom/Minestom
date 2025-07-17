package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class PandaMeta extends AnimalMeta {
    public PandaMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getBreedTimer() {
        return metadata.get(MetadataDef.Panda.BREED_TIMER);
    }

    public void setBreedTimer(int value) {
        metadata.set(MetadataDef.Panda.BREED_TIMER, value);
    }

    public int getSneezeTimer() {
        return metadata.get(MetadataDef.Panda.SNEEZE_TIMER);
    }

    public void setSneezeTimer(int value) {
        metadata.set(MetadataDef.Panda.SNEEZE_TIMER, value);
    }

    public int getEatTimer() {
        return metadata.get(MetadataDef.Panda.EAT_TIMER);
    }

    public void setEatTimer(int value) {
        metadata.set(MetadataDef.Panda.EAT_TIMER, value);
    }

    @NotNull
    public Gene getMainGene() {
        return Gene.VALUES[metadata.get(MetadataDef.Panda.MAIN_GENE)];
    }

    public void setMainGene(@NotNull Gene value) {
        metadata.set(MetadataDef.Panda.MAIN_GENE, (byte) value.ordinal());
    }

    @NotNull
    public Gene getHiddenGene() {
        return Gene.VALUES[metadata.get(MetadataDef.Panda.HIDDEN_GENE)];
    }

    public void setHiddenGene(@NotNull Gene value) {
        metadata.set(MetadataDef.Panda.HIDDEN_GENE, (byte) value.ordinal());
    }

    public boolean isSneezing() {
        return metadata.get(MetadataDef.Panda.IS_SNEEZING);
    }

    public void setSneezing(boolean value) {
        metadata.set(MetadataDef.Panda.IS_SNEEZING, value);
    }

    public boolean isRolling() {
        return metadata.get(MetadataDef.Panda.IS_ROLLING);
    }

    public void setRolling(boolean value) {
        metadata.set(MetadataDef.Panda.IS_ROLLING, value);
    }

    public boolean isSitting() {
        return metadata.get(MetadataDef.Panda.IS_SITTING);
    }

    public void setSitting(boolean value) {
        metadata.set(MetadataDef.Panda.IS_SITTING, value);
    }

    public boolean isOnBack() {
        return metadata.get(MetadataDef.Panda.IS_ON_BACK);
    }

    public void setOnBack(boolean value) {
        metadata.set(MetadataDef.Panda.IS_ON_BACK, value);
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
