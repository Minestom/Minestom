package net.minestom.server.entity;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.ObjectArray;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTEnd;
import space.vectrix.flare.fastutil.Short2ObjectSyncMap;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Metadata {

    // METADATA TYPES

    public static Value<Byte> Byte(byte value) {
        return new Value<>(TYPE_BYTE, value, writer -> writer.writeByte(value), BinaryReader::readByte);
    }

    public static Value<Integer> VarInt(int value) {
        return new Value<>(TYPE_VARINT, value, writer -> writer.writeVarInt(value), BinaryReader::readVarInt);
    }

    public static Value<Float> Float(float value) {
        return new Value<>(TYPE_FLOAT, value, writer -> writer.writeFloat(value), BinaryReader::readFloat);
    }

    public static Value<String> String(@NotNull String value) {
        return new Value<>(TYPE_STRING, value, writer -> writer.writeSizedString(value), BinaryReader::readSizedString);
    }

    public static Value<Component> Chat(@NotNull Component value) {
        return new Value<>(TYPE_CHAT, value, writer -> writer.writeComponent(value), BinaryReader::readComponent);
    }

    public static Value<Component> OptChat(@Nullable Component value) {
        return new Value<>(TYPE_OPTCHAT, value, writer -> {
            writer.writeBoolean(value != null);
            if (value != null) writer.writeComponent(value);
        }, reader -> reader.readBoolean() ? reader.readComponent() : null);
    }

    public static Value<ItemStack> Slot(@NotNull ItemStack value) {
        return new Value<>(TYPE_SLOT, value, writer -> writer.writeItemStack(value), BinaryReader::readItemStack);
    }

    public static Value<Boolean> Boolean(boolean value) {
        return new Value<>(TYPE_BOOLEAN, value, writer -> writer.writeBoolean(value), BinaryReader::readBoolean);
    }

    public static Value<Point> Rotation(@NotNull Point value) {
        return new Value<>(TYPE_ROTATION, value, writer -> {
            writer.writeFloat((float) value.x());
            writer.writeFloat((float) value.y());
            writer.writeFloat((float) value.z());
        }, reader -> new Vec(reader.readFloat(), reader.readFloat(), reader.readFloat()));
    }

    public static Value<Point> Position(@NotNull Point value) {
        return new Value<>(TYPE_POSITION, value, writer -> writer.writeBlockPosition(value), BinaryReader::readBlockPosition);
    }

    public static Value<Point> OptPosition(@Nullable Point value) {
        return new Value<>(TYPE_OPTPOSITION, value, writer -> {
            writer.writeBoolean(value != null);
            if (value != null) writer.writeBlockPosition(value);
        }, reader -> reader.readBoolean() ? reader.readBlockPosition() : null);
    }

    public static Value<Direction> Direction(@NotNull Direction value) {
        return new Value<>(TYPE_DIRECTION, value,
                writer -> writer.writeVarInt(value.ordinal()),
                reader -> Direction.values()[reader.readVarInt()]);
    }

    public static Value<UUID> OptUUID(@Nullable UUID value) {
        return new Value<>(TYPE_OPTUUID, value, writer -> {
            writer.writeBoolean(value != null);
            if (value != null) writer.writeUuid(value);
        }, reader -> reader.readBoolean() ? reader.readUuid() : null);
    }

    public static Value<Integer> OptBlockID(@Nullable Integer value) {
        return new Value<>(TYPE_OPTBLOCKID, value,
                writer -> writer.writeVarInt(value != null ? value : 0),
                reader -> reader.readBoolean() ? reader.readVarInt() : null);
    }

    public static Value<NBT> NBT(@NotNull NBT nbt) {
        return new Value<>(TYPE_NBT, nbt, writer -> writer.writeNBT("", nbt), BinaryReader::readTag);
    }

    public static Value<int[]> VillagerData(int villagerType,
                                            int villagerProfession,
                                            int level) {
        return new Value<>(TYPE_VILLAGERDATA, new int[]{villagerType, villagerProfession, level}, writer -> {
            writer.writeVarInt(villagerType);
            writer.writeVarInt(villagerProfession);
            writer.writeVarInt(level);
        }, reader -> new int[]{reader.readVarInt(), reader.readVarInt(), reader.readVarInt()});
    }

    public static Value<Integer> OptVarInt(@Nullable Integer value) {
        return new Value<>(TYPE_OPTVARINT,
                value, writer -> writer.writeVarInt(value != null ? value + 1 : 0),
                reader -> reader.readBoolean() ? reader.readVarInt() : null);
    }

    public static Value<Entity.Pose> Pose(@NotNull Entity.Pose value) {
        return new Value<>(TYPE_POSE, value,
                writer -> writer.writeVarInt(value.ordinal()),
                reader -> Entity.Pose.values()[reader.readVarInt()]);
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

    private static final ObjectArray<Value<?>> EMPTY_VALUES = new ObjectArray<>(20);

    static {
        EMPTY_VALUES.set(TYPE_BYTE, Byte((byte) 0));
        EMPTY_VALUES.set(TYPE_VARINT, VarInt(0));
        EMPTY_VALUES.set(TYPE_FLOAT, Float(0f));
        EMPTY_VALUES.set(TYPE_STRING, String(""));
        EMPTY_VALUES.set(TYPE_CHAT, Chat(Component.empty()));
        EMPTY_VALUES.set(TYPE_OPTCHAT, OptChat(null));
        EMPTY_VALUES.set(TYPE_SLOT, Slot(ItemStack.AIR));
        EMPTY_VALUES.set(TYPE_BOOLEAN, Boolean(false));
        EMPTY_VALUES.set(TYPE_ROTATION, Rotation(Vec.ZERO));
        EMPTY_VALUES.set(TYPE_POSITION, Position(Vec.ZERO));
        EMPTY_VALUES.set(TYPE_OPTPOSITION, OptPosition(null));
        EMPTY_VALUES.set(TYPE_DIRECTION, Direction(Direction.DOWN));
        EMPTY_VALUES.set(TYPE_OPTUUID, OptUUID(null));
        EMPTY_VALUES.set(TYPE_OPTBLOCKID, OptBlockID(null));
        EMPTY_VALUES.set(TYPE_NBT, NBT(NBTEnd.INSTANCE));
        //EMPTY_VALUES.set(TYPE_PARTICLE -> throw new UnsupportedOperationException();
        EMPTY_VALUES.set(TYPE_VILLAGERDATA, VillagerData(0, 0, 0));
        EMPTY_VALUES.set(TYPE_OPTVARINT, OptVarInt(null));
        EMPTY_VALUES.set(TYPE_POSE, Pose(Entity.Pose.STANDING));

        EMPTY_VALUES.trim();
    }

    private final Entity entity;
    private final Short2ObjectSyncMap<Entry<?>> metadataMap = Short2ObjectSyncMap.hashmap();

    private volatile boolean notifyAboutChanges = true;
    private final Map<Byte, Entry<?>> notNotifiedChanges = new HashMap<>();

    public Metadata(@Nullable Entity entity) {
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    public <T> T getIndex(int index, @Nullable T defaultValue) {
        Entry<?> entry = this.metadataMap.get((byte) index);
        return entry != null ? (T) entry.value().content : defaultValue;
    }

    public void setIndex(int index, @NotNull Value<?> value) {
        final Entry<?> entry = new Entry<>((byte) index, value);
        this.metadataMap.put((short) index, entry);

        // Send metadata packet to update viewers and self
        if (this.entity != null && this.entity.isActive()) {
            if (!this.notifyAboutChanges) {
                synchronized (this.notNotifiedChanges) {
                    this.notNotifiedChanges.put((byte) index, entry);
                }
                return;
            }
            this.entity.sendPacketToViewersAndSelf(new EntityMetaDataPacket(entity.getEntityId(), Collections.singleton(entry)));
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
        this.entity.sendPacketToViewersAndSelf(new EntityMetaDataPacket(entity.getEntityId(), entries));
    }

    public @NotNull Collection<Entry<?>> getEntries() {
        return metadataMap.values();
    }

    public record Entry<T>(byte index, @NotNull Value<T> value) implements Writeable {
        public Entry(BinaryReader reader) {
            this(reader.readByte(), readValue(reader));
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeByte(index);
            writer.write(value);
        }

        private static <T> Value<T> readValue(BinaryReader reader) {
            final int type = reader.readVarInt();
            final Value<?> value = EMPTY_VALUES.get(type);
            if (value == null)
                throw new UnsupportedOperationException("Unknown value type: " + type);
            //noinspection unchecked
            return (Value<T>) value.withValue(reader);
        }
    }

    public record Value<T>(int type, @UnknownNullability T content,
                           @NotNull Consumer<BinaryWriter> writer,
                           @NotNull Function<BinaryReader, T> reader) implements Writeable {
        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(type);
            this.writer.accept(writer);
        }

        private Value<T> withValue(@NotNull BinaryReader reader) {
            return new Value<>(type, this.reader.apply(reader), writer, this.reader);
        }
    }
}
