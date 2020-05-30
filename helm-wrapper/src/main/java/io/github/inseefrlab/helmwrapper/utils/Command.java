package io.github.inseefrlab.helmwrapper.utils;

import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Executeur
 */
public class Command {
    public static ProcessResult executeAndGetResponseAsJson(HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return new ProcessExecutor().environment(getEnv(helmConfiguration)).readOutput(true).commandSplit(addConfigToCommand(command,helmConfiguration) + " --output json").execute();
    }

    public static ProcessResult executeAndGetResponseAsJson(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return executeAndGetResponseAsJson(null, command);
    }

    public static ProcessResult execute(HelmConfiguration helmConfiguration, String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return new ProcessExecutor().environment(getEnv(helmConfiguration)).readOutput(true).commandSplit(addConfigToCommand(command, helmConfiguration)).execute();
    }

    public static ProcessResult execute(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return execute(null, command);
    }

    private static Map<String,String> getEnv(HelmConfiguration helmConfiguration) {
        Map<String,String> env = new HashMap<>();
        if (System.getProperty("http.proxyHost") != null) {
            env.put("HTTP_PROXY", System.getProperty("http.proxyHost")+(System.getProperty("http.proxyPort") != null ? ":"+System.getProperty("http.proxyPort") : ""));
        }

        if (System.getProperty("https.proxyHost") != null) {
            env.put("HTTPS_PROXY", System.getProperty("https.proxyHost")+(System.getProperty("https.proxyPort") != null ? ":"+System.getProperty("https.proxyPort") : ""));
        }

        if (System.getProperty("http.nonProxyHosts") != null) {
            env.put("NO_PROXY", System.getProperty("http.nonProxyHosts"));
        }

        return env;
    }

    private static String addConfigToCommand(String command, HelmConfiguration helmConfiguration) {
        if (helmConfiguration == null) {
            return command;
        }
        String newCommand = command;
        newCommand = newCommand.concat(" ");
        if (StringUtils.isNotEmpty(helmConfiguration.getApiserverUrl())) {
            newCommand = newCommand.concat(" --kube-apiserver="+helmConfiguration.getApiserverUrl()).concat(" ");
        }

        if (StringUtils.isNotEmpty(helmConfiguration.getKubeToken())) {
            newCommand = newCommand.concat(" --kube-token="+helmConfiguration.getKubeToken()).concat(" ");
        }

        if (StringUtils.isNotEmpty(helmConfiguration.getKubeConfig())) {
            newCommand = newCommand.concat(" --kubeconfig="+ helmConfiguration.getKubeConfig()).concat(" ");
        }


        System.out.println(newCommand);
        return newCommand;
    }
}
