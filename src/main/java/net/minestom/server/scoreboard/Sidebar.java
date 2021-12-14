package net.minestom.server.scoreboard;

import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a sidebar which can contain up to 16 {@link ScoreboardLine}.
 * <p>
 * In order to use it you need to create a new instance using the constructor {@link #Sidebar(String)} and create new lines
 * with {@link #createLine(ScoreboardLine)}. You can then add a {@link Player} to the viewing list using {@link #addViewer(Player)}
 * and remove him later with {@link #removeViewer(Player)}.
 * <p>
 * Lines can be modified using their respective identifier using
 * {@link #updateLineContent(String, Component)} and {@link #updateLineScore(String, int)}.
 */
public class Sidebar implements Scoreboard {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    /**
     * <b>WARNING:</b> You should NOT create any scoreboards/teams with the same prefixes as those
     */
    private static final String SCOREBOARD_PREFIX = "sb-";
    private static final String TEAM_PREFIX = "sbt-";

    /**
     * Limited by the notch client, do not change
     */
    private static final int MAX_LINES_COUNT = 15;

    private final Set<Player> viewers = new CopyOnWriteArraySet<>();

    private final Set<ScoreboardLine> lines = new CopyOnWriteArraySet<>();
    private final IntLinkedOpenHashSet availableColors = new IntLinkedOpenHashSet();

    private final String objectiveName;

    private Component title;

    /**
     * Creates a new sidebar
     *
     * @param title The title of the sidebar
     * @deprecated Use {@link #Sidebar(Component)}
     */
    @Deprecated
    public Sidebar(@NotNull String title) {
        this(Component.text(title));
    }

    /**
     * Creates a new sidebar
     *
     * @param title The title of the sidebar
     */
    public Sidebar(@NotNull Component title) {
        this.title = title;

        this.objectiveName = SCOREBOARD_PREFIX + COUNTER.incrementAndGet();

        // Fill available colors for entities name showed in scoreboard
        for (int i = 0; i < 16; i++) {
            availableColors.add(i);
        }
    }

    /**
     * Changes the {@link Sidebar} title
     *
     * @param title The new sidebar title
     * @deprecated Use {@link #setTitle(Component)}
     */
    @Deprecated
    public void setTitle(@NotNull String title) {
        this.setTitle(Component.text(title));
    }

    /**
     * Changes the {@link Sidebar} title
     *
     * @param title The new sidebar title
     */
    public void setTitle(@NotNull Component title) {
        this.title = title;
        sendPacketToViewers(new ScoreboardObjectivePacket(objectiveName, (byte) 2, title,
                ScoreboardObjectivePacket.Type.INTEGER));
    }

    /**
     * Creates a new {@link ScoreboardLine}.
     *
     * @param scoreboardLine the new scoreboard line
     * @throws IllegalStateException    if the sidebar cannot take more line
     * @throws IllegalArgumentException if the sidebar already contains the line {@code scoreboardLine}
     *                                  or has a line with the same id
     */
    public void createLine(@NotNull ScoreboardLine scoreboardLine) {
        synchronized (lines) {
            Check.stateCondition(lines.size() >= MAX_LINES_COUNT, "You cannot have more than " + MAX_LINES_COUNT + "  lines");
            Check.argCondition(lines.contains(scoreboardLine), "You cannot add two times the same ScoreboardLine");

            // Check ID duplication
            for (ScoreboardLine line : lines) {
                Check.argCondition(line.id.equals(scoreboardLine.id),
                        "You cannot add two ScoreboardLine with the same id");
            }

            // Setup line
            scoreboardLine.retrieveName(availableColors);
            scoreboardLine.createTeam();

            // Finally add the line in cache
            this.lines.add(scoreboardLine);

            // Send to current viewers
            sendPacketsToViewers(scoreboardLine.sidebarTeam.getCreationPacket(), scoreboardLine.getScoreCreationPacket(objectiveName));
        }
    }

    /**
     * Updates a {@link ScoreboardLine} content through the given identifier.
     *
     * @param id      The identifier of the {@link ScoreboardLine}
     * @param content The new content for the {@link ScoreboardLine}
     */
    public void updateLineContent(@NotNull String id, @NotNull Component content) {
        final ScoreboardLine scoreboardLine = getLine(id);
        if (scoreboardLine != null) {
            scoreboardLine.refreshContent(content);
            sendPacketToViewers(scoreboardLine.sidebarTeam.updatePrefix(content));
        }
    }

    /**
     * Updates the score of a {@link ScoreboardLine} through the given identifier
     *
     * @param id    The identifier of the team
     * @param score The new score for the {@link ScoreboardLine}
     */
    public void updateLineScore(@NotNull String id, int score) {
        final ScoreboardLine scoreboardLine = getLine(id);
        if (scoreboardLine != null) {
            scoreboardLine.line = score;
            sendPacketToViewers(scoreboardLine.getLineScoreUpdatePacket(objectiveName, score));
        }
    }

    /**
     * Gets a {@link ScoreboardLine} through the given identifier
     *
     * @param id The identifier of the line
     * @return a {@link ScoreboardLine} or {@code null}
     */
    @Nullable
    public ScoreboardLine getLine(@NotNull String id) {
        for (ScoreboardLine line : lines) {
            if (line.id.equals(id))
                return line;
        }
        return null;
    }

    /**
     * Gets a {@link Set} containing all the registered lines.
     *
     * @return an unmodifiable set containing the sidebar's lines
     */
    @NotNull
    public Set<ScoreboardLine> getLines() {
        return Collections.unmodifiableSet(lines);
    }

    /**
     * Removes a {@link ScoreboardLine} through the given identifier
     *
     * @param id the identifier of the {@link ScoreboardLine}
     */
    public void removeLine(@NotNull String id) {
        this.lines.removeIf(line -> {
            if (line.id.equals(id)) {

                // Remove the line for current viewers
                sendPacketsToViewers(line.getScoreDestructionPacket(objectiveName), line.sidebarTeam.getDestructionPacket());

                line.returnName(availableColors);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean addViewer(@NotNull Player player) {
        final boolean result = this.viewers.add(player);
        PlayerConnection playerConnection = player.getPlayerConnection();

        ScoreboardObjectivePacket scoreboardObjectivePacket = this.getCreationObjectivePacket(this.title, ScoreboardObjectivePacket.Type.INTEGER);
        DisplayScoreboardPacket displayScoreboardPacket = this.getDisplayScoreboardPacket((byte) 1);

        playerConnection.sendPacket(scoreboardObjectivePacket); // Creative objective
        playerConnection.sendPacket(displayScoreboardPacket); // Show sidebar scoreboard (wait for scores packet)

        for (ScoreboardLine line : lines) {
            playerConnection.sendPacket(line.sidebarTeam.getCreationPacket());
            playerConnection.sendPacket(line.getScoreCreationPacket(objectiveName));
        }
        return result;
    }

    @Override
    public boolean removeViewer(@NotNull Player player) {
        final boolean result = this.viewers.remove(player);
        PlayerConnection playerConnection = player.getPlayerConnection();
        ScoreboardObjectivePacket scoreboardObjectivePacket = this.getDestructionObjectivePacket();
        playerConnection.sendPacket(scoreboardObjectivePacket);

        for (ScoreboardLine line : lines) {
            playerConnection.sendPacket(line.getScoreDestructionPacket(objectiveName)); // Is it necessary?
            playerConnection.sendPacket(line.sidebarTeam.getDestructionPacket());
        }
        return result;
    }

    @NotNull
    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    @Override
    public @NotNull String getObjectiveName() {
        return this.objectiveName;
    }

    /**
     * This class is used to create a line for the sidebar.
     */
    public static class ScoreboardLine {

        /**
         * The identifier is used to modify the line later
         */
        private final String id;
        /**
         * The content for the line
         */
        private final Component content;
        /**
         * The score of the line
         */
        private int line;

        private final String teamName;
        /**
         * The name of the score ({@code entityName}) which is essentially an identifier
         */
        private int colorName;
        private String entityName;
        /**
         * The sidebar team of the line
         */
        private SidebarTeam sidebarTeam;

        public ScoreboardLine(@NotNull String id, @NotNull Component content, int line) {
            this.id = id;
            this.content = content;
            this.line = line;

            this.teamName = TEAM_PREFIX + COUNTER.incrementAndGet();
        }

        /**
         * Gets the identifier of the line
         *
         * @return the line identifier
         */
        public @NotNull String getId() {
            return id;
        }

        /**
         * Gets the content of the line
         *
         * @return The line content
         */
        public @NotNull Component getContent() {
            return sidebarTeam == null ? content : sidebarTeam.getPrefix();
        }

        /**
         * Gets the position of the line
         *
         * @return the line position
         */
        public int getLine() {
            return line;
        }

        private void retrieveName(IntLinkedOpenHashSet colors) {
            synchronized (colors) {
                this.colorName = colors.removeFirstInt();
            }
        }

        /**
         * Creates a new {@link SidebarTeam}
         */
        private void createTeam() {
            this.entityName = ((char) 0xA7) + Integer.toHexString(colorName);

            this.sidebarTeam = new SidebarTeam(teamName, content, Component.empty(), entityName);
        }

        private void returnName(IntLinkedOpenHashSet colors) {
            synchronized (colors) {
                colors.add(colorName);
            }
        }

        /**
         * Gets a score creation packet
         *
         * @param objectiveName The objective name to be updated
         * @return a {@link UpdateScorePacket}
         */
        private UpdateScorePacket getScoreCreationPacket(String objectiveName) {
            return new UpdateScorePacket(entityName, (byte) 0, objectiveName, line);
        }

        /**
         * Gets a score destruction packet
         *
         * @param objectiveName The objective name to be destroyed
         * @return a {@link UpdateScorePacket}
         */
        private UpdateScorePacket getScoreDestructionPacket(String objectiveName) {
            return new UpdateScorePacket(entityName, (byte) 1, objectiveName, 0);
        }

        /**
         * Gets a line score update packet
         *
         * @param objectiveName The objective name to be updated
         * @param score         The new score
         * @return a {@link UpdateScorePacket}
         */
        private UpdateScorePacket getLineScoreUpdatePacket(String objectiveName, int score) {
            return new UpdateScorePacket(entityName, (byte) 0, objectiveName, score);
        }

        /**
         * Refresh the prefix of the {@link SidebarTeam}
         *
         * @param content The new content
         */
        private void refreshContent(Component content) {
            this.sidebarTeam.refreshPrefix(content);
        }

    }

    /**
     * This class is used to create a team for the {@link Sidebar}
     */
    private static class SidebarTeam {

        private final String teamName;
        private Component prefix, suffix;
        private final String entityName;

        private final Component teamDisplayName = Component.text("displaynametest");
        private final byte friendlyFlags = 0x00;
        private final TeamsPacket.NameTagVisibility nameTagVisibility = TeamsPacket.NameTagVisibility.NEVER;
        private final TeamsPacket.CollisionRule collisionRule = TeamsPacket.CollisionRule.NEVER;
        private final NamedTextColor teamColor = NamedTextColor.DARK_GREEN;


        /**
         * The constructor to creates a team
         *
         * @param teamName   The registry name of the team
         * @param prefix     The team prefix
         * @param suffix     The team suffix
         * @param entityName The team entity name
         */
        private SidebarTeam(String teamName, Component prefix, Component suffix, String entityName) {
            this.teamName = teamName;
            this.prefix = prefix;
            this.suffix = suffix;
            this.entityName = entityName;
        }

        /**
         * Gets a team creation packet
         *
         * @return a {@link TeamsPacket} which creates a new team
         */
        private TeamsPacket getCreationPacket() {
            final var action = new TeamsPacket.CreateTeamAction(teamDisplayName, friendlyFlags,
                    nameTagVisibility, collisionRule, teamColor, prefix, suffix, List.of(entityName));
            return new TeamsPacket(teamName, action);
        }

        /**
         * Gets a team destruction packet
         *
         * @return a {@link TeamsPacket} which destroyed a team
         */
        private TeamsPacket getDestructionPacket() {
            return new TeamsPacket(teamName, new TeamsPacket.RemoveTeamAction());
        }

        /**
         * Updates the prefix of the {@link SidebarTeam}
         *
         * @param prefix The new prefix
         * @return a {@link TeamsPacket} with the updated prefix
         */
        private TeamsPacket updatePrefix(Component prefix) {
            final var action = new TeamsPacket.UpdateTeamAction(teamDisplayName, friendlyFlags,
                    nameTagVisibility, collisionRule, teamColor, prefix, suffix);
            return new TeamsPacket(teamName, action);
        }

        /**
         * Gets the entity name of the team
         *
         * @return the entity name
         */
        private String getEntityName() {
            return entityName;
        }

        /**
         * Gets the prefix of the team
         *
         * @return the prefix
         */
        private Component getPrefix() {
            return prefix;
        }

        /**
         * Refresh the prefix of the {@link SidebarTeam}
         *
         * @param prefix The refreshed prefix
         */
        private void refreshPrefix(@NotNull Component prefix) {
            this.prefix = prefix;
        }
    }
}
