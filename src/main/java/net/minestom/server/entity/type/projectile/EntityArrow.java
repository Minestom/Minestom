package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.arrow.ArrowMeta;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityArrow extends EntityAbstractArrow {

    public EntityArrow(@Nullable Entity shooter, @NotNull Position spawnPosition) {
        super(shooter, EntityType.ARROW, spawnPosition);
        ((ArrowMeta) getEntityMeta()).setShooter(shooter);
    }

}
