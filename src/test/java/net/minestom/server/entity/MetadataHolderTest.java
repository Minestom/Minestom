package net.minestom.server.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class MetadataHolderTest {

    @Test
    public void changesListenerCalledOnSet() {
        List<Map<Integer, Metadata.Entry<?>>> received = new ArrayList<>();
        MetadataHolder holder = new MetadataHolder(received::add);

        holder.set(MetadataDef.CUSTOM_NAME_VISIBLE, true);

        assertEquals(1, received.size());
        Map<Integer, Metadata.Entry<?>> changes = received.getFirst();
        assertEquals(1, changes.size());
        assertEquals(true, changes.get(MetadataDef.CUSTOM_NAME_VISIBLE.index()).value());
    }

    @Test
    public void changesListenerBatchedWhenNotifyDisabled() {
        List<Map<Integer, Metadata.Entry<?>>> received = new ArrayList<>();
        MetadataHolder holder = new MetadataHolder(received::add);

        holder.setNotifyAboutChanges(false);
        holder.set(MetadataDef.CUSTOM_NAME_VISIBLE, true);
        holder.set(MetadataDef.AIR_TICKS, 42);
        assertTrue(received.isEmpty(), "Listener should not be called while notification is disabled");

        holder.setNotifyAboutChanges(true);

        assertEquals(1, received.size());
        Map<Integer, Metadata.Entry<?>> changes = received.getFirst();
        assertEquals(2, changes.size());
        assertEquals(true, changes.get(MetadataDef.CUSTOM_NAME_VISIBLE.index()).value());
        assertEquals(42, changes.get(MetadataDef.AIR_TICKS.index()).value());
    }

    @Test
    public void changesListenerNotCalledWhenNothingBatched() {
        List<Map<Integer, Metadata.Entry<?>>> received = new ArrayList<>();
        MetadataHolder holder = new MetadataHolder(received::add);

        holder.setNotifyAboutChanges(false);
        holder.setNotifyAboutChanges(true);

        assertTrue(received.isEmpty());
    }

    @SuppressWarnings({"ConstantConditions", "removal"})
    @Test
    public void testNullCtor() {
        assertDoesNotThrow(() -> new MetadataHolder((Entity) null));
        assertThrows(NullPointerException.class, () -> new MetadataHolder((Consumer<Map<Integer, Metadata.Entry<?>>>) null));
    }
}
