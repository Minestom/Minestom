package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.arrow.SpectralArrowMeta;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntitySpectralArrow extends EntityAbstractArrow {

    public EntitySpectralArrow(@Nullable Entity shooter, @NotNull Position spawnPosition) {
        super(shooter, EntityType.SPECTRAL_ARROW, spawnPosition);
        ((SpectralArrowMeta) getEntityMeta()).setShooter(shooter);
    }

}
