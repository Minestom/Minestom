package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.Monster;
import net.minestom.server.utils.Position;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.flying.PhantomMeta} instead.
 */
@Deprecated
public class EntityPhantom extends EntityCreature implements Monster {

    public EntityPhantom(Position spawnPosition) {
        super(EntityType.PHANTOM, spawnPosition);
        setBoundingBox(0.9f, 0.5f, 0.9f); // TODO change based on size
    }

    public int getSize() {
        return metadata.getIndex((byte) 17, 0);
    }

    public void setSize(int size) {
        this.metadata.setIndex((byte) 15, Metadata.VarInt(size));
    }
}
