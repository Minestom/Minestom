package net.minestom.codegen;

import net.minestom.codegen.color.DyeColorGenerator;
import net.minestom.codegen.particle.ParticleGenerator;
import net.minestom.codegen.recipe.RecipeTypeGenerator;
import net.minestom.codegen.util.GenericEnumGenerator;
import net.minestom.codegen.worldevent.WorldEventGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import java.io.File;
import java.io.InputStream;
import java.util.Locale;

public final class Generators {
    private static final Logger LOGGER = LoggerFactory.getLogger(Generators.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            LOGGER.error("Usage: <target folder>");
            return;
        }
        File outputFolder = new File(args[0]);

        // Special generators
        new DyeColorGenerator(resource("dye_colors.json"), outputFolder).generate();
        new ParticleGenerator(resource("particles.json"), outputFolder).generate();
        new ConstantsGenerator(resource("constants.json"), outputFolder).generate();
        new RecipeTypeGenerator(resource("recipe_types.json"), outputFolder).generate();
        new GenericEnumGenerator("net.minestom.server.recipe.display", "RecipeDisplayType",
                resource("recipe_display_types.json"), outputFolder).generate();
        new GenericEnumGenerator("net.minestom.server.recipe.display", "SlotDisplayType",
                resource("slot_display_types.json"), outputFolder).generate();
        new GenericEnumGenerator("net.minestom.server.recipe", "RecipeBookCategory",
                resource("recipe_book_categories.json"), outputFolder).generate();
        new GenericEnumGenerator("net.minestom.server.item.component", "ConsumeEffectType",
                resource("consume_effects.json"), outputFolder).packagePrivate().generate();
        new GenericEnumGenerator("net.minestom.server.command", "ArgumentParserType",
                resource("command_arguments.json"), outputFolder).generate();
        new GenericEnumGenerator("net.minestom.server.entity", "VillagerType",
                resource("villager_types.json"), outputFolder).generate();
        new WorldEventGenerator("net.minestom.server.worldevent", "WorldEvent",
                resource("world_events.json"), outputFolder).generate();

        var generator = new CodeGenerator(outputFolder);

        // Static registries
        generator.generate(resource("block.json"), "net.minestom.server.instance.block", "Block", "BlockImpl", "Blocks");
        generator.generate(resource("item.json"), "net.minestom.server.item", "Material", "MaterialImpl", "Materials");
        generator.generate(resource("entity_type.json"), "net.minestom.server.entity", "EntityType", "EntityTypeImpl", "EntityTypes");
        generator.generate(resource("potion_effect.json"), "net.minestom.server.potion", "PotionEffect", "PotionEffectImpl", "PotionEffects");
        generator.generate(resource("potion_type.json"), "net.minestom.server.potion", "PotionType", "PotionTypeImpl", "PotionTypes");
        generator.generate(resource("sound_event.json"), "net.minestom.server.sound", "SoundEvent", "BuiltinSoundEvent", "SoundEvents");
        generator.generate(resource("custom_statistics.json"), "net.minestom.server.statistic", "StatisticType", "StatisticTypeImpl", "StatisticTypes");
        generator.generate(resource("attribute.json"), "net.minestom.server.entity.attribute", "Attribute", "AttributeImpl", "Attributes");
        generator.generate(resource("feature_flag.json"), "net.minestom.server", "FeatureFlag", "FeatureFlagImpl", "FeatureFlags");
        generator.generate(resource("fluid.json"), "net.minestom.server.instance.fluid", "Fluid", "FluidImpl", "Fluids");
        generator.generate(resource("villager_profession.json"), "net.minestom.server.entity", "VillagerProfession", "VillagerProfessionImpl", "VillagerProfessions");
        generator.generate(resource("game_event.json"), "net.minestom.server.game", "GameEvent", "GameEventImpl", "GameEvents");
        generator.generate(resource("block_sound_type.json"), "net.minestom.server.instance.block", "BlockSoundType", "BlockSoundImpl", "BlockSoundTypes");

        // Dynamic registries
        generator.generateKeys(resource("chat_type.json"), "net.minestom.server.message", "ChatType");
        generator.generateKeys(resource("dimension_type.json"), "net.minestom.server.world", "DimensionType");
        generator.generateKeys(resource("damage_type.json"), "net.minestom.server.entity.damage", "DamageType");
        generator.generateKeys(resource("trim_material.json"), "net.minestom.server.item.armor", "TrimMaterial");
        generator.generateKeys(resource("trim_pattern.json"), "net.minestom.server.item.armor", "TrimPattern");
        generator.generateKeys(resource("banner_pattern.json"), "net.minestom.server.instance.block.banner", "BannerPattern");
        generator.generateKeys(resource("enchantment.json"), "net.minestom.server.item.enchant", "Enchantment");
        generator.generateKeys(resource("painting_variant.json"), "net.minestom.server.entity.metadata.other", "PaintingVariant");
        generator.generateKeys(resource("jukebox_song.json"), "net.minestom.server.instance.block.jukebox", "JukeboxSong");
        generator.generateKeys(resource("instrument.json"), "net.minestom.server.item.instrument", "Instrument");
        generator.generateKeys(resource("wolf_variant.json"), "net.minestom.server.entity.metadata.animal.tameable", "WolfVariant");
        generator.generateKeys(resource("wolf_sound_variant.json"), "net.minestom.server.entity.metadata.animal.tameable", "WolfSoundVariant");
        generator.generateKeys(resource("cat_variant.json"), "net.minestom.server.entity.metadata.animal.tameable", "CatVariant");
        generator.generateKeys(resource("chicken_variant.json"), "net.minestom.server.entity.metadata.animal", "ChickenVariant");
        generator.generateKeys(resource("cow_variant.json"), "net.minestom.server.entity.metadata.animal", "CowVariant");
        generator.generateKeys(resource("frog_variant.json"), "net.minestom.server.entity.metadata.animal", "FrogVariant");
        generator.generateKeys(resource("pig_variant.json"), "net.minestom.server.entity.metadata.animal", "PigVariant");
        generator.generateKeys(resource("worldgen/biome.json"), "net.minestom.server.world.biome", "Biome");

        LOGGER.info("Finished generating code");
    }

    public static String namespaceToConstant(String namespace) {
        String constant = namespace
                .replace("minecraft:", "")
                .replace(".", "_")
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
}
