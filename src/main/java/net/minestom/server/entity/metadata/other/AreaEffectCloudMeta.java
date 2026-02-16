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
        return get(MetadataDef.AreaEffectCloud.RADIUS);
    }

    public void setRadius(float value) {
        set(MetadataDef.AreaEffectCloud.RADIUS, value);
    }

    public boolean isWaiting() {
        return get(MetadataDef.AreaEffectCloud.WAITING);
    }

    public void setWaiting(boolean value) {
        set(MetadataDef.AreaEffectCloud.WAITING, value);
    }

    public Particle getParticle() {
        return get(MetadataDef.AreaEffectCloud.PARTICLE);
    }

    public void setParticle(Particle value) {
        set(MetadataDef.AreaEffectCloud.PARTICLE, value);
    }

}
