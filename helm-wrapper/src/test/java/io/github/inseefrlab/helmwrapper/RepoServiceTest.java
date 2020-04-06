package io.github.inseefrlab.helmwrapper;

import io.github.inseefrlab.helmwrapper.model.HelmRepo;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RepoServiceTest {

    @Autowired
    private HelmRepoService helmRepoService;

    @Test
    public void should() throws Exception {
        helmRepoService.addHelmRepo("https://inseefrlab.github.io/helm-charts","inseefrlab");
        HelmRepo[] repos = helmRepoService.getHelmRepo();
        Assertions.assertEquals(1,repos.length);
    }
}
