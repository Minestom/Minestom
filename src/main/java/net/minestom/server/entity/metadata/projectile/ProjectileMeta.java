package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.item.FireballMeta;
import net.minestom.server.entity.metadata.item.SmallFireballMeta;
import org.jetbrains.annotations.Nullable;

public sealed interface ProjectileMeta permits FireballMeta, SmallFireballMeta, AbstractWindChargeMeta, ArrowMeta, DragonFireballMeta, FireworkRocketMeta, SpectralArrowMeta, WitherSkullMeta {

    @Nullable
    Entity getShooter();

    void setShooter(@Nullable Entity shooter);

}
