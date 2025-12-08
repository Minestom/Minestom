package net.minestom.codegen;

import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

import static net.minestom.codegen.CodegenValue.builder;

public final class CodegenRegistries {
    private static final CodegenRegistry REGISTRY = createCodegenRegistry();

    public static CodegenRegistry registry() {
        return REGISTRY;
    }

    // Required if we want to eventually generate BlockValues for example, as it needs cross values.
    // In the future the data generator might be able to do this task, but unlikely
    private static CodegenRegistry createCodegenRegistry() {
        return CodegenRegistry.builder(CodegenRegistries::resourceReader)
                .putAll(specialRegistry())
                .putAll(staticRegistry())
                .putAll(dynamicRegistry())
                .build();
    }

    private static @Nullable InputStreamReader resourceReader(String filename) {
        var out = Generators.class.getResourceAsStream("/%s.json".formatted(filename));
        if (out == null) return null;
        return new InputStreamReader(out);
    }

    private static Collection<CodegenValue> specialRegistry() {
        return List.of(
                builder().specialType()
                        .generator(DyeColorGenerator::new)
                        .namespace("dye_colors")
                        .packageName("net.minestom.server.color")
                        .typeName("Color")
                        .generatedName("DyeColor")
                        .build(),
                builder().specialType() // Static special codegenType
                        .generator(ParticleGenerator::new)
                        .namespace("particle")
                        .packageName("net.minestom.server.particle")
                        .typeName("Particle")
                        .loaderName("ParticleImpl")
                        .generatedName("Particles")
                        .registryKeyOverride(CodegenValue.Type.STATIC)
                        .build(),
                builder().specialType()
                        .generator(ConstantsGenerator::new)
                        .namespace("constants")
                        .packageName("net.minestom.server")
                        .typeName("MinecraftServer")
                        .generatedName("MinecraftConstants")
                        .build(),
                builder().specialType()
                        .generator(RecipeTypeGenerator::new)
                        .namespace("recipe_types")
                        .packageName("net.minestom.server.recipe")
                        .typeName("RecipeType")
                        .build(),
                builder().specialType()
                        .generator(GenericEnumGenerator::new)
                        .namespace("recipe_display_types")
                        .packageName("net.minestom.server.recipe.display")
                        .typeName("RecipeDisplayType")
                        .build(),
                builder().specialType()
                        .generator(GenericEnumGenerator::new)
                        .namespace("slot_display_types")
                        .packageName("net.minestom.server.recipe.display")
                        .typeName("SlotDisplayType")
                        .build(),
                builder().specialType()
                        .generator(GenericEnumGenerator::new)
                        .namespace("recipe_book_categories")
                        .packageName("net.minestom.server.recipe")
                        .typeName("RecipeBookCategory")
                        .build(),
                builder().specialType()
                        .generator(GenericPackagePrivateEnumGenerator::new)
                        .namespace("consume_effects")
                        .packageName("net.minestom.server.item.component")
                        .typeName("ConsumeEffectType")
                        .build(),
                builder().specialType()
                        .generator(GenericEnumGenerator::new)
                        .namespace("command_arguments")
                        .packageName("net.minestom.server.command")
                        .typeName("ArgumentParserType")
                        .build(),
                builder().specialType()
                        .generator(GenericEnumGenerator::new)
                        .namespace("villager_types")
                        .packageName("net.minestom.server.entity")
                        .typeName("VillagerType")
                        .build(),
                builder().specialType()
                        .generator(WorldEventGenerator::new)
                        .namespace("world_events")
                        .packageName("net.minestom.server.worldevent")
                        .typeName("WorldEvent")
                        .build(),
                builder().specialType()
                        .generator(BuiltInRegistryGenerator::new)
                        .namespace("built_in_registries")
                        .packageName("net.minestom.server.registry")
                        .typeName("BuiltinRegistries")
                        .build()
        );
    }

    private static Collection<CodegenValue> dynamicRegistry() {
        return List.of(
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("chat_type")
                        .packageName("net.minestom.server.message")
                        .typeName("ChatType")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("dimension_type")
                        .packageName("net.minestom.server.world")
                        .typeName("DimensionType")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("damage_type")
                        .packageName("net.minestom.server.entity.damage")
                        .typeName("DamageType")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("trim_material")
                        .packageName("net.minestom.server.item.armor")
                        .typeName("TrimMaterial")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("trim_pattern")
                        .packageName("net.minestom.server.item.armor")
                        .typeName("TrimPattern")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("banner_pattern")
                        .packageName("net.minestom.server.instance.block.banner")
                        .typeName("BannerPattern")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("enchantment")
                        .packageName("net.minestom.server.item.enchant")
                        .typeName("Enchantment")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("painting_variant")
                        .packageName("net.minestom.server.entity.metadata.other")
                        .typeName("PaintingVariant")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("jukebox_song")
                        .packageName("net.minestom.server.instance.block.jukebox")
                        .typeName("JukeboxSong")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("instrument")
                        .packageName("net.minestom.server.item.instrument")
                        .typeName("Instrument")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("wolf_variant")
                        .packageName("net.minestom.server.entity.metadata.animal.tameable")
                        .typeName("WolfVariant")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("wolf_sound_variant")
                        .packageName("net.minestom.server.entity.metadata.animal.tameable")
                        .typeName("WolfSoundVariant")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("cat_variant")
                        .packageName("net.minestom.server.entity.metadata.animal.tameable")
                        .typeName("CatVariant")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("chicken_variant")
                        .packageName("net.minestom.server.entity.metadata.animal")
                        .typeName("ChickenVariant")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("cow_variant")
                        .packageName("net.minestom.server.entity.metadata.animal")
                        .typeName("CowVariant")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("frog_variant")
                        .packageName("net.minestom.server.entity.metadata.animal")
                        .typeName("FrogVariant")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("pig_variant")
                        .packageName("net.minestom.server.entity.metadata.animal")
                        .typeName("PigVariant")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("worldgen/biome")
                        .packageName("net.minestom.server.world.biome")
                        .typeName("Biome")
                        .build(),
                builder().dynamicType()
                        .generator(RegistryGenerator::new)
                        .namespace("dialog")
                        .packageName("net.minestom.server.dialog")
                        .typeName("Dialog")
                        .generatedName("Dialogs")
                        .build()
        );
    }

    private static Collection<CodegenValue> staticRegistry() {
        return List.of(
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("block")
                        .packageName("net.minestom.server.instance.block")
                        .typeName("Block")
                        .generatedName("Blocks")
                        .loaderName("BlockImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("item")
                        .packageName("net.minestom.server.item")
                        .typeName("Material")
                        .generatedName("Materials")
                        .loaderName("MaterialImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("entity_type")
                        .packageName("net.minestom.server.entity")
                        .typeName("EntityType")
                        .generatedName("EntityTypes")
                        .loaderName("EntityTypeImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("potion_effect")
                        .packageName("net.minestom.server.potion")
                        .typeName("PotionEffect")
                        .generatedName("PotionEffects")
                        .loaderName("PotionEffectImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("potion_type")
                        .packageName("net.minestom.server.potion")
                        .typeName("PotionType")
                        .generatedName("PotionTypes")
                        .loaderName("PotionTypeImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("sound_event")
                        .packageName("net.minestom.server.sound")
                        .typeName("SoundEvent")
                        .generatedName("SoundEvents")
                        .loaderName("BuiltinSoundEvent")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("custom_statistics")
                        .packageName("net.minestom.server.statistic")
                        .typeName("StatisticType")
                        .generatedName("StatisticTypes")
                        .loaderName("StatisticTypeImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("attribute")
                        .packageName("net.minestom.server.entity.attribute")
                        .typeName("Attribute")
                        .generatedName("Attributes")
                        .loaderName("AttributeImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("feature_flag")
                        .packageName("net.minestom.server")
                        .typeName("FeatureFlag")
                        .generatedName("FeatureFlags")
                        .loaderName("FeatureFlagImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("fluid")
                        .packageName("net.minestom.server.instance.fluid")
                        .typeName("Fluid")
                        .generatedName("Fluids")
                        .loaderName("FluidImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("villager_profession")
                        .packageName("net.minestom.server.entity")
                        .typeName("VillagerProfession")
                        .generatedName("VillagerProfessions")
                        .loaderName("VillagerProfessionImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("game_event")
                        .packageName("net.minestom.server.game")
                        .typeName("GameEvent")
                        .generatedName("GameEvents")
                        .loaderName("GameEventImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("block_sound_type")
                        .packageName("net.minestom.server.instance.block")
                        .typeName("BlockSoundType")
                        .generatedName("BlockSoundTypes")
                        .loaderName("BlockSoundImpl")
                        .build(),
                builder().staticType()
                        .generator(RegistryGenerator::new)
                        .namespace("block_entity_types")
                        .packageName("net.minestom.server.instance.block")
                        .typeName("BlockEntityType")
                        .generatedName("BlockEntityTypes")
                        .loaderName("BlockEntityTypeImpl")
                        .build()
        );
    }
}
