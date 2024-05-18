package net.minestom.server.message;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.format.Style;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ChatTypeDecoration(
        @NotNull String translationKey,
        @NotNull List<Parameter> parameters,
        @NotNull Style style
) {

    public static final BinaryTagSerializer<ChatTypeDecoration> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                String translationKey = tag.getString("translation_key");
                List<Parameter> parameters = Parameter.LIST_NBT_TYPE.read(tag.getList("parameters"));
                Style style = Style.empty();
                if (tag.get("style") instanceof CompoundBinaryTag styleTag)
                    style = BinaryTagSerializer.NBT_COMPONENT_STYLE.read(styleTag);
                return new ChatTypeDecoration(translationKey, parameters, style);
            },
            deco -> {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                builder.putString("translation_key", deco.translationKey());
                builder.put("parameters", Parameter.LIST_NBT_TYPE.write(deco.parameters()));
                if (!deco.style.isEmpty())
                    builder.put("style", BinaryTagSerializer.NBT_COMPONENT_STYLE.write(deco.style()));
                return builder.build();
            }
    );

    public enum Parameter {
        SENDER,
        TARGET,
        CONTENT;

        public static final BinaryTagSerializer<Parameter> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(Parameter.class);
        private static final BinaryTagSerializer<List<Parameter>> LIST_NBT_TYPE = NBT_TYPE.list();
    }

}
