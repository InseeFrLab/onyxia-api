package io.github.inseefrlab.helmwrapper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.utils.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.InvalidExitValueException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * HelmInstall
 */
@Service
public class HelmInstallService {

    private final Logger logger = LoggerFactory.getLogger(HelmInstallService.class);

    public HelmInstaller installChart(String chart, String namespace, String name, Boolean dryRun, Map<String, String> env)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return installChart(chart, namespace, name, dryRun, null, env);
    }

    public HelmInstaller installChart(String chart, String namespace, String name, boolean dryRun, File values)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return installChart(chart, namespace, name, dryRun, values, null);
    }

    public HelmInstaller installChart(String chart, String namespace, String name, boolean dryRun, File values,
            Map<String, String> env)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        String command = "helm install ";
        if (name != null) {
            command = command.concat(name+ " ");
        }
        else {
            command = command.concat("--generate-name ");
        }
        command = command.concat(chart+" ");
        command = command.concat("-n "+namespace);
        if (values != null) {
            command = command.concat(" -f " + values.getAbsolutePath());
        }
        if (env != null) {
            command = command.concat(buildEnvVar(env));
        }
        if (dryRun) {
            command = command.concat(" --dry-run");
        }
        String res = Command.executeAndGetResponseAsJson(command).getOutput().getString();
        return new ObjectMapper().readValue(res, HelmInstaller.class);
    }

    public int uninstaller(String name, String namespace)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return Command.execute("helm uninstall " + name + " -n " + namespace).getExitValue();
    }

    public HelmLs[] listChartInstall(String namespace) throws JsonMappingException, InvalidExitValueException,
            JsonProcessingException, IOException, InterruptedException, TimeoutException {
        String cmd = "helm ls";
        if (namespace != null) {
            cmd = cmd + " -n " + namespace;
        }
        return new ObjectMapper().readValue(Command.executeAndGetResponseAsJson(cmd).getOutput().getString(),
                HelmLs[].class);
    }

    public String getManifest(String id, String namespace) {
        try {
            return Command.execute("helm get manifest " + id + " --namespace " + namespace).getOutput().getString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getValues(String id, String namespace) {
        try {
            return Command.executeAndGetResponseAsJson("helm get values " + id + " --namespace " + namespace).getOutput().getString();
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

    /**
     * 
     * @param appId
     * @param namespace
     * @return
     * @throws MultipleServiceFound
     */
    public HelmLs getAppById(String appId, String namespace) throws MultipleServiceFound {
        try {
            HelmLs[] result = new ObjectMapper()
                    .readValue(Command.executeAndGetResponseAsJson("helm list --filter " + appId + " -n " + namespace)
                            .getOutput().getString(), HelmLs[].class);
            if (result.length == 0) {
                return null;

            } else if (result.length == 1) {
                return result[0];
            } else {
                throw new MultipleServiceFound("One service was expected but " + result.length + " were found");
            }
        } catch (InvalidExitValueException | IOException | InterruptedException | TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public class MultipleServiceFound extends Exception {

        public MultipleServiceFound(String s) {
            super(s);
        }
    }
}