package net.minestom.server.entity.type.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityLlama extends EntityChestedHorse {

    public EntityLlama(@NotNull Position spawnPosition) {
        super(EntityType.LLAMA, spawnPosition);
        setBoundingBox(.9D, 1.87D, .9D);
    }

    public EntityLlama(@NotNull Position spawnPosition, @Nullable Instance instance) {
        super(EntityType.LLAMA, spawnPosition, instance);
        setBoundingBox(.9D, 1.87D, .9D);
    }

    public int getStrength() {
        return this.metadata.getIndex((byte) 19, 0);
    }

    public void setStrength(int value) {
        this.metadata.setIndex((byte) 19, Metadata.VarInt(value));
    }

    public int getCarpetColor() {
        return this.metadata.getIndex((byte) 20, -1);
    }

    public void setCarpetColor(int value) {
        this.metadata.setIndex((byte) 20, Metadata.VarInt(value));
    }

    public Variant getVariant() {
        return Variant.VALUES[this.metadata.getIndex((byte) 21, 0)];
    }

    public void setVariant(Variant value) {
        this.metadata.setIndex((byte) 21, Metadata.VarInt(value.ordinal()));
    }

    public enum Variant {
        CREAMY,
        WHITE,
        BROWN,
        GRAY;

        private final static Variant[] VALUES = values();
    }

}
