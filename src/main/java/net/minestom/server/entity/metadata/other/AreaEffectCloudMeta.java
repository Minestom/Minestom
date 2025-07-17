package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

public class AreaEffectCloudMeta extends EntityMeta {
    public AreaEffectCloudMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public float getRadius() {
        return metadata.get(MetadataDef.AreaEffectCloud.RADIUS);
    }

    public void setRadius(float value) {
        metadata.set(MetadataDef.AreaEffectCloud.RADIUS, value);
    }

    public int getColor() {
        return metadata.get(MetadataDef.AreaEffectCloud.COLOR);
    }

    public void setColor(int value) {
        metadata.set(MetadataDef.AreaEffectCloud.COLOR, value);
    }

    public boolean isSinglePoint() {
        return metadata.get(MetadataDef.AreaEffectCloud.IGNORE_RADIUS_AND_SINGLE_POINT);
    }

    public void setSinglePoint(boolean value) {
        metadata.set(MetadataDef.AreaEffectCloud.IGNORE_RADIUS_AND_SINGLE_POINT, value);
    }

    public @NotNull Particle getParticle() {
        return metadata.get(MetadataDef.AreaEffectCloud.PARTICLE);
    }

    public void setParticle(@NotNull Particle value) {
        metadata.set(MetadataDef.AreaEffectCloud.PARTICLE, value);
    }

}
