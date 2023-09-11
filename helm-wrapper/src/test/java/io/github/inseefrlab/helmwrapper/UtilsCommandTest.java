package io.github.inseefrlab.helmwrapper;

import io.github.inseefrlab.helmwrapper.utils.Command;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class UtilsCommandTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "rstudio",
                    "hello-world",
                    " vscode-python-33432 ",
                    "vscode-python-11 ",
                    " rstudio"
            })
    public void shouldAllowConcatenate(String toConcatenate) {
        Command.safeConcat(new StringBuilder("helm ls "), toConcatenate);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "",
                    "--post-renderer test",
                    "-o",
                    "-o",
                    "--option",
                    " --option",
                    "&",
                    "&&",
                    "@",
                    "|"
            })
    public void shouldDisallowConcatenate(String toConcatenate) {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Command.safeConcat(new StringBuilder("helm ls "), toConcatenate);
                });
    }
}
