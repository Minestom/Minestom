package fr.themode.demo;

import fr.themode.demo.blocks.StoneBlock;
import fr.themode.demo.blocks.UpdatableBlockDemo;
import fr.themode.demo.commands.HealthCommand;
import fr.themode.demo.commands.SimpleCommand;
import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.command.CommandManager;
import fr.themode.minestom.instance.block.BlockManager;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.item.Material;
import fr.themode.minestom.net.packet.server.play.DeclareRecipesPacket;
import fr.themode.minestom.recipe.RecipeManager;
import fr.themode.minestom.recipe.ShapelessRecipe;


public class Main {

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerCustomBlock(new StoneBlock());
        blockManager.registerCustomBlock(new UpdatableBlockDemo());

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new HealthCommand());
        commandManager.register(new SimpleCommand());

        RecipeManager recipeManager = MinecraftServer.getRecipeManager();
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe("test", "groupname");
        shapelessRecipe.setResult(new ItemStack(Material.STONE, (byte) 1));
        DeclareRecipesPacket.Ingredient ingredient = new DeclareRecipesPacket.Ingredient();
        ingredient.items = new ItemStack[]{new ItemStack(Material.STONE, (byte) 3)};
        shapelessRecipe.addIngredient(ingredient);
        recipeManager.addRecipe(shapelessRecipe);


        PlayerInit.init();

        minecraftServer.start("localhost", 55555);
    }

}
