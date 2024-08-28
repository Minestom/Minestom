package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record AttributeList(@NotNull List<Modifier> modifiers, boolean showInTooltip) {
    public static final AttributeList EMPTY = new AttributeList(List.of(), true);

    public static final NetworkBuffer.Type<AttributeList> NETWORK_TYPE = NetworkBufferTemplate.template(
            Modifier.NETWORK_TYPE.list(Short.MAX_VALUE), AttributeList::modifiers,
            BOOLEAN, AttributeList::showInTooltip,
            AttributeList::new
    );

    public static final BinaryTagSerializer<AttributeList> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull AttributeList value) {
            ListBinaryTag.Builder<BinaryTag> modifiers = ListBinaryTag.builder();
            for (Modifier modifier : value.modifiers) {
                modifiers.add(Modifier.NBT_TYPE.write(modifier));
            }
            return CompoundBinaryTag.builder()
                    .put("modifiers", modifiers.build())
                    .putBoolean("show_in_tooltip", value.showInTooltip)
                    .build();
        }

        @Override
        public @NotNull AttributeList read(@NotNull BinaryTag tag) {
            return switch (tag) {
                case CompoundBinaryTag compound -> new AttributeList(
                        compound.getList("modifiers", BinaryTagTypes.COMPOUND).stream().map(Modifier.NBT_TYPE::read).toList(),
                        compound.getBoolean("show_in_tooltip", true)
                );
                case ListBinaryTag list -> new AttributeList(list.stream().map(Modifier.NBT_TYPE::read).toList());
                default -> EMPTY;
            };
        }
    };

    public record Modifier(@NotNull Attribute attribute, @NotNull AttributeModifier modifier,
                           @NotNull EquipmentSlotGroup slot) {
        public static final NetworkBuffer.Type<Modifier> NETWORK_TYPE = NetworkBufferTemplate.template(
                Attribute.NETWORK_TYPE, Modifier::attribute,
                AttributeModifier.NETWORK_TYPE, Modifier::modifier,
                NetworkBuffer.Enum(EquipmentSlotGroup.class), Modifier::slot,
                Modifier::new
        );
        public static final BinaryTagSerializer<Modifier> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> new Modifier(
                        Attribute.NBT_TYPE.read(tag.get("type")),
                        AttributeModifier.NBT_TYPE.read(tag),
                        tag.get("slot") instanceof BinaryTag slot ? EquipmentSlotGroup.NBT_TYPE.read(slot) : EquipmentSlotGroup.ANY
                ),
                modifier -> CompoundBinaryTag.builder()
                        .put("type", Attribute.NBT_TYPE.write(modifier.attribute))
                        .put((CompoundBinaryTag) AttributeModifier.NBT_TYPE.write(modifier.modifier))
                        .put("slot", EquipmentSlotGroup.NBT_TYPE.write(modifier.slot))
                        .build()
        );
    }

    public AttributeList {
        modifiers = List.copyOf(modifiers);
    }

    public AttributeList(@NotNull List<Modifier> modifiers) {
        this(modifiers, true);
    }

    public AttributeList(@NotNull Modifier modifier, boolean showInTooltip) {
        this(List.of(modifier), showInTooltip);
    }

    public AttributeList(@NotNull Modifier modifier) {
        this(modifier, true);
    }

    public @NotNull AttributeList with(@NotNull Modifier modifier) {
        List<Modifier> newModifiers = new ArrayList<>(modifiers);
        newModifiers.add(modifier);
        return new AttributeList(newModifiers, showInTooltip);
    }

    public @NotNull AttributeList withTooltip(boolean showInTooltip) {
        return new AttributeList(modifiers, showInTooltip);
    }
}
