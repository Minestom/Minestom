package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class RabbitMeta extends AnimalMeta {
    public static final byte OFFSET = AnimalMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public RabbitMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Type getType() {
        int id = super.metadata.getIndex(OFFSET, 0);
        if (id == 99) {
            return Type.KILLER_BUNNY;
        }
        return Type.VALUES[id];
    }

    public void setType(@NotNull Type value) {
        int id = value == Type.KILLER_BUNNY ? 99 : value.ordinal();
        super.metadata.setIndex(OFFSET, Metadata.VarInt(id));
    }

    public enum Type {
        BROWN,
        WHITE,
        BLACK,
        BLACK_AND_WHITE,
        GOLD,
        SALT_AND_PEPPER,
        KILLER_BUNNY;

        private final static Type[] VALUES = values();
    }

}
