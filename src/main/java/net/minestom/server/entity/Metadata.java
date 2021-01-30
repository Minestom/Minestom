package net.minestom.server.entity;

import net.minestom.server.chat.JsonMessage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Metadata {

    // METADATA TYPES

    public static Value<Byte> Byte(byte value) {
        return new Value<>(TYPE_BYTE, value, writer -> writer.writeByte(value));
    }

    public static Value<Integer> VarInt(int value) {
        return new Value<>(TYPE_VARINT, value, writer -> writer.writeVarInt(value));
    }

    public static Value<Float> Float(float value) {
        return new Value<>(TYPE_FLOAT, value, writer -> writer.writeFloat(value));
    }

    public static Value<String> String(@NotNull String value) {
        return new Value<>(TYPE_STRING, value, writer -> writer.writeSizedString(value));
    }

    public static Value<JsonMessage> Chat(@NotNull JsonMessage value) {
        return new Value<>(TYPE_CHAT, value, writer -> writer.writeSizedString(value.toString()));
    }

    public static Value<JsonMessage> OptChat(@Nullable JsonMessage value) {
        return new OptionalValue<>(TYPE_OPTCHAT, value, writer -> {
            assert value != null;
            writer.writeSizedString(value.toString());
        });
    }

    public static Value<ItemStack> Slot(@NotNull ItemStack value) {
        return new Value<>(TYPE_SLOT, value, writer -> writer.writeItemStack(value));
    }

    public static Value<Boolean> Boolean(boolean value) {
        return new Value<>(TYPE_BOOLEAN, value, writer -> writer.writeBoolean(value));
    }

    public static Value<Vector> Rotation(@NotNull Vector value) {
        return new Value<>(TYPE_ROTATION, value, writer -> {
            writer.writeFloat((float) value.getX());
            writer.writeFloat((float) value.getY());
            writer.writeFloat((float) value.getZ());
        });
    }

    public static Value<BlockPosition> Position(@NotNull BlockPosition value) {
        return new Value<>(TYPE_POSITION, value, writer -> writer.writeBlockPosition(value));
    }

    public static Value<BlockPosition> OptPosition(@Nullable BlockPosition value) {
        return new OptionalValue<>(TYPE_OPTPOSITION, value, writer -> {
            assert value != null;
            writer.writeBlockPosition(value);
        });
    }

    public static Value<Direction> Direction(@NotNull Direction value) {
        return new Value<>(TYPE_DIRECTION, value, writer -> writer.writeVarInt(value.ordinal()));
    }

    public static Value<UUID> OptUUID(@Nullable UUID value) {
        return new OptionalValue<>(TYPE_OPTUUID, value, writer -> {
            assert value != null;
            writer.writeUuid(value);
        });
    }

    public static Value<Entity.Pose> Pose(@NotNull Entity.Pose value) {
        return new Value<>(TYPE_POSE, value, writer -> writer.writeVarInt(value.ordinal()));
    }

    public static final byte TYPE_BYTE = 0;
    public static final byte TYPE_VARINT = 1;
    public static final byte TYPE_FLOAT = 2;
    public static final byte TYPE_STRING = 3;
    public static final byte TYPE_CHAT = 4;
    public static final byte TYPE_OPTCHAT = 5;
    public static final byte TYPE_SLOT = 6;
    public static final byte TYPE_BOOLEAN = 7;
    public static final byte TYPE_ROTATION = 8;
    public static final byte TYPE_POSITION = 9;
    public static final byte TYPE_OPTPOSITION = 10;
    public static final byte TYPE_DIRECTION = 11;
    public static final byte TYPE_OPTUUID = 12;
    public static final byte TYPE_OPTBLOCKID = 13;
    public static final byte TYPE_NBT = 14;
    public static final byte TYPE_PARTICLE = 15;
    public static final byte TYPE_VILLAGERDATA = 16;
    public static final byte TYPE_OPTVARINT = 17;
    public static final byte TYPE_POSE = 18;

    private final Entity entity;

    private Map<Byte, Entry<?>> metadataMap = new ConcurrentHashMap<>();

    public Metadata(@Nullable Entity entity) {
        this.entity = entity;
    }

    public <T> T getIndex(byte index, @Nullable T defaultValue) {
        Entry<?> value = metadataMap.get(index);
        return value != null ? (T) value.getMetaValue().value : defaultValue;
    }

    public void setIndex(byte index, @NotNull Value<?> value) {
        final Entry<?> entry = new Entry<>(index, value);
        this.metadataMap.put(index, entry);

        // Send metadata packet to update viewers and self
        if (entity != null && entity.isActive()) {
            EntityMetaDataPacket metaDataPacket = new EntityMetaDataPacket();
            metaDataPacket.entityId = entity.getEntityId();
            metaDataPacket.entries = Collections.singleton(entry);

            this.entity.sendPacketToViewersAndSelf(metaDataPacket);
        }
    }

    @NotNull
    public Collection<Entry<?>> getEntries() {
        return metadataMap.values();
    }

    public static class Entry<T> implements Writeable {

        protected final byte index;
        protected final Value<T> value;

        private Entry(byte index, @NotNull Value<T> value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeByte(index);
            this.value.write(writer);
        }

        public byte getIndex() {
            return index;
        }

        @NotNull
        public Value<T> getMetaValue() {
            return value;
        }
    }

    public static class Value<T> implements Writeable {

        protected final int type;
        protected final T value;
        protected final Consumer<BinaryWriter> valueWriter;

        private Value(int type, T value, @NotNull Consumer<BinaryWriter> valueWriter) {
            this.type = type;
            this.value = value;
            this.valueWriter = valueWriter;
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(type);
            this.valueWriter.accept(writer);
        }

        public int getType() {
            return type;
        }

        public T getValue() {
            return value;
        }
    }

    private static class OptionalValue<T> extends Value<T> {
        private OptionalValue(int type, T value, @NotNull Consumer<BinaryWriter> valueWriter) {
            super(type, value, valueWriter);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            final boolean present = value != null;
            writer.writeBoolean(present);
            if (present) {
                super.write(writer);
            }
        }
    }

}
