package net.minestom.codegen;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Objects;

import static net.minestom.codegen.CodegenValue.builder;

public final class Generators {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <target folder>");
            return;
        }
        Path outputFolder = Path.of(args[0]);

        CodegenRegistry registry = codegenRegistry();

        var generator = new RegistryGenerator(outputFolder);
        for (CodegenValue value: registry.registry().values()) {
            switch (value.type()) {
                case STATIC, DYNAMIC -> generator.generate(registry, value);
                case SPECIAL -> resolveSpecialGenerator(value.resource(), outputFolder).generate(registry, value);
            }
        }

        System.out.println("Finished generating code");
    }

    private static @Nullable InputStreamReader resourceReader(String filename) {
        var out = Generators.class.getResourceAsStream("/%s.json".formatted(filename));
        if (out == null) return null;
        return new InputStreamReader(out);
    }

    // Required if we want to eventually generate BlockValues for example, as it needs cross registry.
    // In the future the data generator might be able to do this task, but unlikely
    private static CodegenRegistry codegenRegistry() {
        return CodegenRegistry.builder(Generators::resourceReader)
                .putAll(
                        // --- SPECIAL GENERATORS (Custom logic) ---
                        builder().specialType()
                                .namespace("dye_colors")
                                .packageName("net.minestom.server.color")
                                .typeName("Color")
                                .generatedName("DyeColor")
                                .build(),
                        builder().specialType() // Static special type
                                .namespace("particle")
                                .packageName("net.minestom.server.particle")
                                .typeName("Particle")
                                .loaderName("ParticleImpl")
                                .generatedName("Particles")
                                .build(),
                        builder().specialType()
                                .namespace("constants")
                                .packageName("net.minestom.server")
                                .typeName("MinecraftServer")
                                .generatedName("MinecraftConstants")
                                .build(),
                        builder().specialType()
                                .namespace("recipe_types")
                                .packageName("net.minestom.server.recipe")
                                .typeName("RecipeType")
                                .build(),
                        builder().specialType()
                                .namespace("recipe_display_types")
                                .packageName("net.minestom.server.recipe.display")
                                .typeName("RecipeDisplayType")
                                .build(),
                        builder().specialType()
                                .namespace("slot_display_types")
                                .packageName("net.minestom.server.recipe.display")
                                .typeName("SlotDisplayType")
                                .build(),
                        builder().specialType()
                                .namespace("recipe_book_categories")
                                .packageName("net.minestom.server.recipe")
                                .typeName("RecipeBookCategory")
                                .build(),
                        builder().specialType()
                                .namespace("consume_effects")
                                .packageName("net.minestom.server.item.component")
                                .typeName("ConsumeEffectType")
                                .build(),
                        builder().specialType()
                                .namespace("command_arguments")
                                .packageName("net.minestom.server.command")
                                .typeName("ArgumentParserType")
                                .build(),
                        builder().specialType()
                                .namespace("villager_types")
                                .packageName("net.minestom.server.entity")
                                .typeName("VillagerType")
                                .build(),
                        builder().specialType()
                                .namespace("world_events")
                                .packageName("net.minestom.server.worldevent")
                                .typeName("WorldEvent")
                                .build(),

                        // --- STATIC REGISTRIES (Impl class + constant class) ---
                        builder().staticType()
                                .namespace("block")
                                .packageName("net.minestom.server.instance.block")
                                .typeName("Block")
                                .generatedName("Blocks")
                                .loaderName("BlockImpl")
                                .build(),
                        builder().staticType()
                                .namespace("item")
                                .packageName("net.minestom.server.item")
                                .typeName("Material")
                                .generatedName("Materials")
                                .loaderName("MaterialImpl")
                                .build(),
                        builder().staticType()
                                .namespace("entity_type")
                                .packageName("net.minestom.server.entity")
                                .typeName("EntityType")
                                .generatedName("EntityTypes")
                                .loaderName("EntityTypeImpl")
                                .build(),
                        builder().staticType()
                                .namespace("potion_effect")
                                .packageName("net.minestom.server.potion")
                                .typeName("PotionEffect")
                                .generatedName("PotionEffects")
                                .loaderName("PotionEffectImpl")
                                .build(),
                        builder().staticType()
                                .namespace("potion_type")
                                .packageName("net.minestom.server.potion")
                                .typeName("PotionType")
                                .generatedName("PotionTypes")
                                .loaderName("PotionTypeImpl")
                                .build(),
                        builder().staticType()
                                .namespace("sound_event")
                                .packageName("net.minestom.server.sound")
                                .typeName("SoundEvent")
                                .generatedName("SoundEvents")
                                .loaderName("BuiltinSoundEvent")
                                .build(),
                        builder().staticType()
                                .namespace("custom_statistics")
                                .packageName("net.minestom.server.statistic")
                                .typeName("StatisticType")
                                .generatedName("StatisticTypes")
                                .loaderName("StatisticTypeImpl")
                                .build(),
                        builder().staticType()
                                .namespace("attribute")
                                .packageName("net.minestom.server.entity.attribute")
                                .typeName("Attribute")
                                .generatedName("Attributes")
                                .loaderName("AttributeImpl")
                                .build(),
                        builder().staticType()
                                .namespace("feature_flag")
                                .packageName("net.minestom.server")
                                .typeName("FeatureFlag")
                                .generatedName("FeatureFlags")
                                .loaderName("FeatureFlagImpl")
                                .build(),
                        builder().staticType()
                                .namespace("fluid")
                                .packageName("net.minestom.server.instance.fluid")
                                .typeName("Fluid")
                                .generatedName("Fluids")
                                .loaderName("FluidImpl")
                                .build(),
                        builder().staticType()
                                .namespace("villager_profession")
                                .packageName("net.minestom.server.entity")
                                .typeName("VillagerProfession")
                                .generatedName("VillagerProfessions")
                                .loaderName("VillagerProfessionImpl")
                                .build(),
                        builder().staticType()
                                .namespace("game_event")
                                .packageName("net.minestom.server.game")
                                .typeName("GameEvent")
                                .generatedName("GameEvents")
                                .loaderName("GameEventImpl")
                                .build(),
                        builder().staticType()
                                .namespace("block_sound_type")
                                .packageName("net.minestom.server.instance.block")
                                .typeName("BlockSoundType")
                                .generatedName("BlockSoundTypes")
                                .loaderName("BlockSoundImpl")
                                .build(),
                        builder().staticType()
                                .namespace("block_entity_types")
                                .packageName("net.minestom.server.instance.block")
                                .typeName("BlockEntityType")
                                .generatedName("BlockEntityTypes")
                                .loaderName("BlockEntityTypeImpl")
                                .build(),

                        // --- DYNAMIC REGISTRIES (RegistryKeys + constant class) ---
                        builder().dynamicType()
                                .namespace("chat_type")
                                .packageName("net.minestom.server.message")
                                .typeName("ChatType")
                                .build(),
                        builder().dynamicType()
                                .namespace("dimension_type")
                                .packageName("net.minestom.server.world")
                                .typeName("DimensionType")
                                .build(),
                        builder().dynamicType()
                                .namespace("damage_type")
                                .packageName("net.minestom.server.entity.damage")
                                .typeName("DamageType")
                                .build(),
                        builder().dynamicType()
                                .namespace("trim_material")
                                .packageName("net.minestom.server.item.armor")
                                .typeName("TrimMaterial")
                                .build(),
                        builder().dynamicType()
                                .namespace("trim_pattern")
                                .packageName("net.minestom.server.item.armor")
                                .typeName("TrimPattern")
                                .build(),
                        builder().dynamicType()
                                .namespace("banner_pattern")
                                .packageName("net.minestom.server.instance.block.banner")
                                .typeName("BannerPattern")
                                .build(),
                        builder().dynamicType()
                                .namespace("enchantment")
                                .packageName("net.minestom.server.item.enchant")
                                .typeName("Enchantment")
                                .build(),
                        builder().dynamicType()
                                .namespace("painting_variant")
                                .packageName("net.minestom.server.entity.metadata.other")
                                .typeName("PaintingVariant")
                                .build(),
                        builder().dynamicType()
                                .namespace("jukebox_song")
                                .packageName("net.minestom.server.instance.block.jukebox")
                                .typeName("JukeboxSong")
                                .build(),
                        builder().dynamicType()
                                .namespace("instrument")
                                .packageName("net.minestom.server.item.instrument")
                                .typeName("Instrument")
                                .build(),
                        builder().dynamicType()
                                .namespace("wolf_variant")
                                .packageName("net.minestom.server.entity.metadata.animal.tameable")
                                .typeName("WolfVariant")
                                .build(),
                        builder().dynamicType()
                                .namespace("wolf_sound_variant")
                                .packageName("net.minestom.server.entity.metadata.animal.tameable")
                                .typeName("WolfSoundVariant")
                                .build(),
                        builder().dynamicType()
                                .namespace("cat_variant")
                                .packageName("net.minestom.server.entity.metadata.animal.tameable")
                                .typeName("CatVariant")
                                .build(),
                        builder().dynamicType()
                                .namespace("chicken_variant")
                                .packageName("net.minestom.server.entity.metadata.animal")
                                .typeName("ChickenVariant")
                                .build(),
                        builder().dynamicType()
                                .namespace("cow_variant")
                                .packageName("net.minestom.server.entity.metadata.animal")
                                .typeName("CowVariant")
                                .build(),
                        builder().dynamicType()
                                .namespace("frog_variant")
                                .packageName("net.minestom.server.entity.metadata.animal")
                                .typeName("FrogVariant")
                                .build(),
                        builder().dynamicType()
                                .namespace("pig_variant")
                                .packageName("net.minestom.server.entity.metadata.animal")
                                .typeName("PigVariant")
                                .build(),
                        builder().dynamicType()
                                .namespace("worldgen/biome")
                                .packageName("net.minestom.server.world.biome")
                                .typeName("Biome")
                                .build(),
                        builder().dynamicType()
                                .namespace("dialog")
                                .packageName("net.minestom.server.dialog")
                                .typeName("Dialog")
                                .generatedName("Dialogs")
                                .build()
                )
                .build();
    }

    private static MinestomCodeGenerator resolveSpecialGenerator(String resourceName, Path outputFolder) {
        return switch (resourceName) {
            case "dye_colors" -> new DyeColorGenerator(outputFolder);
            case "particle" -> new ParticleGenerator(outputFolder);
            case "constants" -> new ConstantsGenerator(outputFolder);
            case "recipe_types" -> new RecipeTypeGenerator(outputFolder);
            case "recipe_display_types", "slot_display_types", "recipe_book_categories", "command_arguments",
                 "villager_types" -> new GenericEnumGenerator(outputFolder);
            case "consume_effects" -> new GenericPackagePrivateEnumGenerator(outputFolder);
            case "world_events" -> new WorldEventGenerator(outputFolder);
            default -> throw new RuntimeException("Unknown resource name: " + resourceName);
        };
    }

    private static InputStream resource(String name) {
        return Objects.requireNonNull(Generators.class.getResourceAsStream("/" + name), "Cannot find resource: %s".formatted(name));
    }
}
