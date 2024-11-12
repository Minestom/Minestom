package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class RabbitMeta extends AnimalMeta {
    public RabbitMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public Type getType() {
        int id = metadata.get(MetadataDef.Rabbit.TYPE);
        if (id == 99) {
            return Type.KILLER_BUNNY;
        }
        return Type.VALUES[id];
    }

    public void setType(@NotNull Type value) {
        int id = value == Type.KILLER_BUNNY ? 99 : value.ordinal();
        metadata.set(MetadataDef.Rabbit.TYPE, id);
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
