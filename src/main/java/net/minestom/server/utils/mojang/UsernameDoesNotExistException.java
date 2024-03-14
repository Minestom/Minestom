package net.minestom.server.utils.mojang;

public class UsernameDoesNotExistException extends Exception {

    public UsernameDoesNotExistException(String errorMessage) {
        super(errorMessage);
    }
}

