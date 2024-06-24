package io.github.inseefrlab.helmwrapper.utils;

import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import java.io.IOException;
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

    static ProcessExecutor getProcessExecutor() { // Changed to package-private
        ProcessExecutor processExecutor = new ProcessExecutor();
        processExecutor.redirectError(System.err);
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

    public static ProcessResult executeAndGetResponseAsJson(
            HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        validateCommand(command);
        return getProcessExecutor()
                .environment(getEnv(helmConfiguration))
                .commandSplit(buildSecureCommand(command, helmConfiguration) + " --output json")
                .execute();
    }

    public static ProcessResult executeAndGetResponseAsJson(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        validateCommand(command);
        return executeAndGetResponseAsJson(null, command);
    }

    public static ProcessResult executeAndGetResponseAsRaw(
            HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return executeAndGetResponseAsRaw(helmConfiguration, command, false);
    }

    public static ProcessResult executeAndGetResponseAsRaw(
            HelmConfiguration helmConfiguration, String command, boolean bypassCommandValidation)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        if (!bypassCommandValidation) {
            validateCommand(command);
        }
        return getProcessExecutor()
                .environment(getEnv(helmConfiguration))
                .commandSplit(buildSecureCommand(command, helmConfiguration))
                .execute();
    }

    public static ProcessResult executeAndGetResponseAsRaw(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return executeAndGetResponseAsRaw(null, command);
    }

    public static ProcessResult execute(HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        validateCommand(command);
        return getProcessExecutor()
                .environment(getEnv(helmConfiguration))
                .commandSplit(buildSecureCommand(command, helmConfiguration))
                .execute();
    }

    public static ProcessResult execute(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        validateCommand(command);
        return execute(null, command);
    }

    static Map<String, String> getEnv(
            HelmConfiguration helmConfiguration) { // Changed to package-private
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

    static String buildSecureCommand(
            String command, HelmConfiguration helmConfiguration) { // Changed to package-private
        StringBuilder newCommand = new StringBuilder(command).append(" ");
        if (helmConfiguration != null) {
            if (helmConfiguration.getAsKubeUser() != null) {
                newCommand
                        .append(" --kube-as-user ")
                        .append(escapeArgument(helmConfiguration.getAsKubeUser()))
                        .append(" ");
            }
            String kubeConfig = null;
            if (StringUtils.isNotEmpty(helmConfiguration.getApiserverUrl())) {
                newCommand
                        .append(" --kube-apiserver=")
                        .append(escapeArgument(helmConfiguration.getApiserverUrl()))
                        .append(" ");
                kubeConfig = "/dev/null";
            }
            if (StringUtils.isNotEmpty(helmConfiguration.getKubeToken())) {
                newCommand
                        .append(" --kube-token=")
                        .append(escapeArgument(helmConfiguration.getKubeToken()))
                        .append(" ");
                kubeConfig = "/dev/null";
            }
            if (StringUtils.isNotEmpty(helmConfiguration.getKubeConfig())) {
                kubeConfig = helmConfiguration.getKubeConfig();
            }
            if (kubeConfig != null) {
                newCommand.append(" --kubeconfig=").append(escapeArgument(kubeConfig)).append(" ");
            }
        }
        return newCommand.toString();
    }

    static void validateCommand(String command)
            throws IllegalArgumentException { // Changed to package-private
        if (!safeToConcatenate.matcher(command).matches()) {
            throw new IllegalArgumentException("Illegal characters in command");
        }
    }

    static String escapeArgument(String argument) { // Changed to package-private
        return argument.replaceAll("([\"\\\\])", "\\\\$1");
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
