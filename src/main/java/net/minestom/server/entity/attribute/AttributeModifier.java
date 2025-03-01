package net.minestom.server.entity.attribute;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Represent an attribute modifier.
 */
public record AttributeModifier(@NotNull Key id, double amount, @NotNull AttributeOperation operation) {
    public static final NetworkBuffer.Type<AttributeModifier> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, AttributeModifier value) {
            buffer.write(NetworkBuffer.STRING, value.id.asString());
            buffer.write(NetworkBuffer.DOUBLE, value.amount);
            buffer.write(AttributeOperation.NETWORK_TYPE, value.operation);
        }

        @Override
        public AttributeModifier read(@NotNull NetworkBuffer buffer) {
            return new AttributeModifier(Key.key(buffer.read(NetworkBuffer.STRING)),
                    buffer.read(NetworkBuffer.DOUBLE), buffer.read(AttributeOperation.NETWORK_TYPE));
        }
    };
    public static final BinaryTagSerializer<AttributeModifier> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new AttributeModifier(Key.key(tag.getString("id")), tag.getDouble("amount"),
                    AttributeOperation.NBT_TYPE.read(tag.get("operation"))),
            value -> CompoundBinaryTag.builder()
                    .putString("id", value.id.asString())
                    .putDouble("amount", value.amount)
                    .put("operation", AttributeOperation.NBT_TYPE.write(value.operation))
                    .build()
    );

    /**
     * Creates a new modifier with a random id.
     *
     * @param id        the (namespace) id of this modifier
     * @param amount    the value of this modifier
     * @param operation the operation to apply this modifier with
     */
    public AttributeModifier(@NotNull String id, double amount, @NotNull AttributeOperation operation) {
        this(Key.key(id), amount, operation);
    }

}
