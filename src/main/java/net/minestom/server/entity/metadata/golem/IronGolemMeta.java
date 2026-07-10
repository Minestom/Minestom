package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class IronGolemMeta extends AbstractGolemMeta {
    public IronGolemMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isPlayerCreated() {
        return metadata.get(MetadataDef.IronGolem.IS_PLAYER_CREATED);
    }

    public void setPlayerCreated(boolean value) {
        metadata.set(MetadataDef.IronGolem.IS_PLAYER_CREATED, value);
    }

}
