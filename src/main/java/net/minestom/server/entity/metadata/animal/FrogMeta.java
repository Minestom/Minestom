package net.minestom.server.entity.metadata.animal;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FrogMeta extends AnimalMeta {
    public FrogMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @Nullable Integer getTongueTarget() {
        return metadata.get(MetadataDef.Frog.TONGUE_TARGET);
    }

    public void setTongueTarget(@Nullable Integer value) {
        metadata.set(MetadataDef.Frog.TONGUE_TARGET, value);
    }

    @Override
    protected <T> @Nullable T get(@NotNull DataComponent<T> component) {
        return super.get(component);
    }

    @Override
    protected <T> void set(@NotNull DataComponent<T> component, @NotNull T value) {
        super.set(component, value);
    }
}
