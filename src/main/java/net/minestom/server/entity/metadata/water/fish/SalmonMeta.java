package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class SalmonMeta extends AbstractFishMeta {
    public SalmonMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SALMON_SIZE} instead.
     */
    @Deprecated
    public SalmonMeta.Size getSize() {
        return Size.VALUES[metadata.get(MetadataDef.Salmon.SIZE)];
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SALMON_SIZE} instead.
     */
    @Deprecated
    public void setSize(SalmonMeta.Size size) {
        metadata.set(MetadataDef.Salmon.SIZE, size.ordinal());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> @Nullable T get(DataComponent<T> component) {
        if (component == DataComponents.SALMON_SIZE)
            return (T) getSize();
        return super.get(component);
    }

    @Override
    protected <T> void set(DataComponent<T> component, T value) {
        if (component == DataComponents.SALMON_SIZE)
            setSize((SalmonMeta.Size) value);
        else super.set(component, value);
    }

    public enum Size {
        SMALL("small"),
        MEDIUM("medium"),
        LARGE("large");

        private static final Size[] VALUES = values();

        public static final NetworkBuffer.Type<Size> NETWORK_TYPE = NetworkBuffer.Enum(Size.class);
        public static final Codec<Size> CODEC = Codec.Enum(Size.class);

        private static final Map<String, Size> BY_ID = Arrays.stream(values())
                .collect(Collectors.toMap(Size::id, (size) -> size));

        private final String id;

        Size(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

}
