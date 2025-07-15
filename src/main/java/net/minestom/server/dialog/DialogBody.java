package net.minestom.server.dialog;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registry;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public sealed interface DialogBody {
    Registry<StructCodec<? extends DialogBody>> REGISTRY = DynamicRegistry.fromMap(
            Key.key("minecraft:dialog_body_type"),
            Map.entry(Key.key("item"), Item.CODEC),
            Map.entry(Key.key("plain_message"), PlainMessage.CODEC));
    StructCodec<DialogBody> CODEC = Codec.RegistryTaggedUnion(REGISTRY, DialogBody::codec, "type");

    record Item(
            ItemStack itemStack,
            @Nullable PlainMessage description,
            boolean showDecoration,
            boolean showTooltip,
            int width, int height
    ) implements DialogBody {
        public static final StructCodec<Item> CODEC = StructCodec.struct(
                "item", ItemStack.CODEC, Item::itemStack,
                "description", PlainMessage.CODEC.optional(), Item::description,
                "show_decoration", Codec.BOOLEAN.optional(true), Item::showDecoration,
                "show_tooltip", Codec.BOOLEAN.optional(true), Item::showTooltip,
                "width", Codec.INT.optional(16), Item::width,
                "height", Codec.INT.optional(16), Item::height,
                Item::new);

        @Override
        public StructCodec<? extends DialogBody> codec() {
            return CODEC;
        }
    }

    record PlainMessage(Component contents, int width) implements DialogBody {
        public static final int DEFAULT_WIDTH = 200;

        private static final StructCodec<PlainMessage> COMPONENT_CODEC = StructCodec.struct(
                StructCodec.INLINE, Codec.COMPONENT, PlainMessage::contents,
                (component) -> new PlainMessage(component, DEFAULT_WIDTH));
        public static final StructCodec<PlainMessage> CODEC = StructCodec.struct(
                "contents", Codec.COMPONENT, PlainMessage::contents,
                "width", Codec.INT.optional(200), PlainMessage::width,
                PlainMessage::new).orElseStruct(COMPONENT_CODEC);

        @Override
        public StructCodec<? extends DialogBody> codec() {
            return CODEC;
        }
    }

    StructCodec<? extends DialogBody> codec();

}
