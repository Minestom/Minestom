package net.minestom.server.item;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.SetCooldownPacket;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class MaterialImpl implements Material {
    private static final Registry.Container<Material> CONTAINER = new Registry.Container<>(Registry.Resource.ITEMS,
            (container, namespace, object) -> container.register(new MaterialImpl(Registry.material(namespace, object, null))));

    static Material get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    static Material getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }

    static Material getId(int id) {
        return CONTAINER.getId(id);
    }

    static Collection<Material> values() {
        return CONTAINER.values();
    }

    private final Registry.MaterialEntry registry;

    MaterialImpl(Registry.MaterialEntry registry) {
        this.registry = registry;
    }

    @Override
    public @NotNull Registry.MaterialEntry registry() {
        return registry;
    }

    @Override
    public void setCooldown(Player player, int ticks, boolean lagCompensation) {
        final SetCooldownPacket packet = new SetCooldownPacket();
        packet.cooldownTicks = ticks;
        packet.itemId = this.id();

        player.getPlayerConnection().sendPacket(packet);

        if (!lagCompensation) return;

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            final SetCooldownPacket lagCompensatePacket = new SetCooldownPacket();
            lagCompensatePacket.cooldownTicks = 0;
            lagCompensatePacket.itemId = this.id();

            player.getPlayerConnection().sendPacket(lagCompensatePacket);
        }).delay(ticks, TimeUnit.CLIENT_TICK).schedule();
    }

    @Override
    public String toString() {
        return name();
    }
}
