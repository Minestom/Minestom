package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
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
import net.minestom.server.item.predicate.ItemPredicate;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public sealed interface DataComponentPredicate extends Predicate<DataComponent.Holder> {

    record Damage(Range.Int durability, Range.Int damage) implements DataComponentPredicate {
        public static Codec<Damage> CODEC = StructCodec.struct(
                "durability", DataComponentPredicates.INT_RANGE_CODEC.optional(), Damage::durability,
                "damage", DataComponentPredicates.INT_RANGE_CODEC.optional(), Damage::damage,
                Damage::new
        );

        @Override
        public boolean test(DataComponent.Holder holder) {
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

    record Enchantments(List<Enchantment> children) implements DataComponentPredicate {
        public static Codec<Enchantments> CODEC = Enchantment.CODEC.list().transform(Enchantments::new, Enchantments::children);

        @Override
        public boolean test(DataComponent.Holder holder) {
            return children.stream().allMatch(enchantment -> enchantment.test(holder));
        }
    }

    record Enchantment(@Nullable List<DynamicRegistry.Key<net.minestom.server.item.enchant.Enchantment>> enchantments,
                       Range.Int levels) {

        public static Codec<Enchantment> CODEC = StructCodec.struct(
                "enchantments", net.minestom.server.item.enchant.Enchantment.CODEC.listOrSingle().optional(), Enchantment::enchantments,
                "levels", DataComponentPredicates.INT_RANGE_CODEC.optional(), Enchantment::levels,
                Enchantment::new
        );

        public boolean test(DataComponent.Holder holder) {
            EnchantmentList holderEnchants = holder.get(DataComponents.ENCHANTMENTS);
            if (holderEnchants == null) {
                return false;
            }
            if (enchantments == null) {
                // If `enchantments` is not specified, the predicate returns true when any enchantment matches the specified `level`
                return holderEnchants.enchantments().entrySet().stream().anyMatch(entry -> levels == null || levels.inRange(entry.getValue()));
            }
            for (DynamicRegistry.Key<net.minestom.server.item.enchant.Enchantment> e : enchantments) {
                if (holderEnchants.has(e) && (levels == null || levels.inRange(holderEnchants.level(e)))) {
                    return true;
                }
            }

            return false;
        }
    }

    record StoredEnchantments(List<StoredEnchantment> children) implements DataComponentPredicate {
        public static Codec<StoredEnchantments> CODEC = StoredEnchantment.CODEC.list().transform(StoredEnchantments::new, StoredEnchantments::children);

        @Override
        public boolean test(DataComponent.Holder holder) {
            return children.stream().allMatch(enchantment -> enchantment.test(holder));
        }
    }

    record StoredEnchantment(
            @Nullable List<DynamicRegistry.Key<net.minestom.server.item.enchant.Enchantment>> enchantments,
            Range.Int levels) {

        public static Codec<StoredEnchantment> CODEC = StructCodec.struct(
                "enchantments", net.minestom.server.item.enchant.Enchantment.CODEC.listOrSingle().optional(), StoredEnchantment::enchantments,
                "levels", DataComponentPredicates.INT_RANGE_CODEC.optional(), StoredEnchantment::levels,
                StoredEnchantment::new
        );

        public boolean test(DataComponent.Holder holder) {
            EnchantmentList holderEnchants = holder.get(DataComponents.STORED_ENCHANTMENTS);
            if (holderEnchants == null) {
                return false;
            }
            if (enchantments == null) {
                // If `enchantments` is not specified, the predicate returns true when any enchantment matches the specified `level`
                return holderEnchants.enchantments().entrySet().stream().anyMatch(entry -> levels == null || levels.inRange(entry.getValue()));
            }
            for (DynamicRegistry.Key<net.minestom.server.item.enchant.Enchantment> e : enchantments) {
                if (holderEnchants.has(e) && (levels == null || levels.inRange(holderEnchants.level(e)))) {
                    return true;
                }
            }

            return false;
        }
    }

    record Potions(List<PotionType> potionTypes) implements DataComponentPredicate {
        public static Codec<Potions> CODEC = PotionType.CODEC.listOrSingle().transform(Potions::new, Potions::potionTypes);

        @Override
        public boolean test(DataComponent.Holder holder) {
            var potion = holder.get(DataComponents.POTION_CONTENTS);
            if (potion == null) return false;
            return potionTypes.contains(potion.potion());
        }
    }

    record CustomData(BinaryTag nbt) implements DataComponentPredicate {
        public static Codec<CustomData> CODEC = Codec.NBT.transform(CustomData::new, CustomData::nbt);

        @Override
        public boolean test(DataComponent.Holder holder) {
            net.minestom.server.item.component.CustomData other = holder.get(DataComponents.CUSTOM_DATA);
            return other != null && other.nbt().equals(nbt);
        }
    }

    record Container(CollectionPredicate<ItemStack, ItemPredicate> items) implements DataComponentPredicate {
        public static Codec<Container> CODEC = StructCodec.struct(
                "items", CollectionPredicate.createCodec(ItemPredicate.CODEC), Container::items,
                Container::new
        );

        @Override
        public boolean test(DataComponent.Holder holder) {
            Iterable<ItemStack> itemStacks = holder.get(DataComponents.CONTAINER);
            return items().test(itemStacks);
        }
    }

    record BundleContents(CollectionPredicate<ItemStack, ItemPredicate> items) implements DataComponentPredicate {
        public static Codec<BundleContents> CODEC = StructCodec.struct(
                "items", CollectionPredicate.createCodec(ItemPredicate.CODEC), BundleContents::items,
                BundleContents::new
        );

        @Override
        public boolean test(DataComponent.Holder holder) {
            Iterable<ItemStack> itemStacks = holder.get(DataComponents.BUNDLE_CONTENTS);
            return items().test(itemStacks);
        }
    }

    record Fireworks(
            CollectionPredicate<net.minestom.server.item.component.FireworkExplosion, FireworkExplosionPredicate> explosions,
            Range.Int flightDuration) implements DataComponentPredicate {

        public static final Codec<Fireworks> CODEC = StructCodec.struct(
                "explosions", CollectionPredicate.createCodec(FireworkExplosionPredicate.CODEC), Fireworks::explosions,
                "flight_duration", DataComponentPredicates.INT_RANGE_CODEC.optional(), Fireworks::flightDuration,
                Fireworks::new
        );

        @Override
        public boolean test(DataComponent.Holder holder) {
            FireworkList fireworks = holder.get(DataComponents.FIREWORKS);
            if (fireworks == null) return false;
            if (flightDuration != null && !flightDuration.inRange(fireworks.flightDuration()))
                return false;
            return explosions.test(fireworks.explosions());
        }

        record FireworkExplosionPredicate(net.minestom.server.item.component.FireworkExplosion.Shape shape,
                                          Boolean hasTwinkle,
                                          Boolean hasTrail) implements Predicate<net.minestom.server.item.component.FireworkExplosion> {

            public static final Codec<FireworkExplosionPredicate> CODEC = StructCodec.struct(
                    "shape", Codec.Enum(net.minestom.server.item.component.FireworkExplosion.Shape.class).optional(), FireworkExplosionPredicate::shape,
                    "has_twinkle", Codec.BOOLEAN.optional(), FireworkExplosionPredicate::hasTwinkle,
                    "has_trail", Codec.BOOLEAN.optional(), FireworkExplosionPredicate::hasTrail,
                    FireworkExplosionPredicate::new
            );

            @Override
            public boolean test(net.minestom.server.item.component.FireworkExplosion explosion) {
                if (explosion == null) {
                    return false;
                }
                return (this.shape == null || this.shape == explosion.shape()) &&
                        (this.hasTwinkle == null || this.hasTwinkle == explosion.hasTwinkle()) &&
                        (this.hasTrail == null || this.hasTrail == explosion.hasTrail());
            }
        }
    }

    record FireworkExplosion(Fireworks.FireworkExplosionPredicate delegate) implements DataComponentPredicate {
        public static final Codec<FireworkExplosion> CODEC = Fireworks.FireworkExplosionPredicate.CODEC.transform(FireworkExplosion::new, FireworkExplosion::delegate);

        @Override
        public boolean test(DataComponent.Holder holder) {
            net.minestom.server.item.component.FireworkExplosion explosion = holder.get(DataComponents.FIREWORK_EXPLOSION);
            return delegate.test(explosion);
        }
    }

    record WritableBook(
            CollectionPredicate<FilteredText<String>, WritableBook.PagePredicate> pages) implements DataComponentPredicate {
        public static final Codec<WritableBook> CODEC = StructCodec.struct(
                "pages", CollectionPredicate.createCodec(PagePredicate.CODEC).optional(), WritableBook::pages,
                WritableBook::new
        );

        @Override
        public boolean test(DataComponent.Holder holder) {
            if (pages == null) return true;
            WritableBookContent content = holder.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content == null) return false;
            return pages.test(content.pages());
        }

        record PagePredicate(String contents) implements Predicate<FilteredText<String>> {

            public static final Codec<PagePredicate> CODEC = Codec.STRING.transform(PagePredicate::new, PagePredicate::contents);

            @Override
            public boolean test(FilteredText<String> text) {
                return text.text().equals(contents);
            }
        }

    }

    record WrittenBook(CollectionPredicate<FilteredText<Component>, WrittenBook.PagePredicate> pages, String author,
                       String title,
                       Range.Int generation, Boolean resolved) implements DataComponentPredicate {

        public static final Codec<WrittenBook> CODEC = StructCodec.struct(
                "pages", CollectionPredicate.createCodec(PagePredicate.CODEC).optional(), WrittenBook::pages,
                "author", Codec.STRING.optional(), WrittenBook::author,
                "title", Codec.STRING.optional(), WrittenBook::title,
                "generation", DataComponentPredicates.INT_RANGE_CODEC.optional(), WrittenBook::generation,
                "resolved", Codec.BOOLEAN.optional(), WrittenBook::resolved,
                WrittenBook::new
        );

        @Override
        public boolean test(DataComponent.Holder holder) {
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

        record PagePredicate(Component contents) implements Predicate<FilteredText<Component>> {
            public static final Codec<PagePredicate> CODEC = Codec.COMPONENT.transform(PagePredicate::new, PagePredicate::contents);

            @Override
            public boolean test(FilteredText<Component> text) {
                return text.text().equals(contents);
            }
        }
    }

    record AttributeModifiers(
            CollectionPredicate<Entry, AttributeModifierPredicate> modifiers) implements DataComponentPredicate {

        public static final Codec<AttributeModifiers> CODEC = StructCodec.struct(
                "modifiers", CollectionPredicate.createCodec(AttributeModifierPredicate.CODEC).optional(), AttributeModifiers::modifiers,
                AttributeModifiers::new
        );

        @Override
        public boolean test(DataComponent.Holder holder) {
            if (modifiers == null) return true;
            AttributeList attributes = holder.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (attributes == null) return false;
            return modifiers.test(attributes.modifiers().stream().map(modifier -> new Entry(
                    modifier.attribute(), modifier.modifier().id(), modifier.modifier().amount(), modifier.modifier().operation(), modifier.slot()
            ))::iterator);
        }

        record Entry(Attribute attribute, Key id, Double amount, AttributeOperation operation,
                     EquipmentSlotGroup slot) {
        }

        record AttributeModifierPredicate(Attribute attribute, Key id, Range.Double amount,
                                          AttributeOperation operation,
                                          EquipmentSlotGroup slot) implements Predicate<Entry> {

            public static final Codec<AttributeModifierPredicate> CODEC = StructCodec.struct(
                    "attribute", Attribute.CODEC.optional(), AttributeModifierPredicate::attribute,
                    "id", Codec.KEY.optional(), AttributeModifierPredicate::id,
                    "amount", DataComponentPredicates.DOUBLE_RANGE_CODEC.optional(), AttributeModifierPredicate::amount,
                    "operation", AttributeOperation.CODEC.optional(), AttributeModifierPredicate::operation,
                    "slot", EquipmentSlotGroup.CODEC.optional(), AttributeModifierPredicate::slot,
                    AttributeModifierPredicate::new
            );

            @Override
            public boolean test(Entry other) {
                if (attribute != null && !attribute.key().equals(other.attribute.key()))
                    return false;
                if (id != null && !id.equals(other.id))
                    return false;
                if (amount != null && !amount.inRange(other.amount))
                    return false;
                if (operation != null && !operation.equals(other.operation))
                    return false;
                return slot == null || slot.equals(other.slot);
            }
        }
    }

    record ArmorTrim(List<TrimMaterial> material, List<TrimPattern> pattern) implements DataComponentPredicate {

        public static final Codec<ArmorTrim> CODEC = StructCodec.struct(
                "material", TrimMaterial.CODEC.transform(holder -> holder.resolve(MinecraftServer.getTrimMaterialRegistry()), Holder.Direct::new).listOrSingle().optional(), ArmorTrim::material,
                "pattern", TrimPattern.CODEC.transform(holder -> holder.resolve(MinecraftServer.getTrimPatternRegistry()), Holder.Direct::new).listOrSingle().optional(), ArmorTrim::pattern,
                ArmorTrim::new
        );

        @Override
        public boolean test(DataComponent.Holder holder) {
            var trim = holder.get(DataComponents.TRIM);
            if (trim == null) return false;
            if (material != null && !material.contains(trim.material().resolve(MinecraftServer.getTrimMaterialRegistry())))
                return false;
            return pattern == null || pattern.contains(trim.pattern().resolve(MinecraftServer.getTrimPatternRegistry()));
        }
    }

    record JukeboxPlayable(List<Key> songs) implements DataComponentPredicate {

        public static final Codec<JukeboxPlayable> CODEC = StructCodec.struct(
                "song", JukeboxSong.CODEC.transform(DynamicRegistry.Key::key, DynamicRegistry.Key::of).listOrSingle().optional(), JukeboxPlayable::songs,
                JukeboxPlayable::new
        );

        @Override
        public boolean test(DataComponent.Holder holder) {
            var song = holder.get(DataComponents.JUKEBOX_PLAYABLE);
            if (song == null) return false;
            return songs.contains(song.reference());
        }
    }
}
