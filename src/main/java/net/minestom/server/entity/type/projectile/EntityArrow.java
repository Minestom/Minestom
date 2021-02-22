package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityArrow extends EntityAbstractArrow {

    public EntityArrow(@Nullable Entity shooter, @NotNull Position spawnPosition) {
        super(shooter, EntityType.ARROW, spawnPosition);

    }

    public void setColor(int value) {
        this.metadata.setIndex((byte) 9, Metadata.VarInt(value));
    }

    public int getColor() {
        return this.metadata.getIndex((byte) 9, -1);
    }

}
