package net.minestom.server.adventure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents something which can have a locale.
 */
public interface Localizable {

    /**
     * Gets a localizable that returns {@code null} for all calls to {@link #getLocale()}.
     *
     * @return the empty localizable
     */
    static @NotNull Localizable empty() {
        return MinestomAdventure.NULL_LOCALIZABLE;
    }

    /**
     * Gets the locale.
     *
     * @return the locale, or {@code null} if they do not have a locale set
     */
    @Nullable Locale getLocale();

    /**
     * Sets the locale. This can be set to {@code null} to remove a locale registration.
     *
     * @param locale the new locale
     */
    default void setLocale(@Nullable Locale locale) {
        throw new UnsupportedOperationException("You cannot set the locale for this object!");
    }
}
