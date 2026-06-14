package net.minestom.server;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Scoreboard;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static net.minestom.server.scoreboard.Scoreboard.Position.*;
import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class ScoreboardViewerTest {
    @Test
    public void viewerTest(Env env) {
        Player player = env.createPlayer(env.createEmptyInstance(), Pos.ZERO);
        Scoreboard scoreboard = Scoreboard.create("scoreboard");

        assertTrue(scoreboard.addViewer(player, SIDEBAR));
        assertFalse(scoreboard.addViewer(player, SIDEBAR));
        assertTrue(scoreboard.addViewer(player, PLAYER_LIST));

        assertTrue(scoreboard.removeViewer(player, SIDEBAR));
        assertFalse(scoreboard.removeViewer(player, SIDEBAR));
        assertTrue(scoreboard.removeViewer(player, PLAYER_LIST));

        assertFalse(scoreboard.getViewers().containsKey(player));
    }
}
