package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public record Weapon(int itemDamagePerAttack, float disableBlockingForSeconds) {
    public static final Weapon DEFAULT = new Weapon(1, 0.0f);

    public static final NetworkBuffer.Type<Weapon> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, Weapon::itemDamagePerAttack,
            NetworkBuffer.FLOAT, Weapon::disableBlockingForSeconds,
            Weapon::new);
    public static final Codec<Weapon> CODEC = StructCodec.struct(
            "item_damage_per_attack", Codec.INT.optional(1), Weapon::itemDamagePerAttack,
            "disable_blocking_for_seconds", Codec.FLOAT.optional(0f), Weapon::disableBlockingForSeconds,
            Weapon::new);

    public Weapon(int itemDamagePerAttack) {
        this(itemDamagePerAttack, 0.0f);
    }

    public Weapon withItemDamagePerAttack(int itemDamagePerAttack) {
        return new Weapon(itemDamagePerAttack, this.disableBlockingForSeconds);
    }

    public Weapon withDisableBlockingForSeconds(float disableBlockingForSeconds) {
        return new Weapon(this.itemDamagePerAttack, disableBlockingForSeconds);
    }

}
