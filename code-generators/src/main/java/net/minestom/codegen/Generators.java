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

public class Generators {
    private static final Logger LOGGER = LoggerFactory.getLogger(Generators.class);

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
        // TODO: Generate attributes
//        new AttributeGenerator(
//                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_attributes.json"),
//                outputFolder
//        ).generate();
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
        // TODO: Generate villager professions
//        new VillagerProfessionGenerator(
//                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_villager_professions.json"),
//                outputFolder
//        ).generate();
        // TODO: Generate villager types
//        new VillagerTypeGenerator(
//                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_villager_types.json"),
//                outputFolder
//        ).generate();
        // Generate statistics
        new StatisticGenerator(
                new File(inputFolder, targetVersion.replaceAll("\\.", "_") + "_custom_statistics.json"),
                outputFolder
        ).generate();
        LOGGER.info("Finished generating code");
    }
}
