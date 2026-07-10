package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.client.play.ClientPlaceRecipePacket;
import net.minestom.server.network.packet.server.play.PlaceGhostRecipePacket;
import net.minestom.server.recipe.RecipeManager;
import net.minestom.server.recipe.display.RecipeDisplay;

public class RecipeListener {

    public static void listener(ClientPlaceRecipePacket packet, Player player) {
        final RecipeManager recipeManager = MinecraftServer.getRecipeManager();
        final RecipeDisplay recipeDisplay = recipeManager.getRecipeDisplay(packet.recipeDisplayId(), player);
        if (recipeDisplay == null) return;

        player.sendPacket(new PlaceGhostRecipePacket(packet.windowId(), recipeDisplay));
    }
}
