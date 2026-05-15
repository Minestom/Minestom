package net.minestom.demo.feature.chat;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.object.ObjectContents;
import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.advancements.Notification;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;

/**
 * Chat / text component demos:
 * <ul>
 *   <li>Commands: {@code /title}, {@code /book}, {@code /echo},
 *       {@code /notification}, {@code /sidebar}, {@code /belowname},
 *       {@code /players}.</li>
 *   <li>First-spawn welcome: action message with click + sprite/playerhead
 *       object components, a {@link Notification}, and a played sound.</li>
 * </ul>
 */
public final class ChatFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(
                new TitleCommand(),
                new BookCommand(),
                new EchoCommand(),
                new NotificationCommand(),
                new SidebarCommand(),
                new BelowNameCommand(),
                new PlayersCommand()
        );

        process.eventHandler().addListener(PlayerSpawnEvent.class, event -> {
            var player = event.getPlayer();
            player.sendMessage(Component.text("click me for less health ")
                    .clickEvent(ClickEvent.runCommand("health set 2"))
                    .append(Component.object(ObjectContents.sprite(Key.key("block/stone"))))
                    .append(Component.object(ObjectContents.playerHead("Minestom"))));

            if (event.isFirstSpawn()) {
                player.sendNotification(new Notification(
                        Component.text("Welcome!"),
                        FrameType.TASK,
                        Material.IRON_SWORD
                ));
                player.playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 0.5f, 1f));
            }
        });
    }
}
