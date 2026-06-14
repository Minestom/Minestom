package net.minestom.codegen;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public final class Generators {
    private static final UnaryOperator<String> IDENTITY = UnaryOperator.identity();
    private static final UnaryOperator<String> STRIP_CRAFTING = name -> name.replace("CRAFTING_", "");

    private static final List<EnumSpec> ENUMS = List.of(
            new EnumSpec("recipe_types.json", "net.minestom.server.recipe", "RecipeType", true, STRIP_CRAFTING, "RecipeTypeGenerator"),
            new EnumSpec("recipe_display_types.json", "net.minestom.server.recipe.display", "RecipeDisplayType"),
            new EnumSpec("slot_display_types.json", "net.minestom.server.recipe.display", "SlotDisplayType"),
            new EnumSpec("recipe_book_categories.json", "net.minestom.server.recipe", "RecipeBookCategory"),
            new EnumSpec("consume_effects.json", "net.minestom.server.item.component", "ConsumeEffectType", false, IDENTITY, "GenericEnumGenerator"),
            new EnumSpec("command_arguments.json", "net.minestom.server.command", "ArgumentParserType"),
            new EnumSpec("villager_types.json", "net.minestom.server.entity", "VillagerType")
    );

    private static final List<StaticRegistrySpec> STATIC_REGISTRIES = List.of(
            new StaticRegistrySpec("block.json", "net.minestom.server.instance.block", "Block", "BlockImpl", "Blocks"),
            new StaticRegistrySpec("item.json", "net.minestom.server.item", "Material", "MaterialImpl", "Materials"),
            new StaticRegistrySpec("entity_type.json", "net.minestom.server.entity", "EntityType", "EntityTypeImpl", "EntityTypes"),
            new StaticRegistrySpec("potion_effect.json", "net.minestom.server.potion", "PotionEffect", "PotionEffectImpl", "PotionEffects"),
            new StaticRegistrySpec("potion_type.json", "net.minestom.server.potion", "PotionType", "PotionTypeImpl", "PotionTypes"),
            new StaticRegistrySpec("sound_event.json", "net.minestom.server.sound", "SoundEvent", "BuiltinSoundEvent", "SoundEvents"),
            new StaticRegistrySpec("custom_statistics.json", "net.minestom.server.statistic", "StatisticType", "StatisticTypeImpl", "StatisticTypes"),
            new StaticRegistrySpec("attribute.json", "net.minestom.server.entity.attribute", "Attribute", "AttributeImpl", "Attributes"),
            new StaticRegistrySpec("feature_flag.json", "net.minestom.server", "FeatureFlag", "FeatureFlagImpl", "FeatureFlags"),
            new StaticRegistrySpec("fluid.json", "net.minestom.server.instance.fluid", "Fluid", "FluidImpl", "Fluids"),
            new StaticRegistrySpec("villager_profession.json", "net.minestom.server.entity", "VillagerProfession", "VillagerProfessionImpl", "VillagerProfessions"),
            new StaticRegistrySpec("game_event.json", "net.minestom.server.game", "GameEvent", "GameEventImpl", "GameEvents"),
            new StaticRegistrySpec("block_sound_type.json", "net.minestom.server.instance.block", "BlockSoundType", "BlockSoundImpl", "BlockSoundTypes"),
            new StaticRegistrySpec("block_entity_types.json", "net.minestom.server.instance.block", "BlockEntityType", "BlockEntityTypeImpl", "BlockEntityTypes")
    );

    private static final List<DynamicRegistrySpec> DYNAMIC_REGISTRIES = List.of(
            new DynamicRegistrySpec("chat_type.json", "net.minestom.server.message", "ChatType"),
            new DynamicRegistrySpec("dimension_type.json", "net.minestom.server.world", "DimensionType"),
            new DynamicRegistrySpec("damage_type.json", "net.minestom.server.entity.damage", "DamageType"),
            new DynamicRegistrySpec("trim_material.json", "net.minestom.server.item.armor", "TrimMaterial"),
            new DynamicRegistrySpec("trim_pattern.json", "net.minestom.server.item.armor", "TrimPattern"),
            new DynamicRegistrySpec("banner_pattern.json", "net.minestom.server.instance.block.banner", "BannerPattern"),
            new DynamicRegistrySpec("enchantment.json", "net.minestom.server.item.enchant", "Enchantment"),
            new DynamicRegistrySpec("painting_variant.json", "net.minestom.server.entity.metadata.other", "PaintingVariant"),
            new DynamicRegistrySpec("jukebox_song.json", "net.minestom.server.instance.block.jukebox", "JukeboxSong"),
            new DynamicRegistrySpec("instrument.json", "net.minestom.server.item.instrument", "Instrument"),
            new DynamicRegistrySpec("wolf_variant.json", "net.minestom.server.entity.metadata.animal.tameable", "WolfVariant"),
            new DynamicRegistrySpec("wolf_sound_variant.json", "net.minestom.server.entity.metadata.animal.tameable", "WolfSoundVariant"),
            new DynamicRegistrySpec("cat_variant.json", "net.minestom.server.entity.metadata.animal.tameable", "CatVariant"),
            new DynamicRegistrySpec("cat_sound_variant.json", "net.minestom.server.entity.metadata.animal.tameable", "CatSoundVariant"),
            new DynamicRegistrySpec("chicken_variant.json", "net.minestom.server.entity.metadata.animal", "ChickenVariant"),
            new DynamicRegistrySpec("chicken_sound_variant.json", "net.minestom.server.entity.metadata.animal", "ChickenSoundVariant"),
            new DynamicRegistrySpec("cow_variant.json", "net.minestom.server.entity.metadata.animal", "CowVariant"),
            new DynamicRegistrySpec("cow_sound_variant.json", "net.minestom.server.entity.metadata.animal", "CowSoundVariant"),
            new DynamicRegistrySpec("frog_variant.json", "net.minestom.server.entity.metadata.animal", "FrogVariant"),
            new DynamicRegistrySpec("pig_variant.json", "net.minestom.server.entity.metadata.animal", "PigVariant"),
            new DynamicRegistrySpec("pig_sound_variant.json", "net.minestom.server.entity.metadata.animal", "PigSoundVariant"),
            new DynamicRegistrySpec("zombie_nautilus_variant.json", "net.minestom.server.entity.metadata.animal", "ZombieNautilusVariant"),
            new DynamicRegistrySpec("worldgen/biome.json", "net.minestom.server.world.biome", "Biome"),
            new DynamicRegistrySpec("timeline.json", "net.minestom.server.world.timeline", "Timeline"),
            new DynamicRegistrySpec("world_clock.json", "net.minestom.server.world.clock", "WorldClock"),
            new DynamicRegistrySpec("clock_time_marker.json", "net.minestom.server.world.clock", "ClockTimeMarker")
    );

    static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <target folder>");
            return;
        }
        generateAll(Path.of(args[0]));
        System.out.println("Finished generating code");
    }

    public static void generateAll(Path outputFolder) {
        Codegen codegen = new Codegen(outputFolder);

        new DyeColorGenerator(codegen).generate();
        new ParticleGenerator(codegen).generate();
        new ConstantsGenerator(codegen).generate();
        new GameRuleGenerator(codegen).generate();

        GenericEnumGenerator enumGenerator = new GenericEnumGenerator(codegen);
        ENUMS.forEach(enumGenerator::generate);

        new WorldEventGenerator(codegen, "net.minestom.server.worldevent", "WorldEvent", "world_events.json").generate();

        RegistryGenerator registryGenerator = new RegistryGenerator(codegen);
        STATIC_REGISTRIES.forEach(registryGenerator::generate);
        DYNAMIC_REGISTRIES.forEach(registryGenerator::generate);
    }

    record EnumSpec(String resource, String packageName, String className, boolean isPublic,
                    UnaryOperator<String> constantNameTransform, String generatorName) {
        EnumSpec(String resource, String packageName, String className) {
            this(resource, packageName, className, true, IDENTITY, "GenericEnumGenerator");
        }

        EnumSpec {
            Objects.requireNonNull(resource, "resource cannot be null");
            Objects.requireNonNull(packageName, "packageName cannot be null");
            Objects.requireNonNull(className, "className cannot be null");
            Objects.requireNonNull(constantNameTransform, "constantNameTransform cannot be null");
            Objects.requireNonNull(generatorName, "generatorName cannot be null");
        }
    }

    record StaticRegistrySpec(String resource, String packageName, String typeName, String loaderName,
                              String generatedName) {
        StaticRegistrySpec {
            Objects.requireNonNull(resource, "resource cannot be null");
            Objects.requireNonNull(packageName, "packageName cannot be null");
            Objects.requireNonNull(typeName, "typeName cannot be null");
            Objects.requireNonNull(loaderName, "loaderName cannot be null");
            Objects.requireNonNull(generatedName, "generatedName cannot be null");
        }
    }

    record DynamicRegistrySpec(String resource, String packageName, String typeName, String generatedName) {
        DynamicRegistrySpec(String resource, String packageName, String typeName) {
            this(resource, packageName, typeName, typeName + "s");
        }

        DynamicRegistrySpec {
            Objects.requireNonNull(resource, "resource cannot be null");
            Objects.requireNonNull(packageName, "packageName cannot be null");
            Objects.requireNonNull(typeName, "typeName cannot be null");
            Objects.requireNonNull(generatedName, "generatedName cannot be null");
        }
    }
}
