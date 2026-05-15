/**
 * One file per server launcher. Each class has a {@code main} and wires up
 * a small, focused set of {@link net.minestom.demo.core.Feature features}
 * via {@link net.minestom.demo.core.DemoServer DemoServer}.
 * <p>
 * Use {@link net.minestom.demo.server.AllInOneServer AllInOneServer} for
 * parity with the original demo, or any of the focused launchers
 * ({@code ChatServer}, {@code BlocksServer}, {@code EntitiesServer}, …)
 * to minimise the surface area when reproducing a specific issue.
 */
package net.minestom.demo.server;
