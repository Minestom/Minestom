package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class IronGolemMeta extends AbstractGolemMeta {
    public IronGolemMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isPlayerCreated() {
        return metadata.get(MetadataDef.IronGolem.IS_PLAYER_CREATED);
    }

    public void setPlayerCreated(boolean value) {
        metadata.set(MetadataDef.IronGolem.IS_PLAYER_CREATED, value);
    }

}
