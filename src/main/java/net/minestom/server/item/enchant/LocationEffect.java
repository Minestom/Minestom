package net.minestom.server.item.enchant;

import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public non-sealed interface LocationEffect extends Enchantment.Effect {
    //todo

    @NotNull BinaryTagSerializer<LocationEffect> NBT_TYPE = null; //todo

}
