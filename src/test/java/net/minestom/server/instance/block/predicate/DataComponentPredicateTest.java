package net.minestom.server.instance.block.predicate;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.instance.block.jukebox.JukeboxSong;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.armor.TrimMaterial;
import net.minestom.server.item.armor.TrimPattern;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.item.component.*;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.item.predicate.ItemPredicate;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.utils.Range;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataComponentPredicateTest {
    private static <T> DataComponent.Holder holderOf(DataComponent<T> component, T value) {
        return DataComponentMap.builder().set(component, value);
    }

    private static <T1, T2> DataComponent.Holder holderOf(DataComponent<T1> component, T1 value, DataComponent<T2> component2, T2 value2) {
        return DataComponentMap.builder().set(component, value).set(component2, value2);
    }

    private static final DataComponent.Holder EMPTY_HOLDER = new DataComponent.Holder() {
        @Override
        public <T> @Nullable T get(DataComponent<T> component) {
            return null;
        }
    };

    private <T> void assertPass(DataComponentPredicate predicate, DataComponent<T> component, T value) {
        assertPass(predicate, holderOf(component, value));
    }

    private void assertPass(DataComponentPredicate predicate, DataComponent.Holder holder) {
        assertTrue(predicate.test(holder));
    }

    private <T> void assertFail(DataComponentPredicate predicate, DataComponent<T> component, T value) {
        assertFail(predicate, holderOf(component, value));
    }

    private <T> void assertFail(DataComponentPredicate predicate, DataComponent.Holder holder) {
        assertFalse(predicate.test(holder));
    }

    @Test
    void testDamage() {
        var durability = new DataComponentPredicate.Damage(new Range.Int(0, 10), // remaining durability
                null // damage
        );

        assertFail(durability, holderOf(DataComponents.MAX_DAMAGE, 100, DataComponents.DAMAGE, 10));
        assertPass(durability, holderOf(DataComponents.MAX_DAMAGE, 100, DataComponents.DAMAGE, 90));
        assertFail(durability, holderOf(DataComponents.MAX_DAMAGE, 10)); // should always return false if DAMAGE component isn't present

        var damage = new DataComponentPredicate.Damage(null, // remaining durability
                new Range.Int(0, 10) // damage
        );

        assertFail(damage, DataComponents.DAMAGE, -1);
        assertPass(damage, DataComponents.DAMAGE, 0);
        assertPass(damage, DataComponents.DAMAGE, 5);
        assertPass(damage, DataComponents.DAMAGE, 10);
        assertFail(damage, DataComponents.DAMAGE, 11);

        var both = new DataComponentPredicate.Damage(new Range.Int(0, 10), // remaining durability
                new Range.Int(0, 5) // damage
        );

        // `both` requires both the damage and the remaining durability to match
        assertPass(both, holderOf(DataComponents.MAX_DAMAGE, 15, DataComponents.DAMAGE, 5));
        assertFail(both, holderOf(DataComponents.MAX_DAMAGE, 15, DataComponents.DAMAGE, 6)); // durability passes but damage fails
        assertFail(both, holderOf(DataComponents.MAX_DAMAGE, 50, DataComponents.DAMAGE, 5)); // damage passes but durability fails

        var neither = new DataComponentPredicate.Damage(null, null);

        // `neither` should always pass as long as DAMAGE is present
        assertPass(neither, DataComponents.DAMAGE, 0);
        assertFail(neither, EMPTY_HOLDER);
    }

    @Test
    void testEnchantments() {
        // Enchantments and StoredEnchantments work the same way, so this test covers both of them

        var enchOnly = new DataComponentPredicate.Enchantments(List.of(new DataComponentPredicate.EnchantmentListPredicate(RegistryTag.direct(Enchantment.SHARPNESS), null)));
        assertPass(enchOnly, DataComponents.ENCHANTMENTS, new EnchantmentList(Enchantment.SHARPNESS, 2));
        assertPass(enchOnly, DataComponents.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.SHARPNESS, 2, Enchantment.PROTECTION, 2)));
        assertFail(enchOnly, DataComponents.ENCHANTMENTS, new EnchantmentList(Enchantment.PROTECTION, 2));

        var levelOnly = new DataComponentPredicate.Enchantments(List.of(new DataComponentPredicate.EnchantmentListPredicate(null, new Range.Int(2, 3))));
        assertPass(levelOnly, DataComponents.ENCHANTMENTS, new EnchantmentList(Enchantment.SHARPNESS, 2));
        assertPass(levelOnly, DataComponents.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.SHARPNESS, 2, Enchantment.PROTECTION, 4)));
        assertFail(levelOnly, DataComponents.ENCHANTMENTS, new EnchantmentList(Enchantment.PROTECTION, 1));
        assertFail(levelOnly, DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY);

        var none = new DataComponentPredicate.EnchantmentListPredicate(null, null);
        // `none` should pass if the item has any enchantments
        assertFail(new DataComponentPredicate.Enchantments(List.of(none)), EMPTY_HOLDER);
        assertFail(new DataComponentPredicate.Enchantments(List.of(none)), DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY);
        assertPass(new DataComponentPredicate.Enchantments(List.of(none)), DataComponents.ENCHANTMENTS, new EnchantmentList(Enchantment.PROTECTION, 1));

        var both = new DataComponentPredicate.Enchantments(List.of(new DataComponentPredicate.EnchantmentListPredicate(RegistryTag.direct(Enchantment.SHARPNESS), new Range.Int(2, 3))));

        assertPass(both, DataComponents.ENCHANTMENTS, new EnchantmentList(Enchantment.SHARPNESS, 2)); // matching enchantment and level
        assertFail(both, DataComponents.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.SHARPNESS, 4, Enchantment.PROTECTION, 2))); // matching level on a different enchantment
        assertFail(both, DataComponents.ENCHANTMENTS, new EnchantmentList(Enchantment.PROTECTION, 2)); // matching level on the wrong enchantment
        assertFail(both, DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY);

        var multiple = new DataComponentPredicate.Enchantments(List.of(new DataComponentPredicate.EnchantmentListPredicate(RegistryTag.direct(Enchantment.SHARPNESS), new Range.Int(2, 3)), new DataComponentPredicate.EnchantmentListPredicate(RegistryTag.direct(Enchantment.PROTECTION), null)));

        // If multiple predicates are specified, all of them must return true for the parent to return true
        assertPass(multiple, DataComponents.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.SHARPNESS, 2, Enchantment.PROTECTION, 4)));
        assertFail(multiple, DataComponents.ENCHANTMENTS, new EnchantmentList(Map.of(Enchantment.SHARPNESS, 2))); // second predicate doesn't pass
    }

    @Test
    void testPotionContents() {
        var potions = new DataComponentPredicate.Potions(RegistryTag.direct(PotionType.FIRE_RESISTANCE, PotionType.HEALING, PotionType.HARMING));
        assertPass(potions, DataComponents.POTION_CONTENTS, new PotionContents(PotionType.HEALING));
        assertFail(potions, DataComponents.POTION_CONTENTS, new PotionContents(PotionType.STRENGTH)); // Potion type isn't contained in the predicate's list
        assertFail(potions, DataComponents.POTION_CONTENTS, new PotionContents(null, null, List.of(), null));
        assertFail(potions, EMPTY_HOLDER);
        assertFail(new DataComponentPredicate.Potions(RegistryTag.empty()), DataComponents.POTION_CONTENTS, new PotionContents(PotionType.STRENGTH)); // Predicate's list is empty
        assertFail(new DataComponentPredicate.Potions(null), DataComponents.POTION_CONTENTS, new PotionContents(PotionType.STRENGTH)); // Predicate's list is empty
    }

    @Test
    void testCustomData() {
        var tag = CompoundBinaryTag.builder()
                .put("test", IntBinaryTag.intBinaryTag(123))
                .put("nested", CompoundBinaryTag.builder()
                        .put("key", StringBinaryTag.stringBinaryTag("value"))
                        .build())
                .build();
        var customData = new DataComponentPredicate.CustomData(new NbtPredicate(tag));
        var empty = new DataComponentPredicate.CustomData(CompoundBinaryTag.builder().build());

        var tagWithExtraField = tag.put("extra", IntBinaryTag.intBinaryTag(456));
        var nonMatchingTag = tag.put("test", IntBinaryTag.intBinaryTag(-1));

        assertPass(empty, DataComponents.CUSTOM_DATA, new CustomData(CompoundBinaryTag.empty()));
        assertFail(customData, DataComponents.CUSTOM_DATA, new CustomData(CompoundBinaryTag.empty()));

        assertPass(customData, DataComponents.CUSTOM_DATA, new CustomData(tag));
        assertPass(customData, DataComponents.CUSTOM_DATA, new CustomData(tagWithExtraField)); // The target is allowed to have more fields than the predicate's standard
        assertFail(customData, DataComponents.CUSTOM_DATA, new CustomData(nonMatchingTag)); // However, all fields specified in the predicate must match
    }

    @Test
    void testContainer() {
        // Container and BundleContents work the same way, so this test covers both of them

        var container = new DataComponentPredicate.Container(
                CollectionPredicate.<ItemStack, ItemPredicate>builder()
                        .matchSize(new Range.Int(1, 3))
                        .mustContain(new ItemPredicate(List.of(Material.STONE)))
                        .mustMatchCount(new ItemPredicate(List.of(Material.DIAMOND_BLOCK)), new Range.Int(2, 3))
                        .build()
        );
        var empty = new DataComponentPredicate.Container(CollectionPredicate.<ItemStack, ItemPredicate>builder().build());

        assertPass(new DataComponentPredicate.Container(null), EMPTY_HOLDER);
        assertPass(empty, DataComponents.CONTAINER, List.of());
        assertFail(container, DataComponents.CONTAINER, List.of());

        assertFail(container, DataComponents.CONTAINER, List.of(
                ItemStack.of(Material.STONE),
                ItemStack.of(Material.DIAMOND_BLOCK),
                ItemStack.of(Material.DIAMOND_BLOCK),
                ItemStack.of(Material.DIRT) // This dirt makes the collection too large (4 > 3)
        ));

        assertFail(container, DataComponents.CONTAINER, List.of(
                ItemStack.of(Material.STONE),
                ItemStack.of(Material.DIAMOND_BLOCK),
                ItemStack.of(Material.GOLD_BLOCK) // There aren't enough diamond blocks (must be in the range  2..3)
        ));

        assertFail(container, DataComponents.CONTAINER, List.of(
                ItemStack.of(Material.GOLD_BLOCK), // This list doesn't contain stone
                ItemStack.of(Material.DIAMOND_BLOCK),
                ItemStack.of(Material.DIAMOND_BLOCK)
        ));

        assertPass(container, DataComponents.CONTAINER, List.of(
                ItemStack.of(Material.STONE),
                ItemStack.of(Material.DIAMOND_BLOCK),
                ItemStack.of(Material.DIAMOND_BLOCK)
        ));

        // If the inner CollectionPredicate is null, the predicate always returns true
        assertPass(new DataComponentPredicate.Container(null), EMPTY_HOLDER);
        assertPass(new DataComponentPredicate.Container(null), DataComponents.CONTAINER, List.of());
    }

    @Test
    void testFireworks() {
        var empty = new DataComponentPredicate.Fireworks(null, null);

        assertPass(empty, DataComponents.FIREWORKS, FireworkList.EMPTY);
        assertFail(empty, EMPTY_HOLDER); // Even though the predicate has no checks, the FIREWORKS component still must be present

        var fireworks = new DataComponentPredicate.Fireworks(
                CollectionPredicate.<FireworkExplosion, DataComponentPredicate.FireworkExplosionPredicate>builder()
                        .mustMatchCount(new DataComponentPredicate.FireworkExplosionPredicate(FireworkExplosion.Shape.CREEPER, null, null), new Range.Int(1, 2))
                        .mustContain(new DataComponentPredicate.FireworkExplosionPredicate(null, true, null))
                        .mustContain(new DataComponentPredicate.FireworkExplosionPredicate(null, null, true))
                        .build(),
                new Range.Int(3, 5)
        );

        assertPass(fireworks, DataComponents.FIREWORKS, new FireworkList(3, List.of(
                new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), true, false),
                new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), true, false),
                new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), false, true)
        )));

        assertFail(fireworks, DataComponents.FIREWORKS, new FireworkList(1, List.of( // Flight duration must be in the range 3..5
                new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), true, false),
                new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), true, false),
                new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), false, true)
        )));

        assertFail(fireworks, DataComponents.FIREWORKS, new FireworkList(3, List.of(
                new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), true, false),
                new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), true, false),
                new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), false, true) // Too many with a shape of CREEPER, should be in the range 1..2
        )));

        assertFail(fireworks, DataComponents.FIREWORKS, new FireworkList(3, List.of(
                new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), true, false),
                new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), true, false),
                new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, List.of(NamedTextColor.YELLOW), List.of(NamedTextColor.GRAY), false, false) // None of the fireworks contain `hasTwinkle` == true
        )));

        var durationOnly = new DataComponentPredicate.Fireworks(CollectionPredicate.<FireworkExplosion, DataComponentPredicate.FireworkExplosionPredicate>builder().build(), new Range.Int(1, 3));
        assertPass(durationOnly, DataComponents.FIREWORKS, new FireworkList(3, List.of()));
        assertFail(durationOnly, DataComponents.FIREWORKS, new FireworkList(4, List.of()));
        assertFail(durationOnly, EMPTY_HOLDER);
    }

    @Test
    void testFireworkExplosion() {
        var empty = new DataComponentPredicate.FireworkExplosion(null, null, null);
        var shapeOnly = new DataComponentPredicate.FireworkExplosion(FireworkExplosion.Shape.CREEPER, null, null);
        var twinkleOnly = new DataComponentPredicate.FireworkExplosion(null, true, null);
        var trailOnly = new DataComponentPredicate.FireworkExplosion(null, null, true);
        var all = new DataComponentPredicate.FireworkExplosion(FireworkExplosion.Shape.CREEPER, true, false);

        assertFail(empty, EMPTY_HOLDER);
        assertPass(empty, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(), List.of(), true, true));

        assertPass(shapeOnly, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(), List.of(), true, true));
        assertFail(shapeOnly, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, List.of(), List.of(), true, true));

        assertPass(twinkleOnly, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(), List.of(), true, true));
        assertFail(twinkleOnly, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(), List.of(), false, true));

        assertPass(trailOnly, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(), List.of(), true, true));
        assertFail(trailOnly, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, List.of(), List.of(), true, false));

        assertPass(all, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(), List.of(), true, false));
        assertFail(all, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.LARGE_BALL, List.of(), List.of(), true, false));
        assertFail(all, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(), List.of(), true, true));
        assertFail(all, DataComponents.FIREWORK_EXPLOSION, new FireworkExplosion(FireworkExplosion.Shape.CREEPER, List.of(), List.of(), false, false));
    }

    @Test
    void testWritableBook() {
        var empty = new DataComponentPredicate.WritableBook(CollectionPredicate.<FilteredText<String>, DataComponentPredicate.WritableBook.PagePredicate>builder().build());
        var writableBook = new DataComponentPredicate.WritableBook(CollectionPredicate.<FilteredText<String>, DataComponentPredicate.WritableBook.PagePredicate>builder()
                .mustContain(new DataComponentPredicate.WritableBook.PagePredicate("Hello, world!"))
                .build()
        );

        assertPass(new DataComponentPredicate.WritableBook(null), EMPTY_HOLDER);
        assertFail(empty, EMPTY_HOLDER);
        assertPass(empty, DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY);

        assertPass(writableBook, DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(List.of(new FilteredText<>("Hello, world!", "???"))));
        assertFail(writableBook, DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(List.of(new FilteredText<>("???", "Hello, world!"))));
        assertFail(writableBook, DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY);
    }

    @Test
    void testWrittenBook() {
        var empty = new DataComponentPredicate.WrittenBook(CollectionPredicate.<FilteredText<Component>, DataComponentPredicate.WrittenBook.PagePredicate>builder().build(), null, null, null, null);
        var writableBook = new DataComponentPredicate.WrittenBook(
                CollectionPredicate.<FilteredText<Component>, DataComponentPredicate.WrittenBook.PagePredicate>builder()
                        .mustContain(new DataComponentPredicate.WrittenBook.PagePredicate(Component.text("Hello, world!")))
                        .build(),
                "Author", "Title", new Range.Int(1, 5), true
        );

        assertFail(empty, EMPTY_HOLDER);
        assertPass(empty, DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY);

        assertPass(writableBook, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(new FilteredText<>("Title", ""), "Author", 2, List.of(new FilteredText<>(Component.text("Hello, world!"), null)), true));
        assertFail(writableBook, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(new FilteredText<>("Not the title", ""), "Author", 2, List.of(new FilteredText<>(Component.text("Hello, world!"), null)), true));
        assertFail(writableBook, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(new FilteredText<>("Title", ""), "Not the author", 2, List.of(new FilteredText<>(Component.text("Hello, world!"), null)), true));
        assertFail(writableBook, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(new FilteredText<>("Title", ""), "Author", 6, List.of(new FilteredText<>(Component.text("Hello, world!"), null)), true));
        assertFail(writableBook, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(new FilteredText<>("Title", ""), "Author", 2, List.of(new FilteredText<>(Component.text("Page content that doesn't match"), null)), true));
        assertFail(writableBook, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(new FilteredText<>("Title", ""), "Author", 2, List.of(new FilteredText<>(Component.text("Hello, world!"), null)), false));
        assertFail(writableBook, DataComponents.WRITTEN_BOOK_CONTENT, WrittenBookContent.EMPTY);

        var pagesOnly = new DataComponentPredicate.WrittenBook(
                CollectionPredicate.<FilteredText<Component>, DataComponentPredicate.WrittenBook.PagePredicate>builder()
                        .mustContain(new DataComponentPredicate.WrittenBook.PagePredicate(Component.text("Hello, world!")))
                        .build(),
                null, null, null, null
        );

        assertPass(pagesOnly, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent("", "", List.of(Component.text("Hello, world!"))));
        assertFail(pagesOnly, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent("", "", List.of()));

        var authorOnly = new DataComponentPredicate.WrittenBook(null, "Author", null, null, null);
        assertPass(authorOnly, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent("", "Author", List.of()));
        assertFail(authorOnly, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent("", "", List.of()));

        var titleOnly = new DataComponentPredicate.WrittenBook(null, null, "Title", null, null);
        assertPass(titleOnly, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent("Title", "", List.of()));
        assertFail(titleOnly, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent("", "", List.of()));

        var generationOnly = new DataComponentPredicate.WrittenBook(null, null, null, new Range.Int(5), null);
        assertPass(generationOnly, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent("", "", 5, List.of(), true));
        assertFail(generationOnly, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent("", "", 2, List.of(), true));

        var resolvedOnly = new DataComponentPredicate.WrittenBook(null, null, null, null, true);
        assertPass(resolvedOnly, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent("", "", 0, List.of(), true));
        assertFail(resolvedOnly, DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent("", "", 0, List.of(), false));
    }

    @Test
    void testAttributeModifiers() {
        var empty = new DataComponentPredicate.AttributeModifiers(CollectionPredicate.<AttributeList.Modifier, DataComponentPredicate.AttributeModifierPredicate>builder().build());

        assertFail(empty, EMPTY_HOLDER);
        assertPass(empty, DataComponents.ATTRIBUTE_MODIFIERS, AttributeList.EMPTY);

        var attributeModifiers = new DataComponentPredicate.AttributeModifiers(
                CollectionPredicate.<AttributeList.Modifier, DataComponentPredicate.AttributeModifierPredicate>builder()
                        .mustContain(new DataComponentPredicate.AttributeModifierPredicate(Attribute.ATTACK_SPEED, Key.key("minestom:extra_attack_speed"), new Range.Double(0.0, 5.0), AttributeOperation.ADD_MULTIPLIED_BASE, EquipmentSlotGroup.MAIN_HAND))
                        .build()
        );

        assertPass(attributeModifiers, DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(new AttributeList.Modifier(Attribute.ATTACK_SPEED, new AttributeModifier(Key.key("minestom:extra_attack_speed"), 2.0, AttributeOperation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAIN_HAND)));
        assertFail(attributeModifiers, DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(new AttributeList.Modifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(Key.key("minestom:extra_attack_speed"), 2.0, AttributeOperation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAIN_HAND)));
        assertFail(attributeModifiers, DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(new AttributeList.Modifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(Key.key("minestom:different_key"), 2.0, AttributeOperation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAIN_HAND)));
        assertFail(attributeModifiers, DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(new AttributeList.Modifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(Key.key("minestom:extra_attack_speed"), 10.0, AttributeOperation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAIN_HAND)));
        assertFail(attributeModifiers, DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(new AttributeList.Modifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(Key.key("minestom:extra_attack_speed"), 2.0, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.MAIN_HAND)));
        assertFail(attributeModifiers, DataComponents.ATTRIBUTE_MODIFIERS, new AttributeList(new AttributeList.Modifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(Key.key("minestom:extra_attack_speed"), 2.0, AttributeOperation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.OFF_HAND)));
    }

    @Test
    void testTrim() {
        var empty = new DataComponentPredicate.ArmorTrim(null, null);
        var materialOnly = new DataComponentPredicate.ArmorTrim(RegistryTag.direct(TrimMaterial.AMETHYST), null);
        var patternOnly = new DataComponentPredicate.ArmorTrim(null, RegistryTag.direct(TrimPattern.BOLT));
        var both = new DataComponentPredicate.ArmorTrim(RegistryTag.direct(TrimMaterial.AMETHYST), RegistryTag.direct(TrimPattern.BOLT));

        assertFail(empty, EMPTY_HOLDER);
        assertPass(empty, DataComponents.TRIM, new ArmorTrim(TrimMaterial.COPPER, TrimPattern.FLOW));

        assertPass(materialOnly, DataComponents.TRIM, new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.FLOW));
        assertFail(materialOnly, DataComponents.TRIM, new ArmorTrim(TrimMaterial.COPPER, TrimPattern.FLOW));

        assertPass(patternOnly, DataComponents.TRIM, new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.BOLT));
        assertFail(patternOnly, DataComponents.TRIM, new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.FLOW));

        assertPass(both, DataComponents.TRIM, new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.BOLT));
        assertFail(both, DataComponents.TRIM, new ArmorTrim(TrimMaterial.AMETHYST, TrimPattern.FLOW));
        assertFail(both, DataComponents.TRIM, new ArmorTrim(TrimMaterial.COPPER, TrimPattern.BOLT));
    }

    @Test
    void testJukeboxPlayable() {
        var none = new DataComponentPredicate.JukeboxPlayable(null);
        var single = new DataComponentPredicate.JukeboxPlayable(RegistryTag.direct(JukeboxSong.CAT));
        var multiple = new DataComponentPredicate.JukeboxPlayable(RegistryTag.direct(JukeboxSong.CAT, JukeboxSong.STAL, JukeboxSong.WAIT));

        assertFail(none, EMPTY_HOLDER);
        assertPass(none, DataComponents.JUKEBOX_PLAYABLE, JukeboxSong.PIGSTEP);

        assertPass(single, DataComponents.JUKEBOX_PLAYABLE, JukeboxSong.CAT);
        assertFail(single, DataComponents.JUKEBOX_PLAYABLE, JukeboxSong.BLOCKS);

        assertPass(multiple, DataComponents.JUKEBOX_PLAYABLE, JukeboxSong.CAT);
        assertPass(multiple, DataComponents.JUKEBOX_PLAYABLE, JukeboxSong.STAL);
        assertPass(multiple, DataComponents.JUKEBOX_PLAYABLE, JukeboxSong.WAIT);
        assertFail(multiple, DataComponents.JUKEBOX_PLAYABLE, JukeboxSong.BLOCKS);
    }
}
