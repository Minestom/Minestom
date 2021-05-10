package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class SnowGolemMeta extends AbstractGolemMeta {

    public SnowGolemMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isHasPumpkinHat() {
        return super.metadata.getIndex((byte) 15, (byte) 0x10) == (byte) 0x10;
    }

    public void setHasPumpkinHat(boolean value) {
        super.metadata.setIndex((byte) 15, Metadata.Byte(value ? (byte) 0x10 : (byte) 0x00));
    }

}
