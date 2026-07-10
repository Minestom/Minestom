package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.testing.Collector;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.testing.TestConnection;
import org.junit.jupiter.api.Test;

import static net.minestom.server.scoreboard.Scoreboard.Position.*;
import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class ScoreboardIntegrationTest {
    @Test
    public void viewerTest(Env env) {
        TestConnection connection = env.createConnection();
        Player player = connection.connect(env.createEmptyInstance(), Pos.ZERO);
        Collector<ServerPacket> collector = connection.trackIncoming();

        String objectiveName = "scoreboard";
        ScoreEntry entry = new ScoreEntry(1234, Component.text("Test entry"), NumberFormat.blank());
        Scoreboard scoreboard = Scoreboard.create(objectiveName);
        ServerPacket creationPacket = new ScoreboardObjectivePacket(objectiveName, (byte) 0, Component.text(objectiveName), Scoreboard.RenderType.INTEGER, null);
        ServerPacket entryUpdatePacket = entry.getUpdateScorePacket("Test", "scoreboard");
        scoreboard.updateEntry("Test", entry);

        assertTrue(scoreboard.addViewer(player, SIDEBAR));
        assertFalse(scoreboard.addViewer(player, SIDEBAR));
        assertTrue(scoreboard.addViewer(player, PLAYER_LIST));

        assertTrue(scoreboard.removeViewer(player, SIDEBAR));
        assertFalse(scoreboard.removeViewer(player, SIDEBAR));
        assertTrue(scoreboard.isViewer(player, PLAYER_LIST));
        assertTrue(scoreboard.removeViewer(player, PLAYER_LIST));

        assertFalse(scoreboard.isViewer(player));

        var packets = collector.collect();
        var it = packets.iterator();
        assertEquals(creationPacket, it.next());
        assertEquals(entryUpdatePacket, it.next());
        assertEquals(new DisplayScoreboardPacket(SIDEBAR.asByte(), objectiveName), it.next());
        assertEquals(new DisplayScoreboardPacket(PLAYER_LIST.asByte(), objectiveName), it.next());
        assertEquals(new DisplayScoreboardPacket(SIDEBAR.asByte(), ""), it.next());
        assertEquals(new DisplayScoreboardPacket(PLAYER_LIST.asByte(), ""), it.next());
        assertTrue(it.next() instanceof ScoreboardObjectivePacket(_, byte b, _, _, _) && b == 1);
        assertFalse(it.hasNext());
    }
}
