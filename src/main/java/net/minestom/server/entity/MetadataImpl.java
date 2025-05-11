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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

import static net.minestom.server.entity.Metadata.*;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

final class MetadataImpl {
    private static final List<Metadata.Entry<?>> EMPTY_VALUES = List.of(
        Byte((byte) 0),
        VarInt(0),
        VarLong(0L),
        Float(0f),
        String(""),
        Chat(Component.empty()),
        OptChat(null),
        ItemStack(ItemStack.AIR),
        Boolean(false),
        Rotation(Vec.ZERO),
        BlockPosition(Vec.ZERO),
        OptBlockPosition(null),
        Direction(Direction.DOWN),
        OptUUID(null),
        BlockState(Block.AIR),
        OptBlockState(null),
        NBT(EndBinaryTag.endBinaryTag()),
        Particle(Particle.DUST),
        ParticleList(List.of()),
        VillagerData(VillagerMeta.VillagerData.DEFAULT),
        OptVarInt(null),
        Pose(EntityPose.STANDING),
        CatVariant(CatMeta.Variant.TABBY),
        WolfVariant(WolfVariant.PALE),
        FrogVariant(FrogMeta.Variant.TEMPERATE),
        OptionalWorldPosition(null),
        PaintingVariant(new Holder.Reference<>(PaintingVariant.KEBAB)),
        SnifferState(SnifferMeta.State.IDLING),
        ArmadilloState(ArmadilloMeta.State.IDLE),
        Vector3(Vec.ZERO),
        Quaternion(new float[]{0, 0, 0, 0})
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
                final EntryImpl<?> value = (EntryImpl<?>) EMPTY_VALUES.get(type);
                if (value == null) throw new UnsupportedOperationException("Unknown value type: " + type);
                return new EntryImpl(type, value.serializer.read(buffer), value.serializer);
            }
        };
    }
}
