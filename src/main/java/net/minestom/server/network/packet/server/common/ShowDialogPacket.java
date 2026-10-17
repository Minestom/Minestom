package net.minestom.server.network.packet.server.common;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.registry.Holder;

import java.util.Collection;
import java.util.Objects;
import java.util.function.UnaryOperator;

public record ShowDialogPacket(
        Holder<Dialog> dialog
) implements ServerPacket.Configuration, ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<ShowDialogPacket> SERIALIZER = NetworkBufferTemplate.template(
            Dialog.NETWORK_TYPE, ShowDialogPacket::dialog,
            ShowDialogPacket::new);

    public static final NetworkBuffer.Type<ShowDialogPacket> INLINE_SERIALIZER = NetworkBufferTemplate.template(
            Dialog.REGISTRY_NETWORK_TYPE, (dialog) -> Objects.requireNonNull(dialog.dialog().asValue(), "Dialog holder must be direct during inline serialization"),
            ShowDialogPacket::new
    );

    @Override
    public Collection<? extends Component> components() {
        var usableDialog = dialog.unwrap().unify(
                registryKey -> MinecraftServer.getDialogRegistry().get(registryKey),
                dialog1 -> dialog1
        );
        return usableDialog.components();
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        var usableDialog = dialog.unwrap().unify(
                registryKey -> MinecraftServer.getDialogRegistry().get(registryKey),
                dialog1 -> dialog1
        );
        return new ShowDialogPacket(usableDialog.copyWithOperator(operator));
    }
}
