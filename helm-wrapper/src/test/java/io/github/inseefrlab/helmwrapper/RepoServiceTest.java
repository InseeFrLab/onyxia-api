package io.github.inseefrlab.helmwrapper;

import io.github.inseefrlab.helmwrapper.model.HelmRepo;
import io.github.inseefrlab.helmwrapper.service.HelmRepoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled
public class RepoServiceTest {

    @Autowired private HelmRepoService helmRepoService;

    @Test
    public void should() throws Exception {
        HelmRepoService repoService = new HelmRepoService();
        helmRepoService.addHelmRepo(
                "https://inseefrlab.github.io/helm-charts", "inseefrlab", false, null);
        HelmRepo[] repos = helmRepoService.getHelmRepo();
        Assertions.assertEquals(1, repos.length);
    }
}
