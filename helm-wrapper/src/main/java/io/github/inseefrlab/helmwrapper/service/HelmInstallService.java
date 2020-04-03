package io.github.inseefrlab.helmwrapper.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.model.install.HelmInstaller;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.InvalidExitValueException;

import io.github.inseefrlab.helmwrapper.utils.Command;

/**
 * HelmInstall
 */
@Service
public class HelmInstallService {

    public HelmInstaller installChart(String name, String chart, Map<String, String> env)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return new ObjectMapper().readValue(
                Command.executeAndGetResponseAsJson("helm install " + name + " " + chart + " " + buildEnvVar(env))
                        .getOutput().getString(),
                HelmInstaller.class);
    }

    public int uninstaller(String name)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return Command.execute("helm uninstall " + name).getExitValue();
    }

    public HelmLs[] listChartInstall() throws JsonMappingException, InvalidExitValueException,
            JsonProcessingException, IOException, InterruptedException, TimeoutException {
        return new ObjectMapper().readValue(
                Command.executeAndGetResponseAsJson("helm ls --output json").getOutput().getString(), HelmLs[].class);
    }

    private String buildEnvVar(Map<String, String> env) {
        Set<String> envKeys = env.keySet();
        return envKeys.stream().map(key -> "--set " + key + "=" + env.get(key)).collect(Collectors.joining(" "));
    }
}