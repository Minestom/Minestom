package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.TameableAnimalCreature;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.animal.tameable.CatMeta} instead.
 */
@Deprecated
public class EntityCat extends TameableAnimalCreature {

    public EntityCat(@NotNull Position spawnPosition) {
        super(EntityType.CAT, spawnPosition);
        setBoundingBox(.6D, .7D, .6D);
    }

    public EntityCat(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.CAT, spawnPosition, instance);
        setBoundingBox(.6D, .7D, .6D);
    }

    public Color getColor() {
        return Color.VALUES[this.metadata.getIndex((byte) 18, 1)];
    }

    public void setColor(Color value) {
        this.metadata.setIndex((byte) 18, Metadata.VarInt(value.ordinal()));
    }

    public boolean isLying() {
        return this.metadata.getIndex((byte) 19, false);
    }

    public void setLying(boolean value) {
        this.metadata.setIndex((byte) 19, Metadata.Boolean(value));
    }

    public boolean isRelaxed() {
        return this.metadata.getIndex((byte) 20, false);
    }

    public void setRelaxed(boolean value) {
        this.metadata.setIndex((byte) 20, Metadata.Boolean(value));
    }

    public int getCollarColor() {
        return this.metadata.getIndex((byte) 21, 14);
    }

    public void setCollarColor(int value) {
        this.metadata.setIndex((byte) 21, Metadata.VarInt(value));
    }

    public enum Color {
        TABBY,
        BLACK,
        RED,
        SIAMESE,
        BRITISH_SHORTHAIR,
        CALICO,
        PERSIAN,
        RAGDOLL,
        WHITE,
        JELLIE,
        ALL_BLACK;

        private final static Color[] VALUES = values();
    }
}
