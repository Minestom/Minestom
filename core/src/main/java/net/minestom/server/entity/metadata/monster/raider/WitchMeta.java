package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class WitchMeta extends RaiderMeta {

    public WitchMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isDrinkingPotion() {
        return super.metadata.getIndex((byte) 16, false);
    }

    public void setDrinkingPotion(boolean value) {
        super.metadata.setIndex((byte) 16, Metadata.Boolean(value));
    }

}
