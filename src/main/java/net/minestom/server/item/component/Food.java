package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;

import static net.minestom.server.network.NetworkBuffer.*;

public record Food(int nutrition, float saturationModifier, boolean canAlwaysEat) {

    public static final NetworkBuffer.Type<Food> NETWORK_TYPE = NetworkBufferTemplate.template(
            VAR_INT, Food::nutrition,
            FLOAT, Food::saturationModifier,
            BOOLEAN, Food::canAlwaysEat,
            Food::new);
    public static final BinaryTagSerializer<Food> NBT_TYPE = BinaryTagTemplate.object(
            "nutrition", BinaryTagSerializer.INT, Food::nutrition,
            "saturation", BinaryTagSerializer.FLOAT, Food::saturationModifier,
            "can_always_eat", BinaryTagSerializer.BOOLEAN.optional(false), Food::canAlwaysEat,
            Food::new
    );

}
