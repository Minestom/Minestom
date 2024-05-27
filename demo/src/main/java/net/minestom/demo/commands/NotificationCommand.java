package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;

public class NotificationCommand extends Command {
    public NotificationCommand() {
        super("notification");

        setDefaultExecutor((sender, context) -> {
            var player = (Player) sender;
            var notification = net.minestom.server.notifications.Notification.builder()
                    .title(Component.text("Hello World!"))
                    .frameType(FrameType.GOAL)
                    .icon(Material.DIAMOND_AXE)
                    .build();
            notification.send(player);
        });
    }
}
