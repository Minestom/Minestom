package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

public class ExperienceOrbMeta extends EntityMeta {

    private int count = 1;

    public ExperienceOrbMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public int getCount() {
        return count;
    }

    /**
     * Sets count of orbs.
     * This is possible only before spawn packet is sent.
     *
     * @param count count of orbs.
     */
    public void setCount(int count) {
        this.count = count;
    }
}
