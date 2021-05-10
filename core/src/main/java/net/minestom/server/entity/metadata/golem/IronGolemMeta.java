package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class IronGolemMeta extends AbstractGolemMeta {

    private final static byte MASK_INDEX = 15;

    private final static byte PLAYER_CREATED_BIT = 0x01;

    public IronGolemMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isPlayerCreated() {
        return getMaskBit(MASK_INDEX, PLAYER_CREATED_BIT);
    }

    public void setPlayerCreated(boolean value) {
        setMaskBit(MASK_INDEX, PLAYER_CREATED_BIT, value);
    }

}
