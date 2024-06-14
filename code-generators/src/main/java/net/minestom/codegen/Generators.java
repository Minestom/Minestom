package net.minestom.codegen;

import net.minestom.codegen.color.DyeColorGenerator;
import net.minestom.codegen.fluid.FluidGenerator;
import net.minestom.codegen.particle.ParticleGenerator;
import net.minestom.codegen.recipe.RecipeTypeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

public class Generators {
    private static final Logger LOGGER = LoggerFactory.getLogger(Generators.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            LOGGER.error("Usage: <target folder>");
            return;
        }
        File outputFolder = new File(args[0]);

        // Special generators
        new DyeColorGenerator(resource("dye_colors.json"), outputFolder).generate();
        new RecipeTypeGenerator(resource("recipe_types.json"), outputFolder).generate();
        new ParticleGenerator(resource("particles.json"), outputFolder).generate();
        new ConstantsGenerator(resource("constants.json"), outputFolder).generate();

        var generator = new CodeGenerator(outputFolder);

        // Static registries
        generator.generate(resource("blocks.json"), "net.minestom.server.instance.block", "Block", "BlockImpl", "Blocks");
        generator.generate(resource("items.json"), "net.minestom.server.item", "Material", "MaterialImpl", "Materials");
        generator.generate(resource("entities.json"), "net.minestom.server.entity", "EntityType", "EntityTypeImpl", "EntityTypes");
        generator.generate(resource("potion_effects.json"), "net.minestom.server.potion", "PotionEffect", "PotionEffectImpl", "PotionEffects");
        generator.generate(resource("potions.json"), "net.minestom.server.potion", "PotionType", "PotionTypeImpl", "PotionTypes");
        generator.generate(resource("sounds.json"), "net.minestom.server.sound", "SoundEvent", "BuiltinSoundEvent", "SoundEvents");
        generator.generate(resource("custom_statistics.json"), "net.minestom.server.statistic", "StatisticType", "StatisticTypeImpl", "StatisticTypes");
        generator.generate(resource("attributes.json"), "net.minestom.server.entity.attribute", "Attribute", "AttributeImpl", "Attributes");

        // Dynamic registries
        generator.generateKeys(resource("chat_types.json"), "net.minestom.server.message", "ChatType", "ChatTypes");
        generator.generateKeys(resource("dimension_types.json"), "net.minestom.server.world", "DimensionType", "DimensionTypes");
        generator.generateKeys(resource("biomes.json"), "net.minestom.server.world.biome", "Biome", "Biomes");
        generator.generateKeys(resource("damage_types.json"), "net.minestom.server.entity.damage", "DamageType", "DamageTypes");
        generator.generateKeys(resource("trim_materials.json"), "net.minestom.server.item.armor", "TrimMaterial", "TrimMaterials");
        generator.generateKeys(resource("trim_patterns.json"), "net.minestom.server.item.armor", "TrimPattern", "TrimPatterns");
        generator.generateKeys(resource("banner_patterns.json"), "net.minestom.server.instance.block.banner", "BannerPattern", "BannerPatterns");
        generator.generateKeys(resource("wolf_variants.json"), "net.minestom.server.entity.metadata.animal.tameable", "WolfMeta.Variant", "WolfVariants");
        generator.generateKeys(resource("enchantments.json"), "net.minestom.server.item.enchant", "Enchantment", "Enchantments");
        generator.generateKeys(resource("painting_variants.json"), "net.minestom.server.entity.metadata.other", "PaintingMeta.Variant", "PaintingVariants");
        generator.generateKeys(resource("jukebox_songs.json"), "net.minestom.server.instance.block.jukebox", "JukeboxSong", "JukeboxSongs");

        // Generate fluids
        new FluidGenerator(resource("fluids.json"), outputFolder).generate();

        // TODO: Generate villager professions
//        new VillagerProfessionGenerator(
//                new File(inputFolder, targetVersion + "_villager_professions.json"),
//                outputFolder
//        ).generate();
        // TODO: Generate villager types
//        new VillagerTypeGenerator(
//                new File(inputFolder, targetVersion + "_villager_types.json"),
//                outputFolder
//        ).generate();
        LOGGER.info("Finished generating code");
    }

    private static InputStream resource(String name) {
        return Generators.class.getResourceAsStream("/" + name);
    }
}
