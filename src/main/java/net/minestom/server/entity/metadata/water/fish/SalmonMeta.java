package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.codec.Codec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class SalmonMeta extends AbstractFishMeta {
    public SalmonMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SALMON_SIZE} instead.
     */
    @Deprecated
    public @NotNull SalmonMeta.Size getSize() {
        return Size.BY_ID.getOrDefault(metadata.get(MetadataDef.Salmon.SIZE), Size.MEDIUM);
    }

    /**
     * @deprecated use {@link net.minestom.server.component.DataComponents#SALMON_SIZE} instead.
     */
    @Deprecated
    public void setSize(@NotNull SalmonMeta.Size size) {
        metadata.set(MetadataDef.Salmon.SIZE, size.id());
    }

    public enum Size {
        SMALL("small"),
        MEDIUM("medium"),
        LARGE("large");

        public static final NetworkBuffer.Type<Size> NETWORK_TYPE = NetworkBuffer.Enum(Size.class);
        public static final Codec<Size> CODEC = Codec.Enum(Size.class);

        private static final Map<String, Size> BY_ID = Arrays.stream(values())
                .collect(Collectors.toMap(Size::id, (size) -> size));

        private final String id;

        Size(@NotNull String id) {
            this.id = id;
        }

        public @NotNull String id() {
            return id;
        }
    }

}
