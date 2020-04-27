package net.minestom.server.entity.damage;

import club.thectm.minecraft.text.TextBuilder;
import club.thectm.minecraft.text.TextObject;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;

/**
 * Represents a type of damage
 */
public class DamageType {

    public static final DamageType VOID = new DamageType("void");
    public static final DamageType GRAVITY = new DamageType("gravity");
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

    public TextObject buildChatMessage(Player killed) {
        return TextBuilder.of(killed.getUsername() + " was killed by damage of type " + identifier).build();
    }

    public static DamageType fromPlayer(Player player) {
        return new EntityDamage(player);
    }

    public TextObject buildDeathMessage() {
        return TextBuilder.of("Killed by damage of type " + identifier).build();
    }
}
