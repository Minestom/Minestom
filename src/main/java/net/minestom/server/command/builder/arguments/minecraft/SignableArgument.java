package net.minestom.server.command.builder.arguments.minecraft;

/**
 * Sealed because argument signing is entirely dependent on the argument type, and it's decided by the client
 */
public sealed interface SignableArgument permits ArgumentMessage {
}
