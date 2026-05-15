package net.minestom.demo.feature.world;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.event.player.PlayerLeaveBedEvent;
import net.minestom.server.sound.SoundEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * World-state showcase:
 * <ul>
 *   <li>Commands: {@code /weather}, {@code /worldborder},
 *       {@code /dimension}, {@code /save}, {@code /sleep}.</li>
 *   <li>{@link PlayerLeaveBedEvent}: with 70% probability cancel the leave
 *       (sending an "I'm too tired to stand up!" action bar and a sound),
 *       otherwise clear the action bar.</li>
 * </ul>
 */
public final class WorldFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(
                new WeatherCommand(),
                new WorldBorderCommand(),
                new DimensionCommand(),
                new SaveCommand(),
                new SleepCommand()
        );

        process.eventHandler().addListener(PlayerLeaveBedEvent.class, event -> {
            var player = event.getPlayer();
            boolean snooze = ThreadLocalRandom.current().nextFloat() < 0.7f;
            if (snooze) {
                event.setCancelled(true);
                player.playSound(Sound.sound(SoundEvent.ENTITY_ALLAY_ITEM_THROWN, Sound.Source.PLAYER, 1f, 0.6f));
                player.sendActionBar(Component.text("I'm too tired to stand up!"));
            } else {
                player.sendActionBar(Component.empty());
            }
        });
    }
}
