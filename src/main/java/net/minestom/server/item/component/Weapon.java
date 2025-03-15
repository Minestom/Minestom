package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;

public record Weapon(int itemDamagePerAttack, float disableBlockingForSeconds) {
    public static final Weapon DEFAULT = new Weapon(1, 0.0f);

    public static final NetworkBuffer.Type<Weapon> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, Weapon::itemDamagePerAttack,
            NetworkBuffer.FLOAT, Weapon::disableBlockingForSeconds,
            Weapon::new);
    public static final BinaryTagSerializer<Weapon> NBT_TYPE = BinaryTagTemplate.object(
            "item_damage_per_attack", BinaryTagSerializer.INT.optional(1), Weapon::itemDamagePerAttack,
            "disable_blocking_for_seconds", BinaryTagSerializer.FLOAT.optional(0f), Weapon::disableBlockingForSeconds,
            Weapon::new);

    public Weapon(int itemDamagePerAttack) {
        this(itemDamagePerAttack, 0.0f);
    }

    public @NotNull Weapon withItemDamagePerAttack(int itemDamagePerAttack) {
        return new Weapon(itemDamagePerAttack, this.disableBlockingForSeconds);
    }

    public @NotNull Weapon withDisableBlockingForSeconds(float disableBlockingForSeconds) {
        return new Weapon(this.itemDamagePerAttack, disableBlockingForSeconds);
    }

}
