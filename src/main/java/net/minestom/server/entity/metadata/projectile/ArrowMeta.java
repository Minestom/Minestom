package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArrowMeta extends AbstractArrowMeta implements ObjectDataProvider{

    public ArrowMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getColor() {
        return metadata.get(MetadataDef.Arrow.COLOR);
    }

    public void setColor(int value) {
        metadata.set(MetadataDef.Arrow.COLOR, value);
    }

    @Override
    public int getObjectData() {
        final var shooter = getShooter();

        return shooter == null ? 0 : shooter.getEntityId();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
