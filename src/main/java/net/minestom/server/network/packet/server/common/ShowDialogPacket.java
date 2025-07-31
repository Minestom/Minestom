package net.minestom.server.network.packet.server.common;

import net.minestom.server.dialog.Dialog;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.registry.Holder;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public record ShowDialogPacket(
        @NotNull Holder<Dialog> dialog
) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<ShowDialogPacket> SERIALIZER = NetworkBufferTemplate.template(
            Dialog.NETWORK_TYPE, ShowDialogPacket::dialog,
            ShowDialogPacket::new);

    public static final NetworkBuffer.Type<ShowDialogPacket> INLINE_SERIALIZER = NetworkBufferTemplate.template(
            Dialog.REGISTRY_NETWORK_TYPE, (dialog) -> Objects.requireNonNull(dialog.dialog().asValue(), "Dialog holder must be direct during inline serialization"),
            ShowDialogPacket::new
    );
}
