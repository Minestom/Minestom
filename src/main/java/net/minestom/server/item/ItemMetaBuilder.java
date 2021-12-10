package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagWritable;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.Utils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class ItemMetaBuilder implements TagWritable {

    private static final AtomicReferenceFieldUpdater<ItemMetaBuilder, NBTCompound> NBT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(ItemMetaBuilder.class, NBTCompound.class, "nbt");

    protected volatile boolean built = false;
    private volatile NBTCompound nbt = new NBTCompound();

    protected int damage;
    protected boolean unbreakable;
    protected int hideFlag;
    protected Component displayName;
    protected List<Component> lore = new ArrayList<>();
    protected Map<Enchantment, Short> enchantmentMap = new HashMap<>();
    protected List<ItemAttribute> attributes = new ArrayList<>();
    protected int customModelData;
    protected Set<Block> canDestroy = new HashSet<>();
    protected Set<Block> canPlaceOn = new HashSet<>();

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder damage(int damage) {
        this.damage = damage;
        mutateNbt(compound -> compound.setInt("Damage", damage));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        mutateNbt(compound -> compound.setByte("Unbreakable", (byte) (unbreakable ? 1 : 0)));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder hideFlag(int hideFlag) {
        this.hideFlag = hideFlag;
        mutateNbt(compound -> compound.setInt("HideFlags", hideFlag));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder hideFlag(@NotNull ItemHideFlag... hideFlags) {
        int result = 0;
        for (ItemHideFlag hideFlag : hideFlags) {
            result |= hideFlag.getBitFieldPart();
        }
        return hideFlag(result);
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder displayName(@Nullable Component displayName) {
        this.displayName = displayName;
        handleCompound("display", nbtCompound -> {
            if (displayName != null) {
                final String name = GsonComponentSerializer.gson().serialize(displayName);
                nbtCompound.setString("Name", name);
            } else {
                nbtCompound.remove("Name");
            }
        });
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder lore(@NotNull List<@NotNull Component> lore) {
        this.lore = new ArrayList<>(lore);
        handleCompound("display", nbtCompound -> {
            final NBTList<NBTString> loreNBT = NBT.List(NBTType.TAG_String,
                    lore.stream()
                            .map(line -> new NBTString(GsonComponentSerializer.gson().serialize(line)))
                            .toList()
            );
            nbtCompound.set("Lore", loreNBT);
        });
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder lore(Component... lore) {
        lore(Arrays.asList(lore));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
        this.enchantmentMap = new HashMap<>(enchantments);
        handleMap(enchantmentMap, "Enchantments", () -> {
            MutableNBTCompound mutableCopy = new MutableNBTCompound(nbt);
            NBTUtils.writeEnchant(mutableCopy, "Enchantments", enchantmentMap);
            return mutableCopy.get("Enchantments");
        });
        return this;
    }

    @Contract("_, _ -> this")
    public @NotNull ItemMetaBuilder enchantment(@NotNull Enchantment enchantment, short level) {
        this.enchantmentMap.put(enchantment, level);
        enchantments(enchantmentMap);
        return this;
    }

    @Contract("-> this")
    public @NotNull ItemMetaBuilder clearEnchantment() {
        this.enchantmentMap = new HashMap<>();
        enchantments(enchantmentMap);
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder attributes(@NotNull List<@NotNull ItemAttribute> attributes) {
        this.attributes = new ArrayList<>(attributes);

        handleCollection(attributes, "AttributeModifiers", () -> NBT.List(
                NBTType.TAG_Compound,
                attributes.stream()
                        .map(itemAttribute -> NBT.Compound(nbt -> {
                            nbt.setIntArray("UUID", Utils.uuidToIntArray(itemAttribute.getUuid()));
                            nbt.setDouble("Amount", itemAttribute.getValue());
                            nbt.setString("Slot", itemAttribute.getSlot().name().toLowerCase());
                            nbt.setString("AttributeName", itemAttribute.getAttribute().getKey());
                            nbt.setInt("Operation", itemAttribute.getOperation().getId());
                            nbt.setString("Name", itemAttribute.getInternalName());
                        }))
                        .toList()
        ));

        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder customModelData(int customModelData) {
        this.customModelData = customModelData;
        mutateNbt(compound -> compound.setInt("CustomModelData", customModelData));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canPlaceOn(@NotNull Set<@NotNull Block> blocks) {
        this.canPlaceOn = new HashSet<>(blocks);
        handleCollection(canPlaceOn, "CanPlaceOn", () -> NBT.List(
                NBTType.TAG_String,
                canPlaceOn.stream()
                        .map(block -> new NBTString(block.name()))
                        .toList()
        ));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canPlaceOn(@NotNull Block... blocks) {
        return canPlaceOn(Set.of(blocks));
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canDestroy(@NotNull Set<@NotNull Block> blocks) {
        this.canDestroy = new HashSet<>(blocks);
        handleCollection(canPlaceOn, "CanPlaceOn", () -> NBT.List(
                NBTType.TAG_String,
                canDestroy.stream()
                        .map(block -> new NBTString(block.name()))
                        .toList()
        ));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canDestroy(@NotNull Block... blocks) {
        return canDestroy(Set.of(blocks));
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        mutateNbt(compound -> tag.write(compound, value));
    }

    public <T> @NotNull ItemMetaBuilder set(@NotNull Tag<T> tag, @Nullable T value) {
        setTag(tag, value);
        return this;
    }

    @Contract("-> new")
    public abstract @NotNull ItemMeta build();

    public abstract void read(@NotNull NBTCompound nbtCompound);

    protected abstract @NotNull Supplier<@NotNull ItemMetaBuilder> getSupplier();

    protected synchronized void mutateNbt(Consumer<MutableNBTCompound> consumer) {
        MutableNBTCompound copy = new MutableNBTCompound(nbt);
        consumer.accept(copy);
        if (built) {
            built = false;
            final var currentNbt = nbt;
            NBT_UPDATER.compareAndSet(this, currentNbt, copy.toCompound());
        } else {
            nbt = copy.toCompound();
        }
    }

    protected synchronized NBTCompound nbt() {
        return nbt;
    }

    protected @NotNull ItemMeta generate() {
        this.built = true;
        return build();
    }

    protected void handleCompound(@NotNull String key,
                                  @NotNull Consumer<@NotNull MutableNBTCompound> consumer) {
        mutateNbt(nbt -> {
            MutableNBTCompound newCompound = new MutableNBTCompound();
            consumer.accept(newCompound);

            nbt.set(key, newCompound.toCompound());
        });
    }

    protected void handleNullable(@Nullable Object value,
                                  @NotNull String key,
                                  @NotNull Supplier<@NotNull NBT> supplier) {
        mutateNbt(compound -> {
            if (value != null) {
                compound.set(key, supplier.get());
            } else {
                compound.remove(key);
            }
        });
    }

    protected void handleCollection(@NotNull Collection<?> objects,
                                    @NotNull String key,
                                    @NotNull Supplier<@NotNull NBT> supplier) {
        mutateNbt(compound -> {
            if (!objects.isEmpty()) {
                compound.set(key, supplier.get());
            } else {
                compound.remove(key);
            }
        });
    }

    protected void handleMap(@NotNull Map<?, ?> objects,
                             @NotNull String key,
                             @NotNull Supplier<@NotNull NBT> supplier) {
        mutateNbt(compound -> {
            if (!objects.isEmpty()) {
                compound.set(key, supplier.get());
            } else {
                compound.remove(key);
            }
        });
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull ItemMetaBuilder fromNBT(@NotNull ItemMetaBuilder src, @NotNull NBTCompound nbtCompound) {
        ItemMetaBuilder dest = src.getSupplier().get();
        dest.nbt = nbtCompound;
        NBTUtils.loadDataIntoMeta(dest, dest.nbt);
        return dest;
    }

    public interface Provider<T extends ItemMetaBuilder> {
    }
}
