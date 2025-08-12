package net.minestom.server.message;

import net.kyori.adventure.text.format.Style;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.ComponentCodecs;
import net.minestom.server.codec.StructCodec;

import java.util.List;

public record ChatTypeDecoration(
        String translationKey,
        List<Parameter> parameters,
        Style style
) {
    public static final Codec<ChatTypeDecoration> CODEC = StructCodec.struct(
            "translation_key", Codec.STRING, ChatTypeDecoration::translationKey,
            "parameters", Parameter.CODEC.list().optional(List.of()), ChatTypeDecoration::parameters,
            "style", ComponentCodecs.STYLE.optional(Style.empty()), ChatTypeDecoration::style,
            ChatTypeDecoration::new);

    public enum Parameter {
        SENDER,
        TARGET,
        CONTENT;

        public static final Codec<Parameter> CODEC = Codec.Enum(Parameter.class);
    }

}
