package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AttributeList(@NotNull List<Modifier> modifiers) {
    public static final AttributeList EMPTY = new AttributeList(List.of());

    public static final NetworkBuffer.Type<AttributeList> NETWORK_TYPE = Modifier.NETWORK_TYPE.list(Short.MAX_VALUE)
            .transform(AttributeList::new, AttributeList::modifiers);
    public static final Codec<AttributeList> CODEC = Modifier.CODEC.list(Short.MAX_VALUE)
            .transform(AttributeList::new, AttributeList::modifiers);

    public record Modifier(
            @NotNull Attribute attribute,
            @NotNull AttributeModifier modifier,
            @NotNull EquipmentSlotGroup slot
    ) {
        public static final NetworkBuffer.Type<Modifier> NETWORK_TYPE = NetworkBufferTemplate.template(
                Attribute.NETWORK_TYPE, Modifier::attribute,
                AttributeModifier.NETWORK_TYPE, Modifier::modifier,
                NetworkBuffer.Enum(EquipmentSlotGroup.class), Modifier::slot,
                Modifier::new);
        public static final Codec<Modifier> CODEC = StructCodec.struct(
                "type", Attribute.CODEC, Modifier::attribute,
                StructCodec.INLINE, AttributeModifier.CODEC, Modifier::modifier,
                "slot", EquipmentSlotGroup.CODEC.optional(EquipmentSlotGroup.ANY), Modifier::slot,
                Modifier::new);
    }

    public AttributeList {
        modifiers = List.copyOf(modifiers);
    }

    public AttributeList(@NotNull Modifier modifier) {
        this(List.of(modifier));
    }

    public @NotNull AttributeList with(@NotNull Modifier modifier) {
        List<Modifier> newModifiers = new ArrayList<>(modifiers);
        newModifiers.add(modifier);
        return new AttributeList(newModifiers);
    }

    public @NotNull AttributeList remove(@NotNull Modifier modifier) {
        List<Modifier> newModifiers = new ArrayList<>(modifiers);
        newModifiers.remove(modifier);
        return new AttributeList(newModifiers);
    }

}
