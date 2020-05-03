package net.minestom.server.entity.damage;

import net.kyori.text.TextComponent;
import net.kyori.text.TranslatableComponent;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

/**
 * Represents a type of damage
 */
public class DamageType {

    public static final DamageType VOID = new DamageType("attack.outOfWorld");
    public static final DamageType GRAVITY = new DamageType("attack.fall");
    private final String identifier;

    public DamageType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public static DamageType fromProjectile(Entity shooter, Entity projectile) {
        return new EntityProjectileDamage(shooter, projectile);
    }

    public TextComponent buildChatMessage(Player killed) {
        return TextComponent.builder().append(TranslatableComponent.of("death."+identifier)).append(killed.getUsername()).build();
    }

    public static DamageType fromPlayer(Player player) {
        return new EntityDamage(player);
    }

    public TextComponent buildDeathScreenMessage(Player killed) {
        return buildChatMessage(killed);
    }
}
