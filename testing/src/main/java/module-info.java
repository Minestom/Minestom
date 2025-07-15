import org.jspecify.annotations.NullMarked;

@NullMarked
module net.minestom.testing {
    requires static org.jspecify;
    requires static org.jetbrains.annotations; // TODO Remove when JSpecify is mature
    requires net.minestom.server;
    requires net.kyori.adventure;
    requires net.kyori.adventure.nbt;
    requires org.junit.jupiter.api;

    exports net.minestom.testing;
    exports net.minestom.testing.util;
}