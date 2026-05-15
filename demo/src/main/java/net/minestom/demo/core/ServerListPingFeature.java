package net.minestom.demo.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.ServerProcess;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;

/**
 * Demonstrates {@link ServerListPingEvent}: custom MOTD, favicon,
 * sample/hover lines including remote address and protocol version.
 */
public final class ServerListPingFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        byte[] favicon = Favicon.bytes();

        process.eventHandler().addListener(ServerListPingEvent.class, event -> {
            Status.PlayerInfo.Builder builder = Status.PlayerInfo.builder(Status.PlayerInfo.online(20))
                    .sample("The first line is separated from the others")
                    .sample("Could be a name, or a message");

            if (event.getConnection() != null) {
                String ip = event.getConnection().getServerAddress();
                builder = builder
                        .sample("IP test: " + event.getConnection().getRemoteAddress())
                        .sample("Connection Info:")
                        .sample(Component.text('-', NamedTextColor.DARK_GRAY)
                                .append(Component.text(" IP: ", NamedTextColor.GRAY))
                                .append(Component.text(ip != null ? ip : "???", NamedTextColor.YELLOW)))
                        .sample(Component.text('-', NamedTextColor.DARK_GRAY)
                                .append(Component.text(" PORT: ", NamedTextColor.GRAY))
                                .append(Component.text(event.getConnection().getServerPort())))
                        .sample(Component.text('-', NamedTextColor.DARK_GRAY)
                                .append(Component.text(" VERSION: ", NamedTextColor.GRAY))
                                .append(Component.text(event.getConnection().getProtocolVersion())));
            }

            builder = builder
                    .sample(Component.text("Time", NamedTextColor.YELLOW)
                            .append(Component.text(": ", NamedTextColor.GRAY))
                            .append(Component.text(System.currentTimeMillis(), Style.style(TextDecoration.ITALIC))))
                    .sample(Component.text("You can use ").append(Component.text("styling too!", NamedTextColor.RED, TextDecoration.BOLD)));

            event.setStatus(Status.builder()
                    .description(Component.text("This is a Minestom Server", TextColor.color(0x66b3ff)))
                    .favicon(favicon)
                    .playerInfo(builder.build())
                    .build());
        });
    }
}
