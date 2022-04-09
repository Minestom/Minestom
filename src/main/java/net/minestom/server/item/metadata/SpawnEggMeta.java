package net.minestom.server.item.metadata;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class SpawnEggMeta extends ItemMeta implements ItemMetaBuilder.Provider<SpawnEggMeta.Builder> {

    private final EntityType entityType;

    protected SpawnEggMeta(@NotNull ItemMetaBuilder metaBuilder, @Nullable EntityType entityType) {
        super(metaBuilder);
        this.entityType = entityType;
    }

    public @Nullable EntityType getEntityType() {
        return entityType;
    }

    public static class Builder extends ItemMetaBuilder {

        private EntityType entityType;

        public Builder entityType(@Nullable EntityType entityType) {
            this.entityType = entityType;
            // TODO nbt
            return this;
        }

        @Override
        public @NotNull SpawnEggMeta build() {
            return new SpawnEggMeta(this, entityType);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            // TODO
        }
    }
}