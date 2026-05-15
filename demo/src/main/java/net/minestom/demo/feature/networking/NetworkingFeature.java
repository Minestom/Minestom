package net.minestom.demo.feature.networking;

import net.kyori.adventure.text.Component;
import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.network.packet.server.common.CustomReportDetailsPacket;
import net.minestom.server.network.packet.server.common.ServerLinksPacket;

import java.util.Map;

/** {@code /config} command plus server links and custom report details on spawn. */
public final class NetworkingFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(new ConfigCommand());

        process.eventHandler().addListener(PlayerSpawnEvent.class, event -> {
            var player = event.getPlayer();
            player.sendPacket(new CustomReportDetailsPacket(Map.of("hello", "world")));
            player.sendPacket(new ServerLinksPacket(
                    new ServerLinksPacket.Entry(ServerLinksPacket.KnownLinkType.NEWS, "https://minestom.net"),
                    new ServerLinksPacket.Entry(ServerLinksPacket.KnownLinkType.BUG_REPORT, "https://minestom.net"),
                    new ServerLinksPacket.Entry(Component.text("Hello world!"), "https://minestom.net")
            ));
        });
    }
}
