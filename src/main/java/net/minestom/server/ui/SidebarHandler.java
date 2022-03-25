package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import org.jctools.queues.MessagePassingQueue;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static net.minestom.server.ui.PlayerUIImpl.*;

final class SidebarHandler {
    /**
     * Limited by the notchian client, do not change
     */
    private static final int MAX_LINES_COUNT = 15;

    private static final String OBJECTIVE_NAME = MAGIC + "_objective";
    private static final byte OBJECTIVE_POSITION = (byte) 1; // Sidebar
    private static final String TEAM_NAME = MAGIC + "_team";
    private static final Component TEAM_DISPLAY_NAME = Component.text(MAGIC + "_name");
    private static final List<String> ENTITY_NAMES;

    static {
        String[] names = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
        for (int i = 0; i < names.length; i++) {
            names[i] = "\u00A7" + names[i] + "\u00A7r";
        }
        ENTITY_NAMES = List.of(names);
    }

    private final MessagePassingQueue<ServerPacket> queue;

    private final int[] scoreboardHashCodes = new int[MAX_LINES_COUNT + 1];

    SidebarHandler(MessagePassingQueue<ServerPacket> queue) {
        this.queue = queue;
        Arrays.fill(scoreboardHashCodes, -1);
    }

    public void handle(@Nullable SidebarUI sidebar) {
        if (sidebar == null) {
            if (scoreboardHashCodes[0] == -1) return;
            Arrays.fill(scoreboardHashCodes, -1);
            destroySidebarObjective();
        } else if (sidebar instanceof SidebarUIImpl impl) {
            // Set title
            setScoreboardLine(0, impl.title());

            int index = 1;

            // Set specified sidebar lines
            for (; index - 1 < impl.lines().size() && index < scoreboardHashCodes.length; index++) {
                Component line = impl.lines().get(index - 1);
                setScoreboardLine(index, line);
            }

            // Clear remaining lines
            for (; index < scoreboardHashCodes.length; index++) {
                setScoreboardLine(index, null);
            }
        }
    }

    private void setScoreboardLine(int index, @Nullable Component line) {
        int oldLine = scoreboardHashCodes[index];

        if (line == null) {
            if (index <= 0 || oldLine == -1) return;
            scoreboardHashCodes[index] = -1;
            removeSidebarLine(index - 1);
        } else {
            int lineHashCode = line.hashCode();
            if (oldLine == -1) {
                scoreboardHashCodes[index] = lineHashCode;

                if (index <= 0) {
                    createSidebarObjective(line);
                } else {
                    createSidebarLine(index - 1, line);
                }
            } else if (oldLine != lineHashCode) {
                scoreboardHashCodes[index] = lineHashCode;

                if (index <= 0) {
                    updateSidebarTitle(line);
                } else {
                    updateSidebarLine(index - 1, line);
                }
            }
        }
    }

    private void createSidebarObjective(Component title) {
        var scoreboardObjectivePacket = new ScoreboardObjectivePacket(OBJECTIVE_NAME, (byte) 0,
                title, ScoreboardObjectivePacket.Type.INTEGER);

        var displayScoreboardPacket = new DisplayScoreboardPacket(OBJECTIVE_POSITION, OBJECTIVE_NAME);

        queue.offer(scoreboardObjectivePacket); // Create objective
        queue.offer(displayScoreboardPacket); // Show sidebar scoreboard
    }

    private void destroySidebarObjective() {
        var scoreboardObjectivePacket = new ScoreboardObjectivePacket(OBJECTIVE_NAME, (byte) 1, null, null);
        var displayScoreboardPacket = new DisplayScoreboardPacket(OBJECTIVE_POSITION, OBJECTIVE_NAME);

        queue.offer(scoreboardObjectivePacket); // Creative objective
        queue.offer(displayScoreboardPacket); // Show sidebar scoreboard (wait for scores packet)
    }

    private void updateSidebarTitle(Component title) {
        var packet = new ScoreboardObjectivePacket(OBJECTIVE_NAME, (byte) 2,
                title, ScoreboardObjectivePacket.Type.INTEGER);
        queue.offer(packet);
    }

    private void updateSidebarLine(int index, Component line) {
        final var action = new TeamsPacket.UpdateTeamAction(TEAM_DISPLAY_NAME, FRIENDLY_FLAGS,
                NAME_TAG_VISIBILITY, COLLISION_RULE, TEAM_COLOR, line, Component.empty());
        queue.offer(new TeamsPacket(TEAM_NAME + "_" + index, action));
    }

    private void createSidebarLine(int index, Component line) {
        String entityName = ENTITY_NAMES.get(index);

        final var action = new TeamsPacket.CreateTeamAction(TEAM_DISPLAY_NAME, FRIENDLY_FLAGS,
                NAME_TAG_VISIBILITY, COLLISION_RULE, TEAM_COLOR, line, Component.empty(),
                List.of(entityName));
        queue.offer(new TeamsPacket(TEAM_NAME + "_" + index, action));
        queue.offer(new UpdateScorePacket(entityName, (byte) 0, OBJECTIVE_NAME, 0));
    }

    private void removeSidebarLine(int index) {
        queue.offer(new UpdateScorePacket(ENTITY_NAMES.get(index), (byte) 1, OBJECTIVE_NAME, 0));
    }
}
