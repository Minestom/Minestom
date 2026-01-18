package net.minestom.server.entity.metadata.water;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.PathfinderMobMeta;
import net.minestom.server.entity.metadata.water.fish.AbstractFishMeta;

public sealed abstract class WaterAnimalMeta extends PathfinderMobMeta permits AbstractFishMeta {
    protected WaterAnimalMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
