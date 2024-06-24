package io.github.inseefrlab.helmwrapper.service;

import io.github.inseefrlab.helmwrapper.utils.Command;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.zeroturnaround.exec.InvalidExitValueException;

public class HelmVersionService {

    public String getVersion()
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return Command.executeAndGetResponseAsRaw(
                        null, "helm version --template={{.Version}}", true)
                .getOutput()
                .getString(StandardCharsets.UTF_8.name());
    }
}
