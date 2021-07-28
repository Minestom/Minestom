package net.minestom.codegen;

import net.minestom.codegen.fluid.FluidGenerator;
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
        var generator = new CodeGenerator(outputFolder);
        generator.generate(resource("blocks.json"), "net.minestom.server.instance.block", "Block", "BlockLoader", "BlockConstants");
        generator.generate(resource("items.json"), "net.minestom.server.item", "Material", "MaterialLoader", "MaterialConstants");
        generator.generate(resource("entities.json"), "net.minestom.server.entity", "EntityType", "EntityTypeLoader", "EntityTypeConstants");
        generator.generate(resource("enchantments.json"), "net.minestom.server.item", "Enchantment", "EnchantmentLoader", "EnchantmentConstants");
        generator.generate(resource("potion_effects.json"), "net.minestom.server.potion", "PotionEffect", "PotionEffectLoader", "PotionEffectConstants");
        generator.generate(resource("potions.json"), "net.minestom.server.potion", "PotionType", "PotionTypeLoader", "PotionTypeConstants");
        generator.generate(resource("particles.json"), "net.minestom.server.particle", "Particle", "ParticleLoader", "ParticleConstants");
        generator.generate(resource("sounds.json"), "net.minestom.server.sound", "SoundEvent", "SoundEventLoader", "SoundEventConstants");
        generator.generate(resource("custom_statistics.json"), "net.minestom.server.statistic", "StatisticType", "StatisticTypeLoader", "StatisticTypeConstants");

        // Generate fluids
        new FluidGenerator(resource("fluids.json"), outputFolder).generate();
        // TODO: Generate attributes
//        new AttributeGenerator(
//                new File(inputFolder, targetVersion + "_attributes.json"),
//                outputFolder
//        ).generate();
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
