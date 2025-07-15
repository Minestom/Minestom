package net.minestom.server.recipe.display;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.TagKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public sealed interface SlotDisplay extends ComponentHolder<SlotDisplay> {

    NetworkBuffer.Type<SlotDisplay> NETWORK_TYPE = SlotDisplayType.NETWORK_TYPE
            .unionType(SlotDisplay::dataSerializer, SlotDisplay::slotDisplayToType);

    final class Empty implements SlotDisplay {
        public static final Empty INSTANCE = new Empty();

        public static final NetworkBuffer.Type<Empty> NETWORK_TYPE = NetworkBufferTemplate.template(INSTANCE);

        private Empty() {
        }
    }

    final class AnyFuel implements SlotDisplay {
        public static final AnyFuel INSTANCE = new AnyFuel();

        public static final NetworkBuffer.Type<AnyFuel> NETWORK_TYPE = NetworkBufferTemplate.template(INSTANCE);

        private AnyFuel() {
        }
    }

    record Item(Material material) implements SlotDisplay {
        public static final NetworkBuffer.Type<Item> NETWORK_TYPE = NetworkBufferTemplate.template(
                Material.NETWORK_TYPE, Item::material,
                Item::new);
    }

    record ItemStack(net.minestom.server.item.ItemStack itemStack) implements SlotDisplay {
        public static final NetworkBuffer.Type<ItemStack> NETWORK_TYPE = NetworkBufferTemplate.template(
                net.minestom.server.item.ItemStack.STRICT_NETWORK_TYPE, ItemStack::itemStack,
                ItemStack::new);

        @Override
        public Collection<Component> components() {
            return net.minestom.server.item.ItemStack.textComponents(itemStack);
        }

        @Override
        public SlotDisplay copyWithOperator(UnaryOperator<Component> operator) {
            return new ItemStack(net.minestom.server.item.ItemStack.copyWithOperator(itemStack, operator));
        }
    }

    record Tag(TagKey<Material> tag) implements SlotDisplay {
        public static final NetworkBuffer.Type<Tag> NETWORK_TYPE = NetworkBufferTemplate.template(
                TagKey.networkType(ignored -> Material.staticRegistry()), Tag::tag,
                Tag::new);
    }

    record SmithingTrim(
            SlotDisplay base,
            SlotDisplay trimMaterial,
            SlotDisplay trimPattern
    ) implements SlotDisplay {
        public static final NetworkBuffer.Type<SmithingTrim> NETWORK_TYPE = NetworkBufferTemplate.template(
                SlotDisplay.NETWORK_TYPE, SmithingTrim::base,
                SlotDisplay.NETWORK_TYPE, SmithingTrim::trimMaterial,
                SlotDisplay.NETWORK_TYPE, SmithingTrim::trimPattern,
                SmithingTrim::new);

        @Override
        public Collection<Component> components() {
            final var components = new ArrayList<>(base.components());
            components.addAll(trimMaterial.components());
            components.addAll(trimPattern.components());
            return List.copyOf(components);
        }

        @Override
        public SlotDisplay copyWithOperator(UnaryOperator<Component> operator) {
            return new SmithingTrim(base.copyWithOperator(operator),
                    trimMaterial.copyWithOperator(operator),
                    trimPattern.copyWithOperator(operator));
        }
    }

    record WithRemainder(SlotDisplay input, SlotDisplay remainder) implements SlotDisplay {
        public static final NetworkBuffer.Type<WithRemainder> NETWORK_TYPE = NetworkBufferTemplate.template(
                SlotDisplay.NETWORK_TYPE, WithRemainder::input,
                SlotDisplay.NETWORK_TYPE, WithRemainder::remainder,
                WithRemainder::new);

        @Override
        public Collection<Component> components() {
            final var components = new ArrayList<>(input.components());
            components.addAll(remainder.components());
            return List.copyOf(components);
        }

        @Override
        public SlotDisplay copyWithOperator(UnaryOperator<Component> operator) {
            return new WithRemainder(input.copyWithOperator(operator), remainder.copyWithOperator(operator));
        }
    }

    record Composite(List<SlotDisplay> contents) implements SlotDisplay {
        public static final NetworkBuffer.Type<Composite> NETWORK_TYPE = NetworkBufferTemplate.template(
                SlotDisplay.NETWORK_TYPE.list(), Composite::contents,
                Composite::new);

        public Composite {
            contents = List.copyOf(contents);
        }

        @Override
        public Collection<Component> components() {
            final var components = new ArrayList<Component>();
            for (var display : contents)
                components.addAll(display.components());
            return List.copyOf(components);
        }

        @Override
        public SlotDisplay copyWithOperator(UnaryOperator<Component> operator) {
            final var newContents = new ArrayList<SlotDisplay>();
            for (var display : contents)
                newContents.add(display.copyWithOperator(operator));
            return new Composite(newContents);
        }
    }

    @Override
    default Collection<Component> components() {
        return List.of();
    }

    @Override
    default SlotDisplay copyWithOperator(UnaryOperator<Component> operator) {
        return this;
    }

    private static NetworkBuffer.Type<SlotDisplay> dataSerializer(SlotDisplayType type) {
        //noinspection unchecked
        return (NetworkBuffer.Type<SlotDisplay>) switch (type) {
            case EMPTY -> Empty.NETWORK_TYPE;
            case ANY_FUEL -> AnyFuel.NETWORK_TYPE;
            case ITEM -> Item.NETWORK_TYPE;
            case ITEM_STACK -> ItemStack.NETWORK_TYPE;
            case TAG -> Tag.NETWORK_TYPE;
            case SMITHING_TRIM -> SmithingTrim.NETWORK_TYPE;
            case WITH_REMAINDER -> WithRemainder.NETWORK_TYPE;
            case COMPOSITE -> Composite.NETWORK_TYPE;
        };
    }

    private static SlotDisplayType slotDisplayToType(SlotDisplay slotDisplay) {
        return switch (slotDisplay) {
            case Empty ignored -> SlotDisplayType.EMPTY;
            case AnyFuel ignored -> SlotDisplayType.ANY_FUEL;
            case Item ignored -> SlotDisplayType.ITEM;
            case ItemStack ignored -> SlotDisplayType.ITEM_STACK;
            case Tag ignored -> SlotDisplayType.TAG;
            case SmithingTrim ignored -> SlotDisplayType.SMITHING_TRIM;
            case WithRemainder ignored -> SlotDisplayType.WITH_REMAINDER;
            case Composite ignored -> SlotDisplayType.COMPOSITE;
        };
    }

}
