package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.PathfinderMobMeta;

public sealed abstract class MonsterMeta extends PathfinderMobMeta permits BasePiglinMeta, BlazeMeta, BreezeMeta, CreakingMeta, CreeperMeta, EndermanMeta, EndermiteMeta, GiantMeta, GuardianMeta, SilverfishMeta, SpiderMeta, VexMeta, WardenMeta, WitherMeta, ZoglinMeta, net.minestom.server.entity.metadata.monster.raider.RaiderMeta, net.minestom.server.entity.metadata.monster.skeleton.AbstractSkeletonMeta, net.minestom.server.entity.metadata.monster.zombie.ZombieMeta {
    protected MonsterMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
