package io.github.inseefrlab.helmwrapper;

import io.github.inseefrlab.helmwrapper.model.HelmReleaseInfo;
import io.github.inseefrlab.helmwrapper.utils.HelmReleaseInfoParser;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReleaseInfoTest {

    private HelmReleaseInfoParser helmReleaseInfoParser = new HelmReleaseInfoParser();

    @ParameterizedTest
    @ValueSource(strings = {"releaseInfoResult.txt", "releaseInfoResult-without-notes.txt"})
    public void shouldExtractReleaseInfo(String resultFileName) throws Exception {
        String input =
                Files.readString(
                        Paths.get(getClass().getClassLoader().getResource(resultFileName).toURI()));
        HelmReleaseInfo releaseInfo = helmReleaseInfoParser.parseReleaseInfo(input);
        Assertions.assertEquals(1, releaseInfo.getRevision());
    }
}
