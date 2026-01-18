package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.particle.Particle;

public final class AreaEffectCloudMeta extends EntityMeta {
    public AreaEffectCloudMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public float getRadius() {
        return metadata.get(MetadataDef.AreaEffectCloud.RADIUS);
    }

    public void setRadius(float value) {
        metadata.set(MetadataDef.AreaEffectCloud.RADIUS, value);
    }

    public boolean isWaiting() {
        return metadata.get(MetadataDef.AreaEffectCloud.WAITING);
    }

    public void setWaiting(boolean value) {
        metadata.set(MetadataDef.AreaEffectCloud.WAITING, value);
    }

    public Particle getParticle() {
        return metadata.get(MetadataDef.AreaEffectCloud.PARTICLE);
    }

    public void setParticle(Particle value) {
        metadata.set(MetadataDef.AreaEffectCloud.PARTICLE, value);
    }

}
