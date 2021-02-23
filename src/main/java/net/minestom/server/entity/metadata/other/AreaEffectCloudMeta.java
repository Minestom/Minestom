package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.BaseEntityMeta;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleType;
import org.jetbrains.annotations.NotNull;

public class AreaEffectCloudMeta extends BaseEntityMeta {

    public AreaEffectCloudMeta(@NotNull Entity entity) {
        super(entity);
    }

    public float getRadius() {
        return getMetadata().getIndex((byte) 7, .5F);
    }

    public void setRadius(float value) {
        getMetadata().setIndex((byte) 7, Metadata.Float(value));
    }

    public int getColor() {
        return getMetadata().getIndex((byte) 8, 0);
    }

    public void setColor(int value) {
        getMetadata().setIndex((byte) 8, Metadata.VarInt(value));
    }

    public boolean isSinglePoint() {
        return getMetadata().getIndex((byte) 9, false);
    }

    public void setSinglePoint(boolean value) {
        getMetadata().setIndex((byte) 9, Metadata.Boolean(value));
    }

    public Particle getParticle() {
        return getMetadata().getIndex((byte) 10, new Particle(ParticleType.EFFECT, null));
    }

    public void setParticle(Particle value) {
        getMetadata().setIndex((byte) 11, Metadata.Particle(value));
    }

}
