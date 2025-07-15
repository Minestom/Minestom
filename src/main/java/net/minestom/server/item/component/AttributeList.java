package net.minestom.server.item.component;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.ArrayList;
import java.util.List;

public record AttributeList(List<Modifier> modifiers) {
    public static final AttributeList EMPTY = new AttributeList(List.of());

    public static final NetworkBuffer.Type<AttributeList> NETWORK_TYPE = Modifier.NETWORK_TYPE.list(Short.MAX_VALUE)
            .transform(AttributeList::new, AttributeList::modifiers);
    public static final Codec<AttributeList> CODEC = Modifier.CODEC.list(Short.MAX_VALUE)
            .transform(AttributeList::new, AttributeList::modifiers);

    public record Modifier(
            Attribute attribute,
            AttributeModifier modifier,
            EquipmentSlotGroup slot,
            Display display
    ) {
        public static final NetworkBuffer.Type<Modifier> NETWORK_TYPE = NetworkBufferTemplate.template(
                Attribute.NETWORK_TYPE, Modifier::attribute,
                AttributeModifier.NETWORK_TYPE, Modifier::modifier,
                NetworkBuffer.Enum(EquipmentSlotGroup.class), Modifier::slot,
                Display.NETWORK_TYPE, Modifier::display,
                Modifier::new);
        public static final Codec<Modifier> CODEC = StructCodec.struct(
                "type", Attribute.CODEC, Modifier::attribute,
                StructCodec.INLINE, AttributeModifier.CODEC, Modifier::modifier,
                "slot", EquipmentSlotGroup.CODEC.optional(EquipmentSlotGroup.ANY), Modifier::slot,
                "display", Display.CODEC.optional(Display.Default.INSTANCE), Modifier::display,
                Modifier::new);

        public Modifier(
                Attribute attribute,
                AttributeModifier modifier,
                EquipmentSlotGroup slot
        ) {
            this(attribute, modifier, slot, Display.Default.INSTANCE);
        }

    }

    public AttributeList {
        modifiers = List.copyOf(modifiers);
    }

    public AttributeList(Modifier modifier) {
        this(List.of(modifier));
    }

    public AttributeList with(Modifier modifier) {
        List<Modifier> newModifiers = new ArrayList<>(modifiers);
        newModifiers.add(modifier);
        return new AttributeList(newModifiers);
    }

    public AttributeList remove(Modifier modifier) {
        List<Modifier> newModifiers = new ArrayList<>(modifiers);
        newModifiers.remove(modifier);
        return new AttributeList(newModifiers);
    }

    public sealed interface Display {
        NetworkBuffer.Type<Display> NETWORK_TYPE = Type.NETWORK_TYPE
                .unionType(Display::dataSerializer, Display::targetToType);
        Codec<Display> CODEC = Type.CODEC.unionType(Display::codec, Display::targetToType);

        record Default() implements Display {
            public static final Default INSTANCE = new Default();

            public static final NetworkBuffer.Type<Default> NETWORK_TYPE = NetworkBufferTemplate.template(INSTANCE);
            public static final StructCodec<Default> CODEC = StructCodec.struct(INSTANCE);
        }

        record Hidden() implements Display {
            public static final Hidden INSTANCE = new Hidden();

            public static final NetworkBuffer.Type<Hidden> NETWORK_TYPE = NetworkBufferTemplate.template(INSTANCE);
            public static final StructCodec<Hidden> CODEC = StructCodec.struct(INSTANCE);
        }

        record Override(Component component) implements Display {
            public static final NetworkBuffer.Type<Override> NETWORK_TYPE = NetworkBufferTemplate.template(
                    NetworkBuffer.COMPONENT, Override::component,
                    Override::new);
            public static final StructCodec<Override> CODEC = StructCodec.struct(
                    "value", Codec.COMPONENT, Override::component,
                    Override::new);
        }


        enum Type {
            DEFAULT, HIDDEN, OVERRIDE;

            public static final NetworkBuffer.Type<Type> NETWORK_TYPE = NetworkBuffer.Enum(Type.class);
            public static final Codec<Type> CODEC = Codec.Enum(Type.class);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static NetworkBuffer.Type<Display> dataSerializer(Type type) {
            return (NetworkBuffer.Type) switch (type) {
                case DEFAULT -> Default.NETWORK_TYPE;
                case HIDDEN -> Hidden.NETWORK_TYPE;
                case OVERRIDE -> Override.NETWORK_TYPE;
            };
        }

        private static StructCodec<? extends Display> codec(Type type) {
            return switch (type) {
                case DEFAULT -> Default.CODEC;
                case HIDDEN -> Hidden.CODEC;
                case OVERRIDE -> Override.CODEC;
            };
        }

        private static Type targetToType(Display display) {
            return switch (display) {
                case Default ignored -> Type.DEFAULT;
                case Hidden ignored -> Type.HIDDEN;
                case Override ignored -> Type.OVERRIDE;
            };
        }
    }

}
