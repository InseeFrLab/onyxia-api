package io.github.inseefrlab.helmwrapper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.utils.Command;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;

/** HelmInstall */
public class HelmInstallService {

    private final Logger logger = LoggerFactory.getLogger(HelmInstallService.class);

    public HelmInstallService() {}

    public HelmInstaller installChart(
            HelmConfiguration configuration,
            String chart,
            String namespace,
            String name,
            String version,
            boolean dryRun,
            File values,
            Map<String, String> env,
            final boolean skipTlsVerify,
            String caFile)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        String command = "helm upgrade --install ";
        if (skipTlsVerify) {
            command = command.concat("--insecure-skip-tls-verify ");
        } else if (caFile != null) {
            command =
                    command.concat(
                            "--ca-file " + System.getenv("CACERTS_DIR") + "/" + caFile + " ");
        }
        if (name != null) {
            command = command.concat(name + " ");
        } else {
            command = command.concat("--generate-name ");
        }
        command = command.concat(chart + " ");
        command = command.concat("-n " + namespace);
        if (StringUtils.isNotBlank(version)) {
            command = command.concat(" --version " + version);
        }
        if (values != null) {
            command = command.concat(" -f " + values.getAbsolutePath());
        }
        if (env != null) {
            command = command.concat(buildEnvVar(env));
        }
        if (dryRun) {
            command = command.concat(" --dry-run");
        }
        String res =
                Command.executeAndGetResponseAsJson(configuration, command).getOutput().getString();
        return new ObjectMapper().readValue(res, HelmInstaller.class);
    }

    public int uninstaller(HelmConfiguration configuration, String name, String namespace)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return Command.execute(configuration, "helm uninstall " + name + " -n " + namespace)
                .getExitValue();
    }

    public HelmLs[] listChartInstall(HelmConfiguration configuration, String namespace)
            throws JsonMappingException, InvalidExitValueException, JsonProcessingException,
                    IOException, InterruptedException, TimeoutException {
        String cmd = "helm ls";
        if (namespace != null) {
            cmd = cmd + " -n " + namespace;
        }
        return new ObjectMapper()
                .readValue(
                        Command.executeAndGetResponseAsJson(configuration, cmd)
                                .getOutput()
                                .getString(),
                        HelmLs[].class);
    }

    public String getManifest(HelmConfiguration configuration, String id, String namespace) {
        try {
            return Command.execute(
                            configuration, "helm get manifest " + id + " --namespace " + namespace)
                    .getOutput()
                    .getString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getValues(HelmConfiguration configuration, String id, String namespace) {
        try {
            return Command.executeAndGetResponseAsJson(
                            configuration, "helm get values " + id + " --namespace " + namespace)
                    .getOutput()
                    .getString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getNotes(HelmConfiguration configuration, String id, String namespace) {
        try {
            return Command.executeAndGetResponseAsRaw(
                            configuration, "helm get notes " + id + " --namespace " + namespace)
                    .getOutput()
                    .getString();
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
            return envKeys.stream()
                    .map(key -> "--set " + key + "=" + env.get(key))
                    .collect(Collectors.joining(" "));
        }
        return "";
    }

    /**
     * @param appId
     * @param namespace
     * @return
     * @throws MultipleServiceFound
     */
    public HelmLs getAppById(HelmConfiguration configuration, String appId, String namespace)
            throws MultipleServiceFound {
        try {
            HelmLs[] result =
                    new ObjectMapper()
                            .readValue(
                                    Command.executeAndGetResponseAsJson(
                                                    configuration,
                                                    "helm list --filter "
                                                            + appId
                                                            + " -n "
                                                            + namespace)
                                            .getOutput()
                                            .getString(),
                                    HelmLs[].class);
            if (result.length == 0) {
                return null;

            } else if (result.length == 1) {
                return result[0];
            } else {
                throw new MultipleServiceFound(
                        "One service was expected but " + result.length + " were found");
            }
        } catch (InvalidExitValueException
                | IOException
                | InterruptedException
                | TimeoutException e) {
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
