module net.minestom.testing {
    requires transitive net.minestom.server;
    requires org.junit.jupiter.api; // Users can bring their own version.

    exports net.minestom.testing;
    exports net.minestom.testing.util;
}