package fr.insee.onyxia.api.configuration;

import fr.insee.onyxia.model.region.Region;
import io.micrometer.common.util.StringUtils;
import java.io.File;
import java.io.IOException;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientProvider {

    @Value("${http.cacheEnabled}")
    private boolean cacheEnabled;

    @Value("${http.cacheMaxSizeMB}")
    private Integer cacheMaxSizeMB;

    @Value("${http.overrideCacheLocation}")
    private String overrideCacheLocation;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientProvider.class);

    @Bean
    public OkHttpClient httpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        enableDebugFeatureIfEnabled(builder);
        return builder.build();
    }

    @Bean
    @Qualifier("withCache")
    public OkHttpClient httpClientWithCache() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        enableDebugFeatureIfEnabled(builder);
        if (cacheEnabled) {
            File httpCacheDirectory;
            if (StringUtils.isNotBlank(overrideCacheLocation)) {
                httpCacheDirectory = new File(overrideCacheLocation);
            } else {
                httpCacheDirectory = new File(System.getProperty("java.io.tmpdir"), "http_cache");
            }
            LOGGER.info("Caching helm packages at {}", httpCacheDirectory.getAbsolutePath());
            long cacheSize = cacheMaxSizeMB * 1024L * 1024L;
            Cache cache = new Cache(httpCacheDirectory, cacheSize);
            builder.cache(cache);
        }
        return builder.build();
    }

    public OkHttpClient getClientForRegion(Region region) {
        OkHttpClient.Builder builder =
                new OkHttpClient.Builder()
                        .addInterceptor(
                                new Interceptor() {
                                    @NotNull
                                    @Override
                                    public Response intercept(@NotNull Chain chain)
                                            throws IOException {
                                        Request request = chain.request();
                                        Request newRequest = request;

                                        Region.Auth auth =
                                                region.getServices().getServer().getAuth();

                                        if (auth != null && auth.getToken() != null) {
                                            newRequest =
                                                    newRequest
                                                            .newBuilder()
                                                            .addHeader(
                                                                    "Authorization",
                                                                    "token=" + auth.getToken())
                                                            .build();
                                        }

                                        if (auth != null && auth.getUsername() != null) {
                                            String credentials =
                                                    Credentials.basic(
                                                            auth.getUsername(), auth.getPassword());
                                            newRequest =
                                                    newRequest
                                                            .newBuilder()
                                                            .addHeader("Authorization", credentials)
                                                            .build();
                                        }

                                        return chain.proceed(newRequest);
                                    }
                                });

        enableDebugFeatureIfEnabled(builder);

        return builder.build();
    }

    private void enableDebugFeatureIfEnabled(OkHttpClient.Builder builder) {
        // Currently disabled
        if (false) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheMaxSizeMB(Integer cacheMaxSizeMB) {
        this.cacheMaxSizeMB = cacheMaxSizeMB;
    }

    public Integer getCacheMaxSizeMB() {
        return cacheMaxSizeMB;
    }

    public void setOverrideCacheLocation(String overrideCacheLocation) {
        this.overrideCacheLocation = overrideCacheLocation;
    }

    public String getOverrideCacheLocation() {
        return overrideCacheLocation;
    }
}
