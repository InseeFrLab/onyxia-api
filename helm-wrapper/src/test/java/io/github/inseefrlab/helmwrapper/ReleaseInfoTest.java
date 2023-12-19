package io.github.inseefrlab.helmwrapper;

import io.github.inseefrlab.helmwrapper.model.HelmReleaseInfo;
import io.github.inseefrlab.helmwrapper.utils.HelmReleaseInfoParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
public class ReleaseInfoTest {

    private HelmReleaseInfoParser helmReleaseInfoParser = new HelmReleaseInfoParser();

    @Test
    public void shouldExtractReleaseInfo() throws Exception {
        String input = Files.readString(Paths.get(getClass().getClassLoader().getResource("releaseInfoResult.txt").toURI()));
        HelmReleaseInfo releaseInfo = helmReleaseInfoParser.parseReleaseInfo(input);
        System.out.println(releaseInfo.getName());
    }
}
