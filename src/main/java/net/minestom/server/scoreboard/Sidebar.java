package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Sidebar implements Viewable {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    /**
     * <b>WARNING:</b> You should NOT create any scoreboards/teams with the same prefixes as those
     */
    private static final String SCOREBOARD_PREFIX = "ms-sidebar-";
    private static final String TEAM_PREFIX = "ms-sidebar-";

    private static final List<String> ENTITY_NAMES = List.of("0123456789abcdef".split(""));

    /**
     * Limited by the client, ensures that name queue is never empty
     */
    private static final int MAX_LINES_COUNT = 15;

    private final Scoreboard scoreboard;
    private final Map<LineIdentifier, LineData> lines = new IdentityHashMap<>();
    private final Queue<String> availableNames = new ArrayDeque<>(ENTITY_NAMES);

    /**
     * Creates a new sidebar
     *
     * @param title The title of the sidebar
     */
    public Sidebar(Component title) {
        scoreboard = Scoreboard.create(SCOREBOARD_PREFIX + COUNTER.incrementAndGet(), Scoreboard.Position.SIDEBAR);
        scoreboard.setDisplayName(title);
    }

    /**
     * Gets the {@link Sidebar} title
     *
     * @return The sidebar title
     */
    public @Nullable Component getTitle() {
        return scoreboard.getDisplayName();
    }

    /**
     * Changes the {@link Sidebar} title
     *
     * @param title The new sidebar title
     */
    public void setTitle(Component title) {
        scoreboard.setDisplayName(title);
    }

    public LineIdentifier addLine(LineContent content) {
        LineIdentifier id = new LineIdentifier();
        addLine(id, content);
        return id;
    }

    public void addLine(LineIdentifier id, LineContent content) {
        Check.stateCondition(lines.size() >= MAX_LINES_COUNT, "You cannot have more than " + MAX_LINES_COUNT + "  lines");
        String entity = availableNames.poll();
        assert entity != null;
        LineData data = new LineData("§" + entity, content, TEAM_PREFIX + entity);
        lines.put(id, data);
        scoreboard.updateScore(data.entity, 0);
        scoreboard.sendPacketToViewers(data.createPacket());
    }

    public @Nullable LineContent getLineContent(LineIdentifier id) {
        LineData data = lines.get(id);
        return data != null ? data.content() : null;
    }

    public void setLineContent(LineIdentifier id, LineContent content) {
        lines.compute(id, (_, v) -> {
            if (v == null || v.content.equals(content)) return null;
            scoreboard.updateScore(v.entity, 0);
            LineData updated = v.withContent(content);
            scoreboard.sendPacketToViewers(updated.updatePacket());
            return updated;
        });
    }

    public void removeLine(LineIdentifier id) {
        LineData data = lines.remove(id);
        if (data == null) return;
        scoreboard.removeScore(data.entity);
        scoreboard.sendPacketToViewers(data.destroyPacket());
    }

    @Override
    public boolean addViewer(Player player) {
        if (scoreboard.getViewers().contains(player)) return false;
        player.showScoreboard(scoreboard);
        lines.forEach((_, data) -> player.sendPackets(
                new UpdateScorePacket(data.entity(), scoreboard.getObjectiveName(), 0, null, data.content().numberFormat()),
                data.createPacket()
        ));
        return true;
    }

    @Override
    public boolean removeViewer(Player player) {
        if (!scoreboard.getViewers().contains(player)) return false;
        player.hideScoreboard(scoreboard);
        lines.forEach((_, data) -> player.sendPacket(data.destroyPacket()));
        return true;
    }

    @Override
    public Set<Player> getViewers() {
        return scoreboard.getViewers();
    }

    public record LineContent(Component content, @Nullable NumberFormat numberFormat, int score) {
        public LineContent withContent(Component content) {
            return new LineContent(content, numberFormat, score);
        }

        public LineContent withNumberFormat(NumberFormat numberFormat) {
            return new LineContent(content, numberFormat, score);
        }

        public LineContent withScore(int score) {
            return new LineContent(content, numberFormat, score);
        }
    }

    public record LineIdentifier(String id) {
        private static final AtomicInteger COUNTER = new AtomicInteger();
        public LineIdentifier() {
            this("Unnamed Sidebar Line " + COUNTER.getAndIncrement());
        }
    }

    private record LineData(String entity, LineContent content, String teamName) {
        static final Component EMPTY = Component.empty();

        TeamsPacket createPacket() {
            return new TeamsPacket(teamName, new TeamsPacket.CreateTeamAction(
                    EMPTY, (byte) 0, TeamsPacket.NameTagVisibility.NEVER, TeamsPacket.CollisionRule.NEVER,
                    NamedTextColor.BLACK, content.content(), EMPTY, List.of(entity)
            ));
        }

        TeamsPacket destroyPacket() {
            return new TeamsPacket(teamName, new TeamsPacket.RemoveTeamAction());
        }

        TeamsPacket updatePacket() {
            return new TeamsPacket(teamName, new TeamsPacket.UpdateTeamAction(
                    EMPTY, (byte) 0, TeamsPacket.NameTagVisibility.NEVER, TeamsPacket.CollisionRule.NEVER,
                    NamedTextColor.BLACK, content.content(), EMPTY
            ));
        }

        LineData withContent(LineContent content) {
            return new LineData(entity, content, teamName);
        }
    }
}
