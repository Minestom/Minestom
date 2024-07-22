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

    public static final NetworkBuffer.Type<UnlockRecipesPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, UnlockRecipesPacket value) {
            writer.write(VAR_INT, value.mode);
            writer.write(BOOLEAN, value.craftingRecipeBookOpen);
            writer.write(BOOLEAN, value.craftingRecipeBookFilterActive);
            writer.write(BOOLEAN, value.smeltingRecipeBookOpen);
            writer.write(BOOLEAN, value.smeltingRecipeBookFilterActive);
            writer.write(BOOLEAN, value.blastFurnaceRecipeBookOpen);
            writer.write(BOOLEAN, value.blastFurnaceRecipeBookFilterActive);
            writer.write(BOOLEAN, value.smokerRecipeBookOpen);
            writer.write(BOOLEAN, value.smokerRecipeBookFilterActive);

            writer.writeCollection(STRING, value.recipeIds);
            if (value.mode == 0) {
                writer.writeCollection(STRING, value.initRecipeIds);
            }
        }

        @Override
        public UnlockRecipesPacket read(@NotNull NetworkBuffer reader) {
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
    };
}
