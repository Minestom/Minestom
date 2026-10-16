package net.minestom.server.dialog;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public sealed interface DialogBody extends ComponentHolder<DialogBody> {
    Registry<StructCodec<? extends DialogBody>> REGISTRY = DynamicRegistry.fromMap(
            Key.key("dialog_body_type"),
            Map.entry(Key.key("item"), Item.CODEC),
            Map.entry(Key.key("plain_message"), PlainMessage.CODEC));
    StructCodec<DialogBody> CODEC = Codec.RegistryTaggedUnion(REGISTRY, DialogBody::codec);

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
        public StructCodec<Item> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            List<Component> components = new ArrayList<>();
            if (description != null) components.addAll(description.components());
            return List.copyOf(components);
        }

        @Override
        public Item copyWithOperator(UnaryOperator<Component> operator) {
            return new Item(
                    itemStack,
                    description != null ? description.copyWithOperator(operator) : null,
                    showDecoration,
                    showTooltip,
                    width,
                    height
            );
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
        public StructCodec<PlainMessage> codec() {
            return CODEC;
        }

        @Override
        public List<Component> components() {
            return List.of(contents);
        }

        @Override
        public PlainMessage copyWithOperator(UnaryOperator<Component> operator) {
            return new PlainMessage(
                    operator.apply(contents),
                    width
            );
        }
    }

    @ApiStatus.OverrideOnly
    StructCodec<? extends DialogBody> codec();

}
