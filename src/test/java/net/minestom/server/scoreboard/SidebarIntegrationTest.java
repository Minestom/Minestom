package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ResetScorePacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class SidebarIntegrationTest {
    @Test
    public void updateOnlySendsChangedLines(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        Sidebar sidebar = Sidebar.create(Component.text("Title"));
        sidebar.addViewer(player);
        assertTrue(sidebar.isViewer(player));

        var scoreCollector = connection.trackIncoming(UpdateScorePacket.class);
        sidebar.update(Component.text("a"), Component.text("b"), Component.text("c"));
        assertEquals(List.of(Component.text("a"), Component.text("b"), Component.text("c")), sidebar.getLines());

        var packets = scoreCollector.collect();
        assertEquals(3, packets.size());
        assertEquals(
                List.of(Component.text("a"), Component.text("b"), Component.text("c")),
                packets.stream().map(UpdateScorePacket::displayName).toList()
        );
        assertTrue(packets.get(0).score() > packets.get(1).score());
        assertTrue(packets.get(1).score() > packets.get(2).score());
        final String secondLineHolder = packets.get(1).entityName();

        scoreCollector = connection.trackIncoming(UpdateScorePacket.class);
        sidebar.update(Component.text("a"), Component.text("updated"), Component.text("c"));
        scoreCollector.assertSingle(packet -> {
            assertEquals(Component.text("updated"), packet.displayName());
            assertEquals(secondLineHolder, packet.entityName());
        });

        scoreCollector = connection.trackIncoming(UpdateScorePacket.class);
        sidebar.setLine(0, Component.text("first"));
        assertEquals(Component.text("first"), sidebar.getLines().getFirst());
        scoreCollector.assertSingle(packet -> assertEquals(Component.text("first"), packet.displayName()));

        scoreCollector = connection.trackIncoming(UpdateScorePacket.class);
        sidebar.update(sidebar.getLines());
        scoreCollector.assertEmpty();
    }

    @Test
    public void shrinkingRemovesLines(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        Sidebar sidebar = Sidebar.create(Component.text("Title"));
        sidebar.addViewer(player);

        var scoreCollector = connection.trackIncoming(UpdateScorePacket.class);
        sidebar.update(Component.text("a"), Component.text("b"), Component.text("c"));
        var holders = scoreCollector.collect().stream().map(UpdateScorePacket::entityName).toList();

        var resetCollector = connection.trackIncoming(ResetScorePacket.class);
        sidebar.update(Component.text("a"));
        var removed = resetCollector.collect().stream().map(ResetScorePacket::owner).collect(Collectors.toSet());
        assertEquals(Set.of(holders.get(1), holders.get(2)), removed);
        assertEquals(1, sidebar.getLines().size());
        assertEquals(1, sidebar.getObjective().getEntries().size());
    }

    @Test
    public void lateViewerSeesLines(Env env) {
        var instance = env.createFlatInstance();

        Sidebar sidebar = Sidebar.create(Component.text("Title"));
        sidebar.update(Component.text("a"), Component.text("b"), Component.text("c"));

        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));
        var collector = connection.trackIncoming();
        sidebar.addViewer(player);

        var packets = collector.collect();
        assertEquals(5, packets.size());

        var creation = assertInstanceOf(ScoreboardObjectivePacket.class, packets.getFirst());
        assertEquals(0, creation.mode());
        assertEquals(Component.text("Title"), creation.objectiveValue());
        assertEquals(NumberFormat.blank(), creation.numberFormat());

        var contents = packets.subList(1, 4).stream()
                .map(packet -> assertInstanceOf(UpdateScorePacket.class, packet).displayName())
                .collect(Collectors.toSet());
        assertEquals(Set.of(Component.text("a"), Component.text("b"), Component.text("c")), contents);

        var display = assertInstanceOf(DisplayScoreboardPacket.class, packets.getLast());
        assertEquals(DisplaySlot.SIDEBAR, display.position());
    }

    @Test
    public void showHide(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        Sidebar first = Sidebar.create(Component.text("First"));
        Sidebar second = Sidebar.create(Component.text("Second"));

        assertTrue(first.addViewer(player));
        assertFalse(first.addViewer(player));
        assertTrue(first.isViewer(player));

        assertTrue(second.addViewer(player));
        assertFalse(first.isViewer(player));
        assertTrue(second.isViewer(player));

        assertFalse(first.removeViewer(player));
        assertTrue(second.isViewer(player));

        assertTrue(second.removeViewer(player));
        assertFalse(second.isViewer(player));
        assertNull(player.getDisplayedObjective(DisplaySlot.SIDEBAR));
    }

    @Test
    public void maxLines(Env env) {
        Sidebar sidebar = Sidebar.create(Component.text("Title"));
        List<Component> lines = IntStream.range(0, Sidebar.MAX_LINES + 1)
                .mapToObj(i -> (Component) Component.text(i))
                .toList();
        assertThrows(IllegalArgumentException.class, () -> sidebar.update(lines));
        assertTrue(sidebar.getLines().isEmpty());

        sidebar.update(lines.subList(0, Sidebar.MAX_LINES));
        assertEquals(Sidebar.MAX_LINES, sidebar.getLines().size());
    }
}
