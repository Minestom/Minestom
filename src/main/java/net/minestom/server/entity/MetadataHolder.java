package net.minestom.server.entity;

import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class MetadataHolder {
    private static final VarHandle NOTIFIED_CHANGES;

    static {
        try {
            NOTIFIED_CHANGES = MethodHandles.lookup().findVarHandle(MetadataHolder.class, "notifyAboutChanges", boolean.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private final Entity entity;
    private volatile Metadata.Entry<?>[] entries = new Metadata.Entry<?>[0];
    private volatile Map<Integer, Metadata.Entry<?>> entryMap = null;

    @SuppressWarnings("FieldMayBeFinal")
    private volatile boolean notifyAboutChanges = true;
    private final Map<Integer, Metadata.Entry<?>> notNotifiedChanges = new HashMap<>();

    public MetadataHolder(@Nullable Entity entity) {
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    public <T> T getIndex(int index, @Nullable T defaultValue) {
        final Metadata.Entry<?>[] entries = this.entries;
        if (index < 0 || index >= entries.length) return defaultValue;
        final Metadata.Entry<?> entry = entries[index];
        return entry != null ? (T) entry.value() : defaultValue;
    }

    public void setIndex(int index, @NotNull Metadata.Entry<?> entry) {
        Metadata.Entry<?>[] entries = this.entries;
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
        Map<Integer, Metadata.Entry<?>> entries;
        synchronized (this.notNotifiedChanges) {
            Map<Integer, Metadata.Entry<?>> awaitingChanges = this.notNotifiedChanges;
            if (awaitingChanges.isEmpty()) return;
            entries = Map.copyOf(awaitingChanges);
            awaitingChanges.clear();
        }
        entity.sendPacketToViewersAndSelf(new EntityMetaDataPacket(entity.getEntityId(), entries));
    }

    public @NotNull Map<Integer, Metadata.Entry<?>> getEntries() {
        Map<Integer, Metadata.Entry<?>> map = entryMap;
        if (map == null) {
            map = new HashMap<>();
            final Metadata.Entry<?>[] entries = this.entries;
            for (int i = 0; i < entries.length; i++) {
                final Metadata.Entry<?> entry = entries[i];
                if (entry != null) map.put(i, entry);
            }
            this.entryMap = Map.copyOf(map);
        }
        return map;
    }
}
