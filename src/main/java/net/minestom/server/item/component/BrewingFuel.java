package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.loot.number.ResolvableFloat;
import net.minestom.server.loot.number.ResolvableInt;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

/**
 * Marks an item as usable as brewing stand fuel.
 *
 * @param uses            how many brews the item powers, a number or the key of a provider that computes it
 * @param speedMultiplier how much faster than normal the stand brews while burning the item, a number or the
 *                        key of a provider that computes it
 */
public record BrewingFuel(ResolvableInt uses, ResolvableFloat speedMultiplier) {
    public static final NetworkBuffer.Type<BrewingFuel> NETWORK_TYPE = NetworkBufferTemplate.template(
            ResolvableInt.NETWORK_TYPE, BrewingFuel::uses,
            ResolvableFloat.NETWORK_TYPE, BrewingFuel::speedMultiplier,
            BrewingFuel::new);
    public static final Codec<BrewingFuel> CODEC = StructCodec.struct(
            "uses", ResolvableInt.CODEC, BrewingFuel::uses,
            "speed_multiplier", ResolvableFloat.CODEC, BrewingFuel::speedMultiplier,
            BrewingFuel::new);

    /**
     * Creates a BrewingFuel with fixed values.
     *
     * @param uses            how many brews the item powers
     * @param speedMultiplier how much faster than normal the stand brews while burning the item
     */
    public BrewingFuel(int uses, float speedMultiplier) {
        this(ResolvableInt.of(uses), ResolvableFloat.of(speedMultiplier));
    }
}
