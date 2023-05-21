package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InteractionMeta extends EntityMeta {

    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    public InteractionMeta(@Nullable Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public float getWidth() {
        return super.metadata.getIndex(OFFSET, 0f);
    }

    public void setWidth(float value) {
        super.metadata.setIndex(OFFSET, Metadata.Float(value));
    }

    public float getHeight() {
        return super.metadata.getIndex(OFFSET + 1, 0f);
    }

    public void setHeight(float value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.Float(value));
    }

    public boolean getResponse() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setResponse(boolean response) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(response));
    }
}
