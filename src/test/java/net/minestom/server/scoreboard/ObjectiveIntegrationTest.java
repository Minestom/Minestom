package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ResetScorePacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class ObjectiveIntegrationTest {
    @Test
    public void entryUpdates(Env ignored) {
        Objective objective = Objective.create("test");
        assertEquals("test", objective.getName());
        assertEquals(Component.text("test"), objective.getDisplayName());
        assertNull(objective.getEntry("holder"));

        objective.updateScore("holder", 5);
        assertEquals(new ScoreEntry(5, null, null), objective.getEntry("holder"));

        final Component displayName = Component.text("display");
        objective.updateDisplayName("holder", displayName);
        assertEquals(new ScoreEntry(5, displayName, null), objective.getEntry("holder"));

        final NumberFormat numberFormat = NumberFormat.blank();
        objective.updateNumberFormat("holder", numberFormat);
        assertEquals(new ScoreEntry(5, displayName, numberFormat), objective.getEntry("holder"));

        objective.updateScore("holder", 6);
        assertEquals(new ScoreEntry(6, displayName, numberFormat), objective.getEntry("holder"));

        objective.updateDisplayName("other", displayName);
        assertEquals(new ScoreEntry(0, displayName, null), objective.getEntry("other"));

        objective.removeEntry("holder");
        objective.removeEntry("other");
        assertNull(objective.getEntry("holder"));
        assertTrue(objective.getEntries().isEmpty());

        assertThrows(UnsupportedOperationException.class, () -> objective.getEntries().put("holder", ScoreEntry.DEFAULT));
    }

    @Test
    public void scoreHolder(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 40, 0));
        assertEquals(player.getUsername(), Objective.scoreHolder(player));

        var entity = new Entity(EntityType.ZOMBIE);
        assertEquals(entity.getUuid().toString(), Objective.scoreHolder(entity));
    }

    @Test
    public void displaySendsObjectiveInOrder(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        Objective objective = Objective.create("test", Component.text("Test"));
        objective.updateScore("holder", 5);

        var collector = connection.trackIncoming();
        player.setDisplayedObjective(DisplaySlot.SIDEBAR, objective);
        assertEquals(objective, player.getDisplayedObjective(DisplaySlot.SIDEBAR));
        assertTrue(objective.isViewer(player));
        assertEquals(Set.of(player), objective.getViewers());

        var packets = collector.collect();
        assertEquals(3, packets.size());

        var creation = assertInstanceOf(ScoreboardObjectivePacket.class, packets.get(0));
        assertEquals("test", creation.objectiveName());
        assertEquals(0, creation.mode());
        assertEquals(Component.text("Test"), creation.objectiveValue());

        var score = assertInstanceOf(UpdateScorePacket.class, packets.get(1));
        assertEquals("holder", score.owner());
        assertEquals(5, score.score());

        var display = assertInstanceOf(DisplayScoreboardPacket.class, packets.get(2));
        assertEquals(DisplaySlot.SIDEBAR, display.position());
        assertEquals("test", display.scoreName());
    }

    @Test
    public void updatesForwardedToViewers(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        Objective objective = Objective.create("test");
        player.setDisplayedObjective(DisplaySlot.PLAYER_LIST, objective);

        var scoreCollector = connection.trackIncoming(UpdateScorePacket.class);
        objective.updateScore("holder", 1);
        scoreCollector.assertSingle(packet -> assertEquals(1, packet.score()));

        var resetCollector = connection.trackIncoming(ResetScorePacket.class);
        objective.removeEntry("holder");
        resetCollector.assertSingle(packet -> assertEquals("holder", packet.owner()));

        resetCollector = connection.trackIncoming(ResetScorePacket.class);
        objective.removeEntry("missing");
        resetCollector.assertEmpty();

        var objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);
        objective.setDisplayName(Component.text("Updated"));
        objectiveCollector.assertSingle(packet -> {
            assertEquals(2, packet.mode());
            assertEquals(Component.text("Updated"), packet.objectiveValue());
        });

        objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);
        objective.setRenderType(RenderType.HEARTS);
        objectiveCollector.assertSingle(packet -> {
            assertEquals(2, packet.mode());
            assertEquals(RenderType.HEARTS, packet.type());
        });
    }

    @Test
    public void multipleSlots(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        Objective objective = Objective.create("test");

        var objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);
        var displayCollector = connection.trackIncoming(DisplayScoreboardPacket.class);
        player.setDisplayedObjective(DisplaySlot.SIDEBAR, objective);
        player.setDisplayedObjective(DisplaySlot.PLAYER_LIST, objective);
        objectiveCollector.assertSingle(packet -> assertEquals(0, packet.mode()));

        var displays = displayCollector.collect();
        assertEquals(2, displays.size());
        assertEquals(DisplaySlot.SIDEBAR, displays.get(0).position());
        assertEquals(DisplaySlot.PLAYER_LIST, displays.get(1).position());

        objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);
        displayCollector = connection.trackIncoming(DisplayScoreboardPacket.class);
        player.setDisplayedObjective(DisplaySlot.SIDEBAR, null);
        assertTrue(objective.isViewer(player));
        objectiveCollector.assertEmpty();
        displayCollector.assertSingle(packet -> {
            assertEquals(DisplaySlot.SIDEBAR, packet.position());
            assertEquals("", packet.scoreName());
        });

        objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);
        player.setDisplayedObjective(DisplaySlot.PLAYER_LIST, null);
        assertFalse(objective.isViewer(player));
        objectiveCollector.assertSingle(packet -> assertEquals(1, packet.mode()));
    }

    @Test
    public void replaceObjectiveInSlot(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        Objective first = Objective.create("first");
        Objective second = Objective.create("second");
        player.setDisplayedObjective(DisplaySlot.SIDEBAR, first);

        var collector = connection.trackIncoming();
        player.setDisplayedObjective(DisplaySlot.SIDEBAR, second);
        assertFalse(first.isViewer(player));
        assertTrue(second.isViewer(player));

        var packets = collector.collect();
        assertEquals(3, packets.size());

        var creation = assertInstanceOf(ScoreboardObjectivePacket.class, packets.get(0));
        assertEquals("second", creation.objectiveName());
        assertEquals(0, creation.mode());

        var display = assertInstanceOf(DisplayScoreboardPacket.class, packets.get(1));
        assertEquals(DisplaySlot.SIDEBAR, display.position());
        assertEquals("second", display.scoreName());

        var destruction = assertInstanceOf(ScoreboardObjectivePacket.class, packets.get(2));
        assertEquals("first", destruction.objectiveName());
        assertEquals(1, destruction.mode());
    }

    @Test
    public void disconnectCleanup(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        Objective objective = Objective.create("test");
        player.setDisplayedObjective(DisplaySlot.SIDEBAR, objective);
        player.setDisplayedObjective(DisplaySlot.BELOW_NAME, objective);
        assertTrue(objective.isViewer(player));

        player.remove();
        assertFalse(objective.isViewer(player));
        assertTrue(objective.getViewers().isEmpty());
        assertNull(player.getDisplayedObjective(DisplaySlot.SIDEBAR));
        assertNull(player.getDisplayedObjective(DisplaySlot.BELOW_NAME));
    }
}
