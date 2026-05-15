package net.minestom.demo.feature.combat;

import net.kyori.adventure.text.Component;
import net.minestom.demo.core.Feature;
import net.minestom.server.ServerProcess;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerDeathEvent;

/** Attack/knockback/damage, combat commands, and a custom death message. */
public final class CombatFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        process.command().register(
                new HealthCommand(),
                new KillCommand(),
                new AttributeCommand(),
                new PrimedTNTCommand(),
                new PotionCommand()
        );

        process.eventHandler().addListener(EntityAttackEvent.class, event -> {
            final Entity source = event.getEntity();
            final Entity target = event.getTarget();

            double yaw = Math.toRadians(source.getPosition().yaw());
            target.takeKnockback(0.4f, Math.sin(yaw), -Math.cos(yaw));

            if (target instanceof Player p) p.damage(Damage.fromEntity(source, 5));
            if (source instanceof Player p) p.sendMessage("You attacked something!");
        });

        process.eventHandler().addListener(PlayerDeathEvent.class, event ->
                event.setChatMessage(Component.text("custom death message")));
    }
}
