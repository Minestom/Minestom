package net.minestom.server.entity.attribute;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.Nullable;

public enum AttributeOperation {
    ADD_VALUE(0),
    MULTIPLY_BASE(1),
    MULTIPLY_TOTAL(2);

    public static final NetworkBuffer.Type<AttributeOperation> NETWORK_TYPE = NetworkBuffer.Enum(AttributeOperation.class);
    public static final BinaryTagSerializer<AttributeOperation> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(AttributeOperation.class);

    private static final AttributeOperation[] VALUES = new AttributeOperation[]{ADD_VALUE, MULTIPLY_BASE, MULTIPLY_TOTAL};
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
