package net.minestom.server.entity.damage;

import net.kyori.text.Component;
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

    public Component buildChatMessage(Player killed) {
        return TranslatableComponent.of("death."+identifier, TextComponent.of(killed.getUsername()));
    }

    public static DamageType fromPlayer(Player player) {
        return new EntityDamage(player);
    }

    public Component buildDeathScreenMessage(Player killed) {
        return buildChatMessage(killed);
    }
}
