package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class MobMeta extends LivingEntityMeta {
    protected MobMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isNoAi() {
        return metadata.get(MetadataDef.Mob.NO_AI);
    }

    public void setNoAi(boolean value) {
        metadata.set(MetadataDef.Mob.NO_AI, value);
    }

    public boolean isLeftHanded() {
        return metadata.get(MetadataDef.Mob.IS_LEFT_HANDED);
    }

    public void setLeftHanded(boolean value) {
        metadata.set(MetadataDef.Mob.IS_LEFT_HANDED, value);
    }

    public boolean isAggressive() {
        return metadata.get(MetadataDef.Mob.IS_AGGRESSIVE);
    }

    public void setAggressive(boolean value) {
        metadata.set(MetadataDef.Mob.IS_AGGRESSIVE, value);
    }

}
