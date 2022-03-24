package net.minestom.server.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.packet.server.play.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlayerUIImpl extends PlayerUI implements Serializable {

    private static final String MAGIC = "58D0F79F"; // https://xkcd.com/221/

    // region [Scoreboard Constants]
    private static final String OBJECTIVE_NAME = MAGIC+"_objective";
    private static final byte OBJECTIVE_POSITION = (byte) 1; // Sidebar
    private static final String TEAM_NAME = MAGIC+"_team";
    private static final Component TEAM_DISPLAY_NAME = Component.text(MAGIC+"_name");
    private static final byte FRIENDLY_FLAGS = 0x00;
    private static final TeamsPacket.NameTagVisibility NAME_TAG_VISIBILITY = TeamsPacket.NameTagVisibility.NEVER;
    private static final TeamsPacket.CollisionRule COLLISION_RULE = TeamsPacket.CollisionRule.NEVER;
    private static final NamedTextColor TEAM_COLOR = NamedTextColor.WHITE;
    private static final List<String> ENTITY_NAMES;
    static {
        String[] names = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
        for (int i=0; i<names.length; i++) {
            names[i] = "\u00A7"+names[i]+"\u00A7r";
        }
        ENTITY_NAMES = List.of(names);
    }
    // endregion

    private final Player player;

    public PlayerUIImpl(Player player) {
        this.player = player;
    }

    protected void createScoreboardObjective(Component title) {
        var scoreboardObjectivePacket = new ScoreboardObjectivePacket(OBJECTIVE_NAME, (byte) 0,
                title, ScoreboardObjectivePacket.Type.INTEGER);

        var displayScoreboardPacket = new DisplayScoreboardPacket(OBJECTIVE_POSITION, OBJECTIVE_NAME);

        player.sendPacket(scoreboardObjectivePacket); // Create objective
        player.sendPacket(displayScoreboardPacket); // Show sidebar scoreboard
    }

    protected void destroyScoreboardObjective() {
        var scoreboardObjectivePacket = new ScoreboardObjectivePacket(OBJECTIVE_NAME, (byte) 1, null, null);
        var displayScoreboardPacket = new DisplayScoreboardPacket(OBJECTIVE_POSITION, OBJECTIVE_NAME);

        player.sendPacket(scoreboardObjectivePacket); // Creative objective
        player.sendPacket(displayScoreboardPacket); // Show sidebar scoreboard (wait for scores packet)
    }

    @Override
    protected void updateScoreboardLine(int index, Component line) {
        if (index <= 0) {
            var packet = new ScoreboardObjectivePacket(OBJECTIVE_NAME, (byte) 2,
                    line, ScoreboardObjectivePacket.Type.INTEGER);
            player.sendPacket(packet);
        } else {
            index--;

            final var action = new TeamsPacket.UpdateTeamAction(TEAM_DISPLAY_NAME, FRIENDLY_FLAGS,
                    NAME_TAG_VISIBILITY, COLLISION_RULE, TEAM_COLOR, line, Component.empty());
            sendTeamPacket(index, action);
        }

    }

    @Override
    protected void createScoreboardLine(int index, Component line) {
        if (index <= 0) {
            // Don't need to create title
        } else {
            index--;

            String entityName = ENTITY_NAMES.get(index);

            final var action = new TeamsPacket.CreateTeamAction(TEAM_DISPLAY_NAME, FRIENDLY_FLAGS,
                    NAME_TAG_VISIBILITY, COLLISION_RULE, TEAM_COLOR, line, Component.empty(),
                    List.of(entityName));
            sendTeamPacket(index, action);
            player.sendPacket(new UpdateScorePacket(entityName, (byte) 0, OBJECTIVE_NAME, 0));
        }
    }

    @Override
    protected void removeScoreboardLine(int index) {
        if (index <= 0) {
            // Don't need to remove title
        } else {
            index--;

            player.sendPacket(new UpdateScorePacket(ENTITY_NAMES.get(index), (byte) 1, OBJECTIVE_NAME, 0));
        }
    }

    private void sendTeamPacket(int index, TeamsPacket.Action action) {
        player.sendPacket(new TeamsPacket(TEAM_NAME+"_"+index, action));
    }

    private boolean createdTeam = false;
    private static char BEFORE = '!';
    private static char AFTER = '~';
    private static final String TAB_LIST_AFTER_TEAM = "\u9999"+MAGIC;

    @Override
    protected void addTabListEntry(int index, Component text, PlayerSkin skin) {
        String name = (index < 0 ? BEFORE : AFTER) + Integer.toHexString(Math.abs(index)+0x10000000).substring(1);

        List<PlayerInfoPacket.AddPlayer.Property> prop = skin != null ?
                List.of(new PlayerInfoPacket.AddPlayer.Property("textures", skin.textures(), skin.signature())) :
                Collections.emptyList();
        var packet = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER,
                new PlayerInfoPacket.AddPlayer(new UUID(index, 0), name, prop,
                        GameMode.CREATIVE, 0, text));
        player.sendPacket(packet);

        if (index >= 0) {
            if (!createdTeam) {
                final var action = new TeamsPacket.CreateTeamAction(Component.empty(), FRIENDLY_FLAGS,
                        NAME_TAG_VISIBILITY, COLLISION_RULE, TEAM_COLOR, Component.empty(), Component.empty(),
                        List.of(name));
                player.sendPacket(new TeamsPacket(TAB_LIST_AFTER_TEAM, action));
                createdTeam = true;
            } else {
                final var action = new TeamsPacket.AddEntitiesToTeamAction(List.of(name));
                player.sendPacket(new TeamsPacket(TAB_LIST_AFTER_TEAM, action));
            }
        }
    }

    @Override
    protected void updateTabListEntry(int index, Component text) {
        var packet = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_DISPLAY_NAME,
                new PlayerInfoPacket.UpdateDisplayName(new UUID(index, 0), text));
        player.sendPacket(packet);
    }

    @Override
    protected void removeTabListEntry(int index) {
        var packet = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER,
                new PlayerInfoPacket.RemovePlayer(new UUID(index, 0)));
        player.sendPacket(packet);
    }

    @Override
    protected void setHeaderAndFooter(Component header, Component footer) {
        player.sendPlayerListHeaderAndFooter(header, footer);
    }

}
