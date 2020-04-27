package net.minestom.server.entity.damage;

import net.minestom.server.entity.Player;

/**
 * Represents a type of damage
 */
public class DamageType {

    // TODO

    public static final DamageType VOID = new DamageType();
    public static final DamageType PLAYER = new DamageType();

    public static DamageType fromPlayer(Player player) {
        // TODO
        return PLAYER;
    }
}
