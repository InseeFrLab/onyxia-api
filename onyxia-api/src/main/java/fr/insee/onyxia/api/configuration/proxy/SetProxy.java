package fr.insee.onyxia.api.configuration.proxy;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class SetProxy {

    @Value("${http.proxyHost}")
    private String httpProxyHost;

    @Value("${http.proxyPort}")
    private String httpProxyPort;

    @Value("${http.noProxy}")
    private String noProxy;

    @Value("${http.proxyUsername}")
    private String proxyUsername;

    @Value("${http.proxyPassword}")
    private String proxyPassword;

    @PostConstruct
    public void setProxy() {
        if (StringUtils.isNotEmpty(httpProxyHost)) {
            System.out.println("Using proxy host : "+httpProxyHost);
            System.setProperty("http.proxyHost", httpProxyHost);
            System.setProperty("https.proxyHost", httpProxyHost);
            if (StringUtils.isNotEmpty(httpProxyPort)) {
                System.out.println("Using proxy port : "+httpProxyPort);
                System.setProperty("http.proxyPort", httpProxyPort);
                System.setProperty("https.proxyPort", httpProxyPort);
            }
            if (StringUtils.isNotEmpty(noProxy)) {
                System.out.println("No proxy : " + noProxy);
                System.setProperty("http.nonProxyHosts", noProxy);
            }
            if (StringUtils.isNotEmpty(proxyUsername)) {
                System.out.println("Proxy username  : "+proxyUsername);
                System.setProperty("http.proxyUser", proxyUsername);
                if (StringUtils.isNotEmpty(proxyPassword)) {
                    System.setProperty("http.proxyPassword", proxyPassword);
                }
            }
        }
    }
}
