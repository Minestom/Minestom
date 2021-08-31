package net.minestom.server.entity.features.living;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.network.packet.server.play.DeathCombatEventPacket;
import net.minestom.server.network.packet.server.play.UpdateHealthPacket;
import org.jetbrains.annotations.NotNull;

public class EntityFeaturePlayerLiving extends EntityFeatureLiving {

    public EntityFeaturePlayerLiving(Entity entity) {
        super(entity);
    }

    @Override
    public boolean isImmune(@NotNull DamageType type) {
        if (!getPlayer().getGameMode().canTakeDamage()) {
            return type != DamageType.VOID;
        }
        return super.isImmune(type);
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        Player player = getPlayer();
        player.getPlayerConnection().sendPacket(new UpdateHealthPacket(health, player.getFood(), player.getFoodSaturation()));
    }

    @Override
    public void kill() {
        if (!isDead()) {
            Player player = getPlayer();

            Component deathText;
            Component chatMessage;

            // get death screen text to the killed player
            {
                if (lastDamageSource != null) {
                    deathText = lastDamageSource.buildDeathScreenText(player);
                } else { // may happen if killed by the server without applying damage
                    deathText = Component.text("Killed by poor programming.");
                }
            }

            // get death message to chat
            {
                if (lastDamageSource != null) {
                    chatMessage = lastDamageSource.buildDeathMessage(player);
                } else { // may happen if killed by the server without applying damage
                    chatMessage = Component.text(player.getUsername() + " was killed by poor programming.");
                }
            }

            // Call player death event
            PlayerDeathEvent playerDeathEvent = new PlayerDeathEvent(player, deathText, chatMessage);
            EventDispatcher.call(playerDeathEvent);

            deathText = playerDeathEvent.getDeathText();
            chatMessage = playerDeathEvent.getChatMessage();

            // #buildDeathScreenText can return null, check here
            if (deathText != null) {
                player.getPlayerConnection().sendPacket(DeathCombatEventPacket.of(player.getEntityId(), -1, deathText));
            }

            // #buildDeathMessage can return null, check here
            if (chatMessage != null) {
                Audiences.players().sendMessage(chatMessage);
            }

        }
        super.kill();
    }

    private Player getPlayer() {
        return (Player) entity;
    }

}
