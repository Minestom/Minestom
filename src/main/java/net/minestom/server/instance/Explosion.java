package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.PacketSendingUtils;
import org.jetbrains.annotations.NotNull;

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
     *
     * @param instance instance to perform this explosion in
     * @return list of blocks that will be broken.
     */
    protected abstract List<Point> prepare(Instance instance);

    /**
     * Performs the explosion and send the corresponding packet
     *
     * @param instance instance to perform this explosion in
     */
    public void apply(@NotNull Instance instance) {
        List<Point> blocks = prepare(instance);
        for (final Point pos : blocks) {
            instance.setBlock(pos, Block.AIR);
        }

        ExplosionPacket packet = new ExplosionPacket(
                new Vec(centerX, centerY, centerZ), Vec.ZERO,
                Particle.EXPLOSION, SoundEvent.ENTITY_GENERIC_EXPLODE);
        postExplosion(instance, blocks, packet);
        PacketSendingUtils.sendGroupedPacket(instance.getPlayers(), packet);

        postSend(instance, blocks);
    }

    /**
     * Called after removing blocks and preparing the packet, but before sending it.
     *
     * @param instance the instance in which the explosion occurs
     * @param blocks   the block positions returned by prepare
     * @param packet   the explosion packet to sent to the client. Be careful with what you're doing.
     *                 It is initialized with the center and radius of the explosion. The positions in 'blocks' are also
     *                 stored in the packet before this call, but you are free to modify 'records' to modify the blocks sent to the client.
     *                 Just be careful, you might just crash the server or the client. Or you're lucky, both at the same time.
     */
    protected void postExplosion(Instance instance, List<Point> blocks, ExplosionPacket packet) {
    }

    /**
     * Called after sending the explosion packet. Can be used to (re)set blocks that have been destroyed.
     * This is necessary to do after the packet being sent, because the client sets the positions received to air.
     *
     * @param instance the instance in which the explosion occurs
     * @param blocks   the block positions returned by prepare
     */
    protected void postSend(Instance instance, List<Point> blocks) {
    }
}
