package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class FishingHookMeta extends EntityMeta implements ObjectDataProvider {
    private MetaTarget hooked;
    private MetaTarget owner;

    public FishingHookMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getHookedEntityId() {
        return metadata.get(MetadataDef.FishingHook.HOOKED);
    }

    @ApiStatus.Internal
    public void setHookedEntityId(int value) {
        metadata.set(MetadataDef.FishingHook.HOOKED, value);
    }

    @Nullable
    public MetaTarget getHookedEntity() {
        return this.hooked;
    }

    public void setHookedEntity(@Nullable MetaTarget value) {
        this.hooked = value;
        int entityID = value == null ? 0 : value.getEntityId() + 1;
        setHookedEntityId(entityID);
    }

    public boolean isCatchable() {
        return metadata.get(MetadataDef.FishingHook.IS_CATCHABLE);
    }

    public void setCatchable(boolean value) {
        metadata.set(MetadataDef.FishingHook.IS_CATCHABLE, value);
    }

    @Nullable
    public MetaTarget getOwnerEntity() {
        return owner;
    }

    public void setOwnerEntity(@Nullable MetaTarget value) {
        this.owner = value;
    }

    @Override
    public int getObjectData() {
        return owner != null ? owner.getEntityId() : 0;
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return false;
    }
}
