package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.Nullable;

public final class InteractionMeta extends EntityMeta {
    public InteractionMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public float getWidth() {
        return get(MetadataDef.Interaction.WIDTH);
    }

    public void setWidth(float value) {
        set(MetadataDef.Interaction.WIDTH, value);
    }

    public float getHeight() {
        return get(MetadataDef.Interaction.HEIGHT);
    }

    public void setHeight(float value) {
        set(MetadataDef.Interaction.HEIGHT, value);
    }

    public boolean getResponse() {
        return get(MetadataDef.Interaction.RESPONSIVE);
    }

    public void setResponse(boolean response) {
        set(MetadataDef.Interaction.RESPONSIVE, response);
    }
}
