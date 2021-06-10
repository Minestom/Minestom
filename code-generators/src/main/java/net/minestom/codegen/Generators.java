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
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Generators {
    private static final Logger LOGGER = LoggerFactory.getLogger(Generators.class);

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 3) {
            LOGGER.error("Usage: <MC version> <source folder | 'resources'> <target folder>");
            return;
        }
        String targetVersion = args[0].replace(".", "_");
        boolean resourceMode = false;
        if (args[1].equals("resources")) {
            resourceMode = true;
        }
        File inputFolder = new File(args[1]); // This will be ignored if resourceMode = true
        File outputFolder = new File(args[2]);
        // Generate blocks
        new BlockGenerator(
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_blocks.json") : new FileInputStream(new File(inputFolder, targetVersion + "_blocks.json")),
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_block_properties.json") : new FileInputStream(new File(inputFolder, targetVersion + "_block_properties.json")),
                outputFolder
        ).generate();
        // Generate fluids
        new FluidGenerator(
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_fluids.json") : new FileInputStream(new File(inputFolder, targetVersion + "_fluids.json")),
                outputFolder
        ).generate();
        // Generate entities
        new EntityTypeGenerator(
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_entities.json") : new FileInputStream(new File(inputFolder, targetVersion + "_entities.json")),
                outputFolder
        ).generate();
        // Generate items
        new MaterialGenerator(
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_items.json") : new FileInputStream(new File(inputFolder, targetVersion + "_items.json")),
                outputFolder
        ).generate();
        // Generate enchantments
        new EnchantmentGenerator(
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_enchantments.json") : new FileInputStream(new File(inputFolder, targetVersion + "_enchantments.json")),
                outputFolder
        ).generate();
        // TODO: Generate attributes
//        new AttributeGenerator(
//                new File(inputFolder, targetVersion + "_attributes.json"),
//                outputFolder
//        ).generate();
        // Generate potion effects
        new PotionEffectGenerator(
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_potion_effects.json") : new FileInputStream(new File(inputFolder, targetVersion + "_potion_effects.json")),
                outputFolder
        ).generate();
        // Generate potions
        new PotionTypeGenerator(
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_potions.json") : new FileInputStream(new File(inputFolder, targetVersion + "_potions.json")),
                outputFolder
        ).generate();
        // Generate particles
        new ParticleGenerator(
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_particles.json") : new FileInputStream(new File(inputFolder, targetVersion + "_particles.json")),
                outputFolder
        ).generate();
        // Generate sounds
        new SoundEventGenerator(
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_sounds.json") : new FileInputStream(new File(inputFolder, targetVersion + "_sounds.json")),
                outputFolder
        ).generate();
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
        new StatisticGenerator(
                resourceMode ? Generators.class.getResourceAsStream("/" + targetVersion + "_custom_statistics.json") : new FileInputStream(new File(inputFolder, targetVersion + "_custom_statistics.json")),
                outputFolder
        ).generate();
        LOGGER.info("Finished generating code");
    }
}
