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
    public void entryUpdates(Env env) {
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

        objective.removeEntry("holder");
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
    public void displaySendsObjective(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        Objective objective = Objective.create("test", Component.text("Test"));
        objective.updateScore("holder", 5);

        var objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);
        var displayCollector = connection.trackIncoming(DisplayScoreboardPacket.class);
        var scoreCollector = connection.trackIncoming(UpdateScorePacket.class);

        player.setDisplayedObjective(DisplaySlot.SIDEBAR, objective);
        assertEquals(objective, player.getDisplayedObjective(DisplaySlot.SIDEBAR));
        assertTrue(objective.isViewer(player));
        assertEquals(Set.of(player), objective.getViewers());

        objectiveCollector.assertSingle(packet -> {
            assertEquals("test", packet.objectiveName());
            assertEquals(0, packet.mode());
            assertEquals(Component.text("Test"), packet.objectiveValue());
        });
        displayCollector.assertSingle(packet -> {
            assertEquals(DisplaySlot.SIDEBAR, packet.position());
            assertEquals("test", packet.scoreName());
        });
        scoreCollector.assertSingle(packet -> {
            assertEquals("holder", packet.entityName());
            assertEquals(5, packet.score());
        });
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

        var objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);
        objective.setDisplayName(Component.text("Updated"));
        objectiveCollector.assertSingle(packet -> {
            assertEquals(2, packet.mode());
            assertEquals(Component.text("Updated"), packet.objectiveValue());
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
        displayCollector.assertCount(2);

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

        var objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);
        player.setDisplayedObjective(DisplaySlot.SIDEBAR, second);
        assertFalse(first.isViewer(player));
        assertTrue(second.isViewer(player));
        objectiveCollector.assertCount(2);
        objectiveCollector = connection.trackIncoming(ScoreboardObjectivePacket.class);
        objectiveCollector.assertEmpty();
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
    }
}
