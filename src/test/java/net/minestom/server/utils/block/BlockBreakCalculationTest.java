package net.minestom.server.utils.block;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
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
        if (instantBreakSpeed > Attribute.PLAYER_BLOCK_BREAK_SPEED.maxValue()) return;
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
        player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(Attribute.PLAYER_BLOCK_BREAK_SPEED.defaultValue());
    }

    private void resetBreakEfficiency() {
        player.getAttribute(Attribute.PLAYER_MINING_EFFICIENCY).setBaseValue(Attribute.PLAYER_MINING_EFFICIENCY.defaultValue());
    }

    private void updateBreakSpeed(double speed) {
        player.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED).setBaseValue(speed);
    }

    private void updateEfficiency(double efficiency) {
        player.getAttribute(Attribute.PLAYER_MINING_EFFICIENCY).setBaseValue(efficiency);
    }
}
