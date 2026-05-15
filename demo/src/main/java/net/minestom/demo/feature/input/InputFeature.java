package net.minestom.demo.feature.input;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.event.player.PlayerInputEvent;

/** Live keybind state painted to the action bar via {@link PlayerInputEvent}. */
public final class InputFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.eventHandler().addListener(PlayerInputEvent.class, event -> event.getPlayer().sendActionBar(Component.empty()
                .append(Component.keybind("key.left").color(event.isHoldingLeftKey() ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text(" "))
                .append(Component.keybind("key.forward").color(event.isHoldingForwardKey() ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text(" "))
                .append(Component.keybind("key.back").color(event.isHoldingBackwardKey() ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text(" "))
                .append(Component.keybind("key.right").color(event.isHoldingRightKey() ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text(" | "))
                .append(Component.keybind("key.jump").color(event.isHoldingJumpKey() ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text(" "))
                .append(Component.keybind("key.sneak").color(event.isHoldingShiftKey() ? NamedTextColor.GREEN : NamedTextColor.RED))
                .append(Component.text(" "))
                .append(Component.keybind("key.sprint").color(event.isHoldingSprintKey() ? NamedTextColor.GREEN : NamedTextColor.RED))
        ));
    }
}
