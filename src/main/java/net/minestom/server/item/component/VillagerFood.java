package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.validate.Check;

/**
 * Marks an item as edible by a villager.
 *
 * @param nutrition how much the item feeds a villager, must be positive
 */
public record VillagerFood(int nutrition) {
    public static final NetworkBuffer.Type<VillagerFood> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, VillagerFood::nutrition,
            VillagerFood::new);
    public static final Codec<VillagerFood> CODEC = StructCodec.struct(
            "nutrition", Codec.INT, VillagerFood::nutrition,
            VillagerFood::new);

    public VillagerFood {
        Check.argCondition(nutrition <= 0, "Nutrition must be positive");
    }
}
