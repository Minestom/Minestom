package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import static net.minestom.server.network.NetworkBuffer.*;

public record Food(int nutrition, float saturationModifier, boolean canAlwaysEat) {

    public static final NetworkBuffer.Type<Food> NETWORK_TYPE = NetworkBufferTemplate.template(
            VAR_INT, Food::nutrition,
            FLOAT, Food::saturationModifier,
            BOOLEAN, Food::canAlwaysEat,
            Food::new);
    public static final Codec<Food> CODEC = StructCodec.struct(
            "nutrition", Codec.INT, Food::nutrition,
            "saturation", Codec.FLOAT, Food::saturationModifier,
            "can_always_eat", Codec.BOOLEAN.optional(false), Food::canAlwaysEat,
            Food::new
    );

}
