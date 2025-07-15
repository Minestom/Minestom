package net.minestom.server.entity.attribute;

import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jspecify.annotations.Nullable;

public enum AttributeOperation {
    ADD_VALUE(0),
    ADD_MULTIPLIED_BASE(1),
    ADD_MULTIPLIED_TOTAL(2);

    public static final NetworkBuffer.Type<AttributeOperation> NETWORK_TYPE = NetworkBuffer.Enum(AttributeOperation.class);
    public static final Codec<AttributeOperation> CODEC = Codec.Enum(AttributeOperation.class);

    private static final AttributeOperation[] VALUES = new AttributeOperation[]{ADD_VALUE, ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL};
    private final int id;

    AttributeOperation(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static @Nullable AttributeOperation fromId(int id) {
        if (id >= 0 && id < VALUES.length) {
            return VALUES[id];
        }
        return null;
    }
}
