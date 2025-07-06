package net.minestom.codegen;

import net.minestom.codegen.color.DyeColorGenerator;
import net.minestom.codegen.particle.ParticleGenerator;
import net.minestom.codegen.recipe.RecipeTypeGenerator;
import net.minestom.codegen.util.GenericEnumGenerator;
import net.minestom.codegen.worldevent.WorldEventGenerator;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Generators {
    private static final Logger LOGGER = LoggerFactory.getLogger(Generators.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            LOGGER.error("Usage: <target folder>");
            return;
        }
        File outputFolder = new File(args[0]);

        // Special generator
        new ConstantsGenerator(resource("constants.json"), outputFolder).generate();

        // Custom static generators
        var customStaticRegistries = customStaticRegistries();
        var customStaticRegistriesMap = customStaticRegistries.stream().collect(Collectors.toMap(StaticEntry::namespace, Function.identity()));
        new ParticleGenerator(customStaticRegistriesMap.get("particle").resource(), outputFolder).generate();

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

        var generator = new CodeGenerator(outputFolder);

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

        LOGGER.info("Finished generating code");
    }

    private static List<StaticEntry> customStaticRegistries() {
        return List.of(
                new StaticEntry("dye_colors", "net.minestom.server.color", "DyeColor", null, null),
                new StaticEntry("particle", "net.minestom.server.particle", "Particle", null, null),
                new StaticEntry("recipe_types", "net.minestom.server.recipe", "RecipeType", null, null),
                new StaticEntry("recipe_display_types", "net.minestom.server.recipe.display", "RecipeDisplayType", null, null),
                new StaticEntry("slot_display_types", "net.minestom.server.recipe.display", "SlotDisplayType", null, null),
                new StaticEntry("recipe_book_categories", "net.minestom.server.recipe", "RecipeBookCategory", null, null),
                new StaticEntry("consume_effects", "net.minestom.server.item.component", "ConsumeEffect", "ConsumeEffectType", "ConsumeEffectType"),
                new StaticEntry("command_arguments", "net.minestom.server.command", "ArgumentParserType", null, null),
                new StaticEntry("villager_types", "net.minestom.server.entity", "VillagerType", null, null),
                new StaticEntry("world_events", "net.minestom.server.worldevent", "WorldEvent", null, null)
        );
    }

    private static List<StaticEntry> staticRegistries() {
        return List.of(
                new StaticEntry("block", "net.minestom.server.instance.block", "Block"),
                new StaticEntry("item", "net.minestom.server.item", "Material"),
                new StaticEntry("entity_type", "net.minestom.server.entity", "EntityType"),
                new StaticEntry("potion_effect", "net.minestom.server.potion", "PotionEffect"),
                new StaticEntry("potion_type", "net.minestom.server.potion", "PotionType"),
                new StaticEntry("sound_event", "net.minestom.server.sound", "SoundEvent", "BuiltinSoundEvent", "SoundEvents","SoundEventKeys", true),
                new StaticEntry("custom_statistics", "net.minestom.server.statistic", "StatisticType"),
                new StaticEntry("attribute", "net.minestom.server.entity.attribute", "Attribute"),
                new StaticEntry("feature_flag", "net.minestom.server", "FeatureFlag"),
                new StaticEntry("fluid", "net.minestom.server.instance.fluid", "Fluid"),
                new StaticEntry("villager_profession", "net.minestom.server.entity", "VillagerProfession"),
                new StaticEntry("game_event", "net.minestom.server.game", "GameEvent"),
                new StaticEntry("block_sound_type", "net.minestom.server.instance.block", "BlockSoundType", "BlockSoundImpl", "BlockSoundTypes")
        );
    }

    private static List<DynamicEntry> dynamicRegistries() {
        return List.of(
                new DynamicEntry("chat_type", "net.minestom.server.message", "ChatType"),
                new DynamicEntry("dimension_type", "net.minestom.server.world", "DimensionType"),
                new DynamicEntry("damage_type", "net.minestom.server.entity.damage", "DamageType"),
                new DynamicEntry("trim_material", "net.minestom.server.item.armor", "TrimMaterial"),
                new DynamicEntry("trim_pattern", "net.minestom.server.item.armor", "TrimPattern"),
                new DynamicEntry("banner_pattern", "net.minestom.server.instance.block.banner", "BannerPattern"),
                new DynamicEntry("enchantment", "net.minestom.server.item.enchant", "Enchantment"),
                new DynamicEntry("painting_variant", "net.minestom.server.entity.metadata.other", "PaintingVariant"),
                new DynamicEntry("jukebox_song", "net.minestom.server.instance.block.jukebox", "JukeboxSong"),
                new DynamicEntry("instrument", "net.minestom.server.item.instrument", "Instrument"),
                new DynamicEntry("wolf_variant", "net.minestom.server.entity.metadata.animal.tameable", "WolfVariant"),
                new DynamicEntry("wolf_sound_variant", "net.minestom.server.entity.metadata.animal.tameable", "WolfSoundVariant"),
                new DynamicEntry("cat_variant", "net.minestom.server.entity.metadata.animal.tameable", "CatVariant"),
                new DynamicEntry("chicken_variant", "net.minestom.server.entity.metadata.animal", "ChickenVariant"),
                new DynamicEntry("cow_variant", "net.minestom.server.entity.metadata.animal", "CowVariant"),
                new DynamicEntry("frog_variant", "net.minestom.server.entity.metadata.animal", "FrogVariant"),
                new DynamicEntry("pig_variant", "net.minestom.server.entity.metadata.animal", "PigVariant"),
                new DynamicEntry("worldgen/biome", "net.minestom.server.world.biome", "Biome"),
                new DynamicEntry("dialog", "net.minestom.server.dialog", "Dialog", "Dialogs", true)
        );
    }

    public static String namespaceToConstant(String namespace) {
        String constant = namespace
                .replace("minecraft:", "")
                .replace(".", "_")
                .replace("/", "_")
                .toUpperCase(Locale.ROOT);
        if (!SourceVersion.isName(constant)) {
            constant = "_" + constant;
        }
        return constant;
    }

    public static String namespaceShort(String namespace) {
        return namespace.replaceFirst("minecraft:", "");
    }

    private static InputStream resource(String name) {
        return Generators.class.getResourceAsStream("/" + name);
    }

    public sealed interface Entry {
        String namespace();
        String packageName();
        String typeName();
        String generatedName();

        default String tagsName() {
            return typeName() + "Tags";
        }

        default InputStream resource() {
            return Generators.class.getResourceAsStream("/%s.json".formatted(namespace()));
        }

        default InputStream tagResource() {
            return Generators.class.getResourceAsStream("/tags/%s.json".formatted(namespace()));
        }
    }

    public record StaticEntry(String namespace, String packageName, String typeName, String loaderName, String generatedName, String keysName, boolean wildcardKey) implements Entry {
        public StaticEntry {
            loaderName = Objects.requireNonNullElse(loaderName, typeName);
            generatedName = Objects.requireNonNullElse(generatedName, typeName);
        }

        StaticEntry(String namespace, String packageName, String typeName) {
            this(namespace, packageName, typeName, typeName + "Impl", typeName + "s", typeName + "Keys", false);
        }
        StaticEntry(String namespace, String packageName, String typeName, @Nullable String loaderName, @Nullable String generatedName) {
            this(namespace, packageName, typeName, loaderName, generatedName, typeName + "Keys", false);
        }
    }

    public record DynamicEntry(String namespace, String packageName, String typeName, String generatedName, boolean ignoreKeys) implements Entry {
        public DynamicEntry(String namespace, String packageName, String typeName) {
            this(namespace, packageName, typeName, typeName + "s", false);
        }
    }
}
