package io.github.inseefrlab.helmwrapper.utils;

import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

/** Executeur */
public class Command {

    private static final Pattern safeToConcatenate =
            Pattern.compile(
                    "^[ ]*[a-z0-9]([-a-z0-9 ]*[a-z0-9 ])?(\\.[a-z0-9 ]([-a-z0-9 ]*[a-z0-9 ])?)*[ ]*$");
    private static final Logger LOGGER = LoggerFactory.getLogger(Command.class);

    private static ProcessExecutor getProcessExecutor(OutputStream errorOutputStream) {
        ProcessExecutor processExecutor = new ProcessExecutor();
        if (errorOutputStream != null) {
            processExecutor.redirectError(errorOutputStream);
        }
        processExecutor.readOutput(true);
        processExecutor.addListener(
                new ProcessListener() {
                    @Override
                    public void afterStart(Process process, ProcessExecutor executor) {
                        process.info().commandLine().ifPresent(cli -> LOGGER.info(cli));
                        super.afterStart(process, executor);
                    }
                });
        return processExecutor;
    }

    public static ProcessResultWithError executeAndGetResponseAsJson(
            HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        ProcessResult processResult =
                getProcessExecutor(errorStream)
                        .environment(getEnv(helmConfiguration))
                        .commandSplit(
                                addConfigToCommand(command, helmConfiguration) + " --output json")
                        .execute();
        return new ProcessResultWithError(processResult, errorStream);
    }

    public static ProcessResultWithError executeAndGetResponseAsJson(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return executeAndGetResponseAsJson(null, command);
    }

    public static ProcessResultWithError executeAndGetResponseAsRaw(
            HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        ProcessResult processResult =
                getProcessExecutor(errorStream)
                        .environment(getEnv(helmConfiguration))
                        .commandSplit(addConfigToCommand(command, helmConfiguration))
                        .execute();
        return new ProcessResultWithError(processResult, errorStream);
    }

    public static ProcessResultWithError executeAndGetResponseAsRaw(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return executeAndGetResponseAsRaw(null, command);
    }

    public static ProcessResultWithError execute(
            HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        ProcessResult processResult =
                getProcessExecutor(errorStream)
                        .environment(getEnv(helmConfiguration))
                        .commandSplit(addConfigToCommand(command, helmConfiguration))
                        .execute();
        return new ProcessResultWithError(processResult, errorStream);
    }

    public static ProcessResultWithError execute(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return execute(null, command);
    }

    public static class ProcessResultWithError {

        private ProcessResult processResult;
        private String error = null;

        public ProcessResultWithError() {}

        public ProcessResultWithError(ProcessResult processResult, ByteArrayOutputStream boas) {
            this.processResult = processResult;
            if (boas != null) {
                error = boas.toString(Charset.defaultCharset());
            }
        }

        public ProcessResult getProcessResult() {
            return processResult;
        }

        public String getError() {
            return error;
        }
    }

    private static Map<String, String> getEnv(HelmConfiguration helmConfiguration) {
        Map<String, String> env = new HashMap<>();
        if (System.getProperty("http.proxyHost") != null) {
            env.put(
                    "HTTP_PROXY",
                    System.getProperty("http.proxyHost")
                            + (System.getProperty("http.proxyPort") != null
                                    ? ":" + System.getProperty("http.proxyPort")
                                    : ""));
        }

        if (System.getProperty("https.proxyHost") != null) {
            env.put(
                    "HTTPS_PROXY",
                    System.getProperty("https.proxyHost")
                            + (System.getProperty("https.proxyPort") != null
                                    ? ":" + System.getProperty("https.proxyPort")
                                    : ""));
        }

        if (System.getProperty("no_proxy") != null) {
            env.put("NO_PROXY", System.getProperty("no_proxy"));
        }

        return env;
    }

    private static String addConfigToCommand(String command, HelmConfiguration helmConfiguration) {
        if (helmConfiguration == null) {
            return command;
        }
        String newCommand = command;
        newCommand = newCommand.concat(" ");
        if (helmConfiguration.getAsKubeUser() != null) {
            newCommand =
                    newCommand.concat(" --kube-as-user " + helmConfiguration.getAsKubeUser() + " ");
        }
        String kubeConfig = null;
        if (StringUtils.isNotEmpty(helmConfiguration.getApiserverUrl())) {
            newCommand =
                    newCommand
                            .concat(" --kube-apiserver=" + helmConfiguration.getApiserverUrl())
                            .concat(" ");
            // Kubeconfig should be set to /dev/null to prevent mixing user provided configuration
            // with pre-existing local kubeconfig (most likely re-using a cluster certificate from
            // another cluster)
            kubeConfig = "/dev/null";
        }

        if (StringUtils.isNotEmpty(helmConfiguration.getKubeToken())) {
            newCommand =
                    newCommand
                            .concat(" --kube-token=" + helmConfiguration.getKubeToken())
                            .concat(" ");
            kubeConfig = "/dev/null";
        }

        if (StringUtils.isNotEmpty(helmConfiguration.getKubeConfig())) {
            kubeConfig = helmConfiguration.getKubeConfig();
        }

        if (kubeConfig != null) {
            newCommand = newCommand.concat(" --kubeconfig=" + kubeConfig).concat(" ");
        }

        return newCommand;
    }

    public static void safeConcat(StringBuilder currentCommand, String toConcat)
            throws IllegalArgumentException {
        if (!safeToConcatenate.matcher(toConcat).matches()) {
            throw new IllegalArgumentException(
                    "Illegal character found while building helm command, refusing to concatenate "
                            + toConcat
                            + " to "
                            + currentCommand.toString());
        }
        currentCommand.append(toConcat);
    }
}
