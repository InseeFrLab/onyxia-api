package fr.insee.onyxia.api.dao.universe;

import static org.mockito.Mockito.*;

import fr.insee.onyxia.api.configuration.Catalogs;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

@ExtendWith(MockitoExtension.class)
public class CatalogRefresherTest {

    @Mock private Catalogs catalogs;

    @Mock private CatalogLoader catalogLoader;

    @Mock private HelmRepoService helmRepoService;

    @Mock private ApplicationArguments applicationArguments;

    private CatalogRefresher catalogRefresher;

    @BeforeEach
    public void setUp() {
        catalogRefresher = new CatalogRefresher(catalogs, catalogLoader, helmRepoService, 1000L);
    }

    @Test
    public void testRefreshHandlesInterruptedException() throws Exception {
        doThrow(new InterruptedException("Thread was interrupted"))
                .when(helmRepoService)
                .repoUpdate();

        catalogRefresher.run(applicationArguments);

        verify(helmRepoService).repoUpdate();
        // Ensure that the current thread's interrupted status is set
        assert (Thread.currentThread().isInterrupted());
    }

    @Test
    public void testRefreshHandlesTimeoutAndIOException() throws Exception {
        doThrow(new TimeoutException("Timeout")).when(helmRepoService).repoUpdate();

        catalogRefresher.run(applicationArguments);
        verify(helmRepoService).repoUpdate();

        doThrow(new IOException("IO Exception")).when(helmRepoService).repoUpdate();

        catalogRefresher.run(applicationArguments);
        verify(helmRepoService, times(2)).repoUpdate();
    }
}
