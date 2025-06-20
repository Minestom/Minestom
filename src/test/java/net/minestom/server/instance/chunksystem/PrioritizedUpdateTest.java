package net.minestom.server.instance.chunksystem;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrioritizedUpdateTest {
    @SuppressWarnings("DataFlowIssue")
    @Test
    void testOrder() {
        var update1 = new PrioritizedUpdate(UpdateType.ADD_CLAIM_EXPLICIT, 1, 0, 0, null);
        var update2 = new PrioritizedUpdate(UpdateType.LOAD_PROPAGATE, 2, 0, 0, null);
        var update3 = new PrioritizedUpdate(UpdateType.LOAD_PROPAGATE, 3, 0, 0, null);
        var update4 = new PrioritizedUpdate(UpdateType.LOAD_PROPAGATE, 4, 0, 0, null);
        var update5 = new PrioritizedUpdate(UpdateType.UNLOAD_PROPAGATE, -5, 0, 0, null);
        var update6 = new PrioritizedUpdate(UpdateType.UNLOAD_PROPAGATE, -3, 0, 0, null);
        var update7 = new PrioritizedUpdate(UpdateType.REMOVE_CLAIM_EXPLICIT, -10, 0, 0, null);

        var list = Stream.of(update1, update2, update3, update4, update5, update6, update7).sorted(PrioritizedUpdate.COMPARATOR).toList();

        assertEquals(update7, list.getFirst());
        assertEquals(update1, list.get(1));
        assertEquals(update6, list.get(2));
        assertEquals(update5, list.get(3));
        assertEquals(update4, list.get(4));
        assertEquals(update3, list.get(5));
        assertEquals(update2, list.get(6));
    }
}
