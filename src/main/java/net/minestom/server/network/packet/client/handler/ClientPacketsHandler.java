package net.minestom.server.network.packet.client.handler;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Contains registered packets and a way to instantiate them.
 * <p>
 * Packets are register using {@link #register(int, ClientPacketSupplier)}
 * (you can override a packet id even if not recommended and not officially supported) and retrieved with {@link #getPacketInstance(int)}.
 * <p>
 * If you want to fill the packet from a buffer, consider using {@link ClientPacket#read(BinaryReader)} after getting the packet instance.
 */
public class ClientPacketsHandler {

    // Max packet id
    private static final int SIZE = 0x30;

    private final ClientPacketSupplier[] supplierAccesses = new ClientPacketSupplier[SIZE];

    /**
     * Registers a client packet which can be retrieved later using {@link #getPacketInstance(int)}.
     *
     * @param id             the packet id
     * @param packetSupplier the supplier of the packet
     */
    public void register(int id, @NotNull ClientPacketSupplier packetSupplier) {
        this.supplierAccesses[id] = packetSupplier;
    }

    /**
     * Retrieves a {@link net.minestom.server.network.packet.client.ClientPlayPacket} from its id.
     *
     * @param id the packet id
     * @return the associated client packet
     * @throws IllegalStateException if {@code id} is not a valid packet id, or unregistered
     */
    public ClientPacket getPacketInstance(int id) {
        if (id > SIZE)
            throw new IllegalStateException("Packet ID 0x" + Integer.toHexString(id) + " has been tried to be parsed, debug needed");

        ClientPacketSupplier supplier = supplierAccesses[id];
        if (supplierAccesses[id] == null)
            throw new IllegalStateException("Packet id 0x" + Integer.toHexString(id) + " isn't registered!");

        //ClientPacket packet = supplier.get();
        //System.out.println("RECEIVED PACKET 0x" + Integer.toHexString(id)+" : "+packet.getClass().getSimpleName());
        return supplier.get();
    }

    /**
     * Convenient interface to supply a {@link ClientPacket}.
     */
    protected interface ClientPacketSupplier extends Supplier<ClientPacket> {
    }

}
