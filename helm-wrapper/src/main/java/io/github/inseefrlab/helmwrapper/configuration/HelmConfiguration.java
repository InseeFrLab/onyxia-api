package io.github.inseefrlab.helmwrapper.configuration;

public class HelmConfiguration {

    private String apiserverUrl;
    private String kubeToken;
    private String kubeConfig;
    private String asKubeUser;

    public String getApiserverUrl() {
        return apiserverUrl;
    }

    public void setApiserverUrl(String apiserverUrl) {
        this.apiserverUrl = apiserverUrl;
    }

    public String getKubeToken() {
        return kubeToken;
    }

    public void setKubeToken(String kubeToken) {
        this.kubeToken = kubeToken;
    }

    public String getKubeConfig() {
        return kubeConfig;
    }

    public void setKubeConfig(String kubeConfig) {
        this.kubeConfig = kubeConfig;
    }

    public String getAsKubeUser() {
        return asKubeUser;
    }

    public void setAsKubeUser(String asKubeUser) {
        this.asKubeUser = asKubeUser;
    }
}
