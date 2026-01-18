package net.minestom.server.entity.metadata;

import module net.minestom.server;

public sealed abstract class MobMeta extends LivingEntityMeta permits PathfinderMobMeta, AmbientCreatureMeta, FlyingMeta, EnderDragonMeta, SlimeMeta {
    protected MobMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isNoAi() {
        return get(MetadataDef.Mob.NO_AI);
    }

    public void setNoAi(boolean value) {
        set(MetadataDef.Mob.NO_AI, value);
    }

    public boolean isLeftHanded() {
        return get(MetadataDef.Mob.IS_LEFT_HANDED);
    }

    public void setLeftHanded(boolean value) {
        set(MetadataDef.Mob.IS_LEFT_HANDED, value);
    }

    public boolean isAggressive() {
        return get(MetadataDef.Mob.IS_AGGRESSIVE);
    }

    public void setAggressive(boolean value) {
        set(MetadataDef.Mob.IS_AGGRESSIVE, value);
    }

}
