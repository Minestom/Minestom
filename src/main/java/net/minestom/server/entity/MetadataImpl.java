package net.minestom.server.entity;

import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.animal.ArmadilloMeta;
import net.minestom.server.entity.metadata.animal.FrogMeta;
import net.minestom.server.entity.metadata.animal.SnifferMeta;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfVariant;
import net.minestom.server.entity.metadata.other.PaintingVariant;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.Holder;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

import static net.minestom.server.entity.Metadata.*;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

final class MetadataImpl {
    private static final List<Metadata.Entry<?>> EMPTY_VALUES;

    static {
        final ObjectArray<Metadata.Entry<?>> _EMPTY_VALUES = ObjectArray.singleThread(30);
        _EMPTY_VALUES.set(TYPE_BYTE, Byte((byte) 0));
        _EMPTY_VALUES.set(TYPE_VARINT, VarInt(0));
        _EMPTY_VALUES.set(TYPE_LONG, VarLong(0L));
        _EMPTY_VALUES.set(TYPE_FLOAT, Float(0f));
        _EMPTY_VALUES.set(TYPE_STRING, String(""));
        _EMPTY_VALUES.set(TYPE_CHAT, Chat(Component.empty()));
        _EMPTY_VALUES.set(TYPE_OPT_CHAT, OptChat(null));
        _EMPTY_VALUES.set(TYPE_ITEM_STACK, ItemStack(ItemStack.AIR));
        _EMPTY_VALUES.set(TYPE_BOOLEAN, Boolean(false));
        _EMPTY_VALUES.set(TYPE_ROTATION, Rotation(Vec.ZERO));
        _EMPTY_VALUES.set(TYPE_BLOCK_POSITION, BlockPosition(Vec.ZERO));
        _EMPTY_VALUES.set(TYPE_OPT_BLOCK_POSITION, OptBlockPosition(null));
        _EMPTY_VALUES.set(TYPE_DIRECTION, Direction(Direction.DOWN));
        _EMPTY_VALUES.set(TYPE_OPT_UUID, OptUUID(null));
        _EMPTY_VALUES.set(TYPE_BLOCKSTATE, BlockState(Block.AIR));
        _EMPTY_VALUES.set(TYPE_OPT_BLOCKSTATE, OptBlockState(null));
        _EMPTY_VALUES.set(TYPE_NBT, NBT(EndBinaryTag.endBinaryTag()));
        _EMPTY_VALUES.set(TYPE_PARTICLE, Particle(Particle.DUST));
        _EMPTY_VALUES.set(TYPE_PARTICLE_LIST, ParticleList(List.of()));
        _EMPTY_VALUES.set(TYPE_VILLAGERDATA, VillagerData(VillagerMeta.VillagerData.DEFAULT));
        _EMPTY_VALUES.set(TYPE_OPT_VARINT, OptVarInt(null));
        _EMPTY_VALUES.set(TYPE_POSE, Pose(EntityPose.STANDING));
        _EMPTY_VALUES.set(TYPE_CAT_VARIANT, CatVariant(CatMeta.Variant.TABBY));
        _EMPTY_VALUES.set(TYPE_WOLF_VARIANT, WolfVariant(WolfVariant.PALE));
        _EMPTY_VALUES.set(TYPE_FROG_VARIANT, FrogVariant(FrogMeta.Variant.TEMPERATE));
        _EMPTY_VALUES.set(TYPE_OPT_GLOBAL_POSITION, OptionalWorldPos(null));
        _EMPTY_VALUES.set(TYPE_PAINTING_VARIANT, PaintingVariant(new Holder.Reference<>(PaintingVariant.KEBAB)));
        _EMPTY_VALUES.set(TYPE_SNIFFER_STATE, SnifferState(SnifferMeta.State.IDLING));
        _EMPTY_VALUES.set(TYPE_ARMADILLO_STATE, ArmadilloState(ArmadilloMeta.State.IDLE));
        _EMPTY_VALUES.set(TYPE_VECTOR3, Vector3(Vec.ZERO));
        _EMPTY_VALUES.set(TYPE_QUATERNION, Quaternion(new float[]{0, 0, 0, 0}));
        EMPTY_VALUES = _EMPTY_VALUES.toList();
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
