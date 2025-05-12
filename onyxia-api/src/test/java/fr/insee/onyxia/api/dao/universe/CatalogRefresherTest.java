package fr.insee.onyxia.api.dao.universe;

import static org.mockito.Mockito.*;

import fr.insee.onyxia.api.configuration.CatalogWrapper;
import fr.insee.onyxia.api.configuration.Catalogs;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = "catalogs.refresh.ms=1000")
class CatalogRefresherTest {

    @Mock private Catalogs catalogs;

    @Mock private CatalogLoader catalogLoader;

    @Mock private HelmRepoService helmRepoService;

    private CatalogRefresher catalogRefresher;

    @BeforeEach
    void setUp() {
        catalogRefresher = new CatalogRefresher(catalogs, catalogLoader, helmRepoService);
        Thread.interrupted(); // Clear any existing interrupted status
    }

    @Test
    @DisplayName("Test InterruptedException Handling")
    void testInterruptedExceptionHandling() throws Exception {
        doThrow(new InterruptedException("Thread was interrupted"))
                .when(helmRepoService)
                .repoUpdate();

        catalogRefresher.run();

        verify(helmRepoService).repoUpdate();
        verify(catalogLoader, never()).updateCatalog(any(CatalogWrapper.class));

        assert (Thread.currentThread().isInterrupted());
        Thread.interrupted(); // Reset interrupt status for other tests
    }

    @Test
    @DisplayName("Test TimeoutException Handling")
    void testTimeoutExceptionHandling() throws Exception {
        doThrow(new TimeoutException("Timeout")).when(helmRepoService).repoUpdate();

        catalogRefresher.run();

        verify(helmRepoService).repoUpdate();
        verify(catalogLoader, never()).updateCatalog(any(CatalogWrapper.class));
    }

    @Test
    @DisplayName("Test IOException Handling")
    void testIOExceptionHandling() throws Exception {
        doThrow(new IOException("IO error")).when(helmRepoService).repoUpdate();

        catalogRefresher.run();

        verify(helmRepoService).repoUpdate();
        verify(catalogLoader, never()).updateCatalog(any(CatalogWrapper.class));
    }

    @Test
    @DisplayName("Test Successful Refresh")
    void testSuccessfulRefresh() throws Exception {
        CatalogWrapper catalogWrapper = mock(CatalogWrapper.class);
        when(catalogWrapper.getLocation()).thenReturn("location");
        when(catalogWrapper.getId()).thenReturn("id");
        when(catalogWrapper.getSkipTlsVerify()).thenReturn(false);
        when(catalogWrapper.getCaFile()).thenReturn(null);

        when(catalogs.getCatalogs()).thenReturn(List.of(catalogWrapper));
        when(helmRepoService.addHelmRepo("location", "id", false, null, null, null))
                .thenReturn("Repo added");

        catalogRefresher.run();

        verify(helmRepoService).repoUpdate();
        verify(helmRepoService, times(1)).addHelmRepo("location", "id", false, null, null, null);
        verify(catalogLoader, times(1)).updateCatalog(catalogWrapper);
    }
}
