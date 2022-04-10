package net.minestom.server.item;

import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import net.minestom.server.tag.TagWritable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class ItemSerializers {
    static final TagSerializer<EnchantmentEntry> ENCHANTMENT_SERIALIZER = new TagSerializer<>() {
        static final Tag<Short> LEVEL = Tag.Short("Level");
        static final Tag<String> ID = Tag.String("Id");

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

    record EnchantmentEntry(Enchantment enchantment, short level) {
    }
}
