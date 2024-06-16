/**
 * This package contains extensions for parameter resolution and invocation interception in tests.
 * <p>
 * The main class in this package is {@link net.minestom.testing.extension.MicrotusExtension}, which provides
 * parameter resolution for {@link net.minestom.testing.Env} type parameters and intercepts test method invocations.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>
 * &#64;ExtendWith(MicrotusExtension.class)
 * public class MyTest {
 *     &#64;Test
 *     public void testWithEnv(Env env) {
 *         // test code using env
 *     }
 * }
 * </pre>
 *
 * @since 1.5.0
 */
package net.minestom.testing.extension;