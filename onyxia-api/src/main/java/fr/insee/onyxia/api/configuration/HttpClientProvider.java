package fr.insee.onyxia.api.configuration;

import fr.insee.onyxia.model.region.Region;
import fr.insee.onyxia.model.region.Server;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class HttpClientProvider {

    @Bean
    public OkHttpClient httpClient(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        enableDebugFeatureIfEnabled(builder);
        return builder.build();
    }

    public OkHttpClient getClientForRegion(Region region) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Chain chain) throws IOException {
                Request request = chain.request();
                Request newRequest = request;

                Server.Auth auth = region.getServices().getServer().getAuth();

                if (auth != null && auth.getToken() != null) {
                    newRequest = newRequest.newBuilder()
                            .addHeader("Authorization", "token="+auth.getToken())
                            .build();
                }

                if (auth != null && auth.getUsername() != null) {
                    String credentials = Credentials.basic(auth.getUsername(),auth.getPassword());
                    newRequest = newRequest.newBuilder()
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
}