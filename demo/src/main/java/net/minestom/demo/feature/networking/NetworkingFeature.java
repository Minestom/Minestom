package net.minestom.demo.feature.networking;

import net.kyori.adventure.text.Component;
import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.network.packet.server.common.CustomReportDetailsPacket;
import net.minestom.server.network.packet.server.common.ServerLinksPacket;

import java.util.Map;

/**
 * Connection / config-phase showcase:
 * <ul>
 *   <li>{@code /config} command (sends the player back to the config phase).</li>
 *   <li>On first spawn, ships a {@link CustomReportDetailsPacket} with a
 *       single key/value plus a {@link ServerLinksPacket} with a NEWS link,
 *       a BUG_REPORT link, and a custom component link.</li>
 * </ul>
 */
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
