package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class SnowGolemMeta extends AbstractGolemMeta {

    public SnowGolemMeta(@NotNull Entity entity) {
        super(entity);
    }

    public boolean isHasPumpkinHat() {
        return getMetadata().getIndex((byte) 15, (byte) 0x10) == (byte) 0x10;
    }

    public void setHasPumpkinHat(boolean value) {
        getMetadata().setIndex((byte) 15, Metadata.Byte(value ? (byte) 0x10 : (byte) 0x00));
    }

}
