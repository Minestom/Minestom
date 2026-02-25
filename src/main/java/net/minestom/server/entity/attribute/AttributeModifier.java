package net.minestom.server.entity.attribute;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

/**
 * Represent an attribute modifier.
 */
public record AttributeModifier(Key id, double amount, AttributeOperation operation) {
    public static final NetworkBuffer.Type<AttributeModifier> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.KEY, AttributeModifier::id,
            NetworkBuffer.DOUBLE, AttributeModifier::amount,
            AttributeOperation.NETWORK_TYPE, AttributeModifier::operation,
            AttributeModifier::new);
    public static final Codec<AttributeModifier> CODEC = StructCodec.struct(
            "id", Codec.KEY, AttributeModifier::id,
            "amount", Codec.DOUBLE, AttributeModifier::amount,
            "operation", AttributeOperation.CODEC, AttributeModifier::operation,
            AttributeModifier::new);

    /**
     * Creates a new modifier with a random id.
     *
     * @param id        the (namespace) id of this modifier
     * @param amount    the value of this modifier
     * @param operation the operation to apply this modifier with
     */
    public AttributeModifier(@KeyPattern String id, double amount, AttributeOperation operation) {
        this(Key.key(id), amount, operation);
    }

}
