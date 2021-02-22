package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 22.02.2021
 */
public class EntitySpectralArrow extends EntityAbstractArrow {

    public EntitySpectralArrow(@Nullable Entity shooter, @NotNull Position spawnPosition) {
        super(shooter, EntityType.SPECTRAL_ARROW, spawnPosition);
    }

}
