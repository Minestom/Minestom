package net.minestom.server.entity.metadata.other;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class FallingBlockMeta extends EntityMeta implements ObjectDataProvider {
    public static final byte OFFSET = EntityMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    private Block block = Block.STONE;

    public FallingBlockMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public Point getSpawnPosition() {
        return super.metadata.getIndex(OFFSET, Vec.ZERO);
    }

    public void setSpawnPosition(Point value) {
        super.metadata.setIndex(OFFSET, Metadata.Position(value));
    }

    @NotNull
    public Block getBlock() {
        return block;
    }

    /**
     * Sets which block to display.
     * This is possible only before spawn packet is sent.
     *
     * @param block which block to display.
     */
    public void setBlock(@NotNull Block block) {
        this.block = block;
    }

    @Override
    public int getObjectData() {
        return block.stateId();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return false;
    }
}
