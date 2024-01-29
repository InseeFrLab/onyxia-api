package fr.insee.onyxia.api.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.springframework.core.io.ClassPathResource;

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

    /**
     * Get resource under 'resources' folder for test.
     *
     * @param path the path, e.g. my-folder/my-file.txt
     * @return the contents of the file, utf-8
     */
    public static String getClassPathResource(String path) {
        try {
            return new String(new ClassPathResource(path).getInputStream().readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
