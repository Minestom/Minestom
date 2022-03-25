package net.minestom.server.ui;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.*;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

final class PlayerUIImpl implements PlayerUI {

    // region [Constants]
    /**
     * Limited by the notchian client, do not change
     */
    private static final int MAX_LINES_COUNT = 15;

    private static final String MAGIC = "58D0F79F"; // https://xkcd.com/221/
    private static final String OBJECTIVE_NAME = MAGIC + "_objective";
    private static final byte OBJECTIVE_POSITION = (byte) 1; // Sidebar
    private static final String TEAM_NAME = MAGIC + "_team";
    private static final Component TEAM_DISPLAY_NAME = Component.text(MAGIC + "_name");
    private static final byte FRIENDLY_FLAGS = 0x00;
    private static final TeamsPacket.NameTagVisibility NAME_TAG_VISIBILITY = TeamsPacket.NameTagVisibility.NEVER;
    private static final TeamsPacket.CollisionRule COLLISION_RULE = TeamsPacket.CollisionRule.NEVER;
    private static final NamedTextColor TEAM_COLOR = NamedTextColor.WHITE;
    private static final List<String> ENTITY_NAMES;

    static {
        String[] names = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
        for (int i = 0; i < names.length; i++) {
            names[i] = "\u00A7" + names[i] + "\u00A7r";
        }
        ENTITY_NAMES = List.of(names);
    }

    private static final char TAB_LIST_BEFORE = '!';
    private static final char TAB_LIST_AFTER = '~';
    private static final String TAB_LIST_AFTER_TEAM_PREFIX = "\u9999" + MAGIC;
    // endregion

    private final MessagePassingQueue<ServerPacket> queue = new MpscUnboundedArrayQueue<>(32);

    private final int[] scoreboardHashCodes = new int[MAX_LINES_COUNT + 1];

    private boolean tabListAfterTeamCreated = false;

    private Component header = Component.empty();
    private Component footer = Component.empty();

    private final IntList textHashCodesBefore = new IntArrayList();
    private final IntList skinHashCodesBefore = new IntArrayList();
    private final IntList textHashCodesAfter = new IntArrayList();
    private final IntList skinHashCodesAfter = new IntArrayList();

    public PlayerUIImpl() {
        Arrays.fill(scoreboardHashCodes, -1);
    }

    @Override
    public void sidebar(@Nullable SidebarUI sidebar) {
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

    @Override
    public void tabList(TabList tabList) {
        if (tabList == null) {
            updatePlayerList(Collections.emptyList(), Collections.emptyList(), textHashCodesBefore, skinHashCodesBefore, -1);
            updatePlayerList(Collections.emptyList(), Collections.emptyList(), textHashCodesAfter, skinHashCodesAfter, 1);

            header = Component.empty();
            footer = Component.empty();
            setHeaderAndFooter(header, footer);
        } else if (tabList instanceof TabListImpl impl) {
            if (impl.hasPlayerList()) {
                updatePlayerList(impl.beforeText(), impl.beforeSkin(), textHashCodesBefore, skinHashCodesBefore, -1);
                updatePlayerList(impl.afterText(), impl.afterSkin(), textHashCodesAfter, skinHashCodesAfter, 1);
            }

            boolean headerFooterChanged = false;

            if (impl.header() != null) {
                if (!header.equals(impl.header())) {
                    headerFooterChanged = true;
                    header = impl.header();
                }
            }

            if (impl.footer() != null) {
                if (!footer.equals(impl.footer())) {
                    headerFooterChanged = true;
                    footer = impl.footer();
                }
            }

            if (headerFooterChanged) {
                setHeaderAndFooter(header, footer);
            }
        }
    }

    @Override
    public void drain(@NotNull Consumer<ServerPacket> consumer) {
        this.queue.drain(consumer::accept);
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

    private void updatePlayerList(List<Component> lines, List<PlayerSkin> skins,
                                     IntList textHashCodeList, IntList skinHashCodeList, int indexMultiplier) {
        assert lines.size() == skins.size();
        assert textHashCodeList.size() == skinHashCodeList.size();

        int index = 0;
        for (; index < lines.size(); index++) {
            Component line = lines.get(index);
            PlayerSkin skin = skins.get(index);

            int lineHashCode = line.hashCode();
            int skinHashCode = skin.hashCode();

            if (index >= textHashCodeList.size()) {
                addTabListEntry((index + 1) * indexMultiplier, line, skin);
                textHashCodeList.add(lineHashCode);
                skinHashCodeList.add(skinHashCode);
            } else {
                if (skinHashCodeList.getInt(index) != skinHashCode) {
                    addTabListEntry((index + 1) * indexMultiplier, line, skin);
                    textHashCodeList.set(index, lineHashCode);
                    skinHashCodeList.set(index, skinHashCode);
                } else if (textHashCodeList.getInt(index) != lineHashCode) {
                    updateTabListEntry((index + 1) * indexMultiplier, line);
                    textHashCodeList.set(index, lineHashCode);
                }
            }
        }

        int beforeSize = textHashCodeList.size();
        for (; index < beforeSize; index++) {
            removeTabListEntry((index + 1) * indexMultiplier);
            textHashCodeList.removeInt(textHashCodeList.size() - 1);
            skinHashCodeList.removeInt(skinHashCodeList.size() - 1);
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

    private void addTabListEntry(int index, Component text, PlayerSkin skin) {
        String name = (index < 0 ? TAB_LIST_BEFORE : TAB_LIST_AFTER) + Integer.toHexString(Math.abs(index) + 0x10000000).substring(1);

        List<PlayerInfoPacket.AddPlayer.Property> prop = skin != null ?
                List.of(new PlayerInfoPacket.AddPlayer.Property("textures", skin.textures(), skin.signature())) :
                Collections.emptyList();
        var packet = new PlayerInfoPacket(PlayerInfoPacket.Action.ADD_PLAYER,
                new PlayerInfoPacket.AddPlayer(new UUID(index, 0), name, prop,
                        GameMode.CREATIVE, 0, text));
        queue.offer(packet);

        if (index >= 0) {
            if (!tabListAfterTeamCreated) {
                final var action = new TeamsPacket.CreateTeamAction(Component.empty(), FRIENDLY_FLAGS,
                        NAME_TAG_VISIBILITY, COLLISION_RULE, TEAM_COLOR, Component.empty(), Component.empty(),
                        List.of(name));
                queue.offer(new TeamsPacket(TAB_LIST_AFTER_TEAM_PREFIX, action));
                tabListAfterTeamCreated = true;
            } else {
                final var action = new TeamsPacket.AddEntitiesToTeamAction(List.of(name));
                queue.offer(new TeamsPacket(TAB_LIST_AFTER_TEAM_PREFIX, action));
            }
        }
    }

    private void updateTabListEntry(int index, Component text) {
        var packet = new PlayerInfoPacket(PlayerInfoPacket.Action.UPDATE_DISPLAY_NAME,
                new PlayerInfoPacket.UpdateDisplayName(new UUID(index, 0), text));
        queue.offer(packet);
    }

    private void removeTabListEntry(int index) {
        var packet = new PlayerInfoPacket(PlayerInfoPacket.Action.REMOVE_PLAYER,
                new PlayerInfoPacket.RemovePlayer(new UUID(index, 0)));
        queue.offer(packet);
    }

    private void setHeaderAndFooter(Component header, Component footer) {
        var packet = new PlayerListHeaderAndFooterPacket(header, footer);
        queue.offer(packet);
    }

}
