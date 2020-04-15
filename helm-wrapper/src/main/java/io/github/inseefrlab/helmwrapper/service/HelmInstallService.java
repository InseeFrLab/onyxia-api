package io.github.inseefrlab.helmwrapper.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.InvalidExitValueException;

import io.github.inseefrlab.helmwrapper.utils.Command;

/**
 * HelmInstall
 */
@Service
public class HelmInstallService {


    private final Logger logger = LoggerFactory.getLogger(HelmInstallService.class);


    public HelmInstaller installChart(String name, String chart, Map<String, String> env, String namespace, Boolean dryRun)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        String command = "helm install " + name + " " + chart + " " + buildEnvVar(env);
        if (dryRun){
            command=command.concat(" --dry-run");
        }
        String res =Command.executeAndGetResponseAsJson(command).getOutput().getString();
        logger.info(res);
        return new ObjectMapper().readValue(res,HelmInstaller.class);
    }

    public HelmInstaller installChart(String name, String chart, File values, String namespace, boolean dryRun)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        String command = "helm install " + name + " " + chart + " -f " + values.getAbsolutePath();
        if (dryRun){
            command=command.concat(" --dry-run");
        }
        logger.info(command);
        String res = Command.executeAndGetResponseAsJson(command).getOutput().getString();
        logger.info(res);
        return new ObjectMapper().readValue(res,HelmInstaller.class);
    }


    public int uninstaller(String name)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return Command.execute("helm uninstall " + name).getExitValue();
    }

    public HelmLs[] listChartInstall(String namespace) throws JsonMappingException, InvalidExitValueException,
            JsonProcessingException, IOException, InterruptedException, TimeoutException {
        String cmd ="helm ls";
        if (namespace != null){
            cmd= cmd+  " -n "+namespace;
        }
        return new ObjectMapper().readValue(
                Command.executeAndGetResponseAsJson(cmd).getOutput().getString(), HelmLs[].class);
    }

    public String getRelease(String id, String namespace){
        try {
            return Command.execute("helm get manifest " + id+" --namespace "+ namespace).getOutput().getString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String buildEnvVar(Map<String, String> env) {
        if (env != null) {
            Set<String> envKeys = env.keySet();
            return envKeys.stream().map(key -> "--set " + key + "=" + env.get(key)).collect(Collectors.joining(" "));
        }
        return "";
    }
}