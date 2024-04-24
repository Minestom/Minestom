package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record EnchantmentList(@NotNull Map<Enchantment, Integer> enchantments, boolean showInTooltip) {
    public static final EnchantmentList EMPTY = new EnchantmentList(Map.of(), true);

    public static NetworkBuffer.Type<EnchantmentList> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, @NotNull EnchantmentList value) {
            buffer.write(NetworkBuffer.VAR_INT, value.enchantments.size());
            for (Map.Entry<Enchantment, Integer> entry : value.enchantments.entrySet()) {
                buffer.write(NetworkBuffer.VAR_INT, entry.getKey().id());
                buffer.write(NetworkBuffer.VAR_INT, entry.getValue());
            }
            buffer.write(NetworkBuffer.BOOLEAN, value.showInTooltip);
        }

        @Override
        public @NotNull EnchantmentList read(@NotNull NetworkBuffer buffer) {
            int size = buffer.read(NetworkBuffer.VAR_INT);
            Check.argCondition(size < 0 || size > Short.MAX_VALUE, "Invalid enchantment list size: {0}", size);
            Map<Enchantment, Integer> enchantments = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                Enchantment enchantment = Enchantment.fromId(buffer.read(NetworkBuffer.VAR_INT));
                int level = buffer.read(NetworkBuffer.VAR_INT);
                enchantments.put(enchantment, level);
            }
            boolean showInTooltip = buffer.read(NetworkBuffer.BOOLEAN);
            return new EnchantmentList(enchantments, showInTooltip);
        }
    };

    public static BinaryTagSerializer<EnchantmentList> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                // We have two variants of the enchantment list, one with {levels: {...}, show_in_tooltip: boolean} and one with {...}.
                CompoundBinaryTag levels = tag.keySet().contains("levels") ? tag.getCompound("levels") : tag;
                Map<Enchantment, Integer> enchantments = new HashMap<>(levels.size());
                for (Map.Entry<String, ? extends BinaryTag> entry : levels) {
                    Enchantment enchantment = Enchantment.fromNamespaceId(entry.getKey());
                    Check.notNull(enchantment, "Unknown enchantment: {0}", entry.getKey());
                    int level = BinaryTagSerializer.INT.read(entry.getValue());
                    if (level > 0) enchantments.put(enchantment, level);
                }

                // Doesnt matter which variant we chose, the default will work.
                // https://github.com/KyoriPowered/adventure/issues/1068
                boolean showInTooltip = true;
                if (tag.get("show_in_tooltip") instanceof ByteBinaryTag byteTag) {
                    showInTooltip = byteTag.value() != 0;
                }
                return new EnchantmentList(enchantments, showInTooltip);
            },
            value -> {
                CompoundBinaryTag.Builder levels = CompoundBinaryTag.builder();
                for (Map.Entry<Enchantment, Integer> entry : value.enchantments.entrySet()) {
                    levels.put(entry.getKey().name(), BinaryTagSerializer.INT.write(entry.getValue()));
                }

                return CompoundBinaryTag.builder()
                        .put("levels", levels.build())
                        .putBoolean("show_in_tooltip", value.showInTooltip)
                        .build();
            }
    );

    public EnchantmentList {
        enchantments = Map.copyOf(enchantments);
    }

    public @NotNull EnchantmentList with(@NotNull Enchantment enchantment, int level) {
        Map<Enchantment, Integer> newEnchantments = new HashMap<>(enchantments);
        newEnchantments.put(enchantment, level);
        return new EnchantmentList(newEnchantments, showInTooltip);
    }
}
