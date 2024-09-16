package net.minestom.server.item.component;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static net.kyori.adventure.nbt.StringBinaryTag.stringBinaryTag;

public record EnchantmentList(@NotNull Map<DynamicRegistry.Key<Enchantment>, Integer> enchantments,
                              boolean showInTooltip) {
    public static final EnchantmentList EMPTY = new EnchantmentList(Map.of(), true);

    public static final NetworkBuffer.Type<EnchantmentList> NETWORK_TYPE = NetworkBufferTemplate.template(
            Enchantment.NETWORK_TYPE.mapValue(NetworkBuffer.VAR_INT, Short.MAX_VALUE), EnchantmentList::enchantments,
            NetworkBuffer.BOOLEAN, EnchantmentList::showInTooltip,
            EnchantmentList::new
    );
    public static BinaryTagSerializer<EnchantmentList> NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull EnchantmentList value) {
            CompoundBinaryTag.Builder levels = CompoundBinaryTag.builder();
            for (Map.Entry<DynamicRegistry.Key<Enchantment>, Integer> entry : value.enchantments.entrySet()) {
                levels.put(entry.getKey().name(), BinaryTagSerializer.INT.write(context, entry.getValue()));
            }

            return CompoundBinaryTag.builder()
                    .put("levels", levels.build())
                    .putBoolean("show_in_tooltip", value.showInTooltip)
                    .build();
        }

        @Override
        public @NotNull EnchantmentList read(@NotNull Context context, @NotNull BinaryTag raw) {
            if (!(raw instanceof CompoundBinaryTag tag))
                throw new IllegalArgumentException("Enchantment list must be a compound tag");

            // We have two variants of the enchantment list, one with {levels: {...}, show_in_tooltip: boolean} and one with {...}.
            CompoundBinaryTag levels = tag.keySet().contains("levels") ? tag.getCompound("levels") : tag;
            Map<DynamicRegistry.Key<Enchantment>, Integer> enchantments = new HashMap<>(levels.size());
            for (Map.Entry<String, ? extends BinaryTag> entry : levels) {
                DynamicRegistry.Key<Enchantment> enchantment = Enchantment.NBT_TYPE.read(context, stringBinaryTag(entry.getKey()));
                int level = BinaryTagSerializer.INT.read(entry.getValue());
                if (level > 0) enchantments.put(enchantment, level);
            }

            // Doesnt matter which variant we chose, the default will work.
            boolean showInTooltip = tag.getBoolean("show_in_tooltip", true);

            return new EnchantmentList(enchantments, showInTooltip);
        }
    };

    public EnchantmentList {
        enchantments = Map.copyOf(enchantments);
    }

    public EnchantmentList(@NotNull Map<DynamicRegistry.Key<Enchantment>, Integer> enchantments) {
        this(enchantments, true);
    }

    public EnchantmentList(@NotNull DynamicRegistry.Key<Enchantment> enchantment, int level) {
        this(Map.of(enchantment, level), true);
    }

    public boolean has(@NotNull DynamicRegistry.Key<Enchantment> enchantment) {
        return enchantments.containsKey(enchantment);
    }

    public int level(@NotNull DynamicRegistry.Key<Enchantment> enchantment) {
        return enchantments.getOrDefault(enchantment, 0);
    }

    public @NotNull EnchantmentList with(@NotNull DynamicRegistry.Key<Enchantment> enchantment, int level) {
        Map<DynamicRegistry.Key<Enchantment>, Integer> newEnchantments = new HashMap<>(enchantments);
        newEnchantments.put(enchantment, level);
        return new EnchantmentList(newEnchantments, showInTooltip);
    }

    public @NotNull EnchantmentList remove(@NotNull DynamicRegistry.Key<Enchantment> enchantment) {
        Map<DynamicRegistry.Key<Enchantment>, Integer> newEnchantments = new HashMap<>(enchantments);
        newEnchantments.remove(enchantment);
        return new EnchantmentList(newEnchantments, showInTooltip);
    }

    public @NotNull EnchantmentList withTooltip(boolean showInTooltip) {
        return new EnchantmentList(enchantments, showInTooltip);
    }
}
