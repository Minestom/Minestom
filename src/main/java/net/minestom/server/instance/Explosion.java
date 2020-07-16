package net.minestom.server.instance;

import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.server.utils.BlockPosition;

import java.util.List;

/**
 * Abstract explosion.
 * Instance can provide a supplier through {@link Instance#setExplosionSupplier}
 */
public abstract class Explosion {

    private final float centerX;
    private final float centerY;
    private final float centerZ;
    private final float strength;

    public Explosion(float centerX, float centerY, float centerZ, float strength) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.strength = strength;
    }

    public float getStrength() {
        return strength;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float getCenterZ() {
        return centerZ;
    }

    /**
     * Prepares the list of blocks that will be broken. Also pushes and damage entities affected by this explosion
     * @param instance instance to perform this explosion in
     * @return list of blocks that will be broken.
     */
    protected abstract List<BlockPosition> prepare(Instance instance);

    /**
     * Performs the explosion and send the corresponding packet
     * @param instance instance to perform this explosion in
     */
    public void apply(Instance instance) {
        List<BlockPosition> blocks = prepare(instance);
        ExplosionPacket packet = new ExplosionPacket();
        packet.x = getCenterX();
        packet.y = getCenterY();
        packet.z = getCenterZ();
        packet.radius = getStrength();
        packet.playerMotionX = 0.0f; // TODO: figure out why this is here
        packet.playerMotionY = 0.0f; // TODO: figure out why this is here
        packet.playerMotionZ = 0.0f; // TODO: figure out why this is here

        packet.records = new byte[3*blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            BlockPosition pos = blocks.get(i);
            instance.setBlock(pos, Block.AIR);
            byte x = (byte) (pos.getX()-Math.floor(getCenterX()));
            byte y = (byte) (pos.getY()-Math.floor(getCenterY()));
            byte z = (byte) (pos.getZ()-Math.floor(getCenterZ()));
            packet.records[i*3+0] = x;
            packet.records[i*3+1] = y;
            packet.records[i*3+2] = z;
        }

        postExplosion(instance, blocks);

        instance.getPlayers().forEach(player -> {
            player.sendPacketToViewersAndSelf(packet);
        });
    }

    /**
     * Called after removing blocks and preparing the packet, but before sending it.
     * @param instance the instance in which the explosion occurs
     * @param blocks the block positions returned by prepare
     */
    protected void postExplosion(Instance instance, List<BlockPosition> blocks) {}
}
