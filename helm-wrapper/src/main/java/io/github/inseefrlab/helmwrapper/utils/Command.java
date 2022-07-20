package io.github.inseefrlab.helmwrapper.utils;

import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.listener.ProcessListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Executeur
 */
public class Command {

    private static Logger LOGGER = LoggerFactory.getLogger(Command.class);

    private static ProcessExecutor getProcessExecutor() {
        ProcessExecutor processExecutor = new ProcessExecutor();
        processExecutor.redirectError(System.err);
        processExecutor.readOutput(true);
        processExecutor.addListener(new ProcessListener() {
            @Override
            public void afterStart(Process process, ProcessExecutor executor) {
                process.info().commandLine().ifPresent(cli -> LOGGER.info(cli));
                super.afterStart(process, executor);
            }
        });
        return processExecutor;
    }

    public static ProcessResult executeAndGetResponseAsJson(HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return getProcessExecutor().environment(getEnv(helmConfiguration)).commandSplit(addConfigToCommand(command, helmConfiguration) + " --output json").execute();
    }

    public static ProcessResult executeAndGetResponseAsJson(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return executeAndGetResponseAsJson(null, command);
    }

    public static ProcessResult executeAndGetResponseAsRaw(HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return getProcessExecutor().environment(getEnv(helmConfiguration)).commandSplit(addConfigToCommand(command, helmConfiguration)).execute();
    }

    public static ProcessResult executeAndGetResponseAsRaw(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return executeAndGetResponseAsRaw(null, command);
    }

    public static ProcessResult execute(HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return getProcessExecutor().environment(getEnv(helmConfiguration)).commandSplit(addConfigToCommand(command, helmConfiguration)).execute();
    }

    public static ProcessResult execute(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return execute(null, command);
    }

    private static Map<String, String> getEnv(HelmConfiguration helmConfiguration) {
        Map<String, String> env = new HashMap<>();
        if (System.getProperty("http.proxyHost") != null) {
            env.put("HTTP_PROXY", System.getProperty("http.proxyHost") + (System.getProperty("http.proxyPort") != null ? ":" + System.getProperty("http.proxyPort") : ""));
        }

        if (System.getProperty("https.proxyHost") != null) {
            env.put("HTTPS_PROXY", System.getProperty("https.proxyHost") + (System.getProperty("https.proxyPort") != null ? ":" + System.getProperty("https.proxyPort") : ""));
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
            newCommand = newCommand.concat(" --kube-as-user " + helmConfiguration.getAsKubeUser() + " ");
        }
        String kubeConfig = null;
        if (StringUtils.isNotEmpty(helmConfiguration.getApiserverUrl())) {
            newCommand = newCommand.concat(" --kube-apiserver=" + helmConfiguration.getApiserverUrl()).concat(" ");
            // Kubeconfig should be set to /dev/null to prevent mixing user provided configuration with pre-existing local kubeconfig (most likely re-using a cluster certificate from another cluster)
            kubeConfig = "/dev/null";
        }

        if (StringUtils.isNotEmpty(helmConfiguration.getKubeToken())) {
            newCommand = newCommand.concat(" --kube-token=" + helmConfiguration.getKubeToken()).concat(" ");
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
}
