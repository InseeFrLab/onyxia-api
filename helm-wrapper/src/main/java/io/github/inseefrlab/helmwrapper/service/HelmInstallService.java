package io.github.inseefrlab.helmwrapper.service;

import static io.github.inseefrlab.helmwrapper.utils.Command.safeConcat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import io.github.inseefrlab.helmwrapper.events.InstallAppEventPublisher;
import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.utils.Command;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zeroturnaround.exec.InvalidExitValueException;

/** HelmInstall */
public class HelmInstallService {

    private final Pattern helmNamePattern =
            Pattern.compile("^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$");

    private static final Logger logger = LoggerFactory.getLogger(HelmInstallService.class);

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
            throws InvalidExitValueException,
                    IOException,
                    InterruptedException,
                    TimeoutException,
                    IllegalArgumentException {
        StringBuilder command = new StringBuilder("helm upgrade --install ");
        if (skipTlsVerify) {
            command.append("--insecure-skip-tls-verify ");
        } else if (caFile != null) {
            command.append("--ca-file " + System.getenv("CACERTS_DIR") + "/" + caFile + " ");
        }

        if (name != null) {
            if (!helmNamePattern.matcher(name).matches() || name.length() > 53) {
                throw new IllegalArgumentException(
                        "Invalid release name "
                                + name
                                + " , must match regex ^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$ and the length must not be longer than 53");
            }
            safeConcat(command, name + " ");
        } else {
            command.append("--generate-name ");
        }
        command.append(chart + " ");
        command.append("-n ");
        safeConcat(command, namespace);
        if (StringUtils.isNotBlank(version)) {
            command.append(" --version ");
            safeConcat(command, version);
        }
        if (values != null) {
            command.append(" -f " + values.getAbsolutePath());
        }
        if (env != null) {
            command.append(buildEnvVar(env));
        }
        if (dryRun) {
            command.append(" --dry-run");
        }
        String res =
                Command.executeAndGetResponseAsJson(configuration, command.toString())
                        .getOutput()
                        .getString();

        HelmInstaller helmInstaller = new ObjectMapper().readValue(res, HelmInstaller.class);
        return helmInstaller;
    }

    public int uninstaller(HelmConfiguration configuration, String name, String namespace)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        StringBuilder command = new StringBuilder("helm uninstall ");
        safeConcat(command, name);
        command.append(" -n ");
        safeConcat(command, namespace);
        return Command.execute(configuration, command.toString()).getExitValue();
    }

    public HelmLs[] listChartInstall(HelmConfiguration configuration, String namespace)
            throws JsonMappingException,
                    InvalidExitValueException,
                    JsonProcessingException,
                    IOException,
                    InterruptedException,
                    TimeoutException {
        StringBuilder command = new StringBuilder("helm ls");
        if (namespace != null) {
            command.append(" -n ");
            safeConcat(command, namespace);
        }
        return new ObjectMapper()
                .readValue(
                        Command.executeAndGetResponseAsJson(configuration, command.toString())
                                .getOutput()
                                .getString(),
                        HelmLs[].class);
    }

    public String getManifest(HelmConfiguration configuration, String id, String namespace) {
        return getReleaseInfo(configuration, "manifest", id, namespace);
    }

    public String getValues(HelmConfiguration configuration, String id, String namespace) {
        return getReleaseInfo(configuration, "values", id, namespace);
    }

    public String getNotes(HelmConfiguration configuration, String id, String namespace) {
        return getReleaseInfo(configuration, "notes", id, namespace);
    }

    private String getReleaseInfo(
            HelmConfiguration configuration, String infoType, String id, String namespace) {
        if (!List.of("manifest", "notes", "values").contains(infoType)) {
            throw new IllegalArgumentException(
                    "Invalid info type " + infoType + ", should be manifest, notes or values");
        }
        StringBuilder command = new StringBuilder("helm get " + infoType + " ");
        try {
            safeConcat(command, id);
            command.append(" --namespace ");
            safeConcat(command, namespace);
            if (infoType.equals("notes")) {
                return Command.executeAndGetResponseAsRaw(configuration, command.toString())
                        .getOutput()
                        .getString();
            } else if (infoType.equals("values")) {
                return Command.executeAndGetResponseAsJson(configuration, command.toString())
                        .getOutput()
                        .getString();
            } else {
                return Command.execute(configuration, command.toString()).getOutput().getString();
            }
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
        StringBuilder command = new StringBuilder("helm list --filter ");
        safeConcat(command, appId);
        command.append(" -n ");
        safeConcat(command, namespace);
        try {
            HelmLs[] result =
                    new ObjectMapper()
                            .readValue(
                                    Command.executeAndGetResponseAsJson(
                                                    configuration, command.toString())
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
