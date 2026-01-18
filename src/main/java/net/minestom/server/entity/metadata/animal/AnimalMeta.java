package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta;

public sealed abstract class AnimalMeta extends AgeableMobMeta permits AbstractHorseMeta, ArmadilloMeta, BeeMeta, ChickenMeta, CowMeta, FoxMeta, FrogMeta, GoatMeta, HappyGhastMeta, HoglinMeta, MooshroomMeta, OcelotMeta, PandaMeta, PigMeta, PolarBearMeta, RabbitMeta, SheepMeta, SnifferMeta, StriderMeta, TurtleMeta, TameableAnimalMeta, net.minestom.server.entity.metadata.water.AxolotlMeta {
    protected AnimalMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
