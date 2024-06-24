package io.github.inseefrlab.helmwrapper.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

class CommandTest {

    private HelmConfiguration helmConfiguration;
    private ProcessExecutor processExecutor;

    @BeforeEach
    void setUp() {
        helmConfiguration = new HelmConfiguration();
        helmConfiguration.setAsKubeUser("admin-user");
        helmConfiguration.setApiserverUrl("https://k8s-cluster.local");
        helmConfiguration.setKubeToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        helmConfiguration.setKubeConfig("/etc/kubernetes/admin.conf");

        processExecutor = mock(ProcessExecutor.class);
    }

    @Test
    void testExecuteAndGetResponseAsJson_withValidCommand() throws Exception {
        try (MockedStatic<Command> mockedCommand =
                Mockito.mockStatic(Command.class, Mockito.CALLS_REAL_METHODS)) {
            mockedCommand.when(Command::getProcessExecutor).thenReturn(processExecutor);

            ProcessResult expectedResult = mock(ProcessResult.class);
            when(processExecutor.commandSplit(any())).thenReturn(processExecutor);
            when(processExecutor.environment(any(Map.class))).thenReturn(processExecutor);
            when(processExecutor.execute()).thenReturn(expectedResult);

            ProcessResult result =
                    Command.executeAndGetResponseAsJson(helmConfiguration, "helm repo list");

            assertNotNull(result);
            verify(processExecutor, times(1)).execute();
        }
    }

    @Test
    void testExecuteAndGetResponseAsJson_withInvalidCommand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Command.executeAndGetResponseAsJson(helmConfiguration, "invalid;command");
                });
    }

    @Test
    void testExecuteAndGetResponseAsRaw_withValidCommand() throws Exception {
        try (MockedStatic<Command> mockedCommand =
                Mockito.mockStatic(Command.class, Mockito.CALLS_REAL_METHODS)) {
            mockedCommand.when(Command::getProcessExecutor).thenReturn(processExecutor);

            ProcessResult expectedResult = mock(ProcessResult.class);
            when(processExecutor.commandSplit(any())).thenReturn(processExecutor);
            when(processExecutor.environment(any(Map.class))).thenReturn(processExecutor);
            when(processExecutor.execute()).thenReturn(expectedResult);

            ProcessResult result =
                    Command.executeAndGetResponseAsRaw(helmConfiguration, "helm repo list");

            assertNotNull(result);
            verify(processExecutor, times(1)).execute();
        }
    }

    @Test
    void testExecuteAndGetResponseAsRaw_withInvalidCommand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Command.executeAndGetResponseAsRaw(helmConfiguration, "invalid;command");
                });
    }

    @Test
    void testExecute_withValidCommand() throws Exception {
        try (MockedStatic<Command> mockedCommand =
                Mockito.mockStatic(Command.class, Mockito.CALLS_REAL_METHODS)) {
            mockedCommand.when(Command::getProcessExecutor).thenReturn(processExecutor);

            ProcessResult expectedResult = mock(ProcessResult.class);
            when(processExecutor.commandSplit(any())).thenReturn(processExecutor);
            when(processExecutor.environment(any(Map.class))).thenReturn(processExecutor);
            when(processExecutor.execute()).thenReturn(expectedResult);

            ProcessResult result = Command.execute(helmConfiguration, "helm repo list");

            assertNotNull(result);
            verify(processExecutor, times(1)).execute();
        }
    }

    @Test
    void testExecute_withNefariousCommand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Command.execute(helmConfiguration, "helm repo list; rm -rf /");
                });
    }

    @Test
    void testValidateCommand_withValidCommand() {
        assertDoesNotThrow(
                () -> {
                    Command.validateCommand("helm repo list");
                });
    }

    @Test
    void testValidateCommand_withInvalidCommand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Command.validateCommand("invalid;command");
                });
    }

    @Test
    void testValidateCommand_withNefariousCommand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Command.validateCommand("helm repo list; rm -rf /");
                });
    }

    @Test
    void testEscapeArgument() {
        String escapedArgument = Command.escapeArgument("\"test\"");
        assertEquals("\\\"test\\\"", escapedArgument);
    }

    @Test
    void testBuildSecureCommand() {
        String secureCommand = Command.buildSecureCommand("helm repo list", helmConfiguration);
        assertTrue(secureCommand.contains(" --kube-as-user admin-user "));
        assertTrue(secureCommand.contains(" --kube-apiserver=https://k8s-cluster.local "));
        assertTrue(
                secureCommand.contains(" --kube-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... "));
        assertTrue(secureCommand.contains(" --kubeconfig=/etc/kubernetes/admin.conf "));
    }

    @Test
    void testGetEnv() {
        System.setProperty("http.proxyHost", "http://proxy");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "https://secureproxy");
        System.setProperty("https.proxyPort", "8443");
        System.setProperty("no_proxy", "localhost");

        Map<String, String> env = Command.getEnv(helmConfiguration);

        assertEquals("http://proxy:8080", env.get("HTTP_PROXY"));
        assertEquals("https://secureproxy:8443", env.get("HTTPS_PROXY"));
        assertEquals("localhost", env.get("NO_PROXY"));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "rstudio",
                "hello-world",
                "vscode-python-33432",
                "vscode-python-11",
                "rstudio"
            })
    public void shouldAllowConcatenate(String toConcatenate) {
        Command.safeConcat(new StringBuilder("helm ls -a"), toConcatenate);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "",
                "--post-renderer test",
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
                    Command.safeConcat(new StringBuilder("helm ls -a"), toConcatenate);
                });
    }
}
