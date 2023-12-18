package fr.insee.onyxia.api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class TestUtils {
    public static String tapSystemOut(Runnable runnableThatLogs) {
        PrintStream standard = System.out;
        try (ByteArrayOutputStream reroutedOutputStream = new ByteArrayOutputStream()) {
            try (PrintStream printStream = new PrintStream(reroutedOutputStream)) {
                System.setOut(printStream);
                runnableThatLogs.run();
                return reroutedOutputStream.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.setOut(standard);
        }
    }
}
