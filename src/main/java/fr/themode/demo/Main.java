package fr.themode.demo;

import fr.themode.demo.blocks.BurningTorchBlock;
import fr.themode.demo.blocks.StoneBlock;
import fr.themode.demo.blocks.UpdatableBlockDemo;
import fr.themode.demo.commands.DimensionCommand;
import fr.themode.demo.commands.GamemodeCommand;
import fr.themode.demo.commands.HealthCommand;
import fr.themode.demo.commands.SimpleCommand;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.storage.systems.FileStorageSystem;
import net.minestom.server.timer.TaskRunnable;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;


public class Main {

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerCustomBlock(new StoneBlock());
        blockManager.registerCustomBlock(new UpdatableBlockDemo());
        blockManager.registerCustomBlock(new BurningTorchBlock());

        blockManager.registerBlockPlacementRule(new RedstonePlacementRule());

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new HealthCommand());
        commandManager.register(new SimpleCommand());
        commandManager.register(new GamemodeCommand());
        commandManager.register(new DimensionCommand());

        /*RecipeManager recipeManager = MinecraftServer.getRecipeManager();
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe("test", "groupname") {
            @Override
            public boolean shouldShow(Player player) {
                return true;
            }
        };
        shapelessRecipe.setResult(new ItemStack(Material.STONE, (byte) 1));
        DeclareRecipesPacket.Ingredient ingredient = new DeclareRecipesPacket.Ingredient();
        ingredient.items = new ItemStack[]{new ItemStack(Material.STONE, (byte) 3)};
        shapelessRecipe.addIngredient(ingredient);
        recipeManager.addRecipe(shapelessRecipe);*/

        StorageManager storageManager = MinecraftServer.getStorageManager();
        storageManager.defineDefaultStorageSystem(FileStorageSystem::new);

        MinecraftServer.getBenchmarkManager().enable(new UpdateOption(10 * 1000, TimeUnit.MILLISECOND));

        MinecraftServer.getSchedulerManager().addShutdownTask(new TaskRunnable() {
            @Override
            public void run() {
                System.out.println("Good night");
            }
        });

        PlayerInit.init();

        MojangAuth.init();

        minecraftServer.start("localhost", 25565, PlayerInit.getResponseDataConsumer());
    }

}
