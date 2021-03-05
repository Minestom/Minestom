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
import org.jglrxavpok.hephaistos.nbt.NBT;

import java.util.*;
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
        return new Value<>(TYPE_OPTCHAT, value, writer -> {
            final boolean present = value != null;
            writer.writeBoolean(present);
            if (present) {
                writer.writeSizedString(value.toString());
            }
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
        return new Value<>(TYPE_OPTPOSITION, value, writer -> {
            final boolean present = value != null;
            writer.writeBoolean(present);
            if (present) {
                writer.writeBlockPosition(value);
            }
        });
    }

    public static Value<Direction> Direction(@NotNull Direction value) {
        return new Value<>(TYPE_DIRECTION, value, writer -> writer.writeVarInt(value.ordinal()));
    }

    public static Value<UUID> OptUUID(@Nullable UUID value) {
        return new Value<>(TYPE_OPTUUID, value, writer -> {
            final boolean present = value != null;
            writer.writeBoolean(present);
            if (present) {
                writer.writeUuid(value);
            }
        });
    }

    public static Value<Integer> OptBlockID(@Nullable Integer value) {
        return new Value<>(TYPE_OPTBLOCKID, value, writer -> {
            final boolean present = value != null;
            writer.writeVarInt(present ? value : 0);
        });
    }

    public static Value<NBT> NBT(@NotNull NBT nbt) {
        return new Value<>(TYPE_NBT, nbt, writer -> writer.writeNBT("", nbt));
    }

    public static Value<int[]> VillagerData(int villagerType,
                                            int villagerProfession,
                                            int level) {
        return new Value<>(TYPE_VILLAGERDATA, new int[]{villagerType, villagerProfession, level}, writer -> {
            writer.writeVarInt(villagerType);
            writer.writeVarInt(villagerProfession);
            writer.writeVarInt(level);
        });
    }

    public static Value<Integer> OptVarInt(@Nullable Integer value) {
        return new Value<>(TYPE_OPTVARINT, value, writer -> {
            final boolean present = value != null;
            writer.writeVarInt(present ? value + 1 : 0);
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

    private final Map<Byte, Entry<?>> metadataMap = new ConcurrentHashMap<>();

    private volatile boolean notifyAboutChanges = true;
    private final Map<Byte, Entry<?>> notNotifiedChanges = new HashMap<>();

    public Metadata(@Nullable Entity entity) {
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    public <T> T getIndex(byte index, @Nullable T defaultValue) {
        Entry<?> value = this.metadataMap.get(index);
        return value != null ? (T) value.getMetaValue().value : defaultValue;
    }

    public void setIndex(byte index, @NotNull Value<?> value) {
        final Entry<?> entry = new Entry<>(index, value);
        this.metadataMap.put(index, entry);

        // Send metadata packet to update viewers and self
        if (this.entity != null && this.entity.isActive()) {
            if (!this.notifyAboutChanges) {
                synchronized (this.notNotifiedChanges) {
                    this.notNotifiedChanges.put(index, entry);
                }
                return;
            }
            EntityMetaDataPacket metaDataPacket = new EntityMetaDataPacket();
            metaDataPacket.entityId = this.entity.getEntityId();
            metaDataPacket.entries = Collections.singleton(entry);

            this.entity.sendPacketToViewersAndSelf(metaDataPacket);
        }
    }

    public void setNotifyAboutChanges(boolean notifyAboutChanges) {
        if (this.notifyAboutChanges == notifyAboutChanges) {
            return;
        }

        Collection<Entry<?>> entries = null;
        synchronized (this.notNotifiedChanges) {
            this.notifyAboutChanges = notifyAboutChanges;
            if (notifyAboutChanges) {
                entries = this.notNotifiedChanges.values();
                if (entries.isEmpty()) {
                    return;
                }
                this.notNotifiedChanges.clear();
            }
        }

        if (entries == null || this.entity == null || !this.entity.isActive()) {
            return;
        }

        EntityMetaDataPacket metaDataPacket = new EntityMetaDataPacket();
        metaDataPacket.entityId = this.entity.getEntityId();
        metaDataPacket.entries = entries;

        this.entity.sendPacketToViewersAndSelf(metaDataPacket);
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

}
