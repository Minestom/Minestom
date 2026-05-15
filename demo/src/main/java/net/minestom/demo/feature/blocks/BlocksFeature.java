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

/** Placement rules, handlers, bed/door/waterlog mechanics, sign editing. */
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

        process.eventHandler().addListener(PlayerBlockBreakEvent.class, event -> {
            Point other = bedTwin(event.getBlockPosition(), event.getBlock());
            if (other == null) return;
            var instance = event.getInstance();
            if (instance.getBlock(other).id() == event.getBlock().id()) {
                instance.setBlock(other, Block.AIR);
            }
        });

        process.eventHandler().addListener(PlayerBlockInteractEvent.class, event -> {
            if (!event.getBlock().key().asMinimalString().endsWith("_bed")) return;
            Point other = bedTwin(event.getBlockPosition(), event.getBlock());
            if (other == null) return;
            var instance = event.getInstance();
            if (instance.getBlock(other).id() != event.getBlock().id()) return;
            var player = event.getPlayer();
            player.setVelocity(Vec.ZERO);
            player.swingMainHand();
            boolean isHead = "head".equals(event.getBlock().getProperty("part"));
            player.enterBed(isHead ? event.getBlockPosition() : other);
        });

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

        process.eventHandler().addListener(PlayerBlockPlaceEvent.class, event -> {
            Block block = event.getBlock();
            if (block.handler() != null) return;
            BlockHandler handler = blockManager.getHandler(block.key().asString());
            if (handler != null) event.setBlock(block.withHandler(handler));
        });

        process.eventHandler().addListener(PlayerEditSignEvent.class, event -> event.getLines()
                .stream()
                .map(Component::text)
                .forEach(event.getPlayer()::sendMessage));
    }

    /** Position of the other half of a bed, or {@code null} if not a bed. */
    private static @Nullable Point bedTwin(Point pos, Block bed) {
        String part = bed.getProperty("part");
        String facing = bed.getProperty("facing");
        if (part == null || facing == null) return null;
        BlockFace face = BlockFace.valueOf(facing.toUpperCase());
        BlockFace toOther = "head".equals(part) ? face.getOppositeFace() : face;
        return pos.add(toOther.toDirection().vec().asPos());
    }
}
