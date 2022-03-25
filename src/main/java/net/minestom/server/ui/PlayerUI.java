package net.minestom.server.ui;

import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public sealed interface PlayerUI permits PlayerUIImpl {

    static PlayerUI newPlayerUI() {
        return new PlayerUIImpl();
    }

    boolean sidebar(@Nullable SidebarUI sidebar);

    boolean tabList(@Nullable TabList tabList);

    void drain(@NotNull Consumer<ServerPacket> consumer);

}
