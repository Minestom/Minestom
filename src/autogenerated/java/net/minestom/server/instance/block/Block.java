package net.minestom.server.instance.block;

import java.lang.Deprecated;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
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
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * AUTOGENERATED
 */
@SuppressWarnings("deprecation")
public class Block implements Keyed {
  public static final Block AIR = new Block(NamespaceID.from("minecraft:air"), (short) 0);

  public static final Block STONE = new Block(NamespaceID.from("minecraft:stone"), (short) 1);

  public static final Block GRANITE = new Block(NamespaceID.from("minecraft:granite"), (short) 2);

  public static final Block POLISHED_GRANITE = new Block(NamespaceID.from("minecraft:polished_granite"), (short) 3);

  public static final Block DIORITE = new Block(NamespaceID.from("minecraft:diorite"), (short) 4);

  public static final Block POLISHED_DIORITE = new Block(NamespaceID.from("minecraft:polished_diorite"), (short) 5);

  public static final Block ANDESITE = new Block(NamespaceID.from("minecraft:andesite"), (short) 6);

  public static final Block POLISHED_ANDESITE = new Block(NamespaceID.from("minecraft:polished_andesite"), (short) 7);

  public static final Block GRASS_BLOCK = new Block(NamespaceID.from("minecraft:grass_block"), (short) 9);

  public static final Block DIRT = new Block(NamespaceID.from("minecraft:dirt"), (short) 10);

  public static final Block COARSE_DIRT = new Block(NamespaceID.from("minecraft:coarse_dirt"), (short) 11);

  public static final Block PODZOL = new Block(NamespaceID.from("minecraft:podzol"), (short) 13);

  public static final Block COBBLESTONE = new Block(NamespaceID.from("minecraft:cobblestone"), (short) 14);

  public static final Block OAK_PLANKS = new Block(NamespaceID.from("minecraft:oak_planks"), (short) 15);

  public static final Block SPRUCE_PLANKS = new Block(NamespaceID.from("minecraft:spruce_planks"), (short) 16);

  public static final Block BIRCH_PLANKS = new Block(NamespaceID.from("minecraft:birch_planks"), (short) 17);

  public static final Block JUNGLE_PLANKS = new Block(NamespaceID.from("minecraft:jungle_planks"), (short) 18);

  public static final Block ACACIA_PLANKS = new Block(NamespaceID.from("minecraft:acacia_planks"), (short) 19);

  public static final Block DARK_OAK_PLANKS = new Block(NamespaceID.from("minecraft:dark_oak_planks"), (short) 20);

  public static final Block OAK_SAPLING = new Block(NamespaceID.from("minecraft:oak_sapling"), (short) 21);

  public static final Block SPRUCE_SAPLING = new Block(NamespaceID.from("minecraft:spruce_sapling"), (short) 23);

  public static final Block BIRCH_SAPLING = new Block(NamespaceID.from("minecraft:birch_sapling"), (short) 25);

  public static final Block JUNGLE_SAPLING = new Block(NamespaceID.from("minecraft:jungle_sapling"), (short) 27);

  public static final Block ACACIA_SAPLING = new Block(NamespaceID.from("minecraft:acacia_sapling"), (short) 29);

  public static final Block DARK_OAK_SAPLING = new Block(NamespaceID.from("minecraft:dark_oak_sapling"), (short) 31);

  public static final Block BEDROCK = new Block(NamespaceID.from("minecraft:bedrock"), (short) 33);

  public static final Block WATER = new Block(NamespaceID.from("minecraft:water"), (short) 34);

  public static final Block LAVA = new Block(NamespaceID.from("minecraft:lava"), (short) 50);

  public static final Block SAND = new Block(NamespaceID.from("minecraft:sand"), (short) 66);

  public static final Block RED_SAND = new Block(NamespaceID.from("minecraft:red_sand"), (short) 67);

  public static final Block GRAVEL = new Block(NamespaceID.from("minecraft:gravel"), (short) 68);

  public static final Block GOLD_ORE = new Block(NamespaceID.from("minecraft:gold_ore"), (short) 69);

  public static final Block IRON_ORE = new Block(NamespaceID.from("minecraft:iron_ore"), (short) 70);

  public static final Block COAL_ORE = new Block(NamespaceID.from("minecraft:coal_ore"), (short) 71);

  public static final Block NETHER_GOLD_ORE = new Block(NamespaceID.from("minecraft:nether_gold_ore"), (short) 72);

  public static final Block OAK_LOG = new Block(NamespaceID.from("minecraft:oak_log"), (short) 74);

  public static final Block SPRUCE_LOG = new Block(NamespaceID.from("minecraft:spruce_log"), (short) 77);

  public static final Block BIRCH_LOG = new Block(NamespaceID.from("minecraft:birch_log"), (short) 80);

  public static final Block JUNGLE_LOG = new Block(NamespaceID.from("minecraft:jungle_log"), (short) 83);

  public static final Block ACACIA_LOG = new Block(NamespaceID.from("minecraft:acacia_log"), (short) 86);

  public static final Block DARK_OAK_LOG = new Block(NamespaceID.from("minecraft:dark_oak_log"), (short) 89);

  public static final Block STRIPPED_SPRUCE_LOG = new Block(NamespaceID.from("minecraft:stripped_spruce_log"), (short) 92);

  public static final Block STRIPPED_BIRCH_LOG = new Block(NamespaceID.from("minecraft:stripped_birch_log"), (short) 95);

  public static final Block STRIPPED_JUNGLE_LOG = new Block(NamespaceID.from("minecraft:stripped_jungle_log"), (short) 98);

  public static final Block STRIPPED_ACACIA_LOG = new Block(NamespaceID.from("minecraft:stripped_acacia_log"), (short) 101);

  public static final Block STRIPPED_DARK_OAK_LOG = new Block(NamespaceID.from("minecraft:stripped_dark_oak_log"), (short) 104);

  public static final Block STRIPPED_OAK_LOG = new Block(NamespaceID.from("minecraft:stripped_oak_log"), (short) 107);

  public static final Block OAK_WOOD = new Block(NamespaceID.from("minecraft:oak_wood"), (short) 110);

  public static final Block SPRUCE_WOOD = new Block(NamespaceID.from("minecraft:spruce_wood"), (short) 113);

  public static final Block BIRCH_WOOD = new Block(NamespaceID.from("minecraft:birch_wood"), (short) 116);

  public static final Block JUNGLE_WOOD = new Block(NamespaceID.from("minecraft:jungle_wood"), (short) 119);

  public static final Block ACACIA_WOOD = new Block(NamespaceID.from("minecraft:acacia_wood"), (short) 122);

  public static final Block DARK_OAK_WOOD = new Block(NamespaceID.from("minecraft:dark_oak_wood"), (short) 125);

  public static final Block STRIPPED_OAK_WOOD = new Block(NamespaceID.from("minecraft:stripped_oak_wood"), (short) 128);

  public static final Block STRIPPED_SPRUCE_WOOD = new Block(NamespaceID.from("minecraft:stripped_spruce_wood"), (short) 131);

  public static final Block STRIPPED_BIRCH_WOOD = new Block(NamespaceID.from("minecraft:stripped_birch_wood"), (short) 134);

  public static final Block STRIPPED_JUNGLE_WOOD = new Block(NamespaceID.from("minecraft:stripped_jungle_wood"), (short) 137);

  public static final Block STRIPPED_ACACIA_WOOD = new Block(NamespaceID.from("minecraft:stripped_acacia_wood"), (short) 140);

  public static final Block STRIPPED_DARK_OAK_WOOD = new Block(NamespaceID.from("minecraft:stripped_dark_oak_wood"), (short) 143);

  public static final Block OAK_LEAVES = new Block(NamespaceID.from("minecraft:oak_leaves"), (short) 158);

  public static final Block SPRUCE_LEAVES = new Block(NamespaceID.from("minecraft:spruce_leaves"), (short) 172);

  public static final Block BIRCH_LEAVES = new Block(NamespaceID.from("minecraft:birch_leaves"), (short) 186);

  public static final Block JUNGLE_LEAVES = new Block(NamespaceID.from("minecraft:jungle_leaves"), (short) 200);

  public static final Block ACACIA_LEAVES = new Block(NamespaceID.from("minecraft:acacia_leaves"), (short) 214);

  public static final Block DARK_OAK_LEAVES = new Block(NamespaceID.from("minecraft:dark_oak_leaves"), (short) 228);

  public static final Block SPONGE = new Block(NamespaceID.from("minecraft:sponge"), (short) 229);

  public static final Block WET_SPONGE = new Block(NamespaceID.from("minecraft:wet_sponge"), (short) 230);

  public static final Block GLASS = new Block(NamespaceID.from("minecraft:glass"), (short) 231);

  public static final Block LAPIS_ORE = new Block(NamespaceID.from("minecraft:lapis_ore"), (short) 232);

  public static final Block LAPIS_BLOCK = new Block(NamespaceID.from("minecraft:lapis_block"), (short) 233);

  public static final Block DISPENSER = new Block(NamespaceID.from("minecraft:dispenser"), (short) 235);

  public static final Block SANDSTONE = new Block(NamespaceID.from("minecraft:sandstone"), (short) 246);

  public static final Block CHISELED_SANDSTONE = new Block(NamespaceID.from("minecraft:chiseled_sandstone"), (short) 247);

  public static final Block CUT_SANDSTONE = new Block(NamespaceID.from("minecraft:cut_sandstone"), (short) 248);

  public static final Block NOTE_BLOCK = new Block(NamespaceID.from("minecraft:note_block"), (short) 250);

  public static final Block WHITE_BED = new Block(NamespaceID.from("minecraft:white_bed"), (short) 1052);

  public static final Block ORANGE_BED = new Block(NamespaceID.from("minecraft:orange_bed"), (short) 1068);

  public static final Block MAGENTA_BED = new Block(NamespaceID.from("minecraft:magenta_bed"), (short) 1084);

  public static final Block LIGHT_BLUE_BED = new Block(NamespaceID.from("minecraft:light_blue_bed"), (short) 1100);

  public static final Block YELLOW_BED = new Block(NamespaceID.from("minecraft:yellow_bed"), (short) 1116);

  public static final Block LIME_BED = new Block(NamespaceID.from("minecraft:lime_bed"), (short) 1132);

  public static final Block PINK_BED = new Block(NamespaceID.from("minecraft:pink_bed"), (short) 1148);

  public static final Block GRAY_BED = new Block(NamespaceID.from("minecraft:gray_bed"), (short) 1164);

  public static final Block LIGHT_GRAY_BED = new Block(NamespaceID.from("minecraft:light_gray_bed"), (short) 1180);

  public static final Block CYAN_BED = new Block(NamespaceID.from("minecraft:cyan_bed"), (short) 1196);

  public static final Block PURPLE_BED = new Block(NamespaceID.from("minecraft:purple_bed"), (short) 1212);

  public static final Block BLUE_BED = new Block(NamespaceID.from("minecraft:blue_bed"), (short) 1228);

  public static final Block BROWN_BED = new Block(NamespaceID.from("minecraft:brown_bed"), (short) 1244);

  public static final Block GREEN_BED = new Block(NamespaceID.from("minecraft:green_bed"), (short) 1260);

  public static final Block RED_BED = new Block(NamespaceID.from("minecraft:red_bed"), (short) 1276);

  public static final Block BLACK_BED = new Block(NamespaceID.from("minecraft:black_bed"), (short) 1292);

  public static final Block POWERED_RAIL = new Block(NamespaceID.from("minecraft:powered_rail"), (short) 1311);

  public static final Block DETECTOR_RAIL = new Block(NamespaceID.from("minecraft:detector_rail"), (short) 1323);

  public static final Block STICKY_PISTON = new Block(NamespaceID.from("minecraft:sticky_piston"), (short) 1335);

  public static final Block COBWEB = new Block(NamespaceID.from("minecraft:cobweb"), (short) 1341);

  public static final Block GRASS = new Block(NamespaceID.from("minecraft:grass"), (short) 1342);

  public static final Block FERN = new Block(NamespaceID.from("minecraft:fern"), (short) 1343);

  public static final Block DEAD_BUSH = new Block(NamespaceID.from("minecraft:dead_bush"), (short) 1344);

  public static final Block SEAGRASS = new Block(NamespaceID.from("minecraft:seagrass"), (short) 1345);

  public static final Block TALL_SEAGRASS = new Block(NamespaceID.from("minecraft:tall_seagrass"), (short) 1347);

  public static final Block PISTON = new Block(NamespaceID.from("minecraft:piston"), (short) 1354);

  public static final Block PISTON_HEAD = new Block(NamespaceID.from("minecraft:piston_head"), (short) 1362);

  public static final Block WHITE_WOOL = new Block(NamespaceID.from("minecraft:white_wool"), (short) 1384);

  public static final Block ORANGE_WOOL = new Block(NamespaceID.from("minecraft:orange_wool"), (short) 1385);

  public static final Block MAGENTA_WOOL = new Block(NamespaceID.from("minecraft:magenta_wool"), (short) 1386);

  public static final Block LIGHT_BLUE_WOOL = new Block(NamespaceID.from("minecraft:light_blue_wool"), (short) 1387);

  public static final Block YELLOW_WOOL = new Block(NamespaceID.from("minecraft:yellow_wool"), (short) 1388);

  public static final Block LIME_WOOL = new Block(NamespaceID.from("minecraft:lime_wool"), (short) 1389);

  public static final Block PINK_WOOL = new Block(NamespaceID.from("minecraft:pink_wool"), (short) 1390);

  public static final Block GRAY_WOOL = new Block(NamespaceID.from("minecraft:gray_wool"), (short) 1391);

  public static final Block LIGHT_GRAY_WOOL = new Block(NamespaceID.from("minecraft:light_gray_wool"), (short) 1392);

  public static final Block CYAN_WOOL = new Block(NamespaceID.from("minecraft:cyan_wool"), (short) 1393);

  public static final Block PURPLE_WOOL = new Block(NamespaceID.from("minecraft:purple_wool"), (short) 1394);

  public static final Block BLUE_WOOL = new Block(NamespaceID.from("minecraft:blue_wool"), (short) 1395);

  public static final Block BROWN_WOOL = new Block(NamespaceID.from("minecraft:brown_wool"), (short) 1396);

  public static final Block GREEN_WOOL = new Block(NamespaceID.from("minecraft:green_wool"), (short) 1397);

  public static final Block RED_WOOL = new Block(NamespaceID.from("minecraft:red_wool"), (short) 1398);

  public static final Block BLACK_WOOL = new Block(NamespaceID.from("minecraft:black_wool"), (short) 1399);

  public static final Block MOVING_PISTON = new Block(NamespaceID.from("minecraft:moving_piston"), (short) 1400);

  public static final Block DANDELION = new Block(NamespaceID.from("minecraft:dandelion"), (short) 1412);

  public static final Block POPPY = new Block(NamespaceID.from("minecraft:poppy"), (short) 1413);

  public static final Block BLUE_ORCHID = new Block(NamespaceID.from("minecraft:blue_orchid"), (short) 1414);

  public static final Block ALLIUM = new Block(NamespaceID.from("minecraft:allium"), (short) 1415);

  public static final Block AZURE_BLUET = new Block(NamespaceID.from("minecraft:azure_bluet"), (short) 1416);

  public static final Block RED_TULIP = new Block(NamespaceID.from("minecraft:red_tulip"), (short) 1417);

  public static final Block ORANGE_TULIP = new Block(NamespaceID.from("minecraft:orange_tulip"), (short) 1418);

  public static final Block WHITE_TULIP = new Block(NamespaceID.from("minecraft:white_tulip"), (short) 1419);

  public static final Block PINK_TULIP = new Block(NamespaceID.from("minecraft:pink_tulip"), (short) 1420);

  public static final Block OXEYE_DAISY = new Block(NamespaceID.from("minecraft:oxeye_daisy"), (short) 1421);

  public static final Block CORNFLOWER = new Block(NamespaceID.from("minecraft:cornflower"), (short) 1422);

  public static final Block WITHER_ROSE = new Block(NamespaceID.from("minecraft:wither_rose"), (short) 1423);

  public static final Block LILY_OF_THE_VALLEY = new Block(NamespaceID.from("minecraft:lily_of_the_valley"), (short) 1424);

  public static final Block BROWN_MUSHROOM = new Block(NamespaceID.from("minecraft:brown_mushroom"), (short) 1425);

  public static final Block RED_MUSHROOM = new Block(NamespaceID.from("minecraft:red_mushroom"), (short) 1426);

  public static final Block GOLD_BLOCK = new Block(NamespaceID.from("minecraft:gold_block"), (short) 1427);

  public static final Block IRON_BLOCK = new Block(NamespaceID.from("minecraft:iron_block"), (short) 1428);

  public static final Block BRICKS = new Block(NamespaceID.from("minecraft:bricks"), (short) 1429);

  public static final Block TNT = new Block(NamespaceID.from("minecraft:tnt"), (short) 1431);

  public static final Block BOOKSHELF = new Block(NamespaceID.from("minecraft:bookshelf"), (short) 1432);

  public static final Block MOSSY_COBBLESTONE = new Block(NamespaceID.from("minecraft:mossy_cobblestone"), (short) 1433);

  public static final Block OBSIDIAN = new Block(NamespaceID.from("minecraft:obsidian"), (short) 1434);

  public static final Block TORCH = new Block(NamespaceID.from("minecraft:torch"), (short) 1435);

  public static final Block WALL_TORCH = new Block(NamespaceID.from("minecraft:wall_torch"), (short) 1436);

  public static final Block FIRE = new Block(NamespaceID.from("minecraft:fire"), (short) 1471);

  public static final Block SOUL_FIRE = new Block(NamespaceID.from("minecraft:soul_fire"), (short) 1952);

  public static final Block SPAWNER = new Block(NamespaceID.from("minecraft:spawner"), (short) 1953);

  public static final Block OAK_STAIRS = new Block(NamespaceID.from("minecraft:oak_stairs"), (short) 1965);

  public static final Block CHEST = new Block(NamespaceID.from("minecraft:chest"), (short) 2035);

  public static final Block REDSTONE_WIRE = new Block(NamespaceID.from("minecraft:redstone_wire"), (short) 3218);

  public static final Block DIAMOND_ORE = new Block(NamespaceID.from("minecraft:diamond_ore"), (short) 3354);

  public static final Block DIAMOND_BLOCK = new Block(NamespaceID.from("minecraft:diamond_block"), (short) 3355);

  public static final Block CRAFTING_TABLE = new Block(NamespaceID.from("minecraft:crafting_table"), (short) 3356);

  public static final Block WHEAT = new Block(NamespaceID.from("minecraft:wheat"), (short) 3357);

  public static final Block FARMLAND = new Block(NamespaceID.from("minecraft:farmland"), (short) 3365);

  public static final Block FURNACE = new Block(NamespaceID.from("minecraft:furnace"), (short) 3374);

  public static final Block OAK_SIGN = new Block(NamespaceID.from("minecraft:oak_sign"), (short) 3382);

  public static final Block SPRUCE_SIGN = new Block(NamespaceID.from("minecraft:spruce_sign"), (short) 3414);

  public static final Block BIRCH_SIGN = new Block(NamespaceID.from("minecraft:birch_sign"), (short) 3446);

  public static final Block ACACIA_SIGN = new Block(NamespaceID.from("minecraft:acacia_sign"), (short) 3478);

  public static final Block JUNGLE_SIGN = new Block(NamespaceID.from("minecraft:jungle_sign"), (short) 3510);

  public static final Block DARK_OAK_SIGN = new Block(NamespaceID.from("minecraft:dark_oak_sign"), (short) 3542);

  public static final Block OAK_DOOR = new Block(NamespaceID.from("minecraft:oak_door"), (short) 3584);

  public static final Block LADDER = new Block(NamespaceID.from("minecraft:ladder"), (short) 3638);

  public static final Block RAIL = new Block(NamespaceID.from("minecraft:rail"), (short) 3645);

  public static final Block COBBLESTONE_STAIRS = new Block(NamespaceID.from("minecraft:cobblestone_stairs"), (short) 3666);

  public static final Block OAK_WALL_SIGN = new Block(NamespaceID.from("minecraft:oak_wall_sign"), (short) 3736);

  public static final Block SPRUCE_WALL_SIGN = new Block(NamespaceID.from("minecraft:spruce_wall_sign"), (short) 3744);

  public static final Block BIRCH_WALL_SIGN = new Block(NamespaceID.from("minecraft:birch_wall_sign"), (short) 3752);

  public static final Block ACACIA_WALL_SIGN = new Block(NamespaceID.from("minecraft:acacia_wall_sign"), (short) 3760);

  public static final Block JUNGLE_WALL_SIGN = new Block(NamespaceID.from("minecraft:jungle_wall_sign"), (short) 3768);

  public static final Block DARK_OAK_WALL_SIGN = new Block(NamespaceID.from("minecraft:dark_oak_wall_sign"), (short) 3776);

  public static final Block LEVER = new Block(NamespaceID.from("minecraft:lever"), (short) 3792);

  public static final Block STONE_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:stone_pressure_plate"), (short) 3808);

  public static final Block IRON_DOOR = new Block(NamespaceID.from("minecraft:iron_door"), (short) 3820);

  public static final Block OAK_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:oak_pressure_plate"), (short) 3874);

  public static final Block SPRUCE_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:spruce_pressure_plate"), (short) 3876);

  public static final Block BIRCH_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:birch_pressure_plate"), (short) 3878);

  public static final Block JUNGLE_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:jungle_pressure_plate"), (short) 3880);

  public static final Block ACACIA_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:acacia_pressure_plate"), (short) 3882);

  public static final Block DARK_OAK_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:dark_oak_pressure_plate"), (short) 3884);

  public static final Block REDSTONE_ORE = new Block(NamespaceID.from("minecraft:redstone_ore"), (short) 3886);

  public static final Block REDSTONE_TORCH = new Block(NamespaceID.from("minecraft:redstone_torch"), (short) 3887);

  public static final Block REDSTONE_WALL_TORCH = new Block(NamespaceID.from("minecraft:redstone_wall_torch"), (short) 3889);

  public static final Block STONE_BUTTON = new Block(NamespaceID.from("minecraft:stone_button"), (short) 3906);

  public static final Block SNOW = new Block(NamespaceID.from("minecraft:snow"), (short) 3921);

  public static final Block ICE = new Block(NamespaceID.from("minecraft:ice"), (short) 3929);

  public static final Block SNOW_BLOCK = new Block(NamespaceID.from("minecraft:snow_block"), (short) 3930);

  public static final Block CACTUS = new Block(NamespaceID.from("minecraft:cactus"), (short) 3931);

  public static final Block CLAY = new Block(NamespaceID.from("minecraft:clay"), (short) 3947);

  public static final Block SUGAR_CANE = new Block(NamespaceID.from("minecraft:sugar_cane"), (short) 3948);

  public static final Block JUKEBOX = new Block(NamespaceID.from("minecraft:jukebox"), (short) 3965);

  public static final Block OAK_FENCE = new Block(NamespaceID.from("minecraft:oak_fence"), (short) 3997);

  public static final Block PUMPKIN = new Block(NamespaceID.from("minecraft:pumpkin"), (short) 3998);

  public static final Block NETHERRACK = new Block(NamespaceID.from("minecraft:netherrack"), (short) 3999);

  public static final Block SOUL_SAND = new Block(NamespaceID.from("minecraft:soul_sand"), (short) 4000);

  public static final Block SOUL_SOIL = new Block(NamespaceID.from("minecraft:soul_soil"), (short) 4001);

  public static final Block BASALT = new Block(NamespaceID.from("minecraft:basalt"), (short) 4003);

  public static final Block POLISHED_BASALT = new Block(NamespaceID.from("minecraft:polished_basalt"), (short) 4006);

  public static final Block SOUL_TORCH = new Block(NamespaceID.from("minecraft:soul_torch"), (short) 4008);

  public static final Block SOUL_WALL_TORCH = new Block(NamespaceID.from("minecraft:soul_wall_torch"), (short) 4009);

  public static final Block GLOWSTONE = new Block(NamespaceID.from("minecraft:glowstone"), (short) 4013);

  public static final Block NETHER_PORTAL = new Block(NamespaceID.from("minecraft:nether_portal"), (short) 4014);

  public static final Block CARVED_PUMPKIN = new Block(NamespaceID.from("minecraft:carved_pumpkin"), (short) 4016);

  public static final Block JACK_O_LANTERN = new Block(NamespaceID.from("minecraft:jack_o_lantern"), (short) 4020);

  public static final Block CAKE = new Block(NamespaceID.from("minecraft:cake"), (short) 4024);

  public static final Block REPEATER = new Block(NamespaceID.from("minecraft:repeater"), (short) 4034);

  public static final Block WHITE_STAINED_GLASS = new Block(NamespaceID.from("minecraft:white_stained_glass"), (short) 4095);

  public static final Block ORANGE_STAINED_GLASS = new Block(NamespaceID.from("minecraft:orange_stained_glass"), (short) 4096);

  public static final Block MAGENTA_STAINED_GLASS = new Block(NamespaceID.from("minecraft:magenta_stained_glass"), (short) 4097);

  public static final Block LIGHT_BLUE_STAINED_GLASS = new Block(NamespaceID.from("minecraft:light_blue_stained_glass"), (short) 4098);

  public static final Block YELLOW_STAINED_GLASS = new Block(NamespaceID.from("minecraft:yellow_stained_glass"), (short) 4099);

  public static final Block LIME_STAINED_GLASS = new Block(NamespaceID.from("minecraft:lime_stained_glass"), (short) 4100);

  public static final Block PINK_STAINED_GLASS = new Block(NamespaceID.from("minecraft:pink_stained_glass"), (short) 4101);

  public static final Block GRAY_STAINED_GLASS = new Block(NamespaceID.from("minecraft:gray_stained_glass"), (short) 4102);

  public static final Block LIGHT_GRAY_STAINED_GLASS = new Block(NamespaceID.from("minecraft:light_gray_stained_glass"), (short) 4103);

  public static final Block CYAN_STAINED_GLASS = new Block(NamespaceID.from("minecraft:cyan_stained_glass"), (short) 4104);

  public static final Block PURPLE_STAINED_GLASS = new Block(NamespaceID.from("minecraft:purple_stained_glass"), (short) 4105);

  public static final Block BLUE_STAINED_GLASS = new Block(NamespaceID.from("minecraft:blue_stained_glass"), (short) 4106);

  public static final Block BROWN_STAINED_GLASS = new Block(NamespaceID.from("minecraft:brown_stained_glass"), (short) 4107);

  public static final Block GREEN_STAINED_GLASS = new Block(NamespaceID.from("minecraft:green_stained_glass"), (short) 4108);

  public static final Block RED_STAINED_GLASS = new Block(NamespaceID.from("minecraft:red_stained_glass"), (short) 4109);

  public static final Block BLACK_STAINED_GLASS = new Block(NamespaceID.from("minecraft:black_stained_glass"), (short) 4110);

  public static final Block OAK_TRAPDOOR = new Block(NamespaceID.from("minecraft:oak_trapdoor"), (short) 4126);

  public static final Block SPRUCE_TRAPDOOR = new Block(NamespaceID.from("minecraft:spruce_trapdoor"), (short) 4190);

  public static final Block BIRCH_TRAPDOOR = new Block(NamespaceID.from("minecraft:birch_trapdoor"), (short) 4254);

  public static final Block JUNGLE_TRAPDOOR = new Block(NamespaceID.from("minecraft:jungle_trapdoor"), (short) 4318);

  public static final Block ACACIA_TRAPDOOR = new Block(NamespaceID.from("minecraft:acacia_trapdoor"), (short) 4382);

  public static final Block DARK_OAK_TRAPDOOR = new Block(NamespaceID.from("minecraft:dark_oak_trapdoor"), (short) 4446);

  public static final Block STONE_BRICKS = new Block(NamespaceID.from("minecraft:stone_bricks"), (short) 4495);

  public static final Block MOSSY_STONE_BRICKS = new Block(NamespaceID.from("minecraft:mossy_stone_bricks"), (short) 4496);

  public static final Block CRACKED_STONE_BRICKS = new Block(NamespaceID.from("minecraft:cracked_stone_bricks"), (short) 4497);

  public static final Block CHISELED_STONE_BRICKS = new Block(NamespaceID.from("minecraft:chiseled_stone_bricks"), (short) 4498);

  public static final Block INFESTED_STONE = new Block(NamespaceID.from("minecraft:infested_stone"), (short) 4499);

  public static final Block INFESTED_COBBLESTONE = new Block(NamespaceID.from("minecraft:infested_cobblestone"), (short) 4500);

  public static final Block INFESTED_STONE_BRICKS = new Block(NamespaceID.from("minecraft:infested_stone_bricks"), (short) 4501);

  public static final Block INFESTED_MOSSY_STONE_BRICKS = new Block(NamespaceID.from("minecraft:infested_mossy_stone_bricks"), (short) 4502);

  public static final Block INFESTED_CRACKED_STONE_BRICKS = new Block(NamespaceID.from("minecraft:infested_cracked_stone_bricks"), (short) 4503);

  public static final Block INFESTED_CHISELED_STONE_BRICKS = new Block(NamespaceID.from("minecraft:infested_chiseled_stone_bricks"), (short) 4504);

  public static final Block BROWN_MUSHROOM_BLOCK = new Block(NamespaceID.from("minecraft:brown_mushroom_block"), (short) 4505);

  public static final Block RED_MUSHROOM_BLOCK = new Block(NamespaceID.from("minecraft:red_mushroom_block"), (short) 4569);

  public static final Block MUSHROOM_STEM = new Block(NamespaceID.from("minecraft:mushroom_stem"), (short) 4633);

  public static final Block IRON_BARS = new Block(NamespaceID.from("minecraft:iron_bars"), (short) 4728);

  public static final Block CHAIN = new Block(NamespaceID.from("minecraft:chain"), (short) 4732);

  public static final Block GLASS_PANE = new Block(NamespaceID.from("minecraft:glass_pane"), (short) 4766);

  public static final Block MELON = new Block(NamespaceID.from("minecraft:melon"), (short) 4767);

  public static final Block ATTACHED_PUMPKIN_STEM = new Block(NamespaceID.from("minecraft:attached_pumpkin_stem"), (short) 4768);

  public static final Block ATTACHED_MELON_STEM = new Block(NamespaceID.from("minecraft:attached_melon_stem"), (short) 4772);

  public static final Block PUMPKIN_STEM = new Block(NamespaceID.from("minecraft:pumpkin_stem"), (short) 4776);

  public static final Block MELON_STEM = new Block(NamespaceID.from("minecraft:melon_stem"), (short) 4784);

  public static final Block VINE = new Block(NamespaceID.from("minecraft:vine"), (short) 4823);

  public static final Block OAK_FENCE_GATE = new Block(NamespaceID.from("minecraft:oak_fence_gate"), (short) 4831);

  public static final Block BRICK_STAIRS = new Block(NamespaceID.from("minecraft:brick_stairs"), (short) 4867);

  public static final Block STONE_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:stone_brick_stairs"), (short) 4947);

  public static final Block MYCELIUM = new Block(NamespaceID.from("minecraft:mycelium"), (short) 5017);

  public static final Block LILY_PAD = new Block(NamespaceID.from("minecraft:lily_pad"), (short) 5018);

  public static final Block NETHER_BRICKS = new Block(NamespaceID.from("minecraft:nether_bricks"), (short) 5019);

  public static final Block NETHER_BRICK_FENCE = new Block(NamespaceID.from("minecraft:nether_brick_fence"), (short) 5051);

  public static final Block NETHER_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:nether_brick_stairs"), (short) 5063);

  public static final Block NETHER_WART = new Block(NamespaceID.from("minecraft:nether_wart"), (short) 5132);

  public static final Block ENCHANTING_TABLE = new Block(NamespaceID.from("minecraft:enchanting_table"), (short) 5136);

  public static final Block BREWING_STAND = new Block(NamespaceID.from("minecraft:brewing_stand"), (short) 5144);

  public static final Block CAULDRON = new Block(NamespaceID.from("minecraft:cauldron"), (short) 5145);

  public static final Block END_PORTAL = new Block(NamespaceID.from("minecraft:end_portal"), (short) 5149);

  public static final Block END_PORTAL_FRAME = new Block(NamespaceID.from("minecraft:end_portal_frame"), (short) 5154);

  public static final Block END_STONE = new Block(NamespaceID.from("minecraft:end_stone"), (short) 5158);

  public static final Block DRAGON_EGG = new Block(NamespaceID.from("minecraft:dragon_egg"), (short) 5159);

  public static final Block REDSTONE_LAMP = new Block(NamespaceID.from("minecraft:redstone_lamp"), (short) 5161);

  public static final Block COCOA = new Block(NamespaceID.from("minecraft:cocoa"), (short) 5162);

  public static final Block SANDSTONE_STAIRS = new Block(NamespaceID.from("minecraft:sandstone_stairs"), (short) 5185);

  public static final Block EMERALD_ORE = new Block(NamespaceID.from("minecraft:emerald_ore"), (short) 5254);

  public static final Block ENDER_CHEST = new Block(NamespaceID.from("minecraft:ender_chest"), (short) 5256);

  public static final Block TRIPWIRE_HOOK = new Block(NamespaceID.from("minecraft:tripwire_hook"), (short) 5272);

  public static final Block TRIPWIRE = new Block(NamespaceID.from("minecraft:tripwire"), (short) 5406);

  public static final Block EMERALD_BLOCK = new Block(NamespaceID.from("minecraft:emerald_block"), (short) 5407);

  public static final Block SPRUCE_STAIRS = new Block(NamespaceID.from("minecraft:spruce_stairs"), (short) 5419);

  public static final Block BIRCH_STAIRS = new Block(NamespaceID.from("minecraft:birch_stairs"), (short) 5499);

  public static final Block JUNGLE_STAIRS = new Block(NamespaceID.from("minecraft:jungle_stairs"), (short) 5579);

  public static final Block COMMAND_BLOCK = new Block(NamespaceID.from("minecraft:command_block"), (short) 5654);

  public static final Block BEACON = new Block(NamespaceID.from("minecraft:beacon"), (short) 5660);

  public static final Block COBBLESTONE_WALL = new Block(NamespaceID.from("minecraft:cobblestone_wall"), (short) 5664);

  public static final Block MOSSY_COBBLESTONE_WALL = new Block(NamespaceID.from("minecraft:mossy_cobblestone_wall"), (short) 5988);

  public static final Block FLOWER_POT = new Block(NamespaceID.from("minecraft:flower_pot"), (short) 6309);

  public static final Block POTTED_OAK_SAPLING = new Block(NamespaceID.from("minecraft:potted_oak_sapling"), (short) 6310);

  public static final Block POTTED_SPRUCE_SAPLING = new Block(NamespaceID.from("minecraft:potted_spruce_sapling"), (short) 6311);

  public static final Block POTTED_BIRCH_SAPLING = new Block(NamespaceID.from("minecraft:potted_birch_sapling"), (short) 6312);

  public static final Block POTTED_JUNGLE_SAPLING = new Block(NamespaceID.from("minecraft:potted_jungle_sapling"), (short) 6313);

  public static final Block POTTED_ACACIA_SAPLING = new Block(NamespaceID.from("minecraft:potted_acacia_sapling"), (short) 6314);

  public static final Block POTTED_DARK_OAK_SAPLING = new Block(NamespaceID.from("minecraft:potted_dark_oak_sapling"), (short) 6315);

  public static final Block POTTED_FERN = new Block(NamespaceID.from("minecraft:potted_fern"), (short) 6316);

  public static final Block POTTED_DANDELION = new Block(NamespaceID.from("minecraft:potted_dandelion"), (short) 6317);

  public static final Block POTTED_POPPY = new Block(NamespaceID.from("minecraft:potted_poppy"), (short) 6318);

  public static final Block POTTED_BLUE_ORCHID = new Block(NamespaceID.from("minecraft:potted_blue_orchid"), (short) 6319);

  public static final Block POTTED_ALLIUM = new Block(NamespaceID.from("minecraft:potted_allium"), (short) 6320);

  public static final Block POTTED_AZURE_BLUET = new Block(NamespaceID.from("minecraft:potted_azure_bluet"), (short) 6321);

  public static final Block POTTED_RED_TULIP = new Block(NamespaceID.from("minecraft:potted_red_tulip"), (short) 6322);

  public static final Block POTTED_ORANGE_TULIP = new Block(NamespaceID.from("minecraft:potted_orange_tulip"), (short) 6323);

  public static final Block POTTED_WHITE_TULIP = new Block(NamespaceID.from("minecraft:potted_white_tulip"), (short) 6324);

  public static final Block POTTED_PINK_TULIP = new Block(NamespaceID.from("minecraft:potted_pink_tulip"), (short) 6325);

  public static final Block POTTED_OXEYE_DAISY = new Block(NamespaceID.from("minecraft:potted_oxeye_daisy"), (short) 6326);

  public static final Block POTTED_CORNFLOWER = new Block(NamespaceID.from("minecraft:potted_cornflower"), (short) 6327);

  public static final Block POTTED_LILY_OF_THE_VALLEY = new Block(NamespaceID.from("minecraft:potted_lily_of_the_valley"), (short) 6328);

  public static final Block POTTED_WITHER_ROSE = new Block(NamespaceID.from("minecraft:potted_wither_rose"), (short) 6329);

  public static final Block POTTED_RED_MUSHROOM = new Block(NamespaceID.from("minecraft:potted_red_mushroom"), (short) 6330);

  public static final Block POTTED_BROWN_MUSHROOM = new Block(NamespaceID.from("minecraft:potted_brown_mushroom"), (short) 6331);

  public static final Block POTTED_DEAD_BUSH = new Block(NamespaceID.from("minecraft:potted_dead_bush"), (short) 6332);

  public static final Block POTTED_CACTUS = new Block(NamespaceID.from("minecraft:potted_cactus"), (short) 6333);

  public static final Block CARROTS = new Block(NamespaceID.from("minecraft:carrots"), (short) 6334);

  public static final Block POTATOES = new Block(NamespaceID.from("minecraft:potatoes"), (short) 6342);

  public static final Block OAK_BUTTON = new Block(NamespaceID.from("minecraft:oak_button"), (short) 6359);

  public static final Block SPRUCE_BUTTON = new Block(NamespaceID.from("minecraft:spruce_button"), (short) 6383);

  public static final Block BIRCH_BUTTON = new Block(NamespaceID.from("minecraft:birch_button"), (short) 6407);

  public static final Block JUNGLE_BUTTON = new Block(NamespaceID.from("minecraft:jungle_button"), (short) 6431);

  public static final Block ACACIA_BUTTON = new Block(NamespaceID.from("minecraft:acacia_button"), (short) 6455);

  public static final Block DARK_OAK_BUTTON = new Block(NamespaceID.from("minecraft:dark_oak_button"), (short) 6479);

  public static final Block SKELETON_SKULL = new Block(NamespaceID.from("minecraft:skeleton_skull"), (short) 6494);

  public static final Block SKELETON_WALL_SKULL = new Block(NamespaceID.from("minecraft:skeleton_wall_skull"), (short) 6510);

  public static final Block WITHER_SKELETON_SKULL = new Block(NamespaceID.from("minecraft:wither_skeleton_skull"), (short) 6514);

  public static final Block WITHER_SKELETON_WALL_SKULL = new Block(NamespaceID.from("minecraft:wither_skeleton_wall_skull"), (short) 6530);

  public static final Block ZOMBIE_HEAD = new Block(NamespaceID.from("minecraft:zombie_head"), (short) 6534);

  public static final Block ZOMBIE_WALL_HEAD = new Block(NamespaceID.from("minecraft:zombie_wall_head"), (short) 6550);

  public static final Block PLAYER_HEAD = new Block(NamespaceID.from("minecraft:player_head"), (short) 6554);

  public static final Block PLAYER_WALL_HEAD = new Block(NamespaceID.from("minecraft:player_wall_head"), (short) 6570);

  public static final Block CREEPER_HEAD = new Block(NamespaceID.from("minecraft:creeper_head"), (short) 6574);

  public static final Block CREEPER_WALL_HEAD = new Block(NamespaceID.from("minecraft:creeper_wall_head"), (short) 6590);

  public static final Block DRAGON_HEAD = new Block(NamespaceID.from("minecraft:dragon_head"), (short) 6594);

  public static final Block DRAGON_WALL_HEAD = new Block(NamespaceID.from("minecraft:dragon_wall_head"), (short) 6610);

  public static final Block ANVIL = new Block(NamespaceID.from("minecraft:anvil"), (short) 6614);

  public static final Block CHIPPED_ANVIL = new Block(NamespaceID.from("minecraft:chipped_anvil"), (short) 6618);

  public static final Block DAMAGED_ANVIL = new Block(NamespaceID.from("minecraft:damaged_anvil"), (short) 6622);

  public static final Block TRAPPED_CHEST = new Block(NamespaceID.from("minecraft:trapped_chest"), (short) 6627);

  public static final Block LIGHT_WEIGHTED_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:light_weighted_pressure_plate"), (short) 6650);

  public static final Block HEAVY_WEIGHTED_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:heavy_weighted_pressure_plate"), (short) 6666);

  public static final Block COMPARATOR = new Block(NamespaceID.from("minecraft:comparator"), (short) 6683);

  public static final Block DAYLIGHT_DETECTOR = new Block(NamespaceID.from("minecraft:daylight_detector"), (short) 6714);

  public static final Block REDSTONE_BLOCK = new Block(NamespaceID.from("minecraft:redstone_block"), (short) 6730);

  public static final Block NETHER_QUARTZ_ORE = new Block(NamespaceID.from("minecraft:nether_quartz_ore"), (short) 6731);

  public static final Block HOPPER = new Block(NamespaceID.from("minecraft:hopper"), (short) 6732);

  public static final Block QUARTZ_BLOCK = new Block(NamespaceID.from("minecraft:quartz_block"), (short) 6742);

  public static final Block CHISELED_QUARTZ_BLOCK = new Block(NamespaceID.from("minecraft:chiseled_quartz_block"), (short) 6743);

  public static final Block QUARTZ_PILLAR = new Block(NamespaceID.from("minecraft:quartz_pillar"), (short) 6745);

  public static final Block QUARTZ_STAIRS = new Block(NamespaceID.from("minecraft:quartz_stairs"), (short) 6758);

  public static final Block ACTIVATOR_RAIL = new Block(NamespaceID.from("minecraft:activator_rail"), (short) 6833);

  public static final Block DROPPER = new Block(NamespaceID.from("minecraft:dropper"), (short) 6840);

  public static final Block WHITE_TERRACOTTA = new Block(NamespaceID.from("minecraft:white_terracotta"), (short) 6851);

  public static final Block ORANGE_TERRACOTTA = new Block(NamespaceID.from("minecraft:orange_terracotta"), (short) 6852);

  public static final Block MAGENTA_TERRACOTTA = new Block(NamespaceID.from("minecraft:magenta_terracotta"), (short) 6853);

  public static final Block LIGHT_BLUE_TERRACOTTA = new Block(NamespaceID.from("minecraft:light_blue_terracotta"), (short) 6854);

  public static final Block YELLOW_TERRACOTTA = new Block(NamespaceID.from("minecraft:yellow_terracotta"), (short) 6855);

  public static final Block LIME_TERRACOTTA = new Block(NamespaceID.from("minecraft:lime_terracotta"), (short) 6856);

  public static final Block PINK_TERRACOTTA = new Block(NamespaceID.from("minecraft:pink_terracotta"), (short) 6857);

  public static final Block GRAY_TERRACOTTA = new Block(NamespaceID.from("minecraft:gray_terracotta"), (short) 6858);

  public static final Block LIGHT_GRAY_TERRACOTTA = new Block(NamespaceID.from("minecraft:light_gray_terracotta"), (short) 6859);

  public static final Block CYAN_TERRACOTTA = new Block(NamespaceID.from("minecraft:cyan_terracotta"), (short) 6860);

  public static final Block PURPLE_TERRACOTTA = new Block(NamespaceID.from("minecraft:purple_terracotta"), (short) 6861);

  public static final Block BLUE_TERRACOTTA = new Block(NamespaceID.from("minecraft:blue_terracotta"), (short) 6862);

  public static final Block BROWN_TERRACOTTA = new Block(NamespaceID.from("minecraft:brown_terracotta"), (short) 6863);

  public static final Block GREEN_TERRACOTTA = new Block(NamespaceID.from("minecraft:green_terracotta"), (short) 6864);

  public static final Block RED_TERRACOTTA = new Block(NamespaceID.from("minecraft:red_terracotta"), (short) 6865);

  public static final Block BLACK_TERRACOTTA = new Block(NamespaceID.from("minecraft:black_terracotta"), (short) 6866);

  public static final Block WHITE_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:white_stained_glass_pane"), (short) 6898);

  public static final Block ORANGE_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:orange_stained_glass_pane"), (short) 6930);

  public static final Block MAGENTA_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:magenta_stained_glass_pane"), (short) 6962);

  public static final Block LIGHT_BLUE_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:light_blue_stained_glass_pane"), (short) 6994);

  public static final Block YELLOW_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:yellow_stained_glass_pane"), (short) 7026);

  public static final Block LIME_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:lime_stained_glass_pane"), (short) 7058);

  public static final Block PINK_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:pink_stained_glass_pane"), (short) 7090);

  public static final Block GRAY_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:gray_stained_glass_pane"), (short) 7122);

  public static final Block LIGHT_GRAY_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:light_gray_stained_glass_pane"), (short) 7154);

  public static final Block CYAN_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:cyan_stained_glass_pane"), (short) 7186);

  public static final Block PURPLE_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:purple_stained_glass_pane"), (short) 7218);

  public static final Block BLUE_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:blue_stained_glass_pane"), (short) 7250);

  public static final Block BROWN_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:brown_stained_glass_pane"), (short) 7282);

  public static final Block GREEN_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:green_stained_glass_pane"), (short) 7314);

  public static final Block RED_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:red_stained_glass_pane"), (short) 7346);

  public static final Block BLACK_STAINED_GLASS_PANE = new Block(NamespaceID.from("minecraft:black_stained_glass_pane"), (short) 7378);

  public static final Block ACACIA_STAIRS = new Block(NamespaceID.from("minecraft:acacia_stairs"), (short) 7390);

  public static final Block DARK_OAK_STAIRS = new Block(NamespaceID.from("minecraft:dark_oak_stairs"), (short) 7470);

  public static final Block SLIME_BLOCK = new Block(NamespaceID.from("minecraft:slime_block"), (short) 7539);

  public static final Block BARRIER = new Block(NamespaceID.from("minecraft:barrier"), (short) 7540);

  public static final Block IRON_TRAPDOOR = new Block(NamespaceID.from("minecraft:iron_trapdoor"), (short) 7556);

  public static final Block PRISMARINE = new Block(NamespaceID.from("minecraft:prismarine"), (short) 7605);

  public static final Block PRISMARINE_BRICKS = new Block(NamespaceID.from("minecraft:prismarine_bricks"), (short) 7606);

  public static final Block DARK_PRISMARINE = new Block(NamespaceID.from("minecraft:dark_prismarine"), (short) 7607);

  public static final Block PRISMARINE_STAIRS = new Block(NamespaceID.from("minecraft:prismarine_stairs"), (short) 7619);

  public static final Block PRISMARINE_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:prismarine_brick_stairs"), (short) 7699);

  public static final Block DARK_PRISMARINE_STAIRS = new Block(NamespaceID.from("minecraft:dark_prismarine_stairs"), (short) 7779);

  public static final Block PRISMARINE_SLAB = new Block(NamespaceID.from("minecraft:prismarine_slab"), (short) 7851);

  public static final Block PRISMARINE_BRICK_SLAB = new Block(NamespaceID.from("minecraft:prismarine_brick_slab"), (short) 7857);

  public static final Block DARK_PRISMARINE_SLAB = new Block(NamespaceID.from("minecraft:dark_prismarine_slab"), (short) 7863);

  public static final Block SEA_LANTERN = new Block(NamespaceID.from("minecraft:sea_lantern"), (short) 7866);

  public static final Block HAY_BLOCK = new Block(NamespaceID.from("minecraft:hay_block"), (short) 7868);

  public static final Block WHITE_CARPET = new Block(NamespaceID.from("minecraft:white_carpet"), (short) 7870);

  public static final Block ORANGE_CARPET = new Block(NamespaceID.from("minecraft:orange_carpet"), (short) 7871);

  public static final Block MAGENTA_CARPET = new Block(NamespaceID.from("minecraft:magenta_carpet"), (short) 7872);

  public static final Block LIGHT_BLUE_CARPET = new Block(NamespaceID.from("minecraft:light_blue_carpet"), (short) 7873);

  public static final Block YELLOW_CARPET = new Block(NamespaceID.from("minecraft:yellow_carpet"), (short) 7874);

  public static final Block LIME_CARPET = new Block(NamespaceID.from("minecraft:lime_carpet"), (short) 7875);

  public static final Block PINK_CARPET = new Block(NamespaceID.from("minecraft:pink_carpet"), (short) 7876);

  public static final Block GRAY_CARPET = new Block(NamespaceID.from("minecraft:gray_carpet"), (short) 7877);

  public static final Block LIGHT_GRAY_CARPET = new Block(NamespaceID.from("minecraft:light_gray_carpet"), (short) 7878);

  public static final Block CYAN_CARPET = new Block(NamespaceID.from("minecraft:cyan_carpet"), (short) 7879);

  public static final Block PURPLE_CARPET = new Block(NamespaceID.from("minecraft:purple_carpet"), (short) 7880);

  public static final Block BLUE_CARPET = new Block(NamespaceID.from("minecraft:blue_carpet"), (short) 7881);

  public static final Block BROWN_CARPET = new Block(NamespaceID.from("minecraft:brown_carpet"), (short) 7882);

  public static final Block GREEN_CARPET = new Block(NamespaceID.from("minecraft:green_carpet"), (short) 7883);

  public static final Block RED_CARPET = new Block(NamespaceID.from("minecraft:red_carpet"), (short) 7884);

  public static final Block BLACK_CARPET = new Block(NamespaceID.from("minecraft:black_carpet"), (short) 7885);

  public static final Block TERRACOTTA = new Block(NamespaceID.from("minecraft:terracotta"), (short) 7886);

  public static final Block COAL_BLOCK = new Block(NamespaceID.from("minecraft:coal_block"), (short) 7887);

  public static final Block PACKED_ICE = new Block(NamespaceID.from("minecraft:packed_ice"), (short) 7888);

  public static final Block SUNFLOWER = new Block(NamespaceID.from("minecraft:sunflower"), (short) 7890);

  public static final Block LILAC = new Block(NamespaceID.from("minecraft:lilac"), (short) 7892);

  public static final Block ROSE_BUSH = new Block(NamespaceID.from("minecraft:rose_bush"), (short) 7894);

  public static final Block PEONY = new Block(NamespaceID.from("minecraft:peony"), (short) 7896);

  public static final Block TALL_GRASS = new Block(NamespaceID.from("minecraft:tall_grass"), (short) 7898);

  public static final Block LARGE_FERN = new Block(NamespaceID.from("minecraft:large_fern"), (short) 7900);

  public static final Block WHITE_BANNER = new Block(NamespaceID.from("minecraft:white_banner"), (short) 7901);

  public static final Block ORANGE_BANNER = new Block(NamespaceID.from("minecraft:orange_banner"), (short) 7917);

  public static final Block MAGENTA_BANNER = new Block(NamespaceID.from("minecraft:magenta_banner"), (short) 7933);

  public static final Block LIGHT_BLUE_BANNER = new Block(NamespaceID.from("minecraft:light_blue_banner"), (short) 7949);

  public static final Block YELLOW_BANNER = new Block(NamespaceID.from("minecraft:yellow_banner"), (short) 7965);

  public static final Block LIME_BANNER = new Block(NamespaceID.from("minecraft:lime_banner"), (short) 7981);

  public static final Block PINK_BANNER = new Block(NamespaceID.from("minecraft:pink_banner"), (short) 7997);

  public static final Block GRAY_BANNER = new Block(NamespaceID.from("minecraft:gray_banner"), (short) 8013);

  public static final Block LIGHT_GRAY_BANNER = new Block(NamespaceID.from("minecraft:light_gray_banner"), (short) 8029);

  public static final Block CYAN_BANNER = new Block(NamespaceID.from("minecraft:cyan_banner"), (short) 8045);

  public static final Block PURPLE_BANNER = new Block(NamespaceID.from("minecraft:purple_banner"), (short) 8061);

  public static final Block BLUE_BANNER = new Block(NamespaceID.from("minecraft:blue_banner"), (short) 8077);

  public static final Block BROWN_BANNER = new Block(NamespaceID.from("minecraft:brown_banner"), (short) 8093);

  public static final Block GREEN_BANNER = new Block(NamespaceID.from("minecraft:green_banner"), (short) 8109);

  public static final Block RED_BANNER = new Block(NamespaceID.from("minecraft:red_banner"), (short) 8125);

  public static final Block BLACK_BANNER = new Block(NamespaceID.from("minecraft:black_banner"), (short) 8141);

  public static final Block WHITE_WALL_BANNER = new Block(NamespaceID.from("minecraft:white_wall_banner"), (short) 8157);

  public static final Block ORANGE_WALL_BANNER = new Block(NamespaceID.from("minecraft:orange_wall_banner"), (short) 8161);

  public static final Block MAGENTA_WALL_BANNER = new Block(NamespaceID.from("minecraft:magenta_wall_banner"), (short) 8165);

  public static final Block LIGHT_BLUE_WALL_BANNER = new Block(NamespaceID.from("minecraft:light_blue_wall_banner"), (short) 8169);

  public static final Block YELLOW_WALL_BANNER = new Block(NamespaceID.from("minecraft:yellow_wall_banner"), (short) 8173);

  public static final Block LIME_WALL_BANNER = new Block(NamespaceID.from("minecraft:lime_wall_banner"), (short) 8177);

  public static final Block PINK_WALL_BANNER = new Block(NamespaceID.from("minecraft:pink_wall_banner"), (short) 8181);

  public static final Block GRAY_WALL_BANNER = new Block(NamespaceID.from("minecraft:gray_wall_banner"), (short) 8185);

  public static final Block LIGHT_GRAY_WALL_BANNER = new Block(NamespaceID.from("minecraft:light_gray_wall_banner"), (short) 8189);

  public static final Block CYAN_WALL_BANNER = new Block(NamespaceID.from("minecraft:cyan_wall_banner"), (short) 8193);

  public static final Block PURPLE_WALL_BANNER = new Block(NamespaceID.from("minecraft:purple_wall_banner"), (short) 8197);

  public static final Block BLUE_WALL_BANNER = new Block(NamespaceID.from("minecraft:blue_wall_banner"), (short) 8201);

  public static final Block BROWN_WALL_BANNER = new Block(NamespaceID.from("minecraft:brown_wall_banner"), (short) 8205);

  public static final Block GREEN_WALL_BANNER = new Block(NamespaceID.from("minecraft:green_wall_banner"), (short) 8209);

  public static final Block RED_WALL_BANNER = new Block(NamespaceID.from("minecraft:red_wall_banner"), (short) 8213);

  public static final Block BLACK_WALL_BANNER = new Block(NamespaceID.from("minecraft:black_wall_banner"), (short) 8217);

  public static final Block RED_SANDSTONE = new Block(NamespaceID.from("minecraft:red_sandstone"), (short) 8221);

  public static final Block CHISELED_RED_SANDSTONE = new Block(NamespaceID.from("minecraft:chiseled_red_sandstone"), (short) 8222);

  public static final Block CUT_RED_SANDSTONE = new Block(NamespaceID.from("minecraft:cut_red_sandstone"), (short) 8223);

  public static final Block RED_SANDSTONE_STAIRS = new Block(NamespaceID.from("minecraft:red_sandstone_stairs"), (short) 8235);

  public static final Block OAK_SLAB = new Block(NamespaceID.from("minecraft:oak_slab"), (short) 8307);

  public static final Block SPRUCE_SLAB = new Block(NamespaceID.from("minecraft:spruce_slab"), (short) 8313);

  public static final Block BIRCH_SLAB = new Block(NamespaceID.from("minecraft:birch_slab"), (short) 8319);

  public static final Block JUNGLE_SLAB = new Block(NamespaceID.from("minecraft:jungle_slab"), (short) 8325);

  public static final Block ACACIA_SLAB = new Block(NamespaceID.from("minecraft:acacia_slab"), (short) 8331);

  public static final Block DARK_OAK_SLAB = new Block(NamespaceID.from("minecraft:dark_oak_slab"), (short) 8337);

  public static final Block STONE_SLAB = new Block(NamespaceID.from("minecraft:stone_slab"), (short) 8343);

  public static final Block SMOOTH_STONE_SLAB = new Block(NamespaceID.from("minecraft:smooth_stone_slab"), (short) 8349);

  public static final Block SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:sandstone_slab"), (short) 8355);

  public static final Block CUT_SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:cut_sandstone_slab"), (short) 8361);

  public static final Block PETRIFIED_OAK_SLAB = new Block(NamespaceID.from("minecraft:petrified_oak_slab"), (short) 8367);

  public static final Block COBBLESTONE_SLAB = new Block(NamespaceID.from("minecraft:cobblestone_slab"), (short) 8373);

  public static final Block BRICK_SLAB = new Block(NamespaceID.from("minecraft:brick_slab"), (short) 8379);

  public static final Block STONE_BRICK_SLAB = new Block(NamespaceID.from("minecraft:stone_brick_slab"), (short) 8385);

  public static final Block NETHER_BRICK_SLAB = new Block(NamespaceID.from("minecraft:nether_brick_slab"), (short) 8391);

  public static final Block QUARTZ_SLAB = new Block(NamespaceID.from("minecraft:quartz_slab"), (short) 8397);

  public static final Block RED_SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:red_sandstone_slab"), (short) 8403);

  public static final Block CUT_RED_SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:cut_red_sandstone_slab"), (short) 8409);

  public static final Block PURPUR_SLAB = new Block(NamespaceID.from("minecraft:purpur_slab"), (short) 8415);

  public static final Block SMOOTH_STONE = new Block(NamespaceID.from("minecraft:smooth_stone"), (short) 8418);

  public static final Block SMOOTH_SANDSTONE = new Block(NamespaceID.from("minecraft:smooth_sandstone"), (short) 8419);

  public static final Block SMOOTH_QUARTZ = new Block(NamespaceID.from("minecraft:smooth_quartz"), (short) 8420);

  public static final Block SMOOTH_RED_SANDSTONE = new Block(NamespaceID.from("minecraft:smooth_red_sandstone"), (short) 8421);

  public static final Block SPRUCE_FENCE_GATE = new Block(NamespaceID.from("minecraft:spruce_fence_gate"), (short) 8429);

  public static final Block BIRCH_FENCE_GATE = new Block(NamespaceID.from("minecraft:birch_fence_gate"), (short) 8461);

  public static final Block JUNGLE_FENCE_GATE = new Block(NamespaceID.from("minecraft:jungle_fence_gate"), (short) 8493);

  public static final Block ACACIA_FENCE_GATE = new Block(NamespaceID.from("minecraft:acacia_fence_gate"), (short) 8525);

  public static final Block DARK_OAK_FENCE_GATE = new Block(NamespaceID.from("minecraft:dark_oak_fence_gate"), (short) 8557);

  public static final Block SPRUCE_FENCE = new Block(NamespaceID.from("minecraft:spruce_fence"), (short) 8613);

  public static final Block BIRCH_FENCE = new Block(NamespaceID.from("minecraft:birch_fence"), (short) 8645);

  public static final Block JUNGLE_FENCE = new Block(NamespaceID.from("minecraft:jungle_fence"), (short) 8677);

  public static final Block ACACIA_FENCE = new Block(NamespaceID.from("minecraft:acacia_fence"), (short) 8709);

  public static final Block DARK_OAK_FENCE = new Block(NamespaceID.from("minecraft:dark_oak_fence"), (short) 8741);

  public static final Block SPRUCE_DOOR = new Block(NamespaceID.from("minecraft:spruce_door"), (short) 8753);

  public static final Block BIRCH_DOOR = new Block(NamespaceID.from("minecraft:birch_door"), (short) 8817);

  public static final Block JUNGLE_DOOR = new Block(NamespaceID.from("minecraft:jungle_door"), (short) 8881);

  public static final Block ACACIA_DOOR = new Block(NamespaceID.from("minecraft:acacia_door"), (short) 8945);

  public static final Block DARK_OAK_DOOR = new Block(NamespaceID.from("minecraft:dark_oak_door"), (short) 9009);

  public static final Block END_ROD = new Block(NamespaceID.from("minecraft:end_rod"), (short) 9066);

  public static final Block CHORUS_PLANT = new Block(NamespaceID.from("minecraft:chorus_plant"), (short) 9131);

  public static final Block CHORUS_FLOWER = new Block(NamespaceID.from("minecraft:chorus_flower"), (short) 9132);

  public static final Block PURPUR_BLOCK = new Block(NamespaceID.from("minecraft:purpur_block"), (short) 9138);

  public static final Block PURPUR_PILLAR = new Block(NamespaceID.from("minecraft:purpur_pillar"), (short) 9140);

  public static final Block PURPUR_STAIRS = new Block(NamespaceID.from("minecraft:purpur_stairs"), (short) 9153);

  public static final Block END_STONE_BRICKS = new Block(NamespaceID.from("minecraft:end_stone_bricks"), (short) 9222);

  public static final Block BEETROOTS = new Block(NamespaceID.from("minecraft:beetroots"), (short) 9223);

  public static final Block GRASS_PATH = new Block(NamespaceID.from("minecraft:grass_path"), (short) 9227);

  public static final Block END_GATEWAY = new Block(NamespaceID.from("minecraft:end_gateway"), (short) 9228);

  public static final Block REPEATING_COMMAND_BLOCK = new Block(NamespaceID.from("minecraft:repeating_command_block"), (short) 9235);

  public static final Block CHAIN_COMMAND_BLOCK = new Block(NamespaceID.from("minecraft:chain_command_block"), (short) 9247);

  public static final Block FROSTED_ICE = new Block(NamespaceID.from("minecraft:frosted_ice"), (short) 9253);

  public static final Block MAGMA_BLOCK = new Block(NamespaceID.from("minecraft:magma_block"), (short) 9257);

  public static final Block NETHER_WART_BLOCK = new Block(NamespaceID.from("minecraft:nether_wart_block"), (short) 9258);

  public static final Block RED_NETHER_BRICKS = new Block(NamespaceID.from("minecraft:red_nether_bricks"), (short) 9259);

  public static final Block BONE_BLOCK = new Block(NamespaceID.from("minecraft:bone_block"), (short) 9261);

  public static final Block STRUCTURE_VOID = new Block(NamespaceID.from("minecraft:structure_void"), (short) 9263);

  public static final Block OBSERVER = new Block(NamespaceID.from("minecraft:observer"), (short) 9269);

  public static final Block SHULKER_BOX = new Block(NamespaceID.from("minecraft:shulker_box"), (short) 9280);

  public static final Block WHITE_SHULKER_BOX = new Block(NamespaceID.from("minecraft:white_shulker_box"), (short) 9286);

  public static final Block ORANGE_SHULKER_BOX = new Block(NamespaceID.from("minecraft:orange_shulker_box"), (short) 9292);

  public static final Block MAGENTA_SHULKER_BOX = new Block(NamespaceID.from("minecraft:magenta_shulker_box"), (short) 9298);

  public static final Block LIGHT_BLUE_SHULKER_BOX = new Block(NamespaceID.from("minecraft:light_blue_shulker_box"), (short) 9304);

  public static final Block YELLOW_SHULKER_BOX = new Block(NamespaceID.from("minecraft:yellow_shulker_box"), (short) 9310);

  public static final Block LIME_SHULKER_BOX = new Block(NamespaceID.from("minecraft:lime_shulker_box"), (short) 9316);

  public static final Block PINK_SHULKER_BOX = new Block(NamespaceID.from("minecraft:pink_shulker_box"), (short) 9322);

  public static final Block GRAY_SHULKER_BOX = new Block(NamespaceID.from("minecraft:gray_shulker_box"), (short) 9328);

  public static final Block LIGHT_GRAY_SHULKER_BOX = new Block(NamespaceID.from("minecraft:light_gray_shulker_box"), (short) 9334);

  public static final Block CYAN_SHULKER_BOX = new Block(NamespaceID.from("minecraft:cyan_shulker_box"), (short) 9340);

  public static final Block PURPLE_SHULKER_BOX = new Block(NamespaceID.from("minecraft:purple_shulker_box"), (short) 9346);

  public static final Block BLUE_SHULKER_BOX = new Block(NamespaceID.from("minecraft:blue_shulker_box"), (short) 9352);

  public static final Block BROWN_SHULKER_BOX = new Block(NamespaceID.from("minecraft:brown_shulker_box"), (short) 9358);

  public static final Block GREEN_SHULKER_BOX = new Block(NamespaceID.from("minecraft:green_shulker_box"), (short) 9364);

  public static final Block RED_SHULKER_BOX = new Block(NamespaceID.from("minecraft:red_shulker_box"), (short) 9370);

  public static final Block BLACK_SHULKER_BOX = new Block(NamespaceID.from("minecraft:black_shulker_box"), (short) 9376);

  public static final Block WHITE_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:white_glazed_terracotta"), (short) 9378);

  public static final Block ORANGE_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:orange_glazed_terracotta"), (short) 9382);

  public static final Block MAGENTA_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:magenta_glazed_terracotta"), (short) 9386);

  public static final Block LIGHT_BLUE_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:light_blue_glazed_terracotta"), (short) 9390);

  public static final Block YELLOW_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:yellow_glazed_terracotta"), (short) 9394);

  public static final Block LIME_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:lime_glazed_terracotta"), (short) 9398);

  public static final Block PINK_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:pink_glazed_terracotta"), (short) 9402);

  public static final Block GRAY_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:gray_glazed_terracotta"), (short) 9406);

  public static final Block LIGHT_GRAY_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:light_gray_glazed_terracotta"), (short) 9410);

  public static final Block CYAN_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:cyan_glazed_terracotta"), (short) 9414);

  public static final Block PURPLE_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:purple_glazed_terracotta"), (short) 9418);

  public static final Block BLUE_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:blue_glazed_terracotta"), (short) 9422);

  public static final Block BROWN_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:brown_glazed_terracotta"), (short) 9426);

  public static final Block GREEN_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:green_glazed_terracotta"), (short) 9430);

  public static final Block RED_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:red_glazed_terracotta"), (short) 9434);

  public static final Block BLACK_GLAZED_TERRACOTTA = new Block(NamespaceID.from("minecraft:black_glazed_terracotta"), (short) 9438);

  public static final Block WHITE_CONCRETE = new Block(NamespaceID.from("minecraft:white_concrete"), (short) 9442);

  public static final Block ORANGE_CONCRETE = new Block(NamespaceID.from("minecraft:orange_concrete"), (short) 9443);

  public static final Block MAGENTA_CONCRETE = new Block(NamespaceID.from("minecraft:magenta_concrete"), (short) 9444);

  public static final Block LIGHT_BLUE_CONCRETE = new Block(NamespaceID.from("minecraft:light_blue_concrete"), (short) 9445);

  public static final Block YELLOW_CONCRETE = new Block(NamespaceID.from("minecraft:yellow_concrete"), (short) 9446);

  public static final Block LIME_CONCRETE = new Block(NamespaceID.from("minecraft:lime_concrete"), (short) 9447);

  public static final Block PINK_CONCRETE = new Block(NamespaceID.from("minecraft:pink_concrete"), (short) 9448);

  public static final Block GRAY_CONCRETE = new Block(NamespaceID.from("minecraft:gray_concrete"), (short) 9449);

  public static final Block LIGHT_GRAY_CONCRETE = new Block(NamespaceID.from("minecraft:light_gray_concrete"), (short) 9450);

  public static final Block CYAN_CONCRETE = new Block(NamespaceID.from("minecraft:cyan_concrete"), (short) 9451);

  public static final Block PURPLE_CONCRETE = new Block(NamespaceID.from("minecraft:purple_concrete"), (short) 9452);

  public static final Block BLUE_CONCRETE = new Block(NamespaceID.from("minecraft:blue_concrete"), (short) 9453);

  public static final Block BROWN_CONCRETE = new Block(NamespaceID.from("minecraft:brown_concrete"), (short) 9454);

  public static final Block GREEN_CONCRETE = new Block(NamespaceID.from("minecraft:green_concrete"), (short) 9455);

  public static final Block RED_CONCRETE = new Block(NamespaceID.from("minecraft:red_concrete"), (short) 9456);

  public static final Block BLACK_CONCRETE = new Block(NamespaceID.from("minecraft:black_concrete"), (short) 9457);

  public static final Block WHITE_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:white_concrete_powder"), (short) 9458);

  public static final Block ORANGE_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:orange_concrete_powder"), (short) 9459);

  public static final Block MAGENTA_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:magenta_concrete_powder"), (short) 9460);

  public static final Block LIGHT_BLUE_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:light_blue_concrete_powder"), (short) 9461);

  public static final Block YELLOW_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:yellow_concrete_powder"), (short) 9462);

  public static final Block LIME_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:lime_concrete_powder"), (short) 9463);

  public static final Block PINK_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:pink_concrete_powder"), (short) 9464);

  public static final Block GRAY_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:gray_concrete_powder"), (short) 9465);

  public static final Block LIGHT_GRAY_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:light_gray_concrete_powder"), (short) 9466);

  public static final Block CYAN_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:cyan_concrete_powder"), (short) 9467);

  public static final Block PURPLE_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:purple_concrete_powder"), (short) 9468);

  public static final Block BLUE_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:blue_concrete_powder"), (short) 9469);

  public static final Block BROWN_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:brown_concrete_powder"), (short) 9470);

  public static final Block GREEN_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:green_concrete_powder"), (short) 9471);

  public static final Block RED_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:red_concrete_powder"), (short) 9472);

  public static final Block BLACK_CONCRETE_POWDER = new Block(NamespaceID.from("minecraft:black_concrete_powder"), (short) 9473);

  public static final Block KELP = new Block(NamespaceID.from("minecraft:kelp"), (short) 9474);

  public static final Block KELP_PLANT = new Block(NamespaceID.from("minecraft:kelp_plant"), (short) 9500);

  public static final Block DRIED_KELP_BLOCK = new Block(NamespaceID.from("minecraft:dried_kelp_block"), (short) 9501);

  public static final Block TURTLE_EGG = new Block(NamespaceID.from("minecraft:turtle_egg"), (short) 9502);

  public static final Block DEAD_TUBE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:dead_tube_coral_block"), (short) 9514);

  public static final Block DEAD_BRAIN_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:dead_brain_coral_block"), (short) 9515);

  public static final Block DEAD_BUBBLE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:dead_bubble_coral_block"), (short) 9516);

  public static final Block DEAD_FIRE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:dead_fire_coral_block"), (short) 9517);

  public static final Block DEAD_HORN_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:dead_horn_coral_block"), (short) 9518);

  public static final Block TUBE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:tube_coral_block"), (short) 9519);

  public static final Block BRAIN_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:brain_coral_block"), (short) 9520);

  public static final Block BUBBLE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:bubble_coral_block"), (short) 9521);

  public static final Block FIRE_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:fire_coral_block"), (short) 9522);

  public static final Block HORN_CORAL_BLOCK = new Block(NamespaceID.from("minecraft:horn_coral_block"), (short) 9523);

  public static final Block DEAD_TUBE_CORAL = new Block(NamespaceID.from("minecraft:dead_tube_coral"), (short) 9524);

  public static final Block DEAD_BRAIN_CORAL = new Block(NamespaceID.from("minecraft:dead_brain_coral"), (short) 9526);

  public static final Block DEAD_BUBBLE_CORAL = new Block(NamespaceID.from("minecraft:dead_bubble_coral"), (short) 9528);

  public static final Block DEAD_FIRE_CORAL = new Block(NamespaceID.from("minecraft:dead_fire_coral"), (short) 9530);

  public static final Block DEAD_HORN_CORAL = new Block(NamespaceID.from("minecraft:dead_horn_coral"), (short) 9532);

  public static final Block TUBE_CORAL = new Block(NamespaceID.from("minecraft:tube_coral"), (short) 9534);

  public static final Block BRAIN_CORAL = new Block(NamespaceID.from("minecraft:brain_coral"), (short) 9536);

  public static final Block BUBBLE_CORAL = new Block(NamespaceID.from("minecraft:bubble_coral"), (short) 9538);

  public static final Block FIRE_CORAL = new Block(NamespaceID.from("minecraft:fire_coral"), (short) 9540);

  public static final Block HORN_CORAL = new Block(NamespaceID.from("minecraft:horn_coral"), (short) 9542);

  public static final Block DEAD_TUBE_CORAL_FAN = new Block(NamespaceID.from("minecraft:dead_tube_coral_fan"), (short) 9544);

  public static final Block DEAD_BRAIN_CORAL_FAN = new Block(NamespaceID.from("minecraft:dead_brain_coral_fan"), (short) 9546);

  public static final Block DEAD_BUBBLE_CORAL_FAN = new Block(NamespaceID.from("minecraft:dead_bubble_coral_fan"), (short) 9548);

  public static final Block DEAD_FIRE_CORAL_FAN = new Block(NamespaceID.from("minecraft:dead_fire_coral_fan"), (short) 9550);

  public static final Block DEAD_HORN_CORAL_FAN = new Block(NamespaceID.from("minecraft:dead_horn_coral_fan"), (short) 9552);

  public static final Block TUBE_CORAL_FAN = new Block(NamespaceID.from("minecraft:tube_coral_fan"), (short) 9554);

  public static final Block BRAIN_CORAL_FAN = new Block(NamespaceID.from("minecraft:brain_coral_fan"), (short) 9556);

  public static final Block BUBBLE_CORAL_FAN = new Block(NamespaceID.from("minecraft:bubble_coral_fan"), (short) 9558);

  public static final Block FIRE_CORAL_FAN = new Block(NamespaceID.from("minecraft:fire_coral_fan"), (short) 9560);

  public static final Block HORN_CORAL_FAN = new Block(NamespaceID.from("minecraft:horn_coral_fan"), (short) 9562);

  public static final Block DEAD_TUBE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:dead_tube_coral_wall_fan"), (short) 9564);

  public static final Block DEAD_BRAIN_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:dead_brain_coral_wall_fan"), (short) 9572);

  public static final Block DEAD_BUBBLE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:dead_bubble_coral_wall_fan"), (short) 9580);

  public static final Block DEAD_FIRE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:dead_fire_coral_wall_fan"), (short) 9588);

  public static final Block DEAD_HORN_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:dead_horn_coral_wall_fan"), (short) 9596);

  public static final Block TUBE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:tube_coral_wall_fan"), (short) 9604);

  public static final Block BRAIN_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:brain_coral_wall_fan"), (short) 9612);

  public static final Block BUBBLE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:bubble_coral_wall_fan"), (short) 9620);

  public static final Block FIRE_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:fire_coral_wall_fan"), (short) 9628);

  public static final Block HORN_CORAL_WALL_FAN = new Block(NamespaceID.from("minecraft:horn_coral_wall_fan"), (short) 9636);

  public static final Block SEA_PICKLE = new Block(NamespaceID.from("minecraft:sea_pickle"), (short) 9644);

  public static final Block BLUE_ICE = new Block(NamespaceID.from("minecraft:blue_ice"), (short) 9652);

  public static final Block CONDUIT = new Block(NamespaceID.from("minecraft:conduit"), (short) 9653);

  public static final Block BAMBOO_SAPLING = new Block(NamespaceID.from("minecraft:bamboo_sapling"), (short) 9655);

  public static final Block BAMBOO = new Block(NamespaceID.from("minecraft:bamboo"), (short) 9656);

  public static final Block POTTED_BAMBOO = new Block(NamespaceID.from("minecraft:potted_bamboo"), (short) 9668);

  public static final Block VOID_AIR = new Block(NamespaceID.from("minecraft:void_air"), (short) 9669);

  public static final Block CAVE_AIR = new Block(NamespaceID.from("minecraft:cave_air"), (short) 9670);

  public static final Block BUBBLE_COLUMN = new Block(NamespaceID.from("minecraft:bubble_column"), (short) 9671);

  public static final Block POLISHED_GRANITE_STAIRS = new Block(NamespaceID.from("minecraft:polished_granite_stairs"), (short) 9684);

  public static final Block SMOOTH_RED_SANDSTONE_STAIRS = new Block(NamespaceID.from("minecraft:smooth_red_sandstone_stairs"), (short) 9764);

  public static final Block MOSSY_STONE_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:mossy_stone_brick_stairs"), (short) 9844);

  public static final Block POLISHED_DIORITE_STAIRS = new Block(NamespaceID.from("minecraft:polished_diorite_stairs"), (short) 9924);

  public static final Block MOSSY_COBBLESTONE_STAIRS = new Block(NamespaceID.from("minecraft:mossy_cobblestone_stairs"), (short) 10004);

  public static final Block END_STONE_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:end_stone_brick_stairs"), (short) 10084);

  public static final Block STONE_STAIRS = new Block(NamespaceID.from("minecraft:stone_stairs"), (short) 10164);

  public static final Block SMOOTH_SANDSTONE_STAIRS = new Block(NamespaceID.from("minecraft:smooth_sandstone_stairs"), (short) 10244);

  public static final Block SMOOTH_QUARTZ_STAIRS = new Block(NamespaceID.from("minecraft:smooth_quartz_stairs"), (short) 10324);

  public static final Block GRANITE_STAIRS = new Block(NamespaceID.from("minecraft:granite_stairs"), (short) 10404);

  public static final Block ANDESITE_STAIRS = new Block(NamespaceID.from("minecraft:andesite_stairs"), (short) 10484);

  public static final Block RED_NETHER_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:red_nether_brick_stairs"), (short) 10564);

  public static final Block POLISHED_ANDESITE_STAIRS = new Block(NamespaceID.from("minecraft:polished_andesite_stairs"), (short) 10644);

  public static final Block DIORITE_STAIRS = new Block(NamespaceID.from("minecraft:diorite_stairs"), (short) 10724);

  public static final Block POLISHED_GRANITE_SLAB = new Block(NamespaceID.from("minecraft:polished_granite_slab"), (short) 10796);

  public static final Block SMOOTH_RED_SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:smooth_red_sandstone_slab"), (short) 10802);

  public static final Block MOSSY_STONE_BRICK_SLAB = new Block(NamespaceID.from("minecraft:mossy_stone_brick_slab"), (short) 10808);

  public static final Block POLISHED_DIORITE_SLAB = new Block(NamespaceID.from("minecraft:polished_diorite_slab"), (short) 10814);

  public static final Block MOSSY_COBBLESTONE_SLAB = new Block(NamespaceID.from("minecraft:mossy_cobblestone_slab"), (short) 10820);

  public static final Block END_STONE_BRICK_SLAB = new Block(NamespaceID.from("minecraft:end_stone_brick_slab"), (short) 10826);

  public static final Block SMOOTH_SANDSTONE_SLAB = new Block(NamespaceID.from("minecraft:smooth_sandstone_slab"), (short) 10832);

  public static final Block SMOOTH_QUARTZ_SLAB = new Block(NamespaceID.from("minecraft:smooth_quartz_slab"), (short) 10838);

  public static final Block GRANITE_SLAB = new Block(NamespaceID.from("minecraft:granite_slab"), (short) 10844);

  public static final Block ANDESITE_SLAB = new Block(NamespaceID.from("minecraft:andesite_slab"), (short) 10850);

  public static final Block RED_NETHER_BRICK_SLAB = new Block(NamespaceID.from("minecraft:red_nether_brick_slab"), (short) 10856);

  public static final Block POLISHED_ANDESITE_SLAB = new Block(NamespaceID.from("minecraft:polished_andesite_slab"), (short) 10862);

  public static final Block DIORITE_SLAB = new Block(NamespaceID.from("minecraft:diorite_slab"), (short) 10868);

  public static final Block BRICK_WALL = new Block(NamespaceID.from("minecraft:brick_wall"), (short) 10874);

  public static final Block PRISMARINE_WALL = new Block(NamespaceID.from("minecraft:prismarine_wall"), (short) 11198);

  public static final Block RED_SANDSTONE_WALL = new Block(NamespaceID.from("minecraft:red_sandstone_wall"), (short) 11522);

  public static final Block MOSSY_STONE_BRICK_WALL = new Block(NamespaceID.from("minecraft:mossy_stone_brick_wall"), (short) 11846);

  public static final Block GRANITE_WALL = new Block(NamespaceID.from("minecraft:granite_wall"), (short) 12170);

  public static final Block STONE_BRICK_WALL = new Block(NamespaceID.from("minecraft:stone_brick_wall"), (short) 12494);

  public static final Block NETHER_BRICK_WALL = new Block(NamespaceID.from("minecraft:nether_brick_wall"), (short) 12818);

  public static final Block ANDESITE_WALL = new Block(NamespaceID.from("minecraft:andesite_wall"), (short) 13142);

  public static final Block RED_NETHER_BRICK_WALL = new Block(NamespaceID.from("minecraft:red_nether_brick_wall"), (short) 13466);

  public static final Block SANDSTONE_WALL = new Block(NamespaceID.from("minecraft:sandstone_wall"), (short) 13790);

  public static final Block END_STONE_BRICK_WALL = new Block(NamespaceID.from("minecraft:end_stone_brick_wall"), (short) 14114);

  public static final Block DIORITE_WALL = new Block(NamespaceID.from("minecraft:diorite_wall"), (short) 14438);

  public static final Block SCAFFOLDING = new Block(NamespaceID.from("minecraft:scaffolding"), (short) 14790);

  public static final Block LOOM = new Block(NamespaceID.from("minecraft:loom"), (short) 14791);

  public static final Block BARREL = new Block(NamespaceID.from("minecraft:barrel"), (short) 14796);

  public static final Block SMOKER = new Block(NamespaceID.from("minecraft:smoker"), (short) 14808);

  public static final Block BLAST_FURNACE = new Block(NamespaceID.from("minecraft:blast_furnace"), (short) 14816);

  public static final Block CARTOGRAPHY_TABLE = new Block(NamespaceID.from("minecraft:cartography_table"), (short) 14823);

  public static final Block FLETCHING_TABLE = new Block(NamespaceID.from("minecraft:fletching_table"), (short) 14824);

  public static final Block GRINDSTONE = new Block(NamespaceID.from("minecraft:grindstone"), (short) 14829);

  public static final Block LECTERN = new Block(NamespaceID.from("minecraft:lectern"), (short) 14840);

  public static final Block SMITHING_TABLE = new Block(NamespaceID.from("minecraft:smithing_table"), (short) 14853);

  public static final Block STONECUTTER = new Block(NamespaceID.from("minecraft:stonecutter"), (short) 14854);

  public static final Block BELL = new Block(NamespaceID.from("minecraft:bell"), (short) 14859);

  public static final Block LANTERN = new Block(NamespaceID.from("minecraft:lantern"), (short) 14893);

  public static final Block SOUL_LANTERN = new Block(NamespaceID.from("minecraft:soul_lantern"), (short) 14897);

  public static final Block CAMPFIRE = new Block(NamespaceID.from("minecraft:campfire"), (short) 14901);

  public static final Block SOUL_CAMPFIRE = new Block(NamespaceID.from("minecraft:soul_campfire"), (short) 14933);

  public static final Block SWEET_BERRY_BUSH = new Block(NamespaceID.from("minecraft:sweet_berry_bush"), (short) 14962);

  public static final Block WARPED_STEM = new Block(NamespaceID.from("minecraft:warped_stem"), (short) 14967);

  public static final Block STRIPPED_WARPED_STEM = new Block(NamespaceID.from("minecraft:stripped_warped_stem"), (short) 14970);

  public static final Block WARPED_HYPHAE = new Block(NamespaceID.from("minecraft:warped_hyphae"), (short) 14973);

  public static final Block STRIPPED_WARPED_HYPHAE = new Block(NamespaceID.from("minecraft:stripped_warped_hyphae"), (short) 14976);

  public static final Block WARPED_NYLIUM = new Block(NamespaceID.from("minecraft:warped_nylium"), (short) 14978);

  public static final Block WARPED_FUNGUS = new Block(NamespaceID.from("minecraft:warped_fungus"), (short) 14979);

  public static final Block WARPED_WART_BLOCK = new Block(NamespaceID.from("minecraft:warped_wart_block"), (short) 14980);

  public static final Block WARPED_ROOTS = new Block(NamespaceID.from("minecraft:warped_roots"), (short) 14981);

  public static final Block NETHER_SPROUTS = new Block(NamespaceID.from("minecraft:nether_sprouts"), (short) 14982);

  public static final Block CRIMSON_STEM = new Block(NamespaceID.from("minecraft:crimson_stem"), (short) 14984);

  public static final Block STRIPPED_CRIMSON_STEM = new Block(NamespaceID.from("minecraft:stripped_crimson_stem"), (short) 14987);

  public static final Block CRIMSON_HYPHAE = new Block(NamespaceID.from("minecraft:crimson_hyphae"), (short) 14990);

  public static final Block STRIPPED_CRIMSON_HYPHAE = new Block(NamespaceID.from("minecraft:stripped_crimson_hyphae"), (short) 14993);

  public static final Block CRIMSON_NYLIUM = new Block(NamespaceID.from("minecraft:crimson_nylium"), (short) 14995);

  public static final Block CRIMSON_FUNGUS = new Block(NamespaceID.from("minecraft:crimson_fungus"), (short) 14996);

  public static final Block SHROOMLIGHT = new Block(NamespaceID.from("minecraft:shroomlight"), (short) 14997);

  public static final Block WEEPING_VINES = new Block(NamespaceID.from("minecraft:weeping_vines"), (short) 14998);

  public static final Block WEEPING_VINES_PLANT = new Block(NamespaceID.from("minecraft:weeping_vines_plant"), (short) 15024);

  public static final Block TWISTING_VINES = new Block(NamespaceID.from("minecraft:twisting_vines"), (short) 15025);

  public static final Block TWISTING_VINES_PLANT = new Block(NamespaceID.from("minecraft:twisting_vines_plant"), (short) 15051);

  public static final Block CRIMSON_ROOTS = new Block(NamespaceID.from("minecraft:crimson_roots"), (short) 15052);

  public static final Block CRIMSON_PLANKS = new Block(NamespaceID.from("minecraft:crimson_planks"), (short) 15053);

  public static final Block WARPED_PLANKS = new Block(NamespaceID.from("minecraft:warped_planks"), (short) 15054);

  public static final Block CRIMSON_SLAB = new Block(NamespaceID.from("minecraft:crimson_slab"), (short) 15058);

  public static final Block WARPED_SLAB = new Block(NamespaceID.from("minecraft:warped_slab"), (short) 15064);

  public static final Block CRIMSON_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:crimson_pressure_plate"), (short) 15068);

  public static final Block WARPED_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:warped_pressure_plate"), (short) 15070);

  public static final Block CRIMSON_FENCE = new Block(NamespaceID.from("minecraft:crimson_fence"), (short) 15102);

  public static final Block WARPED_FENCE = new Block(NamespaceID.from("minecraft:warped_fence"), (short) 15134);

  public static final Block CRIMSON_TRAPDOOR = new Block(NamespaceID.from("minecraft:crimson_trapdoor"), (short) 15150);

  public static final Block WARPED_TRAPDOOR = new Block(NamespaceID.from("minecraft:warped_trapdoor"), (short) 15214);

  public static final Block CRIMSON_FENCE_GATE = new Block(NamespaceID.from("minecraft:crimson_fence_gate"), (short) 15270);

  public static final Block WARPED_FENCE_GATE = new Block(NamespaceID.from("minecraft:warped_fence_gate"), (short) 15302);

  public static final Block CRIMSON_STAIRS = new Block(NamespaceID.from("minecraft:crimson_stairs"), (short) 15338);

  public static final Block WARPED_STAIRS = new Block(NamespaceID.from("minecraft:warped_stairs"), (short) 15418);

  public static final Block CRIMSON_BUTTON = new Block(NamespaceID.from("minecraft:crimson_button"), (short) 15496);

  public static final Block WARPED_BUTTON = new Block(NamespaceID.from("minecraft:warped_button"), (short) 15520);

  public static final Block CRIMSON_DOOR = new Block(NamespaceID.from("minecraft:crimson_door"), (short) 15546);

  public static final Block WARPED_DOOR = new Block(NamespaceID.from("minecraft:warped_door"), (short) 15610);

  public static final Block CRIMSON_SIGN = new Block(NamespaceID.from("minecraft:crimson_sign"), (short) 15664);

  public static final Block WARPED_SIGN = new Block(NamespaceID.from("minecraft:warped_sign"), (short) 15696);

  public static final Block CRIMSON_WALL_SIGN = new Block(NamespaceID.from("minecraft:crimson_wall_sign"), (short) 15728);

  public static final Block WARPED_WALL_SIGN = new Block(NamespaceID.from("minecraft:warped_wall_sign"), (short) 15736);

  public static final Block STRUCTURE_BLOCK = new Block(NamespaceID.from("minecraft:structure_block"), (short) 15743);

  public static final Block JIGSAW = new Block(NamespaceID.from("minecraft:jigsaw"), (short) 15757);

  public static final Block COMPOSTER = new Block(NamespaceID.from("minecraft:composter"), (short) 15759);

  public static final Block TARGET = new Block(NamespaceID.from("minecraft:target"), (short) 15768);

  public static final Block BEE_NEST = new Block(NamespaceID.from("minecraft:bee_nest"), (short) 15784);

  public static final Block BEEHIVE = new Block(NamespaceID.from("minecraft:beehive"), (short) 15808);

  public static final Block HONEY_BLOCK = new Block(NamespaceID.from("minecraft:honey_block"), (short) 15832);

  public static final Block HONEYCOMB_BLOCK = new Block(NamespaceID.from("minecraft:honeycomb_block"), (short) 15833);

  public static final Block NETHERITE_BLOCK = new Block(NamespaceID.from("minecraft:netherite_block"), (short) 15834);

  public static final Block ANCIENT_DEBRIS = new Block(NamespaceID.from("minecraft:ancient_debris"), (short) 15835);

  public static final Block CRYING_OBSIDIAN = new Block(NamespaceID.from("minecraft:crying_obsidian"), (short) 15836);

  public static final Block RESPAWN_ANCHOR = new Block(NamespaceID.from("minecraft:respawn_anchor"), (short) 15837);

  public static final Block POTTED_CRIMSON_FUNGUS = new Block(NamespaceID.from("minecraft:potted_crimson_fungus"), (short) 15842);

  public static final Block POTTED_WARPED_FUNGUS = new Block(NamespaceID.from("minecraft:potted_warped_fungus"), (short) 15843);

  public static final Block POTTED_CRIMSON_ROOTS = new Block(NamespaceID.from("minecraft:potted_crimson_roots"), (short) 15844);

  public static final Block POTTED_WARPED_ROOTS = new Block(NamespaceID.from("minecraft:potted_warped_roots"), (short) 15845);

  public static final Block LODESTONE = new Block(NamespaceID.from("minecraft:lodestone"), (short) 15846);

  public static final Block BLACKSTONE = new Block(NamespaceID.from("minecraft:blackstone"), (short) 15847);

  public static final Block BLACKSTONE_STAIRS = new Block(NamespaceID.from("minecraft:blackstone_stairs"), (short) 15859);

  public static final Block BLACKSTONE_WALL = new Block(NamespaceID.from("minecraft:blackstone_wall"), (short) 15931);

  public static final Block BLACKSTONE_SLAB = new Block(NamespaceID.from("minecraft:blackstone_slab"), (short) 16255);

  public static final Block POLISHED_BLACKSTONE = new Block(NamespaceID.from("minecraft:polished_blackstone"), (short) 16258);

  public static final Block POLISHED_BLACKSTONE_BRICKS = new Block(NamespaceID.from("minecraft:polished_blackstone_bricks"), (short) 16259);

  public static final Block CRACKED_POLISHED_BLACKSTONE_BRICKS = new Block(NamespaceID.from("minecraft:cracked_polished_blackstone_bricks"), (short) 16260);

  public static final Block CHISELED_POLISHED_BLACKSTONE = new Block(NamespaceID.from("minecraft:chiseled_polished_blackstone"), (short) 16261);

  public static final Block POLISHED_BLACKSTONE_BRICK_SLAB = new Block(NamespaceID.from("minecraft:polished_blackstone_brick_slab"), (short) 16265);

  public static final Block POLISHED_BLACKSTONE_BRICK_STAIRS = new Block(NamespaceID.from("minecraft:polished_blackstone_brick_stairs"), (short) 16279);

  public static final Block POLISHED_BLACKSTONE_BRICK_WALL = new Block(NamespaceID.from("minecraft:polished_blackstone_brick_wall"), (short) 16351);

  public static final Block GILDED_BLACKSTONE = new Block(NamespaceID.from("minecraft:gilded_blackstone"), (short) 16672);

  public static final Block POLISHED_BLACKSTONE_STAIRS = new Block(NamespaceID.from("minecraft:polished_blackstone_stairs"), (short) 16684);

  public static final Block POLISHED_BLACKSTONE_SLAB = new Block(NamespaceID.from("minecraft:polished_blackstone_slab"), (short) 16756);

  public static final Block POLISHED_BLACKSTONE_PRESSURE_PLATE = new Block(NamespaceID.from("minecraft:polished_blackstone_pressure_plate"), (short) 16760);

  public static final Block POLISHED_BLACKSTONE_BUTTON = new Block(NamespaceID.from("minecraft:polished_blackstone_button"), (short) 16770);

  public static final Block POLISHED_BLACKSTONE_WALL = new Block(NamespaceID.from("minecraft:polished_blackstone_wall"), (short) 16788);

  public static final Block CHISELED_NETHER_BRICKS = new Block(NamespaceID.from("minecraft:chiseled_nether_bricks"), (short) 17109);

  public static final Block CRACKED_NETHER_BRICKS = new Block(NamespaceID.from("minecraft:cracked_nether_bricks"), (short) 17110);

  public static final Block QUARTZ_BRICKS = new Block(NamespaceID.from("minecraft:quartz_bricks"), (short) 17111);

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
    Registries.registerBlock(AIR);
    Registries.registerBlock(STONE);
    Registries.registerBlock(GRANITE);
    Registries.registerBlock(POLISHED_GRANITE);
    Registries.registerBlock(DIORITE);
    Registries.registerBlock(POLISHED_DIORITE);
    Registries.registerBlock(ANDESITE);
    Registries.registerBlock(POLISHED_ANDESITE);
    Registries.registerBlock(GRASS_BLOCK);
    Registries.registerBlock(DIRT);
    Registries.registerBlock(COARSE_DIRT);
    Registries.registerBlock(PODZOL);
    Registries.registerBlock(COBBLESTONE);
    Registries.registerBlock(OAK_PLANKS);
    Registries.registerBlock(SPRUCE_PLANKS);
    Registries.registerBlock(BIRCH_PLANKS);
    Registries.registerBlock(JUNGLE_PLANKS);
    Registries.registerBlock(ACACIA_PLANKS);
    Registries.registerBlock(DARK_OAK_PLANKS);
    Registries.registerBlock(OAK_SAPLING);
    Registries.registerBlock(SPRUCE_SAPLING);
    Registries.registerBlock(BIRCH_SAPLING);
    Registries.registerBlock(JUNGLE_SAPLING);
    Registries.registerBlock(ACACIA_SAPLING);
    Registries.registerBlock(DARK_OAK_SAPLING);
    Registries.registerBlock(BEDROCK);
    Registries.registerBlock(WATER);
    Registries.registerBlock(LAVA);
    Registries.registerBlock(SAND);
    Registries.registerBlock(RED_SAND);
    Registries.registerBlock(GRAVEL);
    Registries.registerBlock(GOLD_ORE);
    Registries.registerBlock(IRON_ORE);
    Registries.registerBlock(COAL_ORE);
    Registries.registerBlock(NETHER_GOLD_ORE);
    Registries.registerBlock(OAK_LOG);
    Registries.registerBlock(SPRUCE_LOG);
    Registries.registerBlock(BIRCH_LOG);
    Registries.registerBlock(JUNGLE_LOG);
    Registries.registerBlock(ACACIA_LOG);
    Registries.registerBlock(DARK_OAK_LOG);
    Registries.registerBlock(STRIPPED_SPRUCE_LOG);
    Registries.registerBlock(STRIPPED_BIRCH_LOG);
    Registries.registerBlock(STRIPPED_JUNGLE_LOG);
    Registries.registerBlock(STRIPPED_ACACIA_LOG);
    Registries.registerBlock(STRIPPED_DARK_OAK_LOG);
    Registries.registerBlock(STRIPPED_OAK_LOG);
    Registries.registerBlock(OAK_WOOD);
    Registries.registerBlock(SPRUCE_WOOD);
    Registries.registerBlock(BIRCH_WOOD);
    Registries.registerBlock(JUNGLE_WOOD);
    Registries.registerBlock(ACACIA_WOOD);
    Registries.registerBlock(DARK_OAK_WOOD);
    Registries.registerBlock(STRIPPED_OAK_WOOD);
    Registries.registerBlock(STRIPPED_SPRUCE_WOOD);
    Registries.registerBlock(STRIPPED_BIRCH_WOOD);
    Registries.registerBlock(STRIPPED_JUNGLE_WOOD);
    Registries.registerBlock(STRIPPED_ACACIA_WOOD);
    Registries.registerBlock(STRIPPED_DARK_OAK_WOOD);
    Registries.registerBlock(OAK_LEAVES);
    Registries.registerBlock(SPRUCE_LEAVES);
    Registries.registerBlock(BIRCH_LEAVES);
    Registries.registerBlock(JUNGLE_LEAVES);
    Registries.registerBlock(ACACIA_LEAVES);
    Registries.registerBlock(DARK_OAK_LEAVES);
    Registries.registerBlock(SPONGE);
    Registries.registerBlock(WET_SPONGE);
    Registries.registerBlock(GLASS);
    Registries.registerBlock(LAPIS_ORE);
    Registries.registerBlock(LAPIS_BLOCK);
    Registries.registerBlock(DISPENSER);
    Registries.registerBlock(SANDSTONE);
    Registries.registerBlock(CHISELED_SANDSTONE);
    Registries.registerBlock(CUT_SANDSTONE);
    Registries.registerBlock(NOTE_BLOCK);
    Registries.registerBlock(WHITE_BED);
    Registries.registerBlock(ORANGE_BED);
    Registries.registerBlock(MAGENTA_BED);
    Registries.registerBlock(LIGHT_BLUE_BED);
    Registries.registerBlock(YELLOW_BED);
    Registries.registerBlock(LIME_BED);
    Registries.registerBlock(PINK_BED);
    Registries.registerBlock(GRAY_BED);
    Registries.registerBlock(LIGHT_GRAY_BED);
    Registries.registerBlock(CYAN_BED);
    Registries.registerBlock(PURPLE_BED);
    Registries.registerBlock(BLUE_BED);
    Registries.registerBlock(BROWN_BED);
    Registries.registerBlock(GREEN_BED);
    Registries.registerBlock(RED_BED);
    Registries.registerBlock(BLACK_BED);
    Registries.registerBlock(POWERED_RAIL);
    Registries.registerBlock(DETECTOR_RAIL);
    Registries.registerBlock(STICKY_PISTON);
    Registries.registerBlock(COBWEB);
    Registries.registerBlock(GRASS);
    Registries.registerBlock(FERN);
    Registries.registerBlock(DEAD_BUSH);
    Registries.registerBlock(SEAGRASS);
    Registries.registerBlock(TALL_SEAGRASS);
    Registries.registerBlock(PISTON);
    Registries.registerBlock(PISTON_HEAD);
    Registries.registerBlock(WHITE_WOOL);
    Registries.registerBlock(ORANGE_WOOL);
    Registries.registerBlock(MAGENTA_WOOL);
    Registries.registerBlock(LIGHT_BLUE_WOOL);
    Registries.registerBlock(YELLOW_WOOL);
    Registries.registerBlock(LIME_WOOL);
    Registries.registerBlock(PINK_WOOL);
    Registries.registerBlock(GRAY_WOOL);
    Registries.registerBlock(LIGHT_GRAY_WOOL);
    Registries.registerBlock(CYAN_WOOL);
    Registries.registerBlock(PURPLE_WOOL);
    Registries.registerBlock(BLUE_WOOL);
    Registries.registerBlock(BROWN_WOOL);
    Registries.registerBlock(GREEN_WOOL);
    Registries.registerBlock(RED_WOOL);
    Registries.registerBlock(BLACK_WOOL);
    Registries.registerBlock(MOVING_PISTON);
    Registries.registerBlock(DANDELION);
    Registries.registerBlock(POPPY);
    Registries.registerBlock(BLUE_ORCHID);
    Registries.registerBlock(ALLIUM);
    Registries.registerBlock(AZURE_BLUET);
    Registries.registerBlock(RED_TULIP);
    Registries.registerBlock(ORANGE_TULIP);
    Registries.registerBlock(WHITE_TULIP);
    Registries.registerBlock(PINK_TULIP);
    Registries.registerBlock(OXEYE_DAISY);
    Registries.registerBlock(CORNFLOWER);
    Registries.registerBlock(WITHER_ROSE);
    Registries.registerBlock(LILY_OF_THE_VALLEY);
    Registries.registerBlock(BROWN_MUSHROOM);
    Registries.registerBlock(RED_MUSHROOM);
    Registries.registerBlock(GOLD_BLOCK);
    Registries.registerBlock(IRON_BLOCK);
    Registries.registerBlock(BRICKS);
    Registries.registerBlock(TNT);
    Registries.registerBlock(BOOKSHELF);
    Registries.registerBlock(MOSSY_COBBLESTONE);
    Registries.registerBlock(OBSIDIAN);
    Registries.registerBlock(TORCH);
    Registries.registerBlock(WALL_TORCH);
    Registries.registerBlock(FIRE);
    Registries.registerBlock(SOUL_FIRE);
    Registries.registerBlock(SPAWNER);
    Registries.registerBlock(OAK_STAIRS);
    Registries.registerBlock(CHEST);
    Registries.registerBlock(REDSTONE_WIRE);
    Registries.registerBlock(DIAMOND_ORE);
    Registries.registerBlock(DIAMOND_BLOCK);
    Registries.registerBlock(CRAFTING_TABLE);
    Registries.registerBlock(WHEAT);
    Registries.registerBlock(FARMLAND);
    Registries.registerBlock(FURNACE);
    Registries.registerBlock(OAK_SIGN);
    Registries.registerBlock(SPRUCE_SIGN);
    Registries.registerBlock(BIRCH_SIGN);
    Registries.registerBlock(ACACIA_SIGN);
    Registries.registerBlock(JUNGLE_SIGN);
    Registries.registerBlock(DARK_OAK_SIGN);
    Registries.registerBlock(OAK_DOOR);
    Registries.registerBlock(LADDER);
    Registries.registerBlock(RAIL);
    Registries.registerBlock(COBBLESTONE_STAIRS);
    Registries.registerBlock(OAK_WALL_SIGN);
    Registries.registerBlock(SPRUCE_WALL_SIGN);
    Registries.registerBlock(BIRCH_WALL_SIGN);
    Registries.registerBlock(ACACIA_WALL_SIGN);
    Registries.registerBlock(JUNGLE_WALL_SIGN);
    Registries.registerBlock(DARK_OAK_WALL_SIGN);
    Registries.registerBlock(LEVER);
    Registries.registerBlock(STONE_PRESSURE_PLATE);
    Registries.registerBlock(IRON_DOOR);
    Registries.registerBlock(OAK_PRESSURE_PLATE);
    Registries.registerBlock(SPRUCE_PRESSURE_PLATE);
    Registries.registerBlock(BIRCH_PRESSURE_PLATE);
    Registries.registerBlock(JUNGLE_PRESSURE_PLATE);
    Registries.registerBlock(ACACIA_PRESSURE_PLATE);
    Registries.registerBlock(DARK_OAK_PRESSURE_PLATE);
    Registries.registerBlock(REDSTONE_ORE);
    Registries.registerBlock(REDSTONE_TORCH);
    Registries.registerBlock(REDSTONE_WALL_TORCH);
    Registries.registerBlock(STONE_BUTTON);
    Registries.registerBlock(SNOW);
    Registries.registerBlock(ICE);
    Registries.registerBlock(SNOW_BLOCK);
    Registries.registerBlock(CACTUS);
    Registries.registerBlock(CLAY);
    Registries.registerBlock(SUGAR_CANE);
    Registries.registerBlock(JUKEBOX);
    Registries.registerBlock(OAK_FENCE);
    Registries.registerBlock(PUMPKIN);
    Registries.registerBlock(NETHERRACK);
    Registries.registerBlock(SOUL_SAND);
    Registries.registerBlock(SOUL_SOIL);
    Registries.registerBlock(BASALT);
    Registries.registerBlock(POLISHED_BASALT);
    Registries.registerBlock(SOUL_TORCH);
    Registries.registerBlock(SOUL_WALL_TORCH);
    Registries.registerBlock(GLOWSTONE);
    Registries.registerBlock(NETHER_PORTAL);
    Registries.registerBlock(CARVED_PUMPKIN);
    Registries.registerBlock(JACK_O_LANTERN);
    Registries.registerBlock(CAKE);
    Registries.registerBlock(REPEATER);
    Registries.registerBlock(WHITE_STAINED_GLASS);
    Registries.registerBlock(ORANGE_STAINED_GLASS);
    Registries.registerBlock(MAGENTA_STAINED_GLASS);
    Registries.registerBlock(LIGHT_BLUE_STAINED_GLASS);
    Registries.registerBlock(YELLOW_STAINED_GLASS);
    Registries.registerBlock(LIME_STAINED_GLASS);
    Registries.registerBlock(PINK_STAINED_GLASS);
    Registries.registerBlock(GRAY_STAINED_GLASS);
    Registries.registerBlock(LIGHT_GRAY_STAINED_GLASS);
    Registries.registerBlock(CYAN_STAINED_GLASS);
    Registries.registerBlock(PURPLE_STAINED_GLASS);
    Registries.registerBlock(BLUE_STAINED_GLASS);
    Registries.registerBlock(BROWN_STAINED_GLASS);
    Registries.registerBlock(GREEN_STAINED_GLASS);
    Registries.registerBlock(RED_STAINED_GLASS);
    Registries.registerBlock(BLACK_STAINED_GLASS);
    Registries.registerBlock(OAK_TRAPDOOR);
    Registries.registerBlock(SPRUCE_TRAPDOOR);
    Registries.registerBlock(BIRCH_TRAPDOOR);
    Registries.registerBlock(JUNGLE_TRAPDOOR);
    Registries.registerBlock(ACACIA_TRAPDOOR);
    Registries.registerBlock(DARK_OAK_TRAPDOOR);
    Registries.registerBlock(STONE_BRICKS);
    Registries.registerBlock(MOSSY_STONE_BRICKS);
    Registries.registerBlock(CRACKED_STONE_BRICKS);
    Registries.registerBlock(CHISELED_STONE_BRICKS);
    Registries.registerBlock(INFESTED_STONE);
    Registries.registerBlock(INFESTED_COBBLESTONE);
    Registries.registerBlock(INFESTED_STONE_BRICKS);
    Registries.registerBlock(INFESTED_MOSSY_STONE_BRICKS);
    Registries.registerBlock(INFESTED_CRACKED_STONE_BRICKS);
    Registries.registerBlock(INFESTED_CHISELED_STONE_BRICKS);
    Registries.registerBlock(BROWN_MUSHROOM_BLOCK);
    Registries.registerBlock(RED_MUSHROOM_BLOCK);
    Registries.registerBlock(MUSHROOM_STEM);
    Registries.registerBlock(IRON_BARS);
    Registries.registerBlock(CHAIN);
    Registries.registerBlock(GLASS_PANE);
    Registries.registerBlock(MELON);
    Registries.registerBlock(ATTACHED_PUMPKIN_STEM);
    Registries.registerBlock(ATTACHED_MELON_STEM);
    Registries.registerBlock(PUMPKIN_STEM);
    Registries.registerBlock(MELON_STEM);
    Registries.registerBlock(VINE);
    Registries.registerBlock(OAK_FENCE_GATE);
    Registries.registerBlock(BRICK_STAIRS);
    Registries.registerBlock(STONE_BRICK_STAIRS);
    Registries.registerBlock(MYCELIUM);
    Registries.registerBlock(LILY_PAD);
    Registries.registerBlock(NETHER_BRICKS);
    Registries.registerBlock(NETHER_BRICK_FENCE);
    Registries.registerBlock(NETHER_BRICK_STAIRS);
    Registries.registerBlock(NETHER_WART);
    Registries.registerBlock(ENCHANTING_TABLE);
    Registries.registerBlock(BREWING_STAND);
    Registries.registerBlock(CAULDRON);
    Registries.registerBlock(END_PORTAL);
    Registries.registerBlock(END_PORTAL_FRAME);
    Registries.registerBlock(END_STONE);
    Registries.registerBlock(DRAGON_EGG);
    Registries.registerBlock(REDSTONE_LAMP);
    Registries.registerBlock(COCOA);
    Registries.registerBlock(SANDSTONE_STAIRS);
    Registries.registerBlock(EMERALD_ORE);
    Registries.registerBlock(ENDER_CHEST);
    Registries.registerBlock(TRIPWIRE_HOOK);
    Registries.registerBlock(TRIPWIRE);
    Registries.registerBlock(EMERALD_BLOCK);
    Registries.registerBlock(SPRUCE_STAIRS);
    Registries.registerBlock(BIRCH_STAIRS);
    Registries.registerBlock(JUNGLE_STAIRS);
    Registries.registerBlock(COMMAND_BLOCK);
    Registries.registerBlock(BEACON);
    Registries.registerBlock(COBBLESTONE_WALL);
    Registries.registerBlock(MOSSY_COBBLESTONE_WALL);
    Registries.registerBlock(FLOWER_POT);
    Registries.registerBlock(POTTED_OAK_SAPLING);
    Registries.registerBlock(POTTED_SPRUCE_SAPLING);
    Registries.registerBlock(POTTED_BIRCH_SAPLING);
    Registries.registerBlock(POTTED_JUNGLE_SAPLING);
    Registries.registerBlock(POTTED_ACACIA_SAPLING);
    Registries.registerBlock(POTTED_DARK_OAK_SAPLING);
    Registries.registerBlock(POTTED_FERN);
    Registries.registerBlock(POTTED_DANDELION);
    Registries.registerBlock(POTTED_POPPY);
    Registries.registerBlock(POTTED_BLUE_ORCHID);
    Registries.registerBlock(POTTED_ALLIUM);
    Registries.registerBlock(POTTED_AZURE_BLUET);
    Registries.registerBlock(POTTED_RED_TULIP);
    Registries.registerBlock(POTTED_ORANGE_TULIP);
    Registries.registerBlock(POTTED_WHITE_TULIP);
    Registries.registerBlock(POTTED_PINK_TULIP);
    Registries.registerBlock(POTTED_OXEYE_DAISY);
    Registries.registerBlock(POTTED_CORNFLOWER);
    Registries.registerBlock(POTTED_LILY_OF_THE_VALLEY);
    Registries.registerBlock(POTTED_WITHER_ROSE);
    Registries.registerBlock(POTTED_RED_MUSHROOM);
    Registries.registerBlock(POTTED_BROWN_MUSHROOM);
    Registries.registerBlock(POTTED_DEAD_BUSH);
    Registries.registerBlock(POTTED_CACTUS);
    Registries.registerBlock(CARROTS);
    Registries.registerBlock(POTATOES);
    Registries.registerBlock(OAK_BUTTON);
    Registries.registerBlock(SPRUCE_BUTTON);
    Registries.registerBlock(BIRCH_BUTTON);
    Registries.registerBlock(JUNGLE_BUTTON);
    Registries.registerBlock(ACACIA_BUTTON);
    Registries.registerBlock(DARK_OAK_BUTTON);
    Registries.registerBlock(SKELETON_SKULL);
    Registries.registerBlock(SKELETON_WALL_SKULL);
    Registries.registerBlock(WITHER_SKELETON_SKULL);
    Registries.registerBlock(WITHER_SKELETON_WALL_SKULL);
    Registries.registerBlock(ZOMBIE_HEAD);
    Registries.registerBlock(ZOMBIE_WALL_HEAD);
    Registries.registerBlock(PLAYER_HEAD);
    Registries.registerBlock(PLAYER_WALL_HEAD);
    Registries.registerBlock(CREEPER_HEAD);
    Registries.registerBlock(CREEPER_WALL_HEAD);
    Registries.registerBlock(DRAGON_HEAD);
    Registries.registerBlock(DRAGON_WALL_HEAD);
    Registries.registerBlock(ANVIL);
    Registries.registerBlock(CHIPPED_ANVIL);
    Registries.registerBlock(DAMAGED_ANVIL);
    Registries.registerBlock(TRAPPED_CHEST);
    Registries.registerBlock(LIGHT_WEIGHTED_PRESSURE_PLATE);
    Registries.registerBlock(HEAVY_WEIGHTED_PRESSURE_PLATE);
    Registries.registerBlock(COMPARATOR);
    Registries.registerBlock(DAYLIGHT_DETECTOR);
    Registries.registerBlock(REDSTONE_BLOCK);
    Registries.registerBlock(NETHER_QUARTZ_ORE);
    Registries.registerBlock(HOPPER);
    Registries.registerBlock(QUARTZ_BLOCK);
    Registries.registerBlock(CHISELED_QUARTZ_BLOCK);
    Registries.registerBlock(QUARTZ_PILLAR);
    Registries.registerBlock(QUARTZ_STAIRS);
    Registries.registerBlock(ACTIVATOR_RAIL);
    Registries.registerBlock(DROPPER);
    Registries.registerBlock(WHITE_TERRACOTTA);
    Registries.registerBlock(ORANGE_TERRACOTTA);
    Registries.registerBlock(MAGENTA_TERRACOTTA);
    Registries.registerBlock(LIGHT_BLUE_TERRACOTTA);
    Registries.registerBlock(YELLOW_TERRACOTTA);
    Registries.registerBlock(LIME_TERRACOTTA);
    Registries.registerBlock(PINK_TERRACOTTA);
    Registries.registerBlock(GRAY_TERRACOTTA);
    Registries.registerBlock(LIGHT_GRAY_TERRACOTTA);
    Registries.registerBlock(CYAN_TERRACOTTA);
    Registries.registerBlock(PURPLE_TERRACOTTA);
    Registries.registerBlock(BLUE_TERRACOTTA);
    Registries.registerBlock(BROWN_TERRACOTTA);
    Registries.registerBlock(GREEN_TERRACOTTA);
    Registries.registerBlock(RED_TERRACOTTA);
    Registries.registerBlock(BLACK_TERRACOTTA);
    Registries.registerBlock(WHITE_STAINED_GLASS_PANE);
    Registries.registerBlock(ORANGE_STAINED_GLASS_PANE);
    Registries.registerBlock(MAGENTA_STAINED_GLASS_PANE);
    Registries.registerBlock(LIGHT_BLUE_STAINED_GLASS_PANE);
    Registries.registerBlock(YELLOW_STAINED_GLASS_PANE);
    Registries.registerBlock(LIME_STAINED_GLASS_PANE);
    Registries.registerBlock(PINK_STAINED_GLASS_PANE);
    Registries.registerBlock(GRAY_STAINED_GLASS_PANE);
    Registries.registerBlock(LIGHT_GRAY_STAINED_GLASS_PANE);
    Registries.registerBlock(CYAN_STAINED_GLASS_PANE);
    Registries.registerBlock(PURPLE_STAINED_GLASS_PANE);
    Registries.registerBlock(BLUE_STAINED_GLASS_PANE);
    Registries.registerBlock(BROWN_STAINED_GLASS_PANE);
    Registries.registerBlock(GREEN_STAINED_GLASS_PANE);
    Registries.registerBlock(RED_STAINED_GLASS_PANE);
    Registries.registerBlock(BLACK_STAINED_GLASS_PANE);
    Registries.registerBlock(ACACIA_STAIRS);
    Registries.registerBlock(DARK_OAK_STAIRS);
    Registries.registerBlock(SLIME_BLOCK);
    Registries.registerBlock(BARRIER);
    Registries.registerBlock(IRON_TRAPDOOR);
    Registries.registerBlock(PRISMARINE);
    Registries.registerBlock(PRISMARINE_BRICKS);
    Registries.registerBlock(DARK_PRISMARINE);
    Registries.registerBlock(PRISMARINE_STAIRS);
    Registries.registerBlock(PRISMARINE_BRICK_STAIRS);
    Registries.registerBlock(DARK_PRISMARINE_STAIRS);
    Registries.registerBlock(PRISMARINE_SLAB);
    Registries.registerBlock(PRISMARINE_BRICK_SLAB);
    Registries.registerBlock(DARK_PRISMARINE_SLAB);
    Registries.registerBlock(SEA_LANTERN);
    Registries.registerBlock(HAY_BLOCK);
    Registries.registerBlock(WHITE_CARPET);
    Registries.registerBlock(ORANGE_CARPET);
    Registries.registerBlock(MAGENTA_CARPET);
    Registries.registerBlock(LIGHT_BLUE_CARPET);
    Registries.registerBlock(YELLOW_CARPET);
    Registries.registerBlock(LIME_CARPET);
    Registries.registerBlock(PINK_CARPET);
    Registries.registerBlock(GRAY_CARPET);
    Registries.registerBlock(LIGHT_GRAY_CARPET);
    Registries.registerBlock(CYAN_CARPET);
    Registries.registerBlock(PURPLE_CARPET);
    Registries.registerBlock(BLUE_CARPET);
    Registries.registerBlock(BROWN_CARPET);
    Registries.registerBlock(GREEN_CARPET);
    Registries.registerBlock(RED_CARPET);
    Registries.registerBlock(BLACK_CARPET);
    Registries.registerBlock(TERRACOTTA);
    Registries.registerBlock(COAL_BLOCK);
    Registries.registerBlock(PACKED_ICE);
    Registries.registerBlock(SUNFLOWER);
    Registries.registerBlock(LILAC);
    Registries.registerBlock(ROSE_BUSH);
    Registries.registerBlock(PEONY);
    Registries.registerBlock(TALL_GRASS);
    Registries.registerBlock(LARGE_FERN);
    Registries.registerBlock(WHITE_BANNER);
    Registries.registerBlock(ORANGE_BANNER);
    Registries.registerBlock(MAGENTA_BANNER);
    Registries.registerBlock(LIGHT_BLUE_BANNER);
    Registries.registerBlock(YELLOW_BANNER);
    Registries.registerBlock(LIME_BANNER);
    Registries.registerBlock(PINK_BANNER);
    Registries.registerBlock(GRAY_BANNER);
    Registries.registerBlock(LIGHT_GRAY_BANNER);
    Registries.registerBlock(CYAN_BANNER);
    Registries.registerBlock(PURPLE_BANNER);
    Registries.registerBlock(BLUE_BANNER);
    Registries.registerBlock(BROWN_BANNER);
    Registries.registerBlock(GREEN_BANNER);
    Registries.registerBlock(RED_BANNER);
    Registries.registerBlock(BLACK_BANNER);
    Registries.registerBlock(WHITE_WALL_BANNER);
    Registries.registerBlock(ORANGE_WALL_BANNER);
    Registries.registerBlock(MAGENTA_WALL_BANNER);
    Registries.registerBlock(LIGHT_BLUE_WALL_BANNER);
    Registries.registerBlock(YELLOW_WALL_BANNER);
    Registries.registerBlock(LIME_WALL_BANNER);
    Registries.registerBlock(PINK_WALL_BANNER);
    Registries.registerBlock(GRAY_WALL_BANNER);
    Registries.registerBlock(LIGHT_GRAY_WALL_BANNER);
    Registries.registerBlock(CYAN_WALL_BANNER);
    Registries.registerBlock(PURPLE_WALL_BANNER);
    Registries.registerBlock(BLUE_WALL_BANNER);
    Registries.registerBlock(BROWN_WALL_BANNER);
    Registries.registerBlock(GREEN_WALL_BANNER);
    Registries.registerBlock(RED_WALL_BANNER);
    Registries.registerBlock(BLACK_WALL_BANNER);
    Registries.registerBlock(RED_SANDSTONE);
    Registries.registerBlock(CHISELED_RED_SANDSTONE);
    Registries.registerBlock(CUT_RED_SANDSTONE);
    Registries.registerBlock(RED_SANDSTONE_STAIRS);
    Registries.registerBlock(OAK_SLAB);
    Registries.registerBlock(SPRUCE_SLAB);
    Registries.registerBlock(BIRCH_SLAB);
    Registries.registerBlock(JUNGLE_SLAB);
    Registries.registerBlock(ACACIA_SLAB);
    Registries.registerBlock(DARK_OAK_SLAB);
    Registries.registerBlock(STONE_SLAB);
    Registries.registerBlock(SMOOTH_STONE_SLAB);
    Registries.registerBlock(SANDSTONE_SLAB);
    Registries.registerBlock(CUT_SANDSTONE_SLAB);
    Registries.registerBlock(PETRIFIED_OAK_SLAB);
    Registries.registerBlock(COBBLESTONE_SLAB);
    Registries.registerBlock(BRICK_SLAB);
    Registries.registerBlock(STONE_BRICK_SLAB);
    Registries.registerBlock(NETHER_BRICK_SLAB);
    Registries.registerBlock(QUARTZ_SLAB);
    Registries.registerBlock(RED_SANDSTONE_SLAB);
    Registries.registerBlock(CUT_RED_SANDSTONE_SLAB);
    Registries.registerBlock(PURPUR_SLAB);
    Registries.registerBlock(SMOOTH_STONE);
    Registries.registerBlock(SMOOTH_SANDSTONE);
    Registries.registerBlock(SMOOTH_QUARTZ);
    Registries.registerBlock(SMOOTH_RED_SANDSTONE);
    Registries.registerBlock(SPRUCE_FENCE_GATE);
    Registries.registerBlock(BIRCH_FENCE_GATE);
    Registries.registerBlock(JUNGLE_FENCE_GATE);
    Registries.registerBlock(ACACIA_FENCE_GATE);
    Registries.registerBlock(DARK_OAK_FENCE_GATE);
    Registries.registerBlock(SPRUCE_FENCE);
    Registries.registerBlock(BIRCH_FENCE);
    Registries.registerBlock(JUNGLE_FENCE);
    Registries.registerBlock(ACACIA_FENCE);
    Registries.registerBlock(DARK_OAK_FENCE);
    Registries.registerBlock(SPRUCE_DOOR);
    Registries.registerBlock(BIRCH_DOOR);
    Registries.registerBlock(JUNGLE_DOOR);
    Registries.registerBlock(ACACIA_DOOR);
    Registries.registerBlock(DARK_OAK_DOOR);
    Registries.registerBlock(END_ROD);
    Registries.registerBlock(CHORUS_PLANT);
    Registries.registerBlock(CHORUS_FLOWER);
    Registries.registerBlock(PURPUR_BLOCK);
    Registries.registerBlock(PURPUR_PILLAR);
    Registries.registerBlock(PURPUR_STAIRS);
    Registries.registerBlock(END_STONE_BRICKS);
    Registries.registerBlock(BEETROOTS);
    Registries.registerBlock(GRASS_PATH);
    Registries.registerBlock(END_GATEWAY);
    Registries.registerBlock(REPEATING_COMMAND_BLOCK);
    Registries.registerBlock(CHAIN_COMMAND_BLOCK);
    Registries.registerBlock(FROSTED_ICE);
    Registries.registerBlock(MAGMA_BLOCK);
    Registries.registerBlock(NETHER_WART_BLOCK);
    Registries.registerBlock(RED_NETHER_BRICKS);
    Registries.registerBlock(BONE_BLOCK);
    Registries.registerBlock(STRUCTURE_VOID);
    Registries.registerBlock(OBSERVER);
    Registries.registerBlock(SHULKER_BOX);
    Registries.registerBlock(WHITE_SHULKER_BOX);
    Registries.registerBlock(ORANGE_SHULKER_BOX);
    Registries.registerBlock(MAGENTA_SHULKER_BOX);
    Registries.registerBlock(LIGHT_BLUE_SHULKER_BOX);
    Registries.registerBlock(YELLOW_SHULKER_BOX);
    Registries.registerBlock(LIME_SHULKER_BOX);
    Registries.registerBlock(PINK_SHULKER_BOX);
    Registries.registerBlock(GRAY_SHULKER_BOX);
    Registries.registerBlock(LIGHT_GRAY_SHULKER_BOX);
    Registries.registerBlock(CYAN_SHULKER_BOX);
    Registries.registerBlock(PURPLE_SHULKER_BOX);
    Registries.registerBlock(BLUE_SHULKER_BOX);
    Registries.registerBlock(BROWN_SHULKER_BOX);
    Registries.registerBlock(GREEN_SHULKER_BOX);
    Registries.registerBlock(RED_SHULKER_BOX);
    Registries.registerBlock(BLACK_SHULKER_BOX);
    Registries.registerBlock(WHITE_GLAZED_TERRACOTTA);
    Registries.registerBlock(ORANGE_GLAZED_TERRACOTTA);
    Registries.registerBlock(MAGENTA_GLAZED_TERRACOTTA);
    Registries.registerBlock(LIGHT_BLUE_GLAZED_TERRACOTTA);
    Registries.registerBlock(YELLOW_GLAZED_TERRACOTTA);
    Registries.registerBlock(LIME_GLAZED_TERRACOTTA);
    Registries.registerBlock(PINK_GLAZED_TERRACOTTA);
    Registries.registerBlock(GRAY_GLAZED_TERRACOTTA);
    Registries.registerBlock(LIGHT_GRAY_GLAZED_TERRACOTTA);
    Registries.registerBlock(CYAN_GLAZED_TERRACOTTA);
    Registries.registerBlock(PURPLE_GLAZED_TERRACOTTA);
    Registries.registerBlock(BLUE_GLAZED_TERRACOTTA);
    Registries.registerBlock(BROWN_GLAZED_TERRACOTTA);
    Registries.registerBlock(GREEN_GLAZED_TERRACOTTA);
    Registries.registerBlock(RED_GLAZED_TERRACOTTA);
    Registries.registerBlock(BLACK_GLAZED_TERRACOTTA);
    Registries.registerBlock(WHITE_CONCRETE);
    Registries.registerBlock(ORANGE_CONCRETE);
    Registries.registerBlock(MAGENTA_CONCRETE);
    Registries.registerBlock(LIGHT_BLUE_CONCRETE);
    Registries.registerBlock(YELLOW_CONCRETE);
    Registries.registerBlock(LIME_CONCRETE);
    Registries.registerBlock(PINK_CONCRETE);
    Registries.registerBlock(GRAY_CONCRETE);
    Registries.registerBlock(LIGHT_GRAY_CONCRETE);
    Registries.registerBlock(CYAN_CONCRETE);
    Registries.registerBlock(PURPLE_CONCRETE);
    Registries.registerBlock(BLUE_CONCRETE);
    Registries.registerBlock(BROWN_CONCRETE);
    Registries.registerBlock(GREEN_CONCRETE);
    Registries.registerBlock(RED_CONCRETE);
    Registries.registerBlock(BLACK_CONCRETE);
    Registries.registerBlock(WHITE_CONCRETE_POWDER);
    Registries.registerBlock(ORANGE_CONCRETE_POWDER);
    Registries.registerBlock(MAGENTA_CONCRETE_POWDER);
    Registries.registerBlock(LIGHT_BLUE_CONCRETE_POWDER);
    Registries.registerBlock(YELLOW_CONCRETE_POWDER);
    Registries.registerBlock(LIME_CONCRETE_POWDER);
    Registries.registerBlock(PINK_CONCRETE_POWDER);
    Registries.registerBlock(GRAY_CONCRETE_POWDER);
    Registries.registerBlock(LIGHT_GRAY_CONCRETE_POWDER);
    Registries.registerBlock(CYAN_CONCRETE_POWDER);
    Registries.registerBlock(PURPLE_CONCRETE_POWDER);
    Registries.registerBlock(BLUE_CONCRETE_POWDER);
    Registries.registerBlock(BROWN_CONCRETE_POWDER);
    Registries.registerBlock(GREEN_CONCRETE_POWDER);
    Registries.registerBlock(RED_CONCRETE_POWDER);
    Registries.registerBlock(BLACK_CONCRETE_POWDER);
    Registries.registerBlock(KELP);
    Registries.registerBlock(KELP_PLANT);
    Registries.registerBlock(DRIED_KELP_BLOCK);
    Registries.registerBlock(TURTLE_EGG);
    Registries.registerBlock(DEAD_TUBE_CORAL_BLOCK);
    Registries.registerBlock(DEAD_BRAIN_CORAL_BLOCK);
    Registries.registerBlock(DEAD_BUBBLE_CORAL_BLOCK);
    Registries.registerBlock(DEAD_FIRE_CORAL_BLOCK);
    Registries.registerBlock(DEAD_HORN_CORAL_BLOCK);
    Registries.registerBlock(TUBE_CORAL_BLOCK);
    Registries.registerBlock(BRAIN_CORAL_BLOCK);
    Registries.registerBlock(BUBBLE_CORAL_BLOCK);
    Registries.registerBlock(FIRE_CORAL_BLOCK);
    Registries.registerBlock(HORN_CORAL_BLOCK);
    Registries.registerBlock(DEAD_TUBE_CORAL);
    Registries.registerBlock(DEAD_BRAIN_CORAL);
    Registries.registerBlock(DEAD_BUBBLE_CORAL);
    Registries.registerBlock(DEAD_FIRE_CORAL);
    Registries.registerBlock(DEAD_HORN_CORAL);
    Registries.registerBlock(TUBE_CORAL);
    Registries.registerBlock(BRAIN_CORAL);
    Registries.registerBlock(BUBBLE_CORAL);
    Registries.registerBlock(FIRE_CORAL);
    Registries.registerBlock(HORN_CORAL);
    Registries.registerBlock(DEAD_TUBE_CORAL_FAN);
    Registries.registerBlock(DEAD_BRAIN_CORAL_FAN);
    Registries.registerBlock(DEAD_BUBBLE_CORAL_FAN);
    Registries.registerBlock(DEAD_FIRE_CORAL_FAN);
    Registries.registerBlock(DEAD_HORN_CORAL_FAN);
    Registries.registerBlock(TUBE_CORAL_FAN);
    Registries.registerBlock(BRAIN_CORAL_FAN);
    Registries.registerBlock(BUBBLE_CORAL_FAN);
    Registries.registerBlock(FIRE_CORAL_FAN);
    Registries.registerBlock(HORN_CORAL_FAN);
    Registries.registerBlock(DEAD_TUBE_CORAL_WALL_FAN);
    Registries.registerBlock(DEAD_BRAIN_CORAL_WALL_FAN);
    Registries.registerBlock(DEAD_BUBBLE_CORAL_WALL_FAN);
    Registries.registerBlock(DEAD_FIRE_CORAL_WALL_FAN);
    Registries.registerBlock(DEAD_HORN_CORAL_WALL_FAN);
    Registries.registerBlock(TUBE_CORAL_WALL_FAN);
    Registries.registerBlock(BRAIN_CORAL_WALL_FAN);
    Registries.registerBlock(BUBBLE_CORAL_WALL_FAN);
    Registries.registerBlock(FIRE_CORAL_WALL_FAN);
    Registries.registerBlock(HORN_CORAL_WALL_FAN);
    Registries.registerBlock(SEA_PICKLE);
    Registries.registerBlock(BLUE_ICE);
    Registries.registerBlock(CONDUIT);
    Registries.registerBlock(BAMBOO_SAPLING);
    Registries.registerBlock(BAMBOO);
    Registries.registerBlock(POTTED_BAMBOO);
    Registries.registerBlock(VOID_AIR);
    Registries.registerBlock(CAVE_AIR);
    Registries.registerBlock(BUBBLE_COLUMN);
    Registries.registerBlock(POLISHED_GRANITE_STAIRS);
    Registries.registerBlock(SMOOTH_RED_SANDSTONE_STAIRS);
    Registries.registerBlock(MOSSY_STONE_BRICK_STAIRS);
    Registries.registerBlock(POLISHED_DIORITE_STAIRS);
    Registries.registerBlock(MOSSY_COBBLESTONE_STAIRS);
    Registries.registerBlock(END_STONE_BRICK_STAIRS);
    Registries.registerBlock(STONE_STAIRS);
    Registries.registerBlock(SMOOTH_SANDSTONE_STAIRS);
    Registries.registerBlock(SMOOTH_QUARTZ_STAIRS);
    Registries.registerBlock(GRANITE_STAIRS);
    Registries.registerBlock(ANDESITE_STAIRS);
    Registries.registerBlock(RED_NETHER_BRICK_STAIRS);
    Registries.registerBlock(POLISHED_ANDESITE_STAIRS);
    Registries.registerBlock(DIORITE_STAIRS);
    Registries.registerBlock(POLISHED_GRANITE_SLAB);
    Registries.registerBlock(SMOOTH_RED_SANDSTONE_SLAB);
    Registries.registerBlock(MOSSY_STONE_BRICK_SLAB);
    Registries.registerBlock(POLISHED_DIORITE_SLAB);
    Registries.registerBlock(MOSSY_COBBLESTONE_SLAB);
    Registries.registerBlock(END_STONE_BRICK_SLAB);
    Registries.registerBlock(SMOOTH_SANDSTONE_SLAB);
    Registries.registerBlock(SMOOTH_QUARTZ_SLAB);
    Registries.registerBlock(GRANITE_SLAB);
    Registries.registerBlock(ANDESITE_SLAB);
    Registries.registerBlock(RED_NETHER_BRICK_SLAB);
    Registries.registerBlock(POLISHED_ANDESITE_SLAB);
    Registries.registerBlock(DIORITE_SLAB);
    Registries.registerBlock(BRICK_WALL);
    Registries.registerBlock(PRISMARINE_WALL);
    Registries.registerBlock(RED_SANDSTONE_WALL);
    Registries.registerBlock(MOSSY_STONE_BRICK_WALL);
    Registries.registerBlock(GRANITE_WALL);
    Registries.registerBlock(STONE_BRICK_WALL);
    Registries.registerBlock(NETHER_BRICK_WALL);
    Registries.registerBlock(ANDESITE_WALL);
    Registries.registerBlock(RED_NETHER_BRICK_WALL);
    Registries.registerBlock(SANDSTONE_WALL);
    Registries.registerBlock(END_STONE_BRICK_WALL);
    Registries.registerBlock(DIORITE_WALL);
    Registries.registerBlock(SCAFFOLDING);
    Registries.registerBlock(LOOM);
    Registries.registerBlock(BARREL);
    Registries.registerBlock(SMOKER);
    Registries.registerBlock(BLAST_FURNACE);
    Registries.registerBlock(CARTOGRAPHY_TABLE);
    Registries.registerBlock(FLETCHING_TABLE);
    Registries.registerBlock(GRINDSTONE);
    Registries.registerBlock(LECTERN);
    Registries.registerBlock(SMITHING_TABLE);
    Registries.registerBlock(STONECUTTER);
    Registries.registerBlock(BELL);
    Registries.registerBlock(LANTERN);
    Registries.registerBlock(SOUL_LANTERN);
    Registries.registerBlock(CAMPFIRE);
    Registries.registerBlock(SOUL_CAMPFIRE);
    Registries.registerBlock(SWEET_BERRY_BUSH);
    Registries.registerBlock(WARPED_STEM);
    Registries.registerBlock(STRIPPED_WARPED_STEM);
    Registries.registerBlock(WARPED_HYPHAE);
    Registries.registerBlock(STRIPPED_WARPED_HYPHAE);
    Registries.registerBlock(WARPED_NYLIUM);
    Registries.registerBlock(WARPED_FUNGUS);
    Registries.registerBlock(WARPED_WART_BLOCK);
    Registries.registerBlock(WARPED_ROOTS);
    Registries.registerBlock(NETHER_SPROUTS);
    Registries.registerBlock(CRIMSON_STEM);
    Registries.registerBlock(STRIPPED_CRIMSON_STEM);
    Registries.registerBlock(CRIMSON_HYPHAE);
    Registries.registerBlock(STRIPPED_CRIMSON_HYPHAE);
    Registries.registerBlock(CRIMSON_NYLIUM);
    Registries.registerBlock(CRIMSON_FUNGUS);
    Registries.registerBlock(SHROOMLIGHT);
    Registries.registerBlock(WEEPING_VINES);
    Registries.registerBlock(WEEPING_VINES_PLANT);
    Registries.registerBlock(TWISTING_VINES);
    Registries.registerBlock(TWISTING_VINES_PLANT);
    Registries.registerBlock(CRIMSON_ROOTS);
    Registries.registerBlock(CRIMSON_PLANKS);
    Registries.registerBlock(WARPED_PLANKS);
    Registries.registerBlock(CRIMSON_SLAB);
    Registries.registerBlock(WARPED_SLAB);
    Registries.registerBlock(CRIMSON_PRESSURE_PLATE);
    Registries.registerBlock(WARPED_PRESSURE_PLATE);
    Registries.registerBlock(CRIMSON_FENCE);
    Registries.registerBlock(WARPED_FENCE);
    Registries.registerBlock(CRIMSON_TRAPDOOR);
    Registries.registerBlock(WARPED_TRAPDOOR);
    Registries.registerBlock(CRIMSON_FENCE_GATE);
    Registries.registerBlock(WARPED_FENCE_GATE);
    Registries.registerBlock(CRIMSON_STAIRS);
    Registries.registerBlock(WARPED_STAIRS);
    Registries.registerBlock(CRIMSON_BUTTON);
    Registries.registerBlock(WARPED_BUTTON);
    Registries.registerBlock(CRIMSON_DOOR);
    Registries.registerBlock(WARPED_DOOR);
    Registries.registerBlock(CRIMSON_SIGN);
    Registries.registerBlock(WARPED_SIGN);
    Registries.registerBlock(CRIMSON_WALL_SIGN);
    Registries.registerBlock(WARPED_WALL_SIGN);
    Registries.registerBlock(STRUCTURE_BLOCK);
    Registries.registerBlock(JIGSAW);
    Registries.registerBlock(COMPOSTER);
    Registries.registerBlock(TARGET);
    Registries.registerBlock(BEE_NEST);
    Registries.registerBlock(BEEHIVE);
    Registries.registerBlock(HONEY_BLOCK);
    Registries.registerBlock(HONEYCOMB_BLOCK);
    Registries.registerBlock(NETHERITE_BLOCK);
    Registries.registerBlock(ANCIENT_DEBRIS);
    Registries.registerBlock(CRYING_OBSIDIAN);
    Registries.registerBlock(RESPAWN_ANCHOR);
    Registries.registerBlock(POTTED_CRIMSON_FUNGUS);
    Registries.registerBlock(POTTED_WARPED_FUNGUS);
    Registries.registerBlock(POTTED_CRIMSON_ROOTS);
    Registries.registerBlock(POTTED_WARPED_ROOTS);
    Registries.registerBlock(LODESTONE);
    Registries.registerBlock(BLACKSTONE);
    Registries.registerBlock(BLACKSTONE_STAIRS);
    Registries.registerBlock(BLACKSTONE_WALL);
    Registries.registerBlock(BLACKSTONE_SLAB);
    Registries.registerBlock(POLISHED_BLACKSTONE);
    Registries.registerBlock(POLISHED_BLACKSTONE_BRICKS);
    Registries.registerBlock(CRACKED_POLISHED_BLACKSTONE_BRICKS);
    Registries.registerBlock(CHISELED_POLISHED_BLACKSTONE);
    Registries.registerBlock(POLISHED_BLACKSTONE_BRICK_SLAB);
    Registries.registerBlock(POLISHED_BLACKSTONE_BRICK_STAIRS);
    Registries.registerBlock(POLISHED_BLACKSTONE_BRICK_WALL);
    Registries.registerBlock(GILDED_BLACKSTONE);
    Registries.registerBlock(POLISHED_BLACKSTONE_STAIRS);
    Registries.registerBlock(POLISHED_BLACKSTONE_SLAB);
    Registries.registerBlock(POLISHED_BLACKSTONE_PRESSURE_PLATE);
    Registries.registerBlock(POLISHED_BLACKSTONE_BUTTON);
    Registries.registerBlock(POLISHED_BLACKSTONE_WALL);
    Registries.registerBlock(CHISELED_NETHER_BRICKS);
    Registries.registerBlock(CRACKED_NETHER_BRICKS);
    Registries.registerBlock(QUARTZ_BRICKS);
  }

  @NotNull
  private final NamespaceID id;

  private final short defaultBlockState;

  @NotNull
  private final List<BlockState> blockStates = new ArrayList<>();

  @NotNull
  private final RawBlockData blockData = new RawBlockData();

  protected Block(@NotNull NamespaceID id, short defaultBlockState) {
    this.id = id;
    this.defaultBlockState = defaultBlockState;
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
    return Registries.getBlockState(defaultBlockState);
  }

  @NotNull
  public final List<BlockState> getBlockStates() {
    return this.blockStates;
  }

  @NotNull
  public final RawBlockData getBlockData() {
    return this.blockData;
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
    return Registries.getBlockState(id).getBlock();
  }

  public int getNumericalId() {
    return Registries.getBlockId(this);
  }

  @NotNull
  public static Block fromId(int id) {
    return Registries.getBlock(id);
  }

  @NotNull
  public static Block fromId(Key id) {
    return Registries.getBlock(id);
  }

  @NotNull
  public static List<Block> values() {
    return Registries.getBlocks();
  }
}
