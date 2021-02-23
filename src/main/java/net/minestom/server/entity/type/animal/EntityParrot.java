package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.TameableAnimalCreature;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityParrot extends TameableAnimalCreature {

    public EntityParrot(@NotNull Position spawnPosition) {
        super(EntityType.PARROT, spawnPosition);
        setBoundingBox(.5D, .9D, .5D);
    }

    public EntityParrot(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.PARROT, spawnPosition, instance);
        setBoundingBox(.5D, .9D, .5D);
    }

    public Color getColor() {
        return Color.VALUES[this.metadata.getIndex((byte) 18, 0)];
    }

    public void setColor(Color value) {
        this.metadata.setIndex((byte) 18, Metadata.VarInt(value.ordinal()));
    }

    public enum Color {
        RED_BLUE,
        BLUE,
        GREEN,
        YELLOW_BLUE,
        GREY;

        private final static Color[] VALUES = values();
    }

}
