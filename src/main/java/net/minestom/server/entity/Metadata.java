package net.minestom.server.entity;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
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
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.Direction;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class Metadata {
    public static Entry<Byte> Byte(byte value) {
        return new MetadataImpl.EntryImpl<>(TYPE_BYTE, value, NetworkBuffer.BYTE);
    }

    public static Entry<Integer> VarInt(int value) {
        return new MetadataImpl.EntryImpl<>(TYPE_VARINT, value, NetworkBuffer.VAR_INT);
    }

    public static Entry<Long> VarLong(long value) {
        return new MetadataImpl.EntryImpl<>(TYPE_LONG, value, NetworkBuffer.VAR_LONG);
    }

    public static Entry<Float> Float(float value) {
        return new MetadataImpl.EntryImpl<>(TYPE_FLOAT, value, NetworkBuffer.FLOAT);
    }

    public static Entry<String> String(@NotNull String value) {
        return new MetadataImpl.EntryImpl<>(TYPE_STRING, value, NetworkBuffer.STRING);
    }

    public static Entry<Component> Chat(@NotNull Component value) {
        return new MetadataImpl.EntryImpl<>(TYPE_CHAT, value, NetworkBuffer.COMPONENT);
    }

    public static Entry<Component> OptChat(@Nullable Component value) {
        return new MetadataImpl.EntryImpl<>(TYPE_OPT_CHAT, value, NetworkBuffer.OPT_CHAT);
    }

    public static Entry<ItemStack> ItemStack(@NotNull ItemStack value) {
        return new MetadataImpl.EntryImpl<>(TYPE_ITEM_STACK, value, ItemStack.NETWORK_TYPE);
    }

    public static Entry<Boolean> Boolean(boolean value) {
        return new MetadataImpl.EntryImpl<>(TYPE_BOOLEAN, value, NetworkBuffer.BOOLEAN);
    }

    public static Entry<Point> Rotation(@NotNull Point value) {
        return new MetadataImpl.EntryImpl<>(TYPE_ROTATION, value, NetworkBuffer.VECTOR3);
    }

    public static Entry<Point> BlockPosition(@NotNull Point value) {
        return new MetadataImpl.EntryImpl<>(TYPE_BLOCK_POSITION, value, NetworkBuffer.BLOCK_POSITION);
    }

    public static Entry<Point> OptBlockPosition(@Nullable Point value) {
        return new MetadataImpl.EntryImpl<>(TYPE_OPT_BLOCK_POSITION, value, NetworkBuffer.OPT_BLOCK_POSITION);
    }

    public static Entry<Direction> Direction(@NotNull Direction value) {
        return new MetadataImpl.EntryImpl<>(TYPE_DIRECTION, value, NetworkBuffer.DIRECTION);
    }

    public static Entry<UUID> OptUUID(@Nullable UUID value) {
        return new MetadataImpl.EntryImpl<>(TYPE_OPT_UUID, value, NetworkBuffer.OPT_UUID);
    }

    public static Entry<Block> BlockState(@NotNull Block value) {
        return new MetadataImpl.EntryImpl<>(TYPE_BLOCKSTATE, value, Block.NETWORK_TYPE);
    }

    public static Entry<Integer> OptBlockState(@Nullable Integer value) {
        return new MetadataImpl.EntryImpl<>(TYPE_OPT_BLOCKSTATE, value, new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Integer value) {
                buffer.write(NetworkBuffer.VAR_INT, value == null ? 0 : value);
            }

            @Override
            public Integer read(@NotNull NetworkBuffer buffer) {
                int value = buffer.read(NetworkBuffer.VAR_INT);
                return value == 0 ? null : value;
            }
        });
    }

    public static Entry<BinaryTag> NBT(@NotNull BinaryTag nbt) {
        return new MetadataImpl.EntryImpl<>(TYPE_NBT, nbt, NetworkBuffer.NBT);
    }

    public static Entry<Particle> Particle(@NotNull Particle particle) {
        return new MetadataImpl.EntryImpl<>(TYPE_PARTICLE, particle, Particle.NETWORK_TYPE);
    }

    public static Entry<List<Particle>> ParticleList(@NotNull List<Particle> particles) {
        return new MetadataImpl.EntryImpl<>(TYPE_PARTICLE_LIST, particles, Particle.NETWORK_TYPE.list(Short.MAX_VALUE));
    }

    public static Entry<int[]> VillagerData(int[] data) {
        Check.argCondition(data.length != 3, "Villager data array must have a length of 3");
        return new MetadataImpl.EntryImpl<>(TYPE_VILLAGERDATA, data, NetworkBuffer.VILLAGER_DATA);
    }

    public static Entry<int[]> VillagerData(int villagerType, int villagerProfession, int level) {
        return VillagerData(new int[]{villagerType, villagerProfession, level});
    }

    public static Entry<Integer> OptVarInt(@Nullable Integer value) {
        return new MetadataImpl.EntryImpl<>(TYPE_OPT_VARINT, value, new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Integer value) {
                buffer.write(NetworkBuffer.VAR_INT, value == null ? 0 : value + 1);
            }

            @Override
            public Integer read(@NotNull NetworkBuffer buffer) {
                int value = buffer.read(NetworkBuffer.VAR_INT);
                return value == 0 ? null : value - 1;
            }
        });
    }

    public static Entry<EntityPose> Pose(@NotNull EntityPose value) {
        return new MetadataImpl.EntryImpl<>(TYPE_POSE, value, NetworkBuffer.POSE);
    }

    public static Entry<CatMeta.Variant> CatVariant(@NotNull CatMeta.Variant value) {
        return new MetadataImpl.EntryImpl<>(TYPE_CAT_VARIANT, value, CatMeta.Variant.NETWORK_TYPE);
    }

    public static Entry<DynamicRegistry.Key<WolfMeta.Variant>> WolfVariant(@NotNull DynamicRegistry.Key<WolfMeta.Variant> value) {
        return new MetadataImpl.EntryImpl<>(TYPE_WOLF_VARIANT, value, WolfMeta.Variant.NETWORK_TYPE);
    }

    public static Entry<FrogMeta.Variant> FrogVariant(@NotNull FrogMeta.Variant value) {
        return new MetadataImpl.EntryImpl<>(TYPE_FROG_VARIANT, value, FrogMeta.Variant.NETWORK_TYPE);
    }

    public static Entry<DynamicRegistry.Key<PaintingMeta.Variant>> PaintingVariant(@NotNull DynamicRegistry.Key<PaintingMeta.Variant> value) {
        return new MetadataImpl.EntryImpl<>(TYPE_PAINTING_VARIANT, value, PaintingMeta.Variant.NETWORK_TYPE);
    }

    public static Entry<SnifferMeta.State> SnifferState(@NotNull SnifferMeta.State value) {
        return new MetadataImpl.EntryImpl<>(TYPE_SNIFFER_STATE, value, SnifferMeta.State.NETWORK_TYPE);
    }

    public static Entry<ArmadilloMeta.State> ArmadilloState(@NotNull ArmadilloMeta.State value) {
        return new MetadataImpl.EntryImpl<>(TYPE_ARMADILLO_STATE, value, ArmadilloMeta.State.NETWORK_TYPE);
    }

    public static Entry<Point> Vector3(@NotNull Point value) {
        return new MetadataImpl.EntryImpl<>(TYPE_VECTOR3, value, NetworkBuffer.VECTOR3);
    }

    public static Entry<float[]> Quaternion(float @NotNull [] value) {
        return new MetadataImpl.EntryImpl<>(TYPE_QUATERNION, value, NetworkBuffer.QUATERNION);
    }

    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    public static final byte TYPE_BYTE = nextId();
    public static final byte TYPE_VARINT = nextId();
    public static final byte TYPE_LONG = nextId();
    public static final byte TYPE_FLOAT = nextId();
    public static final byte TYPE_STRING = nextId();
    public static final byte TYPE_CHAT = nextId();
    public static final byte TYPE_OPT_CHAT = nextId();
    public static final byte TYPE_ITEM_STACK = nextId();
    public static final byte TYPE_BOOLEAN = nextId();
    public static final byte TYPE_ROTATION = nextId();
    public static final byte TYPE_BLOCK_POSITION = nextId();
    public static final byte TYPE_OPT_BLOCK_POSITION = nextId();
    public static final byte TYPE_DIRECTION = nextId();
    public static final byte TYPE_OPT_UUID = nextId();
    public static final byte TYPE_BLOCKSTATE = nextId();
    public static final byte TYPE_OPT_BLOCKSTATE = nextId();
    public static final byte TYPE_NBT = nextId();
    public static final byte TYPE_PARTICLE = nextId();
    public static final byte TYPE_PARTICLE_LIST = nextId();
    public static final byte TYPE_VILLAGERDATA = nextId();
    public static final byte TYPE_OPT_VARINT = nextId();
    public static final byte TYPE_POSE = nextId();
    public static final byte TYPE_CAT_VARIANT = nextId();
    public static final byte TYPE_WOLF_VARIANT = nextId();
    public static final byte TYPE_FROG_VARIANT = nextId();
    public static final byte TYPE_OPT_GLOBAL_POSITION = nextId(); // Unused by protocol it seems
    public static final byte TYPE_PAINTING_VARIANT = nextId();
    public static final byte TYPE_SNIFFER_STATE = nextId();
    public static final byte TYPE_ARMADILLO_STATE = nextId();
    public static final byte TYPE_VECTOR3 = nextId();
    public static final byte TYPE_QUATERNION = nextId();

    // Impl Note: Adding an entry here requires that a default value entry is added in MetadataImpl.EMPTY_VALUES

    private static byte nextId() {
        return (byte) NEXT_ID.getAndIncrement();
    }

    public sealed interface Entry<T> permits MetadataImpl.EntryImpl {
        @SuppressWarnings({"unchecked", "rawtypes"})
        NetworkBuffer.Type<Entry<?>> SERIALIZER = (NetworkBuffer.Type) MetadataImpl.EntryImpl.SERIALIZER;

        int type();

        @UnknownNullability
        T value();
    }
}
