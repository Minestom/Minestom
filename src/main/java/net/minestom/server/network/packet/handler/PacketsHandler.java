package net.minestom.server.network.packet.handler;

import net.minestom.server.network.packet.Packet;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Contains registered packets and a way to instantiate them.
 * <p>
 * Packets are register using {@link #register(int, Supplier<PacketType>)}
 * (you can override a packet id even if not recommended and not officially supported) and retrieved with {@link #getPacketInstance(int)}.
 * <p>
 * If you want to fill the packet from a buffer, consider using {@link Packet#read(BinaryReader)} after getting the packet instance.
 */
public class PacketsHandler<PacketType extends Packet> {

    private final Supplier<PacketType>[] supplierAccesses;
    private final int maxSize;

    public PacketsHandler(int size) {
        this.maxSize = size;
        supplierAccesses = new Supplier[size];
    }

    /**
     * Registers a client packet which can be retrieved later using {@link #getPacketInstance(int)}.
     *
     * @param id             the packet id
     * @param packetSupplier the supplier of the packet
     */
    public void register(int id, @NotNull Supplier<PacketType> packetSupplier) {
        this.supplierAccesses[id] = packetSupplier;
    }

    /**
     * Retrieves a {@link net.minestom.server.network.packet.client.ClientPlayPacket} from its id.
     *
     * @param id the packet id
     * @return the associated client packet
     * @throws IllegalStateException if {@code id} is not a valid packet id, or unregistered
     */
    public PacketType getPacketInstance(int id) {
        if (id > maxSize)
            throw new IllegalStateException("Packet ID 0x" + Integer.toHexString(id) + " is higher than maximum recognized for this handler (0x" + Integer.toHexString(maxSize) + "), debug needed");

        Supplier<PacketType> supplier = supplierAccesses[id];
        if (supplierAccesses[id] == null)
            throw new IllegalStateException("Packet id 0x" + Integer.toHexString(id) + " isn't registered!");

        //PacketType packet = supplier.get();
        //System.out.println("RECEIVED PACKET 0x" + Integer.toHexString(id)+" : "+packet.getClass().getSimpleName());
        return supplier.get();
    }

}
