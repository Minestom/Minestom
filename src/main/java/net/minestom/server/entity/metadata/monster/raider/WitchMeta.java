package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class WitchMeta extends RaiderMeta {
    public WitchMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isDrinkingPotion() {
        return metadata.get(MetadataDef.Witch.IS_DRINKING_POTION);
    }

    public void setDrinkingPotion(boolean value) {
        super.metadata.set(MetadataDef.Witch.IS_DRINKING_POTION, value);
    }

}
