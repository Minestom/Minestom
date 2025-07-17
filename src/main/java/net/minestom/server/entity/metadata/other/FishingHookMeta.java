package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FishingHookMeta extends EntityMeta implements ObjectDataProvider {
    private Entity hooked;
    private Entity owner;

    public FishingHookMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
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
    public Entity getHookedEntity() {
        return this.hooked;
    }

    public void setHookedEntity(@Nullable Entity value) {
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
    public Entity getOwnerEntity() {
        return owner;
    }

    public void setOwnerEntity(@Nullable Entity value) {
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
