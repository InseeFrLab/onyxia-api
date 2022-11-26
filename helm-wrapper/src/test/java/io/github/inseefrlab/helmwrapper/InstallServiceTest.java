package io.github.inseefrlab.helmwrapper;

import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled
public class InstallServiceTest {

    @Test
    public void contextLoads() {}

    @Test
    public void shouldListInstalls() throws Exception {
        HelmInstallService helmInstallService = new HelmInstallService();
        HelmLs[] result = helmInstallService.listChartInstall(null, null);
        Assertions.assertEquals(1, result.length);
    }

    @Test
    public void shouldThrowExceptionOnInvalidConfiguration() throws Exception {
        HelmConfiguration configuration = new HelmConfiguration();
        configuration.setApiserverUrl("https://invaliddomain");
        configuration.setKubeToken("");
        HelmInstallService helmInstallService = new HelmInstallService();
        Assertions.assertThrows(
                Exception.class,
                () -> {
                    HelmLs[] result = helmInstallService.listChartInstall(null, null);
                });
    }

    @SpringBootApplication
    public static class MyApp {}
}
