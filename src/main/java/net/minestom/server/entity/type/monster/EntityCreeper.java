package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.monster.CreeperMeta} instead.
 */
@Deprecated
public class EntityCreeper extends EntityCreature implements Monster {

    public EntityCreeper(Position spawnPosition) {
        super(EntityType.CREEPER, spawnPosition);
        setBoundingBox(0.6f, 1.7f, 0.6f);
    }

    @NotNull
    public CreeperState getCreeperState() {
        final int state = metadata.getIndex((byte) 15, -1);
        return CreeperState.fromState(state);
    }

    public void setCreeperState(@NotNull CreeperState creeperState) {
        this.metadata.setIndex((byte) 15, Metadata.VarInt(creeperState.getState()));
    }

    public boolean isCharged() {
        return metadata.getIndex((byte) 16, false);
    }

    public void setCharged(boolean charged) {
        this.metadata.setIndex((byte) 16, Metadata.Boolean(charged));
    }

    public boolean isIgnited() {
        return metadata.getIndex((byte) 17, false);
    }

    public void setIgnited(boolean ignited) {
        this.metadata.setIndex((byte) 17, Metadata.Boolean(ignited));
    }

    public enum CreeperState {
        IDLE(-1),
        FUSE(1);

        private final int state;

        CreeperState(int state) {
            this.state = state;
        }

        private int getState() {
            return state;
        }

        private static CreeperState fromState(int state) {
            if (state == -1) {
                return IDLE;
            } else if (state == 1) {
                return FUSE;
            }
            throw new IllegalArgumentException("Weird thing happened");
        }
    }
}
