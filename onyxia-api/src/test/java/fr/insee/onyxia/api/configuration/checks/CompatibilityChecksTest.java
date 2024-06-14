package fr.insee.onyxia.api.configuration.checks;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import io.github.inseefrlab.helmwrapper.service.HelmVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompatibilityChecksTest {

    @Mock private HelmVersionService helmVersionService;

    @InjectMocks private CompatibilityChecks compatibilityChecks;

    private boolean exitCalled;

    @BeforeEach
    void setUp() {
        exitCalled = false;
        compatibilityChecks =
                new CompatibilityChecks(null, null, helmVersionService, () -> exitCalled = true);
    }

    @Test
    void testCheckHelmHandlesInterruptedException() throws Exception {
        doThrow(new InterruptedException("Thread was interrupted"))
                .when(helmVersionService)
                .getVersion();

        compatibilityChecks.checkHelm();

        verify(helmVersionService).getVersion();
        // Ensure that the current thread's interrupted status is set
        assert (Thread.currentThread().isInterrupted());
    }

    @Test
    void testCheckHelmHandlesGenericException() throws Exception {
        doThrow(new RuntimeException("Some other exception")).when(helmVersionService).getVersion();

        compatibilityChecks.checkHelm();

        verify(helmVersionService).getVersion();
        assert (exitCalled); // Check that the exit handler was called
    }
}
