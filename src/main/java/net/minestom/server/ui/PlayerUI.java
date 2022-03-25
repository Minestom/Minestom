package net.minestom.server.ui;

import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public sealed interface PlayerUI permits PlayerUIImpl {

    static PlayerUI newPlayerUI() {
        return new PlayerUIImpl();
    }

    void sidebar(@Nullable SidebarUI sidebar);

    void tabList(@Nullable TabListUI tabListUI);

    void drain(@NotNull Consumer<ServerPacket> consumer);

}
