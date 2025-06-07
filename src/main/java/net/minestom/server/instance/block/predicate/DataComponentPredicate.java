package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.item.component.*;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.item.predicate.ItemPredicate;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.*;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public sealed interface DataComponentPredicate extends Predicate<DataComponent.Holder> {

    record Damage(@Nullable Range.Int durability, @Nullable Range.Int damage) implements DataComponentPredicate {
        public static Codec<Damage> CODEC = StructCodec.struct(
                "durability", DataComponentPredicates.INT_RANGE_CODEC.optional(), Damage::durability,
                "damage", DataComponentPredicates.INT_RANGE_CODEC.optional(), Damage::damage,
                Damage::new
        );

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            Integer damageValue = holder.get(DataComponents.DAMAGE);
            if (damageValue == null) {
                return false;
            } else {
                int i = holder.get(DataComponents.MAX_DAMAGE, 0);
                return (durability == null || durability.inRange(i - damageValue)) &&
                        (damage == null || damage.inRange(damageValue));
            }
        }
    }

    record EnchantmentListPredicate(@Nullable RegistryTag<net.minestom.server.item.enchant.Enchantment> enchantments,
                                    @Nullable Range.Int levels) implements Predicate<EnchantmentList> {

        public static Codec<EnchantmentListPredicate> CODEC = StructCodec.struct(
                "enchantments", RegistryTag.codec(Registries::enchantment).optional(), EnchantmentListPredicate::enchantments,
                "levels", DataComponentPredicates.INT_RANGE_CODEC.optional(), EnchantmentListPredicate::levels,
                EnchantmentListPredicate::new
        );

        @Override
        public boolean test(@NotNull EnchantmentList enchantmentList) {
            if (enchantments != null) {
                for (RegistryKey<Enchantment> key : enchantments) {
                    if (enchantmentList.has(key) && (levels == null || levels.inRange(enchantmentList.level(key)))) {
                        return true;
                    }
                }
                return false;
            } else if (levels != null) {
                // If `enchantments` is not specified, the predicate returns true when any enchantment matches the specified `level`
                return enchantmentList.enchantments().entrySet().stream().anyMatch(entry -> levels.inRange(entry.getValue()));
            } else {
                // If neither are specified, the predicate returns true when the item has any enchantments
                return enchantmentList.enchantments().isEmpty();
            }
        }
    }

    record Enchantments(@NotNull List<@NotNull EnchantmentListPredicate> children) implements DataComponentPredicate {
        public static Codec<Enchantments> CODEC = EnchantmentListPredicate.CODEC.list().transform(Enchantments::new, Enchantments::children);

        public Enchantments {
            children = List.copyOf(children);
        }

        public Enchantments(@Nullable RegistryTag<net.minestom.server.item.enchant.Enchantment> enchantments,
                            @Nullable Range.Int levels) {
            this(List.of(new EnchantmentListPredicate(enchantments, levels)));
        }

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            EnchantmentList enchantments = holder.get(DataComponents.ENCHANTMENTS);
            return enchantments != null && children.stream().allMatch(enchantment -> enchantment.test(enchantments));
        }
    }

    record StoredEnchantments(
            @NotNull List<@NotNull EnchantmentListPredicate> children) implements DataComponentPredicate {
        public static Codec<StoredEnchantments> CODEC = EnchantmentListPredicate.CODEC.list().transform(StoredEnchantments::new, StoredEnchantments::children);

        public StoredEnchantments {
            children = List.copyOf(children);
        }

        public StoredEnchantments(@Nullable RegistryTag<net.minestom.server.item.enchant.Enchantment> enchantments,
                                  @Nullable Range.Int levels) {
            this(List.of(new EnchantmentListPredicate(enchantments, levels)));
        }

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            EnchantmentList enchantments = holder.get(DataComponents.STORED_ENCHANTMENTS);
            return enchantments != null && children.stream().allMatch(enchantment -> enchantment.test(enchantments));
        }
    }

    record Potions(@Nullable RegistryTag<PotionType> potionTypes) implements DataComponentPredicate {
        public static Codec<Potions> CODEC = RegistryTag.codec(Registries::potionType).transform(Potions::new, Potions::potionTypes).optional();

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            if (potionTypes == null) return false;
            var potion = holder.get(DataComponents.POTION_CONTENTS);
            if (potion == null || potion.potion() == null) return false;
            return potionTypes.contains(potion.potion());
        }
    }

    record CustomData(@NotNull NbtPredicate nbt) implements DataComponentPredicate {
        public static Codec<CustomData> CODEC = NbtPredicate.CODEC.transform(CustomData::new, CustomData::nbt);

        public CustomData(@Nullable CompoundBinaryTag nbt) {
            this(new NbtPredicate(nbt));
        }

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            net.minestom.server.item.component.CustomData other = holder.get(DataComponents.CUSTOM_DATA);
            return other != null && nbt.test(other.nbt());
        }
    }

    record Container(@Nullable CollectionPredicate<ItemStack, ItemPredicate> items) implements DataComponentPredicate {
        public static Codec<Container> CODEC = StructCodec.struct(
                "items", CollectionPredicate.createCodec(ItemPredicate.CODEC).optional(), Container::items,
                Container::new
        );

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            List<ItemStack> itemStacks = holder.get(DataComponents.CONTAINER);
            return items == null || items.test(itemStacks != null ? itemStacks.stream().filter(item -> !item.isAir()).toList() : List.of());
        }
    }

    record BundleContents(
            @Nullable CollectionPredicate<ItemStack, ItemPredicate> items) implements DataComponentPredicate {
        public static Codec<BundleContents> CODEC = StructCodec.struct(
                "items", CollectionPredicate.createCodec(ItemPredicate.CODEC).optional(), BundleContents::items,
                BundleContents::new
        );

        @Override
        public boolean test(DataComponent.Holder holder) {
            List<ItemStack> itemStacks = holder.get(DataComponents.BUNDLE_CONTENTS);
            return items == null || items.test(itemStacks != null ? itemStacks : List.of());
        }
    }

    record Fireworks(
            @Nullable CollectionPredicate<net.minestom.server.item.component.FireworkExplosion, FireworkExplosionPredicate> explosions,
            @Nullable Range.Int flightDuration) implements DataComponentPredicate {

        public static final Codec<Fireworks> CODEC = StructCodec.struct(
                "explosions", CollectionPredicate.createCodec(FireworkExplosionPredicate.CODEC).optional(), Fireworks::explosions,
                "flight_duration", DataComponentPredicates.INT_RANGE_CODEC.optional().optional(), Fireworks::flightDuration,
                Fireworks::new
        );

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            FireworkList fireworks = holder.get(DataComponents.FIREWORKS);
            if (fireworks == null) return false;
            if (flightDuration != null && !flightDuration.inRange(fireworks.flightDuration()))
                return false;
            return explosions == null || explosions.test(fireworks.explosions());
        }

        record FireworkExplosionPredicate(@Nullable net.minestom.server.item.component.FireworkExplosion.Shape shape,
                                          @Nullable Boolean hasTwinkle,
                                          @Nullable Boolean hasTrail) implements Predicate<net.minestom.server.item.component.FireworkExplosion> {

            public static final Codec<FireworkExplosionPredicate> CODEC = StructCodec.struct(
                    "shape", Codec.Enum(net.minestom.server.item.component.FireworkExplosion.Shape.class).optional(), FireworkExplosionPredicate::shape,
                    "has_twinkle", Codec.BOOLEAN.optional(), FireworkExplosionPredicate::hasTwinkle,
                    "has_trail", Codec.BOOLEAN.optional(), FireworkExplosionPredicate::hasTrail,
                    FireworkExplosionPredicate::new
            );

            @Override
            public boolean test(@Nullable net.minestom.server.item.component.FireworkExplosion explosion) {
                if (explosion == null) {
                    return false;
                }
                return (this.shape == null || this.shape == explosion.shape()) &&
                        (this.hasTwinkle == null || this.hasTwinkle == explosion.hasTwinkle()) &&
                        (this.hasTrail == null || this.hasTrail == explosion.hasTrail());
            }
        }
    }

    record FireworkExplosion(@NotNull Fireworks.FireworkExplosionPredicate delegate) implements DataComponentPredicate {
        public static final Codec<FireworkExplosion> CODEC = Fireworks.FireworkExplosionPredicate.CODEC.transform(FireworkExplosion::new, FireworkExplosion::delegate);

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            net.minestom.server.item.component.FireworkExplosion explosion = holder.get(DataComponents.FIREWORK_EXPLOSION);
            return delegate.test(explosion);
        }
    }

    record WritableBook(
            @Nullable CollectionPredicate<FilteredText<String>, WritableBook.PagePredicate> pages) implements DataComponentPredicate {
        public static final Codec<WritableBook> CODEC = StructCodec.struct(
                "pages", CollectionPredicate.createCodec(PagePredicate.CODEC).optional(), WritableBook::pages,
                WritableBook::new
        );

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            if (pages == null) return true;
            WritableBookContent content = holder.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content == null) return false;
            return pages.test(content.pages());
        }

        record PagePredicate(@NotNull String contents) implements Predicate<FilteredText<String>> {

            public static final Codec<PagePredicate> CODEC = Codec.STRING.transform(PagePredicate::new, PagePredicate::contents);

            @Override
            public boolean test(@NotNull FilteredText<String> text) {
                return text.text().equals(contents);
            }
        }

    }

    record WrittenBook(@Nullable CollectionPredicate<FilteredText<Component>, WrittenBook.PagePredicate> pages,
                       @Nullable String author,
                       @Nullable String title,
                       @Nullable Range.Int generation, @Nullable Boolean resolved) implements DataComponentPredicate {

        public static final Codec<WrittenBook> CODEC = StructCodec.struct(
                "pages", CollectionPredicate.createCodec(PagePredicate.CODEC).optional(), WrittenBook::pages,
                "author", Codec.STRING.optional(), WrittenBook::author,
                "title", Codec.STRING.optional(), WrittenBook::title,
                "generation", DataComponentPredicates.INT_RANGE_CODEC.optional(), WrittenBook::generation,
                "resolved", Codec.BOOLEAN.optional(), WrittenBook::resolved,
                WrittenBook::new
        );

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            WrittenBookContent content = holder.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content == null) return false;

            if (author != null && !author.equals(content.author()))
                return false;
            if (title != null && !title.equals(content.title().text()))
                return false;
            if (generation != null && !generation.inRange(content.generation()))
                return false;
            if (resolved != null && resolved != content.resolved())
                return false;

            return pages == null || pages.test(content.pages());
        }

        record PagePredicate(@NotNull Component contents) implements Predicate<FilteredText<Component>> {
            public static final Codec<PagePredicate> CODEC = Codec.COMPONENT.transform(PagePredicate::new, PagePredicate::contents);

            @Override
            public boolean test(@NotNull FilteredText<Component> text) {
                return text.text().equals(contents);
            }
        }
    }

    record AttributeModifiers(
            @Nullable CollectionPredicate<AttributeList.Modifier, AttributeModifierPredicate> modifiers) implements DataComponentPredicate {

        public static final Codec<AttributeModifiers> CODEC = StructCodec.struct(
                "modifiers", CollectionPredicate.createCodec(AttributeModifierPredicate.CODEC).optional(), AttributeModifiers::modifiers,
                AttributeModifiers::new
        );

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            if (modifiers == null) return true;
            AttributeList attributes = holder.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (attributes == null) return false;
            return modifiers.test(attributes.modifiers());
        }

        record AttributeModifierPredicate(@Nullable Attribute attribute, @Nullable Key id,
                                          @Nullable Range.Double amount,
                                          @Nullable AttributeOperation operation,
                                          @Nullable EquipmentSlotGroup slot) implements Predicate<AttributeList.Modifier> {

            public static final Codec<AttributeModifierPredicate> CODEC = StructCodec.struct(
                    "attribute", Attribute.CODEC.optional(), AttributeModifierPredicate::attribute,
                    "id", Codec.KEY.optional(), AttributeModifierPredicate::id,
                    "amount", DataComponentPredicates.DOUBLE_RANGE_CODEC.optional(), AttributeModifierPredicate::amount,
                    "operation", AttributeOperation.CODEC.optional(), AttributeModifierPredicate::operation,
                    "slot", EquipmentSlotGroup.CODEC.optional(), AttributeModifierPredicate::slot,
                    AttributeModifierPredicate::new
            );

            @Override
            public boolean test(AttributeList.Modifier other) {
                if (attribute != null && !attribute.key().equals(other.attribute().key()))
                    return false;
                if (id != null && !id.equals(other.modifier().id()))
                    return false;
                if (amount != null && !amount.inRange(other.modifier().amount()))
                    return false;
                if (operation != null && !operation.equals(other.modifier().operation()))
                    return false;
                return slot == null || slot.equals(other.slot());
            }
        }
    }

    record ArmorTrim(@Nullable RegistryTag<TrimMaterial> material,
                     @Nullable RegistryTag<TrimPattern> pattern) implements DataComponentPredicate {

        public static final Codec<ArmorTrim> CODEC = StructCodec.struct(
                "material", RegistryTag.codec(Registries::trimMaterial).optional(), ArmorTrim::material,
                "pattern", RegistryTag.codec(Registries::trimPattern).optional(), ArmorTrim::pattern,
                ArmorTrim::new
        );

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            var trim = holder.get(DataComponents.TRIM);
            if (trim == null) return false;
            if (material != null && !trim.material().isDirect() && !material.contains(trim.material().asKey()))
                return false;
            return pattern == null || (!trim.pattern().isDirect() && pattern.contains(trim.pattern().asKey()));
        }
    }

    record JukeboxPlayable(@Nullable RegistryTag<JukeboxSong> songs) implements DataComponentPredicate {

        public static final Codec<JukeboxPlayable> CODEC = StructCodec.struct(
                "song", RegistryTag.codec(Registries::jukeboxSong).optional(), JukeboxPlayable::songs,
                JukeboxPlayable::new
        );

        @Override
        public boolean test(@NotNull DataComponent.Holder holder) {
            var song = holder.get(DataComponents.JUKEBOX_PLAYABLE);
            if (song == null) return false;
            return songs == null || songs.contains(song);
        }
    }
}
