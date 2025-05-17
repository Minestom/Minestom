package net.minestom.server.entity;

import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.animal.*;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.entity.metadata.animal.tameable.WolfSoundVariant;
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
        final ObjectArray<Metadata.Entry<?>> emptyValues = ObjectArray.singleThread(NEXT_ID.get());
        emptyValues.set(TYPE_BYTE, Byte((byte) 0));
        emptyValues.set(TYPE_VARINT, VarInt(0));
        emptyValues.set(TYPE_LONG, VarLong(0L));
        emptyValues.set(TYPE_FLOAT, Float(0f));
        emptyValues.set(TYPE_STRING, String(""));
        emptyValues.set(TYPE_CHAT, Chat(Component.empty()));
        emptyValues.set(TYPE_OPT_CHAT, OptChat(null));
        emptyValues.set(TYPE_ITEM_STACK, ItemStack(ItemStack.AIR));
        emptyValues.set(TYPE_BOOLEAN, Boolean(false));
        emptyValues.set(TYPE_ROTATION, Rotation(Vec.ZERO));
        emptyValues.set(TYPE_BLOCK_POSITION, BlockPosition(Vec.ZERO));
        emptyValues.set(TYPE_OPT_BLOCK_POSITION, OptBlockPosition(null));
        emptyValues.set(TYPE_DIRECTION, Direction(Direction.DOWN));
        emptyValues.set(TYPE_OPT_UUID, OptUUID(null));
        emptyValues.set(TYPE_BLOCKSTATE, BlockState(Block.AIR));
        emptyValues.set(TYPE_OPT_BLOCKSTATE, OptBlockState(null));
        emptyValues.set(TYPE_NBT, NBT(EndBinaryTag.endBinaryTag()));
        emptyValues.set(TYPE_PARTICLE, Particle(Particle.DUST));
        emptyValues.set(TYPE_PARTICLE_LIST, ParticleList(List.of()));
        emptyValues.set(TYPE_VILLAGERDATA, VillagerData(VillagerMeta.VillagerData.DEFAULT));
        emptyValues.set(TYPE_OPT_VARINT, OptVarInt(null));
        emptyValues.set(TYPE_POSE, Pose(EntityPose.STANDING));
        emptyValues.set(TYPE_CAT_VARIANT, CatVariant(CatMeta.Variant.TABBY));
        emptyValues.set(TYPE_COW_VARIANT, CowVariant(CowVariant.TEMPERATE));
        emptyValues.set(TYPE_WOLF_VARIANT, WolfVariant(WolfVariant.PALE));
        emptyValues.set(TYPE_WOLF_SOUND_VARIANT, WolfSoundVariant(WolfSoundVariant.CLASSIC));
        emptyValues.set(TYPE_FROG_VARIANT, FrogVariant(FrogMeta.Variant.TEMPERATE));
        emptyValues.set(TYPE_PIG_VARIANT, PigVariant(PigVariant.TEMPERATE));
        emptyValues.set(TYPE_CHICKEN_VARIANT, ChickenVariant(ChickenVariant.TEMPERATE));
        emptyValues.set(TYPE_OPT_GLOBAL_POSITION, OptionalWorldPos(null));
        emptyValues.set(TYPE_PAINTING_VARIANT, PaintingVariant(new Holder.Reference<>(PaintingVariant.KEBAB)));
        emptyValues.set(TYPE_SNIFFER_STATE, SnifferState(SnifferMeta.State.IDLING));
        emptyValues.set(TYPE_ARMADILLO_STATE, ArmadilloState(ArmadilloMeta.State.IDLE));
        emptyValues.set(TYPE_VECTOR3, Vector3(Vec.ZERO));
        emptyValues.set(TYPE_QUATERNION, Quaternion(new float[]{0, 0, 0, 0}));
        EMPTY_VALUES = emptyValues.toList();
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
