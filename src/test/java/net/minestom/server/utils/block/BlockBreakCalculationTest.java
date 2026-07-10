package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.minestom.server.utils.block.BlockBreakCalculation.breakTicks;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnvTest
public class BlockBreakCalculationTest {
    private Player player;
    private Runnable assertInstabreak;
    private Runnable assertNotQuiteInstabreak;

    @Test
    public void testWool() {
        player.setItemInMainHand(ItemStack.AIR);
        assertInstabreak = this::assertWoolInstabreak;
        assertNotQuiteInstabreak = this::assertWoolNotQuiteInstabreak;
        assertBreak(24, -1, -1, -1, -1);
    }

    @Test
    public void testWoolWithShears() {
        player.setItemInMainHand(ItemStack.of(Material.SHEARS));
        assertInstabreak = this::assertWoolInstabreak;
        assertNotQuiteInstabreak = this::assertWoolNotQuiteInstabreak;
        assertBreak(4.8, 19, 115, 115, 595);
    }

    @Test
    public void testStone() {
        player.setItemInMainHand(ItemStack.AIR);
        assertInstabreak = this::assertStoneInstabreak;
        assertNotQuiteInstabreak = this::assertStoneNotQuiteInstabreak;
        assertBreak(150, -1, -1, -1, -1);
    }

    @Test
    public void testStoneWithDiamondPickaxe() {
        player.setItemInMainHand(ItemStack.of(Material.DIAMOND_PICKAXE));
        assertInstabreak = this::assertStoneInstabreak;
        assertNotQuiteInstabreak = this::assertStoneNotQuiteInstabreak;
        assertBreak(5.625, 37, 217, 217, -1);
    }

    @Test
    public void testBedrock() {
        assertEquals(BlockBreakCalculation.UNBREAKABLE, breakTicks(Block.BEDROCK, player));
    }

    @Test
    public void testZeroHardnessBlock() {
        assertEquals(0, breakTicks(Block.SCAFFOLDING, player));
        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(0);
        assertEquals(BlockBreakCalculation.UNBREAKABLE, breakTicks(Block.SCAFFOLDING, player));
    }

    @Test
    public void testMiningFatigue() {
        player.setItemInMainHand(ItemStack.AIR);
        final int noFatigue = breakTicks(Block.STONE, player);

        final int amplifier0 = breakWithFatigue(0); // x0.3
        final int amplifier1 = breakWithFatigue(1); // x0.09
        final int amplifier2 = breakWithFatigue(2); // x0.0027
        final int amplifier3 = breakWithFatigue(3); // x0.00081 (default case)

        // Mining Fatigue always slows breaking, and stronger levels are progressively slower.
        assertTrue(noFatigue < amplifier0, "fatigue must slow breaking");
        assertTrue(amplifier0 < amplifier1 && amplifier1 < amplifier2 && amplifier2 < amplifier3,
                "a higher amplifier must break slower");

        // Every amplifier >= 3 must fall through to the same default multiplier (the "Fix default case" regression guard).
        assertEquals(amplifier3, breakWithFatigue(4), "amplifier 4 must reuse the default multiplier");
        assertEquals(amplifier3, breakWithFatigue(10), "amplifier 10 must reuse the default multiplier");
        assertEquals(amplifier3, breakWithFatigue(127), "amplifier 127 must reuse the default multiplier");

        // Break time scales as 1/multiplier, so consecutive ratios must match vanilla's hardcoded table
        // (0.3, 0.09, 0.0027, 0.00081 for amplifier 0/1/2/3+). This pins the magnitudes and would catch a
        // regression to a clean 0.3^level table (0.027 / 0.0081).
        assertRatio(0.3 / 0.09, amplifier1, amplifier0);
        assertRatio(0.09 / 0.0027, amplifier2, amplifier1);
        assertRatio(0.0027 / 0.00081, amplifier3, amplifier2);
    }

    private int breakWithFatigue(int amplifier) {
        player.removeEffect(PotionEffect.MINING_FATIGUE);
        player.addEffect(new Potion(PotionEffect.MINING_FATIGUE, amplifier, 200));
        return breakTicks(Block.STONE, player);
    }

    private static void assertRatio(double expected, int slower, int faster) {
        assertEquals(expected, (double) slower / faster, expected * 0.02,
                "break-time ratio should reflect the vanilla mining-fatigue multiplier table");
    }

    @BeforeEach
    void setupPlayer(Env env) {
        final var instance = env.createFlatInstance();
        player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.refreshOnGround(true);
    }

    private void assertBreak(double instantBreakSpeed, double efficiency, double efficiencyNotOnGround, double efficiencyInWater, double efficiencyNotOnGroundInWater) {
        assertBreakSpeed(instantBreakSpeed);
        assertBreakEfficiency(efficiency);
        player.refreshOnGround(false);
        assertBreakSpeed(instantBreakSpeed * 5);
        assertBreakEfficiency(efficiencyNotOnGround);
        submerge();
        player.refreshOnGround(true);
        assertBreakSpeed(instantBreakSpeed * 5);
        assertBreakEfficiency(efficiencyInWater);
        player.refreshOnGround(false);
        assertBreakSpeed(instantBreakSpeed * 5 * 5);
        assertBreakEfficiency(efficiencyNotOnGroundInWater);
    }

    private void assertBreakEfficiency(double instantBreakEfficiency) {
        if (instantBreakEfficiency == -1) return;
        resetBreakSpeed();
        updateEfficiency(instantBreakEfficiency);
        assertInstabreak.run();
        updateEfficiency(instantBreakEfficiency - 0.001);
        assertNotQuiteInstabreak.run();
    }

    private void assertBreakSpeed(double instantBreakSpeed) {
        if (instantBreakSpeed > Attribute.BLOCK_BREAK_SPEED.maxValue()) return;
        resetBreakEfficiency();
        updateBreakSpeed(instantBreakSpeed);
        assertInstabreak.run();
        updateBreakSpeed(instantBreakSpeed - 0.001);
        assertNotQuiteInstabreak.run();
    }

    private void assertWoolInstabreak() {
        assertEquals(0, breakTicks(Block.WHITE_WOOL, player));
        assertEquals(0, breakTicks(Block.BLACK_WOOL, player));
    }

    private void assertWoolNotQuiteInstabreak() {
        assertTrue(breakTicks(Block.WHITE_WOOL, player) > 0);
        assertTrue(breakTicks(Block.BLACK_WOOL, player) > 0);
    }

    private void assertStoneInstabreak() {
        assertEquals(0, breakTicks(Block.STONE, player));
    }

    private void assertStoneNotQuiteInstabreak() {
        assertTrue(breakTicks(Block.STONE, player) > 0);
    }

    private void submerge() {
        player.getInstance().setBlock(player.getPosition().add(0, player.getEyeHeight(), 0), Block.WATER);
    }

    private void resetBreakSpeed() {
        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(Attribute.BLOCK_BREAK_SPEED.defaultValue());
    }

    private void resetBreakEfficiency() {
        player.getAttribute(Attribute.MINING_EFFICIENCY).setBaseValue(Attribute.MINING_EFFICIENCY.defaultValue());
    }

    private void updateBreakSpeed(double speed) {
        player.getAttribute(Attribute.BLOCK_BREAK_SPEED).setBaseValue(speed);
    }

    private void updateEfficiency(double efficiency) {
        player.getAttribute(Attribute.MINING_EFFICIENCY).setBaseValue(efficiency);
    }
}
