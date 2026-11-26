package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.loot.number.ResolvableFloat;
import net.minestom.server.loot.number.ResolvableInt;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

/**
 * Marks an item as usable as furnace fuel.
 *
 * @param burnTime        how many ticks the item burns for, a number or the key of a provider that computes it
 * @param speedMultiplier how much faster than normal the furnace cooks while burning the item, a number or the
 *                        key of a provider that computes it
 */
public record CookingFuel(ResolvableInt burnTime, ResolvableFloat speedMultiplier) {
    public static final NetworkBuffer.Type<CookingFuel> NETWORK_TYPE = NetworkBufferTemplate.template(
            ResolvableInt.NETWORK_TYPE, CookingFuel::burnTime,
            ResolvableFloat.NETWORK_TYPE, CookingFuel::speedMultiplier,
            CookingFuel::new);
    public static final Codec<CookingFuel> CODEC = StructCodec.struct(
            "burn_time", ResolvableInt.CODEC, CookingFuel::burnTime,
            "speed_multiplier", ResolvableFloat.CODEC, CookingFuel::speedMultiplier,
            CookingFuel::new);

    /**
     * Creates a CookingFuel with fixed values.
     *
     * @param burnTime        how many ticks the item burns for
     * @param speedMultiplier how much faster than normal the furnace cooks while burning the item
     */
    public CookingFuel(int burnTime, float speedMultiplier) {
        this(ResolvableInt.of(burnTime), ResolvableFloat.of(speedMultiplier));
    }
}
