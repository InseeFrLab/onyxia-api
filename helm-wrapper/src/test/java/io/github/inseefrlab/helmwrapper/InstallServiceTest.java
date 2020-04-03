package io.github.inseefrlab.helmwrapper;

import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.service.HelmInstallService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class InstallServiceTest {

    @Autowired
    private HelmInstallService helmInstallService;

    @Test
    public void contextLoads() {

    }

    @Test
    public void shouldListInstalls() throws Exception {
        HelmLs[] result = helmInstallService.listChartInstall();
        Assertions.assertEquals(1,result.length);
    }

    @SpringBootApplication
    public static class MyApp {

    }
}
