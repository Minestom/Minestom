package net.minestom.server.entity.metadata.monster.zombie;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.VillagerType;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import org.jspecify.annotations.Nullable;

public class ZombieVillagerMeta extends ZombieMeta {
    public ZombieVillagerMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isConverting() {
        return metadata.get(MetadataDef.ZombieVillager.IS_CONVERTING);
    }

    public void setConverting(boolean value) {
        metadata.set(MetadataDef.ZombieVillager.IS_CONVERTING, value);
    }

    public VillagerMeta.VillagerData getVillagerData() {
        return metadata.get(MetadataDef.ZombieVillager.VILLAGER_DATA);
    }

    public void setVillagerData(VillagerMeta.VillagerData data) {
        metadata.set(MetadataDef.Villager.VARIANT, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.VILLAGER_VARIANT)
            return (T) getVillagerData().type();
        return super.get(component);
    }

    @Override
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.VILLAGER_VARIANT)
            setVillagerData(getVillagerData().withType((VillagerType) value));
        else super.set(component, value);
    }
}
