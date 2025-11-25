package io.github.inseefrlab.helmwrapper.service;

import static io.github.inseefrlab.helmwrapper.utils.Command.safeConcat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.inseefrlab.helmwrapper.configuration.HelmConfiguration;
import io.github.inseefrlab.helmwrapper.model.HelmInstaller;
import io.github.inseefrlab.helmwrapper.model.HelmLs;
import io.github.inseefrlab.helmwrapper.model.HelmReleaseInfo;
import io.github.inseefrlab.helmwrapper.utils.Command;
import io.github.inseefrlab.helmwrapper.utils.HelmReleaseInfoParser;
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
import org.zeroturnaround.exec.InvalidExitValueException;

/** HelmInstall */
public class HelmInstallService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmInstallService.class);
    private final Pattern helmNamePattern =
            Pattern.compile("^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$");
    private final Pattern semverPattern =
            Pattern.compile(
                    "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");
    private final Pattern rfc1123Pattern = Pattern.compile("^[a-z0-9][a-z0-9-]{0,61}[a-z0-9]$");

    private final HelmReleaseInfoParser helmReleaseInfoParser = new HelmReleaseInfoParser();
    private static final String VALUES_INFO_TYPE = "values";
    private static final String MANIFEST_INFO_TYPE = "manifest";
    private static final String NOTES_INFO_TYPE = "notes";

    public void resume(
            HelmConfiguration configuration,
            String chart,
            String namespace,
            String name,
            String version,
            boolean dryRun,
            final boolean skipTlsVerify,
            String timeout,
            String caFile)
            throws InvalidExitValueException,
                    IOException,
                    InterruptedException,
                    TimeoutException,
                    IllegalArgumentException {
        installChart(
                configuration,
                chart,
                namespace,
                name,
                version,
                dryRun,
                null,
                Map.of("global.suspend", "false"),
                skipTlsVerify,
                timeout,
                caFile,
                true);
    }

    public void suspend(
            HelmConfiguration configuration,
            String chart,
            String namespace,
            String name,
            String version,
            boolean dryRun,
            final boolean skipTlsVerify,
            String timeout,
            String caFile)
            throws InvalidExitValueException,
                    IOException,
                    InterruptedException,
                    TimeoutException,
                    IllegalArgumentException {
        installChart(
                configuration,
                chart,
                namespace,
                name,
                version,
                dryRun,
                null,
                Map.of("global.suspend", "true"),
                skipTlsVerify,
                timeout,
                caFile,
                true);
    }

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
            String timeout,
            String caFile)
            throws InvalidExitValueException,
                    IOException,
                    InterruptedException,
                    TimeoutException,
                    IllegalArgumentException {
        return installChart(
                configuration,
                chart,
                namespace,
                name,
                version,
                dryRun,
                values,
                env,
                skipTlsVerify,
                timeout,
                caFile,
                false);
    }

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
            String timeout,
            String caFile,
            boolean reuseValues)
            throws InvalidExitValueException,
                    IOException,
                    InterruptedException,
                    TimeoutException,
                    IllegalArgumentException {
        StringBuilder command = new StringBuilder("helm upgrade --install --history-max 0 ");

        if (timeout != null) {
            command.append("--timeout " + timeout + " ");
        }

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
                                + " , must match regex "
                                + helmNamePattern
                                + " and the length must not be longer than 53");
            }
            safeConcat(command, name + " ");
        } else {
            command.append("--generate-name ");
        }
        command.append(chart + " ");
        command.append("-n ");
        if (namespace.length() > 63 || !rfc1123Pattern.matcher(namespace).matches()) {
            throw new IllegalArgumentException(
                    "Invalid namespace "
                            + namespace
                            + ". Must be 63 or fewer characters and be a valid RFC 1123 string.");
        }
        safeConcat(command, namespace);
        if (StringUtils.isNotBlank(version)) {
            if (!semverPattern.matcher(version).matches()) {
                throw new IllegalArgumentException(
                        "Invalid release version " + version + ", must be a SemVer 2 string");
            }
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
        if (reuseValues) {
            command.append(" --reuse-values");
        }
        String res =
                Command.executeAndGetResponseAsJson(configuration, command.toString())
                        .getOutput()
                        .getString();
        return new ObjectMapper().readValue(res, HelmInstaller.class);
    }

    public int uninstaller(HelmConfiguration configuration, String name, String namespace)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        if (name.length() > 53 || !rfc1123Pattern.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Invalid release "
                            + name
                            + ". Must be 53 or fewer characters and be a valid RFC 1123 string.");
        }
        if (namespace.length() > 63 || !rfc1123Pattern.matcher(namespace).matches()) {
            throw new IllegalArgumentException(
                    "Invalid namespace "
                            + namespace
                            + ". Must be 63 or fewer characters and be a valid RFC 1123 string.");
        }
        StringBuilder command = new StringBuilder("helm uninstall ");
        safeConcat(command, name);
        command.append(" -n ");
        safeConcat(command, namespace);
        return Command.execute(configuration, command.toString()).getExitValue();
    }

    public HelmLs[] listChartInstall(HelmConfiguration configuration, String namespace)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        StringBuilder command = new StringBuilder("helm ls ");
        if (namespace.length() > 63 || !rfc1123Pattern.matcher(namespace).matches()) {
            throw new IllegalArgumentException(
                    "Invalid namespace "
                            + namespace
                            + ". Must be 63 or fewer characters and be a valid RFC 1123 string.");
        }
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
        return getReleaseInfo(configuration, MANIFEST_INFO_TYPE, id, namespace);
    }

    public String getValues(HelmConfiguration configuration, String id, String namespace) {
        return getReleaseInfo(configuration, VALUES_INFO_TYPE, id, namespace);
    }

    public String getNotes(HelmConfiguration configuration, String id, String namespace) {
        return getReleaseInfo(configuration, NOTES_INFO_TYPE, id, namespace);
    }

    public HelmReleaseInfo getAll(HelmConfiguration configuration, String id, String namespace) {
        StringBuilder command = new StringBuilder("helm get all ");
        if (id.length() > 53 || !rfc1123Pattern.matcher(id).matches()) {
            throw new IllegalArgumentException(
                    "Invalid release "
                            + id
                            + ". Must be 53 or fewer characters and be a valid RFC 1123 string.");
        }
        if (namespace.length() > 63 || !rfc1123Pattern.matcher(namespace).matches()) {
            throw new IllegalArgumentException(
                    "Invalid namespace "
                            + namespace
                            + ". Must be 63 or fewer characters and be a valid RFC 1123 string.");
        }
        safeConcat(command, id);
        command.append(" --namespace ");
        safeConcat(command, namespace);
        try {
            String unparsedReleaseInfo =
                    Command.execute(configuration, command.toString()).getOutput().getString();
            return helmReleaseInfoParser.parseReleaseInfo(unparsedReleaseInfo);
        } catch (IOException | InterruptedException | TimeoutException e) {
            LOGGER.warn("Exception occurred", e);
        }
        return null;
    }

    private String getReleaseInfo(
            HelmConfiguration configuration, String infoType, String id, String namespace) {
        if (!List.of(MANIFEST_INFO_TYPE, NOTES_INFO_TYPE, VALUES_INFO_TYPE).contains(infoType)) {
            throw new IllegalArgumentException(
                    "Invalid info type " + infoType + ", should be manifest, notes or values");
        }
        if (id.length() > 53 || !rfc1123Pattern.matcher(id).matches()) {
            throw new IllegalArgumentException(
                    "Invalid release "
                            + id
                            + ". Must be 53 or fewer characters and be a valid RFC 1123 string.");
        }
        if (namespace.length() > 63 || !rfc1123Pattern.matcher(namespace).matches()) {
            throw new IllegalArgumentException(
                    "Invalid namespace "
                            + namespace
                            + ". Must be 63 or fewer characters and be a valid RFC 1123 string.");
        }
        StringBuilder command = new StringBuilder("helm get " + infoType + " ");
        try {
            safeConcat(command, id);
            command.append(" --namespace ");
            safeConcat(command, namespace);
            if (infoType.equals(NOTES_INFO_TYPE)) {
                return Command.executeAndGetResponseAsRaw(configuration, command.toString())
                        .getOutput()
                        .getString();
            } else if (infoType.equals(VALUES_INFO_TYPE)) {
                return Command.executeAndGetResponseAsJson(configuration, command.toString())
                        .getOutput()
                        .getString();
            } else {
                return Command.execute(configuration, command.toString()).getOutput().getString();
            }
        } catch (IOException | InterruptedException | TimeoutException e) {
            LOGGER.warn("Exception occurred", e);
        }
        return "";
    }

    private String buildEnvVar(Map<String, String> env) {
        if (env != null) {
            Set<String> envKeys = env.keySet();
            return envKeys.stream()
                    .map(key -> " --set " + key + "=" + env.get(key))
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
        if (appId.length() > 53 || !rfc1123Pattern.matcher(appId).matches()) {
            throw new IllegalArgumentException(
                    "Invalid app id "
                            + appId
                            + ". Must be 53 or fewer characters and be a valid RFC 1123 string.");
        }
        if (namespace.length() > 63 || !rfc1123Pattern.matcher(namespace).matches()) {
            throw new IllegalArgumentException(
                    "Invalid namespace "
                            + namespace
                            + ". Must be 63 or fewer characters and be a valid RFC 1123 string.");
        }
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
            LOGGER.warn("Exception occurred when getting app by id", e);
        }
        return null;
    }

    public static class MultipleServiceFound extends Exception {

        public MultipleServiceFound(String s) {
            super(s);
        }
    }
}
