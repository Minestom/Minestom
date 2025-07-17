package net.minestom.server.entity.metadata.other;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class FallingBlockMeta extends EntityMeta implements ObjectDataProvider {
    private Block block = Block.STONE;

    public FallingBlockMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Point getSpawnPosition() {
        return metadata.get(MetadataDef.FallingBlock.SPAWN_POSITION);
    }

    public void setSpawnPosition(Point value) {
        metadata.set(MetadataDef.FallingBlock.SPAWN_POSITION, value);
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
