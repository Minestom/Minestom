package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public record UnlockRecipesPacket(int mode,
                                  boolean craftingRecipeBookOpen, boolean craftingRecipeBookFilterActive,
                                  boolean smeltingRecipeBookOpen, boolean smeltingRecipeBookFilterActive,
                                  boolean blastFurnaceRecipeBookOpen, boolean blastFurnaceRecipeBookFilterActive,
                                  boolean smokerRecipeBookOpen, boolean smokerRecipeBookFilterActive,
                                  @NotNull List<String> recipeIds,
                                  @UnknownNullability List<String> initRecipeIds) implements ServerPacket {
    public UnlockRecipesPacket {
        recipeIds = List.copyOf(recipeIds);
        if (initRecipeIds != null) {
            initRecipeIds = List.copyOf(initRecipeIds);
        }
    }

    public UnlockRecipesPacket(BinaryReader reader) {
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

    private static UnlockRecipesPacket read(BinaryReader reader) {
        var mode = reader.readVarInt();
        var craftingRecipeBookOpen = reader.readBoolean();
        var craftingRecipeBookFilterActive = reader.readBoolean();
        var smeltingRecipeBookOpen = reader.readBoolean();
        var smeltingRecipeBookFilterActive = reader.readBoolean();
        var blastFurnaceRecipeBookOpen = reader.readBoolean();
        var blastFurnaceRecipeBookFilterActive = reader.readBoolean();
        var smokerRecipeBookOpen = reader.readBoolean();
        var smokerRecipeBookFilterActive = reader.readBoolean();
        var recipeIds = reader.readVarIntList(BinaryReader::readSizedString);
        var initRecipeIds = mode == 0 ? reader.readVarIntList(BinaryReader::readSizedString) : null;
        return new UnlockRecipesPacket(mode,
                craftingRecipeBookOpen, craftingRecipeBookFilterActive,
                smeltingRecipeBookOpen, smeltingRecipeBookFilterActive,
                blastFurnaceRecipeBookOpen, blastFurnaceRecipeBookFilterActive,
                smokerRecipeBookOpen, smokerRecipeBookFilterActive,
                recipeIds, initRecipeIds);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(mode);
        writer.writeBoolean(craftingRecipeBookOpen);
        writer.writeBoolean(craftingRecipeBookFilterActive);
        writer.writeBoolean(smeltingRecipeBookOpen);
        writer.writeBoolean(smeltingRecipeBookFilterActive);
        writer.writeBoolean(blastFurnaceRecipeBookOpen);
        writer.writeBoolean(blastFurnaceRecipeBookFilterActive);
        writer.writeBoolean(smokerRecipeBookOpen);
        writer.writeBoolean(smokerRecipeBookFilterActive);

        writer.writeVarIntList(recipeIds, BinaryWriter::writeSizedString);
        if (mode == 0) {
            writer.writeVarIntList(initRecipeIds, BinaryWriter::writeSizedString);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UNLOCK_RECIPES;
    }
}
