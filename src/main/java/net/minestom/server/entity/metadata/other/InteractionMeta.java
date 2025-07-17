package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InteractionMeta extends EntityMeta {
    public InteractionMeta(@Nullable Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public float getWidth() {
        return metadata.get(MetadataDef.Interaction.WIDTH);
    }

    public void setWidth(float value) {
        metadata.set(MetadataDef.Interaction.WIDTH, value);
    }

    public float getHeight() {
        return metadata.get(MetadataDef.Interaction.HEIGHT);
    }

    public void setHeight(float value) {
        metadata.set(MetadataDef.Interaction.HEIGHT, value);
    }

    public boolean getResponse() {
        return metadata.get(MetadataDef.Interaction.RESPONSIVE);
    }

    public void setResponse(boolean response) {
        metadata.set(MetadataDef.Interaction.RESPONSIVE, response);
    }
}
