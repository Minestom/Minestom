package net.minestom.server.ui;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

final class PlayerUIImpl implements PlayerUI {

    // region [Constants]
    static final String MAGIC = "58D0F79F"; // https://xkcd.com/221/
    static final byte FRIENDLY_FLAGS = 0x00;
    static final TeamsPacket.NameTagVisibility NAME_TAG_VISIBILITY = TeamsPacket.NameTagVisibility.NEVER;
    static final TeamsPacket.CollisionRule COLLISION_RULE = TeamsPacket.CollisionRule.NEVER;
    static final NamedTextColor TEAM_COLOR = NamedTextColor.WHITE;
    // endregion

    private final MessagePassingQueue<ServerPacket> queue = new MpscUnboundedArrayQueue<>(32);

    private final SidebarHandler sidebarHandler = new SidebarHandler(queue);
    private final TabListHandler tabListHandler = new TabListHandler(queue);

    PlayerUIImpl() {
    }

    @Override
    public void sidebar(@Nullable SidebarUI sidebar) {
        this.sidebarHandler.handle(sidebar);
    }

    @Override
    public void tabList(TabListUI tabListUI) {
        this.tabListHandler.handle(tabListUI);
    }

    @Override
    public void drain(@NotNull Consumer<ServerPacket> consumer) {
        this.queue.drain(consumer::accept);
    }
}
