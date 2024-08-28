package net.minestom.server.entity;

import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.animal.ArmadilloMeta;
import net.minestom.server.entity.metadata.animal.FrogMeta;
import net.minestom.server.entity.metadata.animal.SnifferMeta;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfMeta;
import net.minestom.server.entity.metadata.other.PaintingMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

import static net.minestom.server.entity.Metadata.*;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

final class MetadataImpl {
    static final ObjectArray<Metadata.Entry<?>> EMPTY_VALUES = ObjectArray.singleThread(20);

    static {
        EMPTY_VALUES.set(TYPE_BYTE, Byte((byte) 0));
        EMPTY_VALUES.set(TYPE_VARINT, VarInt(0));
        EMPTY_VALUES.set(TYPE_LONG, VarLong(0L));
        EMPTY_VALUES.set(TYPE_FLOAT, Float(0f));
        EMPTY_VALUES.set(TYPE_STRING, String(""));
        EMPTY_VALUES.set(TYPE_CHAT, Chat(Component.empty()));
        EMPTY_VALUES.set(TYPE_OPT_CHAT, OptChat(null));
        EMPTY_VALUES.set(TYPE_ITEM_STACK, ItemStack(ItemStack.AIR));
        EMPTY_VALUES.set(TYPE_BOOLEAN, Boolean(false));
        EMPTY_VALUES.set(TYPE_ROTATION, Rotation(Vec.ZERO));
        EMPTY_VALUES.set(TYPE_BLOCK_POSITION, BlockPosition(Vec.ZERO));
        EMPTY_VALUES.set(TYPE_OPT_BLOCK_POSITION, OptBlockPosition(null));
        EMPTY_VALUES.set(TYPE_DIRECTION, Direction(Direction.DOWN));
        EMPTY_VALUES.set(TYPE_OPT_UUID, OptUUID(null));
        EMPTY_VALUES.set(TYPE_BLOCKSTATE, BlockState(Block.AIR));
        EMPTY_VALUES.set(TYPE_OPT_BLOCKSTATE, OptBlockState(null));
        EMPTY_VALUES.set(TYPE_NBT, NBT(EndBinaryTag.endBinaryTag()));
        EMPTY_VALUES.set(TYPE_PARTICLE, Particle(Particle.DUST));
        EMPTY_VALUES.set(TYPE_PARTICLE_LIST, ParticleList(List.of()));
        EMPTY_VALUES.set(TYPE_VILLAGERDATA, VillagerData(0, 0, 0));
        EMPTY_VALUES.set(TYPE_OPT_VARINT, OptVarInt(null));
        EMPTY_VALUES.set(TYPE_POSE, Pose(EntityPose.STANDING));
        EMPTY_VALUES.set(TYPE_CAT_VARIANT, CatVariant(CatMeta.Variant.TABBY));
        EMPTY_VALUES.set(TYPE_WOLF_VARIANT, WolfVariant(WolfMeta.Variant.PALE));
        EMPTY_VALUES.set(TYPE_FROG_VARIANT, FrogVariant(FrogMeta.Variant.TEMPERATE));
        // OptGlobalPos
        EMPTY_VALUES.set(TYPE_PAINTING_VARIANT, PaintingVariant(PaintingMeta.Variant.KEBAB));
        EMPTY_VALUES.set(TYPE_SNIFFER_STATE, SnifferState(SnifferMeta.State.IDLING));
        EMPTY_VALUES.set(TYPE_ARMADILLO_STATE, ArmadilloState(ArmadilloMeta.State.IDLE));
        EMPTY_VALUES.set(TYPE_VECTOR3, Vector3(Vec.ZERO));
        EMPTY_VALUES.set(TYPE_QUATERNION, Quaternion(new float[]{0, 0, 0, 0}));
        EMPTY_VALUES.trim();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    record EntryImpl<T>(int type, @UnknownNullability T value,
                        @NotNull NetworkBuffer.Type<T> serializer) implements Metadata.Entry<T> {
        static final NetworkBuffer.Type<EntryImpl<?>> SERIALIZER = new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, EntryImpl value) {
                buffer.write(VAR_INT, value.type);
                buffer.write(value.serializer, value.value);
            }

            @Override
            public EntryImpl read(@NotNull NetworkBuffer buffer) {
                final int type = buffer.read(VAR_INT);
                final EntryImpl<?> value = (EntryImpl<?>) EMPTY_VALUES.get(type);
                if (value == null) throw new UnsupportedOperationException("Unknown value type: " + type);
                return new EntryImpl(type, value.serializer.read(buffer), value.serializer);
            }
        };
    }
}
