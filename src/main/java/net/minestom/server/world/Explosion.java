package net.minestom.server.world;

import net.minestom.server.block.Block;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Abstract explosion.
 * World can provide a supplier through {@link World#setExplosionSupplier}
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
     *
     * @param world World to perform this explosion in
     * @return list of blocks that will be broken.
     */
    protected abstract List<BlockPosition> prepare(World world);

    /**
     * Performs the explosion and send the corresponding packet
     *
     * @param world World to perform this explosion in
     */
    public void apply(@NotNull World world) {
        List<BlockPosition> blocks = prepare(world);
        ExplosionPacket packet = new ExplosionPacket();
        packet.x = getCenterX();
        packet.y = getCenterY();
        packet.z = getCenterZ();
        packet.radius = getStrength();
        packet.playerMotionX = 0.0f; // TODO: figure out why this is here
        packet.playerMotionY = 0.0f; // TODO: figure out why this is here
        packet.playerMotionZ = 0.0f; // TODO: figure out why this is here

        packet.records = new byte[3 * blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            final BlockPosition pos = blocks.get(i);
            world.setBlock(pos, Block.AIR);
            final byte x = (byte) (pos.getX() - Math.floor(getCenterX()));
            final byte y = (byte) (pos.getY() - Math.floor(getCenterY()));
            final byte z = (byte) (pos.getZ() - Math.floor(getCenterZ()));
            packet.records[i * 3 + 0] = x;
            packet.records[i * 3 + 1] = y;
            packet.records[i * 3 + 2] = z;
        }

        postExplosion(world, blocks, packet);

        // TODO send only to close players
        PacketUtils.sendGroupedPacket(world.getPlayers(), packet);

        postSend(world, blocks);
    }

    /**
     * Called after removing blocks and preparing the packet, but before sending it.
     *
     * @param world    the World in which the explosion occurs
     * @param blocks   the block positions returned by prepare
     * @param packet   the explosion packet to sent to the client. Be careful with what you're doing.
     *                 It is initialized with the center and radius of the explosion. The positions in 'blocks' are also
     *                 stored in the packet before this call, but you are free to modify 'records' to modify the blocks sent to the client.
     *                 Just be careful, you might just crash the server or the client. Or you're lucky, both at the same time.
     */
    protected void postExplosion(World world, List<BlockPosition> blocks, ExplosionPacket packet) {
    }

    /**
     * Called after sending the explosion packet. Can be used to (re)set blocks that have been destroyed.
     * This is necessary to do after the packet being sent, because the client sets the positions received to air.
     *
     * @param world    the World in which the explosion occurs
     * @param blocks   the block positions returned by prepare
     */
    protected void postSend(World world, List<BlockPosition> blocks) {
    }
}
