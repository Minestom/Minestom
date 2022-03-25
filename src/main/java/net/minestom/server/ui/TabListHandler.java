package net.minestom.server.ui;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import org.jctools.queues.MessagePassingQueue;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static net.minestom.server.ui.PlayerUIImpl.*;

final class TabListHandler {
    static final char TAB_LIST_BEFORE = '!';
    static final char TAB_LIST_AFTER = '~';
    static final String TAB_LIST_AFTER_TEAM_PREFIX = "\u9999" + MAGIC;

    private final MessagePassingQueue<ServerPacket> queue;

    private boolean tabListAfterTeamCreated = false;

    private Component header = Component.empty();
    private Component footer = Component.empty();

    private final IntList textHashCodesBefore = new IntArrayList();
    private final IntList skinHashCodesBefore = new IntArrayList();
    private final IntList textHashCodesAfter = new IntArrayList();
    private final IntList skinHashCodesAfter = new IntArrayList();

    TabListHandler(MessagePassingQueue<ServerPacket> queue) {
        this.queue = queue;
    }

    public void handle(TabListUI tabList) {
        if (tabList == null) {
            updatePlayerList(Collections.emptyList(), Collections.emptyList(), textHashCodesBefore, skinHashCodesBefore, -1);
            updatePlayerList(Collections.emptyList(), Collections.emptyList(), textHashCodesAfter, skinHashCodesAfter, 1);

            header = Component.empty();
            footer = Component.empty();
            setHeaderAndFooter(header, footer);
        } else if (tabList instanceof TabListUIImpl impl) {
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
