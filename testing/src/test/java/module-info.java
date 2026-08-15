module net.minestom.testing.test {
    requires org.junit.jupiter.api;

    requires net.minestom.testing;

    opens net.minestom.testing.test to org.junit.platform.commons;
}