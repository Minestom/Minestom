package net.minestom.demo.feature.blocks;

import net.kyori.adventure.text.Component;
import net.minestom.demo.core.Feature;
import net.minestom.demo.feature.blocks.handlers.SignHandler;
import net.minestom.demo.feature.blocks.handlers.TestBlockHandler;
import net.minestom.demo.feature.blocks.placement.BedPlacementRule;
import net.minestom.demo.feature.blocks.placement.DripstonePlacementRule;
import net.minestom.server.ServerProcess;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.*;
import net.minestom.server.instance.block.*;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Block-system showcase:
 * <ul>
 *   <li>Placement rules: dripstone direction, bed pairing (one per bed colour).</li>
 *   <li>Handlers: a generic {@link TestBlockHandler}, and {@link SignHandler}
 *       registered for every block in the {@code #minecraft:all_signs} tag.</li>
 *   <li>Commands: {@code /setblock}, {@code /relight}, {@code /debuggrid}.</li>
 *   <li>Bed double-block: break both halves; enter-bed on interact.</li>
 *   <li>Waterlogging: filling/emptying a waterlogged block with a bucket.</li>
 *   <li>Open property toggle (doors, trapdoors, fence gates, ...).</li>
 *   <li>Crafting table interact opens a chest-1-row inventory titled "Crafting".</li>
 *   <li>On block place: auto-attach the registered handler for that block key.</li>
 *   <li>Sign edits are echoed back to the placing player.</li>
 * </ul>
 */
public final class BlocksFeature implements Feature {

    @Override
    public void register(ServerProcess process) {
        BlockManager blockManager = process.block();
        blockManager.registerBlockPlacementRule(new DripstonePlacementRule());
        Block.values().stream()
                .filter(block -> BlockEntityType.BED.equals(block.registry().blockEntityType()))
                .forEach(block -> blockManager.registerBlockPlacementRule(new BedPlacementRule(block)));
        blockManager.registerHandler(TestBlockHandler.INSTANCE.getKey(), () -> TestBlockHandler.INSTANCE);

        RegistryTag<Block> signs = Block.staticRegistry().getTag(TagKey.ofHash("#minecraft:all_signs"));
        SignHandler signHandler = new SignHandler();
        for (RegistryKey<Block> key : Objects.requireNonNull(signs)) {
            blockManager.registerHandler(key.key(), () -> signHandler);
        }

        process.command().register(
                new SetBlockCommand(),
                new RelightCommand(),
                new DebugGridCommand()
        );

        // Bed: break both halves
        process.eventHandler().addListener(PlayerBlockBreakEvent.class, event -> {
            Point other = bedTwin(event.getBlockPosition(), event.getBlock());
            if (other == null) return;
            var instance = event.getInstance();
            if (instance.getBlock(other).id() == event.getBlock().id()) {
                instance.setBlock(other, Block.AIR);
            }
        });

        // Bed: enter on interact (works on either half)
        process.eventHandler().addListener(PlayerBlockInteractEvent.class, event -> {
            if (!event.getBlock().key().asMinimalString().endsWith("_bed")) return;
            Point other = bedTwin(event.getBlockPosition(), event.getBlock());
            if (other == null) return;
            var instance = event.getInstance();
            if (instance.getBlock(other).id() != event.getBlock().id()) return;
            var player = event.getPlayer();
            player.setVelocity(Vec.ZERO);
            player.swingMainHand();
            // Always sleep on the head half
            boolean isHead = "head".equals(event.getBlock().getProperty("part"));
            player.enterBed(isHead ? event.getBlockPosition() : other);
        });

        // Waterlogging via bucket / water bucket
        process.eventHandler().addListener(PlayerUseItemOnBlockEvent.class, event -> {
            if (event.getHand() != PlayerHand.MAIN) return;
            var material = event.getItemStack().material();
            var block = event.getInstance().getBlock(event.getPosition());
            String waterlogged = block.getProperty("waterlogged");
            if ("false".equals(waterlogged) && material.equals(Material.WATER_BUCKET)) {
                event.getInstance().setBlock(event.getPosition(), block.withProperty("waterlogged", "true"));
            } else if ("true".equals(waterlogged) && material.equals(Material.BUCKET)) {
                event.getInstance().setBlock(event.getPosition(), block.withProperty("waterlogged", "false"));
            }
        });

        // Toggle open property + crafting-table inventory
        process.eventHandler().addListener(PlayerBlockInteractEvent.class, event -> {
            var block = event.getBlock();
            String openProp = block.getProperty("open");
            if (openProp != null) {
                block = block.withProperty("open", String.valueOf(!Boolean.parseBoolean(openProp)));
                event.getInstance().setBlock(event.getBlockPosition(), block);
            }
            if (block.id() == Block.CRAFTING_TABLE.id()) {
                event.getPlayer().openInventory(new Inventory(InventoryType.CRAFTING, "Crafting"));
            }
        });

        // Auto-attach a registered handler when its block gets placed
        process.eventHandler().addListener(PlayerBlockPlaceEvent.class, event -> {
            Block block = event.getBlock();
            BlockHandler existing = block.handler();
            if (existing != null) return;
            BlockHandler handler = blockManager.getHandler(block.key().asString());
            if (handler != null) event.setBlock(block.withHandler(handler));
        });

        // Echo sign edits back to the editor
        process.eventHandler().addListener(PlayerEditSignEvent.class, event -> event.getLines()
                .stream()
                .map(Component::text)
                .forEach(event.getPlayer()::sendMessage));
    }

    /**
     * For a bed block at {@code pos}, returns the position of its other
     * half — or {@code null} if the block is not a properly-oriented bed.
     */
    private static @Nullable Point bedTwin(Point pos, Block bed) {
        String part = bed.getProperty("part");
        String facing = bed.getProperty("facing");
        if (part == null || facing == null) return null;
        BlockFace face = BlockFace.valueOf(facing.toUpperCase());
        BlockFace toOther = "head".equals(part) ? face.getOppositeFace() : face;
        return pos.add(toOther.toDirection().vec().asPos());
    }
}
