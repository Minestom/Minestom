package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record UnlockRecipesPacket(int mode,
                                  boolean craftingRecipeBookOpen, boolean craftingRecipeBookFilterActive,
                                  boolean smeltingRecipeBookOpen, boolean smeltingRecipeBookFilterActive,
                                  boolean blastFurnaceRecipeBookOpen, boolean blastFurnaceRecipeBookFilterActive,
                                  boolean smokerRecipeBookOpen, boolean smokerRecipeBookFilterActive,
                                  @NotNull List<String> recipeIds,
                                  @UnknownNullability List<String> initRecipeIds) implements ServerPacket.Play {
    public UnlockRecipesPacket {
        recipeIds = List.copyOf(recipeIds);
        if (initRecipeIds != null) {
            initRecipeIds = List.copyOf(initRecipeIds);
        }
    }

    public UnlockRecipesPacket(@NotNull NetworkBuffer reader) {
        this(read(reader));
    }

    private UnlockRecipesPacket(UnlockRecipesPacket packet) {
        this(packet.mode,
                packet.craftingRecipeBookOpen, packet.craftingRecipeBookFilterActive,
                packet.smeltingRecipeBookOpen, packet.smeltingRecipeBookFilterActive,
                packet.blastFurnaceRecipeBookOpen, packet.blastFurnaceRecipeBookFilterActive,
                packet.smokerRecipeBookOpen, packet.smokerRecipeBookFilterActive,
                packet.recipeIds, packet.initRecipeIds);
    }

    private static UnlockRecipesPacket read(@NotNull NetworkBuffer reader) {
        var mode = reader.read(VAR_INT);
        var craftingRecipeBookOpen = reader.read(BOOLEAN);
        var craftingRecipeBookFilterActive = reader.read(BOOLEAN);
        var smeltingRecipeBookOpen = reader.read(BOOLEAN);
        var smeltingRecipeBookFilterActive = reader.read(BOOLEAN);
        var blastFurnaceRecipeBookOpen = reader.read(BOOLEAN);
        var blastFurnaceRecipeBookFilterActive = reader.read(BOOLEAN);
        var smokerRecipeBookOpen = reader.read(BOOLEAN);
        var smokerRecipeBookFilterActive = reader.read(BOOLEAN);
        var recipeIds = reader.readCollection(STRING, DeclareRecipesPacket.MAX_RECIPES);
        var initRecipeIds = mode == 0 ? reader.readCollection(STRING, DeclareRecipesPacket.MAX_RECIPES) : null;
        return new UnlockRecipesPacket(mode,
                craftingRecipeBookOpen, craftingRecipeBookFilterActive,
                smeltingRecipeBookOpen, smeltingRecipeBookFilterActive,
                blastFurnaceRecipeBookOpen, blastFurnaceRecipeBookFilterActive,
                smokerRecipeBookOpen, smokerRecipeBookFilterActive,
                recipeIds, initRecipeIds);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, mode);
        writer.write(BOOLEAN, craftingRecipeBookOpen);
        writer.write(BOOLEAN, craftingRecipeBookFilterActive);
        writer.write(BOOLEAN, smeltingRecipeBookOpen);
        writer.write(BOOLEAN, smeltingRecipeBookFilterActive);
        writer.write(BOOLEAN, blastFurnaceRecipeBookOpen);
        writer.write(BOOLEAN, blastFurnaceRecipeBookFilterActive);
        writer.write(BOOLEAN, smokerRecipeBookOpen);
        writer.write(BOOLEAN, smokerRecipeBookFilterActive);

        writer.writeCollection(STRING, recipeIds);
        if (mode == 0) {
            writer.writeCollection(STRING, initRecipeIds);
        }
    }

}
