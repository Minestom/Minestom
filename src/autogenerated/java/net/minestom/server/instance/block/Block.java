package net.minestom.server.instance.block;

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
import net.minestom.server.instance.block.states.AcaciaPressurePlate;
import net.minestom.server.instance.block.states.AcaciaSapling;
import net.minestom.server.instance.block.states.AcaciaSign;
import net.minestom.server.instance.block.states.AcaciaSlab;
import net.minestom.server.instance.block.states.AcaciaStairs;
import net.minestom.server.instance.block.states.AcaciaTrapdoor;
import net.minestom.server.instance.block.states.AcaciaWallSign;
import net.minestom.server.instance.block.states.AcaciaWood;
import net.minestom.server.instance.block.states.ActivatorRail;
import net.minestom.server.instance.block.states.AndesiteSlab;
import net.minestom.server.instance.block.states.AndesiteStairs;
import net.minestom.server.instance.block.states.AndesiteWall;
import net.minestom.server.instance.block.states.Anvil;
import net.minestom.server.instance.block.states.AttachedMelonStem;
import net.minestom.server.instance.block.states.AttachedPumpkinStem;
import net.minestom.server.instance.block.states.Bamboo;
import net.minestom.server.instance.block.states.Barrel;
import net.minestom.server.instance.block.states.Basalt;
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
import net.minestom.server.instance.block.states.BlackGlazedTerracotta;
import net.minestom.server.instance.block.states.BlackShulkerBox;
import net.minestom.server.instance.block.states.BlackStainedGlassPane;
import net.minestom.server.instance.block.states.BlackWallBanner;
import net.minestom.server.instance.block.states.BlackstoneSlab;
import net.minestom.server.instance.block.states.BlackstoneStairs;
import net.minestom.server.instance.block.states.BlackstoneWall;
import net.minestom.server.instance.block.states.BlastFurnace;
import net.minestom.server.instance.block.states.BlueBanner;
import net.minestom.server.instance.block.states.BlueBed;
import net.minestom.server.instance.block.states.BlueGlazedTerracotta;
import net.minestom.server.instance.block.states.BlueShulkerBox;
import net.minestom.server.instance.block.states.BlueStainedGlassPane;
import net.minestom.server.instance.block.states.BlueWallBanner;
import net.minestom.server.instance.block.states.BoneBlock;
import net.minestom.server.instance.block.states.BrainCoral;
import net.minestom.server.instance.block.states.BrainCoralFan;
import net.minestom.server.instance.block.states.BrainCoralWallFan;
import net.minestom.server.instance.block.states.BrewingStand;
import net.minestom.server.instance.block.states.BrickSlab;
import net.minestom.server.instance.block.states.BrickStairs;
import net.minestom.server.instance.block.states.BrickWall;
import net.minestom.server.instance.block.states.BrownBanner;
import net.minestom.server.instance.block.states.BrownBed;
import net.minestom.server.instance.block.states.BrownGlazedTerracotta;
import net.minestom.server.instance.block.states.BrownMushroomBlock;
import net.minestom.server.instance.block.states.BrownShulkerBox;
import net.minestom.server.instance.block.states.BrownStainedGlassPane;
import net.minestom.server.instance.block.states.BrownWallBanner;
import net.minestom.server.instance.block.states.BubbleColumn;
import net.minestom.server.instance.block.states.BubbleCoral;
import net.minestom.server.instance.block.states.BubbleCoralFan;
import net.minestom.server.instance.block.states.BubbleCoralWallFan;
import net.minestom.server.instance.block.states.Cactus;
import net.minestom.server.instance.block.states.Cake;
import net.minestom.server.instance.block.states.Campfire;
import net.minestom.server.instance.block.states.Carrots;
import net.minestom.server.instance.block.states.CarvedPumpkin;
import net.minestom.server.instance.block.states.Cauldron;
import net.minestom.server.instance.block.states.Chain;
import net.minestom.server.instance.block.states.ChainCommandBlock;
import net.minestom.server.instance.block.states.Chest;
import net.minestom.server.instance.block.states.ChippedAnvil;
import net.minestom.server.instance.block.states.ChorusFlower;
import net.minestom.server.instance.block.states.ChorusPlant;
import net.minestom.server.instance.block.states.CobblestoneSlab;
import net.minestom.server.instance.block.states.CobblestoneStairs;
import net.minestom.server.instance.block.states.CobblestoneWall;
import net.minestom.server.instance.block.states.Cocoa;
import net.minestom.server.instance.block.states.CommandBlock;
import net.minestom.server.instance.block.states.Comparator;
import net.minestom.server.instance.block.states.Composter;
import net.minestom.server.instance.block.states.Conduit;
import net.minestom.server.instance.block.states.CreeperHead;
import net.minestom.server.instance.block.states.CreeperWallHead;
import net.minestom.server.instance.block.states.CrimsonButton;
import net.minestom.server.instance.block.states.CrimsonDoor;
import net.minestom.server.instance.block.states.CrimsonFence;
import net.minestom.server.instance.block.states.CrimsonFenceGate;
import net.minestom.server.instance.block.states.CrimsonHyphae;
import net.minestom.server.instance.block.states.CrimsonPressurePlate;
import net.minestom.server.instance.block.states.CrimsonSign;
import net.minestom.server.instance.block.states.CrimsonSlab;
import net.minestom.server.instance.block.states.CrimsonStairs;
import net.minestom.server.instance.block.states.CrimsonStem;
import net.minestom.server.instance.block.states.CrimsonTrapdoor;
import net.minestom.server.instance.block.states.CrimsonWallSign;
import net.minestom.server.instance.block.states.CutRedSandstoneSlab;
import net.minestom.server.instance.block.states.CutSandstoneSlab;
import net.minestom.server.instance.block.states.CyanBanner;
import net.minestom.server.instance.block.states.CyanBed;
import net.minestom.server.instance.block.states.CyanGlazedTerracotta;
import net.minestom.server.instance.block.states.CyanShulkerBox;
import net.minestom.server.instance.block.states.CyanStainedGlassPane;
import net.minestom.server.instance.block.states.CyanWallBanner;
import net.minestom.server.instance.block.states.DamagedAnvil;
import net.minestom.server.instance.block.states.DarkOakButton;
import net.minestom.server.instance.block.states.DarkOakDoor;
import net.minestom.server.instance.block.states.DarkOakFence;
import net.minestom.server.instance.block.states.DarkOakFenceGate;
import net.minestom.server.instance.block.states.DarkOakLeaves;
import net.minestom.server.instance.block.states.DarkOakLog;
import net.minestom.server.instance.block.states.DarkOakPressurePlate;
import net.minestom.server.instance.block.states.DarkOakSapling;
import net.minestom.server.instance.block.states.DarkOakSign;
import net.minestom.server.instance.block.states.DarkOakSlab;
import net.minestom.server.instance.block.states.DarkOakStairs;
import net.minestom.server.instance.block.states.DarkOakTrapdoor;
import net.minestom.server.instance.block.states.DarkOakWallSign;
import net.minestom.server.instance.block.states.DarkOakWood;
import net.minestom.server.instance.block.states.DarkPrismarineSlab;
import net.minestom.server.instance.block.states.DarkPrismarineStairs;
import net.minestom.server.instance.block.states.DaylightDetector;
import net.minestom.server.instance.block.states.DeadBrainCoral;
import net.minestom.server.instance.block.states.DeadBrainCoralFan;
import net.minestom.server.instance.block.states.DeadBrainCoralWallFan;
import net.minestom.server.instance.block.states.DeadBubbleCoral;
import net.minestom.server.instance.block.states.DeadBubbleCoralFan;
import net.minestom.server.instance.block.states.DeadBubbleCoralWallFan;
import net.minestom.server.instance.block.states.DeadFireCoral;
import net.minestom.server.instance.block.states.DeadFireCoralFan;
import net.minestom.server.instance.block.states.DeadFireCoralWallFan;
import net.minestom.server.instance.block.states.DeadHornCoral;
import net.minestom.server.instance.block.states.DeadHornCoralFan;
import net.minestom.server.instance.block.states.DeadHornCoralWallFan;
import net.minestom.server.instance.block.states.DeadTubeCoral;
import net.minestom.server.instance.block.states.DeadTubeCoralFan;
import net.minestom.server.instance.block.states.DeadTubeCoralWallFan;
import net.minestom.server.instance.block.states.DetectorRail;
import net.minestom.server.instance.block.states.DioriteSlab;
import net.minestom.server.instance.block.states.DioriteStairs;
import net.minestom.server.instance.block.states.DioriteWall;
import net.minestom.server.instance.block.states.Dispenser;
import net.minestom.server.instance.block.states.DragonHead;
import net.minestom.server.instance.block.states.DragonWallHead;
import net.minestom.server.instance.block.states.Dropper;
import net.minestom.server.instance.block.states.EndPortalFrame;
import net.minestom.server.instance.block.states.EndRod;
import net.minestom.server.instance.block.states.EndStoneBrickSlab;
import net.minestom.server.instance.block.states.EndStoneBrickStairs;
import net.minestom.server.instance.block.states.EndStoneBrickWall;
import net.minestom.server.instance.block.states.EnderChest;
import net.minestom.server.instance.block.states.Farmland;
import net.minestom.server.instance.block.states.Fire;
import net.minestom.server.instance.block.states.FireCoral;
import net.minestom.server.instance.block.states.FireCoralFan;
import net.minestom.server.instance.block.states.FireCoralWallFan;
import net.minestom.server.instance.block.states.FrostedIce;
import net.minestom.server.instance.block.states.Furnace;
import net.minestom.server.instance.block.states.GlassPane;
import net.minestom.server.instance.block.states.GraniteSlab;
import net.minestom.server.instance.block.states.GraniteStairs;
import net.minestom.server.instance.block.states.GraniteWall;
import net.minestom.server.instance.block.states.GrassBlock;
import net.minestom.server.instance.block.states.GrayBanner;
import net.minestom.server.instance.block.states.GrayBed;
import net.minestom.server.instance.block.states.GrayGlazedTerracotta;
import net.minestom.server.instance.block.states.GrayShulkerBox;
import net.minestom.server.instance.block.states.GrayStainedGlassPane;
import net.minestom.server.instance.block.states.GrayWallBanner;
import net.minestom.server.instance.block.states.GreenBanner;
import net.minestom.server.instance.block.states.GreenBed;
import net.minestom.server.instance.block.states.GreenGlazedTerracotta;
import net.minestom.server.instance.block.states.GreenShulkerBox;
import net.minestom.server.instance.block.states.GreenStainedGlassPane;
import net.minestom.server.instance.block.states.GreenWallBanner;
import net.minestom.server.instance.block.states.Grindstone;
import net.minestom.server.instance.block.states.HayBlock;
import net.minestom.server.instance.block.states.HeavyWeightedPressurePlate;
import net.minestom.server.instance.block.states.Hopper;
import net.minestom.server.instance.block.states.HornCoral;
import net.minestom.server.instance.block.states.HornCoralFan;
import net.minestom.server.instance.block.states.HornCoralWallFan;
import net.minestom.server.instance.block.states.IronBars;
import net.minestom.server.instance.block.states.IronDoor;
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
import net.minestom.server.instance.block.states.JunglePressurePlate;
import net.minestom.server.instance.block.states.JungleSapling;
import net.minestom.server.instance.block.states.JungleSign;
import net.minestom.server.instance.block.states.JungleSlab;
import net.minestom.server.instance.block.states.JungleStairs;
import net.minestom.server.instance.block.states.JungleTrapdoor;
import net.minestom.server.instance.block.states.JungleWallSign;
import net.minestom.server.instance.block.states.JungleWood;
import net.minestom.server.instance.block.states.Kelp;
import net.minestom.server.instance.block.states.Ladder;
import net.minestom.server.instance.block.states.Lantern;
import net.minestom.server.instance.block.states.LargeFern;
import net.minestom.server.instance.block.states.Lava;
import net.minestom.server.instance.block.states.Lectern;
import net.minestom.server.instance.block.states.Lever;
import net.minestom.server.instance.block.states.LightBlueBanner;
import net.minestom.server.instance.block.states.LightBlueBed;
import net.minestom.server.instance.block.states.LightBlueGlazedTerracotta;
import net.minestom.server.instance.block.states.LightBlueShulkerBox;
import net.minestom.server.instance.block.states.LightBlueStainedGlassPane;
import net.minestom.server.instance.block.states.LightBlueWallBanner;
import net.minestom.server.instance.block.states.LightGrayBanner;
import net.minestom.server.instance.block.states.LightGrayBed;
import net.minestom.server.instance.block.states.LightGrayGlazedTerracotta;
import net.minestom.server.instance.block.states.LightGrayShulkerBox;
import net.minestom.server.instance.block.states.LightGrayStainedGlassPane;
import net.minestom.server.instance.block.states.LightGrayWallBanner;
import net.minestom.server.instance.block.states.LightWeightedPressurePlate;
import net.minestom.server.instance.block.states.Lilac;
import net.minestom.server.instance.block.states.LimeBanner;
import net.minestom.server.instance.block.states.LimeBed;
import net.minestom.server.instance.block.states.LimeGlazedTerracotta;
import net.minestom.server.instance.block.states.LimeShulkerBox;
import net.minestom.server.instance.block.states.LimeStainedGlassPane;
import net.minestom.server.instance.block.states.LimeWallBanner;
import net.minestom.server.instance.block.states.Loom;
import net.minestom.server.instance.block.states.MagentaBanner;
import net.minestom.server.instance.block.states.MagentaBed;
import net.minestom.server.instance.block.states.MagentaGlazedTerracotta;
import net.minestom.server.instance.block.states.MagentaShulkerBox;
import net.minestom.server.instance.block.states.MagentaStainedGlassPane;
import net.minestom.server.instance.block.states.MagentaWallBanner;
import net.minestom.server.instance.block.states.MelonStem;
import net.minestom.server.instance.block.states.MossyCobblestoneSlab;
import net.minestom.server.instance.block.states.MossyCobblestoneStairs;
import net.minestom.server.instance.block.states.MossyCobblestoneWall;
import net.minestom.server.instance.block.states.MossyStoneBrickSlab;
import net.minestom.server.instance.block.states.MossyStoneBrickStairs;
import net.minestom.server.instance.block.states.MossyStoneBrickWall;
import net.minestom.server.instance.block.states.MovingPiston;
import net.minestom.server.instance.block.states.MushroomStem;
import net.minestom.server.instance.block.states.Mycelium;
import net.minestom.server.instance.block.states.NetherBrickFence;
import net.minestom.server.instance.block.states.NetherBrickSlab;
import net.minestom.server.instance.block.states.NetherBrickStairs;
import net.minestom.server.instance.block.states.NetherBrickWall;
import net.minestom.server.instance.block.states.NetherPortal;
import net.minestom.server.instance.block.states.NetherWart;
import net.minestom.server.instance.block.states.NoteBlock;
import net.minestom.server.instance.block.states.OakButton;
import net.minestom.server.instance.block.states.OakDoor;
import net.minestom.server.instance.block.states.OakFence;
import net.minestom.server.instance.block.states.OakFenceGate;
import net.minestom.server.instance.block.states.OakLeaves;
import net.minestom.server.instance.block.states.OakLog;
import net.minestom.server.instance.block.states.OakPressurePlate;
import net.minestom.server.instance.block.states.OakSapling;
import net.minestom.server.instance.block.states.OakSign;
import net.minestom.server.instance.block.states.OakSlab;
import net.minestom.server.instance.block.states.OakStairs;
import net.minestom.server.instance.block.states.OakTrapdoor;
import net.minestom.server.instance.block.states.OakWallSign;
import net.minestom.server.instance.block.states.OakWood;
import net.minestom.server.instance.block.states.Observer;
import net.minestom.server.instance.block.states.OrangeBanner;
import net.minestom.server.instance.block.states.OrangeBed;
import net.minestom.server.instance.block.states.OrangeGlazedTerracotta;
import net.minestom.server.instance.block.states.OrangeShulkerBox;
import net.minestom.server.instance.block.states.OrangeStainedGlassPane;
import net.minestom.server.instance.block.states.OrangeWallBanner;
import net.minestom.server.instance.block.states.Peony;
import net.minestom.server.instance.block.states.PetrifiedOakSlab;
import net.minestom.server.instance.block.states.PinkBanner;
import net.minestom.server.instance.block.states.PinkBed;
import net.minestom.server.instance.block.states.PinkGlazedTerracotta;
import net.minestom.server.instance.block.states.PinkShulkerBox;
import net.minestom.server.instance.block.states.PinkStainedGlassPane;
import net.minestom.server.instance.block.states.PinkWallBanner;
import net.minestom.server.instance.block.states.Piston;
import net.minestom.server.instance.block.states.PistonHead;
import net.minestom.server.instance.block.states.PlayerHead;
import net.minestom.server.instance.block.states.PlayerWallHead;
import net.minestom.server.instance.block.states.Podzol;
import net.minestom.server.instance.block.states.PolishedAndesiteSlab;
import net.minestom.server.instance.block.states.PolishedAndesiteStairs;
import net.minestom.server.instance.block.states.PolishedBasalt;
import net.minestom.server.instance.block.states.PolishedBlackstoneBrickSlab;
import net.minestom.server.instance.block.states.PolishedBlackstoneBrickStairs;
import net.minestom.server.instance.block.states.PolishedBlackstoneBrickWall;
import net.minestom.server.instance.block.states.PolishedBlackstoneButton;
import net.minestom.server.instance.block.states.PolishedBlackstonePressurePlate;
import net.minestom.server.instance.block.states.PolishedBlackstoneSlab;
import net.minestom.server.instance.block.states.PolishedBlackstoneStairs;
import net.minestom.server.instance.block.states.PolishedBlackstoneWall;
import net.minestom.server.instance.block.states.PolishedDioriteSlab;
import net.minestom.server.instance.block.states.PolishedDioriteStairs;
import net.minestom.server.instance.block.states.PolishedGraniteSlab;
import net.minestom.server.instance.block.states.PolishedGraniteStairs;
import net.minestom.server.instance.block.states.Potatoes;
import net.minestom.server.instance.block.states.PoweredRail;
import net.minestom.server.instance.block.states.PrismarineBrickSlab;
import net.minestom.server.instance.block.states.PrismarineBrickStairs;
import net.minestom.server.instance.block.states.PrismarineSlab;
import net.minestom.server.instance.block.states.PrismarineStairs;
import net.minestom.server.instance.block.states.PrismarineWall;
import net.minestom.server.instance.block.states.PumpkinStem;
import net.minestom.server.instance.block.states.PurpleBanner;
import net.minestom.server.instance.block.states.PurpleBed;
import net.minestom.server.instance.block.states.PurpleGlazedTerracotta;
import net.minestom.server.instance.block.states.PurpleShulkerBox;
import net.minestom.server.instance.block.states.PurpleStainedGlassPane;
import net.minestom.server.instance.block.states.PurpleWallBanner;
import net.minestom.server.instance.block.states.PurpurPillar;
import net.minestom.server.instance.block.states.PurpurSlab;
import net.minestom.server.instance.block.states.PurpurStairs;
import net.minestom.server.instance.block.states.QuartzPillar;
import net.minestom.server.instance.block.states.QuartzSlab;
import net.minestom.server.instance.block.states.QuartzStairs;
import net.minestom.server.instance.block.states.Rail;
import net.minestom.server.instance.block.states.RedBanner;
import net.minestom.server.instance.block.states.RedBed;
import net.minestom.server.instance.block.states.RedGlazedTerracotta;
import net.minestom.server.instance.block.states.RedMushroomBlock;
import net.minestom.server.instance.block.states.RedNetherBrickSlab;
import net.minestom.server.instance.block.states.RedNetherBrickStairs;
import net.minestom.server.instance.block.states.RedNetherBrickWall;
import net.minestom.server.instance.block.states.RedSandstoneSlab;
import net.minestom.server.instance.block.states.RedSandstoneStairs;
import net.minestom.server.instance.block.states.RedSandstoneWall;
import net.minestom.server.instance.block.states.RedShulkerBox;
import net.minestom.server.instance.block.states.RedStainedGlassPane;
import net.minestom.server.instance.block.states.RedWallBanner;
import net.minestom.server.instance.block.states.RedstoneLamp;
import net.minestom.server.instance.block.states.RedstoneOre;
import net.minestom.server.instance.block.states.RedstoneTorch;
import net.minestom.server.instance.block.states.RedstoneWallTorch;
import net.minestom.server.instance.block.states.RedstoneWire;
import net.minestom.server.instance.block.states.Repeater;
import net.minestom.server.instance.block.states.RepeatingCommandBlock;
import net.minestom.server.instance.block.states.RespawnAnchor;
import net.minestom.server.instance.block.states.RoseBush;
import net.minestom.server.instance.block.states.SandstoneSlab;
import net.minestom.server.instance.block.states.SandstoneStairs;
import net.minestom.server.instance.block.states.SandstoneWall;
import net.minestom.server.instance.block.states.Scaffolding;
import net.minestom.server.instance.block.states.SeaPickle;
import net.minestom.server.instance.block.states.ShulkerBox;
import net.minestom.server.instance.block.states.SkeletonSkull;
import net.minestom.server.instance.block.states.SkeletonWallSkull;
import net.minestom.server.instance.block.states.Smoker;
import net.minestom.server.instance.block.states.SmoothQuartzSlab;
import net.minestom.server.instance.block.states.SmoothQuartzStairs;
import net.minestom.server.instance.block.states.SmoothRedSandstoneSlab;
import net.minestom.server.instance.block.states.SmoothRedSandstoneStairs;
import net.minestom.server.instance.block.states.SmoothSandstoneSlab;
import net.minestom.server.instance.block.states.SmoothSandstoneStairs;
import net.minestom.server.instance.block.states.SmoothStoneSlab;
import net.minestom.server.instance.block.states.Snow;
import net.minestom.server.instance.block.states.SoulCampfire;
import net.minestom.server.instance.block.states.SoulLantern;
import net.minestom.server.instance.block.states.SoulWallTorch;
import net.minestom.server.instance.block.states.SpruceButton;
import net.minestom.server.instance.block.states.SpruceDoor;
import net.minestom.server.instance.block.states.SpruceFence;
import net.minestom.server.instance.block.states.SpruceFenceGate;
import net.minestom.server.instance.block.states.SpruceLeaves;
import net.minestom.server.instance.block.states.SpruceLog;
import net.minestom.server.instance.block.states.SprucePressurePlate;
import net.minestom.server.instance.block.states.SpruceSapling;
import net.minestom.server.instance.block.states.SpruceSign;
import net.minestom.server.instance.block.states.SpruceSlab;
import net.minestom.server.instance.block.states.SpruceStairs;
import net.minestom.server.instance.block.states.SpruceTrapdoor;
import net.minestom.server.instance.block.states.SpruceWallSign;
import net.minestom.server.instance.block.states.SpruceWood;
import net.minestom.server.instance.block.states.StickyPiston;
import net.minestom.server.instance.block.states.StoneBrickSlab;
import net.minestom.server.instance.block.states.StoneBrickStairs;
import net.minestom.server.instance.block.states.StoneBrickWall;
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
import net.minestom.server.instance.block.states.SugarCane;
import net.minestom.server.instance.block.states.Sunflower;
import net.minestom.server.instance.block.states.SweetBerryBush;
import net.minestom.server.instance.block.states.TallGrass;
import net.minestom.server.instance.block.states.TallSeagrass;
import net.minestom.server.instance.block.states.Target;
import net.minestom.server.instance.block.states.Tnt;
import net.minestom.server.instance.block.states.TrappedChest;
import net.minestom.server.instance.block.states.Tripwire;
import net.minestom.server.instance.block.states.TripwireHook;
import net.minestom.server.instance.block.states.TubeCoral;
import net.minestom.server.instance.block.states.TubeCoralFan;
import net.minestom.server.instance.block.states.TubeCoralWallFan;
import net.minestom.server.instance.block.states.TurtleEgg;
import net.minestom.server.instance.block.states.TwistingVines;
import net.minestom.server.instance.block.states.Vine;
import net.minestom.server.instance.block.states.WallTorch;
import net.minestom.server.instance.block.states.WarpedButton;
import net.minestom.server.instance.block.states.WarpedDoor;
import net.minestom.server.instance.block.states.WarpedFence;
import net.minestom.server.instance.block.states.WarpedFenceGate;
import net.minestom.server.instance.block.states.WarpedHyphae;
import net.minestom.server.instance.block.states.WarpedPressurePlate;
import net.minestom.server.instance.block.states.WarpedSign;
import net.minestom.server.instance.block.states.WarpedSlab;
import net.minestom.server.instance.block.states.WarpedStairs;
import net.minestom.server.instance.block.states.WarpedStem;
import net.minestom.server.instance.block.states.WarpedTrapdoor;
import net.minestom.server.instance.block.states.WarpedWallSign;
import net.minestom.server.instance.block.states.Water;
import net.minestom.server.instance.block.states.WeepingVines;
import net.minestom.server.instance.block.states.Wheat;
import net.minestom.server.instance.block.states.WhiteBanner;
import net.minestom.server.instance.block.states.WhiteBed;
import net.minestom.server.instance.block.states.WhiteGlazedTerracotta;
import net.minestom.server.instance.block.states.WhiteShulkerBox;
import net.minestom.server.instance.block.states.WhiteStainedGlassPane;
import net.minestom.server.instance.block.states.WhiteWallBanner;
import net.minestom.server.instance.block.states.WitherSkeletonSkull;
import net.minestom.server.instance.block.states.WitherSkeletonWallSkull;
import net.minestom.server.instance.block.states.YellowBanner;
import net.minestom.server.instance.block.states.YellowBed;
import net.minestom.server.instance.block.states.YellowGlazedTerracotta;
import net.minestom.server.instance.block.states.YellowShulkerBox;
import net.minestom.server.instance.block.states.YellowStainedGlassPane;
import net.minestom.server.instance.block.states.YellowWallBanner;
import net.minestom.server.instance.block.states.ZombieHead;
import net.minestom.server.instance.block.states.ZombieWallHead;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * //==============================
 * //  AUTOGENERATED BY EnumGenerator
 * //==============================
 */
@SuppressWarnings({"deprecation"})
public enum Block implements Keyed {
    AIR("minecraft:air", (short) 0, 0.0, 0.0, true, false, null, true),

    STONE("minecraft:stone", (short) 1, 1.5, 6.0, false, true, null, true),

    GRANITE("minecraft:granite", (short) 2, 1.5, 6.0, false, true, null, true),

    POLISHED_GRANITE("minecraft:polished_granite", (short) 3, 1.5, 6.0, false, true, null, true),

    DIORITE("minecraft:diorite", (short) 4, 1.5, 6.0, false, true, null, true),

    POLISHED_DIORITE("minecraft:polished_diorite", (short) 5, 1.5, 6.0, false, true, null, true),

    ANDESITE("minecraft:andesite", (short) 6, 1.5, 6.0, false, true, null, true),

    POLISHED_ANDESITE("minecraft:polished_andesite", (short) 7, 1.5, 6.0, false, true, null, true),

    GRASS_BLOCK("minecraft:grass_block", (short) 9, 0.6, 0.6, false, true, null, false),

    DIRT("minecraft:dirt", (short) 10, 0.5, 0.5, false, true, null, true),

    COARSE_DIRT("minecraft:coarse_dirt", (short) 11, 0.5, 0.5, false, true, null, true),

    PODZOL("minecraft:podzol", (short) 13, 0.5, 0.5, false, true, null, false),

    COBBLESTONE("minecraft:cobblestone", (short) 14, 2.0, 6.0, false, true, null, true),

    OAK_PLANKS("minecraft:oak_planks", (short) 15, 2.0, 3.0, false, true, null, true),

    SPRUCE_PLANKS("minecraft:spruce_planks", (short) 16, 2.0, 3.0, false, true, null, true),

    BIRCH_PLANKS("minecraft:birch_planks", (short) 17, 2.0, 3.0, false, true, null, true),

    JUNGLE_PLANKS("minecraft:jungle_planks", (short) 18, 2.0, 3.0, false, true, null, true),

    ACACIA_PLANKS("minecraft:acacia_planks", (short) 19, 2.0, 3.0, false, true, null, true),

    DARK_OAK_PLANKS("minecraft:dark_oak_planks", (short) 20, 2.0, 3.0, false, true, null, true),

    OAK_SAPLING("minecraft:oak_sapling", (short) 21, 0.0, 0.0, false, false, null, false),

    SPRUCE_SAPLING("minecraft:spruce_sapling", (short) 23, 0.0, 0.0, false, false, null, false),

    BIRCH_SAPLING("minecraft:birch_sapling", (short) 25, 0.0, 0.0, false, false, null, false),

    JUNGLE_SAPLING("minecraft:jungle_sapling", (short) 27, 0.0, 0.0, false, false, null, false),

    ACACIA_SAPLING("minecraft:acacia_sapling", (short) 29, 0.0, 0.0, false, false, null, false),

    DARK_OAK_SAPLING("minecraft:dark_oak_sapling", (short) 31, 0.0, 0.0, false, false, null, false),

    BEDROCK("minecraft:bedrock", (short) 33, 0.0, 3600000.0, false, true, null, true),

    WATER("minecraft:water", (short) 34, 100.0, 100.0, false, false, null, false),

    LAVA("minecraft:lava", (short) 50, 100.0, 100.0, false, false, null, false),

    SAND("minecraft:sand", (short) 66, 0.5, 0.5, false, true, null, true),

    RED_SAND("minecraft:red_sand", (short) 67, 0.5, 0.5, false, true, null, true),

    GRAVEL("minecraft:gravel", (short) 68, 0.6, 0.6, false, true, null, true),

    GOLD_ORE("minecraft:gold_ore", (short) 69, 3.0, 3.0, false, true, null, true),

    IRON_ORE("minecraft:iron_ore", (short) 70, 3.0, 3.0, false, true, null, true),

    COAL_ORE("minecraft:coal_ore", (short) 71, 3.0, 3.0, false, true, null, true),

    NETHER_GOLD_ORE("minecraft:nether_gold_ore", (short) 72, 3.0, 3.0, false, true, null, true),

    OAK_LOG("minecraft:oak_log", (short) 74, 2.0, 2.0, false, true, null, false),

    SPRUCE_LOG("minecraft:spruce_log", (short) 77, 2.0, 2.0, false, true, null, false),

    BIRCH_LOG("minecraft:birch_log", (short) 80, 2.0, 2.0, false, true, null, false),

    JUNGLE_LOG("minecraft:jungle_log", (short) 83, 2.0, 2.0, false, true, null, false),

    ACACIA_LOG("minecraft:acacia_log", (short) 86, 2.0, 2.0, false, true, null, false),

    DARK_OAK_LOG("minecraft:dark_oak_log", (short) 89, 2.0, 2.0, false, true, null, false),

    STRIPPED_SPRUCE_LOG("minecraft:stripped_spruce_log", (short) 92, 2.0, 2.0, false, true, null, false),

    STRIPPED_BIRCH_LOG("minecraft:stripped_birch_log", (short) 95, 2.0, 2.0, false, true, null, false),

    STRIPPED_JUNGLE_LOG("minecraft:stripped_jungle_log", (short) 98, 2.0, 2.0, false, true, null, false),

    STRIPPED_ACACIA_LOG("minecraft:stripped_acacia_log", (short) 101, 2.0, 2.0, false, true, null, false),

    STRIPPED_DARK_OAK_LOG("minecraft:stripped_dark_oak_log", (short) 104, 2.0, 2.0, false, true, null, false),

    STRIPPED_OAK_LOG("minecraft:stripped_oak_log", (short) 107, 2.0, 2.0, false, true, null, false),

    OAK_WOOD("minecraft:oak_wood", (short) 110, 2.0, 2.0, false, true, null, false),

    SPRUCE_WOOD("minecraft:spruce_wood", (short) 113, 2.0, 2.0, false, true, null, false),

    BIRCH_WOOD("minecraft:birch_wood", (short) 116, 2.0, 2.0, false, true, null, false),

    JUNGLE_WOOD("minecraft:jungle_wood", (short) 119, 2.0, 2.0, false, true, null, false),

    ACACIA_WOOD("minecraft:acacia_wood", (short) 122, 2.0, 2.0, false, true, null, false),

    DARK_OAK_WOOD("minecraft:dark_oak_wood", (short) 125, 2.0, 2.0, false, true, null, false),

    STRIPPED_OAK_WOOD("minecraft:stripped_oak_wood", (short) 128, 2.0, 2.0, false, true, null, false),

    STRIPPED_SPRUCE_WOOD("minecraft:stripped_spruce_wood", (short) 131, 2.0, 2.0, false, true, null, false),

    STRIPPED_BIRCH_WOOD("minecraft:stripped_birch_wood", (short) 134, 2.0, 2.0, false, true, null, false),

    STRIPPED_JUNGLE_WOOD("minecraft:stripped_jungle_wood", (short) 137, 2.0, 2.0, false, true, null, false),

    STRIPPED_ACACIA_WOOD("minecraft:stripped_acacia_wood", (short) 140, 2.0, 2.0, false, true, null, false),

    STRIPPED_DARK_OAK_WOOD("minecraft:stripped_dark_oak_wood", (short) 143, 2.0, 2.0, false, true, null, false),

    OAK_LEAVES("minecraft:oak_leaves", (short) 158, 0.2, 0.2, false, true, null, false),

    SPRUCE_LEAVES("minecraft:spruce_leaves", (short) 172, 0.2, 0.2, false, true, null, false),

    BIRCH_LEAVES("minecraft:birch_leaves", (short) 186, 0.2, 0.2, false, true, null, false),

    JUNGLE_LEAVES("minecraft:jungle_leaves", (short) 200, 0.2, 0.2, false, true, null, false),

    ACACIA_LEAVES("minecraft:acacia_leaves", (short) 214, 0.2, 0.2, false, true, null, false),

    DARK_OAK_LEAVES("minecraft:dark_oak_leaves", (short) 228, 0.2, 0.2, false, true, null, false),

    SPONGE("minecraft:sponge", (short) 229, 0.6, 0.6, false, true, null, true),

    WET_SPONGE("minecraft:wet_sponge", (short) 230, 0.6, 0.6, false, true, null, true),

    GLASS("minecraft:glass", (short) 231, 0.3, 0.3, false, true, null, true),

    LAPIS_ORE("minecraft:lapis_ore", (short) 232, 3.0, 3.0, false, true, null, true),

    LAPIS_BLOCK("minecraft:lapis_block", (short) 233, 3.0, 3.0, false, true, null, true),

    DISPENSER("minecraft:dispenser", (short) 235, 3.5, 3.5, false, true, NamespaceID.from("minecraft:dispenser"), false),

    SANDSTONE("minecraft:sandstone", (short) 246, 0.8, 0.8, false, true, null, true),

    CHISELED_SANDSTONE("minecraft:chiseled_sandstone", (short) 247, 0.8, 0.8, false, true, null, true),

    CUT_SANDSTONE("minecraft:cut_sandstone", (short) 248, 0.8, 0.8, false, true, null, true),

    NOTE_BLOCK("minecraft:note_block", (short) 250, 0.8, 0.8, false, true, null, false),

    WHITE_BED("minecraft:white_bed", (short) 1052, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    ORANGE_BED("minecraft:orange_bed", (short) 1068, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    MAGENTA_BED("minecraft:magenta_bed", (short) 1084, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    LIGHT_BLUE_BED("minecraft:light_blue_bed", (short) 1100, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    YELLOW_BED("minecraft:yellow_bed", (short) 1116, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    LIME_BED("minecraft:lime_bed", (short) 1132, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    PINK_BED("minecraft:pink_bed", (short) 1148, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    GRAY_BED("minecraft:gray_bed", (short) 1164, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    LIGHT_GRAY_BED("minecraft:light_gray_bed", (short) 1180, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    CYAN_BED("minecraft:cyan_bed", (short) 1196, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    PURPLE_BED("minecraft:purple_bed", (short) 1212, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    BLUE_BED("minecraft:blue_bed", (short) 1228, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    BROWN_BED("minecraft:brown_bed", (short) 1244, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    GREEN_BED("minecraft:green_bed", (short) 1260, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    RED_BED("minecraft:red_bed", (short) 1276, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    BLACK_BED("minecraft:black_bed", (short) 1292, 0.2, 0.2, false, true, NamespaceID.from("minecraft:bed"), false),

    POWERED_RAIL("minecraft:powered_rail", (short) 1311, 0.7, 0.7, false, false, null, false),

    DETECTOR_RAIL("minecraft:detector_rail", (short) 1323, 0.7, 0.7, false, false, null, false),

    STICKY_PISTON("minecraft:sticky_piston", (short) 1335, 1.5, 1.5, false, true, null, false),

    COBWEB("minecraft:cobweb", (short) 1341, 4.0, 4.0, false, false, null, true),

    GRASS("minecraft:grass", (short) 1342, 0.0, 0.0, false, false, null, true),

    FERN("minecraft:fern", (short) 1343, 0.0, 0.0, false, false, null, true),

    DEAD_BUSH("minecraft:dead_bush", (short) 1344, 0.0, 0.0, false, false, null, true),

    SEAGRASS("minecraft:seagrass", (short) 1345, 0.0, 0.0, false, false, null, true),

    TALL_SEAGRASS("minecraft:tall_seagrass", (short) 1347, 0.0, 0.0, false, false, null, false),

    PISTON("minecraft:piston", (short) 1354, 1.5, 1.5, false, true, null, false),

    PISTON_HEAD("minecraft:piston_head", (short) 1362, 1.5, 1.5, false, true, null, false),

    WHITE_WOOL("minecraft:white_wool", (short) 1384, 0.8, 0.8, false, true, null, true),

    ORANGE_WOOL("minecraft:orange_wool", (short) 1385, 0.8, 0.8, false, true, null, true),

    MAGENTA_WOOL("minecraft:magenta_wool", (short) 1386, 0.8, 0.8, false, true, null, true),

    LIGHT_BLUE_WOOL("minecraft:light_blue_wool", (short) 1387, 0.8, 0.8, false, true, null, true),

    YELLOW_WOOL("minecraft:yellow_wool", (short) 1388, 0.8, 0.8, false, true, null, true),

    LIME_WOOL("minecraft:lime_wool", (short) 1389, 0.8, 0.8, false, true, null, true),

    PINK_WOOL("minecraft:pink_wool", (short) 1390, 0.8, 0.8, false, true, null, true),

    GRAY_WOOL("minecraft:gray_wool", (short) 1391, 0.8, 0.8, false, true, null, true),

    LIGHT_GRAY_WOOL("minecraft:light_gray_wool", (short) 1392, 0.8, 0.8, false, true, null, true),

    CYAN_WOOL("minecraft:cyan_wool", (short) 1393, 0.8, 0.8, false, true, null, true),

    PURPLE_WOOL("minecraft:purple_wool", (short) 1394, 0.8, 0.8, false, true, null, true),

    BLUE_WOOL("minecraft:blue_wool", (short) 1395, 0.8, 0.8, false, true, null, true),

    BROWN_WOOL("minecraft:brown_wool", (short) 1396, 0.8, 0.8, false, true, null, true),

    GREEN_WOOL("minecraft:green_wool", (short) 1397, 0.8, 0.8, false, true, null, true),

    RED_WOOL("minecraft:red_wool", (short) 1398, 0.8, 0.8, false, true, null, true),

    BLACK_WOOL("minecraft:black_wool", (short) 1399, 0.8, 0.8, false, true, null, true),

    MOVING_PISTON("minecraft:moving_piston", (short) 1400, 0.0, -1.0, false, false, null, false),

    DANDELION("minecraft:dandelion", (short) 1412, 0.0, 0.0, false, false, null, true),

    POPPY("minecraft:poppy", (short) 1413, 0.0, 0.0, false, false, null, true),

    BLUE_ORCHID("minecraft:blue_orchid", (short) 1414, 0.0, 0.0, false, false, null, true),

    ALLIUM("minecraft:allium", (short) 1415, 0.0, 0.0, false, false, null, true),

    AZURE_BLUET("minecraft:azure_bluet", (short) 1416, 0.0, 0.0, false, false, null, true),

    RED_TULIP("minecraft:red_tulip", (short) 1417, 0.0, 0.0, false, false, null, true),

    ORANGE_TULIP("minecraft:orange_tulip", (short) 1418, 0.0, 0.0, false, false, null, true),

    WHITE_TULIP("minecraft:white_tulip", (short) 1419, 0.0, 0.0, false, false, null, true),

    PINK_TULIP("minecraft:pink_tulip", (short) 1420, 0.0, 0.0, false, false, null, true),

    OXEYE_DAISY("minecraft:oxeye_daisy", (short) 1421, 0.0, 0.0, false, false, null, true),

    CORNFLOWER("minecraft:cornflower", (short) 1422, 0.0, 0.0, false, false, null, true),

    WITHER_ROSE("minecraft:wither_rose", (short) 1423, 0.0, 0.0, false, false, null, true),

    LILY_OF_THE_VALLEY("minecraft:lily_of_the_valley", (short) 1424, 0.0, 0.0, false, false, null, true),

    BROWN_MUSHROOM("minecraft:brown_mushroom", (short) 1425, 0.0, 0.0, false, false, null, true),

    RED_MUSHROOM("minecraft:red_mushroom", (short) 1426, 0.0, 0.0, false, false, null, true),

    GOLD_BLOCK("minecraft:gold_block", (short) 1427, 3.0, 6.0, false, true, null, true),

    IRON_BLOCK("minecraft:iron_block", (short) 1428, 5.0, 6.0, false, true, null, true),

    BRICKS("minecraft:bricks", (short) 1429, 2.0, 6.0, false, true, null, true),

    TNT("minecraft:tnt", (short) 1431, 0.0, 0.0, false, true, null, false),

    BOOKSHELF("minecraft:bookshelf", (short) 1432, 1.5, 1.5, false, true, null, true),

    MOSSY_COBBLESTONE("minecraft:mossy_cobblestone", (short) 1433, 2.0, 6.0, false, true, null, true),

    OBSIDIAN("minecraft:obsidian", (short) 1434, 50.0, 1200.0, false, true, null, true),

    TORCH("minecraft:torch", (short) 1435, 0.0, 0.0, false, false, null, true),

    WALL_TORCH("minecraft:wall_torch", (short) 1436, 0.0, 0.0, false, false, null, false),

    FIRE("minecraft:fire", (short) 1471, 0.0, 0.0, false, false, null, false),

    SOUL_FIRE("minecraft:soul_fire", (short) 1952, 0.0, 0.0, false, false, null, true),

    SPAWNER("minecraft:spawner", (short) 1953, 5.0, 5.0, false, true, NamespaceID.from("minecraft:mob_spawner"), true),

    OAK_STAIRS("minecraft:oak_stairs", (short) 1965, 0.0, 0.0, false, true, null, false),

    CHEST("minecraft:chest", (short) 2035, 2.5, 2.5, false, true, NamespaceID.from("minecraft:chest"), false),

    REDSTONE_WIRE("minecraft:redstone_wire", (short) 3218, 0.0, 0.0, false, false, null, false),

    DIAMOND_ORE("minecraft:diamond_ore", (short) 3354, 3.0, 3.0, false, true, null, true),

    DIAMOND_BLOCK("minecraft:diamond_block", (short) 3355, 5.0, 6.0, false, true, null, true),

    CRAFTING_TABLE("minecraft:crafting_table", (short) 3356, 2.5, 2.5, false, true, null, true),

    WHEAT("minecraft:wheat", (short) 3357, 0.0, 0.0, false, false, null, false),

    FARMLAND("minecraft:farmland", (short) 3365, 0.6, 0.6, false, true, null, false),

    FURNACE("minecraft:furnace", (short) 3374, 3.5, 3.5, false, true, NamespaceID.from("minecraft:furnace"), false),

    OAK_SIGN("minecraft:oak_sign", (short) 3382, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    SPRUCE_SIGN("minecraft:spruce_sign", (short) 3414, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    BIRCH_SIGN("minecraft:birch_sign", (short) 3446, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    ACACIA_SIGN("minecraft:acacia_sign", (short) 3478, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    JUNGLE_SIGN("minecraft:jungle_sign", (short) 3510, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    DARK_OAK_SIGN("minecraft:dark_oak_sign", (short) 3542, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    OAK_DOOR("minecraft:oak_door", (short) 3584, 3.0, 3.0, false, true, null, false),

    LADDER("minecraft:ladder", (short) 3638, 0.4, 0.4, false, true, null, false),

    RAIL("minecraft:rail", (short) 3645, 0.7, 0.7, false, false, null, false),

    COBBLESTONE_STAIRS("minecraft:cobblestone_stairs", (short) 3666, 0.0, 0.0, false, true, null, false),

    OAK_WALL_SIGN("minecraft:oak_wall_sign", (short) 3736, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    SPRUCE_WALL_SIGN("minecraft:spruce_wall_sign", (short) 3744, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    BIRCH_WALL_SIGN("minecraft:birch_wall_sign", (short) 3752, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    ACACIA_WALL_SIGN("minecraft:acacia_wall_sign", (short) 3760, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    JUNGLE_WALL_SIGN("minecraft:jungle_wall_sign", (short) 3768, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    DARK_OAK_WALL_SIGN("minecraft:dark_oak_wall_sign", (short) 3776, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    LEVER("minecraft:lever", (short) 3792, 0.5, 0.5, false, false, null, false),

    STONE_PRESSURE_PLATE("minecraft:stone_pressure_plate", (short) 3808, 0.5, 0.5, false, false, null, false),

    IRON_DOOR("minecraft:iron_door", (short) 3820, 5.0, 5.0, false, true, null, false),

    OAK_PRESSURE_PLATE("minecraft:oak_pressure_plate", (short) 3874, 0.5, 0.5, false, false, null, false),

    SPRUCE_PRESSURE_PLATE("minecraft:spruce_pressure_plate", (short) 3876, 0.5, 0.5, false, false, null, false),

    BIRCH_PRESSURE_PLATE("minecraft:birch_pressure_plate", (short) 3878, 0.5, 0.5, false, false, null, false),

    JUNGLE_PRESSURE_PLATE("minecraft:jungle_pressure_plate", (short) 3880, 0.5, 0.5, false, false, null, false),

    ACACIA_PRESSURE_PLATE("minecraft:acacia_pressure_plate", (short) 3882, 0.5, 0.5, false, false, null, false),

    DARK_OAK_PRESSURE_PLATE("minecraft:dark_oak_pressure_plate", (short) 3884, 0.5, 0.5, false, false, null, false),

    REDSTONE_ORE("minecraft:redstone_ore", (short) 3886, 3.0, 3.0, false, true, null, false),

    REDSTONE_TORCH("minecraft:redstone_torch", (short) 3887, 0.0, 0.0, false, false, null, false),

    REDSTONE_WALL_TORCH("minecraft:redstone_wall_torch", (short) 3889, 0.0, 0.0, false, false, null, false),

    STONE_BUTTON("minecraft:stone_button", (short) 3906, 0.5, 0.5, false, false, null, false),

    SNOW("minecraft:snow", (short) 3921, 0.1, 0.1, false, true, null, false),

    ICE("minecraft:ice", (short) 3929, 0.5, 0.5, false, true, null, true),

    SNOW_BLOCK("minecraft:snow_block", (short) 3930, 0.2, 0.2, false, true, null, true),

    CACTUS("minecraft:cactus", (short) 3931, 0.4, 0.4, false, true, null, false),

    CLAY("minecraft:clay", (short) 3947, 0.6, 0.6, false, true, null, true),

    SUGAR_CANE("minecraft:sugar_cane", (short) 3948, 0.0, 0.0, false, false, null, false),

    JUKEBOX("minecraft:jukebox", (short) 3965, 2.0, 6.0, false, true, NamespaceID.from("minecraft:jukebox"), false),

    OAK_FENCE("minecraft:oak_fence", (short) 3997, 2.0, 3.0, false, true, null, false),

    PUMPKIN("minecraft:pumpkin", (short) 3998, 1.0, 1.0, false, true, null, true),

    NETHERRACK("minecraft:netherrack", (short) 3999, 0.4, 0.4, false, true, null, true),

    SOUL_SAND("minecraft:soul_sand", (short) 4000, 0.5, 0.5, false, true, null, true),

    SOUL_SOIL("minecraft:soul_soil", (short) 4001, 0.5, 0.5, false, true, null, true),

    BASALT("minecraft:basalt", (short) 4003, 1.25, 4.2, false, true, null, false),

    POLISHED_BASALT("minecraft:polished_basalt", (short) 4006, 1.25, 4.2, false, true, null, false),

    SOUL_TORCH("minecraft:soul_torch", (short) 4008, 0.0, 0.0, false, false, null, true),

    SOUL_WALL_TORCH("minecraft:soul_wall_torch", (short) 4009, 0.0, 0.0, false, false, null, false),

    GLOWSTONE("minecraft:glowstone", (short) 4013, 0.3, 0.3, false, true, null, true),

    NETHER_PORTAL("minecraft:nether_portal", (short) 4014, 0.0, -1.0, false, false, null, false),

    CARVED_PUMPKIN("minecraft:carved_pumpkin", (short) 4016, 1.0, 1.0, false, true, null, false),

    JACK_O_LANTERN("minecraft:jack_o_lantern", (short) 4020, 1.0, 1.0, false, true, null, false),

    CAKE("minecraft:cake", (short) 4024, 0.5, 0.5, false, true, null, false),

    REPEATER("minecraft:repeater", (short) 4034, 0.0, 0.0, false, true, null, false),

    WHITE_STAINED_GLASS("minecraft:white_stained_glass", (short) 4095, 0.3, 0.3, false, true, null, true),

    ORANGE_STAINED_GLASS("minecraft:orange_stained_glass", (short) 4096, 0.3, 0.3, false, true, null, true),

    MAGENTA_STAINED_GLASS("minecraft:magenta_stained_glass", (short) 4097, 0.3, 0.3, false, true, null, true),

    LIGHT_BLUE_STAINED_GLASS("minecraft:light_blue_stained_glass", (short) 4098, 0.3, 0.3, false, true, null, true),

    YELLOW_STAINED_GLASS("minecraft:yellow_stained_glass", (short) 4099, 0.3, 0.3, false, true, null, true),

    LIME_STAINED_GLASS("minecraft:lime_stained_glass", (short) 4100, 0.3, 0.3, false, true, null, true),

    PINK_STAINED_GLASS("minecraft:pink_stained_glass", (short) 4101, 0.3, 0.3, false, true, null, true),

    GRAY_STAINED_GLASS("minecraft:gray_stained_glass", (short) 4102, 0.3, 0.3, false, true, null, true),

    LIGHT_GRAY_STAINED_GLASS("minecraft:light_gray_stained_glass", (short) 4103, 0.3, 0.3, false, true, null, true),

    CYAN_STAINED_GLASS("minecraft:cyan_stained_glass", (short) 4104, 0.3, 0.3, false, true, null, true),

    PURPLE_STAINED_GLASS("minecraft:purple_stained_glass", (short) 4105, 0.3, 0.3, false, true, null, true),

    BLUE_STAINED_GLASS("minecraft:blue_stained_glass", (short) 4106, 0.3, 0.3, false, true, null, true),

    BROWN_STAINED_GLASS("minecraft:brown_stained_glass", (short) 4107, 0.3, 0.3, false, true, null, true),

    GREEN_STAINED_GLASS("minecraft:green_stained_glass", (short) 4108, 0.3, 0.3, false, true, null, true),

    RED_STAINED_GLASS("minecraft:red_stained_glass", (short) 4109, 0.3, 0.3, false, true, null, true),

    BLACK_STAINED_GLASS("minecraft:black_stained_glass", (short) 4110, 0.3, 0.3, false, true, null, true),

    OAK_TRAPDOOR("minecraft:oak_trapdoor", (short) 4126, 3.0, 3.0, false, true, null, false),

    SPRUCE_TRAPDOOR("minecraft:spruce_trapdoor", (short) 4190, 3.0, 3.0, false, true, null, false),

    BIRCH_TRAPDOOR("minecraft:birch_trapdoor", (short) 4254, 3.0, 3.0, false, true, null, false),

    JUNGLE_TRAPDOOR("minecraft:jungle_trapdoor", (short) 4318, 3.0, 3.0, false, true, null, false),

    ACACIA_TRAPDOOR("minecraft:acacia_trapdoor", (short) 4382, 3.0, 3.0, false, true, null, false),

    DARK_OAK_TRAPDOOR("minecraft:dark_oak_trapdoor", (short) 4446, 3.0, 3.0, false, true, null, false),

    STONE_BRICKS("minecraft:stone_bricks", (short) 4495, 1.5, 6.0, false, true, null, true),

    MOSSY_STONE_BRICKS("minecraft:mossy_stone_bricks", (short) 4496, 1.5, 6.0, false, true, null, true),

    CRACKED_STONE_BRICKS("minecraft:cracked_stone_bricks", (short) 4497, 1.5, 6.0, false, true, null, true),

    CHISELED_STONE_BRICKS("minecraft:chiseled_stone_bricks", (short) 4498, 1.5, 6.0, false, true, null, true),

    INFESTED_STONE("minecraft:infested_stone", (short) 4499, 0.0, 0.75, false, true, null, true),

    INFESTED_COBBLESTONE("minecraft:infested_cobblestone", (short) 4500, 0.0, 0.75, false, true, null, true),

    INFESTED_STONE_BRICKS("minecraft:infested_stone_bricks", (short) 4501, 0.0, 0.75, false, true, null, true),

    INFESTED_MOSSY_STONE_BRICKS("minecraft:infested_mossy_stone_bricks", (short) 4502, 0.0, 0.75, false, true, null, true),

    INFESTED_CRACKED_STONE_BRICKS("minecraft:infested_cracked_stone_bricks", (short) 4503, 0.0, 0.75, false, true, null, true),

    INFESTED_CHISELED_STONE_BRICKS("minecraft:infested_chiseled_stone_bricks", (short) 4504, 0.0, 0.75, false, true, null, true),

    BROWN_MUSHROOM_BLOCK("minecraft:brown_mushroom_block", (short) 4505, 0.2, 0.2, false, true, null, false),

    RED_MUSHROOM_BLOCK("minecraft:red_mushroom_block", (short) 4569, 0.2, 0.2, false, true, null, false),

    MUSHROOM_STEM("minecraft:mushroom_stem", (short) 4633, 0.2, 0.2, false, true, null, false),

    IRON_BARS("minecraft:iron_bars", (short) 4728, 5.0, 6.0, false, true, null, false),

    CHAIN("minecraft:chain", (short) 4732, 5.0, 6.0, false, true, null, false),

    GLASS_PANE("minecraft:glass_pane", (short) 4766, 0.3, 0.3, false, true, null, false),

    MELON("minecraft:melon", (short) 4767, 1.0, 1.0, false, true, null, true),

    ATTACHED_PUMPKIN_STEM("minecraft:attached_pumpkin_stem", (short) 4768, 0.0, 0.0, false, false, null, false),

    ATTACHED_MELON_STEM("minecraft:attached_melon_stem", (short) 4772, 0.0, 0.0, false, false, null, false),

    PUMPKIN_STEM("minecraft:pumpkin_stem", (short) 4776, 0.0, 0.0, false, false, null, false),

    MELON_STEM("minecraft:melon_stem", (short) 4784, 0.0, 0.0, false, false, null, false),

    VINE("minecraft:vine", (short) 4823, 0.2, 0.2, false, false, null, false),

    OAK_FENCE_GATE("minecraft:oak_fence_gate", (short) 4831, 2.0, 3.0, false, true, null, false),

    BRICK_STAIRS("minecraft:brick_stairs", (short) 4867, 0.0, 0.0, false, true, null, false),

    STONE_BRICK_STAIRS("minecraft:stone_brick_stairs", (short) 4947, 0.0, 0.0, false, true, null, false),

    MYCELIUM("minecraft:mycelium", (short) 5017, 0.6, 0.6, false, true, null, false),

    LILY_PAD("minecraft:lily_pad", (short) 5018, 0.0, 0.0, false, true, null, true),

    NETHER_BRICKS("minecraft:nether_bricks", (short) 5019, 2.0, 6.0, false, true, null, true),

    NETHER_BRICK_FENCE("minecraft:nether_brick_fence", (short) 5051, 2.0, 6.0, false, true, null, false),

    NETHER_BRICK_STAIRS("minecraft:nether_brick_stairs", (short) 5063, 0.0, 0.0, false, true, null, false),

    NETHER_WART("minecraft:nether_wart", (short) 5132, 0.0, 0.0, false, false, null, false),

    ENCHANTING_TABLE("minecraft:enchanting_table", (short) 5136, 5.0, 1200.0, false, true, NamespaceID.from("minecraft:enchanting_table"), true),

    BREWING_STAND("minecraft:brewing_stand", (short) 5144, 0.5, 0.5, false, true, NamespaceID.from("minecraft:brewing_stand"), false),

    CAULDRON("minecraft:cauldron", (short) 5145, 2.0, 2.0, false, true, null, false),

    END_PORTAL("minecraft:end_portal", (short) 5149, 0.0, 3600000.0, false, false, NamespaceID.from("minecraft:end_portal"), true),

    END_PORTAL_FRAME("minecraft:end_portal_frame", (short) 5154, 0.0, 3600000.0, false, true, null, false),

    END_STONE("minecraft:end_stone", (short) 5158, 3.0, 9.0, false, true, null, true),

    DRAGON_EGG("minecraft:dragon_egg", (short) 5159, 3.0, 9.0, false, true, null, true),

    REDSTONE_LAMP("minecraft:redstone_lamp", (short) 5161, 0.3, 0.3, false, true, null, false),

    COCOA("minecraft:cocoa", (short) 5162, 0.2, 3.0, false, true, null, false),

    SANDSTONE_STAIRS("minecraft:sandstone_stairs", (short) 5185, 0.0, 0.0, false, true, null, false),

    EMERALD_ORE("minecraft:emerald_ore", (short) 5254, 3.0, 3.0, false, true, null, true),

    ENDER_CHEST("minecraft:ender_chest", (short) 5256, 22.5, 600.0, false, true, NamespaceID.from("minecraft:ender_chest"), false),

    TRIPWIRE_HOOK("minecraft:tripwire_hook", (short) 5272, 0.0, 0.0, false, false, null, false),

    TRIPWIRE("minecraft:tripwire", (short) 5406, 0.0, 0.0, false, false, null, false),

    EMERALD_BLOCK("minecraft:emerald_block", (short) 5407, 5.0, 6.0, false, true, null, true),

    SPRUCE_STAIRS("minecraft:spruce_stairs", (short) 5419, 0.0, 0.0, false, true, null, false),

    BIRCH_STAIRS("minecraft:birch_stairs", (short) 5499, 0.0, 0.0, false, true, null, false),

    JUNGLE_STAIRS("minecraft:jungle_stairs", (short) 5579, 0.0, 0.0, false, true, null, false),

    COMMAND_BLOCK("minecraft:command_block", (short) 5654, 0.0, 3600000.0, false, true, NamespaceID.from("minecraft:command_block"), false),

    BEACON("minecraft:beacon", (short) 5660, 3.0, 3.0, false, true, NamespaceID.from("minecraft:beacon"), true),

    COBBLESTONE_WALL("minecraft:cobblestone_wall", (short) 5664, 0.0, 0.0, false, true, null, false),

    MOSSY_COBBLESTONE_WALL("minecraft:mossy_cobblestone_wall", (short) 5988, 0.0, 0.0, false, true, null, false),

    FLOWER_POT("minecraft:flower_pot", (short) 6309, 0.0, 0.0, false, true, null, true),

    POTTED_OAK_SAPLING("minecraft:potted_oak_sapling", (short) 6310, 0.0, 0.0, false, true, null, true),

    POTTED_SPRUCE_SAPLING("minecraft:potted_spruce_sapling", (short) 6311, 0.0, 0.0, false, true, null, true),

    POTTED_BIRCH_SAPLING("minecraft:potted_birch_sapling", (short) 6312, 0.0, 0.0, false, true, null, true),

    POTTED_JUNGLE_SAPLING("minecraft:potted_jungle_sapling", (short) 6313, 0.0, 0.0, false, true, null, true),

    POTTED_ACACIA_SAPLING("minecraft:potted_acacia_sapling", (short) 6314, 0.0, 0.0, false, true, null, true),

    POTTED_DARK_OAK_SAPLING("minecraft:potted_dark_oak_sapling", (short) 6315, 0.0, 0.0, false, true, null, true),

    POTTED_FERN("minecraft:potted_fern", (short) 6316, 0.0, 0.0, false, true, null, true),

    POTTED_DANDELION("minecraft:potted_dandelion", (short) 6317, 0.0, 0.0, false, true, null, true),

    POTTED_POPPY("minecraft:potted_poppy", (short) 6318, 0.0, 0.0, false, true, null, true),

    POTTED_BLUE_ORCHID("minecraft:potted_blue_orchid", (short) 6319, 0.0, 0.0, false, true, null, true),

    POTTED_ALLIUM("minecraft:potted_allium", (short) 6320, 0.0, 0.0, false, true, null, true),

    POTTED_AZURE_BLUET("minecraft:potted_azure_bluet", (short) 6321, 0.0, 0.0, false, true, null, true),

    POTTED_RED_TULIP("minecraft:potted_red_tulip", (short) 6322, 0.0, 0.0, false, true, null, true),

    POTTED_ORANGE_TULIP("minecraft:potted_orange_tulip", (short) 6323, 0.0, 0.0, false, true, null, true),

    POTTED_WHITE_TULIP("minecraft:potted_white_tulip", (short) 6324, 0.0, 0.0, false, true, null, true),

    POTTED_PINK_TULIP("minecraft:potted_pink_tulip", (short) 6325, 0.0, 0.0, false, true, null, true),

    POTTED_OXEYE_DAISY("minecraft:potted_oxeye_daisy", (short) 6326, 0.0, 0.0, false, true, null, true),

    POTTED_CORNFLOWER("minecraft:potted_cornflower", (short) 6327, 0.0, 0.0, false, true, null, true),

    POTTED_LILY_OF_THE_VALLEY("minecraft:potted_lily_of_the_valley", (short) 6328, 0.0, 0.0, false, true, null, true),

    POTTED_WITHER_ROSE("minecraft:potted_wither_rose", (short) 6329, 0.0, 0.0, false, true, null, true),

    POTTED_RED_MUSHROOM("minecraft:potted_red_mushroom", (short) 6330, 0.0, 0.0, false, true, null, true),

    POTTED_BROWN_MUSHROOM("minecraft:potted_brown_mushroom", (short) 6331, 0.0, 0.0, false, true, null, true),

    POTTED_DEAD_BUSH("minecraft:potted_dead_bush", (short) 6332, 0.0, 0.0, false, true, null, true),

    POTTED_CACTUS("minecraft:potted_cactus", (short) 6333, 0.0, 0.0, false, true, null, true),

    CARROTS("minecraft:carrots", (short) 6334, 0.0, 0.0, false, false, null, false),

    POTATOES("minecraft:potatoes", (short) 6342, 0.0, 0.0, false, false, null, false),

    OAK_BUTTON("minecraft:oak_button", (short) 6359, 0.5, 0.5, false, false, null, false),

    SPRUCE_BUTTON("minecraft:spruce_button", (short) 6383, 0.5, 0.5, false, false, null, false),

    BIRCH_BUTTON("minecraft:birch_button", (short) 6407, 0.5, 0.5, false, false, null, false),

    JUNGLE_BUTTON("minecraft:jungle_button", (short) 6431, 0.5, 0.5, false, false, null, false),

    ACACIA_BUTTON("minecraft:acacia_button", (short) 6455, 0.5, 0.5, false, false, null, false),

    DARK_OAK_BUTTON("minecraft:dark_oak_button", (short) 6479, 0.5, 0.5, false, false, null, false),

    SKELETON_SKULL("minecraft:skeleton_skull", (short) 6494, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    SKELETON_WALL_SKULL("minecraft:skeleton_wall_skull", (short) 6510, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    WITHER_SKELETON_SKULL("minecraft:wither_skeleton_skull", (short) 6514, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    WITHER_SKELETON_WALL_SKULL("minecraft:wither_skeleton_wall_skull", (short) 6530, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    ZOMBIE_HEAD("minecraft:zombie_head", (short) 6534, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    ZOMBIE_WALL_HEAD("minecraft:zombie_wall_head", (short) 6550, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    PLAYER_HEAD("minecraft:player_head", (short) 6554, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    PLAYER_WALL_HEAD("minecraft:player_wall_head", (short) 6570, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    CREEPER_HEAD("minecraft:creeper_head", (short) 6574, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    CREEPER_WALL_HEAD("minecraft:creeper_wall_head", (short) 6590, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    DRAGON_HEAD("minecraft:dragon_head", (short) 6594, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    DRAGON_WALL_HEAD("minecraft:dragon_wall_head", (short) 6610, 1.0, 1.0, false, true, NamespaceID.from("minecraft:skull"), false),

    ANVIL("minecraft:anvil", (short) 6614, 5.0, 1200.0, false, true, null, false),

    CHIPPED_ANVIL("minecraft:chipped_anvil", (short) 6618, 5.0, 1200.0, false, true, null, false),

    DAMAGED_ANVIL("minecraft:damaged_anvil", (short) 6622, 5.0, 1200.0, false, true, null, false),

    TRAPPED_CHEST("minecraft:trapped_chest", (short) 6627, 2.5, 2.5, false, true, NamespaceID.from("minecraft:trapped_chest"), false),

    LIGHT_WEIGHTED_PRESSURE_PLATE("minecraft:light_weighted_pressure_plate", (short) 6650, 0.5, 0.5, false, false, null, false),

    HEAVY_WEIGHTED_PRESSURE_PLATE("minecraft:heavy_weighted_pressure_plate", (short) 6666, 0.5, 0.5, false, false, null, false),

    COMPARATOR("minecraft:comparator", (short) 6683, 0.0, 0.0, false, true, NamespaceID.from("minecraft:comparator"), false),

    DAYLIGHT_DETECTOR("minecraft:daylight_detector", (short) 6714, 0.2, 0.2, false, true, NamespaceID.from("minecraft:daylight_detector"), false),

    REDSTONE_BLOCK("minecraft:redstone_block", (short) 6730, 5.0, 6.0, false, true, null, true),

    NETHER_QUARTZ_ORE("minecraft:nether_quartz_ore", (short) 6731, 3.0, 3.0, false, true, null, true),

    HOPPER("minecraft:hopper", (short) 6732, 3.0, 4.8, false, true, NamespaceID.from("minecraft:hopper"), false),

    QUARTZ_BLOCK("minecraft:quartz_block", (short) 6742, 0.8, 0.8, false, true, null, true),

    CHISELED_QUARTZ_BLOCK("minecraft:chiseled_quartz_block", (short) 6743, 0.8, 0.8, false, true, null, true),

    QUARTZ_PILLAR("minecraft:quartz_pillar", (short) 6745, 0.8, 0.8, false, true, null, false),

    QUARTZ_STAIRS("minecraft:quartz_stairs", (short) 6758, 0.0, 0.0, false, true, null, false),

    ACTIVATOR_RAIL("minecraft:activator_rail", (short) 6833, 0.7, 0.7, false, false, null, false),

    DROPPER("minecraft:dropper", (short) 6840, 3.5, 3.5, false, true, NamespaceID.from("minecraft:dropper"), false),

    WHITE_TERRACOTTA("minecraft:white_terracotta", (short) 6851, 1.25, 4.2, false, true, null, true),

    ORANGE_TERRACOTTA("minecraft:orange_terracotta", (short) 6852, 1.25, 4.2, false, true, null, true),

    MAGENTA_TERRACOTTA("minecraft:magenta_terracotta", (short) 6853, 1.25, 4.2, false, true, null, true),

    LIGHT_BLUE_TERRACOTTA("minecraft:light_blue_terracotta", (short) 6854, 1.25, 4.2, false, true, null, true),

    YELLOW_TERRACOTTA("minecraft:yellow_terracotta", (short) 6855, 1.25, 4.2, false, true, null, true),

    LIME_TERRACOTTA("minecraft:lime_terracotta", (short) 6856, 1.25, 4.2, false, true, null, true),

    PINK_TERRACOTTA("minecraft:pink_terracotta", (short) 6857, 1.25, 4.2, false, true, null, true),

    GRAY_TERRACOTTA("minecraft:gray_terracotta", (short) 6858, 1.25, 4.2, false, true, null, true),

    LIGHT_GRAY_TERRACOTTA("minecraft:light_gray_terracotta", (short) 6859, 1.25, 4.2, false, true, null, true),

    CYAN_TERRACOTTA("minecraft:cyan_terracotta", (short) 6860, 1.25, 4.2, false, true, null, true),

    PURPLE_TERRACOTTA("minecraft:purple_terracotta", (short) 6861, 1.25, 4.2, false, true, null, true),

    BLUE_TERRACOTTA("minecraft:blue_terracotta", (short) 6862, 1.25, 4.2, false, true, null, true),

    BROWN_TERRACOTTA("minecraft:brown_terracotta", (short) 6863, 1.25, 4.2, false, true, null, true),

    GREEN_TERRACOTTA("minecraft:green_terracotta", (short) 6864, 1.25, 4.2, false, true, null, true),

    RED_TERRACOTTA("minecraft:red_terracotta", (short) 6865, 1.25, 4.2, false, true, null, true),

    BLACK_TERRACOTTA("minecraft:black_terracotta", (short) 6866, 1.25, 4.2, false, true, null, true),

    WHITE_STAINED_GLASS_PANE("minecraft:white_stained_glass_pane", (short) 6898, 0.3, 0.3, false, true, null, false),

    ORANGE_STAINED_GLASS_PANE("minecraft:orange_stained_glass_pane", (short) 6930, 0.3, 0.3, false, true, null, false),

    MAGENTA_STAINED_GLASS_PANE("minecraft:magenta_stained_glass_pane", (short) 6962, 0.3, 0.3, false, true, null, false),

    LIGHT_BLUE_STAINED_GLASS_PANE("minecraft:light_blue_stained_glass_pane", (short) 6994, 0.3, 0.3, false, true, null, false),

    YELLOW_STAINED_GLASS_PANE("minecraft:yellow_stained_glass_pane", (short) 7026, 0.3, 0.3, false, true, null, false),

    LIME_STAINED_GLASS_PANE("minecraft:lime_stained_glass_pane", (short) 7058, 0.3, 0.3, false, true, null, false),

    PINK_STAINED_GLASS_PANE("minecraft:pink_stained_glass_pane", (short) 7090, 0.3, 0.3, false, true, null, false),

    GRAY_STAINED_GLASS_PANE("minecraft:gray_stained_glass_pane", (short) 7122, 0.3, 0.3, false, true, null, false),

    LIGHT_GRAY_STAINED_GLASS_PANE("minecraft:light_gray_stained_glass_pane", (short) 7154, 0.3, 0.3, false, true, null, false),

    CYAN_STAINED_GLASS_PANE("minecraft:cyan_stained_glass_pane", (short) 7186, 0.3, 0.3, false, true, null, false),

    PURPLE_STAINED_GLASS_PANE("minecraft:purple_stained_glass_pane", (short) 7218, 0.3, 0.3, false, true, null, false),

    BLUE_STAINED_GLASS_PANE("minecraft:blue_stained_glass_pane", (short) 7250, 0.3, 0.3, false, true, null, false),

    BROWN_STAINED_GLASS_PANE("minecraft:brown_stained_glass_pane", (short) 7282, 0.3, 0.3, false, true, null, false),

    GREEN_STAINED_GLASS_PANE("minecraft:green_stained_glass_pane", (short) 7314, 0.3, 0.3, false, true, null, false),

    RED_STAINED_GLASS_PANE("minecraft:red_stained_glass_pane", (short) 7346, 0.3, 0.3, false, true, null, false),

    BLACK_STAINED_GLASS_PANE("minecraft:black_stained_glass_pane", (short) 7378, 0.3, 0.3, false, true, null, false),

    ACACIA_STAIRS("minecraft:acacia_stairs", (short) 7390, 0.0, 0.0, false, true, null, false),

    DARK_OAK_STAIRS("minecraft:dark_oak_stairs", (short) 7470, 0.0, 0.0, false, true, null, false),

    SLIME_BLOCK("minecraft:slime_block", (short) 7539, 0.0, 0.0, false, true, null, true),

    BARRIER("minecraft:barrier", (short) 7540, 0.0, 3600000.75, false, true, null, true),

    IRON_TRAPDOOR("minecraft:iron_trapdoor", (short) 7556, 5.0, 5.0, false, true, null, false),

    PRISMARINE("minecraft:prismarine", (short) 7605, 1.5, 6.0, false, true, null, true),

    PRISMARINE_BRICKS("minecraft:prismarine_bricks", (short) 7606, 1.5, 6.0, false, true, null, true),

    DARK_PRISMARINE("minecraft:dark_prismarine", (short) 7607, 1.5, 6.0, false, true, null, true),

    PRISMARINE_STAIRS("minecraft:prismarine_stairs", (short) 7619, 0.0, 0.0, false, true, null, false),

    PRISMARINE_BRICK_STAIRS("minecraft:prismarine_brick_stairs", (short) 7699, 0.0, 0.0, false, true, null, false),

    DARK_PRISMARINE_STAIRS("minecraft:dark_prismarine_stairs", (short) 7779, 0.0, 0.0, false, true, null, false),

    PRISMARINE_SLAB("minecraft:prismarine_slab", (short) 7851, 1.5, 6.0, false, true, null, false),

    PRISMARINE_BRICK_SLAB("minecraft:prismarine_brick_slab", (short) 7857, 1.5, 6.0, false, true, null, false),

    DARK_PRISMARINE_SLAB("minecraft:dark_prismarine_slab", (short) 7863, 1.5, 6.0, false, true, null, false),

    SEA_LANTERN("minecraft:sea_lantern", (short) 7866, 0.3, 0.3, false, true, null, true),

    HAY_BLOCK("minecraft:hay_block", (short) 7868, 0.5, 0.5, false, true, null, false),

    WHITE_CARPET("minecraft:white_carpet", (short) 7870, 0.1, 0.1, false, true, null, true),

    ORANGE_CARPET("minecraft:orange_carpet", (short) 7871, 0.1, 0.1, false, true, null, true),

    MAGENTA_CARPET("minecraft:magenta_carpet", (short) 7872, 0.1, 0.1, false, true, null, true),

    LIGHT_BLUE_CARPET("minecraft:light_blue_carpet", (short) 7873, 0.1, 0.1, false, true, null, true),

    YELLOW_CARPET("minecraft:yellow_carpet", (short) 7874, 0.1, 0.1, false, true, null, true),

    LIME_CARPET("minecraft:lime_carpet", (short) 7875, 0.1, 0.1, false, true, null, true),

    PINK_CARPET("minecraft:pink_carpet", (short) 7876, 0.1, 0.1, false, true, null, true),

    GRAY_CARPET("minecraft:gray_carpet", (short) 7877, 0.1, 0.1, false, true, null, true),

    LIGHT_GRAY_CARPET("minecraft:light_gray_carpet", (short) 7878, 0.1, 0.1, false, true, null, true),

    CYAN_CARPET("minecraft:cyan_carpet", (short) 7879, 0.1, 0.1, false, true, null, true),

    PURPLE_CARPET("minecraft:purple_carpet", (short) 7880, 0.1, 0.1, false, true, null, true),

    BLUE_CARPET("minecraft:blue_carpet", (short) 7881, 0.1, 0.1, false, true, null, true),

    BROWN_CARPET("minecraft:brown_carpet", (short) 7882, 0.1, 0.1, false, true, null, true),

    GREEN_CARPET("minecraft:green_carpet", (short) 7883, 0.1, 0.1, false, true, null, true),

    RED_CARPET("minecraft:red_carpet", (short) 7884, 0.1, 0.1, false, true, null, true),

    BLACK_CARPET("minecraft:black_carpet", (short) 7885, 0.1, 0.1, false, true, null, true),

    TERRACOTTA("minecraft:terracotta", (short) 7886, 1.25, 4.2, false, true, null, true),

    COAL_BLOCK("minecraft:coal_block", (short) 7887, 5.0, 6.0, false, true, null, true),

    PACKED_ICE("minecraft:packed_ice", (short) 7888, 0.5, 0.5, false, true, null, true),

    SUNFLOWER("minecraft:sunflower", (short) 7890, 0.0, 0.0, false, false, null, false),

    LILAC("minecraft:lilac", (short) 7892, 0.0, 0.0, false, false, null, false),

    ROSE_BUSH("minecraft:rose_bush", (short) 7894, 0.0, 0.0, false, false, null, false),

    PEONY("minecraft:peony", (short) 7896, 0.0, 0.0, false, false, null, false),

    TALL_GRASS("minecraft:tall_grass", (short) 7898, 0.0, 0.0, false, false, null, false),

    LARGE_FERN("minecraft:large_fern", (short) 7900, 0.0, 0.0, false, false, null, false),

    WHITE_BANNER("minecraft:white_banner", (short) 7901, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    ORANGE_BANNER("minecraft:orange_banner", (short) 7917, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    MAGENTA_BANNER("minecraft:magenta_banner", (short) 7933, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    LIGHT_BLUE_BANNER("minecraft:light_blue_banner", (short) 7949, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    YELLOW_BANNER("minecraft:yellow_banner", (short) 7965, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    LIME_BANNER("minecraft:lime_banner", (short) 7981, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    PINK_BANNER("minecraft:pink_banner", (short) 7997, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    GRAY_BANNER("minecraft:gray_banner", (short) 8013, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    LIGHT_GRAY_BANNER("minecraft:light_gray_banner", (short) 8029, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    CYAN_BANNER("minecraft:cyan_banner", (short) 8045, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    PURPLE_BANNER("minecraft:purple_banner", (short) 8061, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    BLUE_BANNER("minecraft:blue_banner", (short) 8077, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    BROWN_BANNER("minecraft:brown_banner", (short) 8093, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    GREEN_BANNER("minecraft:green_banner", (short) 8109, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    RED_BANNER("minecraft:red_banner", (short) 8125, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    BLACK_BANNER("minecraft:black_banner", (short) 8141, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    WHITE_WALL_BANNER("minecraft:white_wall_banner", (short) 8157, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    ORANGE_WALL_BANNER("minecraft:orange_wall_banner", (short) 8161, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    MAGENTA_WALL_BANNER("minecraft:magenta_wall_banner", (short) 8165, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    LIGHT_BLUE_WALL_BANNER("minecraft:light_blue_wall_banner", (short) 8169, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    YELLOW_WALL_BANNER("minecraft:yellow_wall_banner", (short) 8173, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    LIME_WALL_BANNER("minecraft:lime_wall_banner", (short) 8177, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    PINK_WALL_BANNER("minecraft:pink_wall_banner", (short) 8181, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    GRAY_WALL_BANNER("minecraft:gray_wall_banner", (short) 8185, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    LIGHT_GRAY_WALL_BANNER("minecraft:light_gray_wall_banner", (short) 8189, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    CYAN_WALL_BANNER("minecraft:cyan_wall_banner", (short) 8193, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    PURPLE_WALL_BANNER("minecraft:purple_wall_banner", (short) 8197, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    BLUE_WALL_BANNER("minecraft:blue_wall_banner", (short) 8201, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    BROWN_WALL_BANNER("minecraft:brown_wall_banner", (short) 8205, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    GREEN_WALL_BANNER("minecraft:green_wall_banner", (short) 8209, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    RED_WALL_BANNER("minecraft:red_wall_banner", (short) 8213, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    BLACK_WALL_BANNER("minecraft:black_wall_banner", (short) 8217, 1.0, 1.0, false, false, NamespaceID.from("minecraft:banner"), false),

    RED_SANDSTONE("minecraft:red_sandstone", (short) 8221, 0.8, 0.8, false, true, null, true),

    CHISELED_RED_SANDSTONE("minecraft:chiseled_red_sandstone", (short) 8222, 0.8, 0.8, false, true, null, true),

    CUT_RED_SANDSTONE("minecraft:cut_red_sandstone", (short) 8223, 0.8, 0.8, false, true, null, true),

    RED_SANDSTONE_STAIRS("minecraft:red_sandstone_stairs", (short) 8235, 0.0, 0.0, false, true, null, false),

    OAK_SLAB("minecraft:oak_slab", (short) 8307, 2.0, 3.0, false, true, null, false),

    SPRUCE_SLAB("minecraft:spruce_slab", (short) 8313, 2.0, 3.0, false, true, null, false),

    BIRCH_SLAB("minecraft:birch_slab", (short) 8319, 2.0, 3.0, false, true, null, false),

    JUNGLE_SLAB("minecraft:jungle_slab", (short) 8325, 2.0, 3.0, false, true, null, false),

    ACACIA_SLAB("minecraft:acacia_slab", (short) 8331, 2.0, 3.0, false, true, null, false),

    DARK_OAK_SLAB("minecraft:dark_oak_slab", (short) 8337, 2.0, 3.0, false, true, null, false),

    STONE_SLAB("minecraft:stone_slab", (short) 8343, 2.0, 6.0, false, true, null, false),

    SMOOTH_STONE_SLAB("minecraft:smooth_stone_slab", (short) 8349, 2.0, 6.0, false, true, null, false),

    SANDSTONE_SLAB("minecraft:sandstone_slab", (short) 8355, 2.0, 6.0, false, true, null, false),

    CUT_SANDSTONE_SLAB("minecraft:cut_sandstone_slab", (short) 8361, 2.0, 6.0, false, true, null, false),

    PETRIFIED_OAK_SLAB("minecraft:petrified_oak_slab", (short) 8367, 2.0, 6.0, false, true, null, false),

    COBBLESTONE_SLAB("minecraft:cobblestone_slab", (short) 8373, 2.0, 6.0, false, true, null, false),

    BRICK_SLAB("minecraft:brick_slab", (short) 8379, 2.0, 6.0, false, true, null, false),

    STONE_BRICK_SLAB("minecraft:stone_brick_slab", (short) 8385, 2.0, 6.0, false, true, null, false),

    NETHER_BRICK_SLAB("minecraft:nether_brick_slab", (short) 8391, 2.0, 6.0, false, true, null, false),

    QUARTZ_SLAB("minecraft:quartz_slab", (short) 8397, 2.0, 6.0, false, true, null, false),

    RED_SANDSTONE_SLAB("minecraft:red_sandstone_slab", (short) 8403, 2.0, 6.0, false, true, null, false),

    CUT_RED_SANDSTONE_SLAB("minecraft:cut_red_sandstone_slab", (short) 8409, 2.0, 6.0, false, true, null, false),

    PURPUR_SLAB("minecraft:purpur_slab", (short) 8415, 2.0, 6.0, false, true, null, false),

    SMOOTH_STONE("minecraft:smooth_stone", (short) 8418, 2.0, 6.0, false, true, null, true),

    SMOOTH_SANDSTONE("minecraft:smooth_sandstone", (short) 8419, 2.0, 6.0, false, true, null, true),

    SMOOTH_QUARTZ("minecraft:smooth_quartz", (short) 8420, 2.0, 6.0, false, true, null, true),

    SMOOTH_RED_SANDSTONE("minecraft:smooth_red_sandstone", (short) 8421, 2.0, 6.0, false, true, null, true),

    SPRUCE_FENCE_GATE("minecraft:spruce_fence_gate", (short) 8429, 2.0, 3.0, false, true, null, false),

    BIRCH_FENCE_GATE("minecraft:birch_fence_gate", (short) 8461, 2.0, 3.0, false, true, null, false),

    JUNGLE_FENCE_GATE("minecraft:jungle_fence_gate", (short) 8493, 2.0, 3.0, false, true, null, false),

    ACACIA_FENCE_GATE("minecraft:acacia_fence_gate", (short) 8525, 2.0, 3.0, false, true, null, false),

    DARK_OAK_FENCE_GATE("minecraft:dark_oak_fence_gate", (short) 8557, 2.0, 3.0, false, true, null, false),

    SPRUCE_FENCE("minecraft:spruce_fence", (short) 8613, 2.0, 3.0, false, true, null, false),

    BIRCH_FENCE("minecraft:birch_fence", (short) 8645, 2.0, 3.0, false, true, null, false),

    JUNGLE_FENCE("minecraft:jungle_fence", (short) 8677, 2.0, 3.0, false, true, null, false),

    ACACIA_FENCE("minecraft:acacia_fence", (short) 8709, 2.0, 3.0, false, true, null, false),

    DARK_OAK_FENCE("minecraft:dark_oak_fence", (short) 8741, 2.0, 3.0, false, true, null, false),

    SPRUCE_DOOR("minecraft:spruce_door", (short) 8753, 3.0, 3.0, false, true, null, false),

    BIRCH_DOOR("minecraft:birch_door", (short) 8817, 3.0, 3.0, false, true, null, false),

    JUNGLE_DOOR("minecraft:jungle_door", (short) 8881, 3.0, 3.0, false, true, null, false),

    ACACIA_DOOR("minecraft:acacia_door", (short) 8945, 3.0, 3.0, false, true, null, false),

    DARK_OAK_DOOR("minecraft:dark_oak_door", (short) 9009, 3.0, 3.0, false, true, null, false),

    END_ROD("minecraft:end_rod", (short) 9066, 0.0, 0.0, false, true, null, false),

    CHORUS_PLANT("minecraft:chorus_plant", (short) 9131, 0.4, 0.4, false, true, null, false),

    CHORUS_FLOWER("minecraft:chorus_flower", (short) 9132, 0.4, 0.4, false, true, null, false),

    PURPUR_BLOCK("minecraft:purpur_block", (short) 9138, 1.5, 6.0, false, true, null, true),

    PURPUR_PILLAR("minecraft:purpur_pillar", (short) 9140, 1.5, 6.0, false, true, null, false),

    PURPUR_STAIRS("minecraft:purpur_stairs", (short) 9153, 0.0, 0.0, false, true, null, false),

    END_STONE_BRICKS("minecraft:end_stone_bricks", (short) 9222, 3.0, 9.0, false, true, null, true),

    BEETROOTS("minecraft:beetroots", (short) 9223, 0.0, 0.0, false, false, null, false),

    GRASS_PATH("minecraft:grass_path", (short) 9227, 0.65, 0.65, false, true, null, true),

    END_GATEWAY("minecraft:end_gateway", (short) 9228, 0.0, 3600000.0, false, false, NamespaceID.from("minecraft:end_gateway"), true),

    REPEATING_COMMAND_BLOCK("minecraft:repeating_command_block", (short) 9235, 0.0, 3600000.0, false, true, NamespaceID.from("minecraft:command_block"), false),

    CHAIN_COMMAND_BLOCK("minecraft:chain_command_block", (short) 9247, 0.0, 3600000.0, false, true, NamespaceID.from("minecraft:command_block"), false),

    FROSTED_ICE("minecraft:frosted_ice", (short) 9253, 0.5, 0.5, false, true, null, false),

    MAGMA_BLOCK("minecraft:magma_block", (short) 9257, 0.5, 0.5, false, true, null, true),

    NETHER_WART_BLOCK("minecraft:nether_wart_block", (short) 9258, 1.0, 1.0, false, true, null, true),

    RED_NETHER_BRICKS("minecraft:red_nether_bricks", (short) 9259, 2.0, 6.0, false, true, null, true),

    BONE_BLOCK("minecraft:bone_block", (short) 9261, 2.0, 2.0, false, true, null, false),

    STRUCTURE_VOID("minecraft:structure_void", (short) 9263, 0.0, 0.0, false, false, null, true),

    OBSERVER("minecraft:observer", (short) 9269, 3.0, 3.0, false, true, null, false),

    SHULKER_BOX("minecraft:shulker_box", (short) 9280, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    WHITE_SHULKER_BOX("minecraft:white_shulker_box", (short) 9286, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    ORANGE_SHULKER_BOX("minecraft:orange_shulker_box", (short) 9292, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    MAGENTA_SHULKER_BOX("minecraft:magenta_shulker_box", (short) 9298, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    LIGHT_BLUE_SHULKER_BOX("minecraft:light_blue_shulker_box", (short) 9304, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    YELLOW_SHULKER_BOX("minecraft:yellow_shulker_box", (short) 9310, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    LIME_SHULKER_BOX("minecraft:lime_shulker_box", (short) 9316, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    PINK_SHULKER_BOX("minecraft:pink_shulker_box", (short) 9322, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    GRAY_SHULKER_BOX("minecraft:gray_shulker_box", (short) 9328, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    LIGHT_GRAY_SHULKER_BOX("minecraft:light_gray_shulker_box", (short) 9334, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    CYAN_SHULKER_BOX("minecraft:cyan_shulker_box", (short) 9340, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    PURPLE_SHULKER_BOX("minecraft:purple_shulker_box", (short) 9346, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    BLUE_SHULKER_BOX("minecraft:blue_shulker_box", (short) 9352, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    BROWN_SHULKER_BOX("minecraft:brown_shulker_box", (short) 9358, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    GREEN_SHULKER_BOX("minecraft:green_shulker_box", (short) 9364, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    RED_SHULKER_BOX("minecraft:red_shulker_box", (short) 9370, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    BLACK_SHULKER_BOX("minecraft:black_shulker_box", (short) 9376, 2.0, 2.0, false, true, NamespaceID.from("minecraft:shulker_box"), false),

    WHITE_GLAZED_TERRACOTTA("minecraft:white_glazed_terracotta", (short) 9378, 1.4, 1.4, false, true, null, false),

    ORANGE_GLAZED_TERRACOTTA("minecraft:orange_glazed_terracotta", (short) 9382, 1.4, 1.4, false, true, null, false),

    MAGENTA_GLAZED_TERRACOTTA("minecraft:magenta_glazed_terracotta", (short) 9386, 1.4, 1.4, false, true, null, false),

    LIGHT_BLUE_GLAZED_TERRACOTTA("minecraft:light_blue_glazed_terracotta", (short) 9390, 1.4, 1.4, false, true, null, false),

    YELLOW_GLAZED_TERRACOTTA("minecraft:yellow_glazed_terracotta", (short) 9394, 1.4, 1.4, false, true, null, false),

    LIME_GLAZED_TERRACOTTA("minecraft:lime_glazed_terracotta", (short) 9398, 1.4, 1.4, false, true, null, false),

    PINK_GLAZED_TERRACOTTA("minecraft:pink_glazed_terracotta", (short) 9402, 1.4, 1.4, false, true, null, false),

    GRAY_GLAZED_TERRACOTTA("minecraft:gray_glazed_terracotta", (short) 9406, 1.4, 1.4, false, true, null, false),

    LIGHT_GRAY_GLAZED_TERRACOTTA("minecraft:light_gray_glazed_terracotta", (short) 9410, 1.4, 1.4, false, true, null, false),

    CYAN_GLAZED_TERRACOTTA("minecraft:cyan_glazed_terracotta", (short) 9414, 1.4, 1.4, false, true, null, false),

    PURPLE_GLAZED_TERRACOTTA("minecraft:purple_glazed_terracotta", (short) 9418, 1.4, 1.4, false, true, null, false),

    BLUE_GLAZED_TERRACOTTA("minecraft:blue_glazed_terracotta", (short) 9422, 1.4, 1.4, false, true, null, false),

    BROWN_GLAZED_TERRACOTTA("minecraft:brown_glazed_terracotta", (short) 9426, 1.4, 1.4, false, true, null, false),

    GREEN_GLAZED_TERRACOTTA("minecraft:green_glazed_terracotta", (short) 9430, 1.4, 1.4, false, true, null, false),

    RED_GLAZED_TERRACOTTA("minecraft:red_glazed_terracotta", (short) 9434, 1.4, 1.4, false, true, null, false),

    BLACK_GLAZED_TERRACOTTA("minecraft:black_glazed_terracotta", (short) 9438, 1.4, 1.4, false, true, null, false),

    WHITE_CONCRETE("minecraft:white_concrete", (short) 9442, 1.8, 1.8, false, true, null, true),

    ORANGE_CONCRETE("minecraft:orange_concrete", (short) 9443, 1.8, 1.8, false, true, null, true),

    MAGENTA_CONCRETE("minecraft:magenta_concrete", (short) 9444, 1.8, 1.8, false, true, null, true),

    LIGHT_BLUE_CONCRETE("minecraft:light_blue_concrete", (short) 9445, 1.8, 1.8, false, true, null, true),

    YELLOW_CONCRETE("minecraft:yellow_concrete", (short) 9446, 1.8, 1.8, false, true, null, true),

    LIME_CONCRETE("minecraft:lime_concrete", (short) 9447, 1.8, 1.8, false, true, null, true),

    PINK_CONCRETE("minecraft:pink_concrete", (short) 9448, 1.8, 1.8, false, true, null, true),

    GRAY_CONCRETE("minecraft:gray_concrete", (short) 9449, 1.8, 1.8, false, true, null, true),

    LIGHT_GRAY_CONCRETE("minecraft:light_gray_concrete", (short) 9450, 1.8, 1.8, false, true, null, true),

    CYAN_CONCRETE("minecraft:cyan_concrete", (short) 9451, 1.8, 1.8, false, true, null, true),

    PURPLE_CONCRETE("minecraft:purple_concrete", (short) 9452, 1.8, 1.8, false, true, null, true),

    BLUE_CONCRETE("minecraft:blue_concrete", (short) 9453, 1.8, 1.8, false, true, null, true),

    BROWN_CONCRETE("minecraft:brown_concrete", (short) 9454, 1.8, 1.8, false, true, null, true),

    GREEN_CONCRETE("minecraft:green_concrete", (short) 9455, 1.8, 1.8, false, true, null, true),

    RED_CONCRETE("minecraft:red_concrete", (short) 9456, 1.8, 1.8, false, true, null, true),

    BLACK_CONCRETE("minecraft:black_concrete", (short) 9457, 1.8, 1.8, false, true, null, true),

    WHITE_CONCRETE_POWDER("minecraft:white_concrete_powder", (short) 9458, 0.5, 0.5, false, true, null, true),

    ORANGE_CONCRETE_POWDER("minecraft:orange_concrete_powder", (short) 9459, 0.5, 0.5, false, true, null, true),

    MAGENTA_CONCRETE_POWDER("minecraft:magenta_concrete_powder", (short) 9460, 0.5, 0.5, false, true, null, true),

    LIGHT_BLUE_CONCRETE_POWDER("minecraft:light_blue_concrete_powder", (short) 9461, 0.5, 0.5, false, true, null, true),

    YELLOW_CONCRETE_POWDER("minecraft:yellow_concrete_powder", (short) 9462, 0.5, 0.5, false, true, null, true),

    LIME_CONCRETE_POWDER("minecraft:lime_concrete_powder", (short) 9463, 0.5, 0.5, false, true, null, true),

    PINK_CONCRETE_POWDER("minecraft:pink_concrete_powder", (short) 9464, 0.5, 0.5, false, true, null, true),

    GRAY_CONCRETE_POWDER("minecraft:gray_concrete_powder", (short) 9465, 0.5, 0.5, false, true, null, true),

    LIGHT_GRAY_CONCRETE_POWDER("minecraft:light_gray_concrete_powder", (short) 9466, 0.5, 0.5, false, true, null, true),

    CYAN_CONCRETE_POWDER("minecraft:cyan_concrete_powder", (short) 9467, 0.5, 0.5, false, true, null, true),

    PURPLE_CONCRETE_POWDER("minecraft:purple_concrete_powder", (short) 9468, 0.5, 0.5, false, true, null, true),

    BLUE_CONCRETE_POWDER("minecraft:blue_concrete_powder", (short) 9469, 0.5, 0.5, false, true, null, true),

    BROWN_CONCRETE_POWDER("minecraft:brown_concrete_powder", (short) 9470, 0.5, 0.5, false, true, null, true),

    GREEN_CONCRETE_POWDER("minecraft:green_concrete_powder", (short) 9471, 0.5, 0.5, false, true, null, true),

    RED_CONCRETE_POWDER("minecraft:red_concrete_powder", (short) 9472, 0.5, 0.5, false, true, null, true),

    BLACK_CONCRETE_POWDER("minecraft:black_concrete_powder", (short) 9473, 0.5, 0.5, false, true, null, true),

    KELP("minecraft:kelp", (short) 9474, 0.0, 0.0, false, false, null, false),

    KELP_PLANT("minecraft:kelp_plant", (short) 9500, 0.0, 0.0, false, false, null, true),

    DRIED_KELP_BLOCK("minecraft:dried_kelp_block", (short) 9501, 0.5, 2.5, false, true, null, true),

    TURTLE_EGG("minecraft:turtle_egg", (short) 9502, 0.5, 0.5, false, true, null, false),

    DEAD_TUBE_CORAL_BLOCK("minecraft:dead_tube_coral_block", (short) 9514, 1.5, 6.0, false, true, null, true),

    DEAD_BRAIN_CORAL_BLOCK("minecraft:dead_brain_coral_block", (short) 9515, 1.5, 6.0, false, true, null, true),

    DEAD_BUBBLE_CORAL_BLOCK("minecraft:dead_bubble_coral_block", (short) 9516, 1.5, 6.0, false, true, null, true),

    DEAD_FIRE_CORAL_BLOCK("minecraft:dead_fire_coral_block", (short) 9517, 1.5, 6.0, false, true, null, true),

    DEAD_HORN_CORAL_BLOCK("minecraft:dead_horn_coral_block", (short) 9518, 1.5, 6.0, false, true, null, true),

    TUBE_CORAL_BLOCK("minecraft:tube_coral_block", (short) 9519, 1.5, 6.0, false, true, null, true),

    BRAIN_CORAL_BLOCK("minecraft:brain_coral_block", (short) 9520, 1.5, 6.0, false, true, null, true),

    BUBBLE_CORAL_BLOCK("minecraft:bubble_coral_block", (short) 9521, 1.5, 6.0, false, true, null, true),

    FIRE_CORAL_BLOCK("minecraft:fire_coral_block", (short) 9522, 1.5, 6.0, false, true, null, true),

    HORN_CORAL_BLOCK("minecraft:horn_coral_block", (short) 9523, 1.5, 6.0, false, true, null, true),

    DEAD_TUBE_CORAL("minecraft:dead_tube_coral", (short) 9524, 0.0, 0.0, false, false, null, false),

    DEAD_BRAIN_CORAL("minecraft:dead_brain_coral", (short) 9526, 0.0, 0.0, false, false, null, false),

    DEAD_BUBBLE_CORAL("minecraft:dead_bubble_coral", (short) 9528, 0.0, 0.0, false, false, null, false),

    DEAD_FIRE_CORAL("minecraft:dead_fire_coral", (short) 9530, 0.0, 0.0, false, false, null, false),

    DEAD_HORN_CORAL("minecraft:dead_horn_coral", (short) 9532, 0.0, 0.0, false, false, null, false),

    TUBE_CORAL("minecraft:tube_coral", (short) 9534, 0.0, 0.0, false, false, null, false),

    BRAIN_CORAL("minecraft:brain_coral", (short) 9536, 0.0, 0.0, false, false, null, false),

    BUBBLE_CORAL("minecraft:bubble_coral", (short) 9538, 0.0, 0.0, false, false, null, false),

    FIRE_CORAL("minecraft:fire_coral", (short) 9540, 0.0, 0.0, false, false, null, false),

    HORN_CORAL("minecraft:horn_coral", (short) 9542, 0.0, 0.0, false, false, null, false),

    DEAD_TUBE_CORAL_FAN("minecraft:dead_tube_coral_fan", (short) 9544, 0.0, 0.0, false, false, null, false),

    DEAD_BRAIN_CORAL_FAN("minecraft:dead_brain_coral_fan", (short) 9546, 0.0, 0.0, false, false, null, false),

    DEAD_BUBBLE_CORAL_FAN("minecraft:dead_bubble_coral_fan", (short) 9548, 0.0, 0.0, false, false, null, false),

    DEAD_FIRE_CORAL_FAN("minecraft:dead_fire_coral_fan", (short) 9550, 0.0, 0.0, false, false, null, false),

    DEAD_HORN_CORAL_FAN("minecraft:dead_horn_coral_fan", (short) 9552, 0.0, 0.0, false, false, null, false),

    TUBE_CORAL_FAN("minecraft:tube_coral_fan", (short) 9554, 0.0, 0.0, false, false, null, false),

    BRAIN_CORAL_FAN("minecraft:brain_coral_fan", (short) 9556, 0.0, 0.0, false, false, null, false),

    BUBBLE_CORAL_FAN("minecraft:bubble_coral_fan", (short) 9558, 0.0, 0.0, false, false, null, false),

    FIRE_CORAL_FAN("minecraft:fire_coral_fan", (short) 9560, 0.0, 0.0, false, false, null, false),

    HORN_CORAL_FAN("minecraft:horn_coral_fan", (short) 9562, 0.0, 0.0, false, false, null, false),

    DEAD_TUBE_CORAL_WALL_FAN("minecraft:dead_tube_coral_wall_fan", (short) 9564, 0.0, 0.0, false, false, null, false),

    DEAD_BRAIN_CORAL_WALL_FAN("minecraft:dead_brain_coral_wall_fan", (short) 9572, 0.0, 0.0, false, false, null, false),

    DEAD_BUBBLE_CORAL_WALL_FAN("minecraft:dead_bubble_coral_wall_fan", (short) 9580, 0.0, 0.0, false, false, null, false),

    DEAD_FIRE_CORAL_WALL_FAN("minecraft:dead_fire_coral_wall_fan", (short) 9588, 0.0, 0.0, false, false, null, false),

    DEAD_HORN_CORAL_WALL_FAN("minecraft:dead_horn_coral_wall_fan", (short) 9596, 0.0, 0.0, false, false, null, false),

    TUBE_CORAL_WALL_FAN("minecraft:tube_coral_wall_fan", (short) 9604, 0.0, 0.0, false, false, null, false),

    BRAIN_CORAL_WALL_FAN("minecraft:brain_coral_wall_fan", (short) 9612, 0.0, 0.0, false, false, null, false),

    BUBBLE_CORAL_WALL_FAN("minecraft:bubble_coral_wall_fan", (short) 9620, 0.0, 0.0, false, false, null, false),

    FIRE_CORAL_WALL_FAN("minecraft:fire_coral_wall_fan", (short) 9628, 0.0, 0.0, false, false, null, false),

    HORN_CORAL_WALL_FAN("minecraft:horn_coral_wall_fan", (short) 9636, 0.0, 0.0, false, false, null, false),

    SEA_PICKLE("minecraft:sea_pickle", (short) 9644, 0.0, 0.0, false, true, null, false),

    BLUE_ICE("minecraft:blue_ice", (short) 9652, 2.8, 2.8, false, true, null, true),

    CONDUIT("minecraft:conduit", (short) 9653, 3.0, 3.0, false, true, NamespaceID.from("minecraft:conduit"), false),

    BAMBOO_SAPLING("minecraft:bamboo_sapling", (short) 9655, 1.0, 1.0, false, false, null, true),

    BAMBOO("minecraft:bamboo", (short) 9656, 1.0, 1.0, false, true, null, false),

    POTTED_BAMBOO("minecraft:potted_bamboo", (short) 9668, 0.0, 0.0, false, true, null, true),

    VOID_AIR("minecraft:void_air", (short) 9669, 0.0, 0.0, true, false, null, true),

    CAVE_AIR("minecraft:cave_air", (short) 9670, 0.0, 0.0, true, false, null, true),

    BUBBLE_COLUMN("minecraft:bubble_column", (short) 9671, 0.0, 0.0, false, false, null, false),

    POLISHED_GRANITE_STAIRS("minecraft:polished_granite_stairs", (short) 9684, 0.0, 0.0, false, true, null, false),

    SMOOTH_RED_SANDSTONE_STAIRS("minecraft:smooth_red_sandstone_stairs", (short) 9764, 0.0, 0.0, false, true, null, false),

    MOSSY_STONE_BRICK_STAIRS("minecraft:mossy_stone_brick_stairs", (short) 9844, 0.0, 0.0, false, true, null, false),

    POLISHED_DIORITE_STAIRS("minecraft:polished_diorite_stairs", (short) 9924, 0.0, 0.0, false, true, null, false),

    MOSSY_COBBLESTONE_STAIRS("minecraft:mossy_cobblestone_stairs", (short) 10004, 0.0, 0.0, false, true, null, false),

    END_STONE_BRICK_STAIRS("minecraft:end_stone_brick_stairs", (short) 10084, 0.0, 0.0, false, true, null, false),

    STONE_STAIRS("minecraft:stone_stairs", (short) 10164, 0.0, 0.0, false, true, null, false),

    SMOOTH_SANDSTONE_STAIRS("minecraft:smooth_sandstone_stairs", (short) 10244, 0.0, 0.0, false, true, null, false),

    SMOOTH_QUARTZ_STAIRS("minecraft:smooth_quartz_stairs", (short) 10324, 0.0, 0.0, false, true, null, false),

    GRANITE_STAIRS("minecraft:granite_stairs", (short) 10404, 0.0, 0.0, false, true, null, false),

    ANDESITE_STAIRS("minecraft:andesite_stairs", (short) 10484, 0.0, 0.0, false, true, null, false),

    RED_NETHER_BRICK_STAIRS("minecraft:red_nether_brick_stairs", (short) 10564, 0.0, 0.0, false, true, null, false),

    POLISHED_ANDESITE_STAIRS("minecraft:polished_andesite_stairs", (short) 10644, 0.0, 0.0, false, true, null, false),

    DIORITE_STAIRS("minecraft:diorite_stairs", (short) 10724, 0.0, 0.0, false, true, null, false),

    POLISHED_GRANITE_SLAB("minecraft:polished_granite_slab", (short) 10796, 0.0, 0.0, false, true, null, false),

    SMOOTH_RED_SANDSTONE_SLAB("minecraft:smooth_red_sandstone_slab", (short) 10802, 0.0, 0.0, false, true, null, false),

    MOSSY_STONE_BRICK_SLAB("minecraft:mossy_stone_brick_slab", (short) 10808, 0.0, 0.0, false, true, null, false),

    POLISHED_DIORITE_SLAB("minecraft:polished_diorite_slab", (short) 10814, 0.0, 0.0, false, true, null, false),

    MOSSY_COBBLESTONE_SLAB("minecraft:mossy_cobblestone_slab", (short) 10820, 0.0, 0.0, false, true, null, false),

    END_STONE_BRICK_SLAB("minecraft:end_stone_brick_slab", (short) 10826, 0.0, 0.0, false, true, null, false),

    SMOOTH_SANDSTONE_SLAB("minecraft:smooth_sandstone_slab", (short) 10832, 0.0, 0.0, false, true, null, false),

    SMOOTH_QUARTZ_SLAB("minecraft:smooth_quartz_slab", (short) 10838, 0.0, 0.0, false, true, null, false),

    GRANITE_SLAB("minecraft:granite_slab", (short) 10844, 0.0, 0.0, false, true, null, false),

    ANDESITE_SLAB("minecraft:andesite_slab", (short) 10850, 0.0, 0.0, false, true, null, false),

    RED_NETHER_BRICK_SLAB("minecraft:red_nether_brick_slab", (short) 10856, 0.0, 0.0, false, true, null, false),

    POLISHED_ANDESITE_SLAB("minecraft:polished_andesite_slab", (short) 10862, 0.0, 0.0, false, true, null, false),

    DIORITE_SLAB("minecraft:diorite_slab", (short) 10868, 0.0, 0.0, false, true, null, false),

    BRICK_WALL("minecraft:brick_wall", (short) 10874, 0.0, 0.0, false, true, null, false),

    PRISMARINE_WALL("minecraft:prismarine_wall", (short) 11198, 0.0, 0.0, false, true, null, false),

    RED_SANDSTONE_WALL("minecraft:red_sandstone_wall", (short) 11522, 0.0, 0.0, false, true, null, false),

    MOSSY_STONE_BRICK_WALL("minecraft:mossy_stone_brick_wall", (short) 11846, 0.0, 0.0, false, true, null, false),

    GRANITE_WALL("minecraft:granite_wall", (short) 12170, 0.0, 0.0, false, true, null, false),

    STONE_BRICK_WALL("minecraft:stone_brick_wall", (short) 12494, 0.0, 0.0, false, true, null, false),

    NETHER_BRICK_WALL("minecraft:nether_brick_wall", (short) 12818, 0.0, 0.0, false, true, null, false),

    ANDESITE_WALL("minecraft:andesite_wall", (short) 13142, 0.0, 0.0, false, true, null, false),

    RED_NETHER_BRICK_WALL("minecraft:red_nether_brick_wall", (short) 13466, 0.0, 0.0, false, true, null, false),

    SANDSTONE_WALL("minecraft:sandstone_wall", (short) 13790, 0.0, 0.0, false, true, null, false),

    END_STONE_BRICK_WALL("minecraft:end_stone_brick_wall", (short) 14114, 0.0, 0.0, false, true, null, false),

    DIORITE_WALL("minecraft:diorite_wall", (short) 14438, 0.0, 0.0, false, true, null, false),

    SCAFFOLDING("minecraft:scaffolding", (short) 14790, 0.0, 0.0, false, true, null, false),

    LOOM("minecraft:loom", (short) 14791, 2.5, 2.5, false, true, null, false),

    BARREL("minecraft:barrel", (short) 14796, 2.5, 2.5, false, true, NamespaceID.from("minecraft:barrel"), false),

    SMOKER("minecraft:smoker", (short) 14808, 3.5, 3.5, false, true, NamespaceID.from("minecraft:smoker"), false),

    BLAST_FURNACE("minecraft:blast_furnace", (short) 14816, 3.5, 3.5, false, true, NamespaceID.from("minecraft:blast_furnace"), false),

    CARTOGRAPHY_TABLE("minecraft:cartography_table", (short) 14823, 2.5, 2.5, false, true, null, true),

    FLETCHING_TABLE("minecraft:fletching_table", (short) 14824, 2.5, 2.5, false, true, null, true),

    GRINDSTONE("minecraft:grindstone", (short) 14829, 2.0, 6.0, false, true, null, false),

    LECTERN("minecraft:lectern", (short) 14840, 2.5, 2.5, false, true, NamespaceID.from("minecraft:lectern"), false),

    SMITHING_TABLE("minecraft:smithing_table", (short) 14853, 2.5, 2.5, false, true, null, true),

    STONECUTTER("minecraft:stonecutter", (short) 14854, 3.5, 3.5, false, true, null, false),

    BELL("minecraft:bell", (short) 14859, 5.0, 5.0, false, true, NamespaceID.from("minecraft:bell"), false),

    LANTERN("minecraft:lantern", (short) 14893, 3.5, 3.5, false, true, null, false),

    SOUL_LANTERN("minecraft:soul_lantern", (short) 14897, 3.5, 3.5, false, true, null, false),

    CAMPFIRE("minecraft:campfire", (short) 14901, 2.0, 2.0, false, true, NamespaceID.from("minecraft:campfire"), false),

    SOUL_CAMPFIRE("minecraft:soul_campfire", (short) 14933, 2.0, 2.0, false, true, NamespaceID.from("minecraft:campfire"), false),

    SWEET_BERRY_BUSH("minecraft:sweet_berry_bush", (short) 14962, 0.0, 0.0, false, false, null, false),

    WARPED_STEM("minecraft:warped_stem", (short) 14967, 2.0, 2.0, false, true, null, false),

    STRIPPED_WARPED_STEM("minecraft:stripped_warped_stem", (short) 14970, 2.0, 2.0, false, true, null, false),

    WARPED_HYPHAE("minecraft:warped_hyphae", (short) 14973, 2.0, 2.0, false, true, null, false),

    STRIPPED_WARPED_HYPHAE("minecraft:stripped_warped_hyphae", (short) 14976, 2.0, 2.0, false, true, null, false),

    WARPED_NYLIUM("minecraft:warped_nylium", (short) 14978, 0.4, 0.4, false, true, null, true),

    WARPED_FUNGUS("minecraft:warped_fungus", (short) 14979, 0.0, 0.0, false, false, null, true),

    WARPED_WART_BLOCK("minecraft:warped_wart_block", (short) 14980, 1.0, 1.0, false, true, null, true),

    WARPED_ROOTS("minecraft:warped_roots", (short) 14981, 0.0, 0.0, false, false, null, true),

    NETHER_SPROUTS("minecraft:nether_sprouts", (short) 14982, 0.0, 0.0, false, false, null, true),

    CRIMSON_STEM("minecraft:crimson_stem", (short) 14984, 2.0, 2.0, false, true, null, false),

    STRIPPED_CRIMSON_STEM("minecraft:stripped_crimson_stem", (short) 14987, 2.0, 2.0, false, true, null, false),

    CRIMSON_HYPHAE("minecraft:crimson_hyphae", (short) 14990, 2.0, 2.0, false, true, null, false),

    STRIPPED_CRIMSON_HYPHAE("minecraft:stripped_crimson_hyphae", (short) 14993, 2.0, 2.0, false, true, null, false),

    CRIMSON_NYLIUM("minecraft:crimson_nylium", (short) 14995, 0.4, 0.4, false, true, null, true),

    CRIMSON_FUNGUS("minecraft:crimson_fungus", (short) 14996, 0.0, 0.0, false, false, null, true),

    SHROOMLIGHT("minecraft:shroomlight", (short) 14997, 1.0, 1.0, false, true, null, true),

    WEEPING_VINES("minecraft:weeping_vines", (short) 14998, 0.0, 0.0, false, false, null, false),

    WEEPING_VINES_PLANT("minecraft:weeping_vines_plant", (short) 15024, 0.0, 0.0, false, false, null, true),

    TWISTING_VINES("minecraft:twisting_vines", (short) 15025, 0.0, 0.0, false, false, null, false),

    TWISTING_VINES_PLANT("minecraft:twisting_vines_plant", (short) 15051, 0.0, 0.0, false, false, null, true),

    CRIMSON_ROOTS("minecraft:crimson_roots", (short) 15052, 0.0, 0.0, false, false, null, true),

    CRIMSON_PLANKS("minecraft:crimson_planks", (short) 15053, 2.0, 3.0, false, true, null, true),

    WARPED_PLANKS("minecraft:warped_planks", (short) 15054, 2.0, 3.0, false, true, null, true),

    CRIMSON_SLAB("minecraft:crimson_slab", (short) 15058, 2.0, 3.0, false, true, null, false),

    WARPED_SLAB("minecraft:warped_slab", (short) 15064, 2.0, 3.0, false, true, null, false),

    CRIMSON_PRESSURE_PLATE("minecraft:crimson_pressure_plate", (short) 15068, 0.5, 0.5, false, false, null, false),

    WARPED_PRESSURE_PLATE("minecraft:warped_pressure_plate", (short) 15070, 0.5, 0.5, false, false, null, false),

    CRIMSON_FENCE("minecraft:crimson_fence", (short) 15102, 2.0, 3.0, false, true, null, false),

    WARPED_FENCE("minecraft:warped_fence", (short) 15134, 2.0, 3.0, false, true, null, false),

    CRIMSON_TRAPDOOR("minecraft:crimson_trapdoor", (short) 15150, 3.0, 3.0, false, true, null, false),

    WARPED_TRAPDOOR("minecraft:warped_trapdoor", (short) 15214, 3.0, 3.0, false, true, null, false),

    CRIMSON_FENCE_GATE("minecraft:crimson_fence_gate", (short) 15270, 2.0, 3.0, false, true, null, false),

    WARPED_FENCE_GATE("minecraft:warped_fence_gate", (short) 15302, 2.0, 3.0, false, true, null, false),

    CRIMSON_STAIRS("minecraft:crimson_stairs", (short) 15338, 0.0, 0.0, false, true, null, false),

    WARPED_STAIRS("minecraft:warped_stairs", (short) 15418, 0.0, 0.0, false, true, null, false),

    CRIMSON_BUTTON("minecraft:crimson_button", (short) 15496, 0.5, 0.5, false, false, null, false),

    WARPED_BUTTON("minecraft:warped_button", (short) 15520, 0.5, 0.5, false, false, null, false),

    CRIMSON_DOOR("minecraft:crimson_door", (short) 15546, 3.0, 3.0, false, true, null, false),

    WARPED_DOOR("minecraft:warped_door", (short) 15610, 3.0, 3.0, false, true, null, false),

    CRIMSON_SIGN("minecraft:crimson_sign", (short) 15664, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    WARPED_SIGN("minecraft:warped_sign", (short) 15696, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    CRIMSON_WALL_SIGN("minecraft:crimson_wall_sign", (short) 15728, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    WARPED_WALL_SIGN("minecraft:warped_wall_sign", (short) 15736, 1.0, 1.0, false, false, NamespaceID.from("minecraft:sign"), false),

    STRUCTURE_BLOCK("minecraft:structure_block", (short) 15743, 0.0, 3600000.0, false, true, NamespaceID.from("minecraft:structure_block"), false),

    JIGSAW("minecraft:jigsaw", (short) 15757, 0.0, 3600000.0, false, true, NamespaceID.from("minecraft:jigsaw"), false),

    COMPOSTER("minecraft:composter", (short) 15759, 0.6, 0.6, false, true, null, false),

    TARGET("minecraft:target", (short) 15768, 0.5, 0.5, false, true, null, false),

    BEE_NEST("minecraft:bee_nest", (short) 15784, 0.3, 0.3, false, true, NamespaceID.from("minecraft:beehive"), false),

    BEEHIVE("minecraft:beehive", (short) 15808, 0.6, 0.6, false, true, NamespaceID.from("minecraft:beehive"), false),

    HONEY_BLOCK("minecraft:honey_block", (short) 15832, 0.0, 0.0, false, true, null, true),

    HONEYCOMB_BLOCK("minecraft:honeycomb_block", (short) 15833, 0.6, 0.6, false, true, null, true),

    NETHERITE_BLOCK("minecraft:netherite_block", (short) 15834, 50.0, 1200.0, false, true, null, true),

    ANCIENT_DEBRIS("minecraft:ancient_debris", (short) 15835, 30.0, 1200.0, false, true, null, true),

    CRYING_OBSIDIAN("minecraft:crying_obsidian", (short) 15836, 50.0, 1200.0, false, true, null, true),

    RESPAWN_ANCHOR("minecraft:respawn_anchor", (short) 15837, 50.0, 1200.0, false, true, null, false),

    POTTED_CRIMSON_FUNGUS("minecraft:potted_crimson_fungus", (short) 15842, 0.0, 0.0, false, true, null, true),

    POTTED_WARPED_FUNGUS("minecraft:potted_warped_fungus", (short) 15843, 0.0, 0.0, false, true, null, true),

    POTTED_CRIMSON_ROOTS("minecraft:potted_crimson_roots", (short) 15844, 0.0, 0.0, false, true, null, true),

    POTTED_WARPED_ROOTS("minecraft:potted_warped_roots", (short) 15845, 0.0, 0.0, false, true, null, true),

    LODESTONE("minecraft:lodestone", (short) 15846, 3.5, 3.5, false, true, null, true),

    BLACKSTONE("minecraft:blackstone", (short) 15847, 1.5, 6.0, false, true, null, true),

    BLACKSTONE_STAIRS("minecraft:blackstone_stairs", (short) 15859, 0.0, 0.0, false, true, null, false),

    BLACKSTONE_WALL("minecraft:blackstone_wall", (short) 15931, 0.0, 0.0, false, true, null, false),

    BLACKSTONE_SLAB("minecraft:blackstone_slab", (short) 16255, 2.0, 6.0, false, true, null, false),

    POLISHED_BLACKSTONE("minecraft:polished_blackstone", (short) 16258, 2.0, 6.0, false, true, null, true),

    POLISHED_BLACKSTONE_BRICKS("minecraft:polished_blackstone_bricks", (short) 16259, 1.5, 6.0, false, true, null, true),

    CRACKED_POLISHED_BLACKSTONE_BRICKS("minecraft:cracked_polished_blackstone_bricks", (short) 16260, 0.0, 0.0, false, true, null, true),

    CHISELED_POLISHED_BLACKSTONE("minecraft:chiseled_polished_blackstone", (short) 16261, 1.5, 6.0, false, true, null, true),

    POLISHED_BLACKSTONE_BRICK_SLAB("minecraft:polished_blackstone_brick_slab", (short) 16265, 2.0, 6.0, false, true, null, false),

    POLISHED_BLACKSTONE_BRICK_STAIRS("minecraft:polished_blackstone_brick_stairs", (short) 16279, 0.0, 0.0, false, true, null, false),

    POLISHED_BLACKSTONE_BRICK_WALL("minecraft:polished_blackstone_brick_wall", (short) 16351, 0.0, 0.0, false, true, null, false),

    GILDED_BLACKSTONE("minecraft:gilded_blackstone", (short) 16672, 0.0, 0.0, false, true, null, true),

    POLISHED_BLACKSTONE_STAIRS("minecraft:polished_blackstone_stairs", (short) 16684, 0.0, 0.0, false, true, null, false),

    POLISHED_BLACKSTONE_SLAB("minecraft:polished_blackstone_slab", (short) 16756, 0.0, 0.0, false, true, null, false),

    POLISHED_BLACKSTONE_PRESSURE_PLATE("minecraft:polished_blackstone_pressure_plate", (short) 16760, 0.5, 0.5, false, false, null, false),

    POLISHED_BLACKSTONE_BUTTON("minecraft:polished_blackstone_button", (short) 16770, 0.5, 0.5, false, false, null, false),

    POLISHED_BLACKSTONE_WALL("minecraft:polished_blackstone_wall", (short) 16788, 0.0, 0.0, false, true, null, false),

    CHISELED_NETHER_BRICKS("minecraft:chiseled_nether_bricks", (short) 17109, 2.0, 6.0, false, true, null, true),

    CRACKED_NETHER_BRICKS("minecraft:cracked_nether_bricks", (short) 17110, 2.0, 6.0, false, true, null, true),

    QUARTZ_BRICKS("minecraft:quartz_bricks", (short) 17111, 0.0, 0.0, false, true, null, true);

    static {
        GrassBlock.initStates();
        Podzol.initStates();
        OakSapling.initStates();
        SpruceSapling.initStates();
        BirchSapling.initStates();
        JungleSapling.initStates();
        AcaciaSapling.initStates();
        DarkOakSapling.initStates();
        Water.initStates();
        Lava.initStates();
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
        Dispenser.initStates();
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
        TallSeagrass.initStates();
        Piston.initStates();
        PistonHead.initStates();
        MovingPiston.initStates();
        Tnt.initStates();
        WallTorch.initStates();
        Fire.initStates();
        OakStairs.initStates();
        Chest.initStates();
        RedstoneWire.initStates();
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
        Cactus.initStates();
        SugarCane.initStates();
        Jukebox.initStates();
        OakFence.initStates();
        Basalt.initStates();
        PolishedBasalt.initStates();
        SoulWallTorch.initStates();
        NetherPortal.initStates();
        CarvedPumpkin.initStates();
        JackOLantern.initStates();
        Cake.initStates();
        Repeater.initStates();
        OakTrapdoor.initStates();
        SpruceTrapdoor.initStates();
        BirchTrapdoor.initStates();
        JungleTrapdoor.initStates();
        AcaciaTrapdoor.initStates();
        DarkOakTrapdoor.initStates();
        BrownMushroomBlock.initStates();
        RedMushroomBlock.initStates();
        MushroomStem.initStates();
        IronBars.initStates();
        Chain.initStates();
        GlassPane.initStates();
        AttachedPumpkinStem.initStates();
        AttachedMelonStem.initStates();
        PumpkinStem.initStates();
        MelonStem.initStates();
        Vine.initStates();
        OakFenceGate.initStates();
        BrickStairs.initStates();
        StoneBrickStairs.initStates();
        Mycelium.initStates();
        NetherBrickFence.initStates();
        NetherBrickStairs.initStates();
        NetherWart.initStates();
        BrewingStand.initStates();
        Cauldron.initStates();
        EndPortalFrame.initStates();
        RedstoneLamp.initStates();
        Cocoa.initStates();
        SandstoneStairs.initStates();
        EnderChest.initStates();
        TripwireHook.initStates();
        Tripwire.initStates();
        SpruceStairs.initStates();
        BirchStairs.initStates();
        JungleStairs.initStates();
        CommandBlock.initStates();
        CobblestoneWall.initStates();
        MossyCobblestoneWall.initStates();
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
        Hopper.initStates();
        QuartzPillar.initStates();
        QuartzStairs.initStates();
        ActivatorRail.initStates();
        Dropper.initStates();
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
        IronTrapdoor.initStates();
        PrismarineStairs.initStates();
        PrismarineBrickStairs.initStates();
        DarkPrismarineStairs.initStates();
        PrismarineSlab.initStates();
        PrismarineBrickSlab.initStates();
        DarkPrismarineSlab.initStates();
        HayBlock.initStates();
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
        PurpurPillar.initStates();
        PurpurStairs.initStates();
        Beetroots.initStates();
        RepeatingCommandBlock.initStates();
        ChainCommandBlock.initStates();
        FrostedIce.initStates();
        BoneBlock.initStates();
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
        Kelp.initStates();
        TurtleEgg.initStates();
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
        Conduit.initStates();
        Bamboo.initStates();
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
        Grindstone.initStates();
        Lectern.initStates();
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
        CrimsonStem.initStates();
        StrippedCrimsonStem.initStates();
        CrimsonHyphae.initStates();
        StrippedCrimsonHyphae.initStates();
        WeepingVines.initStates();
        TwistingVines.initStates();
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
        RespawnAnchor.initStates();
        BlackstoneStairs.initStates();
        BlackstoneWall.initStates();
        BlackstoneSlab.initStates();
        PolishedBlackstoneBrickSlab.initStates();
        PolishedBlackstoneBrickStairs.initStates();
        PolishedBlackstoneBrickWall.initStates();
        PolishedBlackstoneStairs.initStates();
        PolishedBlackstoneSlab.initStates();
        PolishedBlackstonePressurePlate.initStates();
        PolishedBlackstoneButton.initStates();
        PolishedBlackstoneWall.initStates();
    }

    @NotNull
    private final String namespaceID;

    private final short defaultID;

    private final double hardness;

    private final double resistance;

    private final boolean isAir;

    private final boolean isSolid;

    @Nullable
    private final NamespaceID blockEntity;

    private final boolean singleState;

    private List<BlockAlternative> alternatives = new java.util.ArrayList<>();

    private final Key key;

    Block(@NotNull String namespaceID, short defaultID, double hardness, double resistance,
            boolean isAir, boolean isSolid, @Nullable NamespaceID blockEntity,
            boolean singleState) {
        this.namespaceID = namespaceID;
        this.defaultID = defaultID;
        this.hardness = hardness;
        this.resistance = resistance;
        this.isAir = isAir;
        this.isSolid = isSolid;
        this.blockEntity = blockEntity;
        this.singleState = singleState;
        if(singleState) {
            addBlockAlternative(new BlockAlternative(defaultID));
        }
        Registries.blocks.put(NamespaceID.from(namespaceID), this);
        this.key = Key.key(this.namespaceID);
    }

    public short getBlockId() {
        return defaultID;
    }

    public String getName() {
        return namespaceID;
    }

    public boolean isAir() {
        return isAir;
    }

    public boolean hasBlockEntity() {
        return blockEntity != null;
    }

    public NamespaceID getBlockEntityName() {
        return blockEntity;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public boolean isLiquid() {
        return this == WATER || this == LAVA;
    }

    public double getHardness() {
        return hardness;
    }

    public double getResistance() {
        return resistance;
    }

    public boolean breaksInstantaneously() {
        return hardness == 0;
    }

    public void addBlockAlternative(BlockAlternative alternative) {
        alternatives.add(alternative);
        BlockArray.blocks[alternative.getId()] = this;
    }

    public BlockAlternative getAlternative(short blockId) {
        for(BlockAlternative alt : alternatives) {
            if(alt.getId() == blockId) {
                return alt;
            }
        }
        return null;
    }

    public List<BlockAlternative> getAlternatives() {
        return alternatives;
    }

    public short withProperties(String... properties) {
        for(BlockAlternative alt : alternatives) {
            if(Arrays.equals(alt.getProperties(), properties)) {
                return alt.getId();
            }
        }
        return defaultID;
    }

    public static Block fromStateId(short blockStateId) {
        return BlockArray.blocks[blockStateId];
    }

    public Key key() {
        return this.key;
    }
}
