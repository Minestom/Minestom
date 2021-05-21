package net.minestom.server.instance.block;

import net.minestom.server.item.Material;
import net.minestom.server.map.MapColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockData {
    // Block properties

    /**
     * Gets the blast (explosion) resistance of a {@link Block}
     * @return a double denoting the blast resistance.
     */
    double getExplosionResistance();

    /**
     * Gets the corresponding {@link Material} of a {@link Block}
     * @return the corresponding {@link Material} or null if not applicable.
     */
    @Nullable Material getCorrespondingItem();

    /**
     * Gets the friction value of a {@link Block}
     * @return a double denoting the friction.
     */
    double getFriction();

    /**
     * Gets the speed factor of a {@link Block}
     * @return a double denoting the speed factor.
     */
    double getSpeedFactor();
    /**
     * Gets the jump factor of a {@link Block}
     * @return a double denoting the jump factor.
     */
    double getJumpFactor();

    /**
     * Checks if a {@link Block} is a block entity.
     * @return a boolean, true when a Block is a block entity, false otherwise.
     */
    boolean isBlockEntity();

    // State properties

    /**
     * Gets the hardness (destroy speed) of a {@link Block}.
     * @return a double denoting the hardness.
     */
    double getHardness();

    /**
     * Gets the light level emitted by a {@link Block}
     * @return an int representing the light emission.
     */
    int getLightEmission();

    /**
     * Checks if a {@link Block} is occluding.
     * @return a boolean, true if a Block is occluding, false otherwise.
     */
    boolean isOccluding();

    /**
     * Gets the piston push reaction of a {@link Block}
     * @return a {@link String} containing the push reaction of a Block.
     */
    String getPushReaction(); // TODO: Dedicated object?
    /**
     * Checks if a {@link Block} is blocking motion
     * @return a boolean, true if a Block is blocking motion, false otherwise.
     */
    boolean isBlockingMotion();
    /**
     * Checks if a {@link Block} is flammable.
     * @return a boolean, true if a Block is flammable, false otherwise.
     */
    boolean isFlammable();
    /**
     * Checks if a {@link Block} is an instance of air.
     * @return a boolean, true if a Block is air, false otherwise.
     */
    boolean isAir();
    /**
     * Checks if a {@link Block} is an instance of a fluid.
     * @return a boolean, true if a Block is a liquid, false otherwise.
     */
    boolean isLiquid();
    /**
     * Checks if a {@link Block} is replaceable.
     * @return a boolean, true if a Block is replaceable, false otherwise.
     */
    boolean isReplaceable();
    /**
     * Checks if a {@link Block} is solid.
     * @return a boolean, true if a Block is solid, false otherwise.
     */
    boolean isSolid();
    /**
     * Checks if a {@link Block} is solid and blocking.
     * @return a boolean, true if a Block is solid and blocking, false otherwise.
     */
    boolean isSolidBlocking();
    /**
     * Gets the corresponding {@link MapColor} of a {@link Block}
     * @return the corresponding {@link MapColor}.
     */
    @NotNull MapColor getMapColor();
    /**
     * Gets the piston bounding box of a {@link Block}
     * @return a {@link String} containing the bounding box of a Block.
     */
    String getBoundingBox(); // TODO: Dedicated object?
}
