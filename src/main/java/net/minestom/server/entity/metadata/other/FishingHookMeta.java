package net.minestom.server.entity.metadata.other;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;

public class FishingHookMeta extends EntityMeta implements ObjectDataProvider {

    private Entity hooked;
    private Entity owner;

    public FishingHookMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @Nullable
    public Entity getHookedEntity() {
        return this.hooked;
    }

    public void setHookedEntity(@Nullable Entity value) {
        this.hooked = value;
        int entityID = value == null ? 0 : value.getEntityId() + 1;
        super.metadata.setIndex((byte) 7, Metadata.VarInt(entityID));
    }
    
    @NotNull
    public Entity getOwnerEntity() {
        return owner;
    }

    public void setOwnerEntity(@NotNull Entity value) {
        this.owner = value;
    }

    public boolean isCatchable() {
        return super.metadata.getIndex((byte) 8, false);
    }

    public void setCatchable(boolean value) {
        super.metadata.setIndex((byte) 8, Metadata.Boolean(value));
    }

    @Override
    public int getObjectData() {
        return owner.getEntityId();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return false;
    }
}
