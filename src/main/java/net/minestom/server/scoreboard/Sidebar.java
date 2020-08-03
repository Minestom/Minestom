package net.minestom.server.scoreboard;

import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import net.minestom.server.Viewable;
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

public class Sidebar implements Viewable {

    private static final AtomicInteger counter = new AtomicInteger();

    // WARNING: you shouldn't create scoreboards/teams with the same prefixes as those
    private static final String SCOREBOARD_PREFIX = "sb-";
    private static final String TEAM_PREFIX = "sbt-";

    // Limited by notchian client, do not change
    private static final int MAX_LINES_COUNT = 15;

    private Set<Player> viewers = new CopyOnWriteArraySet<>();

    private ConcurrentLinkedQueue<ScoreboardLine> lines = new ConcurrentLinkedQueue<>();
    private IntLinkedOpenHashSet availableColors = new IntLinkedOpenHashSet();

    private String objectiveName;

    private String title;

    public Sidebar(String title) {
        this.title = title;

        this.objectiveName = SCOREBOARD_PREFIX + counter.incrementAndGet();

        // Fill available colors for entities name showed in scoreboard
        for (int i = 0; i < 16; i++) {
            availableColors.add(i);
        }
    }

    public void setTitle(String title) {
        this.title = title;

        ScoreboardObjectivePacket scoreboardObjectivePacket = new ScoreboardObjectivePacket();
        scoreboardObjectivePacket.objectiveName = objectiveName;
        scoreboardObjectivePacket.mode = 2; // Update display text
        scoreboardObjectivePacket.objectiveValue = ColoredText.of(title);
        scoreboardObjectivePacket.type = 0;

        sendPacketToViewers(scoreboardObjectivePacket);
    }

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

    public void updateLineContent(String id, ColoredText content) {
        final ScoreboardLine scoreboardLine = getLine(id);
        if (scoreboardLine != null) {
            scoreboardLine.refreshContent(content);
            sendPacketToViewers(scoreboardLine.sidebarTeam.updatePrefix(content));
        }
    }

    public void updateLineScore(String id, int score) {
        final ScoreboardLine scoreboardLine = getLine(id);
        if (scoreboardLine != null) {
            scoreboardLine.line = score;
            sendPacketToViewers(scoreboardLine.getLineScoreUpdatePacket(objectiveName, score));
        }
    }

    public ScoreboardLine getLine(String id) {
        for (ScoreboardLine line : lines) {
            if (line.id.equals(id))
                return line;
        }
        return null;
    }

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

        ScoreboardObjectivePacket scoreboardObjectivePacket = new ScoreboardObjectivePacket();
        scoreboardObjectivePacket.objectiveName = objectiveName;
        scoreboardObjectivePacket.mode = 0; // Create scoreboard
        scoreboardObjectivePacket.objectiveValue = ColoredText.of(title);
        scoreboardObjectivePacket.type = 0; // Type integer

        DisplayScoreboardPacket displayScoreboardPacket = new DisplayScoreboardPacket();
        displayScoreboardPacket.position = 1; // Sidebar
        displayScoreboardPacket.scoreName = objectiveName;

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
        ScoreboardObjectivePacket scoreboardObjectivePacket = new ScoreboardObjectivePacket();
        scoreboardObjectivePacket.objectiveName = objectiveName;
        scoreboardObjectivePacket.mode = 1; // Remove
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

    public static class ScoreboardLine {

        private String id; // ID used to modify the line later
        private ColoredText content;
        private int line;

        private String teamName;
        private int colorName; // Name of the score (entityName) which is essentially an ID
        private String entityName;
        private SidebarTeam sidebarTeam;

        public ScoreboardLine(String id, ColoredText content, int line) {
            this.id = id;
            this.content = content;
            this.line = line;

            this.teamName = TEAM_PREFIX + counter.incrementAndGet();
        }

        public String getId() {
            return id;
        }

        public ColoredText getContent() {
            return sidebarTeam == null ? content : sidebarTeam.getPrefix();
        }

        public int getLine() {
            return line;
        }

        private void retrieveName(IntLinkedOpenHashSet colors) {
            synchronized (colors) {
                this.colorName = colors.removeFirstInt();
            }
        }

        private void createTeam() {
            this.entityName = ChatParser.COLOR_CHAR + Integer.toHexString(colorName);

            this.sidebarTeam = new SidebarTeam(teamName, content, ColoredText.of(""), entityName);
        }

        private void returnName(IntLinkedOpenHashSet colors) {
            synchronized (colors) {
                colors.add(colorName);
            }
        }

        private UpdateScorePacket getScoreCreationPacket(String objectiveName) {
            UpdateScorePacket updateScorePacket = new UpdateScorePacket();
            updateScorePacket.entityName = entityName;
            updateScorePacket.action = 0; // Create/Update
            updateScorePacket.objectiveName = objectiveName;
            updateScorePacket.value = line;
            return updateScorePacket;
        }

        private UpdateScorePacket getScoreDestructionPacket(String objectiveName) {
            UpdateScorePacket updateScorePacket = new UpdateScorePacket();
            updateScorePacket.entityName = entityName;
            updateScorePacket.action = 1; // Remove
            updateScorePacket.objectiveName = objectiveName;
            return updateScorePacket;
        }

        private UpdateScorePacket getLineScoreUpdatePacket(String objectiveName, int score) {
            UpdateScorePacket updateScorePacket = getScoreCreationPacket(objectiveName);
            updateScorePacket.value = score;
            return updateScorePacket;
        }

        private void refreshContent(ColoredText content) {
            this.sidebarTeam.refreshPrefix(content);
        }

    }

    private static class SidebarTeam {

        private String teamName;
        private ColoredText prefix, suffix;
        private String entityName;

        private ColoredText teamDisplayName = ColoredText.of("displaynametest");
        private byte friendlyFlags = 0x00;
        private TeamsPacket.NameTagVisibility nameTagVisibility = TeamsPacket.NameTagVisibility.NEVER;
        private TeamsPacket.CollisionRule collisionRule = TeamsPacket.CollisionRule.NEVER;
        private int teamColor = 2;


        private SidebarTeam(String teamName, ColoredText prefix, ColoredText suffix, String entityName) {
            this.teamName = teamName;
            this.prefix = prefix;
            this.suffix = suffix;
            this.entityName = entityName;
        }

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

        private TeamsPacket getDestructionPacket() {
            TeamsPacket teamsPacket = new TeamsPacket();
            teamsPacket.teamName = teamName;
            teamsPacket.action = TeamsPacket.Action.REMOVE_TEAM;
            return teamsPacket;
        }

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

        private String getEntityName() {
            return entityName;
        }

        private ColoredText getPrefix() {
            return prefix;
        }

        private void refreshPrefix(ColoredText prefix) {
            this.prefix = prefix;
        }
    }


}
