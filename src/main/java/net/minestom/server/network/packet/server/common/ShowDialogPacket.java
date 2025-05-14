package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public record ShowDialogPacket(@NotNull BinaryTag inline) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<ShowDialogPacket> PLAY_SERIALIZER = new NetworkBuffer.Type<ShowDialogPacket>() {
        // TODO(1.21.6) In reality this is a Holder<Dialog> but we dont have types for dialogs or proper holder support yet.

        @Override
        public void write(@NotNull NetworkBuffer buffer, ShowDialogPacket value) {
            buffer.write(NetworkBuffer.VAR_INT, 0);
            buffer.write(NetworkBuffer.NBT, value.inline);
        }

        @Override
        public ShowDialogPacket read(@NotNull NetworkBuffer buffer) {
            Check.stateCondition(buffer.read(NetworkBuffer.VAR_INT) == 0, "cannot read dialog reference");
            return new ShowDialogPacket(buffer.read(NetworkBuffer.NBT));
        }
    };
    public static final NetworkBuffer.Type<ShowDialogPacket> CONFIG_SERIALIZER = PLAY_SERIALIZER;

}
