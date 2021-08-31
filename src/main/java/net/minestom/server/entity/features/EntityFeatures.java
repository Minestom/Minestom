package net.minestom.server.entity.features;

import net.minestom.server.entity.features.equipment.EntityFeatureEquipment;
import net.minestom.server.entity.features.equipment.EntityFeatureNpcEquipment;
import net.minestom.server.entity.features.living.EntityFeatureLiving;

import java.util.Set;

public class EntityFeatures {

    public final static EntityFeature<EntityFeatureEquipment> EQUIPMENT = new EntityFeature<>(EntityFeatureNpcEquipment::new);
    public final static EntityFeature<EntityFeatureAttributes> ATTRIBUTES = new EntityFeature<>(EntityFeatureAttributes::new);
    public final static EntityFeature<EntityFeatureLiving> LIVING = new EntityFeature<>(EntityFeatureLiving::new, Set.of(ATTRIBUTES));
    public final static EntityFeature<EntityFeaturePickupItems> PICKUP_ITEMS = new EntityFeature<>(EntityFeaturePickupItems::new);
    public final static EntityFeature<EntityFeatureTeams> TEAMS = new EntityFeature<>(EntityFeatureTeams::new);
    public final static EntityFeature<EntityFeatureNavigable> NAVIGABLE = new EntityFeature<>(EntityFeatureNavigable::new, Set.of(ATTRIBUTES));
    public final static EntityFeature<EntityFeatureAI> AI = new EntityFeature<>(EntityFeatureAI::new, Set.of(NAVIGABLE));

}
