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
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Map;

import static net.minestom.server.entity.Metadata.*;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

final class MetadataImpl {
    private static final Map<Byte, Metadata.Entry<?>> EMPTY_VALUES = Map.ofEntries(
        Map.entry(TYPE_BYTE, Byte((byte) 0)),
        Map.entry(TYPE_VARINT, VarInt(0)),
        Map.entry(TYPE_LONG, VarLong(0L)),
        Map.entry(TYPE_FLOAT, Float(0f)),
        Map.entry(TYPE_STRING, String("")),
        Map.entry(TYPE_CHAT, Chat(Component.empty())),
        Map.entry(TYPE_OPT_CHAT, OptChat(null)),
        Map.entry(TYPE_ITEM_STACK, ItemStack(ItemStack.AIR)),
        Map.entry(TYPE_BOOLEAN, Boolean(false)),
        Map.entry(TYPE_ROTATION, Rotation(Vec.ZERO)),
        Map.entry(TYPE_BLOCK_POSITION, BlockPosition(Vec.ZERO)),
        Map.entry(TYPE_OPT_BLOCK_POSITION, OptBlockPosition(null)),
        Map.entry(TYPE_DIRECTION, Direction(Direction.DOWN)),
        Map.entry(TYPE_OPT_UUID, OptUUID(null)),
        Map.entry(TYPE_BLOCKSTATE, BlockState(Block.AIR)),
        Map.entry(TYPE_OPT_BLOCKSTATE, OptBlockState(null)),
        Map.entry(TYPE_NBT, NBT(EndBinaryTag.endBinaryTag())),
        Map.entry(TYPE_PARTICLE, Particle(Particle.DUST)),
        Map.entry(TYPE_PARTICLE_LIST, ParticleList(List.of())),
        Map.entry(TYPE_VILLAGERDATA, VillagerData(VillagerMeta.VillagerData.DEFAULT)),
        Map.entry(TYPE_OPT_VARINT, OptVarInt(null)),
        Map.entry(TYPE_POSE, Pose(EntityPose.STANDING)),
        Map.entry(TYPE_CAT_VARIANT, CatVariant(CatMeta.Variant.TABBY)),
        Map.entry(TYPE_WOLF_VARIANT, WolfVariant(WolfVariant.PALE)),
        Map.entry(TYPE_FROG_VARIANT, FrogVariant(FrogMeta.Variant.TEMPERATE)),
        // OptGlobalPos
        Map.entry(TYPE_PAINTING_VARIANT, PaintingVariant(new Holder.Reference<>(PaintingVariant.KEBAB))),
        Map.entry(TYPE_SNIFFER_STATE, SnifferState(SnifferMeta.State.IDLING)),
        Map.entry(TYPE_ARMADILLO_STATE, ArmadilloState(ArmadilloMeta.State.IDLE)),
        Map.entry(TYPE_VECTOR3, Vector3(Vec.ZERO)),
        Map.entry(TYPE_QUATERNION, Quaternion(new float[]{0, 0, 0, 0}))
    );

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
                final EntryImpl<?> value = (EntryImpl<?>) EMPTY_VALUES.get((byte) type);
                if (value == null) throw new UnsupportedOperationException("Unknown value type: " + type);
                return new EntryImpl(type, value.serializer.read(buffer), value.serializer);
            }
        };
    }
}
