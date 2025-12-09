package net.minestom.server.world.attribute;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.Nullable;

public record BedRule(
        Rule canSleep,
        Rule canSetSpawn,
        boolean explodes,
        @Nullable Component errorMessage
) {
    /// The default vanilla overworld bed behavior.
    public static final BedRule CAN_SLEEP_WHEN_DARK = new BedRule(BedRule.Rule.WHEN_DARK, BedRule.Rule.ALWAYS,
            false, Component.translatable("block.minecraft.bed.no_sleep"));
    /// THe default vanilla nether/end bed behavior.
    public static final BedRule EXPLODES = new BedRule(BedRule.Rule.NEVER, BedRule.Rule.NEVER, true, null);

    public static final Codec<BedRule> CODEC = StructCodec.struct(
            "can_sleep", Rule.CODEC, BedRule::canSleep,
            "can_set_spawn", Rule.CODEC, BedRule::canSetSpawn,
            "explodes", Codec.BOOLEAN.optional(false), BedRule::explodes,
            "error_message", Codec.COMPONENT.optional(), BedRule::errorMessage,
            BedRule::new);

    public enum Rule {
        ALWAYS,
        WHEN_DARK,
        NEVER;

        public static final Codec<Rule> CODEC = Codec.Enum(Rule.class);
    }
}
