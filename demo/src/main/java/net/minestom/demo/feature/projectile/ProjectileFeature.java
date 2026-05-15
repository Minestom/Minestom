package net.minestom.demo.feature.projectile;

import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

/** {@code /shoot} command and crossbow charge/release behaviour. */
public final class ProjectileFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(new ShootCommand());

        process.eventHandler().addListener(PlayerBeginItemUseEvent.class, event -> {
            final Player player = event.getPlayer();
            final ItemStack itemStack = event.getItemStack();
            final boolean hasProjectile = !itemStack.get(DataComponents.CHARGED_PROJECTILES, List.of()).isEmpty();
            if (itemStack.material() == Material.CROSSBOW && hasProjectile) {
                player.setItemInHand(event.getHand(), itemStack.without(DataComponents.CHARGED_PROJECTILES));
                player.sendMessage("pew pew!");
                event.setItemUseDuration(0);
            }
        });

        process.eventHandler().addListener(PlayerCancelItemUseEvent.class, event -> {
            final Player player = event.getPlayer();
            final ItemStack itemStack = event.getItemStack();
            if (itemStack.material() == Material.CROSSBOW && event.getUseDuration() > 25) {
                player.setItemInHand(event.getHand(), itemStack.with(DataComponents.CHARGED_PROJECTILES, List.of(ItemStack.of(Material.ARROW))));
            }
        });
    }
}
