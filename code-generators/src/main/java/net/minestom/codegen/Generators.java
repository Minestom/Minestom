package net.minestom.codegen;

import net.minestom.codegen.blocks.BlockGenerator;
import net.minestom.codegen.entity.EntityTypeGenerator;
import net.minestom.codegen.fluid.FluidGenerator;
import net.minestom.codegen.item.EnchantmentGenerator;
import net.minestom.codegen.item.MaterialGenerator;
import net.minestom.codegen.particle.ParticleGenerator;
import net.minestom.codegen.potion.PotionEffectGenerator;
import net.minestom.codegen.potion.PotionTypeGenerator;
import net.minestom.codegen.sound.SoundEventGenerator;
import net.minestom.codegen.statistics.StatisticGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Generators {
    private static final Logger LOGGER = LoggerFactory.getLogger(Generators.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            LOGGER.error("Usage: <target folder>");
            return;
        }
        File outputFolder = new File(args[0]);
        // Generate blocks
        new BlockGenerator(resource("blocks.json"), outputFolder).generate();
        // Generate fluids
        new FluidGenerator(resource("fluids.json"), outputFolder).generate();
        // Generate entities
        new EntityTypeGenerator(resource("entities.json"), outputFolder).generate();
        // Generate items
        new MaterialGenerator(resource("items.json"), outputFolder).generate();
        // Generate enchantments
        new EnchantmentGenerator(resource("enchantments.json"), outputFolder).generate();
        // TODO: Generate attributes
//        new AttributeGenerator(
//                new File(inputFolder, targetVersion + "_attributes.json"),
//                outputFolder
//        ).generate();
        // Generate potion effects
        new PotionEffectGenerator(resource("potion_effects.json"), outputFolder).generate();
        // Generate potions
        new PotionTypeGenerator(resource("potions.json"), outputFolder).generate();
        // Generate particles
        new ParticleGenerator(resource("particles.json"), outputFolder).generate();
        // Generate sounds
        new SoundEventGenerator(resource("sounds.json"), outputFolder).generate();
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
        // Generate statistics
        new StatisticGenerator(resource("custom_statistics.json"), outputFolder).generate();
        LOGGER.info("Finished generating code");
    }

    private static InputStream resource(String name) {
        return Generators.class.getResourceAsStream("/" + name);
    }
}
