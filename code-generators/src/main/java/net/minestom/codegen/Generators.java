package net.minestom.codegen;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Generators {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <target folder>");
            return;
        }
        Path outputFolder = Path.of(args[0]);

        // Special generator
        new ConstantsGenerator(Objects.requireNonNull(resource("constants"), "Constants not found"), outputFolder).generate();

        // Custom static generators
        var customStaticRegistries = customStaticRegistries();
        var customStaticRegistriesMap = customStaticRegistries.stream().collect(Collectors.toMap(Entry::namespace, Function.identity()));
        final Entry.Static particleKeys = customStaticRegistriesMap.get("particle");
        new ParticleGenerator(particleKeys, outputFolder).generate();

        // Enum generators.
        new DyeColorGenerator(customStaticRegistriesMap.get("dye_colors").resource(), outputFolder).generate();
        // GenericEnum based generators.
        new RecipeTypeGenerator(customStaticRegistriesMap.get("recipe_types"), outputFolder).generate();
        new GenericEnumGenerator(customStaticRegistriesMap.get("recipe_display_types"), outputFolder).generate();
        new GenericEnumGenerator(customStaticRegistriesMap.get("slot_display_types"), outputFolder).generate();
        new GenericEnumGenerator(customStaticRegistriesMap.get("recipe_book_categories"), outputFolder).generate();
        new GenericEnumGenerator(customStaticRegistriesMap.get("consume_effects"), outputFolder).packagePrivate().generate();
        new GenericEnumGenerator(customStaticRegistriesMap.get("command_arguments"), outputFolder).generate();
        new GenericEnumGenerator(customStaticRegistriesMap.get("villager_types"), outputFolder).generate();
        new WorldEventGenerator(customStaticRegistriesMap.get("world_events"), outputFolder).generate();

        var generator = new RegistryGenerator(outputFolder);

        // Particle Keys
        generator.generateKeys(particleKeys.resource(), particleKeys.packageName(), particleKeys.typeName(), particleKeys.keysName(), true);

        // Static registries
        var staticRegistries = Generators.staticRegistries();
        for (var registry : staticRegistries) {
            generator.generateKeys(registry.resource(), registry.packageName(), registry.typeName(), registry.keysName(), true);
            generator.generate(registry.resource(), registry.packageName(), registry.typeName(), registry.loaderName(), registry.keysName(), registry.generatedName());
        }

        // Dynamic registries
        var dynamicRegistries = Generators.dynamicRegistries();
        for (var registry : dynamicRegistries) {
            if (registry.ignoreKeys()) continue;
            generator.generateKeys(registry.resource(), registry.packageName(), registry.typeName(), registry.generatedName(), false);
        }
        // Combine static registries with custom static registries to generate RegistryKeys
        var staticEntriesCombined = Stream.concat(staticRegistries.stream(), customStaticRegistries.stream()).toList();

        // Generate tags if they exist.
        for (var tag : Stream.concat(dynamicRegistries.stream(), staticEntriesCombined.stream()).toList()) {
            generator.generateTags(tag.tagResource(), tag.packageName(), tag.typeName(), tag.tagsName());
        }

        // Generate RegistryKeys
        generator.generateRegistryKeys(staticEntriesCombined, dynamicRegistries, "net.minestom.server.registry", "RegistryKey", "BuiltinRegistries");

        System.out.println("Finished generating code");
    }

    private static List<Entry.Static> customStaticRegistries() {
        return List.of(
                new Entry.Static("dye_colors", "net.minestom.server.color", "DyeColor", null, null),
                new Entry.Static("particle", "net.minestom.server.particle", "Particle"),
                new Entry.Static("recipe_types", "net.minestom.server.recipe", "RecipeType", null, null),
                new Entry.Static("recipe_display_types", "net.minestom.server.recipe.display", "RecipeDisplayType", null, null),
                new Entry.Static("slot_display_types", "net.minestom.server.recipe.display", "SlotDisplayType", null, null),
                new Entry.Static("recipe_book_categories", "net.minestom.server.recipe", "RecipeBookCategory", null, null),
                new Entry.Static("consume_effects", "net.minestom.server.item.component", "ConsumeEffect", "ConsumeEffectType", "ConsumeEffectType"),
                new Entry.Static("command_arguments", "net.minestom.server.command", "ArgumentParserType", null, null),
                new Entry.Static("villager_types", "net.minestom.server.entity", "VillagerType", null, null),
                new Entry.Static("world_events", "net.minestom.server.worldevent", "WorldEvent", null, null)
        );
    }

    private static List<Entry.Static> staticRegistries() {
        return List.of(
                new Entry.Static("block", "net.minestom.server.instance.block", "Block"),
                new Entry.Static("item", "net.minestom.server.item", "Material"),
                new Entry.Static("entity_type", "net.minestom.server.entity", "EntityType"),
                new Entry.Static("potion_effect", "net.minestom.server.potion", "PotionEffect"),
                new Entry.Static("potion_type", "net.minestom.server.potion", "PotionType"),
                new Entry.Static("sound_event", "net.minestom.server.sound", "SoundEvent", "BuiltinSoundEvent", "SoundEvents","SoundEventKeys", true),
                new Entry.Static("custom_statistics", "net.minestom.server.statistic", "StatisticType"),
                new Entry.Static("attribute", "net.minestom.server.entity.attribute", "Attribute"),
                new Entry.Static("feature_flag", "net.minestom.server", "FeatureFlag"),
                new Entry.Static("fluid", "net.minestom.server.instance.fluid", "Fluid"),
                new Entry.Static("villager_profession", "net.minestom.server.entity", "VillagerProfession"),
                new Entry.Static("game_event", "net.minestom.server.game", "GameEvent"),
                new Entry.Static("block_sound_type", "net.minestom.server.instance.block", "BlockSoundType", "BlockSoundImpl", "BlockSoundTypes")
        );
    }

    private static List<Entry.Dynamic> dynamicRegistries() {
        return List.of(
                new Entry.Dynamic("chat_type", "net.minestom.server.message", "ChatType"),
                new Entry.Dynamic("dimension_type", "net.minestom.server.world", "DimensionType"),
                new Entry.Dynamic("damage_type", "net.minestom.server.entity.damage", "DamageType"),
                new Entry.Dynamic("trim_material", "net.minestom.server.item.armor", "TrimMaterial"),
                new Entry.Dynamic("trim_pattern", "net.minestom.server.item.armor", "TrimPattern"),
                new Entry.Dynamic("banner_pattern", "net.minestom.server.instance.block.banner", "BannerPattern"),
                new Entry.Dynamic("enchantment", "net.minestom.server.item.enchant", "Enchantment"),
                new Entry.Dynamic("painting_variant", "net.minestom.server.entity.metadata.other", "PaintingVariant"),
                new Entry.Dynamic("jukebox_song", "net.minestom.server.instance.block.jukebox", "JukeboxSong"),
                new Entry.Dynamic("instrument", "net.minestom.server.item.instrument", "Instrument"),
                new Entry.Dynamic("wolf_variant", "net.minestom.server.entity.metadata.animal.tameable", "WolfVariant"),
                new Entry.Dynamic("wolf_sound_variant", "net.minestom.server.entity.metadata.animal.tameable", "WolfSoundVariant"),
                new Entry.Dynamic("cat_variant", "net.minestom.server.entity.metadata.animal.tameable", "CatVariant"),
                new Entry.Dynamic("chicken_variant", "net.minestom.server.entity.metadata.animal", "ChickenVariant"),
                new Entry.Dynamic("cow_variant", "net.minestom.server.entity.metadata.animal", "CowVariant"),
                new Entry.Dynamic("frog_variant", "net.minestom.server.entity.metadata.animal", "FrogVariant"),
                new Entry.Dynamic("pig_variant", "net.minestom.server.entity.metadata.animal", "PigVariant"),
                new Entry.Dynamic("worldgen/biome", "net.minestom.server.world.biome", "Biome"),
                new Entry.Dynamic("dialog", "net.minestom.server.dialog", "Dialog", "Dialogs", true)
        );
    }

    public static @Nullable InputStream resource(String name) {
        return Generators.class.getResourceAsStream("/%s.json".formatted(name));
    }
}
