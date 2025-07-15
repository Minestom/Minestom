package net.minestom.server.entity.metadata.villager;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.*;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jspecify.annotations.Nullable;

public class VillagerMeta extends AbstractVillagerMeta {
    public VillagerMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public VillagerData getVillagerData() {
        return metadata.get(MetadataDef.Villager.VARIANT);
    }

    public void setVillagerData(VillagerData data) {
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

    public record VillagerData(
            VillagerType type,
            VillagerProfession profession,
            Level level
    ) {
        public static final VillagerData DEFAULT = new VillagerData(VillagerType.DESERT, VillagerProfession.NONE, Level.NOVICE);

        public static final NetworkBuffer.Type<VillagerData> NETWORK_TYPE = NetworkBufferTemplate.template(
                VillagerType.NETWORK_TYPE, VillagerData::type,
                VillagerProfession.NETWORK_TYPE, VillagerData::profession,
                Level.NETWORK_TYPE, VillagerData::level,
                VillagerData::new);

        public VillagerData withType(VillagerType type) {
            return new VillagerData(type, this.profession, this.level);
        }

        public VillagerData withProfession(VillagerProfession profession) {
            return new VillagerData(this.type, profession, this.level);
        }

        public VillagerData withLevel(Level level) {
            return new VillagerData(this.type, this.profession, level);
        }
    }

    public enum Level {
        NOVICE,
        APPRENTICE,
        JOURNEYMAN,
        EXPERT,
        MASTER;

        public static final NetworkBuffer.Type<Level> NETWORK_TYPE = NetworkBuffer.Enum(Level.class);
    }

}
