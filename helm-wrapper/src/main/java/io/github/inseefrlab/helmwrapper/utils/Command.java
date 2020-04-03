package io.github.inseefrlab.helmwrapper.utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

/**
 * Executeur
 */
public class Command {
    public static ProcessResult executeAndGetResponseAsJson(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return new ProcessExecutor().readOutput(true).commandSplit(command + " --output json").execute();
    }

    public static ProcessResult execute(String command)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return new ProcessExecutor().readOutput(true).commandSplit(command).execute();
    }
}
