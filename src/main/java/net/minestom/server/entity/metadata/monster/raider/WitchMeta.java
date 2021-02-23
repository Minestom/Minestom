package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class WitchMeta extends RaiderMeta {

    public WitchMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isDrinkingPotion() {
        return getMetadata().getIndex((byte) 16, false);
    }

    public void setDrinkingPotion(boolean value) {
        getMetadata().setIndex((byte) 16, Metadata.Boolean(value));
    }

}
