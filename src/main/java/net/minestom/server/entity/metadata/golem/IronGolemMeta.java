package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class IronGolemMeta extends AbstractGolemMeta {
    public static final byte OFFSET = AbstractGolemMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    private final static byte PLAYER_CREATED_BIT = 0x01;

    public IronGolemMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isPlayerCreated() {
        return getMaskBit(OFFSET, PLAYER_CREATED_BIT);
    }

    public void setPlayerCreated(boolean value) {
        setMaskBit(OFFSET, PLAYER_CREATED_BIT, value);
    }

}
