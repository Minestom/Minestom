package net.minestom.code_generation;

import net.minestom.code_generation.attribute.AttributeGenerator;
import net.minestom.code_generation.blocks.BlockGenerator;
import net.minestom.code_generation.entity.EntityTypeGenerator;
import net.minestom.code_generation.entity.VillagerProfessionGenerator;
import net.minestom.code_generation.entity.VillagerTypeGenerator;
import net.minestom.code_generation.fluid.FluidGenerator;
import net.minestom.code_generation.item.EnchantmentGenerator;
import net.minestom.code_generation.item.MaterialGenerator;
import net.minestom.code_generation.map.MapColorsGenerator;
import net.minestom.code_generation.particle.ParticleGenerator;
import net.minestom.code_generation.potion.PotionEffectGenerator;
import net.minestom.code_generation.potion.PotionTypeGenerator;
import net.minestom.code_generation.sound.SoundEventGenerator;
import net.minestom.code_generation.statistics.StatisticGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AllGenerators {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllGenerators.class);

    public static void main(String[] args) {
        if (args.length < 3) {
            LOGGER.error("Usage: <MC version> <source folder> <target folder>");
            return;
        }
        String targetVersion = args[0];
        File inputFolder = new File(args[1]);
        File outputFolder = new File(args[2]);
        // Generate blocks
        new BlockGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_blocks.json"),
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_block_properties.json"),
                outputFolder
        ).generate();
        // Generate fluids
        new FluidGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_fluids.json"),
                outputFolder
        ).generate();
        // Generate entities
        new EntityTypeGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_entities.json"),
                outputFolder
        ).generate();
        // Generate items
        new MaterialGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_items.json"),
                outputFolder
        ).generate();
        // Generate enchantments
        new EnchantmentGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_enchantments.json"),
                outputFolder
        ).generate();
        // Generate attributes
        new AttributeGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_attributes.json"),
                outputFolder
        ).generate();
        // Generate potion effects
        new PotionEffectGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_potion_effects.json"),
                outputFolder
        ).generate();
        // Generate potions
        new PotionTypeGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_potions.json"),
                outputFolder
        ).generate();
        // Generate particles
        new ParticleGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_particles.json"),
                outputFolder
        ).generate();
        // Generate sounds
        new SoundEventGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_sounds.json"),
                outputFolder
        ).generate();
        // Generate villager professions
        new VillagerProfessionGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_villager_professions.json"),
                outputFolder
        ).generate();
        // Generate villager types
        new VillagerTypeGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_villager_types.json"),
                outputFolder
        ).generate();
        // Generate statistics
        new StatisticGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_custom_statistics.json"),
                outputFolder
        ).generate();
        // Generate map colours
        new MapColorsGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_map_colors.json"),
                outputFolder
        ).generate();
        LOGGER.info("Finished generating code");
        LOGGER.info("Please make sure to run the task 'spotlessApply' before committing.");
    }
}
