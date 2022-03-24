package net.minestom.server.ui;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PlayerUI {

    private static final Cache<Player, PlayerUI> playerUICache = Caffeine.newBuilder()
            .weakKeys().build();

    public static PlayerUI forPlayer(Player player) {
        return playerUICache.get(player, PlayerUIImpl::new);
    }

    // region [Implementation]

    // Scoreboard
    protected abstract void createScoreboardObjective(Component title);
    protected abstract void destroyScoreboardObjective();
    protected abstract void createScoreboardLine(int index, Component line);
    protected abstract void updateScoreboardLine(int index, Component line);
    protected abstract void removeScoreboardLine(int index);

    // Tab List
    protected abstract void addTabListEntry(int index, Component text, PlayerSkin skin);
    protected abstract void updateTabListEntry(int index, Component text);
    protected abstract void removeTabListEntry(int index);
    protected abstract void setHeaderAndFooter(Component header, Component footer);

    // endregion

    // region [Scoreboard]

    /**
     * Limited by the notchian client, do not change
     */
    private static final int MAX_LINES_COUNT = 15;

    private final int[] scoreboard = new int[MAX_LINES_COUNT + 1];

    public boolean scoreboard(@Nullable Component title, Component... lines) {
        if (title == null) {
            if (scoreboard[0] == -1) return false;
            scoreboard[0] = -1;
            destroyScoreboardObjective();
            return true;
        }

        boolean changed = setScoreboardLine(0, title);

        int index = 0;
        for (; index < lines.length && index < scoreboard.length-1; index++) {
            Component line = lines[index];
            changed |= setScoreboardLine(index+1, line);
        }

        for (; index < scoreboard.length-1; index++) {
            changed |= setScoreboardLine(index+1, null);
        }

        return changed;
    }

    private boolean setScoreboardLine(int index, @Nullable Component line) {
        int oldLine = scoreboard[index];

        if (line == null) {
            if (index == 0) return false;
            if (oldLine == -1) return false;
            scoreboard[index] = -1;
            removeScoreboardLine(index);
            return true;
        }

        int lineHashCode = line.hashCode();
        if (oldLine == -1) {
            scoreboard[index] = lineHashCode;

            if (index == 0) {
                createScoreboardObjective(line);
            } else {
                createScoreboardLine(index, line);
            }
            return true;
        } else if (oldLine != lineHashCode) {
            updateScoreboardLine(index, line);
            scoreboard[index] = lineHashCode;
            return true;
        }

        return false;
    }

    // endregion [Scoreboard]

    // region [Tab List]

    private Component header = Component.empty();
    private Component footer = Component.empty();

    private final IntList textHashBefore = new IntArrayList();
    private final IntList skinHashBefore = new IntArrayList();
    private final IntList textHashAfter = new IntArrayList();
    private final IntList skinHashAfter = new IntArrayList();

    public void tabList(TabListBuilder tabListBuilder) {
        tabList(tabListBuilder.build());
    }

    public void tabList(TabList tabList) {
        if (tabList instanceof TabListImpl impl) {
            if (impl.hasPlayerList()) {
                playerListInternal(impl.beforeText(), impl.beforeSkin(), textHashBefore, skinHashBefore, -1);
                playerListInternal(impl.afterText(), impl.afterSkin(), textHashAfter, skinHashAfter, 1);
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

    private boolean playerListInternal(List<Component> lines, List<PlayerSkin> skins,
                                       IntList textHashCodeList, IntList skinHashCodeList, int indexMultiplier) {
        boolean modified = false;

        assert lines.size() == skins.size();
        assert textHashCodeList.size() == skinHashCodeList.size();

        int index = 0;
        for (; index < lines.size(); index++) {
            Component line = lines.get(index);
            PlayerSkin skin = skins.get(index);

            int lineHashCode = line.hashCode();
            int skinHashCode = skin.hashCode();

            if (index >= textHashCodeList.size()) {
                modified = true;
                addTabListEntry((index+1)*indexMultiplier, line, skin);
                textHashCodeList.add(lineHashCode);
                skinHashCodeList.add(skinHashCode);
            } else {
                if (skinHashCodeList.getInt(index) != skinHashCode) {
                    modified = true;
                    addTabListEntry((index+1)*indexMultiplier, line, skin);
                    textHashCodeList.set(index, lineHashCode);
                    skinHashCodeList.set(index, skinHashCode);
                } else if (textHashCodeList.getInt(index) != lineHashCode) {
                    modified = true;
                    updateTabListEntry((index+1)*indexMultiplier, line);
                    textHashCodeList.set(index, lineHashCode);
                }
            }
        }

        int beforeSize = textHashCodeList.size();
        for (; index < beforeSize; index++) {
            modified = true;
            removeTabListEntry((index+1)*indexMultiplier);
            textHashCodeList.removeInt(textHashCodeList.size()-1);
            skinHashCodeList.removeInt(skinHashCodeList.size()-1);
        }

        return modified;
    }

    // endregion [Tab List]

}
