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

    public String addHelmRepo(final String url, final String nomRepo)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return Command.execute("helm repo add " + nomRepo + " " + url).getOutput().getString();
    }

    public void repoUpdate() throws InterruptedException, TimeoutException, IOException {
        Command.execute("helm repo update");
    }
}
