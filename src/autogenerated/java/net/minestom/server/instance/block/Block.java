package net.minestom.server.instance.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.instance.block.states.AcaciaButton;
import net.minestom.server.instance.block.states.AcaciaDoor;
import net.minestom.server.instance.block.states.AcaciaFence;
import net.minestom.server.instance.block.states.AcaciaFenceGate;
import net.minestom.server.instance.block.states.AcaciaLeaves;
import net.minestom.server.instance.block.states.AcaciaLog;
import net.minestom.server.instance.block.states.AcaciaPlanks;
import net.minestom.server.instance.block.states.AcaciaPressurePlate;
import net.minestom.server.instance.block.states.AcaciaSapling;
import net.minestom.server.instance.block.states.AcaciaSign;
import net.minestom.server.instance.block.states.AcaciaSlab;
import net.minestom.server.instance.block.states.AcaciaStairs;
import net.minestom.server.instance.block.states.AcaciaTrapdoor;
import net.minestom.server.instance.block.states.AcaciaWallSign;
import net.minestom.server.instance.block.states.AcaciaWood;
import net.minestom.server.instance.block.states.ActivatorRail;
import net.minestom.server.instance.block.states.Air;
import net.minestom.server.instance.block.states.Allium;
import net.minestom.server.instance.block.states.AncientDebris;
import net.minestom.server.instance.block.states.Andesite;
import net.minestom.server.instance.block.states.AndesiteSlab;
import net.minestom.server.instance.block.states.AndesiteStairs;
import net.minestom.server.instance.block.states.AndesiteWall;
import net.minestom.server.instance.block.states.Anvil;
import net.minestom.server.instance.block.states.AttachedMelonStem;
import net.minestom.server.instance.block.states.AttachedPumpkinStem;
import net.minestom.server.instance.block.states.AzureBluet;
import net.minestom.server.instance.block.states.Bamboo;
import net.minestom.server.instance.block.states.BambooSapling;
import net.minestom.server.instance.block.states.Barrel;
import net.minestom.server.instance.block.states.Barrier;
import net.minestom.server.instance.block.states.Basalt;
import net.minestom.server.instance.block.states.Beacon;
import net.minestom.server.instance.block.states.Bedrock;
import net.minestom.server.instance.block.states.BeeNest;
import net.minestom.server.instance.block.states.Beehive;
import net.minestom.server.instance.block.states.Beetroots;
import net.minestom.server.instance.block.states.Bell;
import net.minestom.server.instance.block.states.BirchButton;
import net.minestom.server.instance.block.states.BirchDoor;
import net.minestom.server.instance.block.states.BirchFence;
import net.minestom.server.instance.block.states.BirchFenceGate;
import net.minestom.server.instance.block.states.BirchLeaves;
import net.minestom.server.instance.block.states.BirchLog;
import net.minestom.server.instance.block.states.BirchPlanks;
import net.minestom.server.instance.block.states.BirchPressurePlate;
import net.minestom.server.instance.block.states.BirchSapling;
import net.minestom.server.instance.block.states.BirchSign;
import net.minestom.server.instance.block.states.BirchSlab;
import net.minestom.server.instance.block.states.BirchStairs;
import net.minestom.server.instance.block.states.BirchTrapdoor;
import net.minestom.server.instance.block.states.BirchWallSign;
import net.minestom.server.instance.block.states.BirchWood;
import net.minestom.server.instance.block.states.BlackBanner;
import net.minestom.server.instance.block.states.BlackBed;
import net.minestom.server.instance.block.states.BlackCarpet;
import net.minestom.server.instance.block.states.BlackConcrete;
import net.minestom.server.instance.block.states.BlackConcretePowder;
import net.minestom.server.instance.block.states.BlackGlazedTerracotta;
import net.minestom.server.instance.block.states.BlackShulkerBox;
import net.minestom.server.instance.block.states.BlackStainedGlass;
import net.minestom.server.instance.block.states.BlackStainedGlassPane;
import net.minestom.server.instance.block.states.BlackTerracotta;
import net.minestom.server.instance.block.states.BlackWallBanner;
import net.minestom.server.instance.block.states.BlackWool;
import net.minestom.server.instance.block.states.Blackstone;
import net.minestom.server.instance.block.states.BlackstoneSlab;
import net.minestom.server.instance.block.states.BlackstoneStairs;
import net.minestom.server.instance.block.states.BlackstoneWall;
import net.minestom.server.instance.block.states.BlastFurnace;
import net.minestom.server.instance.block.states.BlueBanner;
import net.minestom.server.instance.block.states.BlueBed;
import net.minestom.server.instance.block.states.BlueCarpet;
import net.minestom.server.instance.block.states.BlueConcrete;
import net.minestom.server.instance.block.states.BlueConcretePowder;
import net.minestom.server.instance.block.states.BlueGlazedTerracotta;
import net.minestom.server.instance.block.states.BlueIce;
import net.minestom.server.instance.block.states.BlueOrchid;
import net.minestom.server.instance.block.states.BlueShulkerBox;
import net.minestom.server.instance.block.states.BlueStainedGlass;
import net.minestom.server.instance.block.states.BlueStainedGlassPane;
import net.minestom.server.instance.block.states.BlueTerracotta;
import net.minestom.server.instance.block.states.BlueWallBanner;
import net.minestom.server.instance.block.states.BlueWool;
import net.minestom.server.instance.block.states.BoneBlock;
import net.minestom.server.instance.block.states.Bookshelf;
import net.minestom.server.instance.block.states.BrainCoral;
import net.minestom.server.instance.block.states.BrainCoralBlock;
import net.minestom.server.instance.block.states.BrainCoralFan;
import net.minestom.server.instance.block.states.BrainCoralWallFan;
import net.minestom.server.instance.block.states.BrewingStand;
import net.minestom.server.instance.block.states.BrickSlab;
import net.minestom.server.instance.block.states.BrickStairs;
import net.minestom.server.instance.block.states.BrickWall;
import net.minestom.server.instance.block.states.Bricks;
import net.minestom.server.instance.block.states.BrownBanner;
import net.minestom.server.instance.block.states.BrownBed;
import net.minestom.server.instance.block.states.BrownCarpet;
import net.minestom.server.instance.block.states.BrownConcrete;
import net.minestom.server.instance.block.states.BrownConcretePowder;
import net.minestom.server.instance.block.states.BrownGlazedTerracotta;
import net.minestom.server.instance.block.states.BrownMushroom;
import net.minestom.server.instance.block.states.BrownMushroomBlock;
import net.minestom.server.instance.block.states.BrownShulkerBox;
import net.minestom.server.instance.block.states.BrownStainedGlass;
import net.minestom.server.instance.block.states.BrownStainedGlassPane;
import net.minestom.server.instance.block.states.BrownTerracotta;
import net.minestom.server.instance.block.states.BrownWallBanner;
import net.minestom.server.instance.block.states.BrownWool;
import net.minestom.server.instance.block.states.BubbleColumn;
import net.minestom.server.instance.block.states.BubbleCoral;
import net.minestom.server.instance.block.states.BubbleCoralBlock;
import net.minestom.server.instance.block.states.BubbleCoralFan;
import net.minestom.server.instance.block.states.BubbleCoralWallFan;
import net.minestom.server.instance.block.states.Cactus;
import net.minestom.server.instance.block.states.Cake;
import net.minestom.server.instance.block.states.Campfire;
import net.minestom.server.instance.block.states.Carrots;
import net.minestom.server.instance.block.states.CartographyTable;
import net.minestom.server.instance.block.states.CarvedPumpkin;
import net.minestom.server.instance.block.states.Cauldron;
import net.minestom.server.instance.block.states.CaveAir;
import net.minestom.server.instance.block.states.Chain;
import net.minestom.server.instance.block.states.ChainCommandBlock;
import net.minestom.server.instance.block.states.Chest;
import net.minestom.server.instance.block.states.ChippedAnvil;
import net.minestom.server.instance.block.states.ChiseledNetherBricks;
import net.minestom.server.instance.block.states.ChiseledPolishedBlackstone;
import net.minestom.server.instance.block.states.ChiseledQuartzBlock;
import net.minestom.server.instance.block.states.ChiseledRedSandstone;
import net.minestom.server.instance.block.states.ChiseledSandstone;
import net.minestom.server.instance.block.states.ChiseledStoneBricks;
import net.minestom.server.instance.block.states.ChorusFlower;
import net.minestom.server.instance.block.states.ChorusPlant;
import net.minestom.server.instance.block.states.Clay;
import net.minestom.server.instance.block.states.CoalBlock;
import net.minestom.server.instance.block.states.CoalOre;
import net.minestom.server.instance.block.states.CoarseDirt;
import net.minestom.server.instance.block.states.Cobblestone;
import net.minestom.server.instance.block.states.CobblestoneSlab;
import net.minestom.server.instance.block.states.CobblestoneStairs;
import net.minestom.server.instance.block.states.CobblestoneWall;
import net.minestom.server.instance.block.states.Cobweb;
import net.minestom.server.instance.block.states.Cocoa;
import net.minestom.server.instance.block.states.CommandBlock;
import net.minestom.server.instance.block.states.Comparator;
import net.minestom.server.instance.block.states.Composter;
import net.minestom.server.instance.block.states.Conduit;
import net.minestom.server.instance.block.states.Cornflower;
import net.minestom.server.instance.block.states.CrackedNetherBricks;
import net.minestom.server.instance.block.states.CrackedPolishedBlackstoneBricks;
import net.minestom.server.instance.block.states.CrackedStoneBricks;
import net.minestom.server.instance.block.states.CraftingTable;
import net.minestom.server.instance.block.states.CreeperHead;
import net.minestom.server.instance.block.states.CreeperWallHead;
import net.minestom.server.instance.block.states.CrimsonButton;
import net.minestom.server.instance.block.states.CrimsonDoor;
import net.minestom.server.instance.block.states.CrimsonFence;
import net.minestom.server.instance.block.states.CrimsonFenceGate;
import net.minestom.server.instance.block.states.CrimsonFungus;
import net.minestom.server.instance.block.states.CrimsonHyphae;
import net.minestom.server.instance.block.states.CrimsonNylium;
import net.minestom.server.instance.block.states.CrimsonPlanks;
import net.minestom.server.instance.block.states.CrimsonPressurePlate;
import net.minestom.server.instance.block.states.CrimsonRoots;
import net.minestom.server.instance.block.states.CrimsonSign;
import net.minestom.server.instance.block.states.CrimsonSlab;
import net.minestom.server.instance.block.states.CrimsonStairs;
import net.minestom.server.instance.block.states.CrimsonStem;
import net.minestom.server.instance.block.states.CrimsonTrapdoor;
import net.minestom.server.instance.block.states.CrimsonWallSign;
import net.minestom.server.instance.block.states.CryingObsidian;
import net.minestom.server.instance.block.states.CutRedSandstone;
import net.minestom.server.instance.block.states.CutRedSandstoneSlab;
import net.minestom.server.instance.block.states.CutSandstone;
import net.minestom.server.instance.block.states.CutSandstoneSlab;
import net.minestom.server.instance.block.states.CyanBanner;
import net.minestom.server.instance.block.states.CyanBed;
import net.minestom.server.instance.block.states.CyanCarpet;
import net.minestom.server.instance.block.states.CyanConcrete;
import net.minestom.server.instance.block.states.CyanConcretePowder;
import net.minestom.server.instance.block.states.CyanGlazedTerracotta;
import net.minestom.server.instance.block.states.CyanShulkerBox;
import net.minestom.server.instance.block.states.CyanStainedGlass;
import net.minestom.server.instance.block.states.CyanStainedGlassPane;
import net.minestom.server.instance.block.states.CyanTerracotta;
import net.minestom.server.instance.block.states.CyanWallBanner;
import net.minestom.server.instance.block.states.CyanWool;
import net.minestom.server.instance.block.states.DamagedAnvil;
import net.minestom.server.instance.block.states.Dandelion;
import net.minestom.server.instance.block.states.DarkOakButton;
import net.minestom.server.instance.block.states.DarkOakDoor;
import net.minestom.server.instance.block.states.DarkOakFence;
import net.minestom.server.instance.block.states.DarkOakFenceGate;
import net.minestom.server.instance.block.states.DarkOakLeaves;
import net.minestom.server.instance.block.states.DarkOakLog;
import net.minestom.server.instance.block.states.DarkOakPlanks;
import net.minestom.server.instance.block.states.DarkOakPressurePlate;
import net.minestom.server.instance.block.states.DarkOakSapling;
import net.minestom.server.instance.block.states.DarkOakSign;
import net.minestom.server.instance.block.states.DarkOakSlab;
import net.minestom.server.instance.block.states.DarkOakStairs;
import net.minestom.server.instance.block.states.DarkOakTrapdoor;
import net.minestom.server.instance.block.states.DarkOakWallSign;
import net.minestom.server.instance.block.states.DarkOakWood;
import net.minestom.server.instance.block.states.DarkPrismarine;
import net.minestom.server.instance.block.states.DarkPrismarineSlab;
import net.minestom.server.instance.block.states.DarkPrismarineStairs;
import net.minestom.server.instance.block.states.DaylightDetector;
import net.minestom.server.instance.block.states.DeadBrainCoral;
import net.minestom.server.instance.block.states.DeadBrainCoralBlock;
import net.minestom.server.instance.block.states.DeadBrainCoralFan;
import net.minestom.server.instance.block.states.DeadBrainCoralWallFan;
import net.minestom.server.instance.block.states.DeadBubbleCoral;
import net.minestom.server.instance.block.states.DeadBubbleCoralBlock;
import net.minestom.server.instance.block.states.DeadBubbleCoralFan;
import net.minestom.server.instance.block.states.DeadBubbleCoralWallFan;
import net.minestom.server.instance.block.states.DeadBush;
import net.minestom.server.instance.block.states.DeadFireCoral;
import net.minestom.server.instance.block.states.DeadFireCoralBlock;
import net.minestom.server.instance.block.states.DeadFireCoralFan;
import net.minestom.server.instance.block.states.DeadFireCoralWallFan;
import net.minestom.server.instance.block.states.DeadHornCoral;
import net.minestom.server.instance.block.states.DeadHornCoralBlock;
import net.minestom.server.instance.block.states.DeadHornCoralFan;
import net.minestom.server.instance.block.states.DeadHornCoralWallFan;
import net.minestom.server.instance.block.states.DeadTubeCoral;
import net.minestom.server.instance.block.states.DeadTubeCoralBlock;
import net.minestom.server.instance.block.states.DeadTubeCoralFan;
import net.minestom.server.instance.block.states.DeadTubeCoralWallFan;
import net.minestom.server.instance.block.states.DetectorRail;
import net.minestom.server.instance.block.states.DiamondBlock;
import net.minestom.server.instance.block.states.DiamondOre;
import net.minestom.server.instance.block.states.Diorite;
import net.minestom.server.instance.block.states.DioriteSlab;
import net.minestom.server.instance.block.states.DioriteStairs;
import net.minestom.server.instance.block.states.DioriteWall;
import net.minestom.server.instance.block.states.Dirt;
import net.minestom.server.instance.block.states.Dispenser;
import net.minestom.server.instance.block.states.DragonEgg;
import net.minestom.server.instance.block.states.DragonHead;
import net.minestom.server.instance.block.states.DragonWallHead;
import net.minestom.server.instance.block.states.DriedKelpBlock;
import net.minestom.server.instance.block.states.Dropper;
import net.minestom.server.instance.block.states.EmeraldBlock;
import net.minestom.server.instance.block.states.EmeraldOre;
import net.minestom.server.instance.block.states.EnchantingTable;
import net.minestom.server.instance.block.states.EndGateway;
import net.minestom.server.instance.block.states.EndPortal;
import net.minestom.server.instance.block.states.EndPortalFrame;
import net.minestom.server.instance.block.states.EndRod;
import net.minestom.server.instance.block.states.EndStone;
import net.minestom.server.instance.block.states.EndStoneBrickSlab;
import net.minestom.server.instance.block.states.EndStoneBrickStairs;
import net.minestom.server.instance.block.states.EndStoneBrickWall;
import net.minestom.server.instance.block.states.EndStoneBricks;
import net.minestom.server.instance.block.states.EnderChest;
import net.minestom.server.instance.block.states.Farmland;
import net.minestom.server.instance.block.states.Fern;
import net.minestom.server.instance.block.states.Fire;
import net.minestom.server.instance.block.states.FireCoral;
import net.minestom.server.instance.block.states.FireCoralBlock;
import net.minestom.server.instance.block.states.FireCoralFan;
import net.minestom.server.instance.block.states.FireCoralWallFan;
import net.minestom.server.instance.block.states.FletchingTable;
import net.minestom.server.instance.block.states.FlowerPot;
import net.minestom.server.instance.block.states.FrostedIce;
import net.minestom.server.instance.block.states.Furnace;
import net.minestom.server.instance.block.states.GildedBlackstone;
import net.minestom.server.instance.block.states.Glass;
import net.minestom.server.instance.block.states.GlassPane;
import net.minestom.server.instance.block.states.Glowstone;
import net.minestom.server.instance.block.states.GoldBlock;
import net.minestom.server.instance.block.states.GoldOre;
import net.minestom.server.instance.block.states.Granite;
import net.minestom.server.instance.block.states.GraniteSlab;
import net.minestom.server.instance.block.states.GraniteStairs;
import net.minestom.server.instance.block.states.GraniteWall;
import net.minestom.server.instance.block.states.Grass;
import net.minestom.server.instance.block.states.GrassBlock;
import net.minestom.server.instance.block.states.GrassPath;
import net.minestom.server.instance.block.states.Gravel;
import net.minestom.server.instance.block.states.GrayBanner;
import net.minestom.server.instance.block.states.GrayBed;
import net.minestom.server.instance.block.states.GrayCarpet;
import net.minestom.server.instance.block.states.GrayConcrete;
import net.minestom.server.instance.block.states.GrayConcretePowder;
import net.minestom.server.instance.block.states.GrayGlazedTerracotta;
import net.minestom.server.instance.block.states.GrayShulkerBox;
import net.minestom.server.instance.block.states.GrayStainedGlass;
import net.minestom.server.instance.block.states.GrayStainedGlassPane;
import net.minestom.server.instance.block.states.GrayTerracotta;
import net.minestom.server.instance.block.states.GrayWallBanner;
import net.minestom.server.instance.block.states.GrayWool;
import net.minestom.server.instance.block.states.GreenBanner;
import net.minestom.server.instance.block.states.GreenBed;
import net.minestom.server.instance.block.states.GreenCarpet;
import net.minestom.server.instance.block.states.GreenConcrete;
import net.minestom.server.instance.block.states.GreenConcretePowder;
import net.minestom.server.instance.block.states.GreenGlazedTerracotta;
import net.minestom.server.instance.block.states.GreenShulkerBox;
import net.minestom.server.instance.block.states.GreenStainedGlass;
import net.minestom.server.instance.block.states.GreenStainedGlassPane;
import net.minestom.server.instance.block.states.GreenTerracotta;
import net.minestom.server.instance.block.states.GreenWallBanner;
import net.minestom.server.instance.block.states.GreenWool;
import net.minestom.server.instance.block.states.Grindstone;
import net.minestom.server.instance.block.states.HayBlock;
import net.minestom.server.instance.block.states.HeavyWeightedPressurePlate;
import net.minestom.server.instance.block.states.HoneyBlock;
import net.minestom.server.instance.block.states.HoneycombBlock;
import net.minestom.server.instance.block.states.Hopper;
import net.minestom.server.instance.block.states.HornCoral;
import net.minestom.server.instance.block.states.HornCoralBlock;
import net.minestom.server.instance.block.states.HornCoralFan;
import net.minestom.server.instance.block.states.HornCoralWallFan;
import net.minestom.server.instance.block.states.Ice;
import net.minestom.server.instance.block.states.InfestedChiseledStoneBricks;
import net.minestom.server.instance.block.states.InfestedCobblestone;
import net.minestom.server.instance.block.states.InfestedCrackedStoneBricks;
import net.minestom.server.instance.block.states.InfestedMossyStoneBricks;
import net.minestom.server.instance.block.states.InfestedStone;
import net.minestom.server.instance.block.states.InfestedStoneBricks;
import net.minestom.server.instance.block.states.IronBars;
import net.minestom.server.instance.block.states.IronBlock;
import net.minestom.server.instance.block.states.IronDoor;
import net.minestom.server.instance.block.states.IronOre;
import net.minestom.server.instance.block.states.IronTrapdoor;
import net.minestom.server.instance.block.states.JackOLantern;
import net.minestom.server.instance.block.states.Jigsaw;
import net.minestom.server.instance.block.states.Jukebox;
import net.minestom.server.instance.block.states.JungleButton;
import net.minestom.server.instance.block.states.JungleDoor;
import net.minestom.server.instance.block.states.JungleFence;
import net.minestom.server.instance.block.states.JungleFenceGate;
import net.minestom.server.instance.block.states.JungleLeaves;
import net.minestom.server.instance.block.states.JungleLog;
import net.minestom.server.instance.block.states.JunglePlanks;
import net.minestom.server.instance.block.states.JunglePressurePlate;
import net.minestom.server.instance.block.states.JungleSapling;
import net.minestom.server.instance.block.states.JungleSign;
import net.minestom.server.instance.block.states.JungleSlab;
import net.minestom.server.instance.block.states.JungleStairs;
import net.minestom.server.instance.block.states.JungleTrapdoor;
import net.minestom.server.instance.block.states.JungleWallSign;
import net.minestom.server.instance.block.states.JungleWood;
import net.minestom.server.instance.block.states.Kelp;
import net.minestom.server.instance.block.states.KelpPlant;
import net.minestom.server.instance.block.states.Ladder;
import net.minestom.server.instance.block.states.Lantern;
import net.minestom.server.instance.block.states.LapisBlock;
import net.minestom.server.instance.block.states.LapisOre;
import net.minestom.server.instance.block.states.LargeFern;
import net.minestom.server.instance.block.states.Lava;
import net.minestom.server.instance.block.states.Lectern;
import net.minestom.server.instance.block.states.Lever;
import net.minestom.server.instance.block.states.LightBlueBanner;
import net.minestom.server.instance.block.states.LightBlueBed;
import net.minestom.server.instance.block.states.LightBlueCarpet;
import net.minestom.server.instance.block.states.LightBlueConcrete;
import net.minestom.server.instance.block.states.LightBlueConcretePowder;
import net.minestom.server.instance.block.states.LightBlueGlazedTerracotta;
import net.minestom.server.instance.block.states.LightBlueShulkerBox;
import net.minestom.server.instance.block.states.LightBlueStainedGlass;
import net.minestom.server.instance.block.states.LightBlueStainedGlassPane;
import net.minestom.server.instance.block.states.LightBlueTerracotta;
import net.minestom.server.instance.block.states.LightBlueWallBanner;
import net.minestom.server.instance.block.states.LightBlueWool;
import net.minestom.server.instance.block.states.LightGrayBanner;
import net.minestom.server.instance.block.states.LightGrayBed;
import net.minestom.server.instance.block.states.LightGrayCarpet;
import net.minestom.server.instance.block.states.LightGrayConcrete;
import net.minestom.server.instance.block.states.LightGrayConcretePowder;
import net.minestom.server.instance.block.states.LightGrayGlazedTerracotta;
import net.minestom.server.instance.block.states.LightGrayShulkerBox;
import net.minestom.server.instance.block.states.LightGrayStainedGlass;
import net.minestom.server.instance.block.states.LightGrayStainedGlassPane;
import net.minestom.server.instance.block.states.LightGrayTerracotta;
import net.minestom.server.instance.block.states.LightGrayWallBanner;
import net.minestom.server.instance.block.states.LightGrayWool;
import net.minestom.server.instance.block.states.LightWeightedPressurePlate;
import net.minestom.server.instance.block.states.Lilac;
import net.minestom.server.instance.block.states.LilyOfTheValley;
import net.minestom.server.instance.block.states.LilyPad;
import net.minestom.server.instance.block.states.LimeBanner;
import net.minestom.server.instance.block.states.LimeBed;
import net.minestom.server.instance.block.states.LimeCarpet;
import net.minestom.server.instance.block.states.LimeConcrete;
import net.minestom.server.instance.block.states.LimeConcretePowder;
import net.minestom.server.instance.block.states.LimeGlazedTerracotta;
import net.minestom.server.instance.block.states.LimeShulkerBox;
import net.minestom.server.instance.block.states.LimeStainedGlass;
import net.minestom.server.instance.block.states.LimeStainedGlassPane;
import net.minestom.server.instance.block.states.LimeTerracotta;
import net.minestom.server.instance.block.states.LimeWallBanner;
import net.minestom.server.instance.block.states.LimeWool;
import net.minestom.server.instance.block.states.Lodestone;
import net.minestom.server.instance.block.states.Loom;
import net.minestom.server.instance.block.states.MagentaBanner;
import net.minestom.server.instance.block.states.MagentaBed;
import net.minestom.server.instance.block.states.MagentaCarpet;
import net.minestom.server.instance.block.states.MagentaConcrete;
import net.minestom.server.instance.block.states.MagentaConcretePowder;
import net.minestom.server.instance.block.states.MagentaGlazedTerracotta;
import net.minestom.server.instance.block.states.MagentaShulkerBox;
import net.minestom.server.instance.block.states.MagentaStainedGlass;
import net.minestom.server.instance.block.states.MagentaStainedGlassPane;
import net.minestom.server.instance.block.states.MagentaTerracotta;
import net.minestom.server.instance.block.states.MagentaWallBanner;
import net.minestom.server.instance.block.states.MagentaWool;
import net.minestom.server.instance.block.states.MagmaBlock;
import net.minestom.server.instance.block.states.Melon;
import net.minestom.server.instance.block.states.MelonStem;
import net.minestom.server.instance.block.states.MossyCobblestone;
import net.minestom.server.instance.block.states.MossyCobblestoneSlab;
import net.minestom.server.instance.block.states.MossyCobblestoneStairs;
import net.minestom.server.instance.block.states.MossyCobblestoneWall;
import net.minestom.server.instance.block.states.MossyStoneBrickSlab;
import net.minestom.server.instance.block.states.MossyStoneBrickStairs;
import net.minestom.server.instance.block.states.MossyStoneBrickWall;
import net.minestom.server.instance.block.states.MossyStoneBricks;
import net.minestom.server.instance.block.states.MovingPiston;
import net.minestom.server.instance.block.states.MushroomStem;
import net.minestom.server.instance.block.states.Mycelium;
import net.minestom.server.instance.block.states.NetherBrickFence;
import net.minestom.server.instance.block.states.NetherBrickSlab;
import net.minestom.server.instance.block.states.NetherBrickStairs;
import net.minestom.server.instance.block.states.NetherBrickWall;
import net.minestom.server.instance.block.states.NetherBricks;
import net.minestom.server.instance.block.states.NetherGoldOre;
import net.minestom.server.instance.block.states.NetherPortal;
import net.minestom.server.instance.block.states.NetherQuartzOre;
import net.minestom.server.instance.block.states.NetherSprouts;
import net.minestom.server.instance.block.states.NetherWart;
import net.minestom.server.instance.block.states.NetherWartBlock;
import net.minestom.server.instance.block.states.NetheriteBlock;
import net.minestom.server.instance.block.states.Netherrack;
import net.minestom.server.instance.block.states.NoteBlock;
import net.minestom.server.instance.block.states.NoteBlock2;
import net.minestom.server.instance.block.states.OakButton;
import net.minestom.server.instance.block.states.OakDoor;
import net.minestom.server.instance.block.states.OakFence;
import net.minestom.server.instance.block.states.OakFenceGate;
import net.minestom.server.instance.block.states.OakLeaves;
import net.minestom.server.instance.block.states.OakLog;
import net.minestom.server.instance.block.states.OakPlanks;
import net.minestom.server.instance.block.states.OakPressurePlate;
import net.minestom.server.instance.block.states.OakSapling;
import net.minestom.server.instance.block.states.OakSign;
import net.minestom.server.instance.block.states.OakSlab;
import net.minestom.server.instance.block.states.OakStairs;
import net.minestom.server.instance.block.states.OakTrapdoor;
import net.minestom.server.instance.block.states.OakWallSign;
import net.minestom.server.instance.block.states.OakWood;
import net.minestom.server.instance.block.states.Observer;
import net.minestom.server.instance.block.states.Obsidian;
import net.minestom.server.instance.block.states.OrangeBanner;
import net.minestom.server.instance.block.states.OrangeBed;
import net.minestom.server.instance.block.states.OrangeCarpet;
import net.minestom.server.instance.block.states.OrangeConcrete;
import net.minestom.server.instance.block.states.OrangeConcretePowder;
import net.minestom.server.instance.block.states.OrangeGlazedTerracotta;
import net.minestom.server.instance.block.states.OrangeShulkerBox;
import net.minestom.server.instance.block.states.OrangeStainedGlass;
import net.minestom.server.instance.block.states.OrangeStainedGlassPane;
import net.minestom.server.instance.block.states.OrangeTerracotta;
import net.minestom.server.instance.block.states.OrangeTulip;
import net.minestom.server.instance.block.states.OrangeWallBanner;
import net.minestom.server.instance.block.states.OrangeWool;
import net.minestom.server.instance.block.states.OxeyeDaisy;
import net.minestom.server.instance.block.states.PackedIce;
import net.minestom.server.instance.block.states.Peony;
import net.minestom.server.instance.block.states.PetrifiedOakSlab;
import net.minestom.server.instance.block.states.PinkBanner;
import net.minestom.server.instance.block.states.PinkBed;
import net.minestom.server.instance.block.states.PinkCarpet;
import net.minestom.server.instance.block.states.PinkConcrete;
import net.minestom.server.instance.block.states.PinkConcretePowder;
import net.minestom.server.instance.block.states.PinkGlazedTerracotta;
import net.minestom.server.instance.block.states.PinkShulkerBox;
import net.minestom.server.instance.block.states.PinkStainedGlass;
import net.minestom.server.instance.block.states.PinkStainedGlassPane;
import net.minestom.server.instance.block.states.PinkTerracotta;
import net.minestom.server.instance.block.states.PinkTulip;
import net.minestom.server.instance.block.states.PinkWallBanner;
import net.minestom.server.instance.block.states.PinkWool;
import net.minestom.server.instance.block.states.Piston;
import net.minestom.server.instance.block.states.PistonHead;
import net.minestom.server.instance.block.states.PlayerHead;
import net.minestom.server.instance.block.states.PlayerWallHead;
import net.minestom.server.instance.block.states.Podzol;
import net.minestom.server.instance.block.states.PolishedAndesite;
import net.minestom.server.instance.block.states.PolishedAndesiteSlab;
import net.minestom.server.instance.block.states.PolishedAndesiteStairs;
import net.minestom.server.instance.block.states.PolishedBasalt;
import net.minestom.server.instance.block.states.PolishedBlackstone;
import net.minestom.server.instance.block.states.PolishedBlackstoneBrickSlab;
import net.minestom.server.instance.block.states.PolishedBlackstoneBrickStairs;
import net.minestom.server.instance.block.states.PolishedBlackstoneBrickWall;
import net.minestom.server.instance.block.states.PolishedBlackstoneBricks;
import net.minestom.server.instance.block.states.PolishedBlackstoneButton;
import net.minestom.server.instance.block.states.PolishedBlackstonePressurePlate;
import net.minestom.server.instance.block.states.PolishedBlackstoneSlab;
import net.minestom.server.instance.block.states.PolishedBlackstoneStairs;
import net.minestom.server.instance.block.states.PolishedBlackstoneWall;
import net.minestom.server.instance.block.states.PolishedDiorite;
import net.minestom.server.instance.block.states.PolishedDioriteSlab;
import net.minestom.server.instance.block.states.PolishedDioriteStairs;
import net.minestom.server.instance.block.states.PolishedGranite;
import net.minestom.server.instance.block.states.PolishedGraniteSlab;
import net.minestom.server.instance.block.states.PolishedGraniteStairs;
import net.minestom.server.instance.block.states.Poppy;
import net.minestom.server.instance.block.states.Potatoes;
import net.minestom.server.instance.block.states.PottedAcaciaSapling;
import net.minestom.server.instance.block.states.PottedAllium;
import net.minestom.server.instance.block.states.PottedAzureBluet;
import net.minestom.server.instance.block.states.PottedBamboo;
import net.minestom.server.instance.block.states.PottedBirchSapling;
import net.minestom.server.instance.block.states.PottedBlueOrchid;
import net.minestom.server.instance.block.states.PottedBrownMushroom;
import net.minestom.server.instance.block.states.PottedCactus;
import net.minestom.server.instance.block.states.PottedCornflower;
import net.minestom.server.instance.block.states.PottedCrimsonFungus;
import net.minestom.server.instance.block.states.PottedCrimsonRoots;
import net.minestom.server.instance.block.states.PottedDandelion;
import net.minestom.server.instance.block.states.PottedDarkOakSapling;
import net.minestom.server.instance.block.states.PottedDeadBush;
import net.minestom.server.instance.block.states.PottedFern;
import net.minestom.server.instance.block.states.PottedJungleSapling;
import net.minestom.server.instance.block.states.PottedLilyOfTheValley;
import net.minestom.server.instance.block.states.PottedOakSapling;
import net.minestom.server.instance.block.states.PottedOrangeTulip;
import net.minestom.server.instance.block.states.PottedOxeyeDaisy;
import net.minestom.server.instance.block.states.PottedPinkTulip;
import net.minestom.server.instance.block.states.PottedPoppy;
import net.minestom.server.instance.block.states.PottedRedMushroom;
import net.minestom.server.instance.block.states.PottedRedTulip;
import net.minestom.server.instance.block.states.PottedSpruceSapling;
import net.minestom.server.instance.block.states.PottedWarpedFungus;
import net.minestom.server.instance.block.states.PottedWarpedRoots;
import net.minestom.server.instance.block.states.PottedWhiteTulip;
import net.minestom.server.instance.block.states.PottedWitherRose;
import net.minestom.server.instance.block.states.PoweredRail;
import net.minestom.server.instance.block.states.Prismarine;
import net.minestom.server.instance.block.states.PrismarineBrickSlab;
import net.minestom.server.instance.block.states.PrismarineBrickStairs;
import net.minestom.server.instance.block.states.PrismarineBricks;
import net.minestom.server.instance.block.states.PrismarineSlab;
import net.minestom.server.instance.block.states.PrismarineStairs;
import net.minestom.server.instance.block.states.PrismarineWall;
import net.minestom.server.instance.block.states.Pumpkin;
import net.minestom.server.instance.block.states.PumpkinStem;
import net.minestom.server.instance.block.states.PurpleBanner;
import net.minestom.server.instance.block.states.PurpleBed;
import net.minestom.server.instance.block.states.PurpleCarpet;
import net.minestom.server.instance.block.states.PurpleConcrete;
import net.minestom.server.instance.block.states.PurpleConcretePowder;
import net.minestom.server.instance.block.states.PurpleGlazedTerracotta;
import net.minestom.server.instance.block.states.PurpleShulkerBox;
import net.minestom.server.instance.block.states.PurpleStainedGlass;
import net.minestom.server.instance.block.states.PurpleStainedGlassPane;
import net.minestom.server.instance.block.states.PurpleTerracotta;
import net.minestom.server.instance.block.states.PurpleWallBanner;
import net.minestom.server.instance.block.states.PurpleWool;
import net.minestom.server.instance.block.states.PurpurBlock;
import net.minestom.server.instance.block.states.PurpurPillar;
import net.minestom.server.instance.block.states.PurpurSlab;
import net.minestom.server.instance.block.states.PurpurStairs;
import net.minestom.server.instance.block.states.QuartzBlock;
import net.minestom.server.instance.block.states.QuartzBricks;
import net.minestom.server.instance.block.states.QuartzPillar;
import net.minestom.server.instance.block.states.QuartzSlab;
import net.minestom.server.instance.block.states.QuartzStairs;
import net.minestom.server.instance.block.states.Rail;
import net.minestom.server.instance.block.states.RedBanner;
import net.minestom.server.instance.block.states.RedBed;
import net.minestom.server.instance.block.states.RedCarpet;
import net.minestom.server.instance.block.states.RedConcrete;
import net.minestom.server.instance.block.states.RedConcretePowder;
import net.minestom.server.instance.block.states.RedGlazedTerracotta;
import net.minestom.server.instance.block.states.RedMushroom;
import net.minestom.server.instance.block.states.RedMushroomBlock;
import net.minestom.server.instance.block.states.RedNetherBrickSlab;
import net.minestom.server.instance.block.states.RedNetherBrickStairs;
import net.minestom.server.instance.block.states.RedNetherBrickWall;
import net.minestom.server.instance.block.states.RedNetherBricks;
import net.minestom.server.instance.block.states.RedSand;
import net.minestom.server.instance.block.states.RedSandstone;
import net.minestom.server.instance.block.states.RedSandstoneSlab;
import net.minestom.server.instance.block.states.RedSandstoneStairs;
import net.minestom.server.instance.block.states.RedSandstoneWall;
import net.minestom.server.instance.block.states.RedShulkerBox;
import net.minestom.server.instance.block.states.RedStainedGlass;
import net.minestom.server.instance.block.states.RedStainedGlassPane;
import net.minestom.server.instance.block.states.RedTerracotta;
import net.minestom.server.instance.block.states.RedTulip;
import net.minestom.server.instance.block.states.RedWallBanner;
import net.minestom.server.instance.block.states.RedWool;
import net.minestom.server.instance.block.states.RedstoneBlock;
import net.minestom.server.instance.block.states.RedstoneLamp;
import net.minestom.server.instance.block.states.RedstoneOre;
import net.minestom.server.instance.block.states.RedstoneTorch;
import net.minestom.server.instance.block.states.RedstoneWallTorch;
import net.minestom.server.instance.block.states.RedstoneWire;
import net.minestom.server.instance.block.states.RedstoneWire2;
import net.minestom.server.instance.block.states.RedstoneWire3;
import net.minestom.server.instance.block.states.Repeater;
import net.minestom.server.instance.block.states.RepeatingCommandBlock;
import net.minestom.server.instance.block.states.RespawnAnchor;
import net.minestom.server.instance.block.states.RoseBush;
import net.minestom.server.instance.block.states.Sand;
import net.minestom.server.instance.block.states.Sandstone;
import net.minestom.server.instance.block.states.SandstoneSlab;
import net.minestom.server.instance.block.states.SandstoneStairs;
import net.minestom.server.instance.block.states.SandstoneWall;
import net.minestom.server.instance.block.states.Scaffolding;
import net.minestom.server.instance.block.states.SeaLantern;
import net.minestom.server.instance.block.states.SeaPickle;
import net.minestom.server.instance.block.states.Seagrass;
import net.minestom.server.instance.block.states.Shroomlight;
import net.minestom.server.instance.block.states.ShulkerBox;
import net.minestom.server.instance.block.states.SkeletonSkull;
import net.minestom.server.instance.block.states.SkeletonWallSkull;
import net.minestom.server.instance.block.states.SlimeBlock;
import net.minestom.server.instance.block.states.SmithingTable;
import net.minestom.server.instance.block.states.Smoker;
import net.minestom.server.instance.block.states.SmoothQuartz;
import net.minestom.server.instance.block.states.SmoothQuartzSlab;
import net.minestom.server.instance.block.states.SmoothQuartzStairs;
import net.minestom.server.instance.block.states.SmoothRedSandstone;
import net.minestom.server.instance.block.states.SmoothRedSandstoneSlab;
import net.minestom.server.instance.block.states.SmoothRedSandstoneStairs;
import net.minestom.server.instance.block.states.SmoothSandstone;
import net.minestom.server.instance.block.states.SmoothSandstoneSlab;
import net.minestom.server.instance.block.states.SmoothSandstoneStairs;
import net.minestom.server.instance.block.states.SmoothStone;
import net.minestom.server.instance.block.states.SmoothStoneSlab;
import net.minestom.server.instance.block.states.Snow;
import net.minestom.server.instance.block.states.SnowBlock;
import net.minestom.server.instance.block.states.SoulCampfire;
import net.minestom.server.instance.block.states.SoulFire;
import net.minestom.server.instance.block.states.SoulLantern;
import net.minestom.server.instance.block.states.SoulSand;
import net.minestom.server.instance.block.states.SoulSoil;
import net.minestom.server.instance.block.states.SoulTorch;
import net.minestom.server.instance.block.states.SoulWallTorch;
import net.minestom.server.instance.block.states.Spawner;
import net.minestom.server.instance.block.states.Sponge;
import net.minestom.server.instance.block.states.SpruceButton;
import net.minestom.server.instance.block.states.SpruceDoor;
import net.minestom.server.instance.block.states.SpruceFence;
import net.minestom.server.instance.block.states.SpruceFenceGate;
import net.minestom.server.instance.block.states.SpruceLeaves;
import net.minestom.server.instance.block.states.SpruceLog;
import net.minestom.server.instance.block.states.SprucePlanks;
import net.minestom.server.instance.block.states.SprucePressurePlate;
import net.minestom.server.instance.block.states.SpruceSapling;
import net.minestom.server.instance.block.states.SpruceSign;
import net.minestom.server.instance.block.states.SpruceSlab;
import net.minestom.server.instance.block.states.SpruceStairs;
import net.minestom.server.instance.block.states.SpruceTrapdoor;
import net.minestom.server.instance.block.states.SpruceWallSign;
import net.minestom.server.instance.block.states.SpruceWood;
import net.minestom.server.instance.block.states.StickyPiston;
import net.minestom.server.instance.block.states.Stone;
import net.minestom.server.instance.block.states.StoneBrickSlab;
import net.minestom.server.instance.block.states.StoneBrickStairs;
import net.minestom.server.instance.block.states.StoneBrickWall;
import net.minestom.server.instance.block.states.StoneBricks;
import net.minestom.server.instance.block.states.StoneButton;
import net.minestom.server.instance.block.states.StonePressurePlate;
import net.minestom.server.instance.block.states.StoneSlab;
import net.minestom.server.instance.block.states.StoneStairs;
import net.minestom.server.instance.block.states.Stonecutter;
import net.minestom.server.instance.block.states.StrippedAcaciaLog;
import net.minestom.server.instance.block.states.StrippedAcaciaWood;
import net.minestom.server.instance.block.states.StrippedBirchLog;
import net.minestom.server.instance.block.states.StrippedBirchWood;
import net.minestom.server.instance.block.states.StrippedCrimsonHyphae;
import net.minestom.server.instance.block.states.StrippedCrimsonStem;
import net.minestom.server.instance.block.states.StrippedDarkOakLog;
import net.minestom.server.instance.block.states.StrippedDarkOakWood;
import net.minestom.server.instance.block.states.StrippedJungleLog;
import net.minestom.server.instance.block.states.StrippedJungleWood;
import net.minestom.server.instance.block.states.StrippedOakLog;
import net.minestom.server.instance.block.states.StrippedOakWood;
import net.minestom.server.instance.block.states.StrippedSpruceLog;
import net.minestom.server.instance.block.states.StrippedSpruceWood;
import net.minestom.server.instance.block.states.StrippedWarpedHyphae;
import net.minestom.server.instance.block.states.StrippedWarpedStem;
import net.minestom.server.instance.block.states.StructureBlock;
import net.minestom.server.instance.block.states.StructureVoid;
import net.minestom.server.instance.block.states.SugarCane;
import net.minestom.server.instance.block.states.Sunflower;
import net.minestom.server.instance.block.states.SweetBerryBush;
import net.minestom.server.instance.block.states.TallGrass;
import net.minestom.server.instance.block.states.TallSeagrass;
import net.minestom.server.instance.block.states.Target;
import net.minestom.server.instance.block.states.Terracotta;
import net.minestom.server.instance.block.states.Tnt;
import net.minestom.server.instance.block.states.Torch;
import net.minestom.server.instance.block.states.TrappedChest;
import net.minestom.server.instance.block.states.Tripwire;
import net.minestom.server.instance.block.states.TripwireHook;
import net.minestom.server.instance.block.states.TubeCoral;
import net.minestom.server.instance.block.states.TubeCoralBlock;
import net.minestom.server.instance.block.states.TubeCoralFan;
import net.minestom.server.instance.block.states.TubeCoralWallFan;
import net.minestom.server.instance.block.states.TurtleEgg;
import net.minestom.server.instance.block.states.TwistingVines;
import net.minestom.server.instance.block.states.TwistingVinesPlant;
import net.minestom.server.instance.block.states.Vine;
import net.minestom.server.instance.block.states.VoidAir;
import net.minestom.server.instance.block.states.WallTorch;
import net.minestom.server.instance.block.states.WarpedButton;
import net.minestom.server.instance.block.states.WarpedDoor;
import net.minestom.server.instance.block.states.WarpedFence;
import net.minestom.server.instance.block.states.WarpedFenceGate;
import net.minestom.server.instance.block.states.WarpedFungus;
import net.minestom.server.instance.block.states.WarpedHyphae;
import net.minestom.server.instance.block.states.WarpedNylium;
import net.minestom.server.instance.block.states.WarpedPlanks;
import net.minestom.server.instance.block.states.WarpedPressurePlate;
import net.minestom.server.instance.block.states.WarpedRoots;
import net.minestom.server.instance.block.states.WarpedSign;
import net.minestom.server.instance.block.states.WarpedSlab;
import net.minestom.server.instance.block.states.WarpedStairs;
import net.minestom.server.instance.block.states.WarpedStem;
import net.minestom.server.instance.block.states.WarpedTrapdoor;
import net.minestom.server.instance.block.states.WarpedWallSign;
import net.minestom.server.instance.block.states.WarpedWartBlock;
import net.minestom.server.instance.block.states.Water;
import net.minestom.server.instance.block.states.WeepingVines;
import net.minestom.server.instance.block.states.WeepingVinesPlant;
import net.minestom.server.instance.block.states.WetSponge;
import net.minestom.server.instance.block.states.Wheat;
import net.minestom.server.instance.block.states.WhiteBanner;
import net.minestom.server.instance.block.states.WhiteBed;
import net.minestom.server.instance.block.states.WhiteCarpet;
import net.minestom.server.instance.block.states.WhiteConcrete;
import net.minestom.server.instance.block.states.WhiteConcretePowder;
import net.minestom.server.instance.block.states.WhiteGlazedTerracotta;
import net.minestom.server.instance.block.states.WhiteShulkerBox;
import net.minestom.server.instance.block.states.WhiteStainedGlass;
import net.minestom.server.instance.block.states.WhiteStainedGlassPane;
import net.minestom.server.instance.block.states.WhiteTerracotta;
import net.minestom.server.instance.block.states.WhiteTulip;
import net.minestom.server.instance.block.states.WhiteWallBanner;
import net.minestom.server.instance.block.states.WhiteWool;
import net.minestom.server.instance.block.states.WitherRose;
import net.minestom.server.instance.block.states.WitherSkeletonSkull;
import net.minestom.server.instance.block.states.WitherSkeletonWallSkull;
import net.minestom.server.instance.block.states.YellowBanner;
import net.minestom.server.instance.block.states.YellowBed;
import net.minestom.server.instance.block.states.YellowCarpet;
import net.minestom.server.instance.block.states.YellowConcrete;
import net.minestom.server.instance.block.states.YellowConcretePowder;
import net.minestom.server.instance.block.states.YellowGlazedTerracotta;
import net.minestom.server.instance.block.states.YellowShulkerBox;
import net.minestom.server.instance.block.states.YellowStainedGlass;
import net.minestom.server.instance.block.states.YellowStainedGlassPane;
import net.minestom.server.instance.block.states.YellowTerracotta;
import net.minestom.server.instance.block.states.YellowWallBanner;
import net.minestom.server.instance.block.states.YellowWool;
import net.minestom.server.instance.block.states.ZombieHead;
import net.minestom.server.instance.block.states.ZombieWallHead;
import net.minestom.server.raw_data.RawBlockData;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * AUTOGENERATED by BlockGenerator
 */
@SuppressWarnings("deprecation")
public class Block implements Keyed {
    public static final Block AIR = new Block(NamespaceID.from("minecraft:air"), (short) 0, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block STONE = new Block(NamespaceID.from("minecraft:stone"), (short) 1, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stone"), 0.6, 1.0, 1.0));

    public static final Block GRANITE = new Block(NamespaceID.from("minecraft:granite"), (short) 2, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:granite"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_GRANITE = new Block(NamespaceID.from("minecraft:polished_granite"), (short) 3, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_granite"), 0.6, 1.0, 1.0));

    public static final Block DIORITE = new Block(NamespaceID.from("minecraft:diorite"), (short) 4, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:diorite"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_DIORITE = new Block(NamespaceID.from("minecraft:polished_diorite"), (short) 5, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_diorite"), 0.6, 1.0, 1.0));

    public static final Block ANDESITE = new Block(NamespaceID.from("minecraft:andesite"), (short) 6, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:andesite"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_ANDESITE = new Block(NamespaceID.from("minecraft:polished_andesite"), (short) 7, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_andesite"), 0.6, 1.0, 1.0));

    public static final Block GRASS_BLOCK = new Block(NamespaceID.from("minecraft:grass_block"), (short) 9, new RawBlockData(0.6, () -> Registry.MATERIAL_REGISTRY.get("minecraft:grass_block"), 0.6, 1.0, 1.0));

    public static final Block DIRT = new Block(NamespaceID.from("minecraft:dirt"), (short) 10, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dirt"), 0.6, 1.0, 1.0));

    public static final Block COARSE_DIRT = new Block(NamespaceID.from("minecraft:coarse_dirt"), (short) 11, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:coarse_dirt"), 0.6, 1.0, 1.0));

    public static final Block PODZOL = new Block(NamespaceID.from("minecraft:podzol"), (short) 13, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:podzol"), 0.6, 1.0, 1.0));

    public static final Block COBBLESTONE = new Block(NamespaceID.from("minecraft:cobblestone"), (short) 14, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cobblestone"), 0.6, 1.0, 1.0));

    public static final Block OAK_PLANKS = new Block(NamespaceID.from("minecraft:oak_planks"), (short) 15, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_planks"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_PLANKS = new Block(NamespaceID.from("minecraft:spruce_planks"), (short) 16, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_planks"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_PLANKS = new Block(NamespaceID.from("minecraft:birch_planks"), (short) 17, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_planks"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_PLANKS = new Block(NamespaceID.from("minecraft:jungle_planks"), (short) 18, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_planks"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_PLANKS = new Block(NamespaceID.from("minecraft:acacia_planks"), (short) 19, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_planks"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_PLANKS = new Block(NamespaceID.from("minecraft:dark_oak_planks"), (short) 20, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_planks"), 0.6, 1.0, 1.0));

    public static final Block OAK_SAPLING = new Block(NamespaceID.from("minecraft:oak_sapling"), (short) 21, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_sapling"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_SAPLING = new Block(NamespaceID.from("minecraft:spruce_sapling"), (short) 23, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_sapling"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_SAPLING = new Block(NamespaceID.from("minecraft:birch_sapling"), (short) 25, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_sapling"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_SAPLING = new Block(NamespaceID.from("minecraft:jungle_sapling"), (short) 27, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_sapling"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_SAPLING = new Block(NamespaceID.from("minecraft:acacia_sapling"), (short) 29, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_sapling"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_SAPLING = new Block(NamespaceID.from("minecraft:dark_oak_sapling"), (short) 31, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_sapling"), 0.6, 1.0, 1.0));

    public static final Block BEDROCK = new Block(NamespaceID.from("minecraft:bedrock"), (short) 33, new RawBlockData(3600000.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bedrock"), 0.6, 1.0, 1.0));

    public static final Block WATER = new Block(NamespaceID.from("minecraft:water"), (short) 34, new RawBlockData(100.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block LAVA = new Block(NamespaceID.from("minecraft:lava"), (short) 50, new RawBlockData(100.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block SAND = new Block(NamespaceID.from("minecraft:sand"), (short) 66, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sand"), 0.6, 1.0, 1.0));

    public static final Block RED_SAND = new Block(NamespaceID.from("minecraft:red_sand"), (short) 67, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_sand"), 0.6, 1.0, 1.0));

    public static final Block GRAVEL = new Block(NamespaceID.from("minecraft:gravel"), (short) 68, new RawBlockData(0.6, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gravel"), 0.6, 1.0, 1.0));

    public static final Block GOLD_ORE = new Block(NamespaceID.from("minecraft:gold_ore"), (short) 69, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gold_ore"), 0.6, 1.0, 1.0));

    public static final Block IRON_ORE = new Block(NamespaceID.from("minecraft:iron_ore"), (short) 70, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:iron_ore"), 0.6, 1.0, 1.0));

    public static final Block COAL_ORE = new Block(NamespaceID.from("minecraft:coal_ore"), (short) 71, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:coal_ore"), 0.6, 1.0, 1.0));

    public static final Block NETHER_GOLD_ORE = new Block(NamespaceID.from("minecraft:nether_gold_ore"), (short) 72, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:nether_gold_ore"), 0.6, 1.0, 1.0));

    public static final Block OAK_LOG = new Block(NamespaceID.from("minecraft:oak_log"), (short) 74, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_log"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_LOG = new Block(NamespaceID.from("minecraft:spruce_log"), (short) 77, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_log"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_LOG = new Block(NamespaceID.from("minecraft:birch_log"), (short) 80, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_log"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_LOG = new Block(NamespaceID.from("minecraft:jungle_log"), (short) 83, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_log"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_LOG = new Block(NamespaceID.from("minecraft:acacia_log"), (short) 86, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_log"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_LOG = new Block(NamespaceID.from("minecraft:dark_oak_log"), (short) 89, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_log"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_SPRUCE_LOG = new Block(NamespaceID.from("minecraft:stripped_spruce_log"), (short) 92, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_spruce_log"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_BIRCH_LOG = new Block(NamespaceID.from("minecraft:stripped_birch_log"), (short) 95, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_birch_log"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_JUNGLE_LOG = new Block(NamespaceID.from("minecraft:stripped_jungle_log"), (short) 98, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_jungle_log"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_ACACIA_LOG = new Block(NamespaceID.from("minecraft:stripped_acacia_log"), (short) 101, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_acacia_log"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_DARK_OAK_LOG = new Block(NamespaceID.from("minecraft:stripped_dark_oak_log"), (short) 104, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_dark_oak_log"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_OAK_LOG = new Block(NamespaceID.from("minecraft:stripped_oak_log"), (short) 107, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_oak_log"), 0.6, 1.0, 1.0));

    public static final Block OAK_WOOD = new Block(NamespaceID.from("minecraft:oak_wood"), (short) 110, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_wood"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_WOOD = new Block(NamespaceID.from("minecraft:spruce_wood"), (short) 113, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_wood"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_WOOD = new Block(NamespaceID.from("minecraft:birch_wood"), (short) 116, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_wood"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_WOOD = new Block(NamespaceID.from("minecraft:jungle_wood"), (short) 119, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_wood"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_WOOD = new Block(NamespaceID.from("minecraft:acacia_wood"), (short) 122, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_wood"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_WOOD = new Block(NamespaceID.from("minecraft:dark_oak_wood"), (short) 125, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_wood"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_OAK_WOOD = new Block(NamespaceID.from("minecraft:stripped_oak_wood"), (short) 128, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_oak_wood"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_SPRUCE_WOOD = new Block(NamespaceID.from("minecraft:stripped_spruce_wood"), (short) 131, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_spruce_wood"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_BIRCH_WOOD = new Block(NamespaceID.from("minecraft:stripped_birch_wood"), (short) 134, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_birch_wood"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_JUNGLE_WOOD = new Block(NamespaceID.from("minecraft:stripped_jungle_wood"), (short) 137, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_jungle_wood"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_ACACIA_WOOD = new Block(NamespaceID.from("minecraft:stripped_acacia_wood"), (short) 140, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_acacia_wood"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_DARK_OAK_WOOD = new Block(NamespaceID.from("minecraft:stripped_dark_oak_wood"), (short) 143, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_dark_oak_wood"), 0.6, 1.0, 1.0));

    public static final Block OAK_LEAVES = new Block(NamespaceID.from("minecraft:oak_leaves"), (short) 158, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_leaves"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_LEAVES = new Block(NamespaceID.from("minecraft:spruce_leaves"), (short) 172, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_leaves"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_LEAVES = new Block(NamespaceID.from("minecraft:birch_leaves"), (short) 186, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_leaves"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_LEAVES = new Block(NamespaceID.from("minecraft:jungle_leaves"), (short) 200, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_leaves"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_LEAVES = new Block(NamespaceID.from("minecraft:acacia_leaves"), (short) 214, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_leaves"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_LEAVES = new Block(NamespaceID.from("minecraft:dark_oak_leaves"), (short) 228, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_leaves"), 0.6, 1.0, 1.0));

    public static final Block SPONGE = new Block(NamespaceID.from("minecraft:sponge"), (short) 229, new RawBlockData(0.6, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sponge"), 0.6, 1.0, 1.0));

    public static final Block WET_SPONGE = new Block(NamespaceID.from("minecraft:wet_sponge"), (short) 230, new RawBlockData(0.6, () -> Registry.MATERIAL_REGISTRY.get("minecraft:wet_sponge"), 0.6, 1.0, 1.0));

    public static final Block GLASS = new Block(NamespaceID.from("minecraft:glass"), (short) 231, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:glass"), 0.6, 1.0, 1.0));

    public static final Block LAPIS_ORE = new Block(NamespaceID.from("minecraft:lapis_ore"), (short) 232, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lapis_ore"), 0.6, 1.0, 1.0));

    public static final Block LAPIS_BLOCK = new Block(NamespaceID.from("minecraft:lapis_block"), (short) 233, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lapis_block"), 0.6, 1.0, 1.0));

    public static final Block DISPENSER = new Block(NamespaceID.from("minecraft:dispenser"), (short) 235, new RawBlockData(3.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dispenser"), 0.6, 1.0, 1.0));

    public static final Block SANDSTONE = new Block(NamespaceID.from("minecraft:sandstone"), (short) 246, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sandstone"), 0.6, 1.0, 1.0));

    public static final Block CHISELED_SANDSTONE = new Block(NamespaceID.from("minecraft:chiseled_sandstone"), (short) 247, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chiseled_sandstone"), 0.6, 1.0, 1.0));

    public static final Block CUT_SANDSTONE = new Block(NamespaceID.from("minecraft:cut_sandstone"), (short) 248, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cut_sandstone"), 0.6, 1.0, 1.0));

    public static final Block NOTE_BLOCK = new Block(NamespaceID.from("minecraft:note_block"), (short) 250, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:note_block"), 0.6, 1.0, 1.0));

    public static final Block WHITE_BED = new Block(NamespaceID.from("minecraft:white_bed"), (short) 1052, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_bed"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_BED = new Block(NamespaceID.from("minecraft:orange_bed"), (short) 1068, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_bed"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_BED = new Block(NamespaceID.from("minecraft:magenta_bed"), (short) 1084, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_bed"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_BED = new Block(NamespaceID.from("minecraft:light_blue_bed"), (short) 1100, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_bed"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_BED = new Block(NamespaceID.from("minecraft:yellow_bed"), (short) 1116, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_bed"), 0.6, 1.0, 1.0));

    public static final Block LIME_BED = new Block(NamespaceID.from("minecraft:lime_bed"), (short) 1132, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_bed"), 0.6, 1.0, 1.0));

    public static final Block PINK_BED = new Block(NamespaceID.from("minecraft:pink_bed"), (short) 1148, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_bed"), 0.6, 1.0, 1.0));

    public static final Block GRAY_BED = new Block(NamespaceID.from("minecraft:gray_bed"), (short) 1164, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_bed"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_BED = new Block(NamespaceID.from("minecraft:light_gray_bed"), (short) 1180, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_bed"), 0.6, 1.0, 1.0));

    public static final Block CYAN_BED = new Block(NamespaceID.from("minecraft:cyan_bed"), (short) 1196, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_bed"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_BED = new Block(NamespaceID.from("minecraft:purple_bed"), (short) 1212, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_bed"), 0.6, 1.0, 1.0));

    public static final Block BLUE_BED = new Block(NamespaceID.from("minecraft:blue_bed"), (short) 1228, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_bed"), 0.6, 1.0, 1.0));

    public static final Block BROWN_BED = new Block(NamespaceID.from("minecraft:brown_bed"), (short) 1244, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_bed"), 0.6, 1.0, 1.0));

    public static final Block GREEN_BED = new Block(NamespaceID.from("minecraft:green_bed"), (short) 1260, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_bed"), 0.6, 1.0, 1.0));

    public static final Block RED_BED = new Block(NamespaceID.from("minecraft:red_bed"), (short) 1276, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_bed"), 0.6, 1.0, 1.0));

    public static final Block BLACK_BED = new Block(NamespaceID.from("minecraft:black_bed"), (short) 1292, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_bed"), 0.6, 1.0, 1.0));

    public static final Block POWERED_RAIL = new Block(NamespaceID.from("minecraft:powered_rail"), (short) 1311, new RawBlockData(0.7, () -> Registry.MATERIAL_REGISTRY.get("minecraft:powered_rail"), 0.6, 1.0, 1.0));

    public static final Block DETECTOR_RAIL = new Block(NamespaceID.from("minecraft:detector_rail"), (short) 1323, new RawBlockData(0.7, () -> Registry.MATERIAL_REGISTRY.get("minecraft:detector_rail"), 0.6, 1.0, 1.0));

    public static final Block STICKY_PISTON = new Block(NamespaceID.from("minecraft:sticky_piston"), (short) 1335, new RawBlockData(1.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sticky_piston"), 0.6, 1.0, 1.0));

    public static final Block COBWEB = new Block(NamespaceID.from("minecraft:cobweb"), (short) 1341, new RawBlockData(4.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cobweb"), 0.6, 1.0, 1.0));

    public static final Block GRASS = new Block(NamespaceID.from("minecraft:grass"), (short) 1342, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:grass"), 0.6, 1.0, 1.0));

    public static final Block FERN = new Block(NamespaceID.from("minecraft:fern"), (short) 1343, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:fern"), 0.6, 1.0, 1.0));

    public static final Block DEAD_BUSH = new Block(NamespaceID.from("minecraft:dead_bush"), (short) 1344, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_bush"), 0.6, 1.0, 1.0));

    public static final Block SEAGRASS = new Block(NamespaceID.from("minecraft:seagrass"), (short) 1345, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:seagrass"), 0.6, 1.0, 1.0));

    public static final Block TALL_SEAGRASS = new Block(NamespaceID.from("minecraft:tall_seagrass"), (short) 1347, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block PISTON = new Block(NamespaceID.from("minecraft:piston"), (short) 1354, new RawBlockData(1.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:piston"), 0.6, 1.0, 1.0));

    public static final Block PISTON_HEAD = new Block(NamespaceID.from("minecraft:piston_head"), (short) 1362, new RawBlockData(1.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block WHITE_WOOL = new Block(NamespaceID.from("minecraft:white_wool"), (short) 1384, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_wool"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_WOOL = new Block(NamespaceID.from("minecraft:orange_wool"), (short) 1385, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_wool"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_WOOL = new Block(NamespaceID.from("minecraft:magenta_wool"), (short) 1386, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_wool"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_WOOL = new Block(NamespaceID.from("minecraft:light_blue_wool"), (short) 1387, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_wool"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_WOOL = new Block(NamespaceID.from("minecraft:yellow_wool"), (short) 1388, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_wool"), 0.6, 1.0, 1.0));

    public static final Block LIME_WOOL = new Block(NamespaceID.from("minecraft:lime_wool"), (short) 1389, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_wool"), 0.6, 1.0, 1.0));

    public static final Block PINK_WOOL = new Block(NamespaceID.from("minecraft:pink_wool"), (short) 1390, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_wool"), 0.6, 1.0, 1.0));

    public static final Block GRAY_WOOL = new Block(NamespaceID.from("minecraft:gray_wool"), (short) 1391, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_wool"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_WOOL = new Block(NamespaceID.from("minecraft:light_gray_wool"), (short) 1392, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_wool"), 0.6, 1.0, 1.0));

    public static final Block CYAN_WOOL = new Block(NamespaceID.from("minecraft:cyan_wool"), (short) 1393, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_wool"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_WOOL = new Block(NamespaceID.from("minecraft:purple_wool"), (short) 1394, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_wool"), 0.6, 1.0, 1.0));

    public static final Block BLUE_WOOL = new Block(NamespaceID.from("minecraft:blue_wool"), (short) 1395, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_wool"), 0.6, 1.0, 1.0));

    public static final Block BROWN_WOOL = new Block(NamespaceID.from("minecraft:brown_wool"), (short) 1396, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_wool"), 0.6, 1.0, 1.0));

    public static final Block GREEN_WOOL = new Block(NamespaceID.from("minecraft:green_wool"), (short) 1397, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_wool"), 0.6, 1.0, 1.0));

    public static final Block RED_WOOL = new Block(NamespaceID.from("minecraft:red_wool"), (short) 1398, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_wool"), 0.6, 1.0, 1.0));

    public static final Block BLACK_WOOL = new Block(NamespaceID.from("minecraft:black_wool"), (short) 1399, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_wool"), 0.6, 1.0, 1.0));

    public static final Block MOVING_PISTON = new Block(NamespaceID.from("minecraft:moving_piston"), (short) 1400, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block DANDELION = new Block(NamespaceID.from("minecraft:dandelion"), (short) 1412, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dandelion"), 0.6, 1.0, 1.0));

    public static final Block POPPY = new Block(NamespaceID.from("minecraft:poppy"), (short) 1413, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:poppy"), 0.6, 1.0, 1.0));

    public static final Block BLUE_ORCHID = new Block(NamespaceID.from("minecraft:blue_orchid"), (short) 1414, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_orchid"), 0.6, 1.0, 1.0));

    public static final Block ALLIUM = new Block(NamespaceID.from("minecraft:allium"), (short) 1415, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:allium"), 0.6, 1.0, 1.0));

    public static final Block AZURE_BLUET = new Block(NamespaceID.from("minecraft:azure_bluet"), (short) 1416, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:azure_bluet"), 0.6, 1.0, 1.0));

    public static final Block RED_TULIP = new Block(NamespaceID.from("minecraft:red_tulip"), (short) 1417, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_tulip"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_TULIP = new Block(NamespaceID.from("minecraft:orange_tulip"), (short) 1418, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_tulip"), 0.6, 1.0, 1.0));

    public static final Block WHITE_TULIP = new Block(NamespaceID.from("minecraft:white_tulip"), (short) 1419, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_tulip"), 0.6, 1.0, 1.0));

    public static final Block PINK_TULIP = new Block(NamespaceID.from("minecraft:pink_tulip"), (short) 1420, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_tulip"), 0.6, 1.0, 1.0));

    public static final Block OXEYE_DAISY = new Block(NamespaceID.from("minecraft:oxeye_daisy"), (short) 1421, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oxeye_daisy"), 0.6, 1.0, 1.0));

    public static final Block CORNFLOWER = new Block(NamespaceID.from("minecraft:cornflower"), (short) 1422, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cornflower"), 0.6, 1.0, 1.0));

    public static final Block WITHER_ROSE = new Block(NamespaceID.from("minecraft:wither_rose"), (short) 1423, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:wither_rose"), 0.6, 1.0, 1.0));

    public static final Block LILY_OF_THE_VALLEY = new Block(NamespaceID.from("minecraft:lily_of_the_valley"), (short) 1424, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lily_of_the_valley"), 0.6, 1.0, 1.0));

    public static final Block BROWN_MUSHROOM = new Block(NamespaceID.from("minecraft:brown_mushroom"), (short) 1425, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_mushroom"), 0.6, 1.0, 1.0));

    public static final Block RED_MUSHROOM = new Block(NamespaceID.from("minecraft:red_mushroom"), (short) 1426, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_mushroom"), 0.6, 1.0, 1.0));

    public static final Block GOLD_BLOCK = new Block(NamespaceID.from("minecraft:gold_block"), (short) 1427, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gold_block"), 0.6, 1.0, 1.0));

    public static final Block IRON_BLOCK = new Block(NamespaceID.from("minecraft:iron_block"), (short) 1428, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:iron_block"), 0.6, 1.0, 1.0));

    public static final Block BRICKS = new Block(NamespaceID.from("minecraft:bricks"), (short) 1429, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bricks"), 0.6, 1.0, 1.0));

    public static final Block TNT = new Block(NamespaceID.from("minecraft:tnt"), (short) 1431, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:tnt"), 0.6, 1.0, 1.0));

    public static final Block BOOKSHELF = new Block(NamespaceID.from("minecraft:bookshelf"), (short) 1432, new RawBlockData(1.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bookshelf"), 0.6, 1.0, 1.0));

    public static final Block MOSSY_COBBLESTONE = new Block(NamespaceID.from("minecraft:mossy_cobblestone"), (short) 1433, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:mossy_cobblestone"), 0.6, 1.0, 1.0));

    public static final Block OBSIDIAN = new Block(NamespaceID.from("minecraft:obsidian"), (short) 1434, new RawBlockData(1200.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:obsidian"), 0.6, 1.0, 1.0));

    public static final Block TORCH = new Block(NamespaceID.from("minecraft:torch"), (short) 1435, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:torch"), 0.6, 1.0, 1.0));

    public static final Block WALL_TORCH = new Block(NamespaceID.from("minecraft:wall_torch"), (short) 1436, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:torch"), 0.6, 1.0, 1.0));

    public static final Block FIRE = new Block(NamespaceID.from("minecraft:fire"), (short) 1471, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block SOUL_FIRE = new Block(NamespaceID.from("minecraft:soul_fire"), (short) 1952, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block SPAWNER = new Block(NamespaceID.from("minecraft:spawner"), (short) 1953, new RawBlockData(5.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spawner"), 0.6, 1.0, 1.0));

    public static final Block OAK_STAIRS = new Block(NamespaceID.from("minecraft:oak_stairs"), (short) 1965, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_stairs"), 0.6, 1.0, 1.0));

    public static final Block CHEST = new Block(NamespaceID.from("minecraft:chest"), (short) 2035, new RawBlockData(2.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chest"), 0.6, 1.0, 1.0));

    public static final Block REDSTONE_WIRE = new Block(NamespaceID.from("minecraft:redstone_wire"), (short) 3218, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:redstone"), 0.6, 1.0, 1.0));

    public static final Block DIAMOND_ORE = new Block(NamespaceID.from("minecraft:diamond_ore"), (short) 3354, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:diamond_ore"), 0.6, 1.0, 1.0));

    public static final Block DIAMOND_BLOCK = new Block(NamespaceID.from("minecraft:diamond_block"), (short) 3355, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:diamond_block"), 0.6, 1.0, 1.0));

    public static final Block CRAFTING_TABLE = new Block(NamespaceID.from("minecraft:crafting_table"), (short) 3356, new RawBlockData(2.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crafting_table"), 0.6, 1.0, 1.0));

    public static final Block WHEAT = new Block(NamespaceID.from("minecraft:wheat"), (short) 3357, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:wheat_seeds"), 0.6, 1.0, 1.0));

    public static final Block FARMLAND = new Block(NamespaceID.from("minecraft:farmland"), (short) 3365, new RawBlockData(0.6, () -> Registry.MATERIAL_REGISTRY.get("minecraft:farmland"), 0.6, 1.0, 1.0));

    public static final Block FURNACE = new Block(NamespaceID.from("minecraft:furnace"), (short) 3374, new RawBlockData(3.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:furnace"), 0.6, 1.0, 1.0));

    public static final Block OAK_SIGN = new Block(NamespaceID.from("minecraft:oak_sign"), (short) 3382, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_sign"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_SIGN = new Block(NamespaceID.from("minecraft:spruce_sign"), (short) 3414, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_sign"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_SIGN = new Block(NamespaceID.from("minecraft:birch_sign"), (short) 3446, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_sign"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_SIGN = new Block(NamespaceID.from("minecraft:acacia_sign"), (short) 3478, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_sign"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_SIGN = new Block(NamespaceID.from("minecraft:jungle_sign"), (short) 3510, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_sign"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_SIGN = new Block(NamespaceID.from("minecraft:dark_oak_sign"), (short) 3542, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_sign"), 0.6, 1.0, 1.0));

    public static final Block OAK_DOOR = new Block(NamespaceID.from("minecraft:oak_door"), (short) 3584, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_door"), 0.6, 1.0, 1.0));

    public static final Block LADDER = new Block(NamespaceID.from("minecraft:ladder"), (short) 3638, new RawBlockData(0.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:ladder"), 0.6, 1.0, 1.0));

    public static final Block RAIL = new Block(NamespaceID.from("minecraft:rail"), (short) 3645, new RawBlockData(0.7, () -> Registry.MATERIAL_REGISTRY.get("minecraft:rail"), 0.6, 1.0, 1.0));

    public static final Block COBBLESTONE_STAIRS = new Block(NamespaceID.from("minecraft:cobblestone_stairs"), (short) 3666, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cobblestone_stairs"), 0.6, 1.0, 1.0));

    public static final Block OAK_WALL_SIGN = new Block(NamespaceID.from("minecraft:oak_wall_sign"), (short) 3736, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_sign"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_WALL_SIGN = new Block(NamespaceID.from("minecraft:spruce_wall_sign"), (short) 3744, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_sign"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_WALL_SIGN = new Block(NamespaceID.from("minecraft:birch_wall_sign"), (short) 3752, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_sign"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_WALL_SIGN = new Block(NamespaceID.from("minecraft:acacia_wall_sign"), (short) 3760, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_sign"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_WALL_SIGN = new Block(NamespaceID.from("minecraft:jungle_wall_sign"), (short) 3768, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_sign"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_WALL_SIGN = new Block(NamespaceID.from("minecraft:dark_oak_wall_sign"), (short) 3776, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_sign"), 0.6, 1.0, 1.0));

    public static final Block LEVER = new Block(NamespaceID.from("minecraft:lever"), (short) 3792, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lever"), 0.6, 1.0, 1.0));

    public static final Block STONE_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:stone_pressure_plate"), (short) 3808, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stone_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block IRON_DOOR = new Block(NamespaceID.from("minecraft:iron_door"), (short) 3820, new RawBlockData(5.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:iron_door"), 0.6, 1.0, 1.0));

    public static final Block OAK_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:oak_pressure_plate"), (short) 3874, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:spruce_pressure_plate"), (short) 3876, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:birch_pressure_plate"), (short) 3878, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:jungle_pressure_plate"), (short) 3880, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:acacia_pressure_plate"), (short) 3882, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:dark_oak_pressure_plate"), (short) 3884, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block REDSTONE_ORE = new Block(NamespaceID.from("minecraft:redstone_ore"), (short) 3886, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:redstone_ore"), 0.6, 1.0, 1.0));

    public static final Block REDSTONE_TORCH = new Block(NamespaceID.from("minecraft:redstone_torch"), (short) 3887, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:redstone_torch"), 0.6, 1.0, 1.0));

    public static final Block REDSTONE_WALL_TORCH = new Block(NamespaceID.from("minecraft:redstone_wall_torch"), (short) 3889, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:redstone_torch"), 0.6, 1.0, 1.0));

    public static final Block STONE_BUTTON = new Block(NamespaceID.from("minecraft:stone_button"), (short) 3906, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stone_button"), 0.6, 1.0, 1.0));

    public static final Block SNOW = new Block(NamespaceID.from("minecraft:snow"), (short) 3921, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:snow"), 0.6, 1.0, 1.0));

    public static final Block ICE = new Block(NamespaceID.from("minecraft:ice"), (short) 3929, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:ice"), 0.98, 1.0, 1.0));

    public static final Block SNOW_BLOCK = new Block(NamespaceID.from("minecraft:snow_block"), (short) 3930, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:snow_block"), 0.6, 1.0, 1.0));

    public static final Block CACTUS = new Block(NamespaceID.from("minecraft:cactus"), (short) 3931, new RawBlockData(0.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cactus"), 0.6, 1.0, 1.0));

    public static final Block CLAY = new Block(NamespaceID.from("minecraft:clay"), (short) 3947, new RawBlockData(0.6, () -> Registry.MATERIAL_REGISTRY.get("minecraft:clay"), 0.6, 1.0, 1.0));

    public static final Block SUGAR_CANE = new Block(NamespaceID.from("minecraft:sugar_cane"), (short) 3948, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sugar_cane"), 0.6, 1.0, 1.0));

    public static final Block JUKEBOX = new Block(NamespaceID.from("minecraft:jukebox"), (short) 3965, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jukebox"), 0.6, 1.0, 1.0));

    public static final Block OAK_FENCE = new Block(NamespaceID.from("minecraft:oak_fence"), (short) 3997, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_fence"), 0.6, 1.0, 1.0));

    public static final Block PUMPKIN = new Block(NamespaceID.from("minecraft:pumpkin"), (short) 3998, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pumpkin"), 0.6, 1.0, 1.0));

    public static final Block NETHERRACK = new Block(NamespaceID.from("minecraft:netherrack"), (short) 3999, new RawBlockData(0.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:netherrack"), 0.6, 1.0, 1.0));

    public static final Block SOUL_SAND = new Block(NamespaceID.from("minecraft:soul_sand"), (short) 4000, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:soul_sand"), 0.6, 0.4, 1.0));

    public static final Block SOUL_SOIL = new Block(NamespaceID.from("minecraft:soul_soil"), (short) 4001, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:soul_soil"), 0.6, 1.0, 1.0));

    public static final Block BASALT = new Block(NamespaceID.from("minecraft:basalt"), (short) 4003, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:basalt"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BASALT = new Block(NamespaceID.from("minecraft:polished_basalt"), (short) 4006, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_basalt"), 0.6, 1.0, 1.0));

    public static final Block SOUL_TORCH = new Block(NamespaceID.from("minecraft:soul_torch"), (short) 4008, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:soul_torch"), 0.6, 1.0, 1.0));

    public static final Block SOUL_WALL_TORCH = new Block(NamespaceID.from("minecraft:soul_wall_torch"), (short) 4009, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:soul_torch"), 0.6, 1.0, 1.0));

    public static final Block GLOWSTONE = new Block(NamespaceID.from("minecraft:glowstone"), (short) 4013, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:glowstone"), 0.6, 1.0, 1.0));

    public static final Block NETHER_PORTAL = new Block(NamespaceID.from("minecraft:nether_portal"), (short) 4014, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block CARVED_PUMPKIN = new Block(NamespaceID.from("minecraft:carved_pumpkin"), (short) 4016, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:carved_pumpkin"), 0.6, 1.0, 1.0));

    public static final Block JACK_O_LANTERN = new Block(NamespaceID.from("minecraft:jack_o_lantern"), (short) 4020, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jack_o_lantern"), 0.6, 1.0, 1.0));

    public static final Block CAKE = new Block(NamespaceID.from("minecraft:cake"), (short) 4024, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cake"), 0.6, 1.0, 1.0));

    public static final Block REPEATER = new Block(NamespaceID.from("minecraft:repeater"), (short) 4034, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:repeater"), 0.6, 1.0, 1.0));

    public static final Block WHITE_STAINED_GLASS = new Block(NamespaceID.from("minecraft:white_stained_glass"), (short) 4095, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_STAINED_GLASS = new Block(NamespaceID.from("minecraft:orange_stained_glass"), (short) 4096, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_STAINED_GLASS = new Block(NamespaceID.from("minecraft:magenta_stained_glass"), (short) 4097, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_STAINED_GLASS = new Block(NamespaceID.from("minecraft:light_blue_stained_glass"), (short) 4098, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_STAINED_GLASS = new Block(NamespaceID.from("minecraft:yellow_stained_glass"), (short) 4099, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block LIME_STAINED_GLASS = new Block(NamespaceID.from("minecraft:lime_stained_glass"), (short) 4100, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block PINK_STAINED_GLASS = new Block(NamespaceID.from("minecraft:pink_stained_glass"), (short) 4101, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block GRAY_STAINED_GLASS = new Block(NamespaceID.from("minecraft:gray_stained_glass"), (short) 4102, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_STAINED_GLASS = new Block(NamespaceID.from("minecraft:light_gray_stained_glass"), (short) 4103, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block CYAN_STAINED_GLASS = new Block(NamespaceID.from("minecraft:cyan_stained_glass"), (short) 4104, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_STAINED_GLASS = new Block(NamespaceID.from("minecraft:purple_stained_glass"), (short) 4105, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block BLUE_STAINED_GLASS = new Block(NamespaceID.from("minecraft:blue_stained_glass"), (short) 4106, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block BROWN_STAINED_GLASS = new Block(NamespaceID.from("minecraft:brown_stained_glass"), (short) 4107, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block GREEN_STAINED_GLASS = new Block(NamespaceID.from("minecraft:green_stained_glass"), (short) 4108, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block RED_STAINED_GLASS = new Block(NamespaceID.from("minecraft:red_stained_glass"), (short) 4109, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block BLACK_STAINED_GLASS = new Block(NamespaceID.from("minecraft:black_stained_glass"), (short) 4110, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_stained_glass"), 0.6, 1.0, 1.0));

    public static final Block OAK_TRAPDOOR = new Block(NamespaceID.from("minecraft:oak_trapdoor"), (short) 4126, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_trapdoor"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_TRAPDOOR = new Block(NamespaceID.from("minecraft:spruce_trapdoor"), (short) 4190, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_trapdoor"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_TRAPDOOR = new Block(NamespaceID.from("minecraft:birch_trapdoor"), (short) 4254, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_trapdoor"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_TRAPDOOR = new Block(NamespaceID.from("minecraft:jungle_trapdoor"), (short) 4318, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_trapdoor"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_TRAPDOOR = new Block(NamespaceID.from("minecraft:acacia_trapdoor"), (short) 4382, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_trapdoor"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_TRAPDOOR = new Block(NamespaceID.from("minecraft:dark_oak_trapdoor"), (short) 4446, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_trapdoor"), 0.6, 1.0, 1.0));

    public static final Block STONE_BRICKS = new Block(NamespaceID.from("minecraft:stone_bricks"), (short) 4495, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stone_bricks"), 0.6, 1.0, 1.0));

    public static final Block MOSSY_STONE_BRICKS = new Block(NamespaceID.from("minecraft:mossy_stone_bricks"), (short) 4496, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:mossy_stone_bricks"), 0.6, 1.0, 1.0));

    public static final Block CRACKED_STONE_BRICKS = new Block(NamespaceID.from("minecraft:cracked_stone_bricks"), (short) 4497, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cracked_stone_bricks"), 0.6, 1.0, 1.0));

    public static final Block CHISELED_STONE_BRICKS = new Block(NamespaceID.from("minecraft:chiseled_stone_bricks"), (short) 4498, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chiseled_stone_bricks"), 0.6, 1.0, 1.0));

    public static final Block INFESTED_STONE = new Block(NamespaceID.from("minecraft:infested_stone"), (short) 4499, new RawBlockData(0.75, () -> Registry.MATERIAL_REGISTRY.get("minecraft:infested_stone"), 0.6, 1.0, 1.0));

    public static final Block INFESTED_COBBLESTONE = new Block(NamespaceID.from("minecraft:infested_cobblestone"), (short) 4500, new RawBlockData(0.75, () -> Registry.MATERIAL_REGISTRY.get("minecraft:infested_cobblestone"), 0.6, 1.0, 1.0));

    public static final Block INFESTED_STONE_BRICKS = new Block(NamespaceID.from("minecraft:infested_stone_bricks"), (short) 4501, new RawBlockData(0.75, () -> Registry.MATERIAL_REGISTRY.get("minecraft:infested_stone_bricks"), 0.6, 1.0, 1.0));

    public static final Block INFESTED_MOSSY_STONE_BRICKS = new Block(NamespaceID.from("minecraft:infested_mossy_stone_bricks"), (short) 4502, new RawBlockData(0.75, () -> Registry.MATERIAL_REGISTRY.get("minecraft:infested_mossy_stone_bricks"), 0.6, 1.0, 1.0));

    public static final Block INFESTED_CRACKED_STONE_BRICKS = new Block(NamespaceID.from("minecraft:infested_cracked_stone_bricks"), (short) 4503, new RawBlockData(0.75, () -> Registry.MATERIAL_REGISTRY.get("minecraft:infested_cracked_stone_bricks"), 0.6, 1.0, 1.0));

    public static final Block INFESTED_CHISELED_STONE_BRICKS = new Block(NamespaceID.from("minecraft:infested_chiseled_stone_bricks"), (short) 4504, new RawBlockData(0.75, () -> Registry.MATERIAL_REGISTRY.get("minecraft:infested_chiseled_stone_bricks"), 0.6, 1.0, 1.0));

    public static final Block BROWN_MUSHROOM_BLOCK = new Block(NamespaceID.from("minecraft:brown_mushroom_block"), (short) 4505, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_mushroom_block"), 0.6, 1.0, 1.0));

    public static final Block RED_MUSHROOM_BLOCK = new Block(NamespaceID.from("minecraft:red_mushroom_block"), (short) 4569, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_mushroom_block"), 0.6, 1.0, 1.0));

    public static final Block MUSHROOM_STEM = new Block(NamespaceID.from("minecraft:mushroom_stem"), (short) 4633, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:mushroom_stem"), 0.6, 1.0, 1.0));

    public static final Block IRON_BARS = new Block(NamespaceID.from("minecraft:iron_bars"), (short) 4728, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:iron_bars"), 0.6, 1.0, 1.0));

    public static final Block CHAIN = new Block(NamespaceID.from("minecraft:chain"), (short) 4732, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chain"), 0.6, 1.0, 1.0));

    public static final Block GLASS_PANE = new Block(NamespaceID.from("minecraft:glass_pane"), (short) 4766, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:glass_pane"), 0.6, 1.0, 1.0));

    public static final Block MELON = new Block(NamespaceID.from("minecraft:melon"), (short) 4767, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:melon"), 0.6, 1.0, 1.0));

    public static final Block ATTACHED_PUMPKIN_STEM = new Block(NamespaceID.from("minecraft:attached_pumpkin_stem"), (short) 4768, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block ATTACHED_MELON_STEM = new Block(NamespaceID.from("minecraft:attached_melon_stem"), (short) 4772, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block PUMPKIN_STEM = new Block(NamespaceID.from("minecraft:pumpkin_stem"), (short) 4776, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pumpkin_seeds"), 0.6, 1.0, 1.0));

    public static final Block MELON_STEM = new Block(NamespaceID.from("minecraft:melon_stem"), (short) 4784, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:melon_seeds"), 0.6, 1.0, 1.0));

    public static final Block VINE = new Block(NamespaceID.from("minecraft:vine"), (short) 4823, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:vine"), 0.6, 1.0, 1.0));

    public static final Block OAK_FENCE_GATE = new Block(NamespaceID.from("minecraft:oak_fence_gate"), (short) 4831, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_fence_gate"), 0.6, 1.0, 1.0));

    public static final Block BRICK_STAIRS = new Block(NamespaceID.from("minecraft:brick_stairs"), (short) 4867, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brick_stairs"), 0.6, 1.0, 1.0));

    public static final Block STONE_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:stone_brick_stairs"), (short) 4947, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stone_brick_stairs"), 0.6, 1.0, 1.0));

    public static final Block MYCELIUM = new Block(NamespaceID.from("minecraft:mycelium"), (short) 5017, new RawBlockData(0.6, () -> Registry.MATERIAL_REGISTRY.get("minecraft:mycelium"), 0.6, 1.0, 1.0));

    public static final Block LILY_PAD = new Block(NamespaceID.from("minecraft:lily_pad"), (short) 5018, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lily_pad"), 0.6, 1.0, 1.0));

    public static final Block NETHER_BRICKS = new Block(NamespaceID.from("minecraft:nether_bricks"), (short) 5019, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:nether_bricks"), 0.6, 1.0, 1.0));

    public static final Block NETHER_BRICK_FENCE = new Block(NamespaceID.from("minecraft:nether_brick_fence"), (short) 5051, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:nether_brick_fence"), 0.6, 1.0, 1.0));

    public static final Block NETHER_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:nether_brick_stairs"), (short) 5063, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:nether_brick_stairs"), 0.6, 1.0, 1.0));

    public static final Block NETHER_WART = new Block(NamespaceID.from("minecraft:nether_wart"), (short) 5132, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:nether_wart"), 0.6, 1.0, 1.0));

    public static final Block ENCHANTING_TABLE = new Block(NamespaceID.from("minecraft:enchanting_table"), (short) 5136, new RawBlockData(1200.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:enchanting_table"), 0.6, 1.0, 1.0));

    public static final Block BREWING_STAND = new Block(NamespaceID.from("minecraft:brewing_stand"), (short) 5144, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brewing_stand"), 0.6, 1.0, 1.0));

    public static final Block CAULDRON = new Block(NamespaceID.from("minecraft:cauldron"), (short) 5145, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cauldron"), 0.6, 1.0, 1.0));

    public static final Block END_PORTAL = new Block(NamespaceID.from("minecraft:end_portal"), (short) 5149, new RawBlockData(3600000.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block END_PORTAL_FRAME = new Block(NamespaceID.from("minecraft:end_portal_frame"), (short) 5154, new RawBlockData(3600000.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:end_portal_frame"), 0.6, 1.0, 1.0));

    public static final Block END_STONE = new Block(NamespaceID.from("minecraft:end_stone"), (short) 5158, new RawBlockData(9.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:end_stone"), 0.6, 1.0, 1.0));

    public static final Block DRAGON_EGG = new Block(NamespaceID.from("minecraft:dragon_egg"), (short) 5159, new RawBlockData(9.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dragon_egg"), 0.6, 1.0, 1.0));

    public static final Block REDSTONE_LAMP = new Block(NamespaceID.from("minecraft:redstone_lamp"), (short) 5161, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:redstone_lamp"), 0.6, 1.0, 1.0));

    public static final Block COCOA = new Block(NamespaceID.from("minecraft:cocoa"), (short) 5162, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cocoa_beans"), 0.6, 1.0, 1.0));

    public static final Block SANDSTONE_STAIRS = new Block(NamespaceID.from("minecraft:sandstone_stairs"), (short) 5185, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sandstone_stairs"), 0.6, 1.0, 1.0));

    public static final Block EMERALD_ORE = new Block(NamespaceID.from("minecraft:emerald_ore"), (short) 5254, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:emerald_ore"), 0.6, 1.0, 1.0));

    public static final Block ENDER_CHEST = new Block(NamespaceID.from("minecraft:ender_chest"), (short) 5256, new RawBlockData(600.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:ender_chest"), 0.6, 1.0, 1.0));

    public static final Block TRIPWIRE_HOOK = new Block(NamespaceID.from("minecraft:tripwire_hook"), (short) 5272, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:tripwire_hook"), 0.6, 1.0, 1.0));

    public static final Block TRIPWIRE = new Block(NamespaceID.from("minecraft:tripwire"), (short) 5406, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:string"), 0.6, 1.0, 1.0));

    public static final Block EMERALD_BLOCK = new Block(NamespaceID.from("minecraft:emerald_block"), (short) 5407, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:emerald_block"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_STAIRS = new Block(NamespaceID.from("minecraft:spruce_stairs"), (short) 5419, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_stairs"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_STAIRS = new Block(NamespaceID.from("minecraft:birch_stairs"), (short) 5499, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_stairs"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_STAIRS = new Block(NamespaceID.from("minecraft:jungle_stairs"), (short) 5579, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_stairs"), 0.6, 1.0, 1.0));

    public static final Block COMMAND_BLOCK = new Block(NamespaceID.from("minecraft:command_block"), (short) 5654, new RawBlockData(3600000.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:command_block"), 0.6, 1.0, 1.0));

    public static final Block BEACON = new Block(NamespaceID.from("minecraft:beacon"), (short) 5660, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:beacon"), 0.6, 1.0, 1.0));

    public static final Block COBBLESTONE_WALL = new Block(NamespaceID.from("minecraft:cobblestone_wall"), (short) 5664, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cobblestone_wall"), 0.6, 1.0, 1.0));

    public static final Block MOSSY_COBBLESTONE_WALL = new Block(NamespaceID.from("minecraft:mossy_cobblestone_wall"), (short) 5988, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:mossy_cobblestone_wall"), 0.6, 1.0, 1.0));

    public static final Block FLOWER_POT = new Block(NamespaceID.from("minecraft:flower_pot"), (short) 6309, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:flower_pot"), 0.6, 1.0, 1.0));

    public static final Block POTTED_OAK_SAPLING = new Block(NamespaceID.from("minecraft:potted_oak_sapling"), (short) 6310, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_SPRUCE_SAPLING = new Block(NamespaceID.from("minecraft:potted_spruce_sapling"), (short) 6311, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_BIRCH_SAPLING = new Block(NamespaceID.from("minecraft:potted_birch_sapling"), (short) 6312, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_JUNGLE_SAPLING = new Block(NamespaceID.from("minecraft:potted_jungle_sapling"), (short) 6313, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_ACACIA_SAPLING = new Block(NamespaceID.from("minecraft:potted_acacia_sapling"), (short) 6314, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_DARK_OAK_SAPLING = new Block(NamespaceID.from("minecraft:potted_dark_oak_sapling"), (short) 6315, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_FERN = new Block(NamespaceID.from("minecraft:potted_fern"), (short) 6316, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_DANDELION = new Block(NamespaceID.from("minecraft:potted_dandelion"), (short) 6317, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_POPPY = new Block(NamespaceID.from("minecraft:potted_poppy"), (short) 6318, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_BLUE_ORCHID = new Block(NamespaceID.from("minecraft:potted_blue_orchid"), (short) 6319, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_ALLIUM = new Block(NamespaceID.from("minecraft:potted_allium"), (short) 6320, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_AZURE_BLUET = new Block(NamespaceID.from("minecraft:potted_azure_bluet"), (short) 6321, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_RED_TULIP = new Block(NamespaceID.from("minecraft:potted_red_tulip"), (short) 6322, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_ORANGE_TULIP = new Block(NamespaceID.from("minecraft:potted_orange_tulip"), (short) 6323, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_WHITE_TULIP = new Block(NamespaceID.from("minecraft:potted_white_tulip"), (short) 6324, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_PINK_TULIP = new Block(NamespaceID.from("minecraft:potted_pink_tulip"), (short) 6325, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_OXEYE_DAISY = new Block(NamespaceID.from("minecraft:potted_oxeye_daisy"), (short) 6326, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_CORNFLOWER = new Block(NamespaceID.from("minecraft:potted_cornflower"), (short) 6327, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_LILY_OF_THE_VALLEY = new Block(NamespaceID.from("minecraft:potted_lily_of_the_valley"), (short) 6328, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_WITHER_ROSE = new Block(NamespaceID.from("minecraft:potted_wither_rose"), (short) 6329, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_RED_MUSHROOM = new Block(NamespaceID.from("minecraft:potted_red_mushroom"), (short) 6330, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_BROWN_MUSHROOM = new Block(NamespaceID.from("minecraft:potted_brown_mushroom"), (short) 6331, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_DEAD_BUSH = new Block(NamespaceID.from("minecraft:potted_dead_bush"), (short) 6332, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_CACTUS = new Block(NamespaceID.from("minecraft:potted_cactus"), (short) 6333, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block CARROTS = new Block(NamespaceID.from("minecraft:carrots"), (short) 6334, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:carrot"), 0.6, 1.0, 1.0));

    public static final Block POTATOES = new Block(NamespaceID.from("minecraft:potatoes"), (short) 6342, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:potato"), 0.6, 1.0, 1.0));

    public static final Block OAK_BUTTON = new Block(NamespaceID.from("minecraft:oak_button"), (short) 6359, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_button"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_BUTTON = new Block(NamespaceID.from("minecraft:spruce_button"), (short) 6383, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_button"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_BUTTON = new Block(NamespaceID.from("minecraft:birch_button"), (short) 6407, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_button"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_BUTTON = new Block(NamespaceID.from("minecraft:jungle_button"), (short) 6431, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_button"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_BUTTON = new Block(NamespaceID.from("minecraft:acacia_button"), (short) 6455, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_button"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_BUTTON = new Block(NamespaceID.from("minecraft:dark_oak_button"), (short) 6479, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_button"), 0.6, 1.0, 1.0));

    public static final Block SKELETON_SKULL = new Block(NamespaceID.from("minecraft:skeleton_skull"), (short) 6494, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:skeleton_skull"), 0.6, 1.0, 1.0));

    public static final Block SKELETON_WALL_SKULL = new Block(NamespaceID.from("minecraft:skeleton_wall_skull"), (short) 6510, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:skeleton_skull"), 0.6, 1.0, 1.0));

    public static final Block WITHER_SKELETON_SKULL = new Block(NamespaceID.from("minecraft:wither_skeleton_skull"), (short) 6514, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:wither_skeleton_skull"), 0.6, 1.0, 1.0));

    public static final Block WITHER_SKELETON_WALL_SKULL = new Block(NamespaceID.from("minecraft:wither_skeleton_wall_skull"), (short) 6530, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:wither_skeleton_skull"), 0.6, 1.0, 1.0));

    public static final Block ZOMBIE_HEAD = new Block(NamespaceID.from("minecraft:zombie_head"), (short) 6534, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:zombie_head"), 0.6, 1.0, 1.0));

    public static final Block ZOMBIE_WALL_HEAD = new Block(NamespaceID.from("minecraft:zombie_wall_head"), (short) 6550, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:zombie_head"), 0.6, 1.0, 1.0));

    public static final Block PLAYER_HEAD = new Block(NamespaceID.from("minecraft:player_head"), (short) 6554, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:player_head"), 0.6, 1.0, 1.0));

    public static final Block PLAYER_WALL_HEAD = new Block(NamespaceID.from("minecraft:player_wall_head"), (short) 6570, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:player_head"), 0.6, 1.0, 1.0));

    public static final Block CREEPER_HEAD = new Block(NamespaceID.from("minecraft:creeper_head"), (short) 6574, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:creeper_head"), 0.6, 1.0, 1.0));

    public static final Block CREEPER_WALL_HEAD = new Block(NamespaceID.from("minecraft:creeper_wall_head"), (short) 6590, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:creeper_head"), 0.6, 1.0, 1.0));

    public static final Block DRAGON_HEAD = new Block(NamespaceID.from("minecraft:dragon_head"), (short) 6594, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dragon_head"), 0.6, 1.0, 1.0));

    public static final Block DRAGON_WALL_HEAD = new Block(NamespaceID.from("minecraft:dragon_wall_head"), (short) 6610, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dragon_head"), 0.6, 1.0, 1.0));

    public static final Block ANVIL = new Block(NamespaceID.from("minecraft:anvil"), (short) 6614, new RawBlockData(1200.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:anvil"), 0.6, 1.0, 1.0));

    public static final Block CHIPPED_ANVIL = new Block(NamespaceID.from("minecraft:chipped_anvil"), (short) 6618, new RawBlockData(1200.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chipped_anvil"), 0.6, 1.0, 1.0));

    public static final Block DAMAGED_ANVIL = new Block(NamespaceID.from("minecraft:damaged_anvil"), (short) 6622, new RawBlockData(1200.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:damaged_anvil"), 0.6, 1.0, 1.0));

    public static final Block TRAPPED_CHEST = new Block(NamespaceID.from("minecraft:trapped_chest"), (short) 6627, new RawBlockData(2.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:trapped_chest"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_WEIGHTED_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:light_weighted_pressure_plate"), (short) 6650, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_weighted_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block HEAVY_WEIGHTED_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:heavy_weighted_pressure_plate"), (short) 6666, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:heavy_weighted_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block COMPARATOR = new Block(NamespaceID.from("minecraft:comparator"), (short) 6683, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:comparator"), 0.6, 1.0, 1.0));

    public static final Block DAYLIGHT_DETECTOR = new Block(NamespaceID.from("minecraft:daylight_detector"), (short) 6714, new RawBlockData(0.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:daylight_detector"), 0.6, 1.0, 1.0));

    public static final Block REDSTONE_BLOCK = new Block(NamespaceID.from("minecraft:redstone_block"), (short) 6730, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:redstone_block"), 0.6, 1.0, 1.0));

    public static final Block NETHER_QUARTZ_ORE = new Block(NamespaceID.from("minecraft:nether_quartz_ore"), (short) 6731, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:nether_quartz_ore"), 0.6, 1.0, 1.0));

    public static final Block HOPPER = new Block(NamespaceID.from("minecraft:hopper"), (short) 6732, new RawBlockData(4.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:hopper"), 0.6, 1.0, 1.0));

    public static final Block QUARTZ_BLOCK = new Block(NamespaceID.from("minecraft:quartz_block"), (short) 6742, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:quartz_block"), 0.6, 1.0, 1.0));

    public static final Block CHISELED_QUARTZ_BLOCK = new Block(NamespaceID.from("minecraft:chiseled_quartz_block"), (short) 6743, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chiseled_quartz_block"), 0.6, 1.0, 1.0));

    public static final Block QUARTZ_PILLAR = new Block(NamespaceID.from("minecraft:quartz_pillar"), (short) 6745, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:quartz_pillar"), 0.6, 1.0, 1.0));

    public static final Block QUARTZ_STAIRS = new Block(NamespaceID.from("minecraft:quartz_stairs"), (short) 6758, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:quartz_stairs"), 0.6, 1.0, 1.0));

    public static final Block ACTIVATOR_RAIL = new Block(NamespaceID.from("minecraft:activator_rail"), (short) 6833, new RawBlockData(0.7, () -> Registry.MATERIAL_REGISTRY.get("minecraft:activator_rail"), 0.6, 1.0, 1.0));

    public static final Block DROPPER = new Block(NamespaceID.from("minecraft:dropper"), (short) 6840, new RawBlockData(3.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dropper"), 0.6, 1.0, 1.0));

    public static final Block WHITE_TERRACOTTA = new Block(NamespaceID.from("minecraft:white_terracotta"), (short) 6851, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_terracotta"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_TERRACOTTA = new Block(NamespaceID.from("minecraft:orange_terracotta"), (short) 6852, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_terracotta"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_TERRACOTTA = new Block(NamespaceID.from("minecraft:magenta_terracotta"), (short) 6853, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_terracotta"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_TERRACOTTA = new Block(NamespaceID.from("minecraft:light_blue_terracotta"), (short) 6854, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_terracotta"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_TERRACOTTA = new Block(NamespaceID.from("minecraft:yellow_terracotta"), (short) 6855, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_terracotta"), 0.6, 1.0, 1.0));

    public static final Block LIME_TERRACOTTA = new Block(NamespaceID.from("minecraft:lime_terracotta"), (short) 6856, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_terracotta"), 0.6, 1.0, 1.0));

    public static final Block PINK_TERRACOTTA = new Block(NamespaceID.from("minecraft:pink_terracotta"), (short) 6857, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_terracotta"), 0.6, 1.0, 1.0));

    public static final Block GRAY_TERRACOTTA = new Block(NamespaceID.from("minecraft:gray_terracotta"), (short) 6858, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_terracotta"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_TERRACOTTA = new Block(NamespaceID.from("minecraft:light_gray_terracotta"), (short) 6859, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_terracotta"), 0.6, 1.0, 1.0));

    public static final Block CYAN_TERRACOTTA = new Block(NamespaceID.from("minecraft:cyan_terracotta"), (short) 6860, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_terracotta"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_TERRACOTTA = new Block(NamespaceID.from("minecraft:purple_terracotta"), (short) 6861, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_terracotta"), 0.6, 1.0, 1.0));

    public static final Block BLUE_TERRACOTTA = new Block(NamespaceID.from("minecraft:blue_terracotta"), (short) 6862, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_terracotta"), 0.6, 1.0, 1.0));

    public static final Block BROWN_TERRACOTTA = new Block(NamespaceID.from("minecraft:brown_terracotta"), (short) 6863, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_terracotta"), 0.6, 1.0, 1.0));

    public static final Block GREEN_TERRACOTTA = new Block(NamespaceID.from("minecraft:green_terracotta"), (short) 6864, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_terracotta"), 0.6, 1.0, 1.0));

    public static final Block RED_TERRACOTTA = new Block(NamespaceID.from("minecraft:red_terracotta"), (short) 6865, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_terracotta"), 0.6, 1.0, 1.0));

    public static final Block BLACK_TERRACOTTA = new Block(NamespaceID.from("minecraft:black_terracotta"), (short) 6866, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_terracotta"), 0.6, 1.0, 1.0));

    public static final Block WHITE_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:white_stained_glass_pane"), (short) 6898, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:orange_stained_glass_pane"), (short) 6930, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:magenta_stained_glass_pane"), (short) 6962, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:light_blue_stained_glass_pane"), (short) 6994, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:yellow_stained_glass_pane"), (short) 7026, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block LIME_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:lime_stained_glass_pane"), (short) 7058, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block PINK_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:pink_stained_glass_pane"), (short) 7090, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block GRAY_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:gray_stained_glass_pane"), (short) 7122, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:light_gray_stained_glass_pane"), (short) 7154, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block CYAN_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:cyan_stained_glass_pane"), (short) 7186, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:purple_stained_glass_pane"), (short) 7218, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block BLUE_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:blue_stained_glass_pane"), (short) 7250, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block BROWN_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:brown_stained_glass_pane"), (short) 7282, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block GREEN_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:green_stained_glass_pane"), (short) 7314, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block RED_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:red_stained_glass_pane"), (short) 7346, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block BLACK_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:black_stained_glass_pane"), (short) 7378, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_stained_glass_pane"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_STAIRS = new Block(NamespaceID.from("minecraft:acacia_stairs"), (short) 7390, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_stairs"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_STAIRS = new Block(NamespaceID.from("minecraft:dark_oak_stairs"), (short) 7470, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_stairs"), 0.6, 1.0, 1.0));

    public static final Block SLIME_BLOCK = new Block(NamespaceID.from("minecraft:slime_block"), (short) 7539, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:slime_block"), 0.8, 1.0, 1.0));

    public static final Block BARRIER = new Block(NamespaceID.from("minecraft:barrier"), (short) 7540, new RawBlockData(3600000.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:barrier"), 0.6, 1.0, 1.0));

    public static final Block IRON_TRAPDOOR = new Block(NamespaceID.from("minecraft:iron_trapdoor"), (short) 7556, new RawBlockData(5.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:iron_trapdoor"), 0.6, 1.0, 1.0));

    public static final Block PRISMARINE = new Block(NamespaceID.from("minecraft:prismarine"), (short) 7605, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:prismarine"), 0.6, 1.0, 1.0));

    public static final Block PRISMARINE_BRICKS = new Block(NamespaceID.from("minecraft:prismarine_bricks"), (short) 7606, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:prismarine_bricks"), 0.6, 1.0, 1.0));

    public static final Block DARK_PRISMARINE = new Block(NamespaceID.from("minecraft:dark_prismarine"), (short) 7607, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_prismarine"), 0.6, 1.0, 1.0));

    public static final Block PRISMARINE_STAIRS = new Block(NamespaceID.from("minecraft:prismarine_stairs"), (short) 7619, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:prismarine_stairs"), 0.6, 1.0, 1.0));

    public static final Block PRISMARINE_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:prismarine_brick_stairs"), (short) 7699, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:prismarine_brick_stairs"), 0.6, 1.0, 1.0));

    public static final Block DARK_PRISMARINE_STAIRS = new Block(NamespaceID.from("minecraft:dark_prismarine_stairs"), (short) 7779, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_prismarine_stairs"), 0.6, 1.0, 1.0));

    public static final Block PRISMARINE_SLAB = new Block(NamespaceID.from("minecraft:prismarine_slab"), (short) 7851, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:prismarine_slab"), 0.6, 1.0, 1.0));

    public static final Block PRISMARINE_BRICK_SLAB = new Block(NamespaceID.from("minecraft:prismarine_brick_slab"), (short) 7857, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:prismarine_brick_slab"), 0.6, 1.0, 1.0));

    public static final Block DARK_PRISMARINE_SLAB = new Block(NamespaceID.from("minecraft:dark_prismarine_slab"), (short) 7863, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_prismarine_slab"), 0.6, 1.0, 1.0));

    public static final Block SEA_LANTERN = new Block(NamespaceID.from("minecraft:sea_lantern"), (short) 7866, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sea_lantern"), 0.6, 1.0, 1.0));

    public static final Block HAY_BLOCK = new Block(NamespaceID.from("minecraft:hay_block"), (short) 7868, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:hay_block"), 0.6, 1.0, 1.0));

    public static final Block WHITE_CARPET = new Block(NamespaceID.from("minecraft:white_carpet"), (short) 7870, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_carpet"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_CARPET = new Block(NamespaceID.from("minecraft:orange_carpet"), (short) 7871, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_carpet"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_CARPET = new Block(NamespaceID.from("minecraft:magenta_carpet"), (short) 7872, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_carpet"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_CARPET = new Block(NamespaceID.from("minecraft:light_blue_carpet"), (short) 7873, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_carpet"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_CARPET = new Block(NamespaceID.from("minecraft:yellow_carpet"), (short) 7874, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_carpet"), 0.6, 1.0, 1.0));

    public static final Block LIME_CARPET = new Block(NamespaceID.from("minecraft:lime_carpet"), (short) 7875, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_carpet"), 0.6, 1.0, 1.0));

    public static final Block PINK_CARPET = new Block(NamespaceID.from("minecraft:pink_carpet"), (short) 7876, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_carpet"), 0.6, 1.0, 1.0));

    public static final Block GRAY_CARPET = new Block(NamespaceID.from("minecraft:gray_carpet"), (short) 7877, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_carpet"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_CARPET = new Block(NamespaceID.from("minecraft:light_gray_carpet"), (short) 7878, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_carpet"), 0.6, 1.0, 1.0));

    public static final Block CYAN_CARPET = new Block(NamespaceID.from("minecraft:cyan_carpet"), (short) 7879, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_carpet"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_CARPET = new Block(NamespaceID.from("minecraft:purple_carpet"), (short) 7880, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_carpet"), 0.6, 1.0, 1.0));

    public static final Block BLUE_CARPET = new Block(NamespaceID.from("minecraft:blue_carpet"), (short) 7881, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_carpet"), 0.6, 1.0, 1.0));

    public static final Block BROWN_CARPET = new Block(NamespaceID.from("minecraft:brown_carpet"), (short) 7882, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_carpet"), 0.6, 1.0, 1.0));

    public static final Block GREEN_CARPET = new Block(NamespaceID.from("minecraft:green_carpet"), (short) 7883, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_carpet"), 0.6, 1.0, 1.0));

    public static final Block RED_CARPET = new Block(NamespaceID.from("minecraft:red_carpet"), (short) 7884, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_carpet"), 0.6, 1.0, 1.0));

    public static final Block BLACK_CARPET = new Block(NamespaceID.from("minecraft:black_carpet"), (short) 7885, new RawBlockData(0.1, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_carpet"), 0.6, 1.0, 1.0));

    public static final Block TERRACOTTA = new Block(NamespaceID.from("minecraft:terracotta"), (short) 7886, new RawBlockData(4.2, () -> Registry.MATERIAL_REGISTRY.get("minecraft:terracotta"), 0.6, 1.0, 1.0));

    public static final Block COAL_BLOCK = new Block(NamespaceID.from("minecraft:coal_block"), (short) 7887, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:coal_block"), 0.6, 1.0, 1.0));

    public static final Block PACKED_ICE = new Block(NamespaceID.from("minecraft:packed_ice"), (short) 7888, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:packed_ice"), 0.98, 1.0, 1.0));

    public static final Block SUNFLOWER = new Block(NamespaceID.from("minecraft:sunflower"), (short) 7890, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sunflower"), 0.6, 1.0, 1.0));

    public static final Block LILAC = new Block(NamespaceID.from("minecraft:lilac"), (short) 7892, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lilac"), 0.6, 1.0, 1.0));

    public static final Block ROSE_BUSH = new Block(NamespaceID.from("minecraft:rose_bush"), (short) 7894, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:rose_bush"), 0.6, 1.0, 1.0));

    public static final Block PEONY = new Block(NamespaceID.from("minecraft:peony"), (short) 7896, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:peony"), 0.6, 1.0, 1.0));

    public static final Block TALL_GRASS = new Block(NamespaceID.from("minecraft:tall_grass"), (short) 7898, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:tall_grass"), 0.6, 1.0, 1.0));

    public static final Block LARGE_FERN = new Block(NamespaceID.from("minecraft:large_fern"), (short) 7900, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:large_fern"), 0.6, 1.0, 1.0));

    public static final Block WHITE_BANNER = new Block(NamespaceID.from("minecraft:white_banner"), (short) 7901, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_banner"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_BANNER = new Block(NamespaceID.from("minecraft:orange_banner"), (short) 7917, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_banner"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_BANNER = new Block(NamespaceID.from("minecraft:magenta_banner"), (short) 7933, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_banner"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_BANNER = new Block(NamespaceID.from("minecraft:light_blue_banner"), (short) 7949, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_banner"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_BANNER = new Block(NamespaceID.from("minecraft:yellow_banner"), (short) 7965, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_banner"), 0.6, 1.0, 1.0));

    public static final Block LIME_BANNER = new Block(NamespaceID.from("minecraft:lime_banner"), (short) 7981, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_banner"), 0.6, 1.0, 1.0));

    public static final Block PINK_BANNER = new Block(NamespaceID.from("minecraft:pink_banner"), (short) 7997, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_banner"), 0.6, 1.0, 1.0));

    public static final Block GRAY_BANNER = new Block(NamespaceID.from("minecraft:gray_banner"), (short) 8013, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_banner"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_BANNER = new Block(NamespaceID.from("minecraft:light_gray_banner"), (short) 8029, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_banner"), 0.6, 1.0, 1.0));

    public static final Block CYAN_BANNER = new Block(NamespaceID.from("minecraft:cyan_banner"), (short) 8045, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_banner"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_BANNER = new Block(NamespaceID.from("minecraft:purple_banner"), (short) 8061, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_banner"), 0.6, 1.0, 1.0));

    public static final Block BLUE_BANNER = new Block(NamespaceID.from("minecraft:blue_banner"), (short) 8077, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_banner"), 0.6, 1.0, 1.0));

    public static final Block BROWN_BANNER = new Block(NamespaceID.from("minecraft:brown_banner"), (short) 8093, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_banner"), 0.6, 1.0, 1.0));

    public static final Block GREEN_BANNER = new Block(NamespaceID.from("minecraft:green_banner"), (short) 8109, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_banner"), 0.6, 1.0, 1.0));

    public static final Block RED_BANNER = new Block(NamespaceID.from("minecraft:red_banner"), (short) 8125, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_banner"), 0.6, 1.0, 1.0));

    public static final Block BLACK_BANNER = new Block(NamespaceID.from("minecraft:black_banner"), (short) 8141, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_banner"), 0.6, 1.0, 1.0));

    public static final Block WHITE_WALL_BANNER = new Block(NamespaceID.from("minecraft:white_wall_banner"), (short) 8157, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_banner"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_WALL_BANNER = new Block(NamespaceID.from("minecraft:orange_wall_banner"), (short) 8161, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_banner"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_WALL_BANNER = new Block(NamespaceID.from("minecraft:magenta_wall_banner"), (short) 8165, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_banner"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_WALL_BANNER = new Block(NamespaceID.from("minecraft:light_blue_wall_banner"), (short) 8169, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_banner"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_WALL_BANNER = new Block(NamespaceID.from("minecraft:yellow_wall_banner"), (short) 8173, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_banner"), 0.6, 1.0, 1.0));

    public static final Block LIME_WALL_BANNER = new Block(NamespaceID.from("minecraft:lime_wall_banner"), (short) 8177, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_banner"), 0.6, 1.0, 1.0));

    public static final Block PINK_WALL_BANNER = new Block(NamespaceID.from("minecraft:pink_wall_banner"), (short) 8181, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_banner"), 0.6, 1.0, 1.0));

    public static final Block GRAY_WALL_BANNER = new Block(NamespaceID.from("minecraft:gray_wall_banner"), (short) 8185, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_banner"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_WALL_BANNER = new Block(NamespaceID.from("minecraft:light_gray_wall_banner"), (short) 8189, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_banner"), 0.6, 1.0, 1.0));

    public static final Block CYAN_WALL_BANNER = new Block(NamespaceID.from("minecraft:cyan_wall_banner"), (short) 8193, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_banner"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_WALL_BANNER = new Block(NamespaceID.from("minecraft:purple_wall_banner"), (short) 8197, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_banner"), 0.6, 1.0, 1.0));

    public static final Block BLUE_WALL_BANNER = new Block(NamespaceID.from("minecraft:blue_wall_banner"), (short) 8201, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_banner"), 0.6, 1.0, 1.0));

    public static final Block BROWN_WALL_BANNER = new Block(NamespaceID.from("minecraft:brown_wall_banner"), (short) 8205, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_banner"), 0.6, 1.0, 1.0));

    public static final Block GREEN_WALL_BANNER = new Block(NamespaceID.from("minecraft:green_wall_banner"), (short) 8209, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_banner"), 0.6, 1.0, 1.0));

    public static final Block RED_WALL_BANNER = new Block(NamespaceID.from("minecraft:red_wall_banner"), (short) 8213, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_banner"), 0.6, 1.0, 1.0));

    public static final Block BLACK_WALL_BANNER = new Block(NamespaceID.from("minecraft:black_wall_banner"), (short) 8217, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_banner"), 0.6, 1.0, 1.0));

    public static final Block RED_SANDSTONE = new Block(NamespaceID.from("minecraft:red_sandstone"), (short) 8221, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_sandstone"), 0.6, 1.0, 1.0));

    public static final Block CHISELED_RED_SANDSTONE = new Block(NamespaceID.from("minecraft:chiseled_red_sandstone"), (short) 8222, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chiseled_red_sandstone"), 0.6, 1.0, 1.0));

    public static final Block CUT_RED_SANDSTONE = new Block(NamespaceID.from("minecraft:cut_red_sandstone"), (short) 8223, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cut_red_sandstone"), 0.6, 1.0, 1.0));

    public static final Block RED_SANDSTONE_STAIRS = new Block(NamespaceID.from("minecraft:red_sandstone_stairs"), (short) 8235, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_sandstone_stairs"), 0.6, 1.0, 1.0));

    public static final Block OAK_SLAB = new Block(NamespaceID.from("minecraft:oak_slab"), (short) 8307, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:oak_slab"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_SLAB = new Block(NamespaceID.from("minecraft:spruce_slab"), (short) 8313, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_slab"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_SLAB = new Block(NamespaceID.from("minecraft:birch_slab"), (short) 8319, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_slab"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_SLAB = new Block(NamespaceID.from("minecraft:jungle_slab"), (short) 8325, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_slab"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_SLAB = new Block(NamespaceID.from("minecraft:acacia_slab"), (short) 8331, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_slab"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_SLAB = new Block(NamespaceID.from("minecraft:dark_oak_slab"), (short) 8337, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_slab"), 0.6, 1.0, 1.0));

    public static final Block STONE_SLAB = new Block(NamespaceID.from("minecraft:stone_slab"), (short) 8343, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stone_slab"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_STONE_SLAB = new Block(NamespaceID.from("minecraft:smooth_stone_slab"), (short) 8349, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_stone_slab"), 0.6, 1.0, 1.0));

    public static final Block SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:sandstone_slab"), (short) 8355, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sandstone_slab"), 0.6, 1.0, 1.0));

    public static final Block CUT_SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:cut_sandstone_slab"), (short) 8361, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cut_sandstone_slab"), 0.6, 1.0, 1.0));

    public static final Block PETRIFIED_OAK_SLAB = new Block(NamespaceID.from("minecraft:petrified_oak_slab"), (short) 8367, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:petrified_oak_slab"), 0.6, 1.0, 1.0));

    public static final Block COBBLESTONE_SLAB = new Block(NamespaceID.from("minecraft:cobblestone_slab"), (short) 8373, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cobblestone_slab"), 0.6, 1.0, 1.0));

    public static final Block BRICK_SLAB = new Block(NamespaceID.from("minecraft:brick_slab"), (short) 8379, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brick_slab"), 0.6, 1.0, 1.0));

    public static final Block STONE_BRICK_SLAB = new Block(NamespaceID.from("minecraft:stone_brick_slab"), (short) 8385, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stone_brick_slab"), 0.6, 1.0, 1.0));

    public static final Block NETHER_BRICK_SLAB = new Block(NamespaceID.from("minecraft:nether_brick_slab"), (short) 8391, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:nether_brick_slab"), 0.6, 1.0, 1.0));

    public static final Block QUARTZ_SLAB = new Block(NamespaceID.from("minecraft:quartz_slab"), (short) 8397, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:quartz_slab"), 0.6, 1.0, 1.0));

    public static final Block RED_SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:red_sandstone_slab"), (short) 8403, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_sandstone_slab"), 0.6, 1.0, 1.0));

    public static final Block CUT_RED_SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:cut_red_sandstone_slab"), (short) 8409, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cut_red_sandstone_slab"), 0.6, 1.0, 1.0));

    public static final Block PURPUR_SLAB = new Block(NamespaceID.from("minecraft:purpur_slab"), (short) 8415, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purpur_slab"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_STONE = new Block(NamespaceID.from("minecraft:smooth_stone"), (short) 8418, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_stone"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_SANDSTONE = new Block(NamespaceID.from("minecraft:smooth_sandstone"), (short) 8419, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_sandstone"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_QUARTZ = new Block(NamespaceID.from("minecraft:smooth_quartz"), (short) 8420, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_quartz"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_RED_SANDSTONE = new Block(NamespaceID.from("minecraft:smooth_red_sandstone"), (short) 8421, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_red_sandstone"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_FENCE_GATE = new Block(NamespaceID.from("minecraft:spruce_fence_gate"), (short) 8429, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_fence_gate"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_FENCE_GATE = new Block(NamespaceID.from("minecraft:birch_fence_gate"), (short) 8461, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_fence_gate"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_FENCE_GATE = new Block(NamespaceID.from("minecraft:jungle_fence_gate"), (short) 8493, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_fence_gate"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_FENCE_GATE = new Block(NamespaceID.from("minecraft:acacia_fence_gate"), (short) 8525, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_fence_gate"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_FENCE_GATE = new Block(NamespaceID.from("minecraft:dark_oak_fence_gate"), (short) 8557, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_fence_gate"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_FENCE = new Block(NamespaceID.from("minecraft:spruce_fence"), (short) 8613, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_fence"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_FENCE = new Block(NamespaceID.from("minecraft:birch_fence"), (short) 8645, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_fence"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_FENCE = new Block(NamespaceID.from("minecraft:jungle_fence"), (short) 8677, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_fence"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_FENCE = new Block(NamespaceID.from("minecraft:acacia_fence"), (short) 8709, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_fence"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_FENCE = new Block(NamespaceID.from("minecraft:dark_oak_fence"), (short) 8741, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_fence"), 0.6, 1.0, 1.0));

    public static final Block SPRUCE_DOOR = new Block(NamespaceID.from("minecraft:spruce_door"), (short) 8753, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:spruce_door"), 0.6, 1.0, 1.0));

    public static final Block BIRCH_DOOR = new Block(NamespaceID.from("minecraft:birch_door"), (short) 8817, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:birch_door"), 0.6, 1.0, 1.0));

    public static final Block JUNGLE_DOOR = new Block(NamespaceID.from("minecraft:jungle_door"), (short) 8881, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jungle_door"), 0.6, 1.0, 1.0));

    public static final Block ACACIA_DOOR = new Block(NamespaceID.from("minecraft:acacia_door"), (short) 8945, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:acacia_door"), 0.6, 1.0, 1.0));

    public static final Block DARK_OAK_DOOR = new Block(NamespaceID.from("minecraft:dark_oak_door"), (short) 9009, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dark_oak_door"), 0.6, 1.0, 1.0));

    public static final Block END_ROD = new Block(NamespaceID.from("minecraft:end_rod"), (short) 9066, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:end_rod"), 0.6, 1.0, 1.0));

    public static final Block CHORUS_PLANT = new Block(NamespaceID.from("minecraft:chorus_plant"), (short) 9131, new RawBlockData(0.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chorus_plant"), 0.6, 1.0, 1.0));

    public static final Block CHORUS_FLOWER = new Block(NamespaceID.from("minecraft:chorus_flower"), (short) 9132, new RawBlockData(0.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chorus_flower"), 0.6, 1.0, 1.0));

    public static final Block PURPUR_BLOCK = new Block(NamespaceID.from("minecraft:purpur_block"), (short) 9138, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purpur_block"), 0.6, 1.0, 1.0));

    public static final Block PURPUR_PILLAR = new Block(NamespaceID.from("minecraft:purpur_pillar"), (short) 9140, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purpur_pillar"), 0.6, 1.0, 1.0));

    public static final Block PURPUR_STAIRS = new Block(NamespaceID.from("minecraft:purpur_stairs"), (short) 9153, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purpur_stairs"), 0.6, 1.0, 1.0));

    public static final Block END_STONE_BRICKS = new Block(NamespaceID.from("minecraft:end_stone_bricks"), (short) 9222, new RawBlockData(9.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:end_stone_bricks"), 0.6, 1.0, 1.0));

    public static final Block BEETROOTS = new Block(NamespaceID.from("minecraft:beetroots"), (short) 9223, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:beetroot_seeds"), 0.6, 1.0, 1.0));

    public static final Block GRASS_PATH = new Block(NamespaceID.from("minecraft:grass_path"), (short) 9227, new RawBlockData(0.65, () -> Registry.MATERIAL_REGISTRY.get("minecraft:grass_path"), 0.6, 1.0, 1.0));

    public static final Block END_GATEWAY = new Block(NamespaceID.from("minecraft:end_gateway"), (short) 9228, new RawBlockData(3600000.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block REPEATING_COMMAND_BLOCK = new Block(NamespaceID.from("minecraft:repeating_command_block"), (short) 9235, new RawBlockData(3600000.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:repeating_command_block"), 0.6, 1.0, 1.0));

    public static final Block CHAIN_COMMAND_BLOCK = new Block(NamespaceID.from("minecraft:chain_command_block"), (short) 9247, new RawBlockData(3600000.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chain_command_block"), 0.6, 1.0, 1.0));

    public static final Block FROSTED_ICE = new Block(NamespaceID.from("minecraft:frosted_ice"), (short) 9253, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.98, 1.0, 1.0));

    public static final Block MAGMA_BLOCK = new Block(NamespaceID.from("minecraft:magma_block"), (short) 9257, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magma_block"), 0.6, 1.0, 1.0));

    public static final Block NETHER_WART_BLOCK = new Block(NamespaceID.from("minecraft:nether_wart_block"), (short) 9258, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:nether_wart_block"), 0.6, 1.0, 1.0));

    public static final Block RED_NETHER_BRICKS = new Block(NamespaceID.from("minecraft:red_nether_bricks"), (short) 9259, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_nether_bricks"), 0.6, 1.0, 1.0));

    public static final Block BONE_BLOCK = new Block(NamespaceID.from("minecraft:bone_block"), (short) 9261, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bone_block"), 0.6, 1.0, 1.0));

    public static final Block STRUCTURE_VOID = new Block(NamespaceID.from("minecraft:structure_void"), (short) 9263, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:structure_void"), 0.6, 1.0, 1.0));

    public static final Block OBSERVER = new Block(NamespaceID.from("minecraft:observer"), (short) 9269, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:observer"), 0.6, 1.0, 1.0));

    public static final Block SHULKER_BOX = new Block(NamespaceID.from("minecraft:shulker_box"), (short) 9280, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:shulker_box"), 0.6, 1.0, 1.0));

    public static final Block WHITE_SHULKER_BOX = new Block(NamespaceID.from("minecraft:white_shulker_box"), (short) 9286, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_SHULKER_BOX = new Block(NamespaceID.from("minecraft:orange_shulker_box"), (short) 9292, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_SHULKER_BOX = new Block(NamespaceID.from("minecraft:magenta_shulker_box"), (short) 9298, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_SHULKER_BOX = new Block(NamespaceID.from("minecraft:light_blue_shulker_box"), (short) 9304, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_SHULKER_BOX = new Block(NamespaceID.from("minecraft:yellow_shulker_box"), (short) 9310, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block LIME_SHULKER_BOX = new Block(NamespaceID.from("minecraft:lime_shulker_box"), (short) 9316, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block PINK_SHULKER_BOX = new Block(NamespaceID.from("minecraft:pink_shulker_box"), (short) 9322, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block GRAY_SHULKER_BOX = new Block(NamespaceID.from("minecraft:gray_shulker_box"), (short) 9328, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_SHULKER_BOX = new Block(NamespaceID.from("minecraft:light_gray_shulker_box"), (short) 9334, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block CYAN_SHULKER_BOX = new Block(NamespaceID.from("minecraft:cyan_shulker_box"), (short) 9340, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_SHULKER_BOX = new Block(NamespaceID.from("minecraft:purple_shulker_box"), (short) 9346, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block BLUE_SHULKER_BOX = new Block(NamespaceID.from("minecraft:blue_shulker_box"), (short) 9352, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block BROWN_SHULKER_BOX = new Block(NamespaceID.from("minecraft:brown_shulker_box"), (short) 9358, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block GREEN_SHULKER_BOX = new Block(NamespaceID.from("minecraft:green_shulker_box"), (short) 9364, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block RED_SHULKER_BOX = new Block(NamespaceID.from("minecraft:red_shulker_box"), (short) 9370, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block BLACK_SHULKER_BOX = new Block(NamespaceID.from("minecraft:black_shulker_box"), (short) 9376, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_shulker_box"), 0.6, 1.0, 1.0));

    public static final Block WHITE_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:white_glazed_terracotta"), (short) 9378, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:orange_glazed_terracotta"), (short) 9382, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:magenta_glazed_terracotta"), (short) 9386, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:light_blue_glazed_terracotta"), (short) 9390, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:yellow_glazed_terracotta"), (short) 9394, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block LIME_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:lime_glazed_terracotta"), (short) 9398, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block PINK_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:pink_glazed_terracotta"), (short) 9402, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block GRAY_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:gray_glazed_terracotta"), (short) 9406, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:light_gray_glazed_terracotta"), (short) 9410, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block CYAN_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:cyan_glazed_terracotta"), (short) 9414, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:purple_glazed_terracotta"), (short) 9418, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block BLUE_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:blue_glazed_terracotta"), (short) 9422, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block BROWN_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:brown_glazed_terracotta"), (short) 9426, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block GREEN_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:green_glazed_terracotta"), (short) 9430, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block RED_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:red_glazed_terracotta"), (short) 9434, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block BLACK_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:black_glazed_terracotta"), (short) 9438, new RawBlockData(1.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_glazed_terracotta"), 0.6, 1.0, 1.0));

    public static final Block WHITE_CONCRETE = new Block(NamespaceID.from("minecraft:white_concrete"), (short) 9442, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_concrete"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_CONCRETE = new Block(NamespaceID.from("minecraft:orange_concrete"), (short) 9443, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_concrete"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_CONCRETE = new Block(NamespaceID.from("minecraft:magenta_concrete"), (short) 9444, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_concrete"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_CONCRETE = new Block(NamespaceID.from("minecraft:light_blue_concrete"), (short) 9445, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_concrete"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_CONCRETE = new Block(NamespaceID.from("minecraft:yellow_concrete"), (short) 9446, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_concrete"), 0.6, 1.0, 1.0));

    public static final Block LIME_CONCRETE = new Block(NamespaceID.from("minecraft:lime_concrete"), (short) 9447, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_concrete"), 0.6, 1.0, 1.0));

    public static final Block PINK_CONCRETE = new Block(NamespaceID.from("minecraft:pink_concrete"), (short) 9448, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_concrete"), 0.6, 1.0, 1.0));

    public static final Block GRAY_CONCRETE = new Block(NamespaceID.from("minecraft:gray_concrete"), (short) 9449, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_concrete"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_CONCRETE = new Block(NamespaceID.from("minecraft:light_gray_concrete"), (short) 9450, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_concrete"), 0.6, 1.0, 1.0));

    public static final Block CYAN_CONCRETE = new Block(NamespaceID.from("minecraft:cyan_concrete"), (short) 9451, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_concrete"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_CONCRETE = new Block(NamespaceID.from("minecraft:purple_concrete"), (short) 9452, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_concrete"), 0.6, 1.0, 1.0));

    public static final Block BLUE_CONCRETE = new Block(NamespaceID.from("minecraft:blue_concrete"), (short) 9453, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_concrete"), 0.6, 1.0, 1.0));

    public static final Block BROWN_CONCRETE = new Block(NamespaceID.from("minecraft:brown_concrete"), (short) 9454, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_concrete"), 0.6, 1.0, 1.0));

    public static final Block GREEN_CONCRETE = new Block(NamespaceID.from("minecraft:green_concrete"), (short) 9455, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_concrete"), 0.6, 1.0, 1.0));

    public static final Block RED_CONCRETE = new Block(NamespaceID.from("minecraft:red_concrete"), (short) 9456, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_concrete"), 0.6, 1.0, 1.0));

    public static final Block BLACK_CONCRETE = new Block(NamespaceID.from("minecraft:black_concrete"), (short) 9457, new RawBlockData(1.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_concrete"), 0.6, 1.0, 1.0));

    public static final Block WHITE_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:white_concrete_powder"), (short) 9458, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:white_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block ORANGE_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:orange_concrete_powder"), (short) 9459, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:orange_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block MAGENTA_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:magenta_concrete_powder"), (short) 9460, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:magenta_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_BLUE_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:light_blue_concrete_powder"), (short) 9461, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_blue_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block YELLOW_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:yellow_concrete_powder"), (short) 9462, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:yellow_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block LIME_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:lime_concrete_powder"), (short) 9463, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lime_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block PINK_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:pink_concrete_powder"), (short) 9464, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:pink_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block GRAY_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:gray_concrete_powder"), (short) 9465, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gray_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block LIGHT_GRAY_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:light_gray_concrete_powder"), (short) 9466, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:light_gray_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block CYAN_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:cyan_concrete_powder"), (short) 9467, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cyan_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block PURPLE_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:purple_concrete_powder"), (short) 9468, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:purple_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block BLUE_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:blue_concrete_powder"), (short) 9469, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block BROWN_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:brown_concrete_powder"), (short) 9470, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brown_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block GREEN_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:green_concrete_powder"), (short) 9471, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:green_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block RED_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:red_concrete_powder"), (short) 9472, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block BLACK_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:black_concrete_powder"), (short) 9473, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:black_concrete_powder"), 0.6, 1.0, 1.0));

    public static final Block KELP = new Block(NamespaceID.from("minecraft:kelp"), (short) 9474, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:kelp"), 0.6, 1.0, 1.0));

    public static final Block KELP_PLANT = new Block(NamespaceID.from("minecraft:kelp_plant"), (short) 9500, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block DRIED_KELP_BLOCK = new Block(NamespaceID.from("minecraft:dried_kelp_block"), (short) 9501, new RawBlockData(2.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dried_kelp_block"), 0.6, 1.0, 1.0));

    public static final Block TURTLE_EGG = new Block(NamespaceID.from("minecraft:turtle_egg"), (short) 9502, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:turtle_egg"), 0.6, 1.0, 1.0));

    public static final Block DEAD_TUBE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:dead_tube_coral_block"), (short) 9514, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_tube_coral_block"), 0.6, 1.0, 1.0));

    public static final Block DEAD_BRAIN_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:dead_brain_coral_block"), (short) 9515, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_brain_coral_block"), 0.6, 1.0, 1.0));

    public static final Block DEAD_BUBBLE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:dead_bubble_coral_block"), (short) 9516, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_bubble_coral_block"), 0.6, 1.0, 1.0));

    public static final Block DEAD_FIRE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:dead_fire_coral_block"), (short) 9517, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_fire_coral_block"), 0.6, 1.0, 1.0));

    public static final Block DEAD_HORN_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:dead_horn_coral_block"), (short) 9518, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_horn_coral_block"), 0.6, 1.0, 1.0));

    public static final Block TUBE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:tube_coral_block"), (short) 9519, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:tube_coral_block"), 0.6, 1.0, 1.0));

    public static final Block BRAIN_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:brain_coral_block"), (short) 9520, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brain_coral_block"), 0.6, 1.0, 1.0));

    public static final Block BUBBLE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:bubble_coral_block"), (short) 9521, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bubble_coral_block"), 0.6, 1.0, 1.0));

    public static final Block FIRE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:fire_coral_block"), (short) 9522, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:fire_coral_block"), 0.6, 1.0, 1.0));

    public static final Block HORN_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:horn_coral_block"), (short) 9523, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:horn_coral_block"), 0.6, 1.0, 1.0));

    public static final Block DEAD_TUBE_CORAL = new Block(NamespaceID.from("minecraft:dead_tube_coral"), (short) 9524, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_tube_coral"), 0.6, 1.0, 1.0));

    public static final Block DEAD_BRAIN_CORAL = new Block(NamespaceID.from("minecraft:dead_brain_coral"), (short) 9526, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_brain_coral"), 0.6, 1.0, 1.0));

    public static final Block DEAD_BUBBLE_CORAL = new Block(NamespaceID.from("minecraft:dead_bubble_coral"), (short) 9528, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_bubble_coral"), 0.6, 1.0, 1.0));

    public static final Block DEAD_FIRE_CORAL = new Block(NamespaceID.from("minecraft:dead_fire_coral"), (short) 9530, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_fire_coral"), 0.6, 1.0, 1.0));

    public static final Block DEAD_HORN_CORAL = new Block(NamespaceID.from("minecraft:dead_horn_coral"), (short) 9532, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_horn_coral"), 0.6, 1.0, 1.0));

    public static final Block TUBE_CORAL = new Block(NamespaceID.from("minecraft:tube_coral"), (short) 9534, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:tube_coral"), 0.6, 1.0, 1.0));

    public static final Block BRAIN_CORAL = new Block(NamespaceID.from("minecraft:brain_coral"), (short) 9536, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brain_coral"), 0.6, 1.0, 1.0));

    public static final Block BUBBLE_CORAL = new Block(NamespaceID.from("minecraft:bubble_coral"), (short) 9538, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bubble_coral"), 0.6, 1.0, 1.0));

    public static final Block FIRE_CORAL = new Block(NamespaceID.from("minecraft:fire_coral"), (short) 9540, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:fire_coral"), 0.6, 1.0, 1.0));

    public static final Block HORN_CORAL = new Block(NamespaceID.from("minecraft:horn_coral"), (short) 9542, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:horn_coral"), 0.6, 1.0, 1.0));

    public static final Block DEAD_TUBE_CORAL_FAN = new Block(NamespaceID.from("minecraft:dead_tube_coral_fan"), (short) 9544, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_tube_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block DEAD_BRAIN_CORAL_FAN = new Block(NamespaceID.from("minecraft:dead_brain_coral_fan"), (short) 9546, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_brain_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block DEAD_BUBBLE_CORAL_FAN = new Block(NamespaceID.from("minecraft:dead_bubble_coral_fan"), (short) 9548, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_bubble_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block DEAD_FIRE_CORAL_FAN = new Block(NamespaceID.from("minecraft:dead_fire_coral_fan"), (short) 9550, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_fire_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block DEAD_HORN_CORAL_FAN = new Block(NamespaceID.from("minecraft:dead_horn_coral_fan"), (short) 9552, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_horn_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block TUBE_CORAL_FAN = new Block(NamespaceID.from("minecraft:tube_coral_fan"), (short) 9554, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:tube_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block BRAIN_CORAL_FAN = new Block(NamespaceID.from("minecraft:brain_coral_fan"), (short) 9556, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brain_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block BUBBLE_CORAL_FAN = new Block(NamespaceID.from("minecraft:bubble_coral_fan"), (short) 9558, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bubble_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block FIRE_CORAL_FAN = new Block(NamespaceID.from("minecraft:fire_coral_fan"), (short) 9560, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:fire_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block HORN_CORAL_FAN = new Block(NamespaceID.from("minecraft:horn_coral_fan"), (short) 9562, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:horn_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block DEAD_TUBE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:dead_tube_coral_wall_fan"), (short) 9564, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_tube_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block DEAD_BRAIN_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:dead_brain_coral_wall_fan"), (short) 9572, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_brain_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block DEAD_BUBBLE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:dead_bubble_coral_wall_fan"), (short) 9580, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_bubble_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block DEAD_FIRE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:dead_fire_coral_wall_fan"), (short) 9588, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_fire_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block DEAD_HORN_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:dead_horn_coral_wall_fan"), (short) 9596, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:dead_horn_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block TUBE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:tube_coral_wall_fan"), (short) 9604, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:tube_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block BRAIN_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:brain_coral_wall_fan"), (short) 9612, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brain_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block BUBBLE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:bubble_coral_wall_fan"), (short) 9620, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bubble_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block FIRE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:fire_coral_wall_fan"), (short) 9628, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:fire_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block HORN_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:horn_coral_wall_fan"), (short) 9636, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:horn_coral_fan"), 0.6, 1.0, 1.0));

    public static final Block SEA_PICKLE = new Block(NamespaceID.from("minecraft:sea_pickle"), (short) 9644, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sea_pickle"), 0.6, 1.0, 1.0));

    public static final Block BLUE_ICE = new Block(NamespaceID.from("minecraft:blue_ice"), (short) 9652, new RawBlockData(2.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blue_ice"), 0.989, 1.0, 1.0));

    public static final Block CONDUIT = new Block(NamespaceID.from("minecraft:conduit"), (short) 9653, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:conduit"), 0.6, 1.0, 1.0));

    public static final Block BAMBOO_SAPLING = new Block(NamespaceID.from("minecraft:bamboo_sapling"), (short) 9655, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block BAMBOO = new Block(NamespaceID.from("minecraft:bamboo"), (short) 9656, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bamboo"), 0.6, 1.0, 1.0));

    public static final Block POTTED_BAMBOO = new Block(NamespaceID.from("minecraft:potted_bamboo"), (short) 9668, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block VOID_AIR = new Block(NamespaceID.from("minecraft:void_air"), (short) 9669, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block CAVE_AIR = new Block(NamespaceID.from("minecraft:cave_air"), (short) 9670, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block BUBBLE_COLUMN = new Block(NamespaceID.from("minecraft:bubble_column"), (short) 9671, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_GRANITE_STAIRS = new Block(NamespaceID.from("minecraft:polished_granite_stairs"), (short) 9684, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_granite_stairs"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_RED_SANDSTONE_STAIRS = new Block(NamespaceID.from("minecraft:smooth_red_sandstone_stairs"), (short) 9764, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_red_sandstone_stairs"), 0.6, 1.0, 1.0));

    public static final Block MOSSY_STONE_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:mossy_stone_brick_stairs"), (short) 9844, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:mossy_stone_brick_stairs"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_DIORITE_STAIRS = new Block(NamespaceID.from("minecraft:polished_diorite_stairs"), (short) 9924, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_diorite_stairs"), 0.6, 1.0, 1.0));

    public static final Block MOSSY_COBBLESTONE_STAIRS = new Block(NamespaceID.from("minecraft:mossy_cobblestone_stairs"), (short) 10004, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:mossy_cobblestone_stairs"), 0.6, 1.0, 1.0));

    public static final Block END_STONE_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:end_stone_brick_stairs"), (short) 10084, new RawBlockData(9.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:end_stone_brick_stairs"), 0.6, 1.0, 1.0));

    public static final Block STONE_STAIRS = new Block(NamespaceID.from("minecraft:stone_stairs"), (short) 10164, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stone_stairs"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_SANDSTONE_STAIRS = new Block(NamespaceID.from("minecraft:smooth_sandstone_stairs"), (short) 10244, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_sandstone_stairs"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_QUARTZ_STAIRS = new Block(NamespaceID.from("minecraft:smooth_quartz_stairs"), (short) 10324, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_quartz_stairs"), 0.6, 1.0, 1.0));

    public static final Block GRANITE_STAIRS = new Block(NamespaceID.from("minecraft:granite_stairs"), (short) 10404, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:granite_stairs"), 0.6, 1.0, 1.0));

    public static final Block ANDESITE_STAIRS = new Block(NamespaceID.from("minecraft:andesite_stairs"), (short) 10484, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:andesite_stairs"), 0.6, 1.0, 1.0));

    public static final Block RED_NETHER_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:red_nether_brick_stairs"), (short) 10564, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_nether_brick_stairs"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_ANDESITE_STAIRS = new Block(NamespaceID.from("minecraft:polished_andesite_stairs"), (short) 10644, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_andesite_stairs"), 0.6, 1.0, 1.0));

    public static final Block DIORITE_STAIRS = new Block(NamespaceID.from("minecraft:diorite_stairs"), (short) 10724, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:diorite_stairs"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_GRANITE_SLAB = new Block(NamespaceID.from("minecraft:polished_granite_slab"), (short) 10796, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_granite_slab"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_RED_SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:smooth_red_sandstone_slab"), (short) 10802, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_red_sandstone_slab"), 0.6, 1.0, 1.0));

    public static final Block MOSSY_STONE_BRICK_SLAB = new Block(NamespaceID.from("minecraft:mossy_stone_brick_slab"), (short) 10808, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:mossy_stone_brick_slab"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_DIORITE_SLAB = new Block(NamespaceID.from("minecraft:polished_diorite_slab"), (short) 10814, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_diorite_slab"), 0.6, 1.0, 1.0));

    public static final Block MOSSY_COBBLESTONE_SLAB = new Block(NamespaceID.from("minecraft:mossy_cobblestone_slab"), (short) 10820, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:mossy_cobblestone_slab"), 0.6, 1.0, 1.0));

    public static final Block END_STONE_BRICK_SLAB = new Block(NamespaceID.from("minecraft:end_stone_brick_slab"), (short) 10826, new RawBlockData(9.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:end_stone_brick_slab"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:smooth_sandstone_slab"), (short) 10832, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_sandstone_slab"), 0.6, 1.0, 1.0));

    public static final Block SMOOTH_QUARTZ_SLAB = new Block(NamespaceID.from("minecraft:smooth_quartz_slab"), (short) 10838, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smooth_quartz_slab"), 0.6, 1.0, 1.0));

    public static final Block GRANITE_SLAB = new Block(NamespaceID.from("minecraft:granite_slab"), (short) 10844, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:granite_slab"), 0.6, 1.0, 1.0));

    public static final Block ANDESITE_SLAB = new Block(NamespaceID.from("minecraft:andesite_slab"), (short) 10850, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:andesite_slab"), 0.6, 1.0, 1.0));

    public static final Block RED_NETHER_BRICK_SLAB = new Block(NamespaceID.from("minecraft:red_nether_brick_slab"), (short) 10856, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_nether_brick_slab"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_ANDESITE_SLAB = new Block(NamespaceID.from("minecraft:polished_andesite_slab"), (short) 10862, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_andesite_slab"), 0.6, 1.0, 1.0));

    public static final Block DIORITE_SLAB = new Block(NamespaceID.from("minecraft:diorite_slab"), (short) 10868, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:diorite_slab"), 0.6, 1.0, 1.0));

    public static final Block BRICK_WALL = new Block(NamespaceID.from("minecraft:brick_wall"), (short) 10874, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:brick_wall"), 0.6, 1.0, 1.0));

    public static final Block PRISMARINE_WALL = new Block(NamespaceID.from("minecraft:prismarine_wall"), (short) 11198, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:prismarine_wall"), 0.6, 1.0, 1.0));

    public static final Block RED_SANDSTONE_WALL = new Block(NamespaceID.from("minecraft:red_sandstone_wall"), (short) 11522, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_sandstone_wall"), 0.6, 1.0, 1.0));

    public static final Block MOSSY_STONE_BRICK_WALL = new Block(NamespaceID.from("minecraft:mossy_stone_brick_wall"), (short) 11846, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:mossy_stone_brick_wall"), 0.6, 1.0, 1.0));

    public static final Block GRANITE_WALL = new Block(NamespaceID.from("minecraft:granite_wall"), (short) 12170, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:granite_wall"), 0.6, 1.0, 1.0));

    public static final Block STONE_BRICK_WALL = new Block(NamespaceID.from("minecraft:stone_brick_wall"), (short) 12494, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stone_brick_wall"), 0.6, 1.0, 1.0));

    public static final Block NETHER_BRICK_WALL = new Block(NamespaceID.from("minecraft:nether_brick_wall"), (short) 12818, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:nether_brick_wall"), 0.6, 1.0, 1.0));

    public static final Block ANDESITE_WALL = new Block(NamespaceID.from("minecraft:andesite_wall"), (short) 13142, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:andesite_wall"), 0.6, 1.0, 1.0));

    public static final Block RED_NETHER_BRICK_WALL = new Block(NamespaceID.from("minecraft:red_nether_brick_wall"), (short) 13466, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:red_nether_brick_wall"), 0.6, 1.0, 1.0));

    public static final Block SANDSTONE_WALL = new Block(NamespaceID.from("minecraft:sandstone_wall"), (short) 13790, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sandstone_wall"), 0.6, 1.0, 1.0));

    public static final Block END_STONE_BRICK_WALL = new Block(NamespaceID.from("minecraft:end_stone_brick_wall"), (short) 14114, new RawBlockData(9.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:end_stone_brick_wall"), 0.6, 1.0, 1.0));

    public static final Block DIORITE_WALL = new Block(NamespaceID.from("minecraft:diorite_wall"), (short) 14438, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:diorite_wall"), 0.6, 1.0, 1.0));

    public static final Block SCAFFOLDING = new Block(NamespaceID.from("minecraft:scaffolding"), (short) 14790, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:scaffolding"), 0.6, 1.0, 1.0));

    public static final Block LOOM = new Block(NamespaceID.from("minecraft:loom"), (short) 14791, new RawBlockData(2.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:loom"), 0.6, 1.0, 1.0));

    public static final Block BARREL = new Block(NamespaceID.from("minecraft:barrel"), (short) 14796, new RawBlockData(2.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:barrel"), 0.6, 1.0, 1.0));

    public static final Block SMOKER = new Block(NamespaceID.from("minecraft:smoker"), (short) 14808, new RawBlockData(3.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smoker"), 0.6, 1.0, 1.0));

    public static final Block BLAST_FURNACE = new Block(NamespaceID.from("minecraft:blast_furnace"), (short) 14816, new RawBlockData(3.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blast_furnace"), 0.6, 1.0, 1.0));

    public static final Block CARTOGRAPHY_TABLE = new Block(NamespaceID.from("minecraft:cartography_table"), (short) 14823, new RawBlockData(2.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cartography_table"), 0.6, 1.0, 1.0));

    public static final Block FLETCHING_TABLE = new Block(NamespaceID.from("minecraft:fletching_table"), (short) 14824, new RawBlockData(2.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:fletching_table"), 0.6, 1.0, 1.0));

    public static final Block GRINDSTONE = new Block(NamespaceID.from("minecraft:grindstone"), (short) 14829, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:grindstone"), 0.6, 1.0, 1.0));

    public static final Block LECTERN = new Block(NamespaceID.from("minecraft:lectern"), (short) 14840, new RawBlockData(2.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lectern"), 0.6, 1.0, 1.0));

    public static final Block SMITHING_TABLE = new Block(NamespaceID.from("minecraft:smithing_table"), (short) 14853, new RawBlockData(2.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:smithing_table"), 0.6, 1.0, 1.0));

    public static final Block STONECUTTER = new Block(NamespaceID.from("minecraft:stonecutter"), (short) 14854, new RawBlockData(3.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stonecutter"), 0.6, 1.0, 1.0));

    public static final Block BELL = new Block(NamespaceID.from("minecraft:bell"), (short) 14859, new RawBlockData(5.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bell"), 0.6, 1.0, 1.0));

    public static final Block LANTERN = new Block(NamespaceID.from("minecraft:lantern"), (short) 14893, new RawBlockData(3.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lantern"), 0.6, 1.0, 1.0));

    public static final Block SOUL_LANTERN = new Block(NamespaceID.from("minecraft:soul_lantern"), (short) 14897, new RawBlockData(3.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:soul_lantern"), 0.6, 1.0, 1.0));

    public static final Block CAMPFIRE = new Block(NamespaceID.from("minecraft:campfire"), (short) 14901, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:campfire"), 0.6, 1.0, 1.0));

    public static final Block SOUL_CAMPFIRE = new Block(NamespaceID.from("minecraft:soul_campfire"), (short) 14933, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:soul_campfire"), 0.6, 1.0, 1.0));

    public static final Block SWEET_BERRY_BUSH = new Block(NamespaceID.from("minecraft:sweet_berry_bush"), (short) 14962, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:sweet_berries"), 0.6, 1.0, 1.0));

    public static final Block WARPED_STEM = new Block(NamespaceID.from("minecraft:warped_stem"), (short) 14967, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_stem"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_WARPED_STEM = new Block(NamespaceID.from("minecraft:stripped_warped_stem"), (short) 14970, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_warped_stem"), 0.6, 1.0, 1.0));

    public static final Block WARPED_HYPHAE = new Block(NamespaceID.from("minecraft:warped_hyphae"), (short) 14973, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_hyphae"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_WARPED_HYPHAE = new Block(NamespaceID.from("minecraft:stripped_warped_hyphae"), (short) 14976, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_warped_hyphae"), 0.6, 1.0, 1.0));

    public static final Block WARPED_NYLIUM = new Block(NamespaceID.from("minecraft:warped_nylium"), (short) 14978, new RawBlockData(0.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_nylium"), 0.6, 1.0, 1.0));

    public static final Block WARPED_FUNGUS = new Block(NamespaceID.from("minecraft:warped_fungus"), (short) 14979, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_fungus"), 0.6, 1.0, 1.0));

    public static final Block WARPED_WART_BLOCK = new Block(NamespaceID.from("minecraft:warped_wart_block"), (short) 14980, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_wart_block"), 0.6, 1.0, 1.0));

    public static final Block WARPED_ROOTS = new Block(NamespaceID.from("minecraft:warped_roots"), (short) 14981, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_roots"), 0.6, 1.0, 1.0));

    public static final Block NETHER_SPROUTS = new Block(NamespaceID.from("minecraft:nether_sprouts"), (short) 14982, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:nether_sprouts"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_STEM = new Block(NamespaceID.from("minecraft:crimson_stem"), (short) 14984, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_stem"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_CRIMSON_STEM = new Block(NamespaceID.from("minecraft:stripped_crimson_stem"), (short) 14987, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_crimson_stem"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_HYPHAE = new Block(NamespaceID.from("minecraft:crimson_hyphae"), (short) 14990, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_hyphae"), 0.6, 1.0, 1.0));

    public static final Block STRIPPED_CRIMSON_HYPHAE = new Block(NamespaceID.from("minecraft:stripped_crimson_hyphae"), (short) 14993, new RawBlockData(2.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:stripped_crimson_hyphae"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_NYLIUM = new Block(NamespaceID.from("minecraft:crimson_nylium"), (short) 14995, new RawBlockData(0.4, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_nylium"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_FUNGUS = new Block(NamespaceID.from("minecraft:crimson_fungus"), (short) 14996, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_fungus"), 0.6, 1.0, 1.0));

    public static final Block SHROOMLIGHT = new Block(NamespaceID.from("minecraft:shroomlight"), (short) 14997, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:shroomlight"), 0.6, 1.0, 1.0));

    public static final Block WEEPING_VINES = new Block(NamespaceID.from("minecraft:weeping_vines"), (short) 14998, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:weeping_vines"), 0.6, 1.0, 1.0));

    public static final Block WEEPING_VINES_PLANT = new Block(NamespaceID.from("minecraft:weeping_vines_plant"), (short) 15024, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block TWISTING_VINES = new Block(NamespaceID.from("minecraft:twisting_vines"), (short) 15025, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:twisting_vines"), 0.6, 1.0, 1.0));

    public static final Block TWISTING_VINES_PLANT = new Block(NamespaceID.from("minecraft:twisting_vines_plant"), (short) 15051, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_ROOTS = new Block(NamespaceID.from("minecraft:crimson_roots"), (short) 15052, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_roots"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_PLANKS = new Block(NamespaceID.from("minecraft:crimson_planks"), (short) 15053, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_planks"), 0.6, 1.0, 1.0));

    public static final Block WARPED_PLANKS = new Block(NamespaceID.from("minecraft:warped_planks"), (short) 15054, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_planks"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_SLAB = new Block(NamespaceID.from("minecraft:crimson_slab"), (short) 15058, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_slab"), 0.6, 1.0, 1.0));

    public static final Block WARPED_SLAB = new Block(NamespaceID.from("minecraft:warped_slab"), (short) 15064, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_slab"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:crimson_pressure_plate"), (short) 15068, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block WARPED_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:warped_pressure_plate"), (short) 15070, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_FENCE = new Block(NamespaceID.from("minecraft:crimson_fence"), (short) 15102, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_fence"), 0.6, 1.0, 1.0));

    public static final Block WARPED_FENCE = new Block(NamespaceID.from("minecraft:warped_fence"), (short) 15134, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_fence"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_TRAPDOOR = new Block(NamespaceID.from("minecraft:crimson_trapdoor"), (short) 15150, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_trapdoor"), 0.6, 1.0, 1.0));

    public static final Block WARPED_TRAPDOOR = new Block(NamespaceID.from("minecraft:warped_trapdoor"), (short) 15214, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_trapdoor"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_FENCE_GATE = new Block(NamespaceID.from("minecraft:crimson_fence_gate"), (short) 15270, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_fence_gate"), 0.6, 1.0, 1.0));

    public static final Block WARPED_FENCE_GATE = new Block(NamespaceID.from("minecraft:warped_fence_gate"), (short) 15302, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_fence_gate"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_STAIRS = new Block(NamespaceID.from("minecraft:crimson_stairs"), (short) 15338, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_stairs"), 0.6, 1.0, 1.0));

    public static final Block WARPED_STAIRS = new Block(NamespaceID.from("minecraft:warped_stairs"), (short) 15418, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_stairs"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_BUTTON = new Block(NamespaceID.from("minecraft:crimson_button"), (short) 15496, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_button"), 0.6, 1.0, 1.0));

    public static final Block WARPED_BUTTON = new Block(NamespaceID.from("minecraft:warped_button"), (short) 15520, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_button"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_DOOR = new Block(NamespaceID.from("minecraft:crimson_door"), (short) 15546, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_door"), 0.6, 1.0, 1.0));

    public static final Block WARPED_DOOR = new Block(NamespaceID.from("minecraft:warped_door"), (short) 15610, new RawBlockData(3.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_door"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_SIGN = new Block(NamespaceID.from("minecraft:crimson_sign"), (short) 15664, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_sign"), 0.6, 1.0, 1.0));

    public static final Block WARPED_SIGN = new Block(NamespaceID.from("minecraft:warped_sign"), (short) 15696, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_sign"), 0.6, 1.0, 1.0));

    public static final Block CRIMSON_WALL_SIGN = new Block(NamespaceID.from("minecraft:crimson_wall_sign"), (short) 15728, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crimson_sign"), 0.6, 1.0, 1.0));

    public static final Block WARPED_WALL_SIGN = new Block(NamespaceID.from("minecraft:warped_wall_sign"), (short) 15736, new RawBlockData(1.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:warped_sign"), 0.6, 1.0, 1.0));

    public static final Block STRUCTURE_BLOCK = new Block(NamespaceID.from("minecraft:structure_block"), (short) 15743, new RawBlockData(3600000.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:structure_block"), 0.6, 1.0, 1.0));

    public static final Block JIGSAW = new Block(NamespaceID.from("minecraft:jigsaw"), (short) 15757, new RawBlockData(3600000.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:jigsaw"), 0.6, 1.0, 1.0));

    public static final Block COMPOSTER = new Block(NamespaceID.from("minecraft:composter"), (short) 15759, new RawBlockData(0.6, () -> Registry.MATERIAL_REGISTRY.get("minecraft:composter"), 0.6, 1.0, 1.0));

    public static final Block TARGET = new Block(NamespaceID.from("minecraft:target"), (short) 15768, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:target"), 0.6, 1.0, 1.0));

    public static final Block BEE_NEST = new Block(NamespaceID.from("minecraft:bee_nest"), (short) 15784, new RawBlockData(0.3, () -> Registry.MATERIAL_REGISTRY.get("minecraft:bee_nest"), 0.6, 1.0, 1.0));

    public static final Block BEEHIVE = new Block(NamespaceID.from("minecraft:beehive"), (short) 15808, new RawBlockData(0.6, () -> Registry.MATERIAL_REGISTRY.get("minecraft:beehive"), 0.6, 1.0, 1.0));

    public static final Block HONEY_BLOCK = new Block(NamespaceID.from("minecraft:honey_block"), (short) 15832, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:honey_block"), 0.6, 0.4, 0.5));

    public static final Block HONEYCOMB_BLOCK = new Block(NamespaceID.from("minecraft:honeycomb_block"), (short) 15833, new RawBlockData(0.6, () -> Registry.MATERIAL_REGISTRY.get("minecraft:honeycomb_block"), 0.6, 1.0, 1.0));

    public static final Block NETHERITE_BLOCK = new Block(NamespaceID.from("minecraft:netherite_block"), (short) 15834, new RawBlockData(1200.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:netherite_block"), 0.6, 1.0, 1.0));

    public static final Block ANCIENT_DEBRIS = new Block(NamespaceID.from("minecraft:ancient_debris"), (short) 15835, new RawBlockData(1200.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:ancient_debris"), 0.6, 1.0, 1.0));

    public static final Block CRYING_OBSIDIAN = new Block(NamespaceID.from("minecraft:crying_obsidian"), (short) 15836, new RawBlockData(1200.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:crying_obsidian"), 0.6, 1.0, 1.0));

    public static final Block RESPAWN_ANCHOR = new Block(NamespaceID.from("minecraft:respawn_anchor"), (short) 15837, new RawBlockData(1200.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:respawn_anchor"), 0.6, 1.0, 1.0));

    public static final Block POTTED_CRIMSON_FUNGUS = new Block(NamespaceID.from("minecraft:potted_crimson_fungus"), (short) 15842, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_WARPED_FUNGUS = new Block(NamespaceID.from("minecraft:potted_warped_fungus"), (short) 15843, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_CRIMSON_ROOTS = new Block(NamespaceID.from("minecraft:potted_crimson_roots"), (short) 15844, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block POTTED_WARPED_ROOTS = new Block(NamespaceID.from("minecraft:potted_warped_roots"), (short) 15845, new RawBlockData(0.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:air"), 0.6, 1.0, 1.0));

    public static final Block LODESTONE = new Block(NamespaceID.from("minecraft:lodestone"), (short) 15846, new RawBlockData(3.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:lodestone"), 0.6, 1.0, 1.0));

    public static final Block BLACKSTONE = new Block(NamespaceID.from("minecraft:blackstone"), (short) 15847, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blackstone"), 0.6, 1.0, 1.0));

    public static final Block BLACKSTONE_STAIRS = new Block(NamespaceID.from("minecraft:blackstone_stairs"), (short) 15859, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blackstone_stairs"), 0.6, 1.0, 1.0));

    public static final Block BLACKSTONE_WALL = new Block(NamespaceID.from("minecraft:blackstone_wall"), (short) 15931, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blackstone_wall"), 0.6, 1.0, 1.0));

    public static final Block BLACKSTONE_SLAB = new Block(NamespaceID.from("minecraft:blackstone_slab"), (short) 16255, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:blackstone_slab"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BLACKSTONE = new Block(NamespaceID.from("minecraft:polished_blackstone"), (short) 16258, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_blackstone"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BLACKSTONE_BRICKS = new Block(NamespaceID.from("minecraft:polished_blackstone_bricks"), (short) 16259, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_blackstone_bricks"), 0.6, 1.0, 1.0));

    public static final Block CRACKED_POLISHED_BLACKSTONE_BRICKS = new Block(NamespaceID.from("minecraft:cracked_polished_blackstone_bricks"), (short) 16260, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cracked_polished_blackstone_bricks"), 0.6, 1.0, 1.0));

    public static final Block CHISELED_POLISHED_BLACKSTONE = new Block(NamespaceID.from("minecraft:chiseled_polished_blackstone"), (short) 16261, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chiseled_polished_blackstone"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BLACKSTONE_BRICK_SLAB = new Block(NamespaceID.from("minecraft:polished_blackstone_brick_slab"), (short) 16265, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_blackstone_brick_slab"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BLACKSTONE_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:polished_blackstone_brick_stairs"), (short) 16279, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_blackstone_brick_stairs"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BLACKSTONE_BRICK_WALL = new Block(NamespaceID.from("minecraft:polished_blackstone_brick_wall"), (short) 16351, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_blackstone_brick_wall"), 0.6, 1.0, 1.0));

    public static final Block GILDED_BLACKSTONE = new Block(NamespaceID.from("minecraft:gilded_blackstone"), (short) 16672, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:gilded_blackstone"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BLACKSTONE_STAIRS = new Block(NamespaceID.from("minecraft:polished_blackstone_stairs"), (short) 16684, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_blackstone_stairs"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BLACKSTONE_SLAB = new Block(NamespaceID.from("minecraft:polished_blackstone_slab"), (short) 16756, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_blackstone_slab"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BLACKSTONE_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:polished_blackstone_pressure_plate"), (short) 16760, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_blackstone_pressure_plate"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BLACKSTONE_BUTTON = new Block(NamespaceID.from("minecraft:polished_blackstone_button"), (short) 16770, new RawBlockData(0.5, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_blackstone_button"), 0.6, 1.0, 1.0));

    public static final Block POLISHED_BLACKSTONE_WALL = new Block(NamespaceID.from("minecraft:polished_blackstone_wall"), (short) 16788, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:polished_blackstone_wall"), 0.6, 1.0, 1.0));

    public static final Block CHISELED_NETHER_BRICKS = new Block(NamespaceID.from("minecraft:chiseled_nether_bricks"), (short) 17109, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:chiseled_nether_bricks"), 0.6, 1.0, 1.0));

    public static final Block CRACKED_NETHER_BRICKS = new Block(NamespaceID.from("minecraft:cracked_nether_bricks"), (short) 17110, new RawBlockData(6.0, () -> Registry.MATERIAL_REGISTRY.get("minecraft:cracked_nether_bricks"), 0.6, 1.0, 1.0));

    public static final Block QUARTZ_BRICKS = new Block(NamespaceID.from("minecraft:quartz_bricks"), (short) 17111, new RawBlockData(0.8, () -> Registry.MATERIAL_REGISTRY.get("minecraft:quartz_bricks"), 0.6, 1.0, 1.0));

    static {
        Air.initStates();
        Stone.initStates();
        Granite.initStates();
        PolishedGranite.initStates();
        Diorite.initStates();
        PolishedDiorite.initStates();
        Andesite.initStates();
        PolishedAndesite.initStates();
        GrassBlock.initStates();
        Dirt.initStates();
        CoarseDirt.initStates();
        Podzol.initStates();
        Cobblestone.initStates();
        OakPlanks.initStates();
        SprucePlanks.initStates();
        BirchPlanks.initStates();
        JunglePlanks.initStates();
        AcaciaPlanks.initStates();
        DarkOakPlanks.initStates();
        OakSapling.initStates();
        SpruceSapling.initStates();
        BirchSapling.initStates();
        JungleSapling.initStates();
        AcaciaSapling.initStates();
        DarkOakSapling.initStates();
        Bedrock.initStates();
        Water.initStates();
        Lava.initStates();
        Sand.initStates();
        RedSand.initStates();
        Gravel.initStates();
        GoldOre.initStates();
        IronOre.initStates();
        CoalOre.initStates();
        NetherGoldOre.initStates();
        OakLog.initStates();
        SpruceLog.initStates();
        BirchLog.initStates();
        JungleLog.initStates();
        AcaciaLog.initStates();
        DarkOakLog.initStates();
        StrippedSpruceLog.initStates();
        StrippedBirchLog.initStates();
        StrippedJungleLog.initStates();
        StrippedAcaciaLog.initStates();
        StrippedDarkOakLog.initStates();
        StrippedOakLog.initStates();
        OakWood.initStates();
        SpruceWood.initStates();
        BirchWood.initStates();
        JungleWood.initStates();
        AcaciaWood.initStates();
        DarkOakWood.initStates();
        StrippedOakWood.initStates();
        StrippedSpruceWood.initStates();
        StrippedBirchWood.initStates();
        StrippedJungleWood.initStates();
        StrippedAcaciaWood.initStates();
        StrippedDarkOakWood.initStates();
        OakLeaves.initStates();
        SpruceLeaves.initStates();
        BirchLeaves.initStates();
        JungleLeaves.initStates();
        AcaciaLeaves.initStates();
        DarkOakLeaves.initStates();
        Sponge.initStates();
        WetSponge.initStates();
        Glass.initStates();
        LapisOre.initStates();
        LapisBlock.initStates();
        Dispenser.initStates();
        Sandstone.initStates();
        ChiseledSandstone.initStates();
        CutSandstone.initStates();
        NoteBlock.initStates();
        NoteBlock2.initStates();
        WhiteBed.initStates();
        OrangeBed.initStates();
        MagentaBed.initStates();
        LightBlueBed.initStates();
        YellowBed.initStates();
        LimeBed.initStates();
        PinkBed.initStates();
        GrayBed.initStates();
        LightGrayBed.initStates();
        CyanBed.initStates();
        PurpleBed.initStates();
        BlueBed.initStates();
        BrownBed.initStates();
        GreenBed.initStates();
        RedBed.initStates();
        BlackBed.initStates();
        PoweredRail.initStates();
        DetectorRail.initStates();
        StickyPiston.initStates();
        Cobweb.initStates();
        Grass.initStates();
        Fern.initStates();
        DeadBush.initStates();
        Seagrass.initStates();
        TallSeagrass.initStates();
        Piston.initStates();
        PistonHead.initStates();
        WhiteWool.initStates();
        OrangeWool.initStates();
        MagentaWool.initStates();
        LightBlueWool.initStates();
        YellowWool.initStates();
        LimeWool.initStates();
        PinkWool.initStates();
        GrayWool.initStates();
        LightGrayWool.initStates();
        CyanWool.initStates();
        PurpleWool.initStates();
        BlueWool.initStates();
        BrownWool.initStates();
        GreenWool.initStates();
        RedWool.initStates();
        BlackWool.initStates();
        MovingPiston.initStates();
        Dandelion.initStates();
        Poppy.initStates();
        BlueOrchid.initStates();
        Allium.initStates();
        AzureBluet.initStates();
        RedTulip.initStates();
        OrangeTulip.initStates();
        WhiteTulip.initStates();
        PinkTulip.initStates();
        OxeyeDaisy.initStates();
        Cornflower.initStates();
        WitherRose.initStates();
        LilyOfTheValley.initStates();
        BrownMushroom.initStates();
        RedMushroom.initStates();
        GoldBlock.initStates();
        IronBlock.initStates();
        Bricks.initStates();
        Tnt.initStates();
        Bookshelf.initStates();
        MossyCobblestone.initStates();
        Obsidian.initStates();
        Torch.initStates();
        WallTorch.initStates();
        Fire.initStates();
        SoulFire.initStates();
        Spawner.initStates();
        OakStairs.initStates();
        Chest.initStates();
        RedstoneWire.initStates();
        RedstoneWire2.initStates();
        RedstoneWire3.initStates();
        DiamondOre.initStates();
        DiamondBlock.initStates();
        CraftingTable.initStates();
        Wheat.initStates();
        Farmland.initStates();
        Furnace.initStates();
        OakSign.initStates();
        SpruceSign.initStates();
        BirchSign.initStates();
        AcaciaSign.initStates();
        JungleSign.initStates();
        DarkOakSign.initStates();
        OakDoor.initStates();
        Ladder.initStates();
        Rail.initStates();
        CobblestoneStairs.initStates();
        OakWallSign.initStates();
        SpruceWallSign.initStates();
        BirchWallSign.initStates();
        AcaciaWallSign.initStates();
        JungleWallSign.initStates();
        DarkOakWallSign.initStates();
        Lever.initStates();
        StonePressurePlate.initStates();
        IronDoor.initStates();
        OakPressurePlate.initStates();
        SprucePressurePlate.initStates();
        BirchPressurePlate.initStates();
        JunglePressurePlate.initStates();
        AcaciaPressurePlate.initStates();
        DarkOakPressurePlate.initStates();
        RedstoneOre.initStates();
        RedstoneTorch.initStates();
        RedstoneWallTorch.initStates();
        StoneButton.initStates();
        Snow.initStates();
        Ice.initStates();
        SnowBlock.initStates();
        Cactus.initStates();
        Clay.initStates();
        SugarCane.initStates();
        Jukebox.initStates();
        OakFence.initStates();
        Pumpkin.initStates();
        Netherrack.initStates();
        SoulSand.initStates();
        SoulSoil.initStates();
        Basalt.initStates();
        PolishedBasalt.initStates();
        SoulTorch.initStates();
        SoulWallTorch.initStates();
        Glowstone.initStates();
        NetherPortal.initStates();
        CarvedPumpkin.initStates();
        JackOLantern.initStates();
        Cake.initStates();
        Repeater.initStates();
        WhiteStainedGlass.initStates();
        OrangeStainedGlass.initStates();
        MagentaStainedGlass.initStates();
        LightBlueStainedGlass.initStates();
        YellowStainedGlass.initStates();
        LimeStainedGlass.initStates();
        PinkStainedGlass.initStates();
        GrayStainedGlass.initStates();
        LightGrayStainedGlass.initStates();
        CyanStainedGlass.initStates();
        PurpleStainedGlass.initStates();
        BlueStainedGlass.initStates();
        BrownStainedGlass.initStates();
        GreenStainedGlass.initStates();
        RedStainedGlass.initStates();
        BlackStainedGlass.initStates();
        OakTrapdoor.initStates();
        SpruceTrapdoor.initStates();
        BirchTrapdoor.initStates();
        JungleTrapdoor.initStates();
        AcaciaTrapdoor.initStates();
        DarkOakTrapdoor.initStates();
        StoneBricks.initStates();
        MossyStoneBricks.initStates();
        CrackedStoneBricks.initStates();
        ChiseledStoneBricks.initStates();
        InfestedStone.initStates();
        InfestedCobblestone.initStates();
        InfestedStoneBricks.initStates();
        InfestedMossyStoneBricks.initStates();
        InfestedCrackedStoneBricks.initStates();
        InfestedChiseledStoneBricks.initStates();
        BrownMushroomBlock.initStates();
        RedMushroomBlock.initStates();
        MushroomStem.initStates();
        IronBars.initStates();
        Chain.initStates();
        GlassPane.initStates();
        Melon.initStates();
        AttachedPumpkinStem.initStates();
        AttachedMelonStem.initStates();
        PumpkinStem.initStates();
        MelonStem.initStates();
        Vine.initStates();
        OakFenceGate.initStates();
        BrickStairs.initStates();
        StoneBrickStairs.initStates();
        Mycelium.initStates();
        LilyPad.initStates();
        NetherBricks.initStates();
        NetherBrickFence.initStates();
        NetherBrickStairs.initStates();
        NetherWart.initStates();
        EnchantingTable.initStates();
        BrewingStand.initStates();
        Cauldron.initStates();
        EndPortal.initStates();
        EndPortalFrame.initStates();
        EndStone.initStates();
        DragonEgg.initStates();
        RedstoneLamp.initStates();
        Cocoa.initStates();
        SandstoneStairs.initStates();
        EmeraldOre.initStates();
        EnderChest.initStates();
        TripwireHook.initStates();
        Tripwire.initStates();
        EmeraldBlock.initStates();
        SpruceStairs.initStates();
        BirchStairs.initStates();
        JungleStairs.initStates();
        CommandBlock.initStates();
        Beacon.initStates();
        CobblestoneWall.initStates();
        MossyCobblestoneWall.initStates();
        FlowerPot.initStates();
        PottedOakSapling.initStates();
        PottedSpruceSapling.initStates();
        PottedBirchSapling.initStates();
        PottedJungleSapling.initStates();
        PottedAcaciaSapling.initStates();
        PottedDarkOakSapling.initStates();
        PottedFern.initStates();
        PottedDandelion.initStates();
        PottedPoppy.initStates();
        PottedBlueOrchid.initStates();
        PottedAllium.initStates();
        PottedAzureBluet.initStates();
        PottedRedTulip.initStates();
        PottedOrangeTulip.initStates();
        PottedWhiteTulip.initStates();
        PottedPinkTulip.initStates();
        PottedOxeyeDaisy.initStates();
        PottedCornflower.initStates();
        PottedLilyOfTheValley.initStates();
        PottedWitherRose.initStates();
        PottedRedMushroom.initStates();
        PottedBrownMushroom.initStates();
        PottedDeadBush.initStates();
        PottedCactus.initStates();
        Carrots.initStates();
        Potatoes.initStates();
        OakButton.initStates();
        SpruceButton.initStates();
        BirchButton.initStates();
        JungleButton.initStates();
        AcaciaButton.initStates();
        DarkOakButton.initStates();
        SkeletonSkull.initStates();
        SkeletonWallSkull.initStates();
        WitherSkeletonSkull.initStates();
        WitherSkeletonWallSkull.initStates();
        ZombieHead.initStates();
        ZombieWallHead.initStates();
        PlayerHead.initStates();
        PlayerWallHead.initStates();
        CreeperHead.initStates();
        CreeperWallHead.initStates();
        DragonHead.initStates();
        DragonWallHead.initStates();
        Anvil.initStates();
        ChippedAnvil.initStates();
        DamagedAnvil.initStates();
        TrappedChest.initStates();
        LightWeightedPressurePlate.initStates();
        HeavyWeightedPressurePlate.initStates();
        Comparator.initStates();
        DaylightDetector.initStates();
        RedstoneBlock.initStates();
        NetherQuartzOre.initStates();
        Hopper.initStates();
        QuartzBlock.initStates();
        ChiseledQuartzBlock.initStates();
        QuartzPillar.initStates();
        QuartzStairs.initStates();
        ActivatorRail.initStates();
        Dropper.initStates();
        WhiteTerracotta.initStates();
        OrangeTerracotta.initStates();
        MagentaTerracotta.initStates();
        LightBlueTerracotta.initStates();
        YellowTerracotta.initStates();
        LimeTerracotta.initStates();
        PinkTerracotta.initStates();
        GrayTerracotta.initStates();
        LightGrayTerracotta.initStates();
        CyanTerracotta.initStates();
        PurpleTerracotta.initStates();
        BlueTerracotta.initStates();
        BrownTerracotta.initStates();
        GreenTerracotta.initStates();
        RedTerracotta.initStates();
        BlackTerracotta.initStates();
        WhiteStainedGlassPane.initStates();
        OrangeStainedGlassPane.initStates();
        MagentaStainedGlassPane.initStates();
        LightBlueStainedGlassPane.initStates();
        YellowStainedGlassPane.initStates();
        LimeStainedGlassPane.initStates();
        PinkStainedGlassPane.initStates();
        GrayStainedGlassPane.initStates();
        LightGrayStainedGlassPane.initStates();
        CyanStainedGlassPane.initStates();
        PurpleStainedGlassPane.initStates();
        BlueStainedGlassPane.initStates();
        BrownStainedGlassPane.initStates();
        GreenStainedGlassPane.initStates();
        RedStainedGlassPane.initStates();
        BlackStainedGlassPane.initStates();
        AcaciaStairs.initStates();
        DarkOakStairs.initStates();
        SlimeBlock.initStates();
        Barrier.initStates();
        IronTrapdoor.initStates();
        Prismarine.initStates();
        PrismarineBricks.initStates();
        DarkPrismarine.initStates();
        PrismarineStairs.initStates();
        PrismarineBrickStairs.initStates();
        DarkPrismarineStairs.initStates();
        PrismarineSlab.initStates();
        PrismarineBrickSlab.initStates();
        DarkPrismarineSlab.initStates();
        SeaLantern.initStates();
        HayBlock.initStates();
        WhiteCarpet.initStates();
        OrangeCarpet.initStates();
        MagentaCarpet.initStates();
        LightBlueCarpet.initStates();
        YellowCarpet.initStates();
        LimeCarpet.initStates();
        PinkCarpet.initStates();
        GrayCarpet.initStates();
        LightGrayCarpet.initStates();
        CyanCarpet.initStates();
        PurpleCarpet.initStates();
        BlueCarpet.initStates();
        BrownCarpet.initStates();
        GreenCarpet.initStates();
        RedCarpet.initStates();
        BlackCarpet.initStates();
        Terracotta.initStates();
        CoalBlock.initStates();
        PackedIce.initStates();
        Sunflower.initStates();
        Lilac.initStates();
        RoseBush.initStates();
        Peony.initStates();
        TallGrass.initStates();
        LargeFern.initStates();
        WhiteBanner.initStates();
        OrangeBanner.initStates();
        MagentaBanner.initStates();
        LightBlueBanner.initStates();
        YellowBanner.initStates();
        LimeBanner.initStates();
        PinkBanner.initStates();
        GrayBanner.initStates();
        LightGrayBanner.initStates();
        CyanBanner.initStates();
        PurpleBanner.initStates();
        BlueBanner.initStates();
        BrownBanner.initStates();
        GreenBanner.initStates();
        RedBanner.initStates();
        BlackBanner.initStates();
        WhiteWallBanner.initStates();
        OrangeWallBanner.initStates();
        MagentaWallBanner.initStates();
        LightBlueWallBanner.initStates();
        YellowWallBanner.initStates();
        LimeWallBanner.initStates();
        PinkWallBanner.initStates();
        GrayWallBanner.initStates();
        LightGrayWallBanner.initStates();
        CyanWallBanner.initStates();
        PurpleWallBanner.initStates();
        BlueWallBanner.initStates();
        BrownWallBanner.initStates();
        GreenWallBanner.initStates();
        RedWallBanner.initStates();
        BlackWallBanner.initStates();
        RedSandstone.initStates();
        ChiseledRedSandstone.initStates();
        CutRedSandstone.initStates();
        RedSandstoneStairs.initStates();
        OakSlab.initStates();
        SpruceSlab.initStates();
        BirchSlab.initStates();
        JungleSlab.initStates();
        AcaciaSlab.initStates();
        DarkOakSlab.initStates();
        StoneSlab.initStates();
        SmoothStoneSlab.initStates();
        SandstoneSlab.initStates();
        CutSandstoneSlab.initStates();
        PetrifiedOakSlab.initStates();
        CobblestoneSlab.initStates();
        BrickSlab.initStates();
        StoneBrickSlab.initStates();
        NetherBrickSlab.initStates();
        QuartzSlab.initStates();
        RedSandstoneSlab.initStates();
        CutRedSandstoneSlab.initStates();
        PurpurSlab.initStates();
        SmoothStone.initStates();
        SmoothSandstone.initStates();
        SmoothQuartz.initStates();
        SmoothRedSandstone.initStates();
        SpruceFenceGate.initStates();
        BirchFenceGate.initStates();
        JungleFenceGate.initStates();
        AcaciaFenceGate.initStates();
        DarkOakFenceGate.initStates();
        SpruceFence.initStates();
        BirchFence.initStates();
        JungleFence.initStates();
        AcaciaFence.initStates();
        DarkOakFence.initStates();
        SpruceDoor.initStates();
        BirchDoor.initStates();
        JungleDoor.initStates();
        AcaciaDoor.initStates();
        DarkOakDoor.initStates();
        EndRod.initStates();
        ChorusPlant.initStates();
        ChorusFlower.initStates();
        PurpurBlock.initStates();
        PurpurPillar.initStates();
        PurpurStairs.initStates();
        EndStoneBricks.initStates();
        Beetroots.initStates();
        GrassPath.initStates();
        EndGateway.initStates();
        RepeatingCommandBlock.initStates();
        ChainCommandBlock.initStates();
        FrostedIce.initStates();
        MagmaBlock.initStates();
        NetherWartBlock.initStates();
        RedNetherBricks.initStates();
        BoneBlock.initStates();
        StructureVoid.initStates();
        Observer.initStates();
        ShulkerBox.initStates();
        WhiteShulkerBox.initStates();
        OrangeShulkerBox.initStates();
        MagentaShulkerBox.initStates();
        LightBlueShulkerBox.initStates();
        YellowShulkerBox.initStates();
        LimeShulkerBox.initStates();
        PinkShulkerBox.initStates();
        GrayShulkerBox.initStates();
        LightGrayShulkerBox.initStates();
        CyanShulkerBox.initStates();
        PurpleShulkerBox.initStates();
        BlueShulkerBox.initStates();
        BrownShulkerBox.initStates();
        GreenShulkerBox.initStates();
        RedShulkerBox.initStates();
        BlackShulkerBox.initStates();
        WhiteGlazedTerracotta.initStates();
        OrangeGlazedTerracotta.initStates();
        MagentaGlazedTerracotta.initStates();
        LightBlueGlazedTerracotta.initStates();
        YellowGlazedTerracotta.initStates();
        LimeGlazedTerracotta.initStates();
        PinkGlazedTerracotta.initStates();
        GrayGlazedTerracotta.initStates();
        LightGrayGlazedTerracotta.initStates();
        CyanGlazedTerracotta.initStates();
        PurpleGlazedTerracotta.initStates();
        BlueGlazedTerracotta.initStates();
        BrownGlazedTerracotta.initStates();
        GreenGlazedTerracotta.initStates();
        RedGlazedTerracotta.initStates();
        BlackGlazedTerracotta.initStates();
        WhiteConcrete.initStates();
        OrangeConcrete.initStates();
        MagentaConcrete.initStates();
        LightBlueConcrete.initStates();
        YellowConcrete.initStates();
        LimeConcrete.initStates();
        PinkConcrete.initStates();
        GrayConcrete.initStates();
        LightGrayConcrete.initStates();
        CyanConcrete.initStates();
        PurpleConcrete.initStates();
        BlueConcrete.initStates();
        BrownConcrete.initStates();
        GreenConcrete.initStates();
        RedConcrete.initStates();
        BlackConcrete.initStates();
        WhiteConcretePowder.initStates();
        OrangeConcretePowder.initStates();
        MagentaConcretePowder.initStates();
        LightBlueConcretePowder.initStates();
        YellowConcretePowder.initStates();
        LimeConcretePowder.initStates();
        PinkConcretePowder.initStates();
        GrayConcretePowder.initStates();
        LightGrayConcretePowder.initStates();
        CyanConcretePowder.initStates();
        PurpleConcretePowder.initStates();
        BlueConcretePowder.initStates();
        BrownConcretePowder.initStates();
        GreenConcretePowder.initStates();
        RedConcretePowder.initStates();
        BlackConcretePowder.initStates();
        Kelp.initStates();
        KelpPlant.initStates();
        DriedKelpBlock.initStates();
        TurtleEgg.initStates();
        DeadTubeCoralBlock.initStates();
        DeadBrainCoralBlock.initStates();
        DeadBubbleCoralBlock.initStates();
        DeadFireCoralBlock.initStates();
        DeadHornCoralBlock.initStates();
        TubeCoralBlock.initStates();
        BrainCoralBlock.initStates();
        BubbleCoralBlock.initStates();
        FireCoralBlock.initStates();
        HornCoralBlock.initStates();
        DeadTubeCoral.initStates();
        DeadBrainCoral.initStates();
        DeadBubbleCoral.initStates();
        DeadFireCoral.initStates();
        DeadHornCoral.initStates();
        TubeCoral.initStates();
        BrainCoral.initStates();
        BubbleCoral.initStates();
        FireCoral.initStates();
        HornCoral.initStates();
        DeadTubeCoralFan.initStates();
        DeadBrainCoralFan.initStates();
        DeadBubbleCoralFan.initStates();
        DeadFireCoralFan.initStates();
        DeadHornCoralFan.initStates();
        TubeCoralFan.initStates();
        BrainCoralFan.initStates();
        BubbleCoralFan.initStates();
        FireCoralFan.initStates();
        HornCoralFan.initStates();
        DeadTubeCoralWallFan.initStates();
        DeadBrainCoralWallFan.initStates();
        DeadBubbleCoralWallFan.initStates();
        DeadFireCoralWallFan.initStates();
        DeadHornCoralWallFan.initStates();
        TubeCoralWallFan.initStates();
        BrainCoralWallFan.initStates();
        BubbleCoralWallFan.initStates();
        FireCoralWallFan.initStates();
        HornCoralWallFan.initStates();
        SeaPickle.initStates();
        BlueIce.initStates();
        Conduit.initStates();
        BambooSapling.initStates();
        Bamboo.initStates();
        PottedBamboo.initStates();
        VoidAir.initStates();
        CaveAir.initStates();
        BubbleColumn.initStates();
        PolishedGraniteStairs.initStates();
        SmoothRedSandstoneStairs.initStates();
        MossyStoneBrickStairs.initStates();
        PolishedDioriteStairs.initStates();
        MossyCobblestoneStairs.initStates();
        EndStoneBrickStairs.initStates();
        StoneStairs.initStates();
        SmoothSandstoneStairs.initStates();
        SmoothQuartzStairs.initStates();
        GraniteStairs.initStates();
        AndesiteStairs.initStates();
        RedNetherBrickStairs.initStates();
        PolishedAndesiteStairs.initStates();
        DioriteStairs.initStates();
        PolishedGraniteSlab.initStates();
        SmoothRedSandstoneSlab.initStates();
        MossyStoneBrickSlab.initStates();
        PolishedDioriteSlab.initStates();
        MossyCobblestoneSlab.initStates();
        EndStoneBrickSlab.initStates();
        SmoothSandstoneSlab.initStates();
        SmoothQuartzSlab.initStates();
        GraniteSlab.initStates();
        AndesiteSlab.initStates();
        RedNetherBrickSlab.initStates();
        PolishedAndesiteSlab.initStates();
        DioriteSlab.initStates();
        BrickWall.initStates();
        PrismarineWall.initStates();
        RedSandstoneWall.initStates();
        MossyStoneBrickWall.initStates();
        GraniteWall.initStates();
        StoneBrickWall.initStates();
        NetherBrickWall.initStates();
        AndesiteWall.initStates();
        RedNetherBrickWall.initStates();
        SandstoneWall.initStates();
        EndStoneBrickWall.initStates();
        DioriteWall.initStates();
        Scaffolding.initStates();
        Loom.initStates();
        Barrel.initStates();
        Smoker.initStates();
        BlastFurnace.initStates();
        CartographyTable.initStates();
        FletchingTable.initStates();
        Grindstone.initStates();
        Lectern.initStates();
        SmithingTable.initStates();
        Stonecutter.initStates();
        Bell.initStates();
        Lantern.initStates();
        SoulLantern.initStates();
        Campfire.initStates();
        SoulCampfire.initStates();
        SweetBerryBush.initStates();
        WarpedStem.initStates();
        StrippedWarpedStem.initStates();
        WarpedHyphae.initStates();
        StrippedWarpedHyphae.initStates();
        WarpedNylium.initStates();
        WarpedFungus.initStates();
        WarpedWartBlock.initStates();
        WarpedRoots.initStates();
        NetherSprouts.initStates();
        CrimsonStem.initStates();
        StrippedCrimsonStem.initStates();
        CrimsonHyphae.initStates();
        StrippedCrimsonHyphae.initStates();
        CrimsonNylium.initStates();
        CrimsonFungus.initStates();
        Shroomlight.initStates();
        WeepingVines.initStates();
        WeepingVinesPlant.initStates();
        TwistingVines.initStates();
        TwistingVinesPlant.initStates();
        CrimsonRoots.initStates();
        CrimsonPlanks.initStates();
        WarpedPlanks.initStates();
        CrimsonSlab.initStates();
        WarpedSlab.initStates();
        CrimsonPressurePlate.initStates();
        WarpedPressurePlate.initStates();
        CrimsonFence.initStates();
        WarpedFence.initStates();
        CrimsonTrapdoor.initStates();
        WarpedTrapdoor.initStates();
        CrimsonFenceGate.initStates();
        WarpedFenceGate.initStates();
        CrimsonStairs.initStates();
        WarpedStairs.initStates();
        CrimsonButton.initStates();
        WarpedButton.initStates();
        CrimsonDoor.initStates();
        WarpedDoor.initStates();
        CrimsonSign.initStates();
        WarpedSign.initStates();
        CrimsonWallSign.initStates();
        WarpedWallSign.initStates();
        StructureBlock.initStates();
        Jigsaw.initStates();
        Composter.initStates();
        Target.initStates();
        BeeNest.initStates();
        Beehive.initStates();
        HoneyBlock.initStates();
        HoneycombBlock.initStates();
        NetheriteBlock.initStates();
        AncientDebris.initStates();
        CryingObsidian.initStates();
        RespawnAnchor.initStates();
        PottedCrimsonFungus.initStates();
        PottedWarpedFungus.initStates();
        PottedCrimsonRoots.initStates();
        PottedWarpedRoots.initStates();
        Lodestone.initStates();
        Blackstone.initStates();
        BlackstoneStairs.initStates();
        BlackstoneWall.initStates();
        BlackstoneSlab.initStates();
        PolishedBlackstone.initStates();
        PolishedBlackstoneBricks.initStates();
        CrackedPolishedBlackstoneBricks.initStates();
        ChiseledPolishedBlackstone.initStates();
        PolishedBlackstoneBrickSlab.initStates();
        PolishedBlackstoneBrickStairs.initStates();
        PolishedBlackstoneBrickWall.initStates();
        GildedBlackstone.initStates();
        PolishedBlackstoneStairs.initStates();
        PolishedBlackstoneSlab.initStates();
        PolishedBlackstonePressurePlate.initStates();
        PolishedBlackstoneButton.initStates();
        PolishedBlackstoneWall.initStates();
        ChiseledNetherBricks.initStates();
        CrackedNetherBricks.initStates();
        QuartzBricks.initStates();
    }
    static {
        Registry.BLOCK_REGISTRY.register(AIR);
        Registry.BLOCK_REGISTRY.register(STONE);
        Registry.BLOCK_REGISTRY.register(GRANITE);
        Registry.BLOCK_REGISTRY.register(POLISHED_GRANITE);
        Registry.BLOCK_REGISTRY.register(DIORITE);
        Registry.BLOCK_REGISTRY.register(POLISHED_DIORITE);
        Registry.BLOCK_REGISTRY.register(ANDESITE);
        Registry.BLOCK_REGISTRY.register(POLISHED_ANDESITE);
        Registry.BLOCK_REGISTRY.register(GRASS_BLOCK);
        Registry.BLOCK_REGISTRY.register(DIRT);
        Registry.BLOCK_REGISTRY.register(COARSE_DIRT);
        Registry.BLOCK_REGISTRY.register(PODZOL);
        Registry.BLOCK_REGISTRY.register(COBBLESTONE);
        Registry.BLOCK_REGISTRY.register(OAK_PLANKS);
        Registry.BLOCK_REGISTRY.register(SPRUCE_PLANKS);
        Registry.BLOCK_REGISTRY.register(BIRCH_PLANKS);
        Registry.BLOCK_REGISTRY.register(JUNGLE_PLANKS);
        Registry.BLOCK_REGISTRY.register(ACACIA_PLANKS);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_PLANKS);
        Registry.BLOCK_REGISTRY.register(OAK_SAPLING);
        Registry.BLOCK_REGISTRY.register(SPRUCE_SAPLING);
        Registry.BLOCK_REGISTRY.register(BIRCH_SAPLING);
        Registry.BLOCK_REGISTRY.register(JUNGLE_SAPLING);
        Registry.BLOCK_REGISTRY.register(ACACIA_SAPLING);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_SAPLING);
        Registry.BLOCK_REGISTRY.register(BEDROCK);
        Registry.BLOCK_REGISTRY.register(WATER);
        Registry.BLOCK_REGISTRY.register(LAVA);
        Registry.BLOCK_REGISTRY.register(SAND);
        Registry.BLOCK_REGISTRY.register(RED_SAND);
        Registry.BLOCK_REGISTRY.register(GRAVEL);
        Registry.BLOCK_REGISTRY.register(GOLD_ORE);
        Registry.BLOCK_REGISTRY.register(IRON_ORE);
        Registry.BLOCK_REGISTRY.register(COAL_ORE);
        Registry.BLOCK_REGISTRY.register(NETHER_GOLD_ORE);
        Registry.BLOCK_REGISTRY.register(OAK_LOG);
        Registry.BLOCK_REGISTRY.register(SPRUCE_LOG);
        Registry.BLOCK_REGISTRY.register(BIRCH_LOG);
        Registry.BLOCK_REGISTRY.register(JUNGLE_LOG);
        Registry.BLOCK_REGISTRY.register(ACACIA_LOG);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_LOG);
        Registry.BLOCK_REGISTRY.register(STRIPPED_SPRUCE_LOG);
        Registry.BLOCK_REGISTRY.register(STRIPPED_BIRCH_LOG);
        Registry.BLOCK_REGISTRY.register(STRIPPED_JUNGLE_LOG);
        Registry.BLOCK_REGISTRY.register(STRIPPED_ACACIA_LOG);
        Registry.BLOCK_REGISTRY.register(STRIPPED_DARK_OAK_LOG);
        Registry.BLOCK_REGISTRY.register(STRIPPED_OAK_LOG);
        Registry.BLOCK_REGISTRY.register(OAK_WOOD);
        Registry.BLOCK_REGISTRY.register(SPRUCE_WOOD);
        Registry.BLOCK_REGISTRY.register(BIRCH_WOOD);
        Registry.BLOCK_REGISTRY.register(JUNGLE_WOOD);
        Registry.BLOCK_REGISTRY.register(ACACIA_WOOD);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_WOOD);
        Registry.BLOCK_REGISTRY.register(STRIPPED_OAK_WOOD);
        Registry.BLOCK_REGISTRY.register(STRIPPED_SPRUCE_WOOD);
        Registry.BLOCK_REGISTRY.register(STRIPPED_BIRCH_WOOD);
        Registry.BLOCK_REGISTRY.register(STRIPPED_JUNGLE_WOOD);
        Registry.BLOCK_REGISTRY.register(STRIPPED_ACACIA_WOOD);
        Registry.BLOCK_REGISTRY.register(STRIPPED_DARK_OAK_WOOD);
        Registry.BLOCK_REGISTRY.register(OAK_LEAVES);
        Registry.BLOCK_REGISTRY.register(SPRUCE_LEAVES);
        Registry.BLOCK_REGISTRY.register(BIRCH_LEAVES);
        Registry.BLOCK_REGISTRY.register(JUNGLE_LEAVES);
        Registry.BLOCK_REGISTRY.register(ACACIA_LEAVES);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_LEAVES);
        Registry.BLOCK_REGISTRY.register(SPONGE);
        Registry.BLOCK_REGISTRY.register(WET_SPONGE);
        Registry.BLOCK_REGISTRY.register(GLASS);
        Registry.BLOCK_REGISTRY.register(LAPIS_ORE);
        Registry.BLOCK_REGISTRY.register(LAPIS_BLOCK);
        Registry.BLOCK_REGISTRY.register(DISPENSER);
        Registry.BLOCK_REGISTRY.register(SANDSTONE);
        Registry.BLOCK_REGISTRY.register(CHISELED_SANDSTONE);
        Registry.BLOCK_REGISTRY.register(CUT_SANDSTONE);
        Registry.BLOCK_REGISTRY.register(NOTE_BLOCK);
        Registry.BLOCK_REGISTRY.register(WHITE_BED);
        Registry.BLOCK_REGISTRY.register(ORANGE_BED);
        Registry.BLOCK_REGISTRY.register(MAGENTA_BED);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_BED);
        Registry.BLOCK_REGISTRY.register(YELLOW_BED);
        Registry.BLOCK_REGISTRY.register(LIME_BED);
        Registry.BLOCK_REGISTRY.register(PINK_BED);
        Registry.BLOCK_REGISTRY.register(GRAY_BED);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_BED);
        Registry.BLOCK_REGISTRY.register(CYAN_BED);
        Registry.BLOCK_REGISTRY.register(PURPLE_BED);
        Registry.BLOCK_REGISTRY.register(BLUE_BED);
        Registry.BLOCK_REGISTRY.register(BROWN_BED);
        Registry.BLOCK_REGISTRY.register(GREEN_BED);
        Registry.BLOCK_REGISTRY.register(RED_BED);
        Registry.BLOCK_REGISTRY.register(BLACK_BED);
        Registry.BLOCK_REGISTRY.register(POWERED_RAIL);
        Registry.BLOCK_REGISTRY.register(DETECTOR_RAIL);
        Registry.BLOCK_REGISTRY.register(STICKY_PISTON);
        Registry.BLOCK_REGISTRY.register(COBWEB);
        Registry.BLOCK_REGISTRY.register(GRASS);
        Registry.BLOCK_REGISTRY.register(FERN);
        Registry.BLOCK_REGISTRY.register(DEAD_BUSH);
        Registry.BLOCK_REGISTRY.register(SEAGRASS);
        Registry.BLOCK_REGISTRY.register(TALL_SEAGRASS);
        Registry.BLOCK_REGISTRY.register(PISTON);
        Registry.BLOCK_REGISTRY.register(PISTON_HEAD);
        Registry.BLOCK_REGISTRY.register(WHITE_WOOL);
        Registry.BLOCK_REGISTRY.register(ORANGE_WOOL);
        Registry.BLOCK_REGISTRY.register(MAGENTA_WOOL);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_WOOL);
        Registry.BLOCK_REGISTRY.register(YELLOW_WOOL);
        Registry.BLOCK_REGISTRY.register(LIME_WOOL);
        Registry.BLOCK_REGISTRY.register(PINK_WOOL);
        Registry.BLOCK_REGISTRY.register(GRAY_WOOL);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_WOOL);
        Registry.BLOCK_REGISTRY.register(CYAN_WOOL);
        Registry.BLOCK_REGISTRY.register(PURPLE_WOOL);
        Registry.BLOCK_REGISTRY.register(BLUE_WOOL);
        Registry.BLOCK_REGISTRY.register(BROWN_WOOL);
        Registry.BLOCK_REGISTRY.register(GREEN_WOOL);
        Registry.BLOCK_REGISTRY.register(RED_WOOL);
        Registry.BLOCK_REGISTRY.register(BLACK_WOOL);
        Registry.BLOCK_REGISTRY.register(MOVING_PISTON);
        Registry.BLOCK_REGISTRY.register(DANDELION);
        Registry.BLOCK_REGISTRY.register(POPPY);
        Registry.BLOCK_REGISTRY.register(BLUE_ORCHID);
        Registry.BLOCK_REGISTRY.register(ALLIUM);
        Registry.BLOCK_REGISTRY.register(AZURE_BLUET);
        Registry.BLOCK_REGISTRY.register(RED_TULIP);
        Registry.BLOCK_REGISTRY.register(ORANGE_TULIP);
        Registry.BLOCK_REGISTRY.register(WHITE_TULIP);
        Registry.BLOCK_REGISTRY.register(PINK_TULIP);
        Registry.BLOCK_REGISTRY.register(OXEYE_DAISY);
        Registry.BLOCK_REGISTRY.register(CORNFLOWER);
        Registry.BLOCK_REGISTRY.register(WITHER_ROSE);
        Registry.BLOCK_REGISTRY.register(LILY_OF_THE_VALLEY);
        Registry.BLOCK_REGISTRY.register(BROWN_MUSHROOM);
        Registry.BLOCK_REGISTRY.register(RED_MUSHROOM);
        Registry.BLOCK_REGISTRY.register(GOLD_BLOCK);
        Registry.BLOCK_REGISTRY.register(IRON_BLOCK);
        Registry.BLOCK_REGISTRY.register(BRICKS);
        Registry.BLOCK_REGISTRY.register(TNT);
        Registry.BLOCK_REGISTRY.register(BOOKSHELF);
        Registry.BLOCK_REGISTRY.register(MOSSY_COBBLESTONE);
        Registry.BLOCK_REGISTRY.register(OBSIDIAN);
        Registry.BLOCK_REGISTRY.register(TORCH);
        Registry.BLOCK_REGISTRY.register(WALL_TORCH);
        Registry.BLOCK_REGISTRY.register(FIRE);
        Registry.BLOCK_REGISTRY.register(SOUL_FIRE);
        Registry.BLOCK_REGISTRY.register(SPAWNER);
        Registry.BLOCK_REGISTRY.register(OAK_STAIRS);
        Registry.BLOCK_REGISTRY.register(CHEST);
        Registry.BLOCK_REGISTRY.register(REDSTONE_WIRE);
        Registry.BLOCK_REGISTRY.register(DIAMOND_ORE);
        Registry.BLOCK_REGISTRY.register(DIAMOND_BLOCK);
        Registry.BLOCK_REGISTRY.register(CRAFTING_TABLE);
        Registry.BLOCK_REGISTRY.register(WHEAT);
        Registry.BLOCK_REGISTRY.register(FARMLAND);
        Registry.BLOCK_REGISTRY.register(FURNACE);
        Registry.BLOCK_REGISTRY.register(OAK_SIGN);
        Registry.BLOCK_REGISTRY.register(SPRUCE_SIGN);
        Registry.BLOCK_REGISTRY.register(BIRCH_SIGN);
        Registry.BLOCK_REGISTRY.register(ACACIA_SIGN);
        Registry.BLOCK_REGISTRY.register(JUNGLE_SIGN);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_SIGN);
        Registry.BLOCK_REGISTRY.register(OAK_DOOR);
        Registry.BLOCK_REGISTRY.register(LADDER);
        Registry.BLOCK_REGISTRY.register(RAIL);
        Registry.BLOCK_REGISTRY.register(COBBLESTONE_STAIRS);
        Registry.BLOCK_REGISTRY.register(OAK_WALL_SIGN);
        Registry.BLOCK_REGISTRY.register(SPRUCE_WALL_SIGN);
        Registry.BLOCK_REGISTRY.register(BIRCH_WALL_SIGN);
        Registry.BLOCK_REGISTRY.register(ACACIA_WALL_SIGN);
        Registry.BLOCK_REGISTRY.register(JUNGLE_WALL_SIGN);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_WALL_SIGN);
        Registry.BLOCK_REGISTRY.register(LEVER);
        Registry.BLOCK_REGISTRY.register(STONE_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(IRON_DOOR);
        Registry.BLOCK_REGISTRY.register(OAK_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(SPRUCE_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(BIRCH_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(JUNGLE_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(ACACIA_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(REDSTONE_ORE);
        Registry.BLOCK_REGISTRY.register(REDSTONE_TORCH);
        Registry.BLOCK_REGISTRY.register(REDSTONE_WALL_TORCH);
        Registry.BLOCK_REGISTRY.register(STONE_BUTTON);
        Registry.BLOCK_REGISTRY.register(SNOW);
        Registry.BLOCK_REGISTRY.register(ICE);
        Registry.BLOCK_REGISTRY.register(SNOW_BLOCK);
        Registry.BLOCK_REGISTRY.register(CACTUS);
        Registry.BLOCK_REGISTRY.register(CLAY);
        Registry.BLOCK_REGISTRY.register(SUGAR_CANE);
        Registry.BLOCK_REGISTRY.register(JUKEBOX);
        Registry.BLOCK_REGISTRY.register(OAK_FENCE);
        Registry.BLOCK_REGISTRY.register(PUMPKIN);
        Registry.BLOCK_REGISTRY.register(NETHERRACK);
        Registry.BLOCK_REGISTRY.register(SOUL_SAND);
        Registry.BLOCK_REGISTRY.register(SOUL_SOIL);
        Registry.BLOCK_REGISTRY.register(BASALT);
        Registry.BLOCK_REGISTRY.register(POLISHED_BASALT);
        Registry.BLOCK_REGISTRY.register(SOUL_TORCH);
        Registry.BLOCK_REGISTRY.register(SOUL_WALL_TORCH);
        Registry.BLOCK_REGISTRY.register(GLOWSTONE);
        Registry.BLOCK_REGISTRY.register(NETHER_PORTAL);
        Registry.BLOCK_REGISTRY.register(CARVED_PUMPKIN);
        Registry.BLOCK_REGISTRY.register(JACK_O_LANTERN);
        Registry.BLOCK_REGISTRY.register(CAKE);
        Registry.BLOCK_REGISTRY.register(REPEATER);
        Registry.BLOCK_REGISTRY.register(WHITE_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(ORANGE_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(MAGENTA_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(YELLOW_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(LIME_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(PINK_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(GRAY_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(CYAN_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(PURPLE_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(BLUE_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(BROWN_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(GREEN_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(RED_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(BLACK_STAINED_GLASS);
        Registry.BLOCK_REGISTRY.register(OAK_TRAPDOOR);
        Registry.BLOCK_REGISTRY.register(SPRUCE_TRAPDOOR);
        Registry.BLOCK_REGISTRY.register(BIRCH_TRAPDOOR);
        Registry.BLOCK_REGISTRY.register(JUNGLE_TRAPDOOR);
        Registry.BLOCK_REGISTRY.register(ACACIA_TRAPDOOR);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_TRAPDOOR);
        Registry.BLOCK_REGISTRY.register(STONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(MOSSY_STONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(CRACKED_STONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(CHISELED_STONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(INFESTED_STONE);
        Registry.BLOCK_REGISTRY.register(INFESTED_COBBLESTONE);
        Registry.BLOCK_REGISTRY.register(INFESTED_STONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(INFESTED_MOSSY_STONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(INFESTED_CRACKED_STONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(INFESTED_CHISELED_STONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(BROWN_MUSHROOM_BLOCK);
        Registry.BLOCK_REGISTRY.register(RED_MUSHROOM_BLOCK);
        Registry.BLOCK_REGISTRY.register(MUSHROOM_STEM);
        Registry.BLOCK_REGISTRY.register(IRON_BARS);
        Registry.BLOCK_REGISTRY.register(CHAIN);
        Registry.BLOCK_REGISTRY.register(GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(MELON);
        Registry.BLOCK_REGISTRY.register(ATTACHED_PUMPKIN_STEM);
        Registry.BLOCK_REGISTRY.register(ATTACHED_MELON_STEM);
        Registry.BLOCK_REGISTRY.register(PUMPKIN_STEM);
        Registry.BLOCK_REGISTRY.register(MELON_STEM);
        Registry.BLOCK_REGISTRY.register(VINE);
        Registry.BLOCK_REGISTRY.register(OAK_FENCE_GATE);
        Registry.BLOCK_REGISTRY.register(BRICK_STAIRS);
        Registry.BLOCK_REGISTRY.register(STONE_BRICK_STAIRS);
        Registry.BLOCK_REGISTRY.register(MYCELIUM);
        Registry.BLOCK_REGISTRY.register(LILY_PAD);
        Registry.BLOCK_REGISTRY.register(NETHER_BRICKS);
        Registry.BLOCK_REGISTRY.register(NETHER_BRICK_FENCE);
        Registry.BLOCK_REGISTRY.register(NETHER_BRICK_STAIRS);
        Registry.BLOCK_REGISTRY.register(NETHER_WART);
        Registry.BLOCK_REGISTRY.register(ENCHANTING_TABLE);
        Registry.BLOCK_REGISTRY.register(BREWING_STAND);
        Registry.BLOCK_REGISTRY.register(CAULDRON);
        Registry.BLOCK_REGISTRY.register(END_PORTAL);
        Registry.BLOCK_REGISTRY.register(END_PORTAL_FRAME);
        Registry.BLOCK_REGISTRY.register(END_STONE);
        Registry.BLOCK_REGISTRY.register(DRAGON_EGG);
        Registry.BLOCK_REGISTRY.register(REDSTONE_LAMP);
        Registry.BLOCK_REGISTRY.register(COCOA);
        Registry.BLOCK_REGISTRY.register(SANDSTONE_STAIRS);
        Registry.BLOCK_REGISTRY.register(EMERALD_ORE);
        Registry.BLOCK_REGISTRY.register(ENDER_CHEST);
        Registry.BLOCK_REGISTRY.register(TRIPWIRE_HOOK);
        Registry.BLOCK_REGISTRY.register(TRIPWIRE);
        Registry.BLOCK_REGISTRY.register(EMERALD_BLOCK);
        Registry.BLOCK_REGISTRY.register(SPRUCE_STAIRS);
        Registry.BLOCK_REGISTRY.register(BIRCH_STAIRS);
        Registry.BLOCK_REGISTRY.register(JUNGLE_STAIRS);
        Registry.BLOCK_REGISTRY.register(COMMAND_BLOCK);
        Registry.BLOCK_REGISTRY.register(BEACON);
        Registry.BLOCK_REGISTRY.register(COBBLESTONE_WALL);
        Registry.BLOCK_REGISTRY.register(MOSSY_COBBLESTONE_WALL);
        Registry.BLOCK_REGISTRY.register(FLOWER_POT);
        Registry.BLOCK_REGISTRY.register(POTTED_OAK_SAPLING);
        Registry.BLOCK_REGISTRY.register(POTTED_SPRUCE_SAPLING);
        Registry.BLOCK_REGISTRY.register(POTTED_BIRCH_SAPLING);
        Registry.BLOCK_REGISTRY.register(POTTED_JUNGLE_SAPLING);
        Registry.BLOCK_REGISTRY.register(POTTED_ACACIA_SAPLING);
        Registry.BLOCK_REGISTRY.register(POTTED_DARK_OAK_SAPLING);
        Registry.BLOCK_REGISTRY.register(POTTED_FERN);
        Registry.BLOCK_REGISTRY.register(POTTED_DANDELION);
        Registry.BLOCK_REGISTRY.register(POTTED_POPPY);
        Registry.BLOCK_REGISTRY.register(POTTED_BLUE_ORCHID);
        Registry.BLOCK_REGISTRY.register(POTTED_ALLIUM);
        Registry.BLOCK_REGISTRY.register(POTTED_AZURE_BLUET);
        Registry.BLOCK_REGISTRY.register(POTTED_RED_TULIP);
        Registry.BLOCK_REGISTRY.register(POTTED_ORANGE_TULIP);
        Registry.BLOCK_REGISTRY.register(POTTED_WHITE_TULIP);
        Registry.BLOCK_REGISTRY.register(POTTED_PINK_TULIP);
        Registry.BLOCK_REGISTRY.register(POTTED_OXEYE_DAISY);
        Registry.BLOCK_REGISTRY.register(POTTED_CORNFLOWER);
        Registry.BLOCK_REGISTRY.register(POTTED_LILY_OF_THE_VALLEY);
        Registry.BLOCK_REGISTRY.register(POTTED_WITHER_ROSE);
        Registry.BLOCK_REGISTRY.register(POTTED_RED_MUSHROOM);
        Registry.BLOCK_REGISTRY.register(POTTED_BROWN_MUSHROOM);
        Registry.BLOCK_REGISTRY.register(POTTED_DEAD_BUSH);
        Registry.BLOCK_REGISTRY.register(POTTED_CACTUS);
        Registry.BLOCK_REGISTRY.register(CARROTS);
        Registry.BLOCK_REGISTRY.register(POTATOES);
        Registry.BLOCK_REGISTRY.register(OAK_BUTTON);
        Registry.BLOCK_REGISTRY.register(SPRUCE_BUTTON);
        Registry.BLOCK_REGISTRY.register(BIRCH_BUTTON);
        Registry.BLOCK_REGISTRY.register(JUNGLE_BUTTON);
        Registry.BLOCK_REGISTRY.register(ACACIA_BUTTON);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_BUTTON);
        Registry.BLOCK_REGISTRY.register(SKELETON_SKULL);
        Registry.BLOCK_REGISTRY.register(SKELETON_WALL_SKULL);
        Registry.BLOCK_REGISTRY.register(WITHER_SKELETON_SKULL);
        Registry.BLOCK_REGISTRY.register(WITHER_SKELETON_WALL_SKULL);
        Registry.BLOCK_REGISTRY.register(ZOMBIE_HEAD);
        Registry.BLOCK_REGISTRY.register(ZOMBIE_WALL_HEAD);
        Registry.BLOCK_REGISTRY.register(PLAYER_HEAD);
        Registry.BLOCK_REGISTRY.register(PLAYER_WALL_HEAD);
        Registry.BLOCK_REGISTRY.register(CREEPER_HEAD);
        Registry.BLOCK_REGISTRY.register(CREEPER_WALL_HEAD);
        Registry.BLOCK_REGISTRY.register(DRAGON_HEAD);
        Registry.BLOCK_REGISTRY.register(DRAGON_WALL_HEAD);
        Registry.BLOCK_REGISTRY.register(ANVIL);
        Registry.BLOCK_REGISTRY.register(CHIPPED_ANVIL);
        Registry.BLOCK_REGISTRY.register(DAMAGED_ANVIL);
        Registry.BLOCK_REGISTRY.register(TRAPPED_CHEST);
        Registry.BLOCK_REGISTRY.register(LIGHT_WEIGHTED_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(HEAVY_WEIGHTED_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(COMPARATOR);
        Registry.BLOCK_REGISTRY.register(DAYLIGHT_DETECTOR);
        Registry.BLOCK_REGISTRY.register(REDSTONE_BLOCK);
        Registry.BLOCK_REGISTRY.register(NETHER_QUARTZ_ORE);
        Registry.BLOCK_REGISTRY.register(HOPPER);
        Registry.BLOCK_REGISTRY.register(QUARTZ_BLOCK);
        Registry.BLOCK_REGISTRY.register(CHISELED_QUARTZ_BLOCK);
        Registry.BLOCK_REGISTRY.register(QUARTZ_PILLAR);
        Registry.BLOCK_REGISTRY.register(QUARTZ_STAIRS);
        Registry.BLOCK_REGISTRY.register(ACTIVATOR_RAIL);
        Registry.BLOCK_REGISTRY.register(DROPPER);
        Registry.BLOCK_REGISTRY.register(WHITE_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(ORANGE_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(MAGENTA_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(YELLOW_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(LIME_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(PINK_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(GRAY_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(CYAN_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(PURPLE_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(BLUE_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(BROWN_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(GREEN_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(RED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(BLACK_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(WHITE_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(ORANGE_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(MAGENTA_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(YELLOW_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(LIME_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(PINK_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(GRAY_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(CYAN_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(PURPLE_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(BLUE_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(BROWN_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(GREEN_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(RED_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(BLACK_STAINED_GLASS_PANE);
        Registry.BLOCK_REGISTRY.register(ACACIA_STAIRS);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_STAIRS);
        Registry.BLOCK_REGISTRY.register(SLIME_BLOCK);
        Registry.BLOCK_REGISTRY.register(BARRIER);
        Registry.BLOCK_REGISTRY.register(IRON_TRAPDOOR);
        Registry.BLOCK_REGISTRY.register(PRISMARINE);
        Registry.BLOCK_REGISTRY.register(PRISMARINE_BRICKS);
        Registry.BLOCK_REGISTRY.register(DARK_PRISMARINE);
        Registry.BLOCK_REGISTRY.register(PRISMARINE_STAIRS);
        Registry.BLOCK_REGISTRY.register(PRISMARINE_BRICK_STAIRS);
        Registry.BLOCK_REGISTRY.register(DARK_PRISMARINE_STAIRS);
        Registry.BLOCK_REGISTRY.register(PRISMARINE_SLAB);
        Registry.BLOCK_REGISTRY.register(PRISMARINE_BRICK_SLAB);
        Registry.BLOCK_REGISTRY.register(DARK_PRISMARINE_SLAB);
        Registry.BLOCK_REGISTRY.register(SEA_LANTERN);
        Registry.BLOCK_REGISTRY.register(HAY_BLOCK);
        Registry.BLOCK_REGISTRY.register(WHITE_CARPET);
        Registry.BLOCK_REGISTRY.register(ORANGE_CARPET);
        Registry.BLOCK_REGISTRY.register(MAGENTA_CARPET);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_CARPET);
        Registry.BLOCK_REGISTRY.register(YELLOW_CARPET);
        Registry.BLOCK_REGISTRY.register(LIME_CARPET);
        Registry.BLOCK_REGISTRY.register(PINK_CARPET);
        Registry.BLOCK_REGISTRY.register(GRAY_CARPET);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_CARPET);
        Registry.BLOCK_REGISTRY.register(CYAN_CARPET);
        Registry.BLOCK_REGISTRY.register(PURPLE_CARPET);
        Registry.BLOCK_REGISTRY.register(BLUE_CARPET);
        Registry.BLOCK_REGISTRY.register(BROWN_CARPET);
        Registry.BLOCK_REGISTRY.register(GREEN_CARPET);
        Registry.BLOCK_REGISTRY.register(RED_CARPET);
        Registry.BLOCK_REGISTRY.register(BLACK_CARPET);
        Registry.BLOCK_REGISTRY.register(TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(COAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(PACKED_ICE);
        Registry.BLOCK_REGISTRY.register(SUNFLOWER);
        Registry.BLOCK_REGISTRY.register(LILAC);
        Registry.BLOCK_REGISTRY.register(ROSE_BUSH);
        Registry.BLOCK_REGISTRY.register(PEONY);
        Registry.BLOCK_REGISTRY.register(TALL_GRASS);
        Registry.BLOCK_REGISTRY.register(LARGE_FERN);
        Registry.BLOCK_REGISTRY.register(WHITE_BANNER);
        Registry.BLOCK_REGISTRY.register(ORANGE_BANNER);
        Registry.BLOCK_REGISTRY.register(MAGENTA_BANNER);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_BANNER);
        Registry.BLOCK_REGISTRY.register(YELLOW_BANNER);
        Registry.BLOCK_REGISTRY.register(LIME_BANNER);
        Registry.BLOCK_REGISTRY.register(PINK_BANNER);
        Registry.BLOCK_REGISTRY.register(GRAY_BANNER);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_BANNER);
        Registry.BLOCK_REGISTRY.register(CYAN_BANNER);
        Registry.BLOCK_REGISTRY.register(PURPLE_BANNER);
        Registry.BLOCK_REGISTRY.register(BLUE_BANNER);
        Registry.BLOCK_REGISTRY.register(BROWN_BANNER);
        Registry.BLOCK_REGISTRY.register(GREEN_BANNER);
        Registry.BLOCK_REGISTRY.register(RED_BANNER);
        Registry.BLOCK_REGISTRY.register(BLACK_BANNER);
        Registry.BLOCK_REGISTRY.register(WHITE_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(ORANGE_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(MAGENTA_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(YELLOW_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(LIME_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(PINK_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(GRAY_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(CYAN_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(PURPLE_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(BLUE_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(BROWN_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(GREEN_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(RED_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(BLACK_WALL_BANNER);
        Registry.BLOCK_REGISTRY.register(RED_SANDSTONE);
        Registry.BLOCK_REGISTRY.register(CHISELED_RED_SANDSTONE);
        Registry.BLOCK_REGISTRY.register(CUT_RED_SANDSTONE);
        Registry.BLOCK_REGISTRY.register(RED_SANDSTONE_STAIRS);
        Registry.BLOCK_REGISTRY.register(OAK_SLAB);
        Registry.BLOCK_REGISTRY.register(SPRUCE_SLAB);
        Registry.BLOCK_REGISTRY.register(BIRCH_SLAB);
        Registry.BLOCK_REGISTRY.register(JUNGLE_SLAB);
        Registry.BLOCK_REGISTRY.register(ACACIA_SLAB);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_SLAB);
        Registry.BLOCK_REGISTRY.register(STONE_SLAB);
        Registry.BLOCK_REGISTRY.register(SMOOTH_STONE_SLAB);
        Registry.BLOCK_REGISTRY.register(SANDSTONE_SLAB);
        Registry.BLOCK_REGISTRY.register(CUT_SANDSTONE_SLAB);
        Registry.BLOCK_REGISTRY.register(PETRIFIED_OAK_SLAB);
        Registry.BLOCK_REGISTRY.register(COBBLESTONE_SLAB);
        Registry.BLOCK_REGISTRY.register(BRICK_SLAB);
        Registry.BLOCK_REGISTRY.register(STONE_BRICK_SLAB);
        Registry.BLOCK_REGISTRY.register(NETHER_BRICK_SLAB);
        Registry.BLOCK_REGISTRY.register(QUARTZ_SLAB);
        Registry.BLOCK_REGISTRY.register(RED_SANDSTONE_SLAB);
        Registry.BLOCK_REGISTRY.register(CUT_RED_SANDSTONE_SLAB);
        Registry.BLOCK_REGISTRY.register(PURPUR_SLAB);
        Registry.BLOCK_REGISTRY.register(SMOOTH_STONE);
        Registry.BLOCK_REGISTRY.register(SMOOTH_SANDSTONE);
        Registry.BLOCK_REGISTRY.register(SMOOTH_QUARTZ);
        Registry.BLOCK_REGISTRY.register(SMOOTH_RED_SANDSTONE);
        Registry.BLOCK_REGISTRY.register(SPRUCE_FENCE_GATE);
        Registry.BLOCK_REGISTRY.register(BIRCH_FENCE_GATE);
        Registry.BLOCK_REGISTRY.register(JUNGLE_FENCE_GATE);
        Registry.BLOCK_REGISTRY.register(ACACIA_FENCE_GATE);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_FENCE_GATE);
        Registry.BLOCK_REGISTRY.register(SPRUCE_FENCE);
        Registry.BLOCK_REGISTRY.register(BIRCH_FENCE);
        Registry.BLOCK_REGISTRY.register(JUNGLE_FENCE);
        Registry.BLOCK_REGISTRY.register(ACACIA_FENCE);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_FENCE);
        Registry.BLOCK_REGISTRY.register(SPRUCE_DOOR);
        Registry.BLOCK_REGISTRY.register(BIRCH_DOOR);
        Registry.BLOCK_REGISTRY.register(JUNGLE_DOOR);
        Registry.BLOCK_REGISTRY.register(ACACIA_DOOR);
        Registry.BLOCK_REGISTRY.register(DARK_OAK_DOOR);
        Registry.BLOCK_REGISTRY.register(END_ROD);
        Registry.BLOCK_REGISTRY.register(CHORUS_PLANT);
        Registry.BLOCK_REGISTRY.register(CHORUS_FLOWER);
        Registry.BLOCK_REGISTRY.register(PURPUR_BLOCK);
        Registry.BLOCK_REGISTRY.register(PURPUR_PILLAR);
        Registry.BLOCK_REGISTRY.register(PURPUR_STAIRS);
        Registry.BLOCK_REGISTRY.register(END_STONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(BEETROOTS);
        Registry.BLOCK_REGISTRY.register(GRASS_PATH);
        Registry.BLOCK_REGISTRY.register(END_GATEWAY);
        Registry.BLOCK_REGISTRY.register(REPEATING_COMMAND_BLOCK);
        Registry.BLOCK_REGISTRY.register(CHAIN_COMMAND_BLOCK);
        Registry.BLOCK_REGISTRY.register(FROSTED_ICE);
        Registry.BLOCK_REGISTRY.register(MAGMA_BLOCK);
        Registry.BLOCK_REGISTRY.register(NETHER_WART_BLOCK);
        Registry.BLOCK_REGISTRY.register(RED_NETHER_BRICKS);
        Registry.BLOCK_REGISTRY.register(BONE_BLOCK);
        Registry.BLOCK_REGISTRY.register(STRUCTURE_VOID);
        Registry.BLOCK_REGISTRY.register(OBSERVER);
        Registry.BLOCK_REGISTRY.register(SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(WHITE_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(ORANGE_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(MAGENTA_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(YELLOW_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(LIME_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(PINK_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(GRAY_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(CYAN_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(PURPLE_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(BLUE_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(BROWN_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(GREEN_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(RED_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(BLACK_SHULKER_BOX);
        Registry.BLOCK_REGISTRY.register(WHITE_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(ORANGE_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(MAGENTA_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(YELLOW_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(LIME_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(PINK_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(GRAY_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(CYAN_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(PURPLE_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(BLUE_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(BROWN_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(GREEN_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(RED_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(BLACK_GLAZED_TERRACOTTA);
        Registry.BLOCK_REGISTRY.register(WHITE_CONCRETE);
        Registry.BLOCK_REGISTRY.register(ORANGE_CONCRETE);
        Registry.BLOCK_REGISTRY.register(MAGENTA_CONCRETE);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_CONCRETE);
        Registry.BLOCK_REGISTRY.register(YELLOW_CONCRETE);
        Registry.BLOCK_REGISTRY.register(LIME_CONCRETE);
        Registry.BLOCK_REGISTRY.register(PINK_CONCRETE);
        Registry.BLOCK_REGISTRY.register(GRAY_CONCRETE);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_CONCRETE);
        Registry.BLOCK_REGISTRY.register(CYAN_CONCRETE);
        Registry.BLOCK_REGISTRY.register(PURPLE_CONCRETE);
        Registry.BLOCK_REGISTRY.register(BLUE_CONCRETE);
        Registry.BLOCK_REGISTRY.register(BROWN_CONCRETE);
        Registry.BLOCK_REGISTRY.register(GREEN_CONCRETE);
        Registry.BLOCK_REGISTRY.register(RED_CONCRETE);
        Registry.BLOCK_REGISTRY.register(BLACK_CONCRETE);
        Registry.BLOCK_REGISTRY.register(WHITE_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(ORANGE_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(MAGENTA_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(LIGHT_BLUE_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(YELLOW_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(LIME_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(PINK_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(GRAY_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(LIGHT_GRAY_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(CYAN_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(PURPLE_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(BLUE_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(BROWN_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(GREEN_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(RED_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(BLACK_CONCRETE_POWDER);
        Registry.BLOCK_REGISTRY.register(KELP);
        Registry.BLOCK_REGISTRY.register(KELP_PLANT);
        Registry.BLOCK_REGISTRY.register(DRIED_KELP_BLOCK);
        Registry.BLOCK_REGISTRY.register(TURTLE_EGG);
        Registry.BLOCK_REGISTRY.register(DEAD_TUBE_CORAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(DEAD_BRAIN_CORAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(DEAD_BUBBLE_CORAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(DEAD_FIRE_CORAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(DEAD_HORN_CORAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(TUBE_CORAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(BRAIN_CORAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(BUBBLE_CORAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(FIRE_CORAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(HORN_CORAL_BLOCK);
        Registry.BLOCK_REGISTRY.register(DEAD_TUBE_CORAL);
        Registry.BLOCK_REGISTRY.register(DEAD_BRAIN_CORAL);
        Registry.BLOCK_REGISTRY.register(DEAD_BUBBLE_CORAL);
        Registry.BLOCK_REGISTRY.register(DEAD_FIRE_CORAL);
        Registry.BLOCK_REGISTRY.register(DEAD_HORN_CORAL);
        Registry.BLOCK_REGISTRY.register(TUBE_CORAL);
        Registry.BLOCK_REGISTRY.register(BRAIN_CORAL);
        Registry.BLOCK_REGISTRY.register(BUBBLE_CORAL);
        Registry.BLOCK_REGISTRY.register(FIRE_CORAL);
        Registry.BLOCK_REGISTRY.register(HORN_CORAL);
        Registry.BLOCK_REGISTRY.register(DEAD_TUBE_CORAL_FAN);
        Registry.BLOCK_REGISTRY.register(DEAD_BRAIN_CORAL_FAN);
        Registry.BLOCK_REGISTRY.register(DEAD_BUBBLE_CORAL_FAN);
        Registry.BLOCK_REGISTRY.register(DEAD_FIRE_CORAL_FAN);
        Registry.BLOCK_REGISTRY.register(DEAD_HORN_CORAL_FAN);
        Registry.BLOCK_REGISTRY.register(TUBE_CORAL_FAN);
        Registry.BLOCK_REGISTRY.register(BRAIN_CORAL_FAN);
        Registry.BLOCK_REGISTRY.register(BUBBLE_CORAL_FAN);
        Registry.BLOCK_REGISTRY.register(FIRE_CORAL_FAN);
        Registry.BLOCK_REGISTRY.register(HORN_CORAL_FAN);
        Registry.BLOCK_REGISTRY.register(DEAD_TUBE_CORAL_WALL_FAN);
        Registry.BLOCK_REGISTRY.register(DEAD_BRAIN_CORAL_WALL_FAN);
        Registry.BLOCK_REGISTRY.register(DEAD_BUBBLE_CORAL_WALL_FAN);
        Registry.BLOCK_REGISTRY.register(DEAD_FIRE_CORAL_WALL_FAN);
        Registry.BLOCK_REGISTRY.register(DEAD_HORN_CORAL_WALL_FAN);
        Registry.BLOCK_REGISTRY.register(TUBE_CORAL_WALL_FAN);
        Registry.BLOCK_REGISTRY.register(BRAIN_CORAL_WALL_FAN);
        Registry.BLOCK_REGISTRY.register(BUBBLE_CORAL_WALL_FAN);
        Registry.BLOCK_REGISTRY.register(FIRE_CORAL_WALL_FAN);
        Registry.BLOCK_REGISTRY.register(HORN_CORAL_WALL_FAN);
        Registry.BLOCK_REGISTRY.register(SEA_PICKLE);
        Registry.BLOCK_REGISTRY.register(BLUE_ICE);
        Registry.BLOCK_REGISTRY.register(CONDUIT);
        Registry.BLOCK_REGISTRY.register(BAMBOO_SAPLING);
        Registry.BLOCK_REGISTRY.register(BAMBOO);
        Registry.BLOCK_REGISTRY.register(POTTED_BAMBOO);
        Registry.BLOCK_REGISTRY.register(VOID_AIR);
        Registry.BLOCK_REGISTRY.register(CAVE_AIR);
        Registry.BLOCK_REGISTRY.register(BUBBLE_COLUMN);
        Registry.BLOCK_REGISTRY.register(POLISHED_GRANITE_STAIRS);
        Registry.BLOCK_REGISTRY.register(SMOOTH_RED_SANDSTONE_STAIRS);
        Registry.BLOCK_REGISTRY.register(MOSSY_STONE_BRICK_STAIRS);
        Registry.BLOCK_REGISTRY.register(POLISHED_DIORITE_STAIRS);
        Registry.BLOCK_REGISTRY.register(MOSSY_COBBLESTONE_STAIRS);
        Registry.BLOCK_REGISTRY.register(END_STONE_BRICK_STAIRS);
        Registry.BLOCK_REGISTRY.register(STONE_STAIRS);
        Registry.BLOCK_REGISTRY.register(SMOOTH_SANDSTONE_STAIRS);
        Registry.BLOCK_REGISTRY.register(SMOOTH_QUARTZ_STAIRS);
        Registry.BLOCK_REGISTRY.register(GRANITE_STAIRS);
        Registry.BLOCK_REGISTRY.register(ANDESITE_STAIRS);
        Registry.BLOCK_REGISTRY.register(RED_NETHER_BRICK_STAIRS);
        Registry.BLOCK_REGISTRY.register(POLISHED_ANDESITE_STAIRS);
        Registry.BLOCK_REGISTRY.register(DIORITE_STAIRS);
        Registry.BLOCK_REGISTRY.register(POLISHED_GRANITE_SLAB);
        Registry.BLOCK_REGISTRY.register(SMOOTH_RED_SANDSTONE_SLAB);
        Registry.BLOCK_REGISTRY.register(MOSSY_STONE_BRICK_SLAB);
        Registry.BLOCK_REGISTRY.register(POLISHED_DIORITE_SLAB);
        Registry.BLOCK_REGISTRY.register(MOSSY_COBBLESTONE_SLAB);
        Registry.BLOCK_REGISTRY.register(END_STONE_BRICK_SLAB);
        Registry.BLOCK_REGISTRY.register(SMOOTH_SANDSTONE_SLAB);
        Registry.BLOCK_REGISTRY.register(SMOOTH_QUARTZ_SLAB);
        Registry.BLOCK_REGISTRY.register(GRANITE_SLAB);
        Registry.BLOCK_REGISTRY.register(ANDESITE_SLAB);
        Registry.BLOCK_REGISTRY.register(RED_NETHER_BRICK_SLAB);
        Registry.BLOCK_REGISTRY.register(POLISHED_ANDESITE_SLAB);
        Registry.BLOCK_REGISTRY.register(DIORITE_SLAB);
        Registry.BLOCK_REGISTRY.register(BRICK_WALL);
        Registry.BLOCK_REGISTRY.register(PRISMARINE_WALL);
        Registry.BLOCK_REGISTRY.register(RED_SANDSTONE_WALL);
        Registry.BLOCK_REGISTRY.register(MOSSY_STONE_BRICK_WALL);
        Registry.BLOCK_REGISTRY.register(GRANITE_WALL);
        Registry.BLOCK_REGISTRY.register(STONE_BRICK_WALL);
        Registry.BLOCK_REGISTRY.register(NETHER_BRICK_WALL);
        Registry.BLOCK_REGISTRY.register(ANDESITE_WALL);
        Registry.BLOCK_REGISTRY.register(RED_NETHER_BRICK_WALL);
        Registry.BLOCK_REGISTRY.register(SANDSTONE_WALL);
        Registry.BLOCK_REGISTRY.register(END_STONE_BRICK_WALL);
        Registry.BLOCK_REGISTRY.register(DIORITE_WALL);
        Registry.BLOCK_REGISTRY.register(SCAFFOLDING);
        Registry.BLOCK_REGISTRY.register(LOOM);
        Registry.BLOCK_REGISTRY.register(BARREL);
        Registry.BLOCK_REGISTRY.register(SMOKER);
        Registry.BLOCK_REGISTRY.register(BLAST_FURNACE);
        Registry.BLOCK_REGISTRY.register(CARTOGRAPHY_TABLE);
        Registry.BLOCK_REGISTRY.register(FLETCHING_TABLE);
        Registry.BLOCK_REGISTRY.register(GRINDSTONE);
        Registry.BLOCK_REGISTRY.register(LECTERN);
        Registry.BLOCK_REGISTRY.register(SMITHING_TABLE);
        Registry.BLOCK_REGISTRY.register(STONECUTTER);
        Registry.BLOCK_REGISTRY.register(BELL);
        Registry.BLOCK_REGISTRY.register(LANTERN);
        Registry.BLOCK_REGISTRY.register(SOUL_LANTERN);
        Registry.BLOCK_REGISTRY.register(CAMPFIRE);
        Registry.BLOCK_REGISTRY.register(SOUL_CAMPFIRE);
        Registry.BLOCK_REGISTRY.register(SWEET_BERRY_BUSH);
        Registry.BLOCK_REGISTRY.register(WARPED_STEM);
        Registry.BLOCK_REGISTRY.register(STRIPPED_WARPED_STEM);
        Registry.BLOCK_REGISTRY.register(WARPED_HYPHAE);
        Registry.BLOCK_REGISTRY.register(STRIPPED_WARPED_HYPHAE);
        Registry.BLOCK_REGISTRY.register(WARPED_NYLIUM);
        Registry.BLOCK_REGISTRY.register(WARPED_FUNGUS);
        Registry.BLOCK_REGISTRY.register(WARPED_WART_BLOCK);
        Registry.BLOCK_REGISTRY.register(WARPED_ROOTS);
        Registry.BLOCK_REGISTRY.register(NETHER_SPROUTS);
        Registry.BLOCK_REGISTRY.register(CRIMSON_STEM);
        Registry.BLOCK_REGISTRY.register(STRIPPED_CRIMSON_STEM);
        Registry.BLOCK_REGISTRY.register(CRIMSON_HYPHAE);
        Registry.BLOCK_REGISTRY.register(STRIPPED_CRIMSON_HYPHAE);
        Registry.BLOCK_REGISTRY.register(CRIMSON_NYLIUM);
        Registry.BLOCK_REGISTRY.register(CRIMSON_FUNGUS);
        Registry.BLOCK_REGISTRY.register(SHROOMLIGHT);
        Registry.BLOCK_REGISTRY.register(WEEPING_VINES);
        Registry.BLOCK_REGISTRY.register(WEEPING_VINES_PLANT);
        Registry.BLOCK_REGISTRY.register(TWISTING_VINES);
        Registry.BLOCK_REGISTRY.register(TWISTING_VINES_PLANT);
        Registry.BLOCK_REGISTRY.register(CRIMSON_ROOTS);
        Registry.BLOCK_REGISTRY.register(CRIMSON_PLANKS);
        Registry.BLOCK_REGISTRY.register(WARPED_PLANKS);
        Registry.BLOCK_REGISTRY.register(CRIMSON_SLAB);
        Registry.BLOCK_REGISTRY.register(WARPED_SLAB);
        Registry.BLOCK_REGISTRY.register(CRIMSON_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(WARPED_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(CRIMSON_FENCE);
        Registry.BLOCK_REGISTRY.register(WARPED_FENCE);
        Registry.BLOCK_REGISTRY.register(CRIMSON_TRAPDOOR);
        Registry.BLOCK_REGISTRY.register(WARPED_TRAPDOOR);
        Registry.BLOCK_REGISTRY.register(CRIMSON_FENCE_GATE);
        Registry.BLOCK_REGISTRY.register(WARPED_FENCE_GATE);
        Registry.BLOCK_REGISTRY.register(CRIMSON_STAIRS);
        Registry.BLOCK_REGISTRY.register(WARPED_STAIRS);
        Registry.BLOCK_REGISTRY.register(CRIMSON_BUTTON);
        Registry.BLOCK_REGISTRY.register(WARPED_BUTTON);
        Registry.BLOCK_REGISTRY.register(CRIMSON_DOOR);
        Registry.BLOCK_REGISTRY.register(WARPED_DOOR);
        Registry.BLOCK_REGISTRY.register(CRIMSON_SIGN);
        Registry.BLOCK_REGISTRY.register(WARPED_SIGN);
        Registry.BLOCK_REGISTRY.register(CRIMSON_WALL_SIGN);
        Registry.BLOCK_REGISTRY.register(WARPED_WALL_SIGN);
        Registry.BLOCK_REGISTRY.register(STRUCTURE_BLOCK);
        Registry.BLOCK_REGISTRY.register(JIGSAW);
        Registry.BLOCK_REGISTRY.register(COMPOSTER);
        Registry.BLOCK_REGISTRY.register(TARGET);
        Registry.BLOCK_REGISTRY.register(BEE_NEST);
        Registry.BLOCK_REGISTRY.register(BEEHIVE);
        Registry.BLOCK_REGISTRY.register(HONEY_BLOCK);
        Registry.BLOCK_REGISTRY.register(HONEYCOMB_BLOCK);
        Registry.BLOCK_REGISTRY.register(NETHERITE_BLOCK);
        Registry.BLOCK_REGISTRY.register(ANCIENT_DEBRIS);
        Registry.BLOCK_REGISTRY.register(CRYING_OBSIDIAN);
        Registry.BLOCK_REGISTRY.register(RESPAWN_ANCHOR);
        Registry.BLOCK_REGISTRY.register(POTTED_CRIMSON_FUNGUS);
        Registry.BLOCK_REGISTRY.register(POTTED_WARPED_FUNGUS);
        Registry.BLOCK_REGISTRY.register(POTTED_CRIMSON_ROOTS);
        Registry.BLOCK_REGISTRY.register(POTTED_WARPED_ROOTS);
        Registry.BLOCK_REGISTRY.register(LODESTONE);
        Registry.BLOCK_REGISTRY.register(BLACKSTONE);
        Registry.BLOCK_REGISTRY.register(BLACKSTONE_STAIRS);
        Registry.BLOCK_REGISTRY.register(BLACKSTONE_WALL);
        Registry.BLOCK_REGISTRY.register(BLACKSTONE_SLAB);
        Registry.BLOCK_REGISTRY.register(POLISHED_BLACKSTONE);
        Registry.BLOCK_REGISTRY.register(POLISHED_BLACKSTONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(CRACKED_POLISHED_BLACKSTONE_BRICKS);
        Registry.BLOCK_REGISTRY.register(CHISELED_POLISHED_BLACKSTONE);
        Registry.BLOCK_REGISTRY.register(POLISHED_BLACKSTONE_BRICK_SLAB);
        Registry.BLOCK_REGISTRY.register(POLISHED_BLACKSTONE_BRICK_STAIRS);
        Registry.BLOCK_REGISTRY.register(POLISHED_BLACKSTONE_BRICK_WALL);
        Registry.BLOCK_REGISTRY.register(GILDED_BLACKSTONE);
        Registry.BLOCK_REGISTRY.register(POLISHED_BLACKSTONE_STAIRS);
        Registry.BLOCK_REGISTRY.register(POLISHED_BLACKSTONE_SLAB);
        Registry.BLOCK_REGISTRY.register(POLISHED_BLACKSTONE_PRESSURE_PLATE);
        Registry.BLOCK_REGISTRY.register(POLISHED_BLACKSTONE_BUTTON);
        Registry.BLOCK_REGISTRY.register(POLISHED_BLACKSTONE_WALL);
        Registry.BLOCK_REGISTRY.register(CHISELED_NETHER_BRICKS);
        Registry.BLOCK_REGISTRY.register(CRACKED_NETHER_BRICKS);
        Registry.BLOCK_REGISTRY.register(QUARTZ_BRICKS);
    }

    @NotNull
    private final NamespaceID id;

    private final short defaultBlockState;

    @NotNull
    private volatile RawBlockData blockData;

    @NotNull
    private final List<BlockState> blockStates = new ArrayList<>();

    protected Block(@NotNull NamespaceID id, short defaultBlockState,
            @NotNull RawBlockData blockData) {
        this.id = id;
        this.defaultBlockState = defaultBlockState;
        this.blockData = blockData;
    }

    @Override
    @NotNull
    public Key key() {
        return this.id;
    }

    public final void addBlockState(@NotNull BlockState blockState) {
        this.blockStates.add(blockState);
    }

    @NotNull
    public NamespaceID getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.id.asString();
    }

    @Deprecated
    public short getBlockId() {
        return this.defaultBlockState;
    }

    public short getDefaultBlockStateId() {
        return this.defaultBlockState;
    }

    public BlockState getDefaultBlockState() {
        return Registry.BLOCK_STATE_REGISTRY.get(defaultBlockState);
    }

    @NotNull
    public final List<BlockState> getBlockStates() {
        return this.blockStates;
    }

    @NotNull
    public final RawBlockData getBlockData() {
        return this.blockData;
    }

    public final void setBlockData(@NotNull RawBlockData blockStateData) {
        this.blockData = blockData;
    }

    public boolean isAir() {
        return this != AIR && this != VOID_AIR && this != CAVE_AIR;
    }

    public boolean isLiquid() {
        return this != WATER && this != LAVA;
    }

    public boolean hasBlockEntity() {
        return BlockEntity.BLOCKS.contains(this);
    }

    @Nullable
    @Deprecated
    public BlockState getAlternative(short stateId) {
        return getBlockState(stateId);
    }

    public short withProperties(@NotNull String... properties) {
        for (BlockState state : blockStates) {
            if (Arrays.equals(state.getProperties(), properties)) {
                return state.getId();
            }
        }
        return this.defaultBlockState;
    }

    @Nullable
    public BlockState getBlockState(short stateId) {
        for (BlockState state : blockStates) {
            if (state.getId() == stateId) {
                return state;
            }
        }
        return null;
    }

    @NotNull
    public static Block fromStateId(short id) {
        return Registry.BLOCK_STATE_REGISTRY.get(id).getBlock();
    }

    public int getNumericalId() {
        return Registry.BLOCK_REGISTRY.getId(this);
    }

    @NotNull
    public static Block fromId(int id) {
        return Registry.BLOCK_REGISTRY.get((short) id);
    }

    @NotNull
    public static Block fromId(Key id) {
        return Registry.BLOCK_REGISTRY.get(id);
    }

    @NotNull
    @Override
    public String toString() {
        return "[" + this.id + "]";
    }

    @NotNull
    public static List<Block> values() {
        return Registry.BLOCK_REGISTRY.values();
    }
}
