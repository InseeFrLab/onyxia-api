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

    @BeforeEach
    public void setUp() {}

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

        try {
            compatibilityChecks.checkHelm();
        } catch (Exception e) {
            // This shouldn't be reached due to System.exit(0), but we want to ensure the LOGGER
            // call is made
            verify(helmVersionService).getVersion();
            assert (false); // Fail the test if an exception is thrown
        }
    }
}
