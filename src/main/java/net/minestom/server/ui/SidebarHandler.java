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
    private static final int MAX_LINES_COUNT = 16;

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
    private final int[] scoreboardHashCodes = new int[MAX_LINES_COUNT];

    private SidebarUI lastSideBar;

    SidebarHandler(MessagePassingQueue<ServerPacket> queue) {
        this.queue = queue;
        Arrays.fill(scoreboardHashCodes, -1);
    }

    public void handle(@Nullable SidebarUI sidebar) {
        if(sidebar == this.lastSideBar) return;
        this.lastSideBar = sidebar;
        if (sidebar == null) {
            if (scoreboardHashCodes[0] == -1) return;
            Arrays.fill(scoreboardHashCodes, -1);
            destroySidebarObjective();
        } else if (sidebar instanceof SidebarUIImpl impl) {
            // Set title
            setScoreboardLine(0, impl.title());
            // Update lines
            final List<Component> lines = impl.lines();
            final int newSize = lines.size();
            for (int i = 1; i < MAX_LINES_COUNT; ++i) {
                final int index = i - 1;
                setScoreboardLine(i, index < newSize ? lines.get(index) : null);
            }
        } else {
            throw new IllegalArgumentException("Unsupported sidebar type: " + sidebar.getClass().getName());
        }
    }

    private void setScoreboardLine(int index, @Nullable Component line) {
        final int oldLine = scoreboardHashCodes[index];
        if (line == null) {
            if (index <= 0 || oldLine == -1) return;
            scoreboardHashCodes[index] = -1;
            removeSidebarLine(index - 1);
        } else {
            final int lineHashCode = line.hashCode();
            scoreboardHashCodes[index] = lineHashCode;
            if (oldLine == -1) {
                // Creation
                if (index <= 0) createSidebarObjective(line);
                else createSidebarLine(index - 1, line);
            } else if (oldLine != lineHashCode) {
                // Update
                if (index <= 0) updateSidebarTitle(line);
                else updateSidebarLine(index - 1, line);
            }
        }
    }

    private void createSidebarObjective(Component title) {
        // Create objective
        queue.offer(new ScoreboardObjectivePacket(OBJECTIVE_NAME, (byte) 0,
                title, ScoreboardObjectivePacket.Type.INTEGER));
        // Show sidebar scoreboard
        queue.offer(new DisplayScoreboardPacket(OBJECTIVE_POSITION, OBJECTIVE_NAME));
    }

    private void destroySidebarObjective() {
        // Creative objective
        queue.offer(new ScoreboardObjectivePacket(OBJECTIVE_NAME, (byte) 1, null, null));
        // Show sidebar scoreboard (wait for scores packet)
        queue.offer(new DisplayScoreboardPacket(OBJECTIVE_POSITION, OBJECTIVE_NAME));
    }

    private void updateSidebarTitle(Component title) {
        queue.offer(new ScoreboardObjectivePacket(OBJECTIVE_NAME, (byte) 2,
                title, ScoreboardObjectivePacket.Type.INTEGER));
    }

    private void updateSidebarLine(int index, Component line) {
        final var action = new TeamsPacket.UpdateTeamAction(TEAM_DISPLAY_NAME, FRIENDLY_FLAGS,
                NAME_TAG_VISIBILITY, COLLISION_RULE, TEAM_COLOR, line, Component.empty());
        queue.offer(new TeamsPacket(TEAM_NAME + "_" + index, action));
    }

    private void createSidebarLine(int index, Component line) {
        final String entityName = ENTITY_NAMES.get(index);

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
