package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;

@FunctionalInterface
non-sealed interface BooleanModifier extends EnvironmentAttribute.Modifier<Boolean, Boolean> {
    BooleanModifier AND = (a, b) -> a && b;
    BooleanModifier NAND = (a, b) -> !a || !b;
    BooleanModifier OR = (a, b) -> a || b;
    BooleanModifier NOR = (a, b) -> !a && !b;
    BooleanModifier XOR = (a, b) -> a ^ b;
    BooleanModifier XNOR = (a, b) -> a == b;

    @java.lang.Override
    default Codec<java.lang.Boolean> argumentCodec() {
        return Codec.BOOLEAN;
    }
}
