package net.minestom.server.instance;

import org.jglrxavpok.hephaistos.nbt.NBTCompound;

@FunctionalInterface
public interface ExplosionSupplier {

    /**
     * Creates a new explosion
     *
     * @param centerX        center X of the explosion
     * @param centerY        center Y of the explosion
     * @param centerZ        center Z of the explosion
     * @param strength       strength of the explosion
     * @param additionalData data passed via {@link Instance#explode(float, float, float, float, NBTCompound)} )}. Can be null
     * @return Explosion object representing the algorithm to use
     */
    Explosion createExplosion(float centerX, float centerY, float centerZ, float strength, NBTCompound additionalData);

}
