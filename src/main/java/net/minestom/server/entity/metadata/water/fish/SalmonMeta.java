package net.minestom.server.entity.metadata.water.fish;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class SalmonMeta extends AbstractFishMeta {
    public SalmonMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull Variant getVariant() {
        return Variant.BY_ID.getOrDefault(metadata.get(MetadataDef.Salmon.VARIANT), Variant.MEDIUM);
    }

    public void setVariant(@NotNull Variant variant) {
        metadata.set(MetadataDef.Salmon.VARIANT, variant.id());
    }

    public enum Variant {
        SMALL("small"),
        MEDIUM("medium"),
        LARGE("large");

        private static final Map<String, Variant> BY_ID = Arrays.stream(values())
                .collect(Collectors.toMap(Variant::id, (variant) -> variant));

        private final String id;
        Variant(@NotNull String id) {
            this.id = id;
        }

        public @NotNull String id() {
            return id;
        }
    }

}
