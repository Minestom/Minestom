package net.minestom.server.extensions;

import net.minestom.server.MinecraftServer;
import net.minestom.server.exception.ExceptionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertSame;

public class ExtensionManagerTest {
    // MinecraftServer mocking to avoid calling MinecraftServer.init()
    private final ExceptionManager exceptionManager = new ExceptionManager();
    private MockedStatic<MinecraftServer> mockedMinecraftServer;

    private ExtensionManager extensionManager;
    private @TempDir Path dataRoot;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock ExceptionHandler in MinecraftServer
        exceptionManager.setExceptionHandler(ignored -> {});
        mockedMinecraftServer = Mockito.mockStatic(MinecraftServer.class);
        //noinspection ResultOfMethodCallIgnored
        mockedMinecraftServer.when(MinecraftServer::getExceptionManager)
                .thenReturn(exceptionManager);

        // Create ExtensionManager
//        extensionManager = new ExtensionManager();
//        extensionManager.setExtensionDataRoot(dataRoot);
    }

    @AfterEach
    public void teardown() {
        mockedMinecraftServer.close();
    }

    @Test
    public void validateMock() {
        ExceptionManager actual = MinecraftServer.getExceptionManager();
        assertSame(exceptionManager, actual);
    }


}
