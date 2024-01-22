package fr.insee.onyxia.api.configuration.proxy;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SetProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetProxy.class);

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
            LOGGER.info("Using proxy host : {}", httpProxyHost);
            System.setProperty("http.proxyHost", httpProxyHost);
            System.setProperty("https.proxyHost", httpProxyHost);
            if (StringUtils.isNotEmpty(httpProxyPort)) {
                LOGGER.info("Using proxy port : {}", httpProxyPort);
                System.setProperty("http.proxyPort", httpProxyPort);
                System.setProperty("https.proxyPort", httpProxyPort);
            }
            if (StringUtils.isNotEmpty(noProxy)) {
                LOGGER.info("No proxy : {}", noProxy);
                System.setProperty("http.nonProxyHosts", noProxy.replace(",", "|"));
                System.setProperty("no_proxy", noProxy);
            }
            if (StringUtils.isNotEmpty(proxyUsername)) {
                LOGGER.info("Proxy username  : {}", proxyUsername);
                System.setProperty("http.proxyUser", proxyUsername);
                if (StringUtils.isNotEmpty(proxyPassword)) {
                    System.setProperty("http.proxyPassword", proxyPassword);
                }
            }
        }
    }
}
