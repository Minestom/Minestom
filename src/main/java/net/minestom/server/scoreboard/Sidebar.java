package net.minestom.server.scoreboard;

import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import net.minestom.server.chat.ChatParser;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.validate.Check;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a sidebar which can contain up to 16 {@link ScoreboardLine}'s
 */
public class Sidebar implements Scoreboard {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    /**
     * <b>WARNING:</b> You shouldn't create scoreboards/teams with the same prefixes as those
     */
    private static final String SCOREBOARD_PREFIX = "sb-";
    private static final String TEAM_PREFIX = "sbt-";

    /**
     * Limited by the notch client, do not change
     */
    private static final int MAX_LINES_COUNT = 15;

    private final Set<Player> viewers = new CopyOnWriteArraySet<>();

    private final ConcurrentLinkedQueue<ScoreboardLine> lines = new ConcurrentLinkedQueue<>();
    private final IntLinkedOpenHashSet availableColors = new IntLinkedOpenHashSet();

    private final String objectiveName;

    private String title;

    /**
     * Creates a new sidebar
     *
     * @param title The title of the sidebar
     */
    public Sidebar(String title) {
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
     */
    public void setTitle(String title) {
        this.title = title;

        ScoreboardObjectivePacket scoreboardObjectivePacket = new ScoreboardObjectivePacket();
        scoreboardObjectivePacket.objectiveName = objectiveName;
        scoreboardObjectivePacket.mode = 2; // Update display text
        scoreboardObjectivePacket.objectiveValue = ColoredText.of(title);
        scoreboardObjectivePacket.type = ScoreboardObjectivePacket.Type.INTEGER;

        sendPacketToViewers(scoreboardObjectivePacket);
    }

    /**
     * Creates a new {@link ScoreboardLine}
     *
     * @param scoreboardLine The new scoreboard line
     */
    public void createLine(ScoreboardLine scoreboardLine) {
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
     * Updates a line content through the given identifier
     *
     * @param id      The identifier of the line
     * @param content The new content for the line
     */
    public void updateLineContent(String id, ColoredText content) {
        final ScoreboardLine scoreboardLine = getLine(id);
        if (scoreboardLine != null) {
            scoreboardLine.refreshContent(content);
            sendPacketToViewers(scoreboardLine.sidebarTeam.updatePrefix(content));
        }
    }

    /**
     * Updates the score of a line through the given identifier
     *
     * @param id    The identifier of the team
     * @param score The new score for the line
     */
    public void updateLineScore(String id, int score) {
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
    public ScoreboardLine getLine(String id) {
        for (ScoreboardLine line : lines) {
            if (line.id.equals(id))
                return line;
        }
        return null;
    }

    /**
     * Removes a {@link ScoreboardLine} through the given identifier
     *
     * @param id The identifier of the line
     */
    public void removeLine(String id) {
        synchronized (lines) {
            Iterator<ScoreboardLine> iterator = lines.iterator();
            while (iterator.hasNext()) {
                final ScoreboardLine line = iterator.next();
                if (line.id.equals(id)) {

                    // Remove the line for current viewers
                    sendPacketsToViewers(line.getScoreCreationPacket(objectiveName), line.sidebarTeam.getDestructionPacket());

                    line.returnName(availableColors);
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public boolean addViewer(Player player) {
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
    public boolean removeViewer(Player player) {
        boolean result = this.viewers.remove(player);
        PlayerConnection playerConnection = player.getPlayerConnection();
        ScoreboardObjectivePacket scoreboardObjectivePacket = this.getDestructionObjectivePacket();
        playerConnection.sendPacket(scoreboardObjectivePacket);

        for (ScoreboardLine line : lines) {
            playerConnection.sendPacket(line.getScoreDestructionPacket(objectiveName)); // Is it necessary?
            playerConnection.sendPacket(line.sidebarTeam.getDestructionPacket());
        }
        return result;
    }

    @Override
    public Set<Player> getViewers() {
        return viewers;
    }

    @Override
    public String getObjectiveName() {
        return this.objectiveName;
    }

    /**
     * This class is used to create a line for the sidebar.
     */
    public static class ScoreboardLine {

        /**
         * The identifier is used to modify the line later
         */
        private String id;
        /**
         * The content for the line
         */
        private ColoredText content;
        /**
         * The score of the line
         */
        private int line;

        private String teamName;
        /**
         * The name of the score ({@code entityName}) which is essentially an identifier
         */
        private int colorName;
        private String entityName;
        /**
         * The sidebar team of the line
         */
        private SidebarTeam sidebarTeam;

        public ScoreboardLine(String id, ColoredText content, int line) {
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
        public String getId() {
            return id;
        }

        /**
         * Gets the content of the line
         *
         * @return The line content
         */
        public ColoredText getContent() {
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
            this.entityName = ChatParser.COLOR_CHAR + Integer.toHexString(colorName);

            this.sidebarTeam = new SidebarTeam(teamName, content, ColoredText.of(""), entityName);
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
            UpdateScorePacket updateScorePacket = new UpdateScorePacket();
            updateScorePacket.entityName = entityName;
            updateScorePacket.action = 0; // Create/Update
            updateScorePacket.objectiveName = objectiveName;
            updateScorePacket.value = line;
            return updateScorePacket;
        }

        /**
         * Gets a score destruction packet
         *
         * @param objectiveName The objective name to be destroyed
         * @return a {@link UpdateScorePacket}
         */
        private UpdateScorePacket getScoreDestructionPacket(String objectiveName) {
            UpdateScorePacket updateScorePacket = new UpdateScorePacket();
            updateScorePacket.entityName = entityName;
            updateScorePacket.action = 1; // Remove
            updateScorePacket.objectiveName = objectiveName;
            return updateScorePacket;
        }

        /**
         * Gets a line score update packet
         *
         * @param objectiveName The objective name to be updated
         * @param score         The new score
         * @return a {@link UpdateScorePacket}
         */
        private UpdateScorePacket getLineScoreUpdatePacket(String objectiveName, int score) {
            UpdateScorePacket updateScorePacket = getScoreCreationPacket(objectiveName);
            updateScorePacket.value = score;
            return updateScorePacket;
        }

        /**
         * Refresh the prefix of the {@link SidebarTeam}
         *
         * @param content The new content
         */
        private void refreshContent(ColoredText content) {
            this.sidebarTeam.refreshPrefix(content);
        }

    }

    /**
     * This class is used to create a team for the sidebar
     */
    private static class SidebarTeam {

        private String teamName;
        private ColoredText prefix, suffix;
        private String entityName;

        private ColoredText teamDisplayName = ColoredText.of("displaynametest");
        private byte friendlyFlags = 0x00;
        private TeamsPacket.NameTagVisibility nameTagVisibility = TeamsPacket.NameTagVisibility.NEVER;
        private TeamsPacket.CollisionRule collisionRule = TeamsPacket.CollisionRule.NEVER;
        private int teamColor = 2;


        /**
         * The constructor to creates a team
         *
         * @param teamName   The registry name of the team
         * @param prefix     The team prefix
         * @param suffix     The team suffix
         * @param entityName The team entity name
         */
        private SidebarTeam(String teamName, ColoredText prefix, ColoredText suffix, String entityName) {
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
            TeamsPacket teamsPacket = new TeamsPacket();
            teamsPacket.teamName = teamName;
            teamsPacket.action = TeamsPacket.Action.CREATE_TEAM;
            teamsPacket.teamDisplayName = teamDisplayName.toString();
            teamsPacket.friendlyFlags = friendlyFlags;
            teamsPacket.nameTagVisibility = nameTagVisibility;
            teamsPacket.collisionRule = collisionRule;
            teamsPacket.teamColor = teamColor;
            teamsPacket.teamPrefix = prefix.toString();
            teamsPacket.teamSuffix = suffix.toString();
            teamsPacket.entities = new String[]{entityName};
            return teamsPacket;
        }

        /**
         * Gets a team destruction packet
         *
         * @return a {@link TeamsPacket} which destroyed a team
         */
        private TeamsPacket getDestructionPacket() {
            TeamsPacket teamsPacket = new TeamsPacket();
            teamsPacket.teamName = teamName;
            teamsPacket.action = TeamsPacket.Action.REMOVE_TEAM;
            return teamsPacket;
        }

        /**
         * Updates the prefix of the {@link SidebarTeam}
         *
         * @param prefix The new prefix
         * @return a {@link TeamsPacket} with the updated prefix
         */
        private TeamsPacket updatePrefix(ColoredText prefix) {
            TeamsPacket teamsPacket = new TeamsPacket();
            teamsPacket.teamName = teamName;
            teamsPacket.action = TeamsPacket.Action.UPDATE_TEAM_INFO;
            teamsPacket.teamDisplayName = teamDisplayName.toString();
            teamsPacket.friendlyFlags = friendlyFlags;
            teamsPacket.nameTagVisibility = nameTagVisibility;
            teamsPacket.collisionRule = collisionRule;
            teamsPacket.teamColor = teamColor;
            teamsPacket.teamPrefix = prefix.toString();
            teamsPacket.teamSuffix = suffix.toString();
            return teamsPacket;
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
        private ColoredText getPrefix() {
            return prefix;
        }

        /**
         * Refresh the prefix of the {@link SidebarTeam}
         *
         * @param prefix The refreshed prefix
         */
        private void refreshPrefix(ColoredText prefix) {
            this.prefix = prefix;
        }
    }


}
