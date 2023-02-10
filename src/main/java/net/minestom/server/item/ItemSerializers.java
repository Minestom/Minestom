package net.minestom.server.item;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import net.minestom.server.tag.TagWritable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

@ApiStatus.Internal
public final class ItemSerializers {
    public static final TagSerializer<EnchantmentEntry> ENCHANTMENT_SERIALIZER = new TagSerializer<>() {
        static final Tag<Short> LEVEL = Tag.Short("lvl");
        static final Tag<String> ID = Tag.String("id");

        @Override
        public @Nullable EnchantmentEntry read(@NotNull TagReadable reader) {
            final String id = reader.getTag(ID);
            final Short level = reader.getTag(LEVEL);
            if (id == null || level == null) return null;
            final Enchantment enchantment = Enchantment.fromNamespaceId(id);
            return new EnchantmentEntry(enchantment, level);
        }

        @Override
        public void write(@NotNull TagWritable writer, @NotNull EnchantmentEntry value) {
            writer.setTag(ID, value.enchantment.name());
            writer.setTag(LEVEL, value.level);
        }
    };

    public record EnchantmentEntry(Enchantment enchantment, short level) {
    }

    static final TagSerializer<ItemAttribute> ATTRIBUTE_SERIALIZER = new TagSerializer<>() {
        static final Tag<UUID> ID = Tag.UUID("UUID");
        static final Tag<Double> AMOUNT = Tag.Double("Amount");
        static final Tag<String> SLOT = Tag.String("Slot").defaultValue("mainhand");
        static final Tag<String> ATTRIBUTE_NAME = Tag.String("AttributeName");
        static final Tag<Integer> OPERATION = Tag.Integer("Operation");
        static final Tag<String> NAME = Tag.String("Name");

        @Override
        public @Nullable ItemAttribute read(@NotNull TagReadable reader) {
            final UUID uuid = reader.getTag(ID);
            final double amount = reader.getTag(AMOUNT);
            final String slot = reader.getTag(SLOT);
            final String attributeName = reader.getTag(ATTRIBUTE_NAME);
            final int operation = reader.getTag(OPERATION);
            final String name = reader.getTag(NAME);

            final Attribute attribute = Attribute.fromKey(attributeName.toUpperCase(Locale.ROOT));
            // Wrong attribute name, stop here
            if (attribute == null) return null;
            final AttributeOperation attributeOperation = AttributeOperation.fromId(operation);
            // Wrong attribute operation, stop here
            if (attributeOperation == null) return null;

            // Find slot, default to the main hand if the nbt tag is invalid
            AttributeSlot attributeSlot;
            try {
                attributeSlot = AttributeSlot.valueOf(slot.toUpperCase());
            } catch (IllegalArgumentException e) {
                attributeSlot = AttributeSlot.MAINHAND;
            }
            return new ItemAttribute(uuid, name, attribute, attributeOperation, amount, attributeSlot);
        }

        @Override
        public void write(@NotNull TagWritable writer, @NotNull ItemAttribute value) {
            writer.setTag(ID, value.uuid());
            writer.setTag(AMOUNT, value.amount());
            writer.setTag(SLOT, value.slot().name().toLowerCase(Locale.ROOT));
            writer.setTag(ATTRIBUTE_NAME, value.attribute().key());
            writer.setTag(OPERATION, value.operation().getId());
            writer.setTag(NAME, value.name());
        }
    };
}
