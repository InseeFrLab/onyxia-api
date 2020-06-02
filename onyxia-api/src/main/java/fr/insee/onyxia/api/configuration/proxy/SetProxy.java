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

    @Value("${http.noproxy}")
    private String noProxy;

    @PostConstruct
    public void setProxy() {
        if (StringUtils.isNotEmpty(httpProxyHost)) {
            System.out.println("Using proxy "+httpProxyHost+(httpProxyPort != null ? ":"+httpProxyPort : ""));
            System.setProperty("http.proxyHost",httpProxyHost);
            System.setProperty("https.proxyHost",httpProxyHost);
            if (StringUtils.isNotEmpty(httpProxyPort)) {
                System.setProperty("http.proxyPort",httpProxyPort);
                System.setProperty("https.proxyPort",httpProxyPort);
            }
            if (StringUtils.isNotEmpty(noProxy)) {
                System.out.println("No proxy : "+noProxy);
                System.setProperty("http.nonProxyHosts", noProxy);
            }
        }
    }
}
