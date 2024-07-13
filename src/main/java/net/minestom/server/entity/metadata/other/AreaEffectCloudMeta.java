package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public class AreaEffectCloudMeta extends EntityMeta {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 4;

    public AreaEffectCloudMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public float getRadius() {
        return super.metadata.getIndex(OFFSET, .5F);
    }

    public void setRadius(float value) {
        super.metadata.setIndex(OFFSET, Metadata.Float(value));
    }

    public int getColor() {
        return super.metadata.getIndex(OFFSET + 1, 0);
    }

    public void setColor(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public boolean isSinglePoint() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setSinglePoint(boolean value) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(value));
    }

    public @NotNull Particle getParticle() {
        return super.metadata.getIndex(OFFSET + 3, Particle.DUST);
    }

    public void setParticle(@NotNull Particle value) {
        super.metadata.setIndex(OFFSET + 3, Metadata.Particle(value));
    }

}
