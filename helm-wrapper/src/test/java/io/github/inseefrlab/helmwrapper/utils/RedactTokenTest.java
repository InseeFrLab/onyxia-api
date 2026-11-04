package io.github.inseefrlab.helmwrapper.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

public class RedactTokenTest {

    private String invokeRedactToken(String commandLine) throws Exception {
        Method method = Command.class.getDeclaredMethod("redactToken", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, commandLine);
    }

    @Test
    public void shouldRedactKubeTokenWithEquals() throws Exception {
        String input =
                "helm ls --kube-token=eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0In0.test";
        String result = invokeRedactToken(input);
        assertEquals("helm ls --kube-token=[REDACTED]", result);
    }

    @Test
    public void shouldRedactKubeTokenWithSpace() throws Exception {
        String input =
                "helm ls --kube-token eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0In0.test";
        String result = invokeRedactToken(input);
        assertEquals("helm ls --kube-token=[REDACTED]", result);
    }

    @Test
    public void shouldRedactMultipleKubeTokens() throws Exception {
        String input = "helm ls --kube-token=token1 --kube-token=token2";
        String result = invokeRedactToken(input);
        assertEquals("helm ls --kube-token=[REDACTED] --kube-token=[REDACTED]", result);
    }

    @Test
    public void shouldLeaveOtherOptionsUnchanged() throws Exception {
        String input =
                "helm ls --kubeconfig=/path/to/config --kube-apiserver=https://api.example.com";
        String result = invokeRedactToken(input);
        assertEquals(
                "helm ls --kubeconfig=/path/to/config --kube-apiserver=https://api.example.com",
                result);
    }

    @Test
    public void shouldNotRedactIfNoToken() throws Exception {
        String input = "helm ls --namespace default";
        String result = invokeRedactToken(input);
        assertEquals("helm ls --namespace default", result);
    }

    @Test
    public void shouldRedactTokenWithTrailingOptions() throws Exception {
        String input = "helm ls --kube-token=secret123 --namespace myns --output json";
        String result = invokeRedactToken(input);
        assertEquals("helm ls --kube-token=[REDACTED] --namespace myns --output json", result);
    }
}
