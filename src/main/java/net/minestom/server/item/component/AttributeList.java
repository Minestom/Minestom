package net.minestom.server.item.component;

import net.kyori.adventure.nbt.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.UniqueIdUtils;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record AttributeList(@NotNull List<Modifier> modifiers, boolean showInTooltip) {
    public static final AttributeList EMPTY = new AttributeList(List.of(), true);

    public static final NetworkBuffer.Type<AttributeList> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, AttributeList value) {
            buffer.writeCollection(value.modifiers);
            buffer.write(NetworkBuffer.BOOLEAN, value.showInTooltip);
        }

        @Override
        public AttributeList read(@NotNull NetworkBuffer buffer) {
            return new AttributeList(buffer.readCollection(Modifier::new, Short.MAX_VALUE),
                    buffer.read(NetworkBuffer.BOOLEAN));
        }
    };

    public static final BinaryTagSerializer<AttributeList> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull AttributeList value) {
            ListBinaryTag.Builder<CompoundBinaryTag> modifiers = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
            for (Modifier modifier : value.modifiers) {
                modifiers.add(CompoundBinaryTag.builder()
                        .putString("type", modifier.attribute.name())
                        .putString("slot", modifier.slot.name().toLowerCase(Locale.ROOT))
                        .put("uuid", UniqueIdUtils.toNbt(modifier.modifier.id()))
                        .putString("name", modifier.modifier.name())
                        .putDouble("amount", modifier.modifier.amount())
                        .putString("operation", modifier.modifier.operation().name().toLowerCase(Locale.ROOT))
                        .build());
            }
            return CompoundBinaryTag.builder()
                    .put("modifiers", modifiers.build())
                    .putBoolean("show_in_tooltip", value.showInTooltip)
                    .build();
        }

        @Override
        public @NotNull AttributeList read(@NotNull BinaryTag tag) {
            boolean showInTooltip = true;
            ListBinaryTag modifiersTag;
            if (tag instanceof CompoundBinaryTag compound) {
                modifiersTag = compound.getList("modifiers", BinaryTagTypes.COMPOUND);
                showInTooltip = compound.getBoolean("show_in_tooltip", true);
            } else if (tag instanceof ListBinaryTag list) {
                modifiersTag = list;
            } else return EMPTY;
            List<Modifier> modifiers = new ArrayList<>(modifiersTag.size());
            for (BinaryTag modifierTagRaw : modifiersTag) {
                if (!(modifierTagRaw instanceof CompoundBinaryTag modifierTag)) continue;
                Attribute attribute = Attribute.fromNamespaceId(modifierTag.getString("type"));
                if (attribute == null) continue; // Unknown attribute, skip
                AttributeSlot slot = AttributeSlot.valueOf(modifierTag.getString("slot").toUpperCase(Locale.ROOT));
                AttributeModifier modifier = new AttributeModifier(
                        UniqueIdUtils.fromNbt((IntArrayBinaryTag) modifierTag.get("uuid")),
                        modifierTag.getString("name"),
                        modifierTag.getDouble("amount"),
                        AttributeOperation.valueOf(modifierTag.getString("operation").toUpperCase(Locale.ROOT))
                );
                modifiers.add(new Modifier(attribute, modifier, slot));
            }
            return new AttributeList(modifiers, showInTooltip);
        }
    };

    public record Modifier(
            @NotNull Attribute attribute,
            @NotNull AttributeModifier modifier,
            @NotNull AttributeSlot slot
    ) implements NetworkBuffer.Writer {

        public Modifier(@NotNull NetworkBuffer reader) {
            this(reader.read(Attribute.NETWORK_TYPE),
                    new AttributeModifier(reader),
                    reader.readEnum(AttributeSlot.class));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(Attribute.NETWORK_TYPE, attribute);
            modifier.write(writer);
            writer.writeEnum(AttributeSlot.class, slot);
        }
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
