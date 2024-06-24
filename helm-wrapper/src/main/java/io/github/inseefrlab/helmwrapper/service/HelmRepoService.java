package io.github.inseefrlab.helmwrapper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.inseefrlab.helmwrapper.model.HelmRepo;
import io.github.inseefrlab.helmwrapper.utils.Command;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.zeroturnaround.exec.InvalidExitValueException;

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
            final String caFile)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        String command = "helm repo add ";
        if (skipTlsVerify) {
            command = command.concat("--insecure-skip-tls-verify ");
        } else if (caFile != null) {
            command =
                    command.concat(
                            "--ca-file " + System.getenv("CACERTS_DIR") + "/" + caFile + " ");
        }
        command = command.concat(nomRepo + " " + url);
        return Command.execute(null, command, true).getOutput().getString();
    }

    public void repoUpdate() throws InterruptedException, TimeoutException, IOException {
        Command.execute("helm repo update");
    }
}
