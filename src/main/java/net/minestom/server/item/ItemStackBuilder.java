package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.metadata.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class ItemStackBuilder {
    private final Material material;
    private int amount;
    private ItemMetaBuilder metaBuilder;

    private StackingRule stackingRule;

    ItemStackBuilder(@NotNull Material material, @NotNull ItemMetaBuilder metaBuilder) {
        this.material = material;
        this.amount = 1;
        this.metaBuilder = metaBuilder;
    }

    private static final Map<Material, Supplier<ItemMetaBuilder>> MATERIAL_SUPPLIER_MAP = new ConcurrentHashMap<>();

    static {
        MATERIAL_SUPPLIER_MAP.put(Material.POTION, PotionMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.LINGERING_POTION, PotionMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.SPLASH_POTION, PotionMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.TIPPED_ARROW, PotionMeta.Builder::new);

        MATERIAL_SUPPLIER_MAP.put(Material.FILLED_MAP, MapMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.COMPASS, CompassMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.ENCHANTED_BOOK, EnchantedBookMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.CROSSBOW, CrossbowMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.WRITABLE_BOOK, WritableBookMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.WRITTEN_BOOK, WrittenBookMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.FIREWORK_STAR, FireworkEffectMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.FIREWORK_ROCKET, FireworkMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.PLAYER_HEAD, PlayerHeadMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.BUNDLE, BundleMeta.Builder::new);

        MATERIAL_SUPPLIER_MAP.put(Material.LEATHER_HELMET, LeatherArmorMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.LEATHER_CHESTPLATE, LeatherArmorMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.LEATHER_LEGGINGS, LeatherArmorMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.LEATHER_BOOTS, LeatherArmorMeta.Builder::new);
        MATERIAL_SUPPLIER_MAP.put(Material.LEATHER_HORSE_ARMOR, LeatherArmorMeta.Builder::new);
    }

    static ItemMetaBuilder getMetaBuilder(Material material) {
        Supplier<ItemMetaBuilder> supplier = MATERIAL_SUPPLIER_MAP.get(material);
        return supplier != null ? supplier.get() : new DefaultMeta();
    }

    ItemStackBuilder(@NotNull Material material) {
        this(material, getMetaBuilder(material));
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder meta(@NotNull ItemMeta itemMeta) {
        this.metaBuilder = itemMeta.builder();
        return this;
    }

    @Contract(value = "_ -> this")
    public <T extends ItemMetaBuilder> @NotNull ItemStackBuilder meta(@NotNull UnaryOperator<@NotNull T> itemMetaConsumer) {
        //noinspection unchecked
        this.metaBuilder = itemMetaConsumer.apply((T) metaBuilder);
        return this;
    }

    @Contract(value = "_, _ -> this")
    public <T extends ItemMetaBuilder, U extends ItemMetaBuilder.Provider<T>> @NotNull ItemStackBuilder meta(@NotNull Class<U> metaType, @NotNull Consumer<@NotNull T> itemMetaConsumer) {
        itemMetaConsumer.accept((T) metaBuilder);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder displayName(@Nullable Component displayName) {
        this.metaBuilder.displayName(displayName);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder lore(@NotNull List<? extends Component> lore) {
        this.metaBuilder.lore(lore);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder lore(Component... lore) {
        this.metaBuilder.lore(lore);
        return this;
    }

    @ApiStatus.Experimental
    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder stackingRule(@Nullable StackingRule stackingRule) {
        this.stackingRule = stackingRule;
        return this;
    }

    @Contract(value = "-> new", pure = true)
    public @NotNull ItemStack build() {
        if (amount < 1) return ItemStack.AIR;
        return new ItemStack(material, amount, metaBuilder.build(), stackingRule);
    }

    private static final class DefaultMeta extends ItemMetaBuilder {
        @Override
        public @NotNull ItemMeta build() {
            return new ItemMeta(this);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            // Empty
        }
    }
}
