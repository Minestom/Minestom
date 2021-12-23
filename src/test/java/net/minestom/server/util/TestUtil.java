package net.minestom.server.util;

import net.minestom.server.exception.ExceptionManager;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public final class TestUtil {
    private TestUtil() {}

    public static final ExceptionManager IGNORING_EXTENSION_MANAGER;
    static {
        IGNORING_EXTENSION_MANAGER = Mockito.mock(ExceptionManager.class);
        IGNORING_EXTENSION_MANAGER.setExceptionHandler(ignored -> {});
        doThrow(new UnsupportedOperationException("Cannot replace exception handler in tests."))
                .when(IGNORING_EXTENSION_MANAGER).setExceptionHandler(any());

    }

}
