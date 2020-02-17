package fr.themode.demo;

import fr.themode.demo.blocks.StoneBlock;
import fr.themode.demo.blocks.UpdatableBlockDemo;
import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.instance.block.BlockManager;

public class Main {

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        PlayerInit.init();

        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerBlock(new StoneBlock());
        blockManager.registerBlock(new UpdatableBlockDemo());

        minecraftServer.start("localhost", 55555);
    }

}
