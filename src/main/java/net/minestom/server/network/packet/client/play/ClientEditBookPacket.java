package net.minestom.server.network.packet.client.play;

import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

public class ClientEditBookPacket extends ClientPlayPacket {

    public ItemStack book;
    public boolean isSigning;
    public Player.Hand hand;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.book = reader.readItemStack();
        this.isSigning = reader.readBoolean();
        this.hand = Player.Hand.values()[reader.readVarInt()];
    }
}
