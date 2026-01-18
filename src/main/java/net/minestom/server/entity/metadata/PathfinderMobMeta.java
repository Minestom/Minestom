package net.minestom.server.entity.metadata;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.golem.AbstractGolemMeta;
import net.minestom.server.entity.metadata.monster.MonsterMeta;
import net.minestom.server.entity.metadata.other.AllayMeta;
import net.minestom.server.entity.metadata.water.WaterAnimalMeta;

public sealed class PathfinderMobMeta extends MobMeta permits AgeableMobMeta, AbstractGolemMeta, MonsterMeta, AllayMeta, WaterAnimalMeta {
    protected PathfinderMobMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
