package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.RegistryKey;

import java.util.HashMap;
import java.util.Map;

public record EnchantmentList(Map<RegistryKey<Enchantment>, Integer> enchantments) {
    public static final EnchantmentList EMPTY = new EnchantmentList(Map.of());

    public static final NetworkBuffer.Type<EnchantmentList> NETWORK_TYPE = NetworkBufferTemplate.template(
            Enchantment.NETWORK_TYPE.mapValue(NetworkBuffer.VAR_INT, Short.MAX_VALUE), EnchantmentList::enchantments,
            EnchantmentList::new);
    public static final Codec<EnchantmentList> CODEC = Enchantment.CODEC.mapValue(Codec.INT, Short.MAX_VALUE)
            .transform(EnchantmentList::new, EnchantmentList::enchantments);

    public EnchantmentList {
        enchantments = Map.copyOf(enchantments);
    }

    public EnchantmentList(RegistryKey<Enchantment> enchantment, int level) {
        this(Map.of(enchantment, level));
    }

    public boolean has(RegistryKey<Enchantment> enchantment) {
        return enchantments.containsKey(enchantment);
    }

    public int level(RegistryKey<Enchantment> enchantment) {
        return enchantments.getOrDefault(enchantment, 0);
    }

    public EnchantmentList with(RegistryKey<Enchantment> enchantment, int level) {
        Map<RegistryKey<Enchantment>, Integer> newEnchantments = new HashMap<>(enchantments);
        newEnchantments.put(enchantment, level);
        return new EnchantmentList(newEnchantments);
    }

    public EnchantmentList remove(RegistryKey<Enchantment> enchantment) {
        Map<RegistryKey<Enchantment>, Integer> newEnchantments = new HashMap<>(enchantments);
        newEnchantments.remove(enchantment);
        return new EnchantmentList(newEnchantments);
    }
}
