package net.minestom.server.scoreboard;

import net.kyori.adventure.text.Component;
import net.minestom.server.Viewable;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/// A sidebar displaying up to [#MAX_LINES] lines of text, ordered top to bottom.
///
/// Scores are hidden and managed internally; how a line's score is displayed
/// can be customized with [#setNumberFormat(int, NumberFormat)]. For anything
/// further, the backing objective is accessible through [#getObjective()].
///
/// ```java
/// Sidebar sidebar = Sidebar.create(Component.text("Title"));
/// sidebar.update(
///         Component.text("Line 1"),
///         Component.text("Line 2")
/// );
/// sidebar.addViewer(player);
/// ```
public final class Sidebar implements Viewable {
    /// The maximum number of lines the client can display.
    public static final int MAX_LINES = 15;

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final String OBJECTIVE_PREFIX = "sidebar-";
    private static final String LINE_PREFIX = "line-";

    private final Objective objective;
    private final List<Component> lines;
    private final @Nullable NumberFormat[] numberFormats;

    private Sidebar(Component title) {
        this.objective = Objective.create(OBJECTIVE_PREFIX + COUNTER.incrementAndGet(), title);
        this.objective.setDefaultNumberFormat(NumberFormat.blank());
        this.lines = new ArrayList<>();
        this.numberFormats = new NumberFormat[MAX_LINES];
    }

    /// Creates a new sidebar with no lines.
    ///
    /// @param title the sidebar title
    /// @return a new sidebar
    public static Sidebar create(Component title) {
        return new Sidebar(title);
    }

    /// Displays this sidebar to a player, replacing any sidebar or objective
    /// previously displayed in [DisplaySlot#SIDEBAR].
    ///
    /// @param player the viewer to add
    /// @return true if the player was not already viewing this sidebar
    @Override
    public boolean addViewer(Player player) {
        if (isViewer(player)) return false;
        player.setDisplayedObjective(DisplaySlot.SIDEBAR, objective);
        return true;
    }

    /// Hides this sidebar from a player. Does nothing if the player currently
    /// displays another objective in [DisplaySlot#SIDEBAR].
    ///
    /// @param player the viewer to remove
    /// @return true if the player was viewing this sidebar
    @Override
    public boolean removeViewer(Player player) {
        if (!isViewer(player)) return false;
        player.setDisplayedObjective(DisplaySlot.SIDEBAR, null);
        return true;
    }

    /// Gets all players currently shown this sidebar. Players displaying the
    /// [backing objective][#getObjective()] in a slot other than [DisplaySlot#SIDEBAR]
    /// are not included.
    ///
    /// @return an unmodifiable copy of the viewers
    @Override
    public Set<Player> getViewers() {
        Set<Player> viewers = new HashSet<>();
        for (Player player : objective.getViewers()) {
            if (isViewer(player)) viewers.add(player);
        }
        return Set.copyOf(viewers);
    }

    /// Gets if a player is currently shown this sidebar.
    ///
    /// @param player the player
    /// @return true if the player views this sidebar
    @Override
    public boolean isViewer(Player player) {
        return player.getDisplayedObjective(DisplaySlot.SIDEBAR) == objective;
    }

    /// Gets the title of the sidebar.
    ///
    /// @return the title
    public Component getTitle() {
        return objective.getDisplayName();
    }

    /// Sets the title of the sidebar.
    ///
    /// @param title the new title
    public void setTitle(Component title) {
        objective.setDisplayName(title);
    }

    /// Gets the current lines, ordered top to bottom.
    ///
    /// @return an immutable copy of the lines
    public synchronized @Unmodifiable List<Component> getLines() {
        return List.copyOf(lines);
    }

    /// Replaces all lines of the sidebar. Only lines whose content changed are sent to viewers.
    ///
    /// @param lines the new lines, ordered top to bottom
    /// @throws IllegalArgumentException if more than [#MAX_LINES] lines are given
    public void update(Component... lines) {
        update(List.of(lines));
    }

    /// Replaces all lines of the sidebar. Only lines whose content changed are sent to viewers.
    ///
    /// @param newLines the new lines, ordered top to bottom
    /// @throws IllegalArgumentException if more than [#MAX_LINES] lines are given
    public synchronized void update(List<Component> newLines) {
        Check.argCondition(newLines.size() > MAX_LINES,
                "A sidebar cannot have more than {0} lines", MAX_LINES);
        for (int i = 0; i < newLines.size(); i++) {
            final Component content = newLines.get(i);
            if (i < lines.size() && lines.get(i).equals(content)) continue;
            objective.updateEntry(LINE_PREFIX + i, new ScoreEntry(MAX_LINES - i, content, numberFormats[i]));
        }
        for (int i = newLines.size(); i < lines.size(); i++) {
            objective.removeEntry(LINE_PREFIX + i);
        }
        lines.clear();
        lines.addAll(newLines);
    }

    /// Updates the content of a single existing line.
    ///
    /// @param index   the line index, from 0 at the top
    /// @param content the new content
    /// @throws IllegalArgumentException if no line exists at `index`
    public synchronized void setLine(int index, Component content) {
        Check.argCondition(index < 0 || index >= lines.size(), "No line at index {0}", index);
        if (lines.get(index).equals(content)) return;
        lines.set(index, content);
        objective.updateEntry(LINE_PREFIX + index, new ScoreEntry(MAX_LINES - index, content, numberFormats[index]));
    }

    /// Gets the number format of a line index.
    ///
    /// @param index the line index, from 0 at the top
    /// @return the number format, or null if the line's score is hidden
    /// @throws IllegalArgumentException if `index` is not within [#MAX_LINES]
    public synchronized @Nullable NumberFormat getNumberFormat(int index) {
        Check.argCondition(index < 0 || index >= MAX_LINES, "No line at index {0}", index);
        return numberFormats[index];
    }

    /// Sets the number format of a line index, controlling how the line's score is displayed.
    /// The format is kept for the index across [#update(List)] calls, and may be set
    /// before a line exists at the index.
    ///
    /// @param index        the line index, from 0 at the top
    /// @param numberFormat the new number format, or null to hide the score
    /// @throws IllegalArgumentException if `index` is not within [#MAX_LINES]
    public synchronized void setNumberFormat(int index, @Nullable NumberFormat numberFormat) {
        Check.argCondition(index < 0 || index >= MAX_LINES, "No line at index {0}", index);
        if (Objects.equals(numberFormats[index], numberFormat)) return;
        numberFormats[index] = numberFormat;
        if (index < lines.size()) {
            objective.updateEntry(LINE_PREFIX + index, new ScoreEntry(MAX_LINES - index, lines.get(index), numberFormat));
        }
    }

    /// Gets the objective backing this sidebar, for advanced customization
    /// such as displaying it in a team color slot.
    ///
    /// @return the backing objective
    public Objective getObjective() {
        return objective;
    }
}
