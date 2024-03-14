package net.minestom.server.utils.mojang;

public class ServiceNotAvailableException extends Exception {

    public ServiceNotAvailableException(String errorMessage) {
        super(errorMessage);
    }
}
