package net.minestom.server.entity.damage;

import club.thectm.minecraft.text.TextBuilder;
import club.thectm.minecraft.text.TextObject;
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

    public TextObject buildChatMessage(Player killed) {
        return TextBuilder.ofTranslation("death."+identifier, TextBuilder.of(killed.getUsername()).build()).build();
    }

    public static DamageType fromPlayer(Player player) {
        return new EntityDamage(player);
    }

    public TextObject buildDeathScreenMessage(Player killed) {
        return buildChatMessage(killed);
    }
}
