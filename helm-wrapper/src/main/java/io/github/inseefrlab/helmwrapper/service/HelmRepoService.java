package io.github.inseefrlab.helmwrapper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.inseefrlab.helmwrapper.model.HelmRepo;
import io.github.inseefrlab.helmwrapper.utils.Command;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.exec.InvalidExitValueException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/** HelmExecuter */
public class HelmRepoService {

    public HelmRepo[] getHelmRepo()
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        // System.out.println(new ProcessExecutor().getDirectory().getAbsolutePath());
        HelmRepo[] repo =
                new ObjectMapper()
                        .readValue(
                                Command.executeAndGetResponseAsJson("helm search repo")
                                        .getOutput()
                                        .getString(StandardCharsets.UTF_8.name()),
                                HelmRepo[].class);
        return repo;
    }

    public String addHelmRepo(
            final String url,
            final String nomRepo,
            final boolean skipTlsVerify,
            final String caFile,
            final String username,
            final String password)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        String command = "helm repo add ";
        if (skipTlsVerify) {
            command = command.concat("--insecure-skip-tls-verify ");
        } else if (caFile != null) {
            command =
                    command.concat(
                            "--ca-file " + System.getenv("CACERTS_DIR") + "/" + caFile + " ");
        }
        if (StringUtils.isNotEmpty(username)) {
            command = command.concat("--username "+username+" ");
        }
        if (StringUtils.isNotEmpty(password)) {
            command = command.concat("--password "+password+" ");
        }
        command = command.concat(nomRepo + " " + url);
        return Command.execute(command).getOutput().getString();
    }

    public void repoUpdate() throws InterruptedException, TimeoutException, IOException {
        Command.execute("helm repo update");
    }
}
