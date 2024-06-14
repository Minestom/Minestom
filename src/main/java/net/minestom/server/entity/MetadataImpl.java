package net.minestom.server.entity;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.animal.FrogMeta;
import net.minestom.server.entity.metadata.animal.SnifferMeta;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBTEnd;

import static net.minestom.server.entity.Metadata.*;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

final class MetadataImpl {
    static final ObjectArray<Metadata.Entry<?>> EMPTY_VALUES = ObjectArray.singleThread(20);

    static {
        EMPTY_VALUES.set(TYPE_BYTE, Byte((byte) 0));
        EMPTY_VALUES.set(TYPE_VARINT, VarInt(0));
        EMPTY_VALUES.set(TYPE_LONG, Long(0L));
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
        EMPTY_VALUES.set(TYPE_BLOCKSTATE, BlockState(Block.AIR.id()));
        EMPTY_VALUES.set(TYPE_OPTBLOCKSTATE, OptBlockState(null));
        EMPTY_VALUES.set(TYPE_NBT, NBT(NBTEnd.INSTANCE));
        //EMPTY_VALUES.set(TYPE_PARTICLE -> throw new UnsupportedOperationException();
        EMPTY_VALUES.set(TYPE_VILLAGERDATA, VillagerData(0, 0, 0));
        EMPTY_VALUES.set(TYPE_OPTVARINT, OptVarInt(null));
        EMPTY_VALUES.set(TYPE_POSE, Pose(Entity.Pose.STANDING));
        EMPTY_VALUES.set(TYPE_CAT_VARIANT, CatVariant(CatMeta.Variant.TABBY));
        EMPTY_VALUES.set(TYPE_FROG_VARIANT, FrogVariant(FrogMeta.Variant.TEMPERATE));
        // OptGlobalPos
        EMPTY_VALUES.set(TYPE_PAINTINGVARIANT, PaintingVariant(PaintingMeta.Variant.KEBAB));
        EMPTY_VALUES.set(TYPE_SNIFFER_STATE, SnifferState(SnifferMeta.State.IDLING));
        EMPTY_VALUES.set(TYPE_VECTOR3, Vector3(Vec.ZERO));
        EMPTY_VALUES.set(TYPE_QUATERNION, Quaternion(new float[]{0, 0, 0, 0}));
        EMPTY_VALUES.trim();
    }

    record EntryImpl<T>(int type, @UnknownNullability T value,
                        @NotNull NetworkBuffer.Type<T> serializer) implements Metadata.Entry<T> {
        static Entry<?> read(int type, @NotNull NetworkBuffer reader) {
            final EntryImpl<?> value = (EntryImpl<?>) EMPTY_VALUES.get(type);
            if (value == null) throw new UnsupportedOperationException("Unknown value type: " + type);
            return value.withValue(reader);
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(VAR_INT, type);
            writer.write(serializer, value);
        }

        private EntryImpl<T> withValue(@NotNull NetworkBuffer reader) {
            return new EntryImpl<>(type, reader.read(serializer), serializer);
        }
    }
}
