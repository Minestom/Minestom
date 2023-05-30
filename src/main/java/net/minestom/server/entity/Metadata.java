package net.minestom.server.entity;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.metadata.animal.FrogMeta;
import net.minestom.server.entity.metadata.animal.tameable.CatMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBT;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Metadata {
    public static Entry<Byte> Byte(byte value) {
        return new MetadataImpl.EntryImpl<>(TYPE_BYTE, value, NetworkBuffer.BYTE);
    }

    public static Entry<Integer> VarInt(int value) {
        return new MetadataImpl.EntryImpl<>(TYPE_VARINT, value, NetworkBuffer.VAR_INT);
    }

    public static Entry<Long> Long(long value) {
        return new MetadataImpl.EntryImpl<>(TYPE_LONG, value, NetworkBuffer.LONG);
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
        return new MetadataImpl.EntryImpl<>(TYPE_OPTCHAT, value, NetworkBuffer.OPT_CHAT);
    }

    public static Entry<ItemStack> Slot(@NotNull ItemStack value) {
        return new MetadataImpl.EntryImpl<>(TYPE_SLOT, value, NetworkBuffer.ITEM);
    }

    public static Entry<Boolean> Boolean(boolean value) {
        return new MetadataImpl.EntryImpl<>(TYPE_BOOLEAN, value, NetworkBuffer.BOOLEAN);
    }

    public static Entry<Point> Rotation(@NotNull Point value) {
        return new MetadataImpl.EntryImpl<>(TYPE_ROTATION, value, NetworkBuffer.ROTATION);
    }

    public static Entry<Point> Position(@NotNull Point value) {
        return new MetadataImpl.EntryImpl<>(TYPE_POSITION, value, NetworkBuffer.BLOCK_POSITION);
    }

    public static Entry<Point> OptPosition(@Nullable Point value) {
        return new MetadataImpl.EntryImpl<>(TYPE_OPTPOSITION, value, NetworkBuffer.OPT_BLOCK_POSITION);
    }

    public static Entry<Direction> Direction(@NotNull Direction value) {
        return new MetadataImpl.EntryImpl<>(TYPE_DIRECTION, value, NetworkBuffer.DIRECTION);
    }

    public static Entry<UUID> OptUUID(@Nullable UUID value) {
        return new MetadataImpl.EntryImpl<>(TYPE_OPTUUID, value, NetworkBuffer.OPT_UUID);
    }

    public static Entry<Integer> OptBlockID(@Nullable Integer value) {
        return new MetadataImpl.EntryImpl<>(TYPE_OPTBLOCKID, value, NetworkBuffer.OPT_BLOCK_ID);
    }

    public static Entry<NBT> NBT(@NotNull NBT nbt) {
        return new MetadataImpl.EntryImpl<>(TYPE_NBT, nbt, NetworkBuffer.NBT);
    }

    public static Entry<int[]> VillagerData(int villagerType,
                                            int villagerProfession,
                                            int level) {
        return new MetadataImpl.EntryImpl<>(TYPE_VILLAGERDATA, new int[]{villagerType, villagerProfession, level},
                NetworkBuffer.VILLAGER_DATA);
    }

    public static Entry<Integer> OptVarInt(@Nullable Integer value) {
        return new MetadataImpl.EntryImpl<>(TYPE_OPTVARINT, value, NetworkBuffer.OPT_VAR_INT);
    }

    public static Entry<Entity.Pose> Pose(@NotNull Entity.Pose value) {
        return new MetadataImpl.EntryImpl<>(TYPE_POSE, value, NetworkBuffer.POSE);
    }

    public static Entry<CatMeta.Variant> CatVariant(@NotNull CatMeta.Variant value) {
        return new MetadataImpl.EntryImpl<>(TYPE_CAT_VARIANT, value, NetworkBuffer.CAT_VARIANT);
    }

    public static Entry<FrogMeta.Variant> FrogVariant(@NotNull FrogMeta.Variant value) {
        return new MetadataImpl.EntryImpl<>(TYPE_FROG_VARIANT, value, NetworkBuffer.FROG_VARIANT);
    }

    public static final byte TYPE_BYTE = 0;
    public static final byte TYPE_VARINT = 1;
    public static final byte TYPE_LONG = 2;
    public static final byte TYPE_FLOAT = 3;
    public static final byte TYPE_STRING = 4;
    public static final byte TYPE_CHAT = 5;
    public static final byte TYPE_OPTCHAT = 6;
    public static final byte TYPE_SLOT = 7;
    public static final byte TYPE_BOOLEAN = 8;
    public static final byte TYPE_ROTATION = 9;
    public static final byte TYPE_POSITION = 10;
    public static final byte TYPE_OPTPOSITION = 11;
    public static final byte TYPE_DIRECTION = 12;
    public static final byte TYPE_OPTUUID = 13;
    public static final byte TYPE_OPTBLOCKID = 14;
    public static final byte TYPE_NBT = 15;
    public static final byte TYPE_PARTICLE = 16;
    public static final byte TYPE_VILLAGERDATA = 17;
    public static final byte TYPE_OPTVARINT = 18;
    public static final byte TYPE_POSE = 19;
    public static final byte TYPE_CAT_VARIANT = 20;
    public static final byte TYPE_FROG_VARIANT = 21;

    private static final VarHandle NOTIFIED_CHANGES;

    static {
        try {
            NOTIFIED_CHANGES = MethodHandles.lookup().findVarHandle(Metadata.class, "notifyAboutChanges", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Entity entity;
    private volatile Entry<?>[] entries = new Entry<?>[0];
    private volatile Map<Integer, Entry<?>> entryMap = null;

    @SuppressWarnings("FieldMayBeFinal")
    private volatile boolean notifyAboutChanges = true;
    private final Map<Integer, Entry<?>> notNotifiedChanges = new HashMap<>();

    public Metadata(@Nullable Entity entity) {
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    public <T> T getIndex(int index, @Nullable T defaultValue) {
        final Entry<?>[] entries = this.entries;
        if (index < 0 || index >= entries.length) return defaultValue;
        final Entry<?> entry = entries[index];
        return entry != null ? (T) entry.value() : defaultValue;
    }

    public void setIndex(int index, @NotNull Entry<?> entry) {
        Entry<?>[] entries = this.entries;
        // Resize array if necessary
        if (index >= entries.length) {
            final int newLength = Math.max(entries.length * 2, index + 1);
            this.entries = entries = Arrays.copyOf(entries, newLength);
        }
        entries[index] = entry;
        this.entryMap = null;
        // Send metadata packet to update viewers and self
        final Entity entity = this.entity;
        if (entity != null && entity.isActive()) {
            if (!this.notifyAboutChanges) {
                synchronized (this.notNotifiedChanges) {
                    this.notNotifiedChanges.put(index, entry);
                }
            } else {
                entity.sendPacketToViewersAndSelf(new EntityMetaDataPacket(entity.getEntityId(), Map.of(index, entry)));
            }
        }
    }

    public void setNotifyAboutChanges(boolean notifyAboutChanges) {
        if (!NOTIFIED_CHANGES.compareAndSet(this, !notifyAboutChanges, notifyAboutChanges))
            return;
        if (!notifyAboutChanges) {
            // Ask future metadata changes to be cached
            return;
        }
        final Entity entity = this.entity;
        if (entity == null || !entity.isActive()) return;
        Map<Integer, Entry<?>> entries;
        synchronized (this.notNotifiedChanges) {
            Map<Integer, Entry<?>> awaitingChanges = this.notNotifiedChanges;
            if (awaitingChanges.isEmpty()) return;
            entries = Map.copyOf(awaitingChanges);
            awaitingChanges.clear();
        }
        entity.sendPacketToViewersAndSelf(new EntityMetaDataPacket(entity.getEntityId(), entries));
    }

    public @NotNull Map<Integer, Entry<?>> getEntries() {
        Map<Integer, Entry<?>> map = entryMap;
        if (map == null) {
            map = new HashMap<>();
            final Entry<?>[] entries = this.entries;
            for (int i = 0; i < entries.length; i++) {
                final Entry<?> entry = entries[i];
                if (entry != null) map.put(i, entry);
            }
            this.entryMap = Map.copyOf(map);
        }
        return map;
    }

    public sealed interface Entry<T> extends NetworkBuffer.Writer
            permits MetadataImpl.EntryImpl {
        int type();

        @UnknownNullability T value();

        @ApiStatus.Internal
        static @NotNull Entry<?> read(int type, @NotNull NetworkBuffer reader) {
            return MetadataImpl.EntryImpl.read(type, reader);
        }
    }
}
